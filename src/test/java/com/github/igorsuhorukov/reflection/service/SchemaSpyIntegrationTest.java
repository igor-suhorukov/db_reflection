package com.github.igorsuhorukov.reflection.service;

import com.github.igorsuhorukov.postgresql.PostgresqlService;
import com.github.igorsuhorukov.postgresql.model.FileWithArgs;
import com.github.igorsuhorukov.reflection.model.copy.Settings;
import com.github.igorsuhorukov.reflection.model.copy.Sort;
import com.github.igorsuhorukov.reflection.model.copy.TableSettings;
import com.github.igorsuhorukov.reflection.model.core.Query;
import com.github.igorsuhorukov.reflection.model.matcher.DbObjectFilter;
import org.junit.jupiter.api.Test;
import org.schemaspy.Config;
import org.schemaspy.input.dbms.service.*;
import org.schemaspy.input.dbms.xml.SchemaMeta;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.model.Table;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.Clock;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class SchemaSpyIntegrationTest {

    @Test
    void schemaspy() throws Exception{
        String databaseName = "test";
        String catalogName = "%";
        String schemaName = "bookings";
        try (PostgresqlService postgresql = new PostgresqlService()){
            postgresql.setDatabaseName(Optional.of(databaseName
            ));
            postgresql.setImportFromFileWithArgs(Optional.of(new FileWithArgs(
                    new File(SchemaFilterTest.class.getResource("/demo-small-20170815.sql").getFile()),null)));
            postgresql.start();
            try (Connection connection = DriverManager.getConnection(postgresql.getJdbcConnectionUrl())){
                String[] args = {
                        "-t", "pgsql",
                        "-db", databaseName,
                        "-s", schemaName,
                        "-cat", catalogName,
                        //"-o", "target/integrationtesting/pgsqlcheckconstraint",
                        //"-u", postgresql.getUsername(),
                        //"-p", postgresql.getPassword(),
                        //"-host", postgresql.getHost(),
                        //"-port", Integer.toString(postgresql.getPort())
                };
                SqlService sqlService = new SqlService();
                ColumnService columnService = new ColumnService(sqlService);
                IndexService indexService = new IndexService(sqlService);
                DatabaseService databaseService = new DatabaseService(Clock.systemDefaultZone(), sqlService,
                        new TableService(sqlService, columnService, indexService),
                        new ViewService(sqlService, columnService), new RoutineService(sqlService),
                        new SequenceService(sqlService));
                Config config = new Config(args);
                sqlService.connect(connection, config.isEvaluateAllEnabled());
                Database database = new Database(
                        sqlService.getDbmsMeta(),
                        databaseName,
                        catalogName,
                        schemaName
                );

                databaseService.gatherSchemaDetails(config, database, null/*TODO override comments new SchemaMeta(null, config.getDb(),schemaName)*/,
                        new ProgressListener() {
                    @Override
                    public long startedGatheringDetails() {
                        return 0;
                    }

                    @Override
                    public void gatheringDetailsProgressed(Table table) {

                    }

                    @Override
                    public long startedConnectingTables() {
                        return 0;
                    }

                    @Override
                    public void connectingTablesProgressed(Table table) {

                    }

                    @Override
                    public long startedGraphingSummaries() {
                        return 0;
                    }

                    @Override
                    public void graphingSummaryProgressed() {

                    }

                    @Override
                    public long startedGraphingDetails() {
                        return 0;
                    }

                    @Override
                    public void graphingDetailsProgressed(Table table) {

                    }

                    @Override
                    public long finishedGatheringDetails() {
                        return 0;
                    }

                    @Override
                    public long finished(Collection<Table> collection, Config config) {
                        return 0;
                    }

                    @Override
                    public String recoverableExceptionEncountered(String s, Exception e, String s1) {
                        return null;
                    }
                });

                List<com.github.igorsuhorukov.reflection.model.core.Table> tables = new SchemaFilter().filterDatabaseObject(database, new
                        DbObjectFilter(null,  true,null));
                TableSettings tableSettings = new TableSettings();
                String bookings = "bookings";
                String flights = "flights";
                tableSettings.put(bookings, new Settings(null, Collections.singletonList(
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
        }

    }
}
