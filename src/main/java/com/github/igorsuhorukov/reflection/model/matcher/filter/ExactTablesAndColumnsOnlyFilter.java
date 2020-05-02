package com.github.igorsuhorukov.reflection.model.matcher.filter;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.HashMap;
import java.util.Set;

@Value
@EqualsAndHashCode(callSuper = true)
public class ExactTablesAndColumnsOnlyFilter  extends HashMap<String, Set<String>> implements TableAndColumnFilter{
    boolean failOnMismatch;
}
