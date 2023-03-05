package com.jorgeiglopez.changedetector.service;

import com.jorgeiglopez.changedetector.configuration.PostgresConfiguration;
import com.jorgeiglopez.changedetector.configuration.ReplicationConfiguration;
import org.springframework.stereotype.Service;

@Service
public class ChangeListenerService implements PostgresConfiguration, ReplicationConfiguration {

    private final SlotReader slotReader;

    public ChangeListenerService() {
        this.slotReader = new SlotReader(this, this);
    }

    public void startService() {
        slotReader.startListener();
    }


    @Override public String getHost() {
        return "localhost";
    }

    @Override public String getDatabase() {
        return "db";
    }

    @Override public String getUsername() {
        return "user";
    }

    @Override public String getPassword() {
        return "pass";
    }

    @Override public String getSlotName() {
        return getDatabase() + "_slot";
    }
}
