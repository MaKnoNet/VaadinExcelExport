package de.makno.vaadinexcelexport.app;

import com.flowingcode.vaadin.addons.gridexporter.GridExporter;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.server.VaadinSession;
import de.makno.vaadinexcelexport.export.GridExcelExporter;
import de.makno.xlsbuilder.builder.DataProviders;

/**
 * Führt beide Excel-Export-Wege vermessen aus – beide lesen aus der SQL-Datenbank:
 *
 * <ul>
 *   <li><b>MaKnos (xlsbuilder):</b> streamt out-of-core direkt aus einem forward-only
 *       JDBC-{@code ResultSet} ({@link TestDataDatabase#openStream}) – der Datenbestand wird nie
 *       vollständig in den Speicher geladen. Die Reihenfolge folgt der aktuellen Grid-Sortierung
 *       ({@code ORDER BY} aus {@link SampleGrid#orderByForKeys}).</li>
 *   <li><b>Vaadins (Flowingcode):</b> liest über das (lazy, seitenweise) Grid, das seinerseits aus
 *       der DB paginiert.</li>
 * </ul>
 *
 * <p><b>Thread-Sicherheit:</b> Hält nur unveränderliche Felder. Die Methoden greifen auf das Grid
 * zu und müssen daher unter gehaltenem {@link VaadinSession}-Lock aufgerufen werden (siehe
 * {@link MainView}).
 */
final class ExportRunner {

    static final String ENGINE_MAKNOS = "MaKnos Export";
    static final String ENGINE_VAADINS = "Vaadins Export";

    private static final String SHEET_NAME = "Beispieldaten";
    private static final String VAADINS_FILE_BASE = "vaadins-export";
    private static final SampleRowMapper MAPPER = new SampleRowMapper();

    private final Grid<SampleRow> grid;
    private final TestDataDatabase db;
    private final GridExporter<SampleRow> flowExporter;

    ExportRunner(Grid<SampleRow> grid, TestDataDatabase db) {
        this.grid = grid;
        this.db = db;
        this.flowExporter = GridExporter.createFor(grid);
        flowExporter.setAutoAttachExportButtons(false);
        flowExporter.setTitle(SHEET_NAME);
        flowExporter.setFileName(VAADINS_FILE_BASE);
    }

    /**
     * Streamt den xlsbuilder-Export out-of-core aus der DB. {@code pageSize} dient als JDBC-Fetch-Size
     * (Zeilen pro DB-Roundtrip); die Sortierung entspricht der aktuellen Grid-Sortierung.
     */
    ExportMeasurement.Result runMaknos(int rowCount, int pageSize) {
        String orderBy = SampleGrid.orderByForKeys(grid.getSortOrder());
        return ExportMeasurement.run(ENGINE_MAKNOS, rowCount, out -> {
            try (TestDataDatabase.StreamingResult stream = db.openStream(orderBy, pageSize)) {
                GridExcelExporter.from(SHEET_NAME, grid)
                        .export(DataProviders.ofResultSet(stream.resultSet(), MAPPER), out);
            }
        });
    }

    /**
     * Führt den Flowingcode-Export über das lazy Grid aus. Der Writer sperrt die {@code session}
     * selbst (reentrant); der Aufruf erfolgt im Worker unter bereits gehaltenem Lock.
     */
    ExportMeasurement.Result runVaadins(int rowCount, VaadinSession session) {
        return ExportMeasurement.run(
                ENGINE_VAADINS,
                rowCount,
                out -> flowExporter.getExcelStreamResource().getWriter().accept(out, session));
    }
}
