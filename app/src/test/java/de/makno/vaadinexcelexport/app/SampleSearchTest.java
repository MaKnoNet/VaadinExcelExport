package de.makno.vaadinexcelexport.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.makno.vaadinexcelexport.app.SampleSearch.WhereClause;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Prüft die Übersetzung des Suchbegriffs in eine parametrisierte SQL-{@code WHERE}-Klausel. */
class SampleSearchTest {

    @Test
    void blankSearchYieldsEmptyClause() {
        for (String blank : new String[] {null, "", "   "}) {
            WhereClause where = SampleSearch.where(blank);
            assertTrue(where.isEmpty(), "Begriff: " + blank);
            assertEquals("", where.sql());
            assertEquals(List.of(), where.params());
        }
    }

    @Test
    void termBuildsTwoLowercasedLikeParameters() {
        WhereClause where = SampleSearch.where("Alice");

        assertTrue(where.sql().contains("LIKE ?"), where.sql());
        assertTrue(where.sql().contains("LOWER(text)"), where.sql());
        assertTrue(where.sql().contains("LOWER(webseite_name)"), where.sql());
        // Ein Parameter je Spalte, lower-case, mit umschließenden Wildcards.
        assertEquals(List.of("%alice%", "%alice%"), where.params());
    }

    @Test
    void escapesLikeWildcardsInTerm() {
        // % und _ im Begriff sollen literal suchen (mit ESCAPE '!').
        WhereClause where = SampleSearch.where("50%_x");
        assertEquals(List.of("%50!%!_x%", "%50!%!_x%"), where.params());
        assertTrue(where.sql().contains("ESCAPE '!'"), where.sql());
    }
}
