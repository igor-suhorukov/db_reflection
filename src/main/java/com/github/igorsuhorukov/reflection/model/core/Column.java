package com.github.igorsuhorukov.reflection.model.core;

import lombok.Value;

@Value
public class Column {
    String column;
    String transformation;

    public Column(String column) {
        this.column = column;
        this.transformation = null;
    }

    public Column(String column, String transformation) {
        this.column = column;
        this.transformation = transformation;
    }
}
