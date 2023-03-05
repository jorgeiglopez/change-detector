package com.jorgeiglopez.changedetector.service;

import com.jorgeiglopez.changedetector.configuration.PostgresConfiguration;
import com.jorgeiglopez.changedetector.configuration.ReplicationConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.PGConnection;
import org.postgresql.replication.LogSequenceNumber;
import org.postgresql.replication.PGReplicationConnection;
import org.postgresql.replication.PGReplicationStream;
import org.postgresql.util.PSQLException;

import java.nio.ByteBuffer;
import java.sql.*;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Slf4j
public class PostgresService implements AutoCloseable {

    private static final String alreadyExistsSqlState = "42710";

    private static final String currentlyRunningProcessOnSlotSqlState = "55006";

    private final Connection queryConnection;

    private final Connection streamingConnection;

    private final PGReplicationStream pgReplicationStream;

    public PostgresService(final PostgresConfiguration postgresConfiguration, final ReplicationConfiguration replicationConfiguration) throws SQLException {
        queryConnection = createConnection(postgresConfiguration.getUrl(), postgresConfiguration.getQueryConnectionProperties());
        streamingConnection = createConnection(postgresConfiguration.getUrl(), postgresConfiguration.getReplicationProperties());
        log.debug("Connected to postgres!");

        PGConnection pgConnection = streamingConnection.unwrap(PGConnection.class);
        PGReplicationConnection pgReplicationConnection = pgConnection.getReplicationAPI();
        try {
            log.info("Attempting to create replication slot {}", replicationConfiguration.getSlotName());

            String sql = "SELECT * FROM pg_create_logical_replication_slot(?, ?)";

            try (PreparedStatement statement = queryConnection.prepareStatement(sql)) {
                statement.setString(1, replicationConfiguration.getSlotName());
                statement.setString(2, replicationConfiguration.getOutputPlugin());
                ResultSet resultSet = statement.executeQuery();
                if(!resultSet.wasNull()) {
                    resultSet.next();
                    log.info("--> Created replication slot: {}:{}", resultSet.getString(1), resultSet.getString(2));
                }
            }
        } catch (SQLException e) {
            if (e.getSQLState().equals(alreadyExistsSqlState)) {
                log.info("Slot {} already exists", replicationConfiguration.getSlotName());
            } else {
                throw (e);
            }
        }
        pgReplicationStream = getPgReplicationStream(replicationConfiguration, pgReplicationConnection);
    }

    public PGReplicationStream getPgReplicationStream() {
        return pgReplicationStream;
    }

    public ByteBuffer readPending() throws SQLException {
        return pgReplicationStream.readPending();
    }

    public LogSequenceNumber getCurrentLSN() throws SQLException {
        try (Statement st = queryConnection.createStatement()) {
            try (ResultSet rs = st.executeQuery("select pg_current_wal_lsn()")) {
                if (rs.next()) {
                    String lsn = rs.getString(1);
                    return LogSequenceNumber.valueOf(lsn);
                } else {
                    return LogSequenceNumber.INVALID_LSN;
                }
            }
        }
    }

    public void setStreamLsn(final LogSequenceNumber lsn) {
        pgReplicationStream.setAppliedLSN(lsn);
        pgReplicationStream.setFlushedLSN(lsn);
    }

    public LogSequenceNumber getLastReceivedLsn() {
        return pgReplicationStream.getLastReceiveLSN();
    }

    @Override
    public void close() {
        if (pgReplicationStream != null) {
            try {
                if (!pgReplicationStream.isClosed()) {
                    pgReplicationStream.forceUpdateStatus();
                    pgReplicationStream.close();
                }
            } catch (SQLException sqlException) {
                log.error("Unable to close replication stream", sqlException);
            }
        }
        if (streamingConnection != null) {
            try {
                streamingConnection.close();
            } catch (SQLException sqlException) {
                log.error("Unable to close postgres streaming connection", sqlException);
            }
        }
        if (queryConnection != null) {
            try {
                queryConnection.close();
            } catch (SQLException sqlException) {
                log.error("Unable to close postgres query connection", sqlException);
            }
        }
    }
    private PGReplicationStream getPgReplicationStream(
            final ReplicationConfiguration replicationConfiguration,
            final PGReplicationConnection pgReplicationConnection)
            throws SQLException {
        boolean listening = false;
        int tries = replicationConfiguration.getExisitingProcessRetryLimit();
        PGReplicationStream pgRepStream = null;
        while (!listening && tries > 0) {
            try {
                pgRepStream = getPgReplicationStreamHelper(replicationConfiguration, pgReplicationConnection);
                listening = true;
            } catch (PSQLException psqlException) {
                if (psqlException.getSQLState().equals(currentlyRunningProcessOnSlotSqlState)) {
                    log.info("Replication slot currently has another process consuming from it");
                    tries--;
                    if (tries > 0) {
                        log.info("Sleeping for {} seconds before retrying {} more times",
                                replicationConfiguration.getExistingProcessRetrySleepSeconds(), tries, psqlException);
                        try {
                            Thread.sleep(TimeUnit.SECONDS.toMillis(replicationConfiguration.getExistingProcessRetrySleepSeconds()));
                        } catch (InterruptedException ie) {
                            log.info("Received interruption while attempting to setup replciation stream");
                            tries = 0;
                        }
                    }
                } else {
                    throw psqlException;
                }
            }
        }
        return pgRepStream;
    }

    private PGReplicationStream getPgReplicationStreamHelper(
            final ReplicationConfiguration replicationConfiguration,
            final PGReplicationConnection pgReplicationConnection)
            throws SQLException {
        return pgReplicationConnection
                .replicationStream()
                .logical()
                .withStatusInterval(replicationConfiguration
                                .getStatusIntervalValue(),
                        replicationConfiguration.getStatusIntervalTimeUnit())
                .withSlotOptions(replicationConfiguration.getSlotOptions())
                .withSlotName(replicationConfiguration.getSlotName()).start();
    }

    Connection createConnection(final String url, final Properties properties) throws SQLException {
        log.debug("Connecting to: {}", url);
        return DriverManager.getConnection(url, properties);
    }
}
