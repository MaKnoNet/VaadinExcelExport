package de.makno.vaadinexcelexport.app;

import de.makno.xlsxbuilder.ResultSetRowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Bildet die aktuelle Zeile eines {@link ResultSet} auf eine {@link SampleRow} ab.
 *
 * <p>Wird sowohl vom lazy, seitenweisen {@link SampleDataProvider} (Grid-Anzeige) als auch vom
 * out-of-core-Stream-Export ({@code DataProviders.ofResultSet(...)}) verwendet – die Spalten der
 * Tabelle {@code testdata} ({@link TestDataDatabase}) werden dabei 1:1 auf die Record-Felder
 * gemappt. {@code java.time}-Typen werden über {@code getObject(spalte, Typ.class)} gelesen
 * (von H2 unterstützt).
 *
 * <p><b>Thread-Sicherheit:</b> zustandslos und damit threadsicher; eine Instanz ist beliebig
 * wiederverwendbar.
 */
final class SampleRowMapper implements ResultSetRowMapper<SampleRow> {

    @Override
    public SampleRow map(ResultSet rs) throws SQLException {
        return new SampleRow(
                rs.getString("text"),
                rs.getInt("ganzzahl"),
                rs.getLong("grosse_zahl"),
                rs.getDouble("gleitkomma"),
                rs.getBigDecimal("betrag"),
                rs.getBoolean("aktiv"),
                rs.getObject("datum", LocalDate.class),
                rs.getObject("zeitstempel", LocalDateTime.class),
                rs.getInt("kommt_sekunden"),
                rs.getString("webseite"),
                rs.getString("webseite_name"));
    }
}
