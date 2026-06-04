package de.makno.vaadinexcelexport.app;

import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import java.io.ByteArrayInputStream;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Einstiegspunkt der Anwendung: vergleicht beide Excel-Export-Wege auf einer einzigen Tabelle.
 *
 * <p>Aufbau (von oben nach unten):
 * <ul>
 *   <li><b>Steuerleiste:</b> Zeilenzahl-Eingabe, „Daten generieren", „Test starten",
 *       „Vergleich (PDF)".</li>
 *   <li><b>Progressbar</b> (indeterminate), nur während des laufenden Tests sichtbar.</li>
 *   <li><b>Vergleichs-Panel:</b> je Engine das letzte Messergebnis (Zeit, Speicher, Größe,
 *       Zeilen).</li>
 *   <li><b>Eine Tabelle</b> mit Multi-Sort: der Nutzer sortiert interaktiv nach mehreren Spalten.
 *       Die Daten erscheinen zunächst unsortiert.</li>
 * </ul>
 *
 * <p>„Test starten" führt beide Exporte in einem Hintergrund-Thread aus, damit die Progressbar
 * sichtbar bleibt; das Ergebnis wird per Server-Push ({@code @Push} auf {@link Application})
 * eingespielt und beide Dateien werden heruntergeladen.
 *
 * <p><b>Thread-Sicherheit:</b> Eine Instanz pro UI. Der Worker ist ein einzelner Daemon-Thread,
 * der in {@link #onDetach(DetachEvent)} beendet wird; der Test-Button ist während des Laufs
 * deaktiviert (kein paralleler Test).
 */
@Route("")
@PageTitle("Excel-Export Demo")
public class MainView extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    private static final String PDF_URL = "/excel-export-vergleich.pdf";
    private static final String XLSX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String MAKNOS_FILE = "maknos-export.xlsx";
    private static final String VAADINS_FILE = "vaadins-export.xlsx";
    private static final NumberFormat ROW_FORMAT = NumberFormat.getIntegerInstance(Locale.GERMANY);

    private final IntegerField rowCountField = new IntegerField();
    private final ProgressBar progressBar = new ProgressBar();
    private final Map<String, Span> metricLines = new LinkedHashMap<>();
    private final Grid<SampleRow> grid = new Grid<>();
    private final ExportRunner runner;
    private final Anchor anchorMaknos = hiddenDownloadAnchor();
    private final Anchor anchorVaadins = hiddenDownloadAnchor();
    private final Button testButton = new Button("Test starten", VaadinIcon.PLAY.create(), e -> runTest());
    private final ExecutorService worker = Executors.newSingleThreadExecutor(this::newDaemonWorker);

    private int rowCount;

    public MainView() {
        SampleGrid.configure(grid);
        grid.setMultiSort(true, Grid.MultiSortPriority.APPEND);
        grid.setSizeFull();
        runner = new ExportRunner(grid);

        add(buildControlBar(), buildProgressBar(), buildMetricsPanel(), grid, anchorMaknos, anchorVaadins);
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        regenerateData();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        worker.shutdownNow();
        super.onDetach(detachEvent);
    }

    private Thread newDaemonWorker(Runnable r) {
        Thread t = new Thread(r, "export-test-worker");
        t.setDaemon(true);
        return t;
    }

    // ─────────────────────────────────────────────────────── Steuerleiste

    private HorizontalLayout buildControlBar() {
        rowCountField.setLabel("Anzahl Testdaten");
        rowCountField.setValue(SampleData.DEFAULT_ROW_COUNT);
        rowCountField.setMin(1);
        rowCountField.setStep(1000);
        rowCountField.setStepButtonsVisible(true);
        rowCountField.setWidth("12em");

        Button generate = new Button("Daten generieren", VaadinIcon.REFRESH.create(), e -> regenerateData());
        Button showPdf = new Button("Vergleich (PDF)", VaadinIcon.FILE_TEXT_O.create(), e -> openPdfDialog());

        HorizontalLayout bar = new HorizontalLayout(rowCountField, generate, testButton, showPdf);
        bar.setAlignItems(FlexComponent.Alignment.BASELINE);
        bar.setWidthFull();
        return bar;
    }

    private ProgressBar buildProgressBar() {
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setWidthFull();
        return progressBar;
    }

    private void regenerateData() {
        Integer count = rowCountField.getValue();
        if (count == null || count < 1) {
            Notification.show("Bitte eine Zeilenzahl ≥ 1 angeben.");
            return;
        }
        List<SampleRow> data = SampleData.rows(count);
        rowCount = data.size();
        grid.setItems(new ListDataProvider<>(data));
        grid.sort(Collections.<GridSortOrder<SampleRow>>emptyList()); // unsortiert anzeigen
        Notification.show(ROW_FORMAT.format(count) + " Zeilen geladen (unsortiert).");
    }

    // ─────────────────────────────────────────────────────── Test (Hintergrund + Push)

    private void runTest() {
        if (rowCount == 0) {
            Notification.show("Bitte zuerst Daten generieren.");
            return;
        }
        progressBar.setVisible(true);
        testButton.setEnabled(false);
        metricLines.forEach((engine, line) -> line.setText(engine + ": wird gemessen …"));

        UI ui = UI.getCurrent();
        VaadinSession session = VaadinSession.getCurrent();
        int rows = rowCount;

        worker.execute(() -> {
            ExportMeasurement.Result maknos;
            ExportMeasurement.Result vaadins;
            session.lock(); // Grid-Zugriff erfordert den Session-Lock
            try {
                maknos = runner.runMaknos(rows);
                vaadins = runner.runVaadins(rows, session);
            } finally {
                session.unlock();
            }
            ui.access(() -> {
                reportMetrics(maknos.metrics());
                reportMetrics(vaadins.metrics());
                triggerDownload(anchorMaknos, maknos.bytes(), MAKNOS_FILE);
                triggerDownload(anchorVaadins, vaadins.bytes(), VAADINS_FILE);
                progressBar.setVisible(false);
                testButton.setEnabled(true);
            });
        });
    }

    // ─────────────────────────────────────────────────────── Vergleichs-Panel

    private VerticalLayout buildMetricsPanel() {
        VerticalLayout panel = new VerticalLayout();
        panel.setPadding(true);
        panel.setSpacing(false);
        panel.getStyle()
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("background", "var(--lumo-contrast-5pct)");
        panel.add(new H4("Letzter Test – Messung"));

        for (String engine : List.of(ExportRunner.ENGINE_MAKNOS, ExportRunner.ENGINE_VAADINS)) {
            Span line = new Span(engine + ": noch nicht gemessen");
            metricLines.put(engine, line);
            panel.add(line);
        }
        return panel;
    }

    private void reportMetrics(ExportMetrics metrics) {
        Span line = metricLines.get(metrics.engine());
        if (line == null) {
            return;
        }
        line.setText(String.format(
                "%s  –  Zeit %s · Speicher %s · Größe %s · Zeilen %s",
                metrics.engine(),
                metrics.durationText(),
                metrics.allocatedText(),
                metrics.outputText(),
                ROW_FORMAT.format(metrics.rowCount())));
    }

    // ─────────────────────────────────────────────────────── Download

    private static Anchor hiddenDownloadAnchor() {
        Anchor anchor = new Anchor();
        anchor.getElement().setAttribute("download", true);
        anchor.getStyle().set("display", "none");
        return anchor;
    }

    private static void triggerDownload(Anchor anchor, byte[] bytes, String fileName) {
        StreamResource resource = new StreamResource(fileName, () -> new ByteArrayInputStream(bytes));
        resource.setContentType(XLSX_MIME_TYPE);
        anchor.setHref(resource);
        anchor.getElement().callJsFunction("click");
    }

    // ─────────────────────────────────────────────────────── PDF-Dialog

    private void openPdfDialog() {
        IFrame frame = new IFrame(PDF_URL);
        frame.setWidth("100%");
        frame.setHeight("100%");

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Excel-Export Vergleich");
        dialog.setWidth("90vw");
        dialog.setHeight("85vh");
        dialog.add(frame);
        dialog.getElement().getStyle().set("--vaadin-dialog-content-padding", "0");
        dialog.getFooter().add(new Button("Schließen", e -> dialog.close()));
        dialog.open();
    }
}
