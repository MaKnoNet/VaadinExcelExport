package de.makno.vaadinexcelexport.app;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Generiert 25.000 Beispielzeilen für die Demo-Tabelle.
 *
 * <p>Die Zeilen werden einmalig beim Klassenlade-Zeitpunkt berechnet ({@code static final}) und
 * als unveränderliche Liste gehalten ({@link List#copyOf}). Damit ist der Zugriff thread-sicher
 * und das wiederholte Öffnen der UI erzeugt keinen Rechenaufwand.
 *
 * <p>{@link SampleRow} ist ein immutable Record – dieselben Instanzen können gefahrlos von
 * mehreren Vaadin-Sessions gleichzeitig gelesen werden.
 */
final class SampleData {

    static final int ROW_COUNT = 25_000;

    // ─── Wertebereiche für die Datengenerierung ────────────────────────────
    // WICHTIG: Diese Felder müssen VOR ROWS deklariert sein, da Java statische
    // Felder in Deklarationsreihenfolge initialisiert und ROWS.generateRows()
    // bereits auf NAMES/DEPARTMENTS/START_DATE zugreift.

    private static final String[] NAMES = {
        "Alice", "Bob", "Charlie", "Diana", "Erik", "Franziska",
        "Georg", "Hannah", "Ivan", "Julia", "Klaus", "Laura",
        "Michael", "Nina", "Otto", "Paula", "Quentin", "Rosa",
        "Stefan", "Tina", "Ulrich", "Vera", "Walter", "Xenia",
        "Yannick", "Zoe"
    };

    private static final String[] DEPARTMENTS = {
        "Engineering", "Marketing", "Sales", "Finance", "HR",
        "Operations", "Legal", "Research", "Support", "Management"
    };

    /** Startdatum für die Datumsspalte; 25.000 Zeilen decken ca. 5 Jahre ab. */
    private static final LocalDate START_DATE = LocalDate.of(2020, 1, 1);

    /** Unveränderliche, einmalig erzeugte Zeilen-Liste; thread-sicher. */
    private static final List<SampleRow> ROWS = generateRows();

    private SampleData() {}

    static List<SampleRow> rows() {
        return ROWS;
    }

    // ─── Generierung ──────────────────────────────────────────────────────

    private static List<SampleRow> generateRows() {
        List<SampleRow> rows = new ArrayList<>(ROW_COUNT);
        for (int i = 0; i < ROW_COUNT; i++) {
            rows.add(generateRow(i));
        }
        return List.copyOf(rows);
    }

    private static SampleRow generateRow(int i) {
        String text = NAMES[i % NAMES.length] + " / " + DEPARTMENTS[(i / NAMES.length) % DEPARTMENTS.length];

        // INTEGER: -1000 bis +1000
        int ganzzahl = (i % 2001) - 1000;

        // LONG: großer positiver und negativer Bereich
        long grosseZahl = (long) i * 123_456_789L - 1_500_000_000L;

        // DOUBLE: sinusförmig für sichtbare Variation im Grid
        double gleitkomma = Math.round(Math.sin(i * 0.1) * 1_000.0) / 10.0;

        // DECIMAL: 0,01 bis 50.000 €, zwei Nachkommastellen
        BigDecimal betrag = BigDecimal.valueOf((i % 50_000) + 1)
                .add(BigDecimal.valueOf((long) (i * 17) % 100, 2))
                .setScale(2, RoundingMode.HALF_UP);

        // BOOLEAN: 2/3 aktiv, 1/3 inaktiv
        boolean aktiv = (i % 3) != 2;

        // DATE: gleichmäßig über 5 Jahre verteilt
        LocalDate datum = START_DATE.plusDays(i % (5 * 365));

        // DATETIME: Datum + Tageszeit aus dem Index abgeleitet (06:00–19:59)
        LocalDateTime zeitstempel = datum.atTime(6 + (i % 14), (i * 7) % 60);

        // TIME (Sekunden): Ankunftszeit zwischen 06:00 und 09:59 Uhr
        int kommtSekunden = toSeconds(6 + (i % 4), (i * 13) % 60, (i * 7) % 60);

        return new SampleRow(text, ganzzahl, grosseZahl, gleitkomma, betrag, aktiv, datum, zeitstempel, kommtSekunden);
    }

    private static int toSeconds(int hours, int minutes, int secs) {
        return (int) LocalTime.of(hours, minutes, secs).toSecondOfDay();
    }
}
