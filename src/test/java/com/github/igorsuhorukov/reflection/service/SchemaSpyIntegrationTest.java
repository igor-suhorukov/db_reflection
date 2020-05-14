package com.github.igorsuhorukov.reflection.service;

import com.github.igorsuhorukov.postgresql.PostgresqlService;
import com.github.igorsuhorukov.postgresql.model.FileWithArgs;
import com.github.igorsuhorukov.reflection.model.copy.Settings;
import com.github.igorsuhorukov.reflection.model.copy.Sort;
import com.github.igorsuhorukov.reflection.model.copy.TableSettings;
import com.github.igorsuhorukov.reflection.model.core.Query;
import com.github.igorsuhorukov.reflection.model.matcher.DbObjectFilter;
import lombok.SneakyThrows;
import org.h2.bnf.context.DbContents;
import org.h2.bnf.context.DbSchema;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.schemaspy.Config;
import org.schemaspy.input.dbms.service.*;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.model.Table;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Clock;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class SchemaSpyIntegrationTest {

    @Test
    void schemaspy() throws Exception{
        String databaseName = "test";
        String databaseType = "pgsql";
        String catalogName = "%";
        try (PostgresqlService postgresql = new PostgresqlService()){
            postgresql.setDatabaseName(Optional.of(databaseName
            ));
            postgresql.setImportFromFileWithArgs(Optional.of(new FileWithArgs(
                    new File(SchemaFilterTest.class.getResource("/demo-small-20170815.sql").getFile()),null)));
            postgresql.start();

            String jdbcConnectionUrl = postgresql.getJdbcConnectionUrl();
            try (Connection connection = DriverManager.getConnection(jdbcConnectionUrl)){

                SqlService sqlService = new SqlService();
                ColumnService columnService = new ColumnService(sqlService);
                IndexService indexService = new IndexService(sqlService);
                DatabaseService databaseService = new DatabaseService(Clock.systemDefaultZone(), sqlService,
                        new TableService(sqlService, columnService, indexService),
                        new ViewService(sqlService, columnService), new RoutineService(sqlService),
                        new SequenceService(sqlService));

                sqlService.connect(connection, true);

                List<DbSchema> availableSchemas = getAvailableSchemas(jdbcConnectionUrl, connection);

                List<Database> databases = availableSchemas.stream().map(dbSchema ->
                        getSchemaInfo(databaseName, catalogName, dbSchema.name, sqlService,
                                databaseService, databaseType)).collect(Collectors.toList());
                assertThat(databases).hasSize(1);
                Database database = databases.get(0);

                List<com.github.igorsuhorukov.reflection.model.core.Table> tables =
                        new SchemaFilter().filterDatabaseObject(database, new
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

    @NotNull
    private List<DbSchema> getAvailableSchemas(String jdbcConnectionUrl, Connection connection) throws SQLException {
        DbContents dbContents = new DbContents();
        dbContents.readContents(jdbcConnectionUrl, connection);
        Set<String> systemSchemas = new HashSet<>(Arrays.asList("information_schema", "pg_catalog"));
        return Arrays.stream(dbContents.getSchemas()).
                filter(dbSchema -> dbSchema.getTables().length > 0).
                filter(dbSchema -> !systemSchemas.contains(dbSchema.name)).collect(Collectors.toList());
    }

    @NotNull
    @SneakyThrows
    private Database getSchemaInfo(String databaseName, String catalogName, String schemaName,
                                   SqlService sqlService, DatabaseService databaseService, String databaseType) {
        String[] args = {
                "-t", databaseType,
                "-db", databaseName,
                "-s", schemaName,
                "-cat", catalogName,
                //"-o", "target/integrationtesting/pgsqlcheckconstraint",
                //"-u", postgresql.getUsername(),
                //"-p", postgresql.getPassword(),
                //"-host", postgresql.getHost(),
                //"-port", Integer.toString(postgresql.getPort())
        };
        Config config = new Config(args);

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
        return database;
    }
}
