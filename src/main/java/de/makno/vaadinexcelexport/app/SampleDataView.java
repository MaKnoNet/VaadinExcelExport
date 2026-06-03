package de.makno.vaadinexcelexport.app;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import de.makno.vaadinexcelexport.export.ExcelColumn;
import de.makno.vaadinexcelexport.export.GridExcelExporter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Comparator;
import java.util.List;

/**
 * Hauptansicht: zeigt die Beispieldaten in einem {@code Grid} und bietet darüber einen Button,
 * der die Tabelle per {@link GridExcelExporter} als {@code .xlsx} herunterlädt.
 *
 * <p>Die {@link ExcelColumn}-Liste ({@link SampleColumns}) ist die einzige Quelle der Wahrheit:
 * Sie definiert sowohl die Grid-Spalten als auch die Export-Spalten. Der Export erhält genau diese
 * Spalten und den {@link ListDataProvider} des Grids.
 */
@Route("")
@PageTitle("Excel-Export Demo")
public class SampleDataView extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    private static final String SHEET_NAME = "Beispieldaten";
    private static final String FILE_NAME = "beispieldaten.xlsx";
    private static final String XLSX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    public SampleDataView() {
        List<ExcelColumn<SampleRow>> columns = SampleColumns.all();
        ListDataProvider<SampleRow> dataProvider = new ListDataProvider<>(SampleData.rows());

        add(createExportButton(columns, dataProvider), createGrid(columns, dataProvider));
        setSizeFull();
    }

    private Grid<SampleRow> createGrid(List<ExcelColumn<SampleRow>> columns, ListDataProvider<SampleRow> dataProvider) {
        Grid<SampleRow> grid = new Grid<>();
        columns.forEach(column -> grid.addColumn(column.gridValueProvider())
                .setHeader(column.header())
                .setAutoWidth(true)
                .setComparator(naturalComparator(column.gridValueProvider())));
        grid.setItems(dataProvider);
        grid.setSizeFull();
        return grid;
    }

    /**
     * Erzeugt einen natürlichen Komparator für eine Grid-Spalte. Der Rückgabewert des
     * {@link ValueProvider} muss {@link Comparable} implementieren – das gilt für alle hier
     * verwendeten Typen (String, Number-Subtypen, LocalDate, LocalDateTime, LocalTime, Boolean).
     * Null-Werte werden an den Anfang einsortiert.
     */
    @SuppressWarnings("unchecked")
    private static <T> Comparator<T> naturalComparator(ValueProvider<T, ?> provider) {
        return Comparator.comparing(
                row -> (Comparable<Object>) provider.apply(row), Comparator.nullsFirst(Comparator.naturalOrder()));
    }

    /**
     * Baut den Download-Auslöser: ein {@link Anchor} mit {@code download}-Attribut, der einen Button
     * umschließt. Die {@link StreamResource} ruft beim Klick den {@link GridExcelExporter} auf und
     * liefert die erzeugte {@code .xlsx} aus.
     *
     * <p>Hinweis: In Vaadin 24.5 ist {@link StreamResource} die aktuelle Download-API. Ab Vaadin 24.8
     * gilt sie als veraltet und wird durch {@code DownloadHandler} ersetzt – beim Upgrade auf 24.8+
     * ist diese Methode entsprechend umzustellen.
     */
    private Anchor createExportButton(List<ExcelColumn<SampleRow>> columns, ListDataProvider<SampleRow> dataProvider) {
        GridExcelExporter<SampleRow> exporter = new GridExcelExporter<>(SHEET_NAME, columns);
        StreamResource resource = new StreamResource(FILE_NAME, () -> toExcelStream(exporter, dataProvider));
        resource.setContentType(XLSX_MIME_TYPE);

        Anchor downloadLink = new Anchor(resource, "");
        downloadLink.getElement().setAttribute("download", true);
        downloadLink.add(new Button("Excel-Export", VaadinIcon.DOWNLOAD.create()));
        return downloadLink;
    }

    private InputStream toExcelStream(GridExcelExporter<SampleRow> exporter, ListDataProvider<SampleRow> dataProvider) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            exporter.export(dataProvider, buffer);
        } catch (IOException e) {
            throw new UncheckedIOException("Excel-Export fehlgeschlagen", e);
        }
        return new ByteArrayInputStream(buffer.toByteArray());
    }
}
