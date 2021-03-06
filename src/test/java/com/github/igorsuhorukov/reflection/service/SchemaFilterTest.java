package com.github.igorsuhorukov.reflection.service;

import com.github.igorsuhorukov.reflection.model.core.Table;
import com.github.igorsuhorukov.reflection.model.matcher.DbObjectFilter;
import org.junit.jupiter.api.Test;
import org.schemaspy.model.Database;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SchemaFilterTest {
    @Test
    void testAllTables() {
        Database database = Utils.getDatabase();
        List<Table> tables = new SchemaFilter().filterDatabaseObject(database, new
                DbObjectFilter(null, false,  null));
        assertThat(tables).hasSize(8);
    }
/*
        List<String> generate = new QueryGenerator().generate(tables);
        try (Connection connection = DriverManager.getConnection("jdbc:tc:postgresql:12.2:///test?TC_INITSCRIPT=demo.sql")){
                //DataSetExporter.getInstance().export(connection, new DataSetExportConfig().queryList(generate.toArray(new String[0])).outputFileName("rs.txt").dataSetFormat(DataSetFormat.XML));
            try (Statement statement = connection.createStatement()){
                for(String q: generate){
                    try (ResultSet resultSet = statement.executeQuery(q)){
                        while (resultSet.next()){
                            System.out.println(resultSet.getString(1));
                        }
                    }
                }
            }
        }

 */

    @Test
    void testAllTablesWithViews() {
        Database database = Utils.getDatabase();
        List<Table> tables = new SchemaFilter().filterDatabaseObject(database, new
                DbObjectFilter(null,true, null));
        assertThat(tables).hasSize(12);
    }
}
