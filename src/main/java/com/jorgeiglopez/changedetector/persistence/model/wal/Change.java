package com.jorgeiglopez.changedetector.persistence.model.wal;

import com.fasterxml.jackson.annotation.*;
import com.jorgeiglopez.changedetector.persistence.model.UnknownColumnNameException;

import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY,
              property = "kind", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = InsertChange.class, name = "insert"),
        @JsonSubTypes.Type(value = UpdateChange.class, name = "update"),
        @JsonSubTypes.Type(value = DeleteChange.class, name = "delete")
})
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public abstract class Change {
    private final String kind;
    private final String table;
    private final String schema;

    @JsonCreator
    public Change(
            @JsonProperty(value = "kind", required = true) final String kindInput,
            @JsonProperty(value = "table", required = true) final String tableInput,
            @JsonProperty(value = "schema", required = true) final String schemaInput
    ) {
        this.kind = kindInput;
        this.table = tableInput;
        this.schema = schemaInput;
    }

    public String getKind() {
        return kind;
    }

    public String getTable() {
        return table;
    }

    public String getSchema() {
        return schema;
    }

    public abstract List<String> getColumnnames();

    public abstract List<Object> getColumnvalues();

    public Object getValueForColumn(final String columnName) throws UnknownColumnNameException {
        int columnIndex = getColumnnames().indexOf(columnName);
        if (columnIndex != -1) {
            return getColumnvalues().get(columnIndex);
        } else {
            throw new UnknownColumnNameException(columnName);
        }
    }

    @Override public String toString() {
        return String.format("SCHEMA: [%s] - TABLE:[%s] - %s - ID:[%s: %s]", schema, table,
                kind.toUpperCase(), getColumnnames().get(0), getColumnvalues().get(0));
    }
}
