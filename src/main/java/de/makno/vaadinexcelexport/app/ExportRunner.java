package de.makno.vaadinexcelexport.app;

import com.flowingcode.vaadin.addons.gridexporter.GridExporter;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.server.VaadinSession;
import de.makno.vaadinexcelexport.export.GridExcelExporter;

/**
 * Führt beide Excel-Export-Wege für ein Grid vermessen aus und kapselt deren Engine-spezifische
 * Verdrahtung. Wird von {@link MainView} im Hintergrund-Thread aufgerufen.
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

    private final Grid<SampleRow> grid;
    private final GridExporter<SampleRow> flowExporter;

    ExportRunner(Grid<SampleRow> grid) {
        this.grid = grid;
        this.flowExporter = GridExporter.createFor(grid);
        flowExporter.setAutoAttachExportButtons(false);
        flowExporter.setTitle(SHEET_NAME);
        flowExporter.setFileName(VAADINS_FILE_BASE);
    }

    /** Führt den xlsbuilder-Export aus (ignoriert die Grid-Sortierung – exportiert den Rohbestand). */
    ExportMeasurement.Result runMaknos(int rowCount) {
        GridExcelExporter<SampleRow> exporter = GridExcelExporter.from(SHEET_NAME, grid);
        return ExportMeasurement.run(ENGINE_MAKNOS, rowCount, out -> exporter.export(grid.getDataProvider(), out));
    }

    /**
     * Führt den Flowingcode-Export aus (respektiert die aktuelle Grid-Sortierung). Der Writer
     * sperrt die {@code session} selbst; da der Lock reentrant ist, ist der Aufruf unter bereits
     * gehaltenem Lock unproblematisch.
     */
    ExportMeasurement.Result runVaadins(int rowCount, VaadinSession session) {
        return ExportMeasurement.run(
                ENGINE_VAADINS,
                rowCount,
                out -> flowExporter.getExcelStreamResource().getWriter().accept(out, session));
    }
}
