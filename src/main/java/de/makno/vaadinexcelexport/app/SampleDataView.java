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
import java.util.List;
import java.util.function.Consumer;

/**
 * Tab-Inhalt für den xlsbuilder-basierten Export („MaKnos Export"): zeigt die Beispieldaten in
 * einem {@code Grid} und exportiert sie per {@link GridExcelExporter} als {@code .xlsx}.
 *
 * <p>Beim Klick auf den Export-Button wird die Erzeugung der Datei vermessen (Laufzeit +
 * allozierte Bytes via {@link ExportMeasurement}); das Ergebnis wird über den
 * {@code metricsSink}-Callback an {@link MainView} gemeldet und dort im Vergleichs-Panel angezeigt.
 * Anschließend wird die bereits erzeugte Datei über einen versteckten {@link Anchor} heruntergeladen
 * – so läuft der Export nur ein einziges Mal.
 *
 * <p>Die Daten werden von außen über {@link #setRows(List)} gesetzt (von {@link MainView}), damit
 * beide Tabs denselben Datenbestand teilen.
 *
 * <p><b>Thread-Sicherheit:</b> Klasse nicht thread-safe; eine Instanz pro UI-Request.
 */
public class SampleDataView extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    private static final String ENGINE_NAME = "MaKnos Export";
    private static final String SHEET_NAME = "Beispieldaten";
    private static final String FILE_NAME = "beispieldaten.xlsx";
    private static final String XLSX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final transient Consumer<ExportMetrics> metricsSink;
    private final Grid<SampleRow> grid = new Grid<>();
    private final Anchor downloadAnchor = new Anchor();

    private int rowCount;

    public SampleDataView(Consumer<ExportMetrics> metricsSink) {
        this.metricsSink = metricsSink;

        SampleGrid.configure(grid);
        grid.setSizeFull();

        downloadAnchor.getElement().setAttribute("download", true);
        downloadAnchor.getStyle().set("display", "none");

        add(buildExportButton(), downloadAnchor, grid);
        setSizeFull();
    }

    /** Setzt den anzuzeigenden und zu exportierenden Datenbestand. */
    public void setRows(List<SampleRow> rows) {
        this.rowCount = rows.size();
        grid.setItems(new ListDataProvider<>(rows));
    }

    private Button buildExportButton() {
        Button button = new Button("MaKnos Export", VaadinIcon.DOWNLOAD.create());
        button.addClickListener(event -> runExport());
        return button;
    }

    /**
     * Führt den Export vermessen aus, meldet die Metriken an {@link MainView} und stößt den
     * Download der erzeugten Bytes an.
     *
     * <p>Hinweis: Der Export läuft synchron im UI-Event-Thread. Für sehr große Zeilenzahlen wäre
     * eine Ausführung im Hintergrund mit {@code @Push} schonender – für die Vergleichs-Demo ist die
     * synchrone Variante bewusst gewählt (unmittelbare Anzeige, einfacher Ablauf).
     */
    private void runExport() {
        GridExcelExporter<SampleRow> exporter = GridExcelExporter.from(SHEET_NAME, grid);
        ExportMeasurement.Result result =
                ExportMeasurement.run(ENGINE_NAME, rowCount, out -> exporter.export(grid.getDataProvider(), out));

        if (metricsSink != null) {
            metricsSink.accept(result.metrics());
        }
        triggerDownload(result.bytes());
    }

    private void triggerDownload(byte[] bytes) {
        StreamResource resource = new StreamResource(FILE_NAME, () -> new ByteArrayInputStream(bytes));
        resource.setContentType(XLSX_MIME_TYPE);
        downloadAnchor.setHref(resource);
        downloadAnchor.getElement().callJsFunction("click");
    }
}
