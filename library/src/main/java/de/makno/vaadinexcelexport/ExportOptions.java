package de.makno.vaadinexcelexport;

import java.util.List;
import java.util.Objects;

/**
 * Unveränderliche Zusatzoptionen für einen Export über {@link GridExcelExporter}: Fußzeile(n),
 * Summenspalten und der optionale Pipeline-Parallelismus von xlsxbuilder. Bewusst als {@code record}
 * (immutable), damit der {@link GridExcelExporter} seinen Thread-Safety-Vertrag wahrt – die Optionen
 * werden pro Aufruf übergeben statt als veränderlicher Zustand gehalten.
 *
 * <p>Footer-Zeilen dürfen Platzhalter enthalten, die xlsxbuilder beim Schreiben auflöst – u. a.
 * {@code {datetime}}/{@code {date}}, {@code {rowCount}} und {@code {sum:Spaltenname}}. Damit
 * {@code {sum:…}} einen Wert liefert, muss die betreffende Spalte in {@link #sumColumns()} stehen
 * (das aktiviert in xlsxbuilder die Summenzeile und damit die Summenverfolgung).
 *
 * @param footerLines Fußzeilen unter den Daten (über die Breite gemerged); leer = keine
 * @param sumColumns  Spaltennamen, für die eine Summe gebildet wird (Summenzeile + {@code {sum:…}})
 * @param parallel    xlsxbuilder-Pipeline-Parallelismus aktivieren (Producer/Consumer-Entkopplung)
 */
public record ExportOptions(List<String> footerLines, List<String> sumColumns, boolean parallel) {

    private static final ExportOptions NONE = new ExportOptions(List.of(), List.of(), false);

    public ExportOptions {
        footerLines = List.copyOf(Objects.requireNonNull(footerLines, "footerLines"));
        sumColumns = List.copyOf(Objects.requireNonNull(sumColumns, "sumColumns"));
    }

    /** Keine Zusatzoptionen (keine Fußzeile, keine Summe, sequenziell). */
    public static ExportOptions none() {
        return NONE;
    }

    /** Kopie mit gesetzten Fußzeilen (kein {@code null}-Array, keine {@code null}-Zeilen). */
    public ExportOptions withFooter(String... lines) {
        Objects.requireNonNull(lines, "lines");
        return new ExportOptions(List.of(lines), sumColumns, parallel);
    }

    /** Kopie mit gesetzten Summenspalten (kein {@code null}-Array, keine {@code null}-Namen). */
    public ExportOptions withSumColumns(String... columns) {
        Objects.requireNonNull(columns, "columns");
        return new ExportOptions(footerLines, List.of(columns), parallel);
    }

    /** Kopie mit (de)aktiviertem Pipeline-Parallelismus. */
    public ExportOptions withParallel(boolean enabled) {
        return new ExportOptions(footerLines, sumColumns, enabled);
    }
}
