package com.github.igorsuhorukov.reflection.service;

import com.github.igorsuhorukov.postgresql.PostgresqlService;
import com.github.igorsuhorukov.postgresql.model.FileWithArgs;
import com.github.igorsuhorukov.reflection.model.copy.Partition;
import com.github.igorsuhorukov.reflection.model.copy.Settings;
import com.github.igorsuhorukov.reflection.model.copy.TableSettings;
import com.github.igorsuhorukov.reflection.model.core.InMemoryResultSet;
import com.github.igorsuhorukov.reflection.model.core.Query;
import com.github.igorsuhorukov.reflection.model.core.Table;
import com.github.igorsuhorukov.reflection.model.matcher.DbObjectFilter;
import com.github.igorsuhorukov.reflection.serializer.ResultSetSerializer;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.schemaspy.model.Database;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class PartitionStatisticsTest {
    @Test
    void prepareDb() throws Exception{
        Database database = Utils.getDatabase();
        List<Table> tables = new SchemaFilter().filterDatabaseObject(database, new
                DbObjectFilter(null, null, true,null));
        final TableSettings tableSettings = new TableSettings();
        tableSettings.put("bookings", new Settings(Collections.singletonList(
                new Partition("book_date_part", "book_date",
                        "date_trunc('month', %s)")),null, null));
        tableSettings.put("flights", new Settings(Arrays.asList(
                new Partition("scheduled_departure_part","scheduled_departure",
                        "date_trunc('month', %s)::date"),
                new Partition("airport_part","departure_airport",null)
        ),null, null));

        List<Query> generate = new QueryGenerator().generateStatQuery(tables, tableSettings);

        Statistics statistics = new Statistics();

        List<InMemoryResultSet> resultSets = new ArrayList<>();

        try (PostgresqlService postgresql = new PostgresqlService()){
            postgresql.setImportFromFileWithArgs(Optional.of(new FileWithArgs(
                    new File(SchemaFilterTest.class.getResource("/demo-small-20170815.sql").getFile()),null)));
            postgresql.start();
            try (Connection connection = DriverManager.getConnection(postgresql.getJdbcConnectionUrl())){
                for(Query query: generate) {
                    resultSets.add(statistics.getStatistics(connection, query));
                }
            }
        }
        final InMemoryResultSet[] sourceStatistics = resultSets.toArray(new InMemoryResultSet[0]);
        String statisticsJson = new ResultSetSerializer().toJson(sourceStatistics);
        assertThat(statisticsJson).isEqualToIgnoringWhitespace(expectedJson());
    }

    @Test
    void testRoundtripSerialization() throws Exception{
        String sourceJson = expectedJson();
        ResultSetSerializer serializer = new ResultSetSerializer();
        InMemoryResultSet[] inMemoryResultSets = serializer.fromJson(sourceJson);
        String resultJson = serializer.toJson(inMemoryResultSets);
        assertThat(resultJson).isEqualToIgnoringWhitespace(sourceJson);
    }

    private String expectedJson() throws IOException {
        return IOUtils.toString(PartitionStatisticsTest.class.getResource("/partitionStatistics.json"), StandardCharsets.UTF_8);
    }
}
