package de.makno.vaadinexcelexport.app;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.function.ValueProvider;
import de.makno.vaadinexcelexport.export.ExcelMeta;
import de.makno.xlsbuilder.builder.ColumnType;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Comparator;

/**
 * Richtet ein {@link Grid}{@code <SampleRow>} mit allen Beispielspalten und zugehörigen
 * {@link ExcelMeta}-Exportkonfigurationen ein. Wird von {@link MainView} genutzt, um die eine
 * Vergleichstabelle aufzubauen.
 *
 * <p>Jede Spalte erhält:
 * <ul>
 *   <li>einen Key (= Excel-Spaltenüberschrift),</li>
 *   <li>eine sichtbare Beschriftung,</li>
 *   <li>automatische Breite,</li>
 *   <li>einen natürlichen Sortier-Komparator, und</li>
 *   <li>{@link ExcelMeta}-Metadaten für den xlsbuilder-Export.</li>
 * </ul>
 *
 * <p>Typisierte Spalten (DATE, DECIMAL, BOOLEAN …) übergeben ihren {@link ValueProvider} direkt
 * an {@link ExcelMeta#type(Column, ColumnType, ValueProvider)}, damit {@link
 * de.makno.vaadinexcelexport.export.ColumnValueExtractor} den typkorrekten Java-Wert liefert –
 * ohne Umweg über die String-formatierte Renderer-Darstellung.
 */
final class SampleGrid {

    private static final String VAT_FORMULA = "E{row}*0.19";
    private static final BigDecimal VAT_RATE = new BigDecimal("0.19");

    private SampleGrid() {}

    /** Fügt dem Grid alle Beispielspalten hinzu. Das Grid muss bereits instanziiert sein. */
    static void configure(Grid<SampleRow> grid) {
        // STRING – 2-Param-Overload reicht (Fallback liefert String, der direkt verwendbar ist)
        addColumn(grid, "Text", ColumnType.STRING, SampleRow::text, null);

        addColumn(grid, "Ganzzahl", ColumnType.INTEGER, SampleRow::ganzzahl, null);
        addColumn(grid, "Große Zahl", ColumnType.LONG, SampleRow::grosseZahl, null);
        addColumn(grid, "Gleitkomma", ColumnType.DOUBLE, SampleRow::gleitkomma, "0.0");
        addColumn(grid, "Betrag", ColumnType.DECIMAL, SampleRow::betrag, "#,##0.00 \"€\"");
        addColumn(grid, "Aktiv", ColumnType.BOOLEAN, SampleRow::aktiv, null);
        addColumn(grid, "Datum", ColumnType.DATE, SampleRow::datum, "dd.mm.yyyy");
        addColumn(grid, "Zeitstempel", ColumnType.DATETIME, SampleRow::zeitstempel, "dd.mm.yyyy hh:mm");
        addTimeColumn(grid);
        addFormulaColumn(grid);
        // Hyperlink-Spalte bewusst ALS LETZTE, damit die Spaltenpositionen davor unverändert
        // bleiben (die VAT-Formel referenziert weiterhin Spalte E = Betrag).
        addHyperlinkColumn(grid);
    }

    /**
     * Hilfsmethode: Spalte hinzufügen, Key/Header setzen, ExcelMeta mit ValueProvider anhängen.
     * Der {@code provider} wird sowohl als Grid-Anzeigewert als auch als Export-Wert verwendet.
     */
    private static <V extends Comparable<? super V>> void addColumn(
            Grid<SampleRow> grid, String label, ColumnType type, ValueProvider<SampleRow, V> provider, String format) {
        Column<SampleRow> col = grid.addColumn(provider)
                .setKey(label)
                .setHeader(label)
                .setAutoWidth(true)
                .setComparator(naturalComparator(provider));
        ExcelMeta.Builder<SampleRow> meta = ExcelMeta.type(col, type, provider);
        if (format != null) {
            meta.format(format);
        }
    }

    /**
     * Kommt-Spalte: Grid zeigt {@link LocalTime}, xlsbuilder empfängt denselben {@link LocalTime}
     * direkt – kein Converter nötig, da der Grid-Wert bereits der Excel-konforme Typ ist.
     */
    private static void addTimeColumn(Grid<SampleRow> grid) {
        ValueProvider<SampleRow, LocalTime> provider = row -> LocalTime.ofSecondOfDay(row.kommtSekunden());
        Column<SampleRow> col = grid.addColumn(provider)
                .setKey("Kommt")
                .setHeader("Kommt")
                .setAutoWidth(true)
                .setComparator(naturalComparator(provider));
        ExcelMeta.type(col, ColumnType.TIME, provider);
    }

    /**
     * MwSt-Spalte: Grid zeigt den berechneten Wert ({@link BigDecimal}), Excel schreibt die
     * Formel {@code "E{row}*0.19"}. Der Export-Provider liefert den Formeltext; der Grid-Provider
     * (Anzeigewert) ist nur im Grid registriert.
     */
    private static void addFormulaColumn(Grid<SampleRow> grid) {
        ValueProvider<SampleRow, BigDecimal> displayProvider =
                row -> row.betrag().multiply(VAT_RATE);
        Column<SampleRow> col = grid.addColumn(displayProvider)
                .setKey("MwSt (Formel)")
                .setHeader("MwSt (Formel)")
                .setAutoWidth(true)
                .setComparator(naturalComparator(displayProvider));
        // Export-Provider liefert Formeltext; displayProvider bleibt im Grid.
        ExcelMeta.type(col, ColumnType.FORMULA, row -> VAT_FORMULA).format("#,##0.00 \"€\"");
    }

    /**
     * Hyperlink-Spalte: das Grid zeigt einen klickbaren Link, dessen <b>Anzeigetext der Name</b>
     * ({@code webseiteName}) ist – nicht die URL. Für den Export schreibt xlsbuilder über den
     * expliziten {@link ExcelMeta}-{@link ValueProvider} eine {@code HYPERLINK(ziel, name)}-Formel
     * (in Excel klickbar mit dem Namen als Text). Der {@link LitRenderer} bindet zwei Properties
     * ({@code url}, {@code name}); Flowingcode nutzt davon das Property {@code name} und exportiert
     * somit ebenfalls den Anzeigenamen.
     */
    private static void addHyperlinkColumn(Grid<SampleRow> grid) {
        Column<SampleRow> col = grid.addColumn(LitRenderer.<SampleRow>of(
                                "<a href=\"${item.url}\" target=\"_blank\" rel=\"noopener\">${item.name}</a>")
                        .withProperty("url", SampleRow::webseite)
                        .withProperty("name", SampleRow::webseiteName))
                .setKey("Webseite")
                .setHeader("Webseite")
                .setAutoWidth(true)
                .setComparator(naturalComparator(SampleRow::webseiteName));
        ExcelMeta.type(col, ColumnType.FORMULA, row -> hyperlinkFormula(row.webseite(), row.webseiteName()));
    }

    /** Baut eine Excel-{@code HYPERLINK(ziel, anzeigename)}-Formel (ohne führendes {@code =}). */
    private static String hyperlinkFormula(String url, String name) {
        return "HYPERLINK(\"" + url + "\",\"" + name + "\")";
    }

    /**
     * Erzeugt einen natürlichen Komparator für eine Grid-Spalte. Der Rückgabewert des
     * {@link ValueProvider} muss {@link Comparable} implementieren. Null-Werte werden an den
     * Anfang einsortiert.
     */
    @SuppressWarnings("unchecked")
    static <T> Comparator<T> naturalComparator(ValueProvider<T, ?> provider) {
        return Comparator.comparing(
                row -> (Comparable<Object>) provider.apply(row), Comparator.nullsFirst(Comparator.naturalOrder()));
    }
}
