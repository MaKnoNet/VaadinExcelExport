package de.makno.vaadinexcelexport.app;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Einstiegspunkt der Anwendung: vergleicht zwei Excel-Export-Wege auf identischem Datenbestand.
 *
 * <p>Aufbau (von oben nach unten):
 * <ul>
 *   <li><b>Steuerleiste:</b> Eingabe der Zeilenzahl, Button „Daten generieren", Button
 *       „Vergleich (PDF)".</li>
 *   <li><b>Vergleichs-Panel:</b> zeigt für beide Engines das jeweils letzte Messergebnis
 *       (Zeit, allozierter Speicher, Dateigröße, Zeilen).</li>
 *   <li><b>TabSheet:</b> ein Reiter je Export-Weg ({@link SampleDataView} / {@link
 *       FlowingcodeExportView}); beide teilen sich denselben Datenbestand.</li>
 * </ul>
 *
 * <p><b>Thread-Sicherheit:</b> Klasse nicht thread-safe; eine Instanz pro UI-Request.
 */
@Route("")
@PageTitle("Excel-Export Demo")
public class MainView extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    private static final String TAB_XLSBUILDER = "xlsbuilder-Export";
    private static final String TAB_FLOWINGCODE = "Vaadin Grid Exporter";

    /** Engine-Bezeichner – müssen den ENGINE_NAME-Konstanten der jeweiligen Views entsprechen. */
    private static final String ENGINE_MAKNOS = "MaKnos Export";

    private static final String ENGINE_VAADIN = "Vaadins Export";

    private static final String PDF_URL = "/excel-export-vergleich.pdf";
    private static final NumberFormat ROW_FORMAT = NumberFormat.getIntegerInstance(Locale.GERMANY);

    private final IntegerField rowCountField = new IntegerField();
    private final Map<String, Span> metricLines = new LinkedHashMap<>();
    private final SampleDataView sampleDataView = new SampleDataView(this::reportMetrics);
    private final FlowingcodeExportView flowingcodeExportView = new FlowingcodeExportView(this::reportMetrics);

    public MainView() {
        add(buildControlBar(), buildMetricsPanel(), buildTabs());
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        regenerateData();
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

        HorizontalLayout bar = new HorizontalLayout(rowCountField, generate, showPdf);
        bar.setAlignItems(FlexComponent.Alignment.BASELINE);
        bar.setWidthFull();
        return bar;
    }

    private void regenerateData() {
        Integer count = rowCountField.getValue();
        if (count == null || count < 1) {
            Notification.show("Bitte eine Zeilenzahl ≥ 1 angeben.");
            return;
        }
        // Einmal erzeugen, dieselbe (unveränderliche) Liste an beide Views → identischer Inhalt.
        List<SampleRow> data = SampleData.rows(count);
        sampleDataView.setRows(data);
        flowingcodeExportView.setRows(data);
        Notification.show(ROW_FORMAT.format(count) + " Zeilen in beide Tabellen geladen.");
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
        panel.add(new H4("Letzter Export – Messung"));

        for (String engine : List.of(ENGINE_MAKNOS, ENGINE_VAADIN)) {
            Span line = new Span(engine + ": noch nicht gemessen");
            metricLines.put(engine, line);
            panel.add(line);
        }
        return panel;
    }

    /** Callback der Views: aktualisiert die Panel-Zeile der jeweiligen Engine. */
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

    // ─────────────────────────────────────────────────────── Tabs

    private TabSheet buildTabs() {
        TabSheet tabs = new TabSheet();
        tabs.add(TAB_XLSBUILDER, sampleDataView);
        tabs.add(TAB_FLOWINGCODE, flowingcodeExportView);
        tabs.setSizeFull();
        return tabs;
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

        Button close = new Button("Schließen", e -> dialog.close());
        dialog.getFooter().add(close);

        dialog.open();
    }
}
