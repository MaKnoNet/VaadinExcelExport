package de.makno.vaadinexcelexport.app;

import de.makno.vaadinexcelexport.export.ExcelColumn;
import de.makno.xlsbuilder.builder.ColumnType;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

/**
 * Einzige Quelle der Wahrheit für die Tabellenspalten: definiert je Excel-Datentyp genau eine Spalte.
 * Aus dieser Liste werden sowohl die Vaadin-{@code Grid}-Spalten als auch der Excel-Export gespeist.
 */
final class SampleColumns {

    /** Mehrwertsteuersatz (19 %) für die Formel- und Vorschauspalte. */
    private static final BigDecimal VAT_RATE = new BigDecimal("0.19");

    /**
     * Excel-Formel der MwSt-Spalte. {@code E} ist die Spalte „Betrag" (5. Spalte); der Platzhalter
     * {@code {row}} wird vom xlsbuilder durch die tatsächliche Zeilennummer ersetzt.
     */
    private static final String VAT_FORMULA = "E{row}*0.19";

    private SampleColumns() {}

    static List<ExcelColumn<SampleRow>> all() {
        return List.of(
                ExcelColumn.of("Text", ColumnType.STRING, SampleRow::text),
                ExcelColumn.of("Ganzzahl", ColumnType.INTEGER, SampleRow::ganzzahl),
                ExcelColumn.of("Große Zahl", ColumnType.LONG, SampleRow::grosseZahl),
                ExcelColumn.of("Gleitkomma", ColumnType.DOUBLE, SampleRow::gleitkomma)
                        .withFormat("0.0"),
                ExcelColumn.of("Betrag", ColumnType.DECIMAL, SampleRow::betrag).withFormat("#,##0.00 \"€\""),
                ExcelColumn.of("Aktiv", ColumnType.BOOLEAN, SampleRow::aktiv),
                ExcelColumn.of("Datum", ColumnType.DATE, SampleRow::datum).withFormat("dd.mm.yyyy"),
                ExcelColumn.of("Zeitstempel", ColumnType.DATETIME, SampleRow::zeitstempel)
                        .withFormat("dd.mm.yyyy hh:mm"),
                ExcelColumn.<SampleRow>of("Kommt", ColumnType.TIME, SampleRow::kommtSekunden)
                        .withConverter(seconds -> LocalTime.ofSecondOfDay(((Number) seconds).longValue()))
                        .withGridValue(row -> LocalTime.ofSecondOfDay(row.kommtSekunden())),
                ExcelColumn.<SampleRow>of("MwSt (Formel)", ColumnType.FORMULA, row -> VAT_FORMULA)
                        .withFormat("#,##0.00 \"€\"")
                        .withGridValue(row -> row.betrag().multiply(VAT_RATE)));
    }
}
