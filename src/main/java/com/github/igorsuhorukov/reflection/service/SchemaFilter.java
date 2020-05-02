package com.github.igorsuhorukov.reflection.service;

import com.github.igorsuhorukov.reflection.model.core.Column;
import com.github.igorsuhorukov.reflection.model.core.Table;
import com.github.igorsuhorukov.reflection.model.matcher.ColumnRule;
import com.github.igorsuhorukov.reflection.model.matcher.DbObjectFilter;
import com.github.igorsuhorukov.reflection.model.matcher.filter.ComplexFilter;
import com.github.igorsuhorukov.reflection.model.matcher.filter.ExactTablesAndColumnsOnlyFilter;
import com.github.igorsuhorukov.reflection.model.matcher.filter.TableAndColumnFilter;
import org.schemaspy.model.Database;

import java.util.*;
import java.util.stream.Collectors;

public class SchemaFilter {

    public List<Table> filterDatabaseObject(Database database, DbObjectFilter objectFilter){
        if (filterSchema(database, objectFilter)){
            return Collections.emptyList();
        }
        TableAndColumnFilter filter = objectFilter.getTableAndColumnFilter();
        if(filter instanceof ExactTablesAndColumnsOnlyFilter){
            ExactTablesAndColumnsOnlyFilter tablesAndColumnsOnlyFilter = (ExactTablesAndColumnsOnlyFilter) filter;
            if(!tablesAndColumnsOnlyFilter.isEmpty()){
                return filterTableWithColumns(database, tablesAndColumnsOnlyFilter, objectFilter.isIncludeView());
            }
        }

        if(filter instanceof ComplexFilter && ((ComplexFilter)filter).getColumn()!=null
                && !((ComplexFilter)filter).getColumn().isEmpty()){
            final ComplexFilter complexFilter = (ComplexFilter) filter;
            return filterColumn(database, complexFilter, filterTable(database, complexFilter, objectFilter.isIncludeView()));
        } else {
            //all columns as is
            List<org.schemaspy.model.Table> filteredTables = filterTable(database, null, objectFilter.isIncludeView());
            return filteredTables.stream().map(table -> new Table(database.getSchema().getName(), table.getName(),
                        table.getColumns().stream().
                        map(tableColumn -> new Column(tableColumn.getName())).collect(Collectors.toList()))).
                    collect(Collectors.toList());
        }
    }

    private List<Table> filterTableWithColumns(Database database, ExactTablesAndColumnsOnlyFilter tablesAndColumns,
                                               boolean includeView) {
        Collection<org.schemaspy.model.Table> tables = getTablesAndViews(database, includeView);
        List<Table> matchedTables = tables.stream().filter(table -> tablesAndColumns.containsKey(table.getName())).
                map(table -> new Table(database.getSchema().getName(), table.getName(), table.getColumns().stream().
                        filter(tableColumn -> tablesAndColumns.get(table.getName()).contains(tableColumn.getName())).
                        map(tableColumn -> new Column(tableColumn.getName())).collect(Collectors.toList()))).
                //filter(table -> !table.getColumns().isEmpty()).
                collect(Collectors.toList());
        if(tablesAndColumns.isFailOnMismatch() && matchedTables.size()!=tablesAndColumns.size()){
            Set<String> tablesNotFound = new HashSet<>(tablesAndColumns.keySet());
            tablesNotFound.removeAll(matchedTables.stream().map(Table::getTable).collect(Collectors.toList()));
            throw new IllegalStateException("Following tables not found in database: " +
                                                String.join(", ", tablesNotFound));
        }
        if(tablesAndColumns.isFailOnMismatch()){
            List<Map.Entry<String, Set<String>>> columnMismatchList = compareColumns(tablesAndColumns, matchedTables);
            if(!columnMismatchList.isEmpty()){
                throw new IllegalStateException("Following columns isn't found in database: " + columnMismatchList);
            }
        }
        return matchedTables;
    }

    private List<Map.Entry<String, Set<String>>> compareColumns(ExactTablesAndColumnsOnlyFilter tablesAndColumns,
                                                                List<Table> matchedTables) {
        return matchedTables.stream().map(table -> {
            Set<String> columnsFromConfiguration = tablesAndColumns.get(table.getTable());
            if (columnsFromConfiguration.size() != table.getColumns().size()) {
                Set<String> columnsNotFound = new HashSet<>(columnsFromConfiguration);
                columnsNotFound.removeAll(table.getColumns().stream().
                        map(Column::getColumn).collect(Collectors.toList()));
                return new AbstractMap.SimpleImmutableEntry<>(table.getTable(), columnsNotFound);
            } else {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private List<Table> filterColumn(Database database, ComplexFilter filter,
                                     List<org.schemaspy.model.Table> filteredTables) {
        return filteredTables.stream().map(table -> {
            List<ColumnRule> applicableColumnRules = filter.getColumn().stream().filter(columnRule ->
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

    private List<org.schemaspy.model.Table> filterTable(Database database, ComplexFilter objectFilter, boolean includeView) {
        Collection<org.schemaspy.model.Table> tables = getTablesAndViews(database, includeView);
        return tables.stream().
                filter(table -> objectFilter==null || objectFilter.getTable()==null || objectFilter.getTable().match(table.getName())).
                collect(Collectors.toList());
    }

    private Collection<org.schemaspy.model.Table> getTablesAndViews(Database database, boolean includeView) {
        Collection<org.schemaspy.model.Table> tables = new ArrayList<>(database.getTables());
        if(includeView){
            tables.addAll(database.getViews());
        }
        return tables;
    }
}
