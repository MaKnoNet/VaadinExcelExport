package de.makno.vaadinexcelexport.app;

import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Einstiegspunkt der App: vergleicht beide Excel-Export-Wege auf einer Tabelle, deren Testdaten in
 * einer SQL-Datenbank ({@link TestDataDatabase}) liegen und lazy/seitenweise geladen werden.
 *
 * <p>Bedienelemente: Zeilenzahl + <b>PageSize</b> (Grid-Page-Size und JDBC-Fetch-Size),
 * „Daten generieren", die Tests <b>einzeln</b> („MaKnos Test" / „Vaadins Test") oder <b>kombiniert</b>
 * („Test starten"), sowie „Vergleich (PDF)". Während eines Laufs zeigt eine Status­zeile, <b>welcher
 * Test gerade läuft</b>; im Kombi-Lauf erscheint das Ergebnis jeder Engine <b>sofort</b> nach deren
 * Abschluss (Server-Push, {@code @Push} auf {@link Application}).
 *
 * <p><b>Thread-Sicherheit:</b> Eine Instanz pro UI. Tests laufen in einem einzelnen Daemon-Worker
 * (in {@link #onDetach} beendet); die DB wird dort ebenfalls geschlossen. Während eines Laufs sind
 * die Bedien-Buttons deaktiviert (kein paralleler Test).
 */
@Route("")
@PageTitle("Excel-Export Demo")
public class MainView extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    private static final System.Logger LOG = System.getLogger(MainView.class.getName());

    private static final String PDF_URL = "/excel-export-vergleich.pdf";
    private static final String XLSX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String MAKNOS_FILE_BASE = "maknos-export";
    private static final String VAADINS_FILE_BASE = "vaadins-export";
    private static final int DEFAULT_PAGE_SIZE = 1000;

    /** Obergrenze der generierbaren Testzeilen (Schutz vor versehentlichem/böswilligem Massen-Insert). */
    private static final int MAX_ROW_COUNT = 1_000_000;

    /** Zeitstempel ohne {@code :} – gültig als Dateiname auf Windows/Linux/macOS. */
    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private final transient TestDataDatabase db = new TestDataDatabase();
    private final Grid<SampleRow> grid = new Grid<>();
    private final transient ExportRunner runner;

    private final IntegerField rowCountField = new IntegerField();
    private final IntegerField pageSizeField = new IntegerField();
    private final TextField searchField = new TextField();
    private final Checkbox parallelCheckbox = new Checkbox("Parallele Pipeline");
    private final Checkbox reorderCheckbox = new Checkbox("Export: Spalten umordnen");
    private final ProgressBar progressBar = new ProgressBar();
    private final Span statusSpan = new Span();
    private final Map<String, Span> metricLines = new LinkedHashMap<>();
    private final Anchor anchorMaknos = hiddenDownloadAnchor();
    private final Anchor anchorVaadins = hiddenDownloadAnchor();

    private final Button generateButton =
            new Button("Daten generieren", VaadinIcon.REFRESH.create(), e -> regenerateData());
    private final Button maknosButton =
            new Button("MaKnos Test", VaadinIcon.PLAY.create(), e -> runSelected(true, false));
    private final Button vaadinsButton =
            new Button("Vaadins Test", VaadinIcon.PLAY.create(), e -> runSelected(false, true));
    private final Button combinedButton =
            new Button("Test starten", VaadinIcon.PLAY_CIRCLE.create(), e -> runSelected(true, true));

    private final ExecutorService worker = Executors.newSingleThreadExecutor(this::newDaemonWorker);

    private transient ConfigurableFilterDataProvider<SampleRow, Void, String> filterProvider;
    private String currentFilter;
    private int seededRows;

    public MainView() {
        SampleGrid.configure(grid);
        grid.setMultiSort(true, Grid.MultiSortPriority.APPEND);
        grid.setSizeFull();
        runner = new ExportRunner(grid, db);

        add(buildControlBar(), buildStatusRow(), buildMetricsPanel(), grid, anchorMaknos, anchorVaadins);
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        regenerateData();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        worker.shutdownNow();
        db.close();
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
        rowCountField.setMax(MAX_ROW_COUNT);
        rowCountField.setStep(1000);
        rowCountField.setStepButtonsVisible(true);
        rowCountField.setWidth("11em");

        pageSizeField.setLabel("PageSize");
        pageSizeField.setValue(DEFAULT_PAGE_SIZE);
        pageSizeField.setMin(1);
        pageSizeField.setStep(100);
        pageSizeField.setStepButtonsVisible(true);
        pageSizeField.setWidth("9em");

        searchField.setLabel("Filter (Text/Webseite)");
        searchField.setPlaceholder("z. B. Alice oder Eintrag 1");
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setWidth("15em");
        searchField.addValueChangeListener(e -> applyFilter(e.getValue()));

        parallelCheckbox.setValue(false);
        reorderCheckbox.setValue(false);

        Button pdfButton = new Button("Vergleich (PDF)", VaadinIcon.FILE_TEXT_O.create(), e -> openPdfDialog());

        HorizontalLayout bar = new HorizontalLayout(
                rowCountField,
                pageSizeField,
                searchField,
                parallelCheckbox,
                reorderCheckbox,
                generateButton,
                maknosButton,
                vaadinsButton,
                combinedButton,
                pdfButton);
        bar.setAlignItems(FlexComponent.Alignment.BASELINE);
        bar.setWidthFull();
        bar.getStyle().set("flex-wrap", "wrap");
        return bar;
    }

    private HorizontalLayout buildStatusRow() {
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setWidth("16em");
        statusSpan.getStyle().set("font-weight", "600");
        HorizontalLayout row = new HorizontalLayout(progressBar, statusSpan);
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        return row;
    }

    private void regenerateData() {
        Integer count = rowCountField.getValue();
        if (count == null || count < 1) {
            Notification.show("Bitte eine Zeilenzahl ≥ 1 angeben.");
            return;
        }
        // Server-seitiger Guard: die UI-Begrenzung (setMax) lässt sich clientseitig umgehen.
        if (count > MAX_ROW_COUNT) {
            Notification.show("Bitte höchstens " + formatRowCount(MAX_ROW_COUNT) + " Zeilen angeben.");
            return;
        }
        int pageSize = currentPageSize();
        db.seed(count);
        seededRows = count;
        filterProvider = SampleDataProvider.bind(grid, db);
        currentFilter = null;
        searchField.clear();
        grid.setPageSize(pageSize);
        grid.sort(Collections.<GridSortOrder<SampleRow>>emptyList()); // unsortiert (Default ORDER BY id)
        Notification.show(formatRowCount(count) + " Zeilen in der DB · PageSize " + pageSize + ".");
    }

    /**
     * Formatiert eine Zeilenzahl mit Tausendertrennung (de-DE). Bewusst über {@link String#format}
     * statt eines geteilten {@link java.text.NumberFormat} – {@code NumberFormat} ist nicht
     * thread-safe und diese Methode wird aus mehreren UI-/Session-Kontexten aufgerufen.
     */
    private static String formatRowCount(long count) {
        return String.format(Locale.GERMANY, "%,d", count);
    }

    /** Setzt den aktiven Grid-Filter; wirkt sofort auf die Anzeige und auf beide Exporte. */
    private void applyFilter(String value) {
        currentFilter = (value == null || value.isBlank()) ? null : value.trim();
        if (filterProvider != null) {
            filterProvider.setFilter(currentFilter);
        }
    }

    private int currentPageSize() {
        Integer value = pageSizeField.getValue();
        return (value == null || value < 1) ? DEFAULT_PAGE_SIZE : value;
    }

    // ─────────────────────────────────────────────────────── Test-Ausführung

    /** Ein Test-Schritt: misst eine Engine, meldet das Ergebnis und löst den Datei-Download aus. */
    @FunctionalInterface
    private interface EngineTask {
        ExportMeasurement.Result run(VaadinSession session, int pageSize);
    }

    private record TestStep(String engine, String fileBase, Anchor anchor, EngineTask task) {}

    private TestStep maknosStep(int rowCount, boolean parallel, String search, List<String> columnOrder) {
        return new TestStep(
                ExportRunner.ENGINE_MAKNOS,
                MAKNOS_FILE_BASE,
                anchorMaknos,
                (session, pageSize) -> runner.runMaknos(rowCount, pageSize, parallel, search, columnOrder));
    }

    private TestStep vaadinsStep(int rowCount) {
        return new TestStep(
                ExportRunner.ENGINE_VAADINS,
                VAADINS_FILE_BASE,
                anchorVaadins,
                (session, pageSize) -> runner.runVaadins(rowCount, session));
    }

    /**
     * Sammelt die gewählten Engines mit dem aktuellen Parallel-Schalter und dem aktiven Filter und
     * startet den Lauf. Filter/Parallelismus und die gefilterte Zeilenzahl werden hier (im UI-Thread)
     * als Schnappschuss gelesen; beide Engines exportieren so genau die gefilterten Zeilen.
     */
    private void runSelected(boolean maknos, boolean vaadins) {
        if (seededRows == 0) {
            Notification.show("Bitte zuerst Daten generieren.");
            return;
        }
        boolean parallel = Boolean.TRUE.equals(parallelCheckbox.getValue());
        String search = currentFilter;
        List<String> columnOrder =
                Boolean.TRUE.equals(reorderCheckbox.getValue()) ? SampleGrid.EXPORT_ORDER_LINK_FIRST : List.of();
        int rowCount = (int) db.count(search);
        List<TestStep> steps = new ArrayList<>();
        if (maknos) {
            steps.add(maknosStep(rowCount, parallel, search, columnOrder));
        }
        if (vaadins) {
            steps.add(vaadinsStep(rowCount));
        }
        runSteps(steps);
    }

    /**
     * Führt die Schritte nacheinander im Hintergrund-Worker aus. Vor jedem Schritt wird der Status
     * („Aktueller Test: …") gepusht, nach jedem Schritt sofort dessen Ergebnis + Download – beim
     * Kombi-Lauf erscheinen die Engines also einzeln, nicht erst am Ende.
     */
    private void runSteps(List<TestStep> steps) {
        int pageSize = currentPageSize();
        grid.setPageSize(pageSize);
        setControlsEnabled(false);
        progressBar.setVisible(true);
        steps.forEach(step -> metricLines.get(step.engine()).setText(step.engine() + ": wartet …"));

        UI ui = UI.getCurrent();
        VaadinSession session = VaadinSession.getCurrent();

        worker.execute(() -> {
            try {
                for (TestStep step : steps) {
                    ui.access(() -> {
                        statusSpan.setText("Aktueller Test: " + step.engine() + " …");
                        metricLines.get(step.engine()).setText(step.engine() + ": wird gemessen …");
                    });
                    ExportMeasurement.Result result;
                    session.lock(); // Grid-/DB-Zugriff erfordert den Session-Lock
                    try {
                        result = step.task().run(session, pageSize);
                    } finally {
                        session.unlock();
                    }
                    ui.access(() -> {
                        reportMetrics(result.metrics());
                        triggerDownload(step.anchor(), result.bytes(), testFileName(step.fileBase()));
                    });
                }
            } catch (RuntimeException ex) {
                // Details server-seitig protokollieren, dem Client nur eine generische Meldung zeigen
                // (keine internen Pfade/SQL-Meldungen ins UI leaken).
                LOG.log(System.Logger.Level.ERROR, "Export-Testlauf fehlgeschlagen", ex);
                ui.access(() -> Notification.show("Testlauf fehlgeschlagen – Details siehe Server-Log."));
            } finally {
                ui.access(() -> {
                    progressBar.setVisible(false);
                    statusSpan.setText("");
                    setControlsEnabled(true);
                });
            }
        });
    }

    private void setControlsEnabled(boolean enabled) {
        generateButton.setEnabled(enabled);
        maknosButton.setEnabled(enabled);
        vaadinsButton.setEnabled(enabled);
        combinedButton.setEnabled(enabled);
        parallelCheckbox.setEnabled(enabled);
        reorderCheckbox.setEnabled(enabled);
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
                formatRowCount(metrics.rowCount())));
    }

    // ─────────────────────────────────────────────────────── Download

    /**
     * Baut einen eindeutigen Download-Namen {@code Test_<base>_<datum_uhrzeit>.xlsx}. Es ist ein
     * HTTP-Download-Name (Content-Disposition), kein Dateisystem-Pfad – daher bewusst {@link String}.
     */
    private static String testFileName(String base) {
        return "Test_" + base + "_" + LocalDateTime.now().format(FILE_TIMESTAMP) + ".xlsx";
    }

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
