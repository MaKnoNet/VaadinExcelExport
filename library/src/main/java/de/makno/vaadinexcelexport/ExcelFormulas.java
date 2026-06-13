package de.makno.vaadinexcelexport;

import java.util.Locale;
import java.util.Objects;

/**
 * Hilfsmethoden zum sicheren Erzeugen von Excel-Formeltexten für
 * {@link de.makno.xlsxbuilder.ColumnType#FORMULA}-Spalten.
 *
 * <p>Formelzellen werden von Excel ausgewertet. Werte, die aus Fremd-/Benutzerdaten in eine Formel
 * eingebettet werden, müssen daher escaped werden, damit sie nicht aus dem Formel-String ausbrechen
 * (Formel-Injection). Diese Klasse kapselt das korrekte Escaping, sodass Aufrufer nicht das fragile
 * String-Konkatenat-Muster ({@code "HYPERLINK(\"" + url + "\",...)"}) wiederholen, das bei nicht
 * escapten Anführungszeichen ungültige bzw. injizierte Formeln erzeugt.
 *
 * <p><b>Thread-Sicherheit:</b> Zustandslos und gefahrlos nebenläufig nutzbar.
 */
public final class ExcelFormulas {

    private static final String SCHEME_HTTPS = "https://";
    private static final String SCHEME_HTTP = "http://";
    private static final String SCHEME_MAILTO = "mailto:";

    private ExcelFormulas() {}

    /**
     * Baut eine Excel-{@code HYPERLINK("ziel","anzeigename")}-Formel (ohne führendes {@code =}) mit
     * korrekt escapten String-Literalen.
     *
     * <p>Eingebettete Anführungszeichen werden verdoppelt ({@code " → ""}), sodass weder {@code url}
     * noch {@code displayName} aus dem Formel-String ausbrechen können. Die Ziel-URL wird auf die
     * Schemata {@code http}, {@code https} und {@code mailto} beschränkt; andere – etwa
     * {@code file:}/{@code smb:}, die beim Öffnen der Datei automatische Netzwerkzugriffe (z. B.
     * SMB-Hash-Capture) auslösen können – werden verworfen (leeres Ziel).
     *
     * @param url         Ziel-URL; nur {@code http}/{@code https}/{@code mailto}, sonst leeres Ziel
     * @param displayName im Excel angezeigter Linktext (nicht {@code null})
     * @return Formeltext {@code HYPERLINK("…","…")} ohne führendes {@code =}
     */
    public static String hyperlink(String url, String displayName) {
        Objects.requireNonNull(displayName, "displayName");
        return "HYPERLINK(\"" + escape(sanitizeUrl(url)) + "\",\"" + escape(displayName) + "\")";
    }

    /** Verdoppelt Anführungszeichen, damit der Wert ein gültiges Excel-String-Literal bleibt. */
    private static String escape(String value) {
        return value.replace("\"", "\"\"");
    }

    /** Lässt nur Web-/Mail-Schemata zu; alles andere (inkl. {@code null}) wird zu einem leeren Ziel. */
    private static String sanitizeUrl(String url) {
        if (url == null) {
            return "";
        }
        String lower = url.toLowerCase(Locale.ROOT);
        boolean allowed =
                lower.startsWith(SCHEME_HTTPS) || lower.startsWith(SCHEME_HTTP) || lower.startsWith(SCHEME_MAILTO);
        return allowed ? url : "";
    }
}
