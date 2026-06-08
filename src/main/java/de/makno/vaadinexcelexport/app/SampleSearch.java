package de.makno.vaadinexcelexport.app;

import java.util.List;

/**
 * Uebersetzt den Freitext-Suchbegriff der Grid-Suche in eine parametrisierte SQL-{@code WHERE}-Klausel
 * fuer die Tabelle {@code testdata}. Gefiltert wird case-insensitive ueber die Spalten {@code text}
 * und {@code webseite_name} ({@code LIKE}).
 *
 * <p><b>SQL-Injektionssicher:</b> Die Spaltennamen sind fest verdrahtet, der Suchbegriff wird
 * ausschliesslich als {@code PreparedStatement}-Parameter gebunden (nie in SQL eingesetzt).
 * LIKE-Sonderzeichen ({@code %}, {@code _}, {@code !}) im Begriff werden mit {@code ESCAPE '!'}
 * neutralisiert, damit sie als Literale suchen.
 *
 * <p><b>Thread-Sicherheit:</b> zustandslos (reine statische Funktion).
 */
final class SampleSearch {

    /** Vollstaendige, an {@code testdata} angepasste WHERE-Klausel (case-insensitive, beide Spalten). */
    private static final String LIKE_CLAUSE =
            " WHERE (LOWER(text) LIKE ? ESCAPE '!' OR LOWER(webseite_name) LIKE ? ESCAPE '!')";

    private static final char ESCAPE = '!';

    private SampleSearch() {}

    /**
     * Ergebnis der Filteruebersetzung: SQL-Fragment (leer = kein Filter) und die zugehoerigen
     * Parameter in Bindungsreihenfolge.
     *
     * @param sql    SQL-{@code WHERE}-Fragment (mit fuehrendem Leerzeichen) oder {@code ""}
     * @param params Parameterwerte in Reihenfolge der {@code ?}-Platzhalter (unveraenderlich)
     */
    record WhereClause(String sql, List<Object> params) {
        WhereClause {
            params = List.copyOf(params);
        }

        boolean isEmpty() {
            return sql.isEmpty();
        }
    }

    private static final WhereClause NONE = new WhereClause("", List.of());

    /**
     * Baut die {@link WhereClause} fuer einen Suchbegriff. {@code null}, leer oder nur Leerraum
     * liefert eine leere Klausel (kein Filter).
     */
    static WhereClause where(String search) {
        if (search == null || search.isBlank()) {
            return NONE;
        }
        String pattern = "%" + escapeLike(search.trim().toLowerCase()) + "%";
        return new WhereClause(LIKE_CLAUSE, List.of(pattern, pattern));
    }

    /** Escaped LIKE-Wildcards, sodass {@code %}, {@code _} und das Escape-Zeichen literal suchen. */
    private static String escapeLike(String value) {
        StringBuilder sb = new StringBuilder(value.length() + 4);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == ESCAPE || c == '%' || c == '_') {
                sb.append(ESCAPE);
            }
            sb.append(c);
        }
        return sb.toString();
    }
}
