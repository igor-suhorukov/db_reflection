package com.github.igorsuhorukov.reflection.service;

import com.github.igorsuhorukov.reflection.model.core.InMemoryResultSet;
import com.github.igorsuhorukov.reflection.model.core.Query;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Statistics {

    public InMemoryResultSet getStatistics(Connection connection, Query query)
            throws SQLException {
        try (Statement statement = connection.createStatement()){
            try (ResultSet resultSet = statement.executeQuery(query.getQuery())){
                final ResultSetMetaData metaData = resultSet.getMetaData();
                final int columnCount = metaData.getColumnCount();
                InMemoryResultSet.ColumnMeta[] columnMetas = readColumnMetadata(metaData, columnCount);
                List<Object[]> rows = new ArrayList<>();
                readRows(resultSet, columnCount, rows);
                return new InMemoryResultSet(
                        query.getTable().getSchema(), query.getTable().getTable(), columnMetas, rows);
            }
        }
    }

    private static void readRows(ResultSet resultSet, int columnCount, List<Object[]> rows) throws SQLException {
        while (resultSet.next()){
            Object[] row = new Object[columnCount];
            for(int idx=1; idx <= columnCount; idx++){
                row[idx-1]=resultSet.getObject(idx);
            }
            rows.add(row);
        }
    }

    private static InMemoryResultSet.ColumnMeta[] readColumnMetadata(
                                                    ResultSetMetaData metaData, int columnCount) throws SQLException {
        InMemoryResultSet.ColumnMeta[] columnMetas =
                                new InMemoryResultSet.ColumnMeta[columnCount];
        for(int idx=1; idx <= columnCount; idx++){
            String columnName = metaData.getColumnName(idx);
            String typeName = metaData.getColumnTypeName(idx);
            int typeId = metaData.getColumnType(idx);
            InMemoryResultSet.ColumnMeta columnMeta =
                    new InMemoryResultSet.ColumnMeta(
                            columnName, typeName, typeId, metaData.getPrecision(idx), metaData.getScale(idx));
            columnMetas[idx-1]= columnMeta;
        }
        return columnMetas;
    }
}
