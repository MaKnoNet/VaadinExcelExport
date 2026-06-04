package de.makno.vaadinexcelexport.app;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.flowingcode.vaadin.addons.gridexporter.GridExporter;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletService;
import com.vaadin.flow.server.VaadinSession;
import de.makno.vaadinexcelexport.export.GridExcelExporter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Performance-Vergleich der beiden Excel-Export-Wege auf demselben {@link Grid}:
 *
 * <ul>
 *   <li><b>xlsbuilder</b> – der eigene {@link GridExcelExporter} (typgesteuert via ExcelMeta).</li>
 *   <li><b>Flowingcode</b> – der Community-{@link GridExporter} (grid-introspektiv).</li>
 * </ul>
 *
 * <p>Beide Engines exportieren denselben programmatisch erzeugten Datenbestand und werden mit
 * Warmup- und Messläufen vermessen. Das Ergebnis (Median/Durchschnitt, Durchsatz, Dateigröße)
 * wird als Tabelle ausgegeben – der Test <em>berichtet</em>, welcher Weg schneller ist.
 *
 * <p><b>Bewusst keine Zeit-Assertions:</b> Absolute Laufzeiten sind maschinen- und
 * plattformabhängig; harte Schwellwerte wären flaky und verstießen gegen die Anforderung
 * plattformübergreifender Lauffähigkeit. Geprüft wird ausschließlich die Korrektheit (beide
 * Ausgaben sind valide, vollständige {@code .xlsx}-Dateien).
 *
 * <p>Der Test ist mit {@code @Tag("benchmark")} markiert und läuft daher nicht im normalen
 * {@code test}-Lauf, sondern nur über die Gradle-Task {@code benchmark}. Die Zeilenzahl ist über
 * die System-Property {@code -Dbenchmark.rows=N} konfigurierbar.
 */
@Tag("benchmark")
class ExcelExporterBenchmarkTest {

    private static final String ENGINE_XLSBUILDER = "xlsbuilder";
    private static final String ENGINE_FLOWINGCODE = "Flowingcode";

    private static final String ROW_COUNT_PROPERTY = "benchmark.rows";
    private static final int DEFAULT_ROW_COUNT = 25_000;
    private static final int WARMUP_ITERATIONS = 2;
    private static final int MEASURED_ITERATIONS = 5;

    private static final double NANOS_PER_MILLI = 1_000_000.0;
    private static final double NANOS_PER_SECOND = 1_000_000_000.0;

    /** Eine Export-Engine: schreibt das übergebene Grid als {@code .xlsx} in den Stream. */
    @FunctionalInterface
    private interface ExportEngine {
        void export(Grid<SampleRow> grid, OutputStream out) throws IOException;
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
        List<SampleRow> rows = generateRows(rowCount);

        BenchmarkResult xlsbuilder =
                benchmark(ENGINE_XLSBUILDER, rows, ExcelExporterBenchmarkTest::exportWithXlsbuilder);
        BenchmarkResult flowingcode =
                benchmark(ENGINE_FLOWINGCODE, rows, ExcelExporterBenchmarkTest::exportWithFlowingcode);

        printReport(rowCount, xlsbuilder, flowingcode);

        assertValidWorkbook(rows, ExcelExporterBenchmarkTest::exportWithXlsbuilder, rowCount);
        assertValidWorkbook(rows, ExcelExporterBenchmarkTest::exportWithFlowingcode, rowCount);
    }

    // ------------------------------------------------------------------ Engines

    private static void exportWithXlsbuilder(Grid<SampleRow> grid, OutputStream out) throws IOException {
        GridExcelExporter.from("Benchmark", grid).export(grid.getDataProvider(), out);
    }

    private static void exportWithFlowingcode(Grid<SampleRow> grid, OutputStream out) throws IOException {
        GridExporter.createFor(grid).getExcelStreamResource().getWriter().accept(out, headlessSession());
    }

    // ------------------------------------------------------- Benchmark-Maschinerie

    private static BenchmarkResult benchmark(String name, List<SampleRow> rows, ExportEngine engine)
            throws IOException {
        Grid<SampleRow> grid = buildGrid(rows);

        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            runOnce(grid, engine);
        }

        long[] timings = new long[MEASURED_ITERATIONS];
        int outputBytes = 0;
        for (int i = 0; i < MEASURED_ITERATIONS; i++) {
            long start = System.nanoTime();
            outputBytes = runOnce(grid, engine);
            timings[i] = System.nanoTime() - start;
        }
        Arrays.sort(timings);
        long median = timings[timings.length / 2];
        long average = Arrays.stream(timings).sum() / timings.length;
        return new BenchmarkResult(name, median, average, outputBytes);
    }

    private static int runOnce(Grid<SampleRow> grid, ExportEngine engine) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        engine.export(grid, out);
        return out.size();
    }

    // ----------------------------------------------------------- Daten & Grid

    private static List<SampleRow> generateRows(int count) {
        return SampleData.rows(count);
    }

    private static Grid<SampleRow> buildGrid(List<SampleRow> rows) {
        Grid<SampleRow> grid = new Grid<>();
        SampleGrid.configure(grid);
        grid.setItems(rows);
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
     * Exportiert einmalig und prüft, dass eine gültige, vollständige {@code .xlsx} entsteht: mit
     * Apache POI öffenbar und mit mindestens {@code rowCount} Datenzeilen (zusätzlich zu eventuellen
     * Kopf-/Titelzeilen, die je Engine variieren).
     */
    private static void assertValidWorkbook(List<SampleRow> rows, ExportEngine engine, int rowCount) throws Exception {
        Grid<SampleRow> grid = buildGrid(rows);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        engine.export(grid, out);

        assertTrue(out.size() > 0, "Export erzeugte keine Bytes");
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(out.toByteArray()))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertTrue(
                    sheet.getLastRowNum() >= rowCount,
                    "Erwartete mindestens " + rowCount + " Datenzeilen, fand " + sheet.getLastRowNum());
        }
    }

    // ----------------------------------------------------------- Report

    private static void printReport(int rowCount, BenchmarkResult xlsbuilder, BenchmarkResult flowingcode) {
        BenchmarkResult faster = xlsbuilder.medianNanos() <= flowingcode.medianNanos() ? xlsbuilder : flowingcode;
        BenchmarkResult slower = faster == xlsbuilder ? flowingcode : xlsbuilder;
        double factor = (double) slower.medianNanos() / faster.medianNanos();

        StringBuilder report = new StringBuilder();
        report.append(System.lineSeparator());
        report.append("=== Excel-Export Benchmark ===================================")
                .append(System.lineSeparator());
        report.append(String.format(
                        "Zeilen: %d  |  Warmup: %d  |  Messläufe: %d",
                        rowCount, WARMUP_ITERATIONS, MEASURED_ITERATIONS))
                .append(System.lineSeparator());
        report.append(String.format("%-14s %12s %12s %14s %12s", "Engine", "Median", "Avg", "Zeilen/s", "Größe"))
                .append(System.lineSeparator());
        report.append(formatRow(xlsbuilder, rowCount)).append(System.lineSeparator());
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
