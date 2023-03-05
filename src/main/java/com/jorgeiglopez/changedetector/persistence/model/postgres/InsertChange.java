package com.jorgeiglopez.changedetector.persistence.model.postgres;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class InsertChange extends Change {
    private final List<Object> columnvalues;
    private final List<String> columnnames;
    private final List<String> columntypes;

    @JsonCreator
    public InsertChange(
            @JsonProperty(value = "kind", required = true) final String kindInput,
            @JsonProperty(value = "columnnames", required = true) final List<String> columnnamesInput,
            @JsonProperty(value = "columntypes", required = true) final List<String> columntypesInput,
            @JsonProperty(value = "table", required = true) final String tableInput,
            @JsonProperty(value = "columnvalues", required = true) final List<Object> columnvaluesInput,
            @JsonProperty(value = "schema", required = true) final String schemaInput
    ) {
        super(kindInput, tableInput, schemaInput);
        this.columnvalues = columnvaluesInput;
        this.columnnames = columnnamesInput;
        this.columntypes = columntypesInput;
    }

    public List<Object> getColumnvalues() {
        return columnvalues;
    }

    public List<String> getColumnnames() {
        return columnnames;
    }

    public List<String> getColumntypes() {
        return columntypes;
    }
}
