package com.github.igorsuhorukov.reflection.model.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.List;

@Value
@EqualsAndHashCode
public class InMemoryResultSet {
    String schema;
    String table;
    ColumnMeta[] columnMetadata;
    List<Object[]> rows;

    @JsonCreator
    public InMemoryResultSet(@JsonProperty("schema") String schema, @JsonProperty("table") String table,
                             @JsonProperty("columnMetadata") ColumnMeta[] columnMetadata,
                             @JsonProperty("rows") List<Object[]> rows) {
        this.schema = schema;
        this.table = table;
        this.columnMetadata = columnMetadata;
        this.rows = rows;
    }

    @Value
    @EqualsAndHashCode
    public static class ColumnMeta {
        String name;
        String type;
        int typeId;
        int precision;
        int scale;

        @JsonCreator
        public ColumnMeta(@JsonProperty("name") String name, @JsonProperty("type") String type,
                          @JsonProperty("typeId") int typeId, @JsonProperty("precision") int precision,
                          @JsonProperty("scale") int scale) {
            this.name = name;
            this.type = type;
            this.typeId = typeId;
            this.precision = precision;
            this.scale = scale;
        }
    }
}
