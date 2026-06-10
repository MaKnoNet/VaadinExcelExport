package de.makno.vaadinexcelexport.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.ResultSet;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;

/**
 * Prüft, dass der Suchbegriff in der {@link TestDataDatabase} konsistent über alle Lesepfade wirkt
 * (Zählen, Seite, out-of-core-Stream) und die No-Filter-Overloads weiterhin alle Zeilen liefern.
 */
class TestDataDatabaseFilterTest {

    private static final String ORDER_BY = "ORDER BY id";
    private static final int ROWS = 50;
    private static final String TERM = "Alice"; // kommt im Namen vor (Spalte text)

    @Test
    void filterIsConsistentAcrossCountPageAndStream() throws Exception {
        try (TestDataDatabase db = new TestDataDatabase()) {
            db.seed(ROWS);

            long total = db.count(null);
            assertEquals(ROWS, total, "ungefilterte Gesamtzahl");

            long filtered = db.count(TERM);
            assertTrue(filtered > 0 && filtered < total, "gefilterte Anzahl plausibel: " + filtered);

            // Seite: gleiche Anzahl wie count, und jede Zeile passt wirklich zum Begriff.
            List<SampleRow> page = db.fetchPage(TERM, 0, ROWS, ORDER_BY);
            assertEquals(filtered, page.size(), "Seitenanzahl == gefilterte Anzahl");
            for (SampleRow row : page) {
                assertTrue(matches(row), "Zeile passt nicht zum Filter: " + row.text());
            }

            // Out-of-core-Stream: gleiche Anzahl.
            assertEquals(filtered, streamCount(db, TERM), "Stream-Anzahl == gefilterte Anzahl");
        }
    }

    @Test
    void noFilterOverloadsReturnAllRows() throws Exception {
        try (TestDataDatabase db = new TestDataDatabase()) {
            db.seed(ROWS);
            assertEquals(ROWS, db.count());
            assertEquals(ROWS, db.fetchPage(0, ROWS, ORDER_BY).size());
            assertEquals(ROWS, streamCount(db, null));
        }
    }

    private static boolean matches(SampleRow row) {
        String needle = TERM.toLowerCase(Locale.ROOT);
        return row.text().toLowerCase(Locale.ROOT).contains(needle)
                || row.webseiteName().toLowerCase(Locale.ROOT).contains(needle);
    }

    private static long streamCount(TestDataDatabase db, String search) throws Exception {
        try (TestDataDatabase.StreamingResult stream = db.openStream(search, ORDER_BY, 10)) {
            ResultSet rs = stream.resultSet();
            long n = 0;
            while (rs.next()) {
                n++;
            }
            return n;
        }
    }
}
