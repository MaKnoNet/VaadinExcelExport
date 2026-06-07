package de.makno.vaadinexcelexport.app;

import de.makno.xlsxbuilder.builder.DataAccessException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Hält die Testdaten in einer <b>file-basierten H2-Datenbank</b> (nicht im JVM-Heap) und stellt
 * sie für die Grid-Anzeige (seitenweise) und den Excel-Export (out-of-core gestreamt) bereit.
 *
 * <p>Reines JDBC über {@link DriverManager} – der H2-Treiber registriert sich via ServiceLoader,
 * daher genügt {@code runtimeOnly}. Jede Instanz nutzt ein eigenes Temp-Verzeichnis und genau
 * <b>eine</b> persistente Verbindung (schnelles Paging, kein {@code AUTO_SERVER}-Hintergrund-Thread).
 *
 * <p><b>Thread-Sicherheit:</b> Die kurzen Operationen ({@link #seed}, {@link #count},
 * {@link #fetchPage}) sind {@code synchronized}. Der {@link #openStream Stream-Export} hält die
 * Verbindung exklusiv, bis das {@link StreamingResult} geschlossen wird – der Aufrufer muss
 * sicherstellen, dass währenddessen keine andere DB-Operation läuft (in der App durch den
 * Vaadin-Session-Lock garantiert, der Export und Grid-Paging serialisiert).
 */
final class TestDataDatabase implements AutoCloseable {

    /** Spaltenliste der Tabelle {@code testdata} – 1:1 zu {@link SampleRow}. */
    private static final String CREATE_TABLE =
            """
            CREATE TABLE IF NOT EXISTS testdata (
                id               BIGINT PRIMARY KEY,
                text             VARCHAR(255),
                ganzzahl         INT,
                grosse_zahl      BIGINT,
                gleitkomma       DOUBLE,
                betrag           DECIMAL(14,2),
                aktiv            BOOLEAN,
                datum            DATE,
                zeitstempel      TIMESTAMP,
                kommt_sekunden   INT,
                webseite         VARCHAR(255),
                webseite_name    VARCHAR(255)
            )""";

    private static final String INSERT_SQL =
            "INSERT INTO testdata (id, text, ganzzahl, grosse_zahl, gleitkomma, betrag, aktiv,"
                    + " datum, zeitstempel, kommt_sekunden, webseite, webseite_name)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final int BATCH_SIZE = 10_000;
    private static final SampleRowMapper MAPPER = new SampleRowMapper();

    private final Path directory;
    private final Connection connection;

    TestDataDatabase() {
        try {
            this.directory = Files.createTempDirectory("vaadinexcelexport-db-");
            Path base = directory.resolve("testdata");
            // Forward-Slashes im JDBC-URL → plattformneutral (Windows/Linux/macOS).
            String url = "jdbc:h2:file:" + base.toAbsolutePath().toString().replace('\\', '/');
            this.connection = DriverManager.getConnection(url);
            try (Statement st = connection.createStatement()) {
                st.execute(CREATE_TABLE);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Temp-Verzeichnis für H2 konnte nicht angelegt werden", e);
        } catch (SQLException e) {
            throw new DataAccessException("H2-Datenbank konnte nicht initialisiert werden", e);
        }
    }

    /**
     * Befüllt die Tabelle neu mit {@code count} deterministischen Zeilen (per Batch-Insert).
     *
     * @param count Anzahl Zeilen (≥ 0)
     */
    synchronized void seed(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count darf nicht negativ sein: " + count);
        }
        try {
            connection.setAutoCommit(false);
            try (Statement st = connection.createStatement()) {
                st.execute("TRUNCATE TABLE testdata");
            }
            try (PreparedStatement ps = connection.prepareStatement(INSERT_SQL)) {
                for (int i = 0; i < count; i++) {
                    bindRow(ps, i, SampleData.generateRow(i));
                    ps.addBatch();
                    if ((i + 1) % BATCH_SIZE == 0) {
                        ps.executeBatch();
                    }
                }
                ps.executeBatch();
            }
            connection.commit();
        } catch (SQLException e) {
            throw new DataAccessException("Seeding der Testdaten fehlgeschlagen", e);
        } finally {
            restoreAutoCommit();
        }
    }

    private void restoreAutoCommit() {
        try {
            connection.setAutoCommit(true);
        } catch (SQLException ignored) {
            // Verbindung evtl. schon geschlossen – unkritisch.
        }
    }

    private static void bindRow(PreparedStatement ps, long id, SampleRow r) throws SQLException {
        ps.setLong(1, id);
        ps.setString(2, r.text());
        ps.setInt(3, r.ganzzahl());
        ps.setLong(4, r.grosseZahl());
        ps.setDouble(5, r.gleitkomma());
        ps.setBigDecimal(6, r.betrag());
        ps.setBoolean(7, r.aktiv());
        ps.setObject(8, r.datum());
        ps.setObject(9, r.zeitstempel());
        ps.setInt(10, r.kommtSekunden());
        ps.setString(11, r.webseite());
        ps.setString(12, r.webseiteName());
    }

    /** Anzahl der Zeilen in der Tabelle. */
    synchronized long count() {
        try (Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM testdata")) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw new DataAccessException("Zählen der Testdaten fehlgeschlagen", e);
        }
    }

    /**
     * Liest eine Seite (für die lazy Grid-Anzeige). {@code orderBy} ist eine bereits validierte,
     * vollständige {@code ORDER BY …}-Klausel (siehe {@link SampleGrid}).
     */
    synchronized List<SampleRow> fetchPage(int offset, int limit, String orderBy) {
        String sql = "SELECT * FROM testdata " + orderBy + " LIMIT ? OFFSET ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                List<SampleRow> page = new ArrayList<>();
                while (rs.next()) {
                    page.add(MAPPER.map(rs));
                }
                return page;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lesen einer Datenseite fehlgeschlagen", e);
        }
    }

    /**
     * Öffnet einen <b>forward-only</b>-Stream über alle Zeilen (für den out-of-core-Export). Der
     * Aufrufer muss das Ergebnis schließen (try-with-resources) – das schließt {@code ResultSet}
     * und {@code Statement} (die geteilte Verbindung bleibt offen).
     *
     * @param orderBy   validierte {@code ORDER BY …}-Klausel (Reihenfolge der Excel-Zeilen)
     * @param fetchSize JDBC-Fetch-Size (Zeilen pro DB-Roundtrip)
     */
    synchronized StreamingResult openStream(String orderBy, int fetchSize) {
        try {
            Statement st = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            st.setFetchSize(Math.max(1, fetchSize));
            ResultSet rs = st.executeQuery("SELECT * FROM testdata " + orderBy);
            return new StreamingResult(st, rs);
        } catch (SQLException e) {
            throw new DataAccessException("Öffnen des Daten-Streams fehlgeschlagen", e);
        }
    }

    @Override
    public synchronized void close() {
        try {
            connection.close();
        } catch (SQLException ignored) {
            // bereits geschlossen
        }
        deleteDirectory(directory);
    }

    private static void deleteDirectory(Path directory) {
        try (Stream<Path> paths = Files.walk(directory)) {
            paths.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException ignored) {
                    // Best-effort-Cleanup; verbleibende Temp-Dateien räumt das OS auf.
                }
            });
        } catch (IOException ignored) {
            // Verzeichnis ggf. bereits weg – ignorieren.
        }
    }

    /**
     * Offener Daten-Stream: bündelt {@link Statement} und {@link ResultSet} und schließt beide in
     * {@link #close()}. Die geteilte {@link Connection} bleibt offen (gehört der
     * {@link TestDataDatabase}).
     */
    static final class StreamingResult implements AutoCloseable {

        private final Statement statement;
        private final ResultSet resultSet;

        StreamingResult(Statement statement, ResultSet resultSet) {
            this.statement = statement;
            this.resultSet = resultSet;
        }

        /** Das offene {@link ResultSet} (forward-only) für {@code DataProviders.ofResultSet(...)}. */
        ResultSet resultSet() {
            return resultSet;
        }

        @Override
        public void close() {
            try {
                resultSet.close();
            } catch (SQLException ignored) {
                // ggf. bereits vom xlsxbuilder geschlossen
            }
            try {
                statement.close();
            } catch (SQLException ignored) {
                // best effort
            }
        }
    }
}
