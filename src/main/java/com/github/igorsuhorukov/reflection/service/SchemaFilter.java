package com.github.igorsuhorukov.reflection.service;

import com.github.igorsuhorukov.reflection.model.core.Column;
import com.github.igorsuhorukov.reflection.model.core.Table;
import com.github.igorsuhorukov.reflection.model.matcher.ColumnRule;
import com.github.igorsuhorukov.reflection.model.matcher.DbObjectFilter;
import org.schemaspy.model.Database;

import java.util.*;
import java.util.stream.Collectors;

public class SchemaFilter {

    public List<Table> filterDatabaseObject(Database database, DbObjectFilter objectFilter){
        if (filterSchema(database, objectFilter)){
            return Collections.emptyList();
        }
        List<org.schemaspy.model.Table> filteredTables = filterTable(database, objectFilter);

        if(objectFilter.getColumn()!=null && !objectFilter.getColumn().isEmpty()){
            return filterColumn(database, objectFilter, filteredTables);
        } else {
            //all columns as is
            return filteredTables.stream().map(table -> new Table(database.getSchema().getName(), table.getName(),
                        table.getColumns().stream().
                        map(tableColumn -> new Column(tableColumn.getName())).collect(Collectors.toList()))).
                    collect(Collectors.toList());
        }
    }

    private List<Table> filterColumn(Database database, DbObjectFilter objectFilter,
                                     List<org.schemaspy.model.Table> filteredTables) {
        return filteredTables.stream().map(table -> {
            List<ColumnRule> applicableColumnRules = objectFilter.getColumn().stream().filter(columnRule ->
                    columnRule.getTable().match(table.getName())).collect(Collectors.toList());
            List<Column> columns;
            if(applicableColumnRules.isEmpty()){
                //add as is
                columns = table.getColumnsMap().keySet().stream().map(Column::new).collect(Collectors.toList());
            } else {
                //filter columns
                columns = table.getColumnsMap().keySet().stream().map(columnName -> {
                    List<ColumnRule> columnMatchedRule = applicableColumnRules.stream().filter(columnRule ->
                            columnRule.getTable().match(table.getName()) && columnRule.getColumn().match(columnName)).collect(Collectors.toList());
                    boolean excludeColumn = columnMatchedRule.stream().anyMatch(ColumnRule::isExclude);
                    if(excludeColumn){
                        return null;
                    } else {
                        Optional<ColumnRule> columnRule = columnMatchedRule.stream().findFirst();
                        return columnRule.map(rule -> new Column(columnName,
                                rule.getTransformation(), rule.getFilterPredicate())).orElse(new Column(columnName));
                    }

                }).filter(Objects::nonNull).collect(Collectors.toList());
            }

            return new Table(database.getSchema().getName(), table.getName(), columns);
        }).collect(Collectors.toList());
    }

    private boolean filterSchema(Database database, DbObjectFilter objectFilter) {
        boolean schemaMatch = objectFilter.getSchema()==null ||
                objectFilter.getSchema().match(database.getSchema().getName());
        return !schemaMatch;
    }

    private List<org.schemaspy.model.Table> filterTable(Database database, DbObjectFilter objectFilter) {
        Collection<org.schemaspy.model.Table> tables = new ArrayList<>(database.getTables());
        if(objectFilter.isIncludeView()){
            tables.addAll(database.getViews());
        }
        return tables.stream().
                filter(table -> objectFilter.getTable()==null || objectFilter.getTable().match(table.getName())).
                collect(Collectors.toList());
    }
}
