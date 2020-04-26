package com.github.igorsuhorukov.reflection.service;

import com.github.igorsuhorukov.reflection.model.copy.Partition;
import com.github.igorsuhorukov.reflection.model.copy.Settings;
import com.github.igorsuhorukov.reflection.model.copy.TableSettings;
import com.github.igorsuhorukov.reflection.model.core.Query;
import com.github.igorsuhorukov.reflection.model.core.Table;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class QueryGenerator {

    public List<Query> generateStatQuery(List<Table> tables, TableSettings tableSettings){
        Set<String> partitionedTables = tableSettings.keySet();//validate non null partitions
        return tables.stream().filter(table ->
                partitionedTables.contains(table.getTable())).map(table -> {
            Settings settings = tableSettings.get(table.getTable());
            //settings.getPartitions().stream().map(partition -> )
            StringBuilder query = new StringBuilder().
                    append("select ").
                        append(generatePartitionsQueryPart(true, settings.getPartitions())).
                    append(" from ");
            if(table.getSchema()!=null && !table.getSchema().isEmpty()){
                query.append(table.getSchema()).append('.');
            }
/*
            if(settings.getRefreshSettings()!=null && settings.getRefreshSettings().getIncrementalField()!=null){
                query.append(" where ").append(settings.getRefreshSettings().
                    getIncrementalField()).append(" > ").append(latestFetchedValue);
            }
*/
            query.append(table.getTable()).
                    append(" group by ").
                        append(generatePartitionsQueryPart(false, settings.getPartitions()));
            return new Query(table, query.toString());
        }).collect(Collectors.toList());
    }

    private String generatePartitionsQueryPart(boolean isColumnsPart, List<Partition> partitions) {
        final String partitionQueryPart = partitions.stream().map(partition -> {
            final String partitionName = isColumnsPart &&
                    partition.getPartitionName() != null && !partition.getPartitionName().isEmpty() ?
                    " as " + partition.getPartitionName() : "";
            if (partition.getTransformation() != null) {
                return String.format(partition.getTransformation(), partition.getColumn()) + partitionName;
            } else {
                return partition.getColumn() + partitionName;
            }
        }).collect(Collectors.joining(", "));
        return isColumnsPart ? partitionQueryPart + ", count(*) as part_rec_cnt" : partitionQueryPart;
    }

    public List<Query> generate(List<Table> tables){
        return generate(tables, null);
    }

    public List<Query> generate(List<Table> tables, TableSettings tableSettings){
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
            if(settings!=null && settings.getSorts()!=null && !settings.getSorts().isEmpty()){
                query.append(" order by ");
                String sortClause = settings.getSorts().stream().map(sort ->
                        sort.getColumn() + (sort.getOrder() != null ? " " + sort.getOrder() : "")).
                        collect(Collectors.joining(", "));
                query.append(sortClause);
            }
            return new Query(table, query.toString());
        }).collect(Collectors.toList());
    }
}
