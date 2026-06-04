package de.makno.vaadinexcelexport.app;

import com.flowingcode.vaadin.addons.gridexporter.GridExporter;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.function.Consumer;

/**
 * Tab-Inhalt für den Flowingcode-Export („Vaadins Export"): zeigt denselben Datenbestand wie
 * {@link SampleDataView}, exportiert ihn aber über den Community-{@link GridExporter}
 * (org.vaadin.addons.flowingcode:grid-exporter-addon).
 *
 * <p>Wie im xlsbuilder-Tab wird der Export beim Klick vermessen ({@link ExportMeasurement}) und das
 * Ergebnis über {@code metricsSink} an {@link MainView} gemeldet. Der Flowingcode-Export wird dabei
 * synchron im UI-Event-Thread ausgeführt: {@code getExcelStreamResource().getWriter().accept(out,
 * session)}. Der Session-Lock ist reentrant, daher ist der Aufruf innerhalb des bereits gesperrten
 * Event-Threads unproblematisch.
 *
 * <p>Die Daten werden von außen über {@link #setRows(List)} gesetzt, damit beide Tabs denselben
 * Datenbestand teilen.
 *
 * <p><b>Thread-Sicherheit:</b> Klasse nicht thread-safe; eine Instanz pro UI-Request.
 */
public class FlowingcodeExportView extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    private static final String ENGINE_NAME = "Vaadins Export";
    private static final String EXPORT_TITLE = "Beispieldaten";
    private static final String FILE_NAME_BASE = "flowingcode-export";
    private static final String FILE_NAME = FILE_NAME_BASE + ".xlsx";
    private static final String XLSX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final transient Consumer<ExportMetrics> metricsSink;
    private final Grid<SampleRow> grid = new Grid<>();
    private final transient GridExporter<SampleRow> exporter;
    private final Anchor downloadAnchor = new Anchor();

    private int rowCount;

    public FlowingcodeExportView(Consumer<ExportMetrics> metricsSink) {
        this.metricsSink = metricsSink;

        SampleGrid.configure(grid);
        grid.setSizeFull();

        exporter = GridExporter.createFor(grid);
        exporter.setAutoAttachExportButtons(false);
        exporter.setTitle(EXPORT_TITLE);
        exporter.setFileName(FILE_NAME_BASE);

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
        Button button = new Button("Vaadins Export", VaadinIcon.DOWNLOAD.create());
        button.addClickListener(event -> runExport());
        return button;
    }

    private void runExport() {
        VaadinSession session = VaadinSession.getCurrent();
        ExportMeasurement.Result result =
                ExportMeasurement.run(ENGINE_NAME, rowCount, out -> exporter.getExcelStreamResource()
                        .getWriter()
                        .accept(out, session));

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
