package com.github.igorsuhorukov.reflection.service;

import com.github.igorsuhorukov.reflection.model.copy.Settings;
import com.github.igorsuhorukov.reflection.model.copy.TableSettings;
import com.github.igorsuhorukov.reflection.model.core.Table;

import java.util.List;
import java.util.stream.Collectors;

public class QueryGenerator {

    public List<String> generate(List<Table> tables){
        return generate(tables, null);
    }

    public List<String> generate(List<Table> tables, TableSettings tableSettings){
        return tables.stream().filter(table -> !table.getColumns().isEmpty()).map(table -> {
            Settings settings = tableSettings!=null ? tableSettings.get(table.getTable()) : null;
            String columns = table.getColumns().stream().map(column -> {
                if (column.getTransformation() != null && !column.getTransformation().isEmpty()) {
                    return String.format(column.getTransformation(), column.getColumn()) + " as " + column.getColumn();
                } else {
                    return column.getColumn();
                }
            }).collect(Collectors.joining(", "));
            StringBuilder query = new StringBuilder().append("select ").append(columns);
            if(settings!=null && settings.getPartitions()!=null && !settings.getPartitions().isEmpty()){
                String partitionsColumns = settings.getPartitions().stream().map(partition -> {
                    if (partition.getTransformation() != null && partition.getTransformation().isEmpty()) {
                        return String.format(partition.getTransformation(), partition.getColumn()) + " as "
                                + partition.getPartitionName();
                    } else {
                        return partition.getColumn() + " as " + partition.getPartitionName();
                    }
                }).collect(Collectors.joining(", "));
                query.append(", ").append(partitionsColumns);
            }
            query.append(" from ");
            if(table.getSchema()!=null && !table.getSchema().isEmpty()){
                query.append(table.getSchema()).append('.');
            }
            query.append(table.getTable());
            return query.toString();
        }).collect(Collectors.toList());
    }
}
