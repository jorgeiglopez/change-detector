package com.jorgeiglopez.changedetector.persistence.model.postgres;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class UpdateChange extends InsertChange {

    private final OldKeys oldkeys;

    @JsonCreator
    public UpdateChange(
            @JsonProperty(value = "kind", required = true) final String kindInput,
            @JsonProperty(value = "columnnames", required = true) final List<String> columnnamesInput,
            @JsonProperty(value = "columntypes", required = true) final List<String> columntypesInput,
            @JsonProperty(value = "table", required = true) final String tableInput,
            @JsonProperty(value = "columnvalues", required = true) final List<Object> columnvaluesInput,
            @JsonProperty(value = "schema", required = true) final String schemaInput,
            @JsonProperty(value = "oldkeys", required = true) final OldKeys oldkeysInput
    ) {
        super(kindInput, columnnamesInput, columntypesInput, tableInput, columnvaluesInput, schemaInput);
        this.oldkeys = oldkeysInput;
    }

    public OldKeys getOldkeys() {
        return oldkeys;
    }
}
