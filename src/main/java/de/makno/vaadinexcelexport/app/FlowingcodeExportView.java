package de.makno.vaadinexcelexport.app;

import com.flowingcode.vaadin.addons.gridexporter.GridExporter;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;

/**
 * Tab-Inhalt für den Flowingcode-Grid-Exporter-Vergleich: zeigt dieselben Beispieldaten wie
 * {@link SampleDataView}, exportiert sie aber über den Community-Exporter
 * {@link GridExporter} (org.vaadin.addons.flowingcode:grid-exporter-addon).
 *
 * <p>Vergleichspunkte zum xlsbuilder-Ansatz:
 * <ul>
 *   <li><b>Flowingcode:</b> liest Spalten direkt aus dem Grid – kein explizites Typ-Mapping nötig,
 *       dafür weniger Kontrolle über Zelltypen und Formate.</li>
 *   <li><b>xlsbuilder:</b> explizites Typ-Mapping pro Spalte (STRING, DATE, FORMULA …),
 *       volle Kontrolle über Formatcodes und Konvertierungslogik.</li>
 * </ul>
 *
 * <p>Kein eigenes Routing – wird als Inhalt eines {@link com.vaadin.flow.component.tabs.TabSheet}
 * in {@link MainView} eingebettet.
 *
 * <p><b>Thread-Sicherheit:</b> Klasse nicht thread-safe; eine Instanz pro UI-Request.
 */
public class FlowingcodeExportView extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    private static final String FILE_NAME = "flowingcode-export";
    private static final String EXPORT_TITLE = "Beispieldaten";

    public FlowingcodeExportView() {
        Grid<SampleRow> grid = buildGrid();
        grid.setItems(new ListDataProvider<>(SampleData.rows()));
        grid.setSizeFull();

        add(buildExportButton(grid), grid);
        setSizeFull();
    }

    private static Grid<SampleRow> buildGrid() {
        Grid<SampleRow> grid = new Grid<>();
        SampleColumns.all().forEach(col -> grid.addColumn(col.gridValueProvider())
                .setHeader(col.header())
                .setAutoWidth(true));
        return grid;
    }

    /**
     * Erzeugt den Flowingcode-Export-Button. Der {@link GridExporter} introspektiert das Grid und
     * erzeugt die Excel-Datei selbstständig – kein manuelles Typ-Mapping erforderlich.
     *
     * <p>Das Download-Muster folgt dem offiziellen Flowingcode-Beispiel
     * {@code GridExporterCustomLinkDemo}: erst {@code setAutoAttachExportButtons(false)},
     * dann {@code setHref(exporter.getExcelStreamResource())} auf dem {@link Anchor}.
     */
    private static Anchor buildExportButton(Grid<SampleRow> grid) {
        GridExporter<SampleRow> exporter = GridExporter.createFor(grid);
        exporter.setAutoAttachExportButtons(false);
        exporter.setTitle(EXPORT_TITLE);
        exporter.setFileName(FILE_NAME);

        Anchor downloadLink = new Anchor("", "");
        downloadLink.setHref(exporter.getExcelStreamResource());
        downloadLink.getElement().setAttribute("download", true);
        downloadLink.add(new Button("Flowingcode Excel-Export", VaadinIcon.DOWNLOAD.create()));
        return downloadLink;
    }
}
