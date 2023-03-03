package com.jorgeiglopez.changedetector.persistence.model.wal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class DeleteChange extends Change {
    private final OldKeys oldkeys;

    @JsonCreator
    public DeleteChange(
            @JsonProperty(value = "kind", required = true) final String kindInput,
            @JsonProperty(value = "table", required = true) final String tableInput,
            @JsonProperty(value = "schema", required = true) final String schemaInput,
            @JsonProperty(value = "oldkeys", required = true) final OldKeys oldkeysInput
    ) {
        super(kindInput,  tableInput, schemaInput);
        this.oldkeys = oldkeysInput;
    }

    public OldKeys getOldkeys() {
        return oldkeys;
    }

    @Override
    @JsonIgnore
    public List<String> getColumnnames() {
        return oldkeys.getKeynames();
    }

    @Override
    @JsonIgnore
    public List<Object> getColumnvalues() {
        return oldkeys.getKeyvalues();
    }
}
