package com.github.igorsuhorukov.reflection.model.matcher;

import lombok.Value;

@Value
public class ColumnRule {
    Matcher table;
    Rule column;
    String transformation;
}
