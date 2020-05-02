package com.github.igorsuhorukov.reflection.model.matcher;

import com.github.igorsuhorukov.reflection.model.matcher.filter.TableAndColumnFilter;
import lombok.Value;

@Value
public class DbObjectFilter {
    Rule schema;
    boolean includeView;
    TableAndColumnFilter tableAndColumnFilter;
    /*
    store schemas on endpoint basis(hierarchy) or as blob/jsonb - rdbms, xsd, json schema, avro schema in RDBMS?
     */
}
