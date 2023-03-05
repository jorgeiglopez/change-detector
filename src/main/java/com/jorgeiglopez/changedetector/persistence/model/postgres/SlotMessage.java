package com.jorgeiglopez.changedetector.persistence.model.postgres;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SlotMessage {
    private final long xid;
    private final List<Change> change;

    @JsonCreator
    public SlotMessage(
            @JsonProperty(value = "xid", required = true) final long xidInput,
            @JsonProperty(value = "change", required = true) final List<Change> changeInput
    ) {
        this.xid = xidInput;
        this.change = changeInput;
    }

    public long getXid() {
        return xid;
    }

    public List<Change> getChange() {
        return change;
    }

    @Override public String toString() {
        String result = "(" + change.size() + ") ";
        for(Change c : change) {
            result += c.toString() + " _*_ ";
        }
        return result;
    }
}
