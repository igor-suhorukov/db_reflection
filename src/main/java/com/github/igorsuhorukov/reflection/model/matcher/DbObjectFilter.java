package com.github.igorsuhorukov.reflection.model.matcher;

import lombok.Value;

import java.util.List;

@Value
public class DbObjectFilter {
    Rule schema;
    Rule table;
    boolean includeView;
    List<ColumnRule> column;
    /*
    store schemas on endpoint basis(hierarchy) or as blob/jsonb - rdbms, xsd, json schema, avro schema in RDBMS?
     */
}
