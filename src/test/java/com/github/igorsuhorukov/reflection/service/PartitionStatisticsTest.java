package com.github.igorsuhorukov.reflection.service;

import com.github.igorsuhorukov.postgresql.PostgresqlService;
import com.github.igorsuhorukov.postgresql.model.FileWithArgs;
import com.github.igorsuhorukov.reflection.model.copy.Partition;
import com.github.igorsuhorukov.reflection.model.copy.Settings;
import com.github.igorsuhorukov.reflection.model.copy.TableSettings;
import com.github.igorsuhorukov.reflection.model.copy.refresh.RefreshSettings;
import com.github.igorsuhorukov.reflection.model.copy.refresh.RefreshType;
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
    void partitionClause() throws Exception{
        Database database = Utils.getDatabase();
        List<Table> tables = new SchemaFilter().filterDatabaseObject(database, new
                DbObjectFilter(null, true,null));
        final TableSettings tableSettings = new TableSettings();
        tableSettings.put("bookings", new Settings(Collections.singletonList(
                new Partition("book_date_part", "book_date",
                        "date_trunc('month', %s)")),null, null));
        tableSettings.put("flights", new Settings(Arrays.asList(
                new Partition("scheduled_departure_part","scheduled_departure",
                        "date_trunc('month', %s)::date"),
                new Partition("airport_part","departure_airport",null)
        ),null, null));
        List<Query> generate = new QueryGenerator().generateStatQuery(tables, tableSettings, false);

        ResultSetReader resultSetReader = new ResultSetReader();

        List<InMemoryResultSet> resultSets = new ArrayList<>();

        try (PostgresqlService postgresql = new PostgresqlService()){
            postgresql.setImportFromFileWithArgs(Optional.of(new FileWithArgs(
                    new File(SchemaFilterTest.class.getResource("/demo-small-20170815.sql").getFile()),null)));
            postgresql.start();
            try (Connection connection = DriverManager.getConnection(postgresql.getJdbcConnectionUrl())){
                for(Query query: generate) {
                    resultSets.add(resultSetReader.getQueryResultSet(connection, query));
                }
            }
        }
        final InMemoryResultSet[] sourceStatistics = resultSets.toArray(new InMemoryResultSet[0]);
        String statisticsJson = new ResultSetSerializer().toJson(sourceStatistics);
        assertThat(statisticsJson).isEqualToIgnoringWhitespace(expectedJson());
    }

    @Test
    void partitionClauseRefreshRule() {
        Database database = Utils.getDatabase();
        List<Table> tables = new SchemaFilter().filterDatabaseObject(database, new
                DbObjectFilter(null, true,null));
        String bookings = "bookings";
        String flights = "flights";
        final TableSettings tableSettings = new TableSettings();
        tableSettings.put(bookings, new Settings(Collections.singletonList(
                new Partition("book_date_part", "book_date",
                        "date_trunc('month', %s)")),null, new RefreshSettings(RefreshType.INCREMENTAL, "",0L,"book_ref")));
        tableSettings.put(flights, new Settings(Arrays.asList(
                new Partition("scheduled_departure_part","scheduled_departure",
                        "date_trunc('month', %s)::date"),
                new Partition("airport_part","departure_airport",null)
        ),null, new RefreshSettings(RefreshType.INCREMENTAL, "",0L,"actual_departure")));

        List<Query> generate = new QueryGenerator().generateStatQuery(tables, tableSettings, true);

        final Optional<Query> flightQuery = generate.stream().filter(query ->
                flights.equals(query.getTable().getTable())).findFirst();
        assertThat(flightQuery.isPresent()).isTrue();
        assertThat(flightQuery.get().getQuery()).isEqualTo("select date_trunc('month', scheduled_departure)::date as scheduled_departure_part, departure_airport as airport_part, count(*) as part_rec_cnt, min(actual_departure) as min_incremental_field, max(actual_departure) as max_incremental_field from bookings.flights where actual_departure >= ?  group by date_trunc('month', scheduled_departure)::date, departure_airport");

        final Optional<Query> bookingsQuery = generate.stream().filter(query ->
                bookings.equals(query.getTable().getTable())).findFirst();
        assertThat(bookingsQuery.isPresent()).isTrue();
        assertThat(bookingsQuery.get().getQuery()).isEqualTo("select date_trunc('month', book_date) as book_date_part, count(*) as part_rec_cnt, min(book_ref) as min_incremental_field, max(book_ref) as max_incremental_field from bookings.bookings where book_ref >= ?  group by date_trunc('month', book_date)");
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
