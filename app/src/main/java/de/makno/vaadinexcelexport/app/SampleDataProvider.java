package de.makno.vaadinexcelexport.app;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;

/**
 * Verbindet das {@link Grid} mit der {@link TestDataDatabase} als <b>lazy, seitenweise und
 * filterbare</b> Datenquelle: das Grid fordert nur die sichtbaren Seiten an (Offset/Limit aus der
 * {@code Query}), die {@code ORDER BY}-Klausel ergibt sich aus der aktuellen Grid-Sortierung
 * ({@link SampleGrid#orderByForQuery}), und ein optionaler Suchbegriff wird als parametrisiertes
 * {@code WHERE} angewandt.
 *
 * <p>Damit liegen die Daten nicht im JVM-Heap, sondern werden je Seite frisch aus der DB gelesen –
 * unabhängig von der Gesamt-Zeilenzahl bleibt der Speicherbedarf der Anzeige konstant.
 */
final class SampleDataProvider {

    private SampleDataProvider() {}

    /**
     * Bindet einen lazy, filterbaren {@code DataProvider} an das Grid, der aus {@code db} liest, und
     * liefert den {@link ConfigurableFilterDataProvider} zurück. Über dessen
     * {@link ConfigurableFilterDataProvider#setFilter(Object) setFilter(String)} wird der aktive
     * Suchbegriff gesetzt – er wirkt sofort auf die Anzeige <em>und</em> (da derselbe Provider) auf den
     * Flowingcode-Export. Mehrfacher Aufruf ersetzt die Datenquelle (z. B. nach {@code seed}).
     */
    static ConfigurableFilterDataProvider<SampleRow, Void, String> bind(Grid<SampleRow> grid, TestDataDatabase db) {
        DataProvider<SampleRow, String> backend = DataProvider.fromFilteringCallbacks(
                query -> db
                        .fetchPage(
                                query.getFilter().orElse(null),
                                query.getOffset(),
                                query.getLimit(),
                                SampleGrid.orderByForQuery(query))
                        .stream(),
                query -> (int) db.count(query.getFilter().orElse(null)));
        ConfigurableFilterDataProvider<SampleRow, Void, String> filterable = backend.withConfigurableFilter();
        grid.setItems(filterable);
        return filterable;
    }
}
