package com.github.igorsuhorukov.reflection.model.matcher;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ColumnRule {
    Matcher table;
    Rule column;
    boolean exclude;
    String transformation;
    String filterPredicate;
}
