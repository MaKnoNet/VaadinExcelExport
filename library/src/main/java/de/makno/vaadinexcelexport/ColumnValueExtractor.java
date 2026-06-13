package de.makno.vaadinexcelexport;

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
 *       {@link ExcelMeta#type(Column, de.makno.xlsxbuilder.ColumnType, ValueProvider)}
 *       gesetzt – liefert den typkorrekten Java-Wert ohne Umwege. Für alle typisierten
 *       Spalten (DATE, DECIMAL, FORMULA …) empfohlen.</li>
 *   <li><b>LitRenderer-ValueProvider:</b> Für Spalten, die mit
 *       {@code Grid.addColumn(LitRenderer.of(...))} aufgebaut wurden.</li>
 *   <li><b>ColumnPathRenderer-Provider (Fallback):</b> Vaadin 24 erzeugt für
 *       {@code Grid.addColumn(ValueProvider)} intern einen {@link ColumnPathRenderer}, dessen
 *       eingebetteter Provider den Wert als {@link String} zurückgibt. Geeignet als Fallback
 *       für {@link de.makno.xlsxbuilder.ColumnType#STRING}.</li>
 * </ol>
 *
 * <p><b>Thread-Sicherheit:</b> Zustandslos (nur statische Methoden, keine Felder) und damit
 * gefahrlos nebenläufig nutzbar.
 */
final class ColumnValueExtractor {

    /**
     * Vaadin-internes Feld {@code ColumnPathRenderer.provider}, einmalig per Reflexion aufgelöst und
     * zwischengespeichert. {@code null}, wenn das Feld in der vorliegenden Vaadin-Version fehlt oder
     * unter dem Java-Modulsystem nicht zugänglich gemacht werden kann; in dem Fall meldet der Fallback
     * den fehlenden Zugriff klar beim Gebrauch (statt bei jedem Aufruf neu zu reflektieren).
     */
    private static final Field COLUMN_PATH_PROVIDER_FIELD = resolveColumnPathProviderField();

    private ColumnValueExtractor() {}

    private static Field resolveColumnPathProviderField() {
        try {
            Field field = ColumnPathRenderer.class.getDeclaredField("provider");
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException | RuntimeException e) {
            // RuntimeException deckt InaccessibleObjectException (Modulsystem) und SecurityException ab.
            return null;
        }
    }

    /**
     * Extrahiert den Wert der Spalte für das übergebene Element.
     *
     * @throws IllegalStateException wenn kein Wert ermittelt werden konnte
     */
    static <T> Object extract(T item, Column<T> column) {
        // 1. Expliziter ValueProvider aus ExcelMeta (Hauptpfad für typisierte Spalten)
        ValueProvider<T, ?> explicit = ExcelMeta.getValueProvider(column);
        if (explicit != null) {
            return explicit.apply(item);
        }

        // 2. LitRenderer – bei genau einem ValueProvider oder Provider namens "name"
        if (column.getRenderer() instanceof LitRenderer<T> litRenderer) {
            Object value = extractFromLitRenderer(item, litRenderer);
            if (value != null) {
                return value;
            }
        }

        // 3. ColumnPathRenderer – Fallback; liefert den string-formatierten Wert (für STRING ok)
        if (column.getRenderer() instanceof ColumnPathRenderer<T> pathRenderer) {
            return extractFromColumnPathRenderer(item, pathRenderer);
        }

        String key = column.getKey() != null ? column.getKey() : "(kein Key)";
        throw new IllegalStateException("Wert für Spalte '" + key + "' konnte nicht ermittelt werden. "
                + "ExcelMeta.type(col, type, valueProvider) explizit setzen.");
    }

    private static <T> Object extractFromLitRenderer(T item, LitRenderer<T> renderer) {
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
     * {@link de.makno.xlsxbuilder.ColumnType#STRING}).
     */
    @SuppressWarnings("unchecked")
    private static <T> Object extractFromColumnPathRenderer(T item, ColumnPathRenderer<T> renderer) {
        if (COLUMN_PATH_PROVIDER_FIELD == null) {
            throw new IllegalStateException("ColumnPathRenderer-Fallback nicht verfügbar: Vaadin-internes Feld "
                    + "'provider' ist nicht zugänglich (Vaadin-Version geändert oder Modulsystem). "
                    + "ExcelMeta.type(col, type, valueProvider) explizit setzen oder --add-opens konfigurieren.");
        }
        try {
            ValueProvider<T, ?> provider = (ValueProvider<T, ?>) COLUMN_PATH_PROVIDER_FIELD.get(renderer);
            return provider.apply(item);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Wert konnte nicht via ColumnPathRenderer-Reflexion ermittelt werden", e);
        }
    }
}
