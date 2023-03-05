package com.jorgeiglopez.changedetector.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jorgeiglopez.changedetector.configuration.PostgresConfiguration;
import com.jorgeiglopez.changedetector.configuration.ReplicationConfiguration;
import com.jorgeiglopez.changedetector.persistence.model.postgres.SlotMessage;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.replication.LogSequenceNumber;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SlotReader {

    protected static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String recoveryModeSqlState = "57P03";

    private static final int recoveryModeSleepMillis = 5000;

    private final PostgresConfiguration postgresConfiguration;

    private final ReplicationConfiguration replicationConfiguration;

    private long lastFlushedTime;

    public SlotReader(
            final PostgresConfiguration postgresConfigurationInput,
            final ReplicationConfiguration replicationConfigurationInput) {
        this.postgresConfiguration = postgresConfigurationInput;
        this.replicationConfiguration = replicationConfigurationInput;
    }

    public void startListener() {
        while (true) {
            readSlotWriteToLogs();
        }
    }

    public void readSlotWriteToLogs() {
        try (PostgresService postgresConnector = createPostgresConnector(postgresConfiguration, replicationConfiguration)) {
            resetIdleCounter();
            log.info("Consuming from slot {}", replicationConfiguration.getSlotName());
            while (true) {
                readMessageSlotHelper(postgresConnector);
            }
        } catch (SQLException sqlException) {
            log.error("Received the following error pertaining to the " + "replication stream, reattempting...", sqlException);
            if (sqlException.getSQLState().equals(recoveryModeSqlState)) {
                log.info("Sleeping for five seconds");
                try {
                    Thread.sleep(recoveryModeSleepMillis);
                } catch (InterruptedException ie) {
                    log.error("Interrupted while sleeping", ie);
                }
            }
        } catch (IOException ioException) {
            log.error("Received an IO Exception while processing the " + "replication stream, reattempting...", ioException);
        } catch (Exception e) {
            log.error("Received exception of type {}", e.getClass().toString(), e);
        }
    }

    private void readMessageSlotHelper(final PostgresService postgresConnector) throws SQLException, IOException {
        ByteBuffer msg = postgresConnector.readPending();
        if (msg != null) {
            log.debug("--> 1.Something to read: {}", msg);
            processByteBuffer(msg, postgresConnector);

        } else if (System.currentTimeMillis() - lastFlushedTime > TimeUnit.SECONDS
                .toMillis(replicationConfiguration.getUpdateIdleSlotInterval())) {
            log.debug("xxx Nothing to read: last flushed {}", System.currentTimeMillis() - lastFlushedTime);
            LogSequenceNumber lsn = postgresConnector.getCurrentLSN();
            msg = postgresConnector.readPending();
            if (msg != null) {
                processByteBuffer(msg, postgresConnector);
            }

            log.info("Fast forwarding stream lsn to {} due to stream " + "inactivity", lsn.toString()); // LSN due to inactivity - 5min
            postgresConnector.setStreamLsn(lsn);
            resetIdleCounter();
        }
    }

    private void processByteBuffer(final ByteBuffer msg, final PostgresService postgresConnector) throws IOException {
        log.debug("Processing chunk from WAL");
        int offset = msg.arrayOffset();
        byte[] source = msg.array();
        SlotMessage slotMessage = getSlotMessage(source, offset); //    TODO: mo0ve to slotMessageRepository
        if (slotMessage.getChange().size() > 0) {
            log.info("--->{}", slotMessage);
            postgresConnector.setStreamLsn(postgresConnector.getLastReceivedLsn());
            log.debug("After read, LNS updated to: {}", postgresConnector.getLastReceivedLsn());
        } else {
            log.warn("Empty slot: slotMessage.getChange().size() = 0");
        }

    }

//    TODO: move to slotMessageRepository
    private SlotMessage getSlotMessage(final byte[] walChunk, final int offset) throws IOException {
        SlotMessage slotMessage = objectMapper.readValue(walChunk, offset, walChunk.length, SlotMessage.class);
        Set<String> relevantTables = replicationConfiguration.getRelevantTables();

        if (relevantTables != null) {
            slotMessage.getChange().removeIf(change -> !relevantTables.contains(change.getTable()));
        }

        return slotMessage;
    }

    public void resetIdleCounter() {
        lastFlushedTime = System.currentTimeMillis();
    }

    private PostgresService createPostgresConnector(final PostgresConfiguration pc, final ReplicationConfiguration rc) throws SQLException {
        return new PostgresService(pc, rc);
    }

}
