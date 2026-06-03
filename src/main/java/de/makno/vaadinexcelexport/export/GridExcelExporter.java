package de.makno.vaadinexcelexport.export;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import de.makno.xlsbuilder.builder.DataProviders;
import de.makno.xlsbuilder.builder.ExcelBuilder;
import de.makno.xlsbuilder.builder.WorkbookBuilder;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Brücke zwischen einer Vaadin-Tabelle und dem xlsbuilder: erzeugt aus den {@link ExcelColumn
 * Tabellenspalten} und dem Vaadin-{@link DataProvider} der Tabelle eine {@code .xlsx}-Datei.
 *
 * <p>Bewusst entkoppelt (Dependency Inversion): hängt nur von der schlanken {@link ExcelColumn}-
 * Abstraktion und Vaadins {@link DataProvider}-Interface ab – nicht von der konkreten
 * {@code Grid}-Komponente. Dadurch ist der Export für beliebige Datenquellen wiederverwendbar.
 *
 * <p>Für den typischen Anwendungsfall, bei dem ein {@link Grid} bereits existiert, steht die
 * Factory-Methode {@link #from(String, Grid, List)} bereit: Sie liest die aktuelle Spaltenreihenfolge
 * und -sichtbarkeit direkt aus dem Grid heraus (Brücke über {@link Grid.Column#getKey()}) und
 * erspart die manuelle Übergabe der {@link ExcelColumn}-Liste in der richtigen Reihenfolge.
 *
 * <p>Die Daten werden über {@link DataProvider#fetch(Query)} gestreamt und via
 * {@link DataProviders#ofStream(Stream)} out-of-core an den xlsbuilder weitergereicht.
 *
 * <p><b>Thread-Sicherheit:</b> Die Instanz hält nur unveränderliche Felder und ist nach der
 * Konstruktion gefahrlos teilbar. {@link #export} arbeitet ausschließlich auf request-lokalen
 * Ressourcen (übergebener {@link OutputStream}, frisch erzeugter Stream/Builder) und hält keinen
 * veränderlichen Zustand.
 *
 * @param <T> Datentyp einer Tabellenzeile
 */
public final class GridExcelExporter<T> {

    private final String sheetName;
    private final List<ExcelColumn<T>> columns;

    public GridExcelExporter(String sheetName, List<ExcelColumn<T>> columns) {
        this.sheetName = Objects.requireNonNull(sheetName, "sheetName");
        this.columns = List.copyOf(Objects.requireNonNull(columns, "columns"));
        if (this.columns.isEmpty()) {
            throw new IllegalArgumentException("Mindestens eine Spalte erforderlich");
        }
    }

    /**
     * Erzeugt einen Exporter, der die aktuelle Spaltenreihenfolge und -sichtbarkeit des
     * übergebenen {@link Grid} übernimmt.
     *
     * <p>Voraussetzung: Jede {@link Grid.Column} muss über {@code setKey(excelColumn.header())}
     * mit der zugehörigen {@link ExcelColumn} verknüpft sein. Grid-Spalten ohne passenden Key
     * (kein Eintrag in {@code columns}) werden im Export übersprungen. Das erlaubt es, einzelne
     * Grid-Spalten vom Export auszunehmen, indem sie keinen Key bekommen.
     *
     * <p>Typische Verwendung in der View:
     * <pre>{@code
     * // Beim Aufbau des Grids:
     * grid.addColumn(col.gridValueProvider())
     *     .setKey(col.header())   // ← Brücke zu ExcelColumn
     *     .setHeader(col.header());
     *
     * // Beim Aufbau des Exporters:
     * GridExcelExporter<MyRow> exporter = GridExcelExporter.from("Blatt", grid, columns);
     * exporter.export(grid.getDataProvider(), outputStream);
     * }</pre>
     *
     * @param sheetName Name des Excel-Tabellenblatts
     * @param grid      das Vaadin-Grid, dessen aktuelle Spaltenreihenfolge übernommen wird
     * @param columns   alle verfügbaren {@link ExcelColumn}-Definitionen (als Lookup-Tabelle)
     */
    public static <T> GridExcelExporter<T> from(String sheetName, Grid<T> grid, List<ExcelColumn<T>> columns) {
        Objects.requireNonNull(grid, "grid");
        Map<String, ExcelColumn<T>> byKey =
                columns.stream().collect(Collectors.toMap(ExcelColumn::header, Function.identity()));
        List<ExcelColumn<T>> ordered = grid.getColumns().stream()
                .map(col -> byKey.get(col.getKey()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return new GridExcelExporter<>(sheetName, ordered);
    }

    /**
     * Schreibt die Tabelle als {@code .xlsx} in den Stream. Der Stream wird <em>nicht</em>
     * geschlossen – das obliegt dem Aufrufer (z. B. dem Vaadin-Download-Mechanismus).
     */
    public void export(DataProvider<T, ?> dataProvider, OutputStream out) throws IOException {
        Objects.requireNonNull(dataProvider, "dataProvider");
        Objects.requireNonNull(out, "out");
        WorkbookBuilder.create().sheet(buildSheet(dataProvider)).write(out);
    }

    private ExcelBuilder<T> buildSheet(DataProvider<T, ?> dataProvider) {
        ExcelBuilder<T> sheet = ExcelBuilder.<T>create().sheetName(sheetName).columnHeaders(true);
        for (ExcelColumn<T> column : columns) {
            // ofType/formatForType/convertToColumnType wirken jeweils auf die zuletzt per
            // column(...) hinzugefügte Spalte – daher direkt im Anschluss setzen.
            sheet.column(column.header(), column.valueExtractor()).ofType(column.type());
            if (column.format() != null) {
                sheet.formatForType(column.format());
            }
            if (column.converter() != null) {
                sheet.convertToColumnType(column.converter());
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
