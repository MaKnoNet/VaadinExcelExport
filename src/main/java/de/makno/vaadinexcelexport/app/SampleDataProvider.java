package de.makno.vaadinexcelexport.app;

import com.vaadin.flow.component.grid.Grid;

/**
 * Verbindet das {@link Grid} mit der {@link TestDataDatabase} als <b>lazy, seitenweise</b>
 * Datenquelle: das Grid fordert nur die sichtbaren Seiten an (Offset/Limit aus der {@code Query}),
 * die {@code ORDER BY}-Klausel ergibt sich aus der aktuellen Grid-Sortierung
 * ({@link SampleGrid#orderByForQuery}).
 *
 * <p>Damit liegen die Daten nicht im JVM-Heap, sondern werden je Seite frisch aus der DB gelesen –
 * unabhängig von der Gesamt-Zeilenzahl bleibt der Speicherbedarf der Anzeige konstant.
 */
final class SampleDataProvider {

    private SampleDataProvider() {}

    /**
     * Setzt einen lazy {@code FetchCallback}/{@code CountCallback} auf das Grid, der aus
     * {@code db} liest. Mehrfacher Aufruf ersetzt die Datenquelle (z. B. nach {@code seed}).
     */
    static void bind(Grid<SampleRow> grid, TestDataDatabase db) {
        grid.setItems(
                query -> db.fetchPage(query.getOffset(), query.getLimit(), SampleGrid.orderByForQuery(query)).stream(),
                query -> (int) db.count());
    }
}
