package de.makno.vaadinexcelexport.app;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Beispiel-Datensatz, dessen Felder die von Excel unterstützten Datentypen abdecken.
 *
 * <p>Der Excel-Formeltyp benötigt kein eigenes Feld – die Formelspalte wird in
 * {@link SampleGrid} aus dem {@code betrag} abgeleitet.
 *
 * @param text          Text (STRING)
 * @param ganzzahl      Ganzzahl (INTEGER)
 * @param grosseZahl    große Ganzzahl (LONG)
 * @param gleitkomma    Gleitkommazahl (DOUBLE)
 * @param betrag        Geldbetrag (DECIMAL)
 * @param aktiv         Wahrheitswert (BOOLEAN)
 * @param datum         Datum (DATE)
 * @param zeitstempel   Datum mit Uhrzeit (DATETIME)
 * @param kommtSekunden Tageszeit als Sekunden seit Mitternacht (Rohwert für TIME)
 * @param webseite      Ziel-URL des Hyperlinks (href bzw. HYPERLINK-Ziel)
 * @param webseiteName  Anzeigename des Hyperlinks (Linktext im Grid und in Excel)
 */
public record SampleRow(
        String text,
        int ganzzahl,
        long grosseZahl,
        double gleitkomma,
        BigDecimal betrag,
        boolean aktiv,
        LocalDate datum,
        LocalDateTime zeitstempel,
        int kommtSekunden,
        String webseite,
        String webseiteName) {}
