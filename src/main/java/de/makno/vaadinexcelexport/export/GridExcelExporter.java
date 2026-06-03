package de.makno.vaadinexcelexport.export;

import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import de.makno.xlsbuilder.builder.DataProviders;
import de.makno.xlsbuilder.builder.ExcelBuilder;
import de.makno.xlsbuilder.builder.WorkbookBuilder;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Brücke zwischen einer Vaadin-Tabelle und dem xlsbuilder: erzeugt aus den {@link ExcelColumn
 * Tabellenspalten} und dem Vaadin-{@link DataProvider} der Tabelle eine {@code .xlsx}-Datei.
 *
 * <p>Bewusst entkoppelt (Dependency Inversion): hängt nur von der schlanken {@link ExcelColumn}-
 * Abstraktion und Vaadins {@link DataProvider}-Interface ab – nicht von der konkreten
 * {@code Grid}-Komponente. Dadurch ist der Export für beliebige Datenquellen wiederverwendbar.
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
