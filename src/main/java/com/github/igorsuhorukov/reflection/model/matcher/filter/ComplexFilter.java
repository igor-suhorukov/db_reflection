package com.github.igorsuhorukov.reflection.model.matcher.filter;

import com.github.igorsuhorukov.reflection.model.matcher.ColumnRule;
import com.github.igorsuhorukov.reflection.model.matcher.Rule;
import lombok.Value;

import java.util.List;

@Value
public class ComplexFilter implements TableAndColumnFilter{
    Rule table;
    List<ColumnRule> column;
}
