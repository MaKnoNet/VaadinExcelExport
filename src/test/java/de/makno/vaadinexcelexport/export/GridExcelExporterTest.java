package de.makno.vaadinexcelexport.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.vaadin.flow.data.provider.ListDataProvider;
import de.makno.xlsbuilder.builder.ColumnType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

/**
 * Verifiziert den {@link GridExcelExporter} end-to-end: Spalten + Vaadin-DataProvider werden
 * exportiert und die erzeugte {@code .xlsx} mit Apache POI zurückgelesen. Geprüft wird, dass jeder
 * Excel-Datentyp als korrekter Zelltyp/-wert landet.
 */
class GridExcelExporterTest {

    /** Spaltenindizes (0-basiert) – entsprechen den Excel-Spalten A..H. */
    private static final int COL_NAME = 0;

    private static final int COL_AGE = 1;
    private static final int COL_SCORE = 2;
    private static final int COL_SALARY = 3; // Excel-Spalte D
    private static final int COL_ACTIVE = 4;
    private static final int COL_BIRTHDAY = 5;
    private static final int COL_TIME = 6;
    private static final int COL_FORMULA = 7;

    private record Person(
            String name,
            int age,
            double score,
            BigDecimal salary,
            boolean active,
            LocalDate birthday,
            int checkInSeconds) {}

    private static List<ExcelColumn<Person>> columns() {
        return List.of(
                ExcelColumn.of("Name", ColumnType.STRING, Person::name),
                ExcelColumn.of("Alter", ColumnType.INTEGER, Person::age),
                ExcelColumn.of("Score", ColumnType.DOUBLE, Person::score).withFormat("0.0"),
                ExcelColumn.of("Gehalt", ColumnType.DECIMAL, Person::salary).withFormat("#,##0.00"),
                ExcelColumn.of("Aktiv", ColumnType.BOOLEAN, Person::active),
                ExcelColumn.of("Geburtstag", ColumnType.DATE, Person::birthday).withFormat("dd.mm.yyyy"),
                ExcelColumn.<Person>of("Kommt", ColumnType.TIME, Person::checkInSeconds)
                        .withConverter(s -> LocalTime.ofSecondOfDay(((Number) s).longValue())),
                // D = Spalte "Gehalt": halbe Steuer auf das Gehalt.
                ExcelColumn.<Person>of("Steuer", ColumnType.FORMULA, p -> "D{row}*0.5"));
    }

    private static List<Person> people() {
        return List.of(
                new Person(
                        "Alice",
                        30,
                        4.5,
                        new BigDecimal("2500.00"),
                        true,
                        LocalDate.of(1994, 3, 15),
                        8 * 3600 + 15 * 60),
                new Person("Bob", 45, 1.0, new BigDecimal("3100.50"), false, LocalDate.of(1981, 11, 2), 9 * 3600));
    }

    @Test
    void exportsEveryColumnTypeWithCorrectCellTypeAndValue() throws Exception {
        byte[] bytes = export();

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertEquals("Beispieldaten", sheet.getSheetName());

            assertHeaderRow(sheet.getRow(0));
            assertFirstDataRow(sheet.getRow(1));
            assertSecondRowFormulaTracksRowNumber(sheet.getRow(2));
        }
    }

    private void assertHeaderRow(Row header) {
        assertEquals("Name", header.getCell(COL_NAME).getStringCellValue());
        assertEquals("Gehalt", header.getCell(COL_SALARY).getStringCellValue());
        assertEquals("Steuer", header.getCell(COL_FORMULA).getStringCellValue());
    }

    private void assertFirstDataRow(Row row) {
        assertEquals("Alice", row.getCell(COL_NAME).getStringCellValue());

        assertEquals(CellType.NUMERIC, row.getCell(COL_AGE).getCellType());
        assertEquals(30L, (long) row.getCell(COL_AGE).getNumericCellValue());

        assertEquals(4.5, row.getCell(COL_SCORE).getNumericCellValue(), 1e-9);
        assertEquals(2500.00, row.getCell(COL_SALARY).getNumericCellValue(), 1e-9);

        assertEquals(CellType.BOOLEAN, row.getCell(COL_ACTIVE).getCellType());
        assertTrue(row.getCell(COL_ACTIVE).getBooleanCellValue());

        assertEquals(
                LocalDate.of(1994, 3, 15),
                row.getCell(COL_BIRTHDAY).getLocalDateTimeCellValue().toLocalDate());

        // TIME wird als Tagesbruchteil gespeichert: 08:15:00 = 29700 s / 86400 s.
        double expectedDayFraction = (8 * 3600 + 15 * 60) / 86_400d;
        assertEquals(expectedDayFraction, row.getCell(COL_TIME).getNumericCellValue(), 1e-9);

        Cell formula = row.getCell(COL_FORMULA);
        assertEquals(CellType.FORMULA, formula.getCellType());
        assertEquals("D2*0.5", formula.getCellFormula()); // erste Datenzeile = Excel-Zeile 2
    }

    private void assertSecondRowFormulaTracksRowNumber(Row row) {
        // {row}-Platzhalter muss je Zeile auf die echte Zeilennummer zeigen.
        assertEquals("D3*0.5", row.getCell(COL_FORMULA).getCellFormula());
    }

    @Test
    void rejectsEmptyColumnList() {
        assertThrows(IllegalArgumentException.class, () -> new GridExcelExporter<Person>("Sheet", List.of()));
    }

    @Test
    void gridValueProviderFallsBackToExtractor() {
        ExcelColumn<Person> column = ExcelColumn.of("Name", ColumnType.STRING, Person::name);
        assertEquals(column.valueExtractor(), column.gridValueProvider());

        ExcelColumn<Person> withDisplay = column.withGridValue(p -> "display");
        assertFalse(withDisplay.valueExtractor().equals(withDisplay.gridValueProvider()));
    }

    private byte[] export() throws Exception {
        GridExcelExporter<Person> exporter = new GridExcelExporter<>("Beispieldaten", columns());
        ListDataProvider<Person> dataProvider = new ListDataProvider<>(people());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        exporter.export(dataProvider, out);
        return out.toByteArray();
    }
}
