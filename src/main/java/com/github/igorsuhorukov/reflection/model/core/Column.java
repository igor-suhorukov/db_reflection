package com.github.igorsuhorukov.reflection.model.core;

import lombok.Value;

@Value
public class Column {
    String column;
    String transformation;
    String filterPredicate;

    public Column(String column) {
        this.column = column;
        this.transformation = null;
        this.filterPredicate = null;
    }

    public Column(String column, String transformation, String filterPredicate) {
        this.column = column;
        this.transformation = transformation;
        this.filterPredicate = filterPredicate;
    }
}
