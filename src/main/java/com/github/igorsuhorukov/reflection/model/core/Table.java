package com.github.igorsuhorukov.reflection.model.core;

import lombok.Value;

import java.util.List;

@Value
public class Table {
    String schema;
    String table;
    List<Column> columns;
}
