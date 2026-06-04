package de.makno.vaadinexcelexport.export;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import de.makno.xlsbuilder.builder.DataProviders;
import de.makno.xlsbuilder.builder.ExcelBuilder;
import de.makno.xlsbuilder.builder.WorkbookBuilder;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Brücke zwischen einer Vaadin-Tabelle und dem xlsbuilder: exportiert alle per
 * {@link ExcelMeta#type(Column, de.makno.xlsbuilder.builder.ColumnType)} annotierten
 * Grid-Spalten als {@code .xlsx}-Datei.
 *
 * <p>Der Wert jeder Spalte wird durch {@link ColumnValueExtractor} automatisch aus dem
 * Grid-Renderer extrahiert – ein expliziter {@link com.vaadin.flow.function.ValueProvider} ist
 * nicht mehr nötig. Für Sonderfälle (z. B. {@link de.makno.xlsbuilder.builder.ColumnType#FORMULA})
 * kann via {@link ExcelMeta.Builder#valueProvider} ein Override gesetzt werden.
 *
 * <p>Verwendung:
 *
 * <pre>{@code
 * // 1. Grid aufbauen und Spalten mit ExcelMeta annotieren:
 * Column<SampleRow> col = grid.addColumn(SampleRow::datum)
 *         .setKey("Datum").setHeader("Datum");
 * ExcelMeta.type(col, ColumnType.DATE).format("dd.mm.yyyy");
 *
 * // 2. Exporter erzeugen – liest Spaltenreihenfolge und Metadaten direkt aus dem Grid:
 * GridExcelExporter<SampleRow> exporter = GridExcelExporter.from("Blatt", grid);
 *
 * // 3. Exportieren:
 * exporter.export(grid.getDataProvider(), outputStream);
 * }</pre>
 *
 * <p><b>Thread-Sicherheit:</b> Die Instanz hält nur unveränderliche Felder und ist nach der
 * Konstruktion gefahrlos teilbar. {@link #export} arbeitet ausschließlich auf request-lokalen
 * Ressourcen (übergebener {@link OutputStream}, frisch erzeugter {@link ColumnValueExtractor})
 * und hält keinen veränderlichen Zustand.
 *
 * @param <T> Datentyp einer Tabellenzeile
 */
public final class GridExcelExporter<T> {

    private final String sheetName;
    private final List<Column<T>> columns;

    private GridExcelExporter(String sheetName, List<Column<T>> columns) {
        this.sheetName = Objects.requireNonNull(sheetName, "sheetName");
        this.columns = List.copyOf(Objects.requireNonNull(columns, "columns"));
        if (this.columns.isEmpty()) {
            throw new IllegalArgumentException(
                    "Mindestens eine Spalte mit ExcelMeta.type(...) und Column.setKey() erforderlich");
        }
    }

    /**
     * Erzeugt einen Exporter für alle {@link ExcelMeta}-annotierten Spalten des übergebenen
     * {@link Grid}. Die Spaltenreihenfolge entspricht der im Grid sichtbaren Reihenfolge.
     *
     * <p>Eine Spalte wird exportiert, wenn sie:
     * <ul>
     *   <li>einen Key hat ({@link Column#getKey()} ≠ {@code null}) – der Key dient als
     *       Excel-Spaltenüberschrift, und</li>
     *   <li>per {@link ExcelMeta#type(Column, de.makno.xlsbuilder.builder.ColumnType)} annotiert
     *       wurde.</li>
     * </ul>
     *
     * @throws IllegalArgumentException wenn keine exportierbare Spalte gefunden wurde
     */
    public static <T> GridExcelExporter<T> from(String sheetName, Grid<T> grid) {
        Objects.requireNonNull(grid, "grid");
        List<Column<T>> exportable = grid.getColumns().stream()
                .filter(col -> col.getKey() != null && ExcelMeta.getType(col) != null)
                .collect(Collectors.toList());
        return new GridExcelExporter<>(sheetName, exportable);
    }

    /**
     * Schreibt die Tabelle als {@code .xlsx} in den Stream. Der Stream wird <em>nicht</em>
     * geschlossen – das obliegt dem Aufrufer.
     */
    public void export(DataProvider<T, ?> dataProvider, OutputStream out) throws IOException {
        Objects.requireNonNull(dataProvider, "dataProvider");
        Objects.requireNonNull(out, "out");
        WorkbookBuilder.create().sheet(buildSheet(dataProvider)).write(out);
    }

    private ExcelBuilder<T> buildSheet(DataProvider<T, ?> dataProvider) {
        ExcelBuilder<T> sheet = ExcelBuilder.<T>create().sheetName(sheetName).columnHeaders(true);
        ColumnValueExtractor<T> extractor = new ColumnValueExtractor<>();

        for (Column<T> column : columns) {
            // column.getKey() ist als Spaltenüberschrift definiert (equals der setKey-Wert)
            sheet.column(column.getKey(), item -> extractor.extract(item, column))
                    .ofType(ExcelMeta.getType(column));

            String format = ExcelMeta.getFormat(column);
            if (format != null) {
                sheet.formatForType(format);
            }
            Function<Object, ?> converter = ExcelMeta.getConverter(column);
            if (converter != null) {
                sheet.convertToColumnType(converter);
            }
        }
        return sheet.data(DataProviders.ofStream(fetchAll(dataProvider)));
    }

    /**
     * Holt alle Zeilen des Vaadin-DataProviders als Stream. Eine unbeschränkte {@link Query}
     * liefert den gesamten (ungefilterten, unsortierten) Datenbestand. Der rohe Cast überbrückt
     * den Wildcard-Filtertyp von {@code DataProvider<T, ?>}.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> Stream<T> fetchAll(DataProvider<T, ?> dataProvider) {
        return ((DataProvider) dataProvider).fetch(new Query());
    }
}
