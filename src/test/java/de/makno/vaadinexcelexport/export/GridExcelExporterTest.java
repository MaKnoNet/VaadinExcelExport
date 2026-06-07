package de.makno.vaadinexcelexport.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import de.makno.xlsxbuilder.builder.ColumnType;
import de.makno.xlsxbuilder.builder.DataProviders;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

/**
 * Verifiziert den {@link GridExcelExporter} end-to-end mit der {@link ExcelMeta}-API: Spalten
 * werden direkt am Grid annotiert, der Exporter liest Typ und {@link
 * com.vaadin.flow.function.ValueProvider} aus den Spalten-Metadaten. Die erzeugte {@code .xlsx}
 * wird mit Apache POI zurückgelesen und pro Zelltyp geprüft.
 */
class GridExcelExporterTest {

    /** Spaltenindizes (0-basiert) in der Reihenfolge, in der die Spalten zum Grid hinzugefügt werden. */
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

    /**
     * Baut ein Grid mit allen Spaltentypen und {@link ExcelMeta}-Annotationen.
     * Typisierte Spalten übergeben ihren {@link com.vaadin.flow.function.ValueProvider} explizit
     * an {@link ExcelMeta#type(Column, ColumnType, com.vaadin.flow.function.ValueProvider)}.
     */
    private static Grid<Person> buildGrid() {
        Grid<Person> grid = new Grid<>();

        // STRING – 2-Param-Overload reicht (Fallback liefert String direkt)
        Column<Person> name = grid.addColumn(Person::name).setKey("Name");
        ExcelMeta.type(name, ColumnType.STRING);

        // Typisierte Spalten: VP explizit übergeben
        Column<Person> age = grid.addColumn(Person::age).setKey("Alter");
        ExcelMeta.type(age, ColumnType.INTEGER, Person::age);

        Column<Person> score = grid.addColumn(Person::score).setKey("Score");
        ExcelMeta.type(score, ColumnType.DOUBLE, Person::score).format("0.0");

        Column<Person> salary = grid.addColumn(Person::salary).setKey("Gehalt");
        ExcelMeta.type(salary, ColumnType.DECIMAL, Person::salary).format("#,##0.00");

        Column<Person> active = grid.addColumn(Person::active).setKey("Aktiv");
        ExcelMeta.type(active, ColumnType.BOOLEAN, Person::active);

        Column<Person> birthday = grid.addColumn(Person::birthday).setKey("Geburtstag");
        ExcelMeta.type(birthday, ColumnType.DATE, Person::birthday).format("dd.mm.yyyy");

        // TIME: Grid zeigt LocalTime; derselbe Provider dient als Export-Wert
        var timeProvider = (com.vaadin.flow.function.ValueProvider<Person, LocalTime>)
                p -> LocalTime.ofSecondOfDay(p.checkInSeconds());
        Column<Person> time = grid.addColumn(timeProvider).setKey("Kommt");
        ExcelMeta.type(time, ColumnType.TIME, timeProvider);

        // FORMULA: Grid zeigt berechneten Wert; Export-VP liefert Formeltext
        Column<Person> formula =
                grid.addColumn(p -> p.salary().multiply(new BigDecimal("0.5"))).setKey("Steuer");
        ExcelMeta.type(formula, ColumnType.FORMULA, p -> "D{row}*0.5");

        grid.setItems(people());
        return grid;
    }

    @Test
    void exportsEveryColumnTypeWithCorrectCellTypeAndValue() throws Exception {
        Grid<Person> grid = buildGrid();
        GridExcelExporter<Person> exporter = GridExcelExporter.from("Beispieldaten", grid);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        exporter.export(grid.getDataProvider(), out);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(out.toByteArray()))) {
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

        // TIME wird als Tagesbruchteil gespeichert: 08:15:00 = 29700 s / 86400 s
        double expectedDayFraction = (8 * 3600 + 15 * 60) / 86_400d;
        assertEquals(expectedDayFraction, row.getCell(COL_TIME).getNumericCellValue(), 1e-9);

        Cell formula = row.getCell(COL_FORMULA);
        assertEquals(CellType.FORMULA, formula.getCellType());
        assertEquals("D2*0.5", formula.getCellFormula()); // erste Datenzeile = Excel-Zeile 2
    }

    private void assertSecondRowFormulaTracksRowNumber(Row row) {
        // {row}-Platzhalter muss je Zeile auf die echte Zeilennummer zeigen
        assertEquals("D3*0.5", row.getCell(COL_FORMULA).getCellFormula());
    }

    @Test
    void rejectsGridWithoutAnnotatedColumns() {
        Grid<Person> grid = new Grid<>();
        grid.addColumn(Person::name).setKey("Name"); // kein ExcelMeta → nicht exportierbar
        grid.setItems(people());

        assertThrows(IllegalArgumentException.class, () -> GridExcelExporter.from("Sheet", grid));
    }

    @Test
    void rejectsGridWithoutKeys() {
        Grid<Person> grid = new Grid<>();
        Column<Person> col = grid.addColumn(Person::name); // kein Key → nicht exportierbar
        ExcelMeta.type(col, ColumnType.STRING);
        grid.setItems(people());

        assertThrows(IllegalArgumentException.class, () -> GridExcelExporter.from("Sheet", grid));
    }

    /**
     * Prüft, dass die Spaltenreihenfolge aus dem Grid übernommen wird. Das Grid wird mit
     * umgekehrter Spaltenreihenfolge aufgebaut; der Exporter muss dieselbe Reihenfolge ausgeben.
     */
    @Test
    void respectsColumnOrderFromGrid() throws Exception {
        Grid<Person> grid = new Grid<>();

        // Umgekehrte Reihenfolge: zuerst Score, dann Name
        Column<Person> score = grid.addColumn(Person::score).setKey("Score");
        ExcelMeta.type(score, ColumnType.DOUBLE, Person::score);

        Column<Person> name = grid.addColumn(Person::name).setKey("Name");
        ExcelMeta.type(name, ColumnType.STRING);

        grid.setItems(people());

        GridExcelExporter<Person> exporter = GridExcelExporter.from("Test", grid);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        exporter.export(grid.getDataProvider(), out);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(out.toByteArray()))) {
            Sheet sheet = workbook.getSheetAt(0);
            // Erste Spalte muss "Score" sein (so wie im Grid)
            assertEquals("Score", sheet.getRow(0).getCell(0).getStringCellValue());
            assertEquals("Name", sheet.getRow(0).getCell(1).getStringCellValue());
        }
    }

    /** Prüft, dass Spalten ohne Key oder ohne ExcelMeta beim Export übersprungen werden. */
    @Test
    void skipsColumnsWithoutKeyOrMeta() throws Exception {
        Grid<Person> grid = new Grid<>();

        Column<Person> name = grid.addColumn(Person::name).setKey("Name");
        ExcelMeta.type(name, ColumnType.STRING);

        Column<Person> age = grid.addColumn(Person::age).setKey("Alter");
        ExcelMeta.type(age, ColumnType.INTEGER, Person::age);

        // Diese Spalte hat keinen Key → wird übersprungen
        grid.addColumn(Person::score);

        // Diese Spalte hat keinen ExcelMeta-Type → wird übersprungen
        grid.addColumn(Person::active).setKey("Aktiv");

        grid.setItems(people());

        GridExcelExporter<Person> exporter = GridExcelExporter.from("Test", grid);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        exporter.export(grid.getDataProvider(), out);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(out.toByteArray()))) {
            Sheet sheet = workbook.getSheetAt(0);
            // Nur 2 Spalten im Export
            assertEquals(2, sheet.getRow(0).getLastCellNum());
        }
    }

    /**
     * Kontrolliert, dass die Excel-Zeilenreihenfolge der übergebenen In-Memory-Sortierung folgt
     * (= der sortierten Tabelle), nicht der Eingabereihenfolge. Eingabe: Alice (30), Bob (45);
     * sortiert nach Alter absteigend muss Bob vor Alice stehen.
     */
    @Test
    void exportRespectsProvidedSortOrder() throws Exception {
        Grid<Person> grid = new Grid<>();
        Column<Person> name = grid.addColumn(Person::name).setKey("Name");
        ExcelMeta.type(name, ColumnType.STRING);
        Column<Person> age = grid.addColumn(Person::age).setKey("Alter");
        ExcelMeta.type(age, ColumnType.INTEGER, Person::age);
        grid.setItems(people());

        GridExcelExporter<Person> exporter = GridExcelExporter.from("Test", grid);
        Comparator<Person> byAgeDesc = Comparator.comparingInt(Person::age).reversed();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        exporter.export(grid.getDataProvider(), byAgeDesc, out);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(out.toByteArray()))) {
            Sheet sheet = workbook.getSheetAt(0);
            // Datenzeilen folgen dem Comparator (Alter absteigend), nicht der Eingabe
            assertEquals("Bob", sheet.getRow(1).getCell(0).getStringCellValue());
            assertEquals("Alice", sheet.getRow(2).getCell(0).getStringCellValue());
        }
    }

    /**
     * Prüft die Überladung, die direkt aus einer xlsxbuilder-{@code DataProvider}-Quelle exportiert
     * (z. B. ein gestreamtes ResultSet): Spalten stammen aus dem Grid, die Daten und ihre
     * Reihenfolge aus der übergebenen Quelle.
     */
    @Test
    void exportsFromXlsbuilderDataProviderInSourceOrder() throws Exception {
        Grid<Person> grid = new Grid<>();
        Column<Person> name = grid.addColumn(Person::name).setKey("Name");
        ExcelMeta.type(name, ColumnType.STRING);
        Column<Person> age = grid.addColumn(Person::age).setKey("Alter");
        ExcelMeta.type(age, ColumnType.INTEGER, Person::age);
        // Kein setItems nötig – die Daten kommen aus der xlsxbuilder-Quelle.

        GridExcelExporter<Person> exporter = GridExcelExporter.from("Test", grid);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        exporter.export(DataProviders.ofIterable(people()), out);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(out.toByteArray()))) {
            Sheet sheet = workbook.getSheetAt(0);
            // Reihenfolge = Quellreihenfolge (Alice, dann Bob)
            assertEquals("Alice", sheet.getRow(1).getCell(0).getStringCellValue());
            assertEquals("Bob", sheet.getRow(2).getCell(0).getStringCellValue());
            assertEquals(30L, (long) sheet.getRow(1).getCell(1).getNumericCellValue());
        }
    }

    /** Prüft, dass eine FORMULA-Spalte mit {@code HYPERLINK(...)} als Formelzelle exportiert wird. */
    @Test
    void exportsHyperlinkAsFormula() throws Exception {
        Grid<Person> grid = new Grid<>();
        Column<Person> link = grid.addColumn(Person::name).setKey("Webseite");
        ExcelMeta.type(link, ColumnType.FORMULA, p -> "HYPERLINK(\"https://x/" + p.name() + "\",\"" + p.name() + "\")");
        grid.setItems(people());

        GridExcelExporter<Person> exporter = GridExcelExporter.from("Test", grid);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        exporter.export(grid.getDataProvider(), out);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(out.toByteArray()))) {
            Cell cell = workbook.getSheetAt(0).getRow(1).getCell(0);
            assertEquals(CellType.FORMULA, cell.getCellType());
            assertTrue(cell.getCellFormula().contains("HYPERLINK"), "Formel: " + cell.getCellFormula());
        }
    }

    /**
     * Prüft die {@link ExportOptions}: eine Fußzeile mit den Platzhaltern {@code {rowCount}} und
     * {@code {sum:Gehalt}} wird mit den tatsächlichen Werten aufgelöst (Summenzeile aktiviert über
     * {@code withSumColumns}).
     */
    @Test
    void appendsFooterWithResolvedPlaceholders() throws Exception {
        Grid<Person> grid = buildGrid();
        GridExcelExporter<Person> exporter = GridExcelExporter.from("Test", grid);
        ExportOptions options =
                ExportOptions.none().withSumColumns("Gehalt").withFooter("Zeilen: {rowCount} – Summe: {sum:Gehalt} €");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        exporter.export(DataProviders.ofIterable(people()), out, options);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(out.toByteArray()))) {
            String footer = findCellContaining(workbook.getSheetAt(0), "Zeilen:");
            assertTrue(footer.contains("Zeilen: 2"), footer);
            assertTrue(footer.contains("5600.50"), footer); // 2500.00 + 3100.50
            assertFalse(footer.contains("{sum"), "Platzhalter nicht aufgelöst: " + footer);
        }
    }

    /** Liefert den ersten String-Zellwert im Blatt, der {@code needle} enthält (für Footer-Suche). */
    private static String findCellContaining(Sheet sheet, String needle) {
        for (Row row : sheet) {
            for (Cell cell : row) {
                if (cell.getCellType() == CellType.STRING
                        && cell.getStringCellValue().contains(needle)) {
                    return cell.getStringCellValue();
                }
            }
        }
        return "";
    }
}
