package de.makno.vaadinexcelexport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Verifiziert {@link ExcelFormulas#hyperlink(String, String)}: korrektes Escaping (Formel-Injection)
 * und die Schema-Whitelist für die Ziel-URL.
 */
class ExcelFormulasTest {

    @Test
    void buildsEscapedHyperlinkFormulaForPlainValues() {
        assertEquals(
                "HYPERLINK(\"https://example.com/p\",\"Beispiel\")",
                ExcelFormulas.hyperlink("https://example.com/p", "Beispiel"));
    }

    @Test
    void doublesEmbeddedQuotesSoValuesCannotBreakOutOfTheFormula() {
        // Anzeigename mit Anführungszeichen: muss als Excel-String-Literal escaped werden (" -> "").
        assertEquals("HYPERLINK(\"https://x\",\"a\"\"b\")", ExcelFormulas.hyperlink("https://x", "a\"b"));
    }

    @Test
    void acceptsHttpHttpsAndMailtoSchemes() {
        assertTrue(ExcelFormulas.hyperlink("http://x", "t").contains("\"http://x\""));
        assertTrue(ExcelFormulas.hyperlink("HTTPS://X", "t").contains("\"HTTPS://X\""));
        assertTrue(ExcelFormulas.hyperlink("mailto:a@b.de", "t").contains("\"mailto:a@b.de\""));
    }

    @Test
    void dropsDangerousSchemesToAnEmptyTarget() {
        // file:/smb:/javascript: u. a. können beim Öffnen Netzwerkzugriffe/Schadcode auslösen.
        assertEquals("HYPERLINK(\"\",\"t\")", ExcelFormulas.hyperlink("file:///etc/passwd", "t"));
        assertEquals("HYPERLINK(\"\",\"t\")", ExcelFormulas.hyperlink("smb://host/share", "t"));
        assertEquals("HYPERLINK(\"\",\"t\")", ExcelFormulas.hyperlink("javascript:alert(1)", "t"));
    }

    @Test
    void treatsNullUrlAsEmptyTarget() {
        assertEquals("HYPERLINK(\"\",\"t\")", ExcelFormulas.hyperlink(null, "t"));
    }

    @Test
    void rejectsNullDisplayName() {
        assertThrows(NullPointerException.class, () -> ExcelFormulas.hyperlink("https://x", null));
    }
}
