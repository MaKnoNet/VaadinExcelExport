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
import java.util.Comparator;
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
 * Grid-Renderer extrahiert. Für typisierte Spalten und Sonderfälle (z. B.
 * {@link de.makno.xlsbuilder.builder.ColumnType#FORMULA}) wird der Export-Wert explizit über
 * {@link ExcelMeta#type(Column, de.makno.xlsbuilder.builder.ColumnType, com.vaadin.flow.function.ValueProvider)}
 * gesetzt.
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
     * Schreibt die Tabelle <em>unsortiert</em> (in Datenbestand-Reihenfolge) als {@code .xlsx} in
     * den Stream. Kurzform für {@link #export(DataProvider, Comparator, OutputStream)} mit
     * {@code null}-Sortierung. Der Stream wird <em>nicht</em> geschlossen.
     */
    public void export(DataProvider<T, ?> dataProvider, OutputStream out) throws IOException {
        export(dataProvider, null, out);
    }

    /**
     * Schreibt die Tabelle als {@code .xlsx} in den Stream und übernimmt dabei die übergebene
     * In-Memory-Sortierung, sodass die Zeilenreihenfolge der Excel-Datei der sortierten Tabelle
     * entspricht. xlsbuilder schreibt die Zeilen genau in Stream-Reihenfolge – die Reihenfolge wird
     * also allein durch den {@code inMemorySort}-Comparator bestimmt.
     *
     * @param dataProvider Datenquelle der Tabelle
     * @param inMemorySort In-Memory-Sortierung (z. B. {@code grid.getDataCommunicator()
     *                     .getInMemorySorting()}); {@code null} = unsortiert
     * @param out          Ziel-Stream (wird nicht geschlossen)
     */
    public void export(DataProvider<T, ?> dataProvider, Comparator<T> inMemorySort, OutputStream out)
            throws IOException {
        Objects.requireNonNull(dataProvider, "dataProvider");
        Objects.requireNonNull(out, "out");
        de.makno.xlsbuilder.builder.DataProvider<T> data = DataProviders.ofStream(fetchAll(dataProvider, inMemorySort));
        export(data, out);
    }

    /**
     * Schreibt die Tabelle als {@code .xlsx} und bezieht die Daten direkt aus einer
     * xlsbuilder-{@link de.makno.xlsbuilder.builder.DataProvider Datenquelle} – etwa einem
     * gestreamten JDBC-{@code ResultSet} via
     * {@link DataProviders#ofResultSet(java.sql.ResultSet, de.makno.xlsbuilder.builder.ResultSetRowMapper)}.
     * So lässt sich <b>out-of-core</b> exportieren, ohne den gesamten Datenbestand in den Speicher
     * zu laden. Die Spaltendefinitionen stammen weiterhin aus dem {@link Grid}.
     *
     * <p>xlsbuilder durchläuft die Quelle genau einmal (forward-only) und schließt sie nach dem
     * Schreiben ({@link de.makno.xlsbuilder.builder.DataProvider#close()}). Ein gehaltenes
     * {@code Statement}/{@code Connection} muss der Aufrufer schließen.
     *
     * @param data xlsbuilder-Datenquelle (Reihenfolge = Schreibreihenfolge)
     * @param out  Ziel-Stream (wird nicht geschlossen)
     */
    public void export(de.makno.xlsbuilder.builder.DataProvider<T> data, OutputStream out) throws IOException {
        Objects.requireNonNull(data, "data");
        Objects.requireNonNull(out, "out");
        WorkbookBuilder.create().sheet(newSheetWithColumns().data(data)).write(out);
    }

    /** Baut das Sheet mit allen Spaltendefinitionen (Header, Typ, Format, Converter) – ohne Daten. */
    private ExcelBuilder<T> newSheetWithColumns() {
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
        return sheet;
    }

    /**
     * Holt alle Zeilen des Vaadin-DataProviders als Stream. Die {@link Query} überträgt die
     * In-Memory-Sortierung (offset 0, unbeschränktes Limit, kein Filter); {@code null} = unsortiert.
     * Der rohe Cast überbrückt den Wildcard-Filtertyp von {@code DataProvider<T, ?>}.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> Stream<T> fetchAll(DataProvider<T, ?> dataProvider, Comparator<T> inMemorySort) {
        Query query = new Query(0, Integer.MAX_VALUE, null, inMemorySort, null);
        return ((DataProvider) dataProvider).fetch(query);
    }
}
