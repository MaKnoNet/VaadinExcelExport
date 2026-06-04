package de.makno.vaadinexcelexport.app;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.server.StreamResource;
import de.makno.vaadinexcelexport.export.GridExcelExporter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/**
 * Tab-Inhalt für den xlsbuilder-basierten Export: zeigt die Beispieldaten in einem {@code Grid}
 * und bietet darüber einen Button, der die Tabelle per {@link GridExcelExporter} als {@code .xlsx}
 * herunterlädt.
 *
 * <p>Kein eigenes Routing – wird als Inhalt eines {@link com.vaadin.flow.component.tabs.TabSheet}
 * in {@link MainView} eingebettet.
 *
 * <p>Der Grid wird durch {@link SampleGrid#configure(Grid)} aufgebaut, der gleichzeitig die
 * {@link de.makno.vaadinexcelexport.export.ExcelMeta}-Exportkonfiguration jeder Spalte setzt.
 * {@link GridExcelExporter#from(String, Grid)} liest Spaltenreihenfolge und Metadaten direkt
 * aus dem Grid – eine separate Spaltenliste ist nicht mehr nötig.
 *
 * <p><b>Thread-Sicherheit:</b> Klasse nicht thread-safe; eine Instanz pro UI-Request.
 */
public class SampleDataView extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    private static final String SHEET_NAME = "Beispieldaten";
    private static final String FILE_NAME = "beispieldaten.xlsx";
    private static final String XLSX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    public SampleDataView() {
        Grid<SampleRow> grid = buildGrid();
        add(buildExportButton(grid), grid);
        setSizeFull();
    }

    private static Grid<SampleRow> buildGrid() {
        Grid<SampleRow> grid = new Grid<>();
        SampleGrid.configure(grid);
        grid.setItems(new ListDataProvider<>(SampleData.rows()));
        grid.setSizeFull();
        return grid;
    }

    /**
     * Baut den Download-Auslöser. {@link GridExcelExporter#from(String, Grid)} liest
     * Spaltenreihenfolge und Metadaten direkt aus dem Grid – der Aufrufer übergibt nur das Grid.
     *
     * <p>Hinweis: In Vaadin 24.5 ist {@link StreamResource} die aktuelle Download-API. Ab
     * Vaadin 24.8 gilt sie als veraltet und wird durch {@code DownloadHandler} ersetzt – beim
     * Upgrade auf 24.8+ ist diese Methode entsprechend umzustellen (siehe UPGRADE-24.10.md).
     */
    private static Anchor buildExportButton(Grid<SampleRow> grid) {
        GridExcelExporter<SampleRow> exporter = GridExcelExporter.from(SHEET_NAME, grid);
        StreamResource resource = new StreamResource(FILE_NAME, () -> toExcelStream(exporter, grid));
        resource.setContentType(XLSX_MIME_TYPE);

        Anchor downloadLink = new Anchor(resource, "");
        downloadLink.getElement().setAttribute("download", true);
        downloadLink.add(new Button("MaKnos Export", VaadinIcon.DOWNLOAD.create()));
        return downloadLink;
    }

    private static InputStream toExcelStream(GridExcelExporter<SampleRow> exporter, Grid<SampleRow> grid) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            exporter.export(grid.getDataProvider(), buffer);
        } catch (IOException e) {
            throw new UncheckedIOException("Excel-Export fehlgeschlagen", e);
        }
        return new ByteArrayInputStream(buffer.toByteArray());
    }
}
