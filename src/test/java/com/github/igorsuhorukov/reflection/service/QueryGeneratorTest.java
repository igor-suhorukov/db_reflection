package com.github.igorsuhorukov.reflection.service;

import com.github.igorsuhorukov.reflection.model.copy.Settings;
import com.github.igorsuhorukov.reflection.model.copy.Sort;
import com.github.igorsuhorukov.reflection.model.copy.TableSettings;
import com.github.igorsuhorukov.reflection.model.core.Query;
import com.github.igorsuhorukov.reflection.model.core.Table;
import com.github.igorsuhorukov.reflection.model.matcher.ColumnRule;
import com.github.igorsuhorukov.reflection.model.matcher.DbObjectFilter;
import com.github.igorsuhorukov.reflection.model.matcher.Matcher;
import com.github.igorsuhorukov.reflection.model.matcher.Rule;
import com.github.igorsuhorukov.reflection.model.matcher.filter.ComplexFilter;
import com.github.igorsuhorukov.reflection.model.matcher.filter.ExactTablesAndColumnsOnlyFilter;
import org.junit.jupiter.api.Test;
import org.schemaspy.model.Database;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryGeneratorTest {
    @Test
    void testSort() {
        Database database = Utils.getDatabase();
        List<Table> tables = new SchemaFilter().filterDatabaseObject(database, new
                DbObjectFilter(null,  true,null));
        TableSettings tableSettings = new TableSettings();
        String bookings = "bookings";
        String flights = "flights";
        tableSettings.put(bookings, new Settings(null,Collections.singletonList(
                new Sort("book_date", Sort.Order.ASC)), null));
        tableSettings.put(flights, new Settings(null, Arrays.asList(
                new Sort("scheduled_departure", null),
                new Sort("departure_airport", Sort.Order.DESC)
        ), null));

        List<Query> generate = new QueryGenerator().generate(tables, tableSettings);
        final Optional<Query> flightQuery = generate.stream().filter(query ->
                                                            flights.equals(query.getTable().getTable())).findFirst();
        assertThat(flightQuery.isPresent()).isTrue();
        assertThat(flightQuery.get().getQuery()).endsWith("order by scheduled_departure, departure_airport DESC");

        final Optional<Query> bookingsQuery = generate.stream().filter(query ->
                                                            bookings.equals(query.getTable().getTable())).findFirst();
        assertThat(bookingsQuery.isPresent()).isTrue();
        assertThat(bookingsQuery.get().getQuery()).endsWith("order by book_date ASC");
    }

    @Test
    void testColumnFilter() throws Exception{
        Database database = Utils.getDatabase();
        final DbObjectFilter objectFilter = new
                DbObjectFilter(null, true, new ComplexFilter(null, Arrays.asList(
                ColumnRule.builder().table(new Matcher("bookings")).column(new Rule(new Matcher("book_date"))).filterPredicate("date_trunc('year',%s)>2018").build(),
                ColumnRule.builder().table(new Matcher("bookings")).column(new Rule(new Matcher("book_date"))).build(),
                ColumnRule.builder().table(new Matcher("bookings")).column(new Rule(new Matcher("book_ref"))).exclude(true).build()
        )));
        List<Table> tables = new SchemaFilter().filterDatabaseObject(database, objectFilter);
        TableSettings tableSettings = new TableSettings();
        String bookings = "bookings";
        tableSettings.put(bookings, new Settings(null,Collections.singletonList(
                new Sort("book_date", Sort.Order.ASC)), null));

        List<Query> generate = new QueryGenerator().generate(tables, tableSettings);

        final Optional<Query> bookingsQuery = generate.stream().filter(query -> bookings.equals(query.getTable().getTable())).findFirst();
        assertThat(bookingsQuery.isPresent()).isTrue();
        assertThat(bookingsQuery.get().getQuery().toLowerCase()).startsWith("select book_date, total_amount").contains("where date_trunc('year',book_date)>2018").endsWith("order by book_date asc");
    }

    @Test
    void testExactTableAndColumnFilter() {
        Database database = Utils.getDatabase();
        final ExactTablesAndColumnsOnlyFilter tableAndColumnFilter = new ExactTablesAndColumnsOnlyFilter();
        final String bookingsTable = "bookings";
        final String flightsTable = "flights";
        Set<String> bookings = new HashSet<>(Arrays.asList("book_ref", "book_date", "total_amount"));
        tableAndColumnFilter.put(bookingsTable, bookings);
        Set<String> flights = new HashSet<>(Arrays.asList("flight_id", "flight_no", "scheduled_departure",
                "scheduled_arrival", "departure_airport", "arrival_airport", "status", "aircraft_code"));
        tableAndColumnFilter.put(flightsTable, flights);

        final DbObjectFilter objectFilter = new
                DbObjectFilter(null, true, tableAndColumnFilter);

        List<Table> tables = new SchemaFilter().filterDatabaseObject(database, objectFilter);
        Table table1 = tables.get(0);
        assertThat(table1.getTable()).isEqualTo(bookingsTable);
        assertThat(table1.getColumns()).hasSize(bookings.size());
        Table table2 = tables.get(1);
        assertThat(table2.getTable()).isEqualTo(flightsTable);
        assertThat(table2.getColumns()).hasSize(flights.size());
    }
}
