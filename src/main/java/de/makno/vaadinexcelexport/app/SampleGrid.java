package de.makno.vaadinexcelexport.app;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.HeaderRow.HeaderCell;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.function.ValueProvider;
import de.makno.vaadinexcelexport.export.ExcelMeta;
import de.makno.xlsxbuilder.builder.ColumnType;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Richtet das eine {@link Grid}{@code <SampleRow>} der Vergleichsansicht ein: Spalten,
 * {@link ExcelMeta}-Exportkonfiguration und – für die SQL-gestützte, lazy Anzeige – die
 * Sortier-Properties (DB-Spaltennamen) je Spalte.
 *
 * <p>Da die Daten lazy aus der Datenbank geladen werden, erfolgt die Sortierung nicht über
 * In-Memory-Comparatoren, sondern über {@link Column#setSortProperty(String...)} (DB-Spalte). Aus
 * der aktuellen Grid-Sortierung baut diese Klasse die {@code ORDER BY}-Klausel sowohl für die
 * Seiten­abfragen ({@link #orderByForQuery(Query)}) als auch für den Stream-Export
 * ({@link #orderByForKeys(List)}).
 */
final class SampleGrid {

    private static final String VAT_FORMULA = "E{row}*0.19";
    private static final BigDecimal VAT_RATE = new BigDecimal("0.19");

    /** Excel-Header (= Spalten-Key) → DB-Spaltenname (für die Sortierung). */
    private static final Map<String, String> DB_COLUMN_BY_KEY = Map.ofEntries(
            Map.entry("Text", "text"),
            Map.entry("Ganzzahl", "ganzzahl"),
            Map.entry("Große Zahl", "grosse_zahl"),
            Map.entry("Gleitkomma", "gleitkomma"),
            Map.entry("Betrag", "betrag"),
            Map.entry("Aktiv", "aktiv"),
            Map.entry("Datum", "datum"),
            Map.entry("Zeitstempel", "zeitstempel"),
            Map.entry("Kommt", "kommt_sekunden"),
            Map.entry("MwSt (Formel)", "betrag"),
            Map.entry("Webseite", "webseite_name"));

    /** Whitelist erlaubter ORDER-BY-Spalten (gegen SQL-Injection); inkl. PK {@code id}. */
    private static final Set<String> ALLOWED_COLUMNS;

    static {
        Set<String> allowed = new HashSet<>(DB_COLUMN_BY_KEY.values());
        allowed.add("id");
        ALLOWED_COLUMNS = Set.copyOf(allowed);
    }

    /**
     * Spaltengruppen für die verbundene Kopfzeile (joined header) – je Gruppe Label und die
     * zusammenhängenden Spalten-Keys in Grid-Reihenfolge. Treibt sowohl die Anzeige
     * ({@link Grid#prependHeaderRow()}/{@link HeaderRow#join}) als auch den Export
     * ({@link ExcelMeta#group}).
     */
    private static final List<Map.Entry<String, List<String>>> HEADER_GROUPS = List.of(
            Map.entry("Beschreibung", List.of("Text")),
            Map.entry("Zahlen", List.of("Ganzzahl", "Große Zahl", "Gleitkomma", "Betrag")),
            Map.entry("Status", List.of("Aktiv")),
            Map.entry("Zeit", List.of("Datum", "Zeitstempel", "Kommt")),
            Map.entry("Berechnung & Link", List.of("MwSt (Formel)", "Webseite")));

    /**
     * Alternative Export-Spaltenreihenfolge für die Demo: stellt die Gruppe „Berechnung &amp; Link"
     * (MwSt, Webseite) nach vorne und lässt alle Gruppen zusammenhängend (verbundene Kopfzeile bleibt
     * korrekt). Übergeben an {@link de.makno.vaadinexcelexport.export.GridExcelExporter#from(String,
     * Grid, List)}.
     */
    static final List<String> EXPORT_ORDER_LINK_FIRST = List.of(
            "MwSt (Formel)",
            "Webseite",
            "Text",
            "Ganzzahl",
            "Große Zahl",
            "Gleitkomma",
            "Betrag",
            "Aktiv",
            "Datum",
            "Zeitstempel",
            "Kommt");

    private SampleGrid() {}

    /** Fügt dem Grid alle Beispielspalten hinzu. Das Grid muss bereits instanziiert sein. */
    static void configure(Grid<SampleRow> grid) {
        addColumn(grid, "Text", "text", ColumnType.STRING, SampleRow::text, null);
        addColumn(grid, "Ganzzahl", "ganzzahl", ColumnType.INTEGER, SampleRow::ganzzahl, null);
        addColumn(grid, "Große Zahl", "grosse_zahl", ColumnType.LONG, SampleRow::grosseZahl, null);
        addColumn(grid, "Gleitkomma", "gleitkomma", ColumnType.DOUBLE, SampleRow::gleitkomma, "0.0");
        addColumn(grid, "Betrag", "betrag", ColumnType.DECIMAL, SampleRow::betrag, "#,##0.00 \"€\"");
        addColumn(grid, "Aktiv", "aktiv", ColumnType.BOOLEAN, SampleRow::aktiv, null);
        addColumn(grid, "Datum", "datum", ColumnType.DATE, SampleRow::datum, "dd.mm.yyyy");
        addColumn(grid, "Zeitstempel", "zeitstempel", ColumnType.DATETIME, SampleRow::zeitstempel, "dd.mm.yyyy hh:mm");
        addTimeColumn(grid);
        addFormulaColumn(grid);
        addHyperlinkColumn(grid);
        applyHeaderGroups(grid);
    }

    /**
     * Setzt je Spalte das Export-Gruppen-Label ({@link ExcelMeta#group}) und legt für die Anzeige eine
     * verbundene Kopfzeile darüber ({@link Grid#prependHeaderRow()} mit {@link HeaderRow#join}), sodass
     * Tabelle und Export dieselbe Gruppierung zeigen. Berührt Daten/Sortierung/Filter nicht.
     */
    private static void applyHeaderGroups(Grid<SampleRow> grid) {
        HeaderRow top = grid.prependHeaderRow();
        for (Map.Entry<String, List<String>> group : HEADER_GROUPS) {
            String label = group.getKey();
            List<Column<SampleRow>> cols =
                    group.getValue().stream().map(grid::getColumnByKey).toList();
            cols.forEach(col -> ExcelMeta.group(col, label));
            if (cols.size() == 1) {
                top.getCell(cols.get(0)).setText(label);
            } else {
                HeaderCell[] cells = cols.stream().map(top::getCell).toArray(HeaderCell[]::new);
                top.join(cells).setText(label);
            }
        }
    }

    /**
     * Hilfsmethode: Spalte hinzufügen, Key/Header + Sortier-Property (DB-Spalte) setzen und
     * ExcelMeta mit ValueProvider anhängen. Der {@code provider} dient als Grid-Anzeigewert und als
     * Export-Wert.
     */
    private static <V> void addColumn(
            Grid<SampleRow> grid,
            String label,
            String dbColumn,
            ColumnType type,
            ValueProvider<SampleRow, V> provider,
            String format) {
        Column<SampleRow> col = grid.addColumn(provider)
                .setKey(label)
                .setHeader(label)
                .setAutoWidth(true)
                .setSortProperty(dbColumn);
        ExcelMeta.Builder<SampleRow> meta = ExcelMeta.type(col, type, provider);
        if (format != null) {
            meta.format(format);
        }
    }

    /** Kommt-Spalte: Grid zeigt {@link LocalTime}; Sortierung über die DB-Spalte {@code kommt_sekunden}. */
    private static void addTimeColumn(Grid<SampleRow> grid) {
        ValueProvider<SampleRow, LocalTime> provider = row -> LocalTime.ofSecondOfDay(row.kommtSekunden());
        Column<SampleRow> col = grid.addColumn(provider)
                .setKey("Kommt")
                .setHeader("Kommt")
                .setAutoWidth(true)
                .setSortProperty("kommt_sekunden");
        ExcelMeta.type(col, ColumnType.TIME, provider);
    }

    /**
     * MwSt-Spalte: Grid zeigt den berechneten Wert ({@link BigDecimal}), Excel schreibt die Formel
     * {@code "E{row}*0.19"}. Sortiert wird über {@code betrag} (monoton zur MwSt).
     */
    private static void addFormulaColumn(Grid<SampleRow> grid) {
        ValueProvider<SampleRow, BigDecimal> displayProvider =
                row -> row.betrag().multiply(VAT_RATE);
        Column<SampleRow> col = grid.addColumn(displayProvider)
                .setKey("MwSt (Formel)")
                .setHeader("MwSt (Formel)")
                .setAutoWidth(true)
                .setSortProperty("betrag");
        ExcelMeta.type(col, ColumnType.FORMULA, row -> VAT_FORMULA).format("#,##0.00 \"€\"");
    }

    /**
     * Hyperlink-Spalte: das Grid zeigt einen klickbaren Link mit dem <b>Anzeigenamen</b> als Text;
     * xlsxbuilder exportiert eine {@code HYPERLINK(ziel, name)}-Formel. Sortiert wird über
     * {@code webseite_name}.
     */
    private static void addHyperlinkColumn(Grid<SampleRow> grid) {
        Column<SampleRow> col = grid.addColumn(LitRenderer.<SampleRow>of(
                                "<a href=\"${item.url}\" target=\"_blank\" rel=\"noopener\">${item.name}</a>")
                        .withProperty("url", SampleRow::webseite)
                        .withProperty("name", SampleRow::webseiteName))
                .setKey("Webseite")
                .setHeader("Webseite")
                .setAutoWidth(true)
                .setSortProperty("webseite_name");
        ExcelMeta.type(col, ColumnType.FORMULA, row -> hyperlinkFormula(row.webseite(), row.webseiteName()));
    }

    /** Baut eine Excel-{@code HYPERLINK(ziel, anzeigename)}-Formel (ohne führendes {@code =}). */
    private static String hyperlinkFormula(String url, String name) {
        return "HYPERLINK(\"" + url + "\",\"" + name + "\")";
    }

    // ─────────────────────────────────────────── ORDER BY aus der Grid-Sortierung

    /**
     * Baut die {@code ORDER BY}-Klausel für eine lazy Seitenabfrage aus den Sort-Properties der
     * {@link Query}. Ohne (gültige) Sortierung wird stabil nach dem Primärschlüssel sortiert.
     */
    static String orderByForQuery(Query<SampleRow, ?> query) {
        StringJoiner terms = new StringJoiner(", ");
        for (QuerySortOrder order : query.getSortOrders()) {
            appendTerm(terms, order.getSorted(), order.getDirection());
        }
        return finishOrderBy(terms);
    }

    /**
     * Baut die {@code ORDER BY}-Klausel für den Stream-Export aus der aktuellen Grid-Sortierung
     * ({@link Grid#getSortOrder()}). So entspricht die Reihenfolge der Excel-Datei der Tabelle.
     */
    static String orderByForKeys(List<GridSortOrder<SampleRow>> sortOrders) {
        StringJoiner terms = new StringJoiner(", ");
        for (GridSortOrder<SampleRow> order : sortOrders) {
            appendTerm(terms, DB_COLUMN_BY_KEY.get(order.getSorted().getKey()), order.getDirection());
        }
        return finishOrderBy(terms);
    }

    private static void appendTerm(StringJoiner terms, String column, SortDirection direction) {
        if (column == null || !ALLOWED_COLUMNS.contains(column)) {
            return; // defensiv: nur bekannte Spalten zulassen
        }
        terms.add(column + (direction == SortDirection.DESCENDING ? " DESC" : " ASC"));
    }

    private static String finishOrderBy(StringJoiner terms) {
        String body = terms.toString();
        return body.isEmpty() ? "ORDER BY id" : "ORDER BY " + body;
    }
}
