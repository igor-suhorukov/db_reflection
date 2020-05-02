package com.github.igorsuhorukov.reflection.model.matcher.filter;

import lombok.Value;

import java.util.HashMap;
import java.util.Set;

@Value
public class ExactTablesAndColumnsOnlyFilter  extends HashMap<String, Set<String>> implements TableAndColumnFilter{
}
