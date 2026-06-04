package de.makno.vaadinexcelexport.export;

import com.vaadin.flow.component.grid.ColumnPathRenderer;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.function.ValueProvider;
import java.lang.reflect.Field;

/**
 * Extrahiert den Exportwert einer {@link Column} für eine gegebene Zeile.
 *
 * <p>Die folgenden Strategien werden der Reihe nach probiert:
 * <ol>
 *   <li><b>ExcelMeta-ValueProvider (Hauptpfad):</b> Explizit über
 *       {@link ExcelMeta#type(Column, de.makno.xlsbuilder.builder.ColumnType, ValueProvider)}
 *       gesetzt – liefert den typkorrekten Java-Wert ohne Umwege. Für alle typisierten
 *       Spalten (DATE, DECIMAL, FORMULA …) empfohlen.</li>
 *   <li><b>LitRenderer-ValueProvider:</b> Für Spalten, die mit
 *       {@code Grid.addColumn(LitRenderer.of(...))} aufgebaut wurden.</li>
 *   <li><b>ColumnPathRenderer-Provider (Fallback):</b> Vaadin 24 erzeugt für
 *       {@code Grid.addColumn(ValueProvider)} intern einen {@link ColumnPathRenderer}, dessen
 *       eingebetteter Provider den Wert als {@link String} zurückgibt. Geeignet als Fallback
 *       für {@link de.makno.xlsbuilder.builder.ColumnType#STRING}.</li>
 * </ol>
 *
 * <p><b>Thread-Sicherheit:</b> Nicht thread-safe; eine Instanz pro Export-Aufruf.
 *
 * @param <T> Datentyp einer Tabellenzeile
 */
final class ColumnValueExtractor<T> {

    /**
     * Extrahiert den Wert der Spalte für das übergebene Element.
     *
     * @throws IllegalStateException wenn kein Wert ermittelt werden konnte
     */
    Object extract(T item, Column<T> column) {
        // 1. Expliziter ValueProvider aus ExcelMeta (Hauptpfad für typisierte Spalten)
        ValueProvider<T, ?> explicit = ExcelMeta.getValueProvider(column);
        if (explicit != null) {
            return explicit.apply(item);
        }

        // 2. LitRenderer – bei genau einem ValueProvider oder Provider namens "name"
        if (column.getRenderer() instanceof LitRenderer) {
            Object value = extractFromLitRenderer(item, (LitRenderer<T>) column.getRenderer());
            if (value != null) {
                return value;
            }
        }

        // 3. ColumnPathRenderer – Fallback; liefert den string-formatierten Wert (für STRING ok)
        if (column.getRenderer() instanceof ColumnPathRenderer) {
            return extractFromColumnPathRenderer(item, (ColumnPathRenderer<T>) column.getRenderer());
        }

        String key = column.getKey() != null ? column.getKey() : "(kein Key)";
        throw new IllegalStateException("Wert für Spalte '" + key + "' konnte nicht ermittelt werden. "
                + "ExcelMeta.type(col, type, valueProvider) explizit setzen.");
    }

    @SuppressWarnings("unchecked")
    private Object extractFromLitRenderer(T item, LitRenderer<T> renderer) {
        var providers = renderer.getValueProviders();
        if (providers.size() == 1) {
            return providers.values().iterator().next().apply(item);
        }
        // Hierarchical column liefert zwei Provider ("children" + "name")
        if (providers.containsKey("name")) {
            return providers.get("name").apply(item);
        }
        return null;
    }

    /**
     * Greift via Reflexion auf das private {@code provider}-Feld von {@link ColumnPathRenderer}
     * zu. Der extrahierte Provider liefert einen String-formatierten Wert (geeignet für
     * {@link de.makno.xlsbuilder.builder.ColumnType#STRING}).
     */
    @SuppressWarnings("unchecked")
    private Object extractFromColumnPathRenderer(T item, ColumnPathRenderer<T> renderer) {
        try {
            Field field = ColumnPathRenderer.class.getDeclaredField("provider");
            field.setAccessible(true);
            ValueProvider<T, ?> provider = (ValueProvider<T, ?>) field.get(renderer);
            return provider.apply(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("Wert konnte nicht via ColumnPathRenderer-Reflexion ermittelt werden", e);
        }
    }
}
