package com.github.igorsuhorukov.reflection.model.core;

import lombok.Value;

@Value
public class Query {
    Table table;
    String query;
}
