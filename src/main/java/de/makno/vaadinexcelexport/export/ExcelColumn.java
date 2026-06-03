package de.makno.vaadinexcelexport.export;

import com.vaadin.flow.function.ValueProvider;
import de.makno.xlsbuilder.builder.ColumnType;
import java.util.Objects;
import java.util.function.Function;

/**
 * Schlanke, von der konkreten {@code Grid}-Komponente entkoppelte Beschreibung <em>einer</em>
 * Tabellenspalte. Eine Spalte speist sowohl die Anzeige im Vaadin-{@code Grid} als auch den
 * Excel-Export ({@link GridExcelExporter}) – die Spalten sind damit die einzige Quelle der Wahrheit
 * (keine doppelte Definition für UI und Export).
 *
 * <p>Aufbau über statische Factory plus „Wither"-Methoden, z. B.:
 * <pre>{@code
 * ExcelColumn.of("Betrag", ColumnType.DECIMAL, Order::amount).withFormat("#,##0.00 \"€\"");
 * }</pre>
 *
 * <p>Der {@code valueExtractor} ist ein {@link ValueProvider} (serialisierbar) und zugleich eine
 * {@link Function} – er wird unverändert an den xlsbuilder weitergereicht.
 *
 * @param <T> Datentyp einer Tabellenzeile
 * @param header        Spaltenüberschrift
 * @param type          Excel-Datentyp der Spalte
 * @param format        Excel-Format-Code oder {@code null} (Standardformat des Typs)
 * @param valueExtractor liefert den Export-Wert (bei {@link ColumnType#FORMULA} den Formeltext)
 * @param converter     optionaler Konverter des Rohwerts in den Zieltyp oder {@code null}
 *                      (z. B. Sekunden → {@link java.time.LocalTime} für {@link ColumnType#TIME})
 * @param gridValue     optionaler abweichender Anzeige-Provider fürs {@code Grid} oder {@code null}
 *                      (Default: {@code valueExtractor})
 */
public record ExcelColumn<T>(
        String header,
        ColumnType type,
        String format,
        ValueProvider<T, ?> valueExtractor,
        Function<Object, ?> converter,
        ValueProvider<T, ?> gridValue) {

    public ExcelColumn {
        Objects.requireNonNull(header, "header");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(valueExtractor, "valueExtractor");
    }

    /** Erzeugt eine Spalte ohne Format, Konverter oder abweichende Grid-Anzeige. */
    public static <T> ExcelColumn<T> of(String header, ColumnType type, ValueProvider<T, ?> valueExtractor) {
        return new ExcelColumn<>(header, type, null, valueExtractor, null, null);
    }

    /** Setzt den Excel-Format-Code (z. B. {@code "#,##0.00"} oder {@code "dd.mm.yyyy"}). */
    public ExcelColumn<T> withFormat(String format) {
        return new ExcelColumn<>(header, type, format, valueExtractor, converter, gridValue);
    }

    /** Setzt den Konverter, der den Rohwert vor dem Schreiben in den Zieltyp umwandelt. */
    public ExcelColumn<T> withConverter(Function<Object, ?> converter) {
        return new ExcelColumn<>(header, type, format, valueExtractor, converter, gridValue);
    }

    /** Setzt einen abweichenden Anzeige-Provider für das {@code Grid}. */
    public ExcelColumn<T> withGridValue(ValueProvider<T, ?> gridValue) {
        return new ExcelColumn<>(header, type, format, valueExtractor, converter, gridValue);
    }

    /** Anzeige-Provider für das {@code Grid}; fällt auf den Export-Extractor zurück. */
    public ValueProvider<T, ?> gridValueProvider() {
        return gridValue != null ? gridValue : valueExtractor;
    }
}
