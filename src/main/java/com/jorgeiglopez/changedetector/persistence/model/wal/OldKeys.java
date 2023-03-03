package com.jorgeiglopez.changedetector.persistence.model.wal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class OldKeys {
    private final List<String> keytypes;
    private final List<Object> keyvalues;
    private final List<String> keynames;

    @JsonCreator
    public OldKeys(
            @JsonProperty(value = "keytypes", required = true) final List<String> keytypesInput,
            @JsonProperty(value = "keyvalues", required = true) final List<Object> keyvaluesInput,
            @JsonProperty(value = "keynames", required = true) final List<String> keynamesInput
    ) {
        this.keytypes = keytypesInput;
        this.keyvalues = keyvaluesInput;
        this.keynames = keynamesInput;
    }

    public List<String> getKeytypes() {
        return keytypes;
    }

    public List<Object> getKeyvalues() {
        return keyvalues;
    }

    public List<String> getKeynames() {
        return keynames;
    }

    @JsonIgnore
    public List<String> getColumnnames() {
        return getKeynames();
    }

    @JsonIgnore
    public List<Object> getColumnvalues() {
        return getKeyvalues();
    }
}
