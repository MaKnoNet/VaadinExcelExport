package de.makno.vaadinexcelexport.app;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.flowingcode.vaadin.addons.gridexporter.GridExporter;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletService;
import com.vaadin.flow.server.VaadinSession;
import de.makno.vaadinexcelexport.GridExcelExporter;
import de.makno.xlsxbuilder.DataProviders;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Performance-Vergleich der beiden Excel-Export-Wege – beide lesen aus der <b>SQL-Datenbank</b>
 * ({@link TestDataDatabase}, file-basierte H2), genau wie die App:
 *
 * <ul>
 *   <li><b>xlsxbuilder</b> – {@link GridExcelExporter} streamt out-of-core aus einem forward-only
 *       JDBC-{@code ResultSet} ({@code DataProviders.ofResultSet}).</li>
 *   <li><b>Flowingcode</b> – der Community-{@link GridExporter} liest über das lazy, seitenweise
 *       Grid (das wiederum aus der DB paginiert).</li>
 * </ul>
 *
 * <p>Beide Engines werden mit Warmup- und Messläufen vermessen; das Ergebnis (Median/Durchschnitt,
 * Durchsatz, Dateigröße) wird als Tabelle ausgegeben. <b>Bewusst keine Zeit-Assertions</b> (absolute
 * Laufzeiten sind maschinen-/plattformabhängig); geprüft wird nur die Korrektheit (valide,
 * vollständige {@code .xlsx}). Markiert mit {@code @Tag("benchmark")} → läuft nur über die
 * Gradle-Task {@code benchmark}. Zeilenzahl via {@code -Dbenchmark.rows=N}.
 */
@Tag("benchmark")
class ExcelExporterBenchmarkTest {

    private static final String ENGINE_XLSBUILDER = "xlsxbuilder";
    private static final String ENGINE_FLOWINGCODE = "Flowingcode";

    private static final String ROW_COUNT_PROPERTY = "benchmark.rows";
    private static final int DEFAULT_ROW_COUNT = 25_000;
    private static final int WARMUP_ITERATIONS = 2;
    private static final int MEASURED_ITERATIONS = 5;
    private static final int FETCH_SIZE = 1_000;

    private static final double NANOS_PER_MILLI = 1_000_000.0;
    private static final double NANOS_PER_SECOND = 1_000_000_000.0;

    private static final SampleRowMapper MAPPER = new SampleRowMapper();

    /** Eine Export-Engine: schreibt den aktuellen DB-Inhalt als {@code .xlsx} in den Stream. */
    @FunctionalInterface
    private interface ExportEngine {
        void export(OutputStream out) throws IOException;
    }

    /** Messergebnis einer Engine über alle Messläufe. */
    private record BenchmarkResult(String engine, long medianNanos, long avgNanos, int outputBytes) {

        double medianMillis() {
            return medianNanos / NANOS_PER_MILLI;
        }

        double avgMillis() {
            return avgNanos / NANOS_PER_MILLI;
        }

        double rowsPerSecond(int rows) {
            return rows / (medianNanos / NANOS_PER_SECOND);
        }
    }

    @Test
    void comparesXlsbuilderAndFlowingcodeExportPerformance() throws Exception {
        int rowCount = Integer.getInteger(ROW_COUNT_PROPERTY, DEFAULT_ROW_COUNT);

        try (TestDataDatabase db = new TestDataDatabase()) {
            db.seed(rowCount);
            Grid<SampleRow> grid = buildGrid(db);

            ExportEngine xlsxbuilderEngine = out -> exportWithXlsbuilder(grid, db, out);
            ExportEngine flowingcodeEngine = out -> exportWithFlowingcode(grid, out);

            BenchmarkResult xlsxbuilder = benchmark(ENGINE_XLSBUILDER, xlsxbuilderEngine);
            BenchmarkResult flowingcode = benchmark(ENGINE_FLOWINGCODE, flowingcodeEngine);

            printReport(rowCount, xlsxbuilder, flowingcode);

            assertValidWorkbook(xlsxbuilderEngine);
            assertValidWorkbook(flowingcodeEngine);
        }
    }

    // ------------------------------------------------------------------ Engines

    private static void exportWithXlsbuilder(Grid<SampleRow> grid, TestDataDatabase db, OutputStream out)
            throws IOException {
        String orderBy = SampleGrid.orderByForKeys(grid.getSortOrder()); // unsortiert → ORDER BY id
        try (TestDataDatabase.StreamingResult stream = db.openStream(orderBy, FETCH_SIZE)) {
            GridExcelExporter.from("Benchmark", grid)
                    .export(DataProviders.ofResultSet(stream.resultSet(), MAPPER), out);
        }
    }

    private static void exportWithFlowingcode(Grid<SampleRow> grid, OutputStream out) throws IOException {
        GridExporter.createFor(grid).getExcelStreamResource().getWriter().accept(out, headlessSession());
    }

    // ------------------------------------------------------- Benchmark-Maschinerie

    private static BenchmarkResult benchmark(String name, ExportEngine engine) throws IOException {
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            runOnce(engine);
        }

        long[] timings = new long[MEASURED_ITERATIONS];
        int outputBytes = 0;
        for (int i = 0; i < MEASURED_ITERATIONS; i++) {
            long start = System.nanoTime();
            outputBytes = runOnce(engine);
            timings[i] = System.nanoTime() - start;
        }
        Arrays.sort(timings);
        long median = timings[timings.length / 2];
        long average = Arrays.stream(timings).sum() / timings.length;
        return new BenchmarkResult(name, median, average, outputBytes);
    }

    private static int runOnce(ExportEngine engine) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        engine.export(out);
        return out.size();
    }

    // ----------------------------------------------------------- Grid (lazy, DB-gestützt)

    private static Grid<SampleRow> buildGrid(TestDataDatabase db) {
        Grid<SampleRow> grid = new Grid<>();
        SampleGrid.configure(grid);
        SampleDataProvider.bind(grid, db);
        return grid;
    }

    /**
     * Minimale, headless {@link VaadinSession} für den Flowingcode-Export. Dessen
     * {@code ExcelStreamResourceWriter} sperrt die Session beim Schreiben – ohne laufende UI genügt
     * eine Session mit eigenem {@link Lock}. Muster aus den Flowingcode-eigenen Tests.
     */
    private static VaadinSession headlessSession() {
        VaadinService service = new VaadinServletService(null, null);
        return new VaadinSession(service) {
            private final Lock lock = new ReentrantLock();

            @Override
            public Lock getLockInstance() {
                return lock;
            }
        };
    }

    // ----------------------------------------------------------- Korrektheit

    /**
     * Exportiert einmalig und prüft leichtgewichtig, dass eine gültige {@code .xlsx} entstanden ist:
     * ZIP-Signatur ({@code PK}) und ein Worksheet-Eintrag. ZIP speichert Eintragsnamen
     * unkomprimiert, daher genügt eine Byte-Suche nach {@code xl/worksheets/sheet} – ohne das
     * (potenziell sehr große) Workbook zu entpacken. Die zellgenaue Korrektheit deckt
     * {@code GridExcelExporterTest} ab.
     */
    private static void assertValidWorkbook(ExportEngine engine) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        engine.export(out);
        byte[] bytes = out.toByteArray();

        assertTrue(bytes.length > 4, "Export erzeugte keine Bytes");
        assertTrue(
                bytes[0] == 'P' && bytes[1] == 'K' && bytes[2] == 3 && bytes[3] == 4,
                "Keine gültige ZIP-/xlsx-Signatur");
        String raw = new String(bytes, StandardCharsets.ISO_8859_1);
        assertTrue(raw.contains("xl/worksheets/sheet"), "Erzeugte Datei enthält kein Worksheet");
    }

    // ----------------------------------------------------------- Report

    private static void printReport(int rowCount, BenchmarkResult xlsxbuilder, BenchmarkResult flowingcode) {
        BenchmarkResult faster = xlsxbuilder.medianNanos() <= flowingcode.medianNanos() ? xlsxbuilder : flowingcode;
        BenchmarkResult slower = faster == xlsxbuilder ? flowingcode : xlsxbuilder;
        double factor = (double) slower.medianNanos() / faster.medianNanos();

        StringBuilder report = new StringBuilder();
        report.append(System.lineSeparator());
        report.append("=== Excel-Export Benchmark (aus H2-Datenbank) ================")
                .append(System.lineSeparator());
        report.append(String.format(
                        "Zeilen: %d  |  Warmup: %d  |  Messläufe: %d  |  FetchSize: %d",
                        rowCount, WARMUP_ITERATIONS, MEASURED_ITERATIONS, FETCH_SIZE))
                .append(System.lineSeparator());
        report.append(String.format("%-14s %12s %12s %14s %12s", "Engine", "Median", "Avg", "Zeilen/s", "Größe"))
                .append(System.lineSeparator());
        report.append(formatRow(xlsxbuilder, rowCount)).append(System.lineSeparator());
        report.append(formatRow(flowingcode, rowCount)).append(System.lineSeparator());
        report.append("--------------------------------------------------------------")
                .append(System.lineSeparator());
        report.append(String.format("Schneller: %s (Faktor %.2fx)", faster.engine(), factor))
                .append(System.lineSeparator());
        report.append("==============================================================");
        System.out.println(report);
    }

    private static String formatRow(BenchmarkResult result, int rowCount) {
        return String.format(
                "%-14s %10.1fms %10.1fms %14.0f %9d KB",
                result.engine(),
                result.medianMillis(),
                result.avgMillis(),
                result.rowsPerSecond(rowCount),
                result.outputBytes() / 1024);
    }
}
