package com.github.igorsuhorukov.reflection.service;

import com.github.igorsuhorukov.reflection.model.copy.Settings;
import com.github.igorsuhorukov.reflection.model.copy.Sort;
import com.github.igorsuhorukov.reflection.model.copy.TableSettings;
import com.github.igorsuhorukov.reflection.model.core.Query;
import com.github.igorsuhorukov.reflection.model.core.Table;
import com.github.igorsuhorukov.reflection.model.matcher.DbObjectFilter;
import org.junit.jupiter.api.Test;
import org.schemaspy.model.Database;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryGeneratorTest {
    @Test
    void testSort() {
        Database database = Utils.getDatabase();
        List<Table> tables = new SchemaFilter().filterDatabaseObject(database, new
                DbObjectFilter(null, null, true,null));
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
        final Optional<Query> flightQuery = generate.stream().filter(query -> flights.equals(query.getTable().getTable())).findFirst();
        assertThat(flightQuery.isPresent()).isTrue();
        assertThat(flightQuery.get().getQuery()).endsWith("order by scheduled_departure, departure_airport DESC");

        final Optional<Query> bookingsQuery = generate.stream().filter(query -> bookings.equals(query.getTable().getTable())).findFirst();
        assertThat(bookingsQuery.isPresent()).isTrue();
        assertThat(bookingsQuery.get().getQuery()).endsWith("order by book_date ASC");
    }
}
