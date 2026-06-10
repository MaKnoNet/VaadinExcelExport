package de.makno.vaadinexcelexport;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.function.ValueProvider;
import de.makno.xlsxbuilder.ColumnType;
import java.util.Objects;
import java.util.function.Function;

/**
 * Hängt Excel-Export-Metadaten direkt an eine {@link Column} via {@link ComponentUtil}.
 *
 * <p>Damit entfällt jede separate Spaltenabstraktion: jede Grid-Spalte trägt ihre
 * Export-Konfiguration in sich und kann direkt von {@link GridExcelExporter#from(String,
 * com.vaadin.flow.component.grid.Grid)} ausgelesen werden. Spalten ohne
 * {@code ExcelMeta.type(...)} werden beim Export übersprungen.
 *
 * <p>Typische Verwendung:
 *
 * <pre>{@code
 * // Typisierte Spalte – ValueProvider explizit übergeben (empfohlen):
 * Column<SampleRow> col = grid.addColumn(SampleRow::datum)
 *         .setKey("Datum").setHeader("Datum").setAutoWidth(true);
 * ExcelMeta.type(col, ColumnType.DATE, SampleRow::datum).format("dd.mm.yyyy");
 *
 * // STRING – ValueProvider optional (Fallback über Renderer-Extraktion):
 * Column<SampleRow> text = grid.addColumn(SampleRow::text).setKey("Text").setHeader("Text");
 * ExcelMeta.type(text, ColumnType.STRING);
 *
 * // FORMULA – Grid zeigt berechneten Wert, Excel schreibt Formeltext:
 * Column<SampleRow> mwst = grid.addColumn(row -> row.betrag().multiply(VAT_RATE))
 *         .setKey("MwSt").setHeader("MwSt").setAutoWidth(true);
 * ExcelMeta.type(mwst, ColumnType.FORMULA, row -> "E{row}*0.19").format("#,##0.00 \"€\"");
 * }</pre>
 *
 * <p><b>Hintergrund:</b> Vaadin 24 erzeugt für {@code Grid.addColumn(ValueProvider)} intern
 * einen {@link com.vaadin.flow.component.grid.ColumnPathRenderer}, der den Wert als
 * {@link String} formatiert. Für typisierte Excel-Spalten (DATE, DECIMAL, BOOLEAN …) muss
 * der ursprüngliche, unformatierte {@link ValueProvider} daher explizit über
 * {@link #type(Column, ColumnType, ValueProvider)} übergeben werden.
 *
 * <p><b>Thread-Sicherheit:</b> Die gespeicherten Werte werden einmalig beim Aufbau gesetzt und
 * danach nur noch gelesen; Lesezugriff ist threadsicher.
 */
public final class ExcelMeta {

    /** Namespace-präfixierte Keys verhindern Kollisionen mit anderen Add-ons. */
    private static final String KEY_TYPE = "de.makno.excel.type";

    private static final String KEY_FORMAT = "de.makno.excel.format";
    private static final String KEY_CONVERTER = "de.makno.excel.converter";
    private static final String KEY_VALUE_PROVIDER = "de.makno.excel.valueProvider";
    private static final String KEY_GROUP = "de.makno.excel.group";

    private ExcelMeta() {}

    /**
     * Verknüpft {@link ColumnType} und {@link ValueProvider} mit der Spalte.
     *
     * <p>Für alle typisierten Spalten (DATE, DECIMAL, BOOLEAN, FORMULA …) ist dieser
     * Overload zu bevorzugen: Der übergeben {@code valueProvider} wird zur Export-Zeit
     * aufgerufen und liefert den typkorrekten Java-Wert direkt an xlsxbuilder.
     *
     * @param column        die Grid-Spalte
     * @param type          der xlsxbuilder-Spaltentyp
     * @param valueProvider liefert den Export-Wert (bei FORMULA: den Formeltext)
     */
    public static <T> Builder<T> type(Column<T> column, ColumnType type, ValueProvider<T, ?> valueProvider) {
        ComponentUtil.setData(column, KEY_TYPE, Objects.requireNonNull(type, "type"));
        ComponentUtil.setData(column, KEY_VALUE_PROVIDER, Objects.requireNonNull(valueProvider, "valueProvider"));
        return new Builder<>(column);
    }

    /**
     * Verknüpft nur den {@link ColumnType} mit der Spalte. Kein {@link ValueProvider} nötig.
     *
     * <p>Ohne expliziten Provider fällt {@link ColumnValueExtractor} auf den Grid-Renderer zurück,
     * der den Wert als {@link String} liefert. Geeignet für {@link ColumnType#STRING}. Für alle
     * anderen Typen empfiehlt sich {@link #type(Column, ColumnType, ValueProvider)}.
     *
     * @param column die Grid-Spalte
     * @param type   der xlsxbuilder-Spaltentyp
     */
    public static <T> Builder<T> type(Column<T> column, ColumnType type) {
        ComponentUtil.setData(column, KEY_TYPE, Objects.requireNonNull(type, "type"));
        return new Builder<>(column);
    }

    static ColumnType getType(Column<?> column) {
        return (ColumnType) ComponentUtil.getData(column, KEY_TYPE);
    }

    static String getFormat(Column<?> column) {
        return (String) ComponentUtil.getData(column, KEY_FORMAT);
    }

    @SuppressWarnings("unchecked")
    static <T> Function<Object, ?> getConverter(Column<T> column) {
        return (Function<Object, ?>) ComponentUtil.getData(column, KEY_CONVERTER);
    }

    @SuppressWarnings("unchecked")
    static <T> ValueProvider<T, ?> getValueProvider(Column<T> column) {
        return (ValueProvider<T, ?>) ComponentUtil.getData(column, KEY_VALUE_PROVIDER);
    }

    /**
     * Ordnet die Spalte einer Gruppe zu. Zusammenhängende Spalten mit demselben Label ergeben im
     * Export eine verbundene (gemergte) Kopfzelle über der Spaltenüberschrift (joined header).
     */
    public static void group(Column<?> column, String label) {
        ComponentUtil.setData(column, KEY_GROUP, label);
    }

    /** Gruppen-Label dieser Spalte für die verbundene Excel-Kopfzeile, oder {@code null}. */
    static String getGroup(Column<?> column) {
        return (String) ComponentUtil.getData(column, KEY_GROUP);
    }

    /** Fluent-Builder für optionale Export-Metadaten einer Spalte. */
    public static final class Builder<T> {

        private final Column<T> column;

        Builder(Column<T> column) {
            this.column = column;
        }

        /**
         * Setzt den Excel-Formatcode, z. B. {@code "dd.mm.yyyy"} oder {@code "#,##0.00 €"}.
         * Wird direkt an {@code xlsxbuilder.formatForType()} weitergereicht.
         */
        public Builder<T> format(String format) {
            ComponentUtil.setData(column, KEY_FORMAT, format);
            return this;
        }

        /**
         * Setzt einen Konverter, der den extrahierten Grid-Wert vor dem Schreiben in die
         * Excel-Zelle umwandelt (z. B. {@code int} → {@link java.time.LocalTime} für
         * {@link ColumnType#TIME}).
         */
        public Builder<T> converter(Function<Object, ?> converter) {
            ComponentUtil.setData(column, KEY_CONVERTER, converter);
            return this;
        }

        /** Wie {@link ExcelMeta#group(Column, String)}, fluent auf dem Builder. */
        public Builder<T> group(String label) {
            ExcelMeta.group(column, label);
            return this;
        }
    }
}
