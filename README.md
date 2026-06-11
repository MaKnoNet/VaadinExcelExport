# VaadinExcelExport

A small, reusable **bridge** that exports a **Vaadin `Grid`** to an **`.xlsx`** file through the
[**xlsxBuilder**](https://github.com/MaKnoNet/xlsxBuilder) library – column types, formats, real formulas, clickable
hyperlinks, grouped headers and footers, all **out-of-core** (constant memory, even straight from a
JDBC `ResultSet`). The repository also ships a **demo app** that compares this exporter against the
[Flowingcode Grid Exporter](https://vaadin.com/directory/component/grid-exporter-add-on).

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Vaadin](https://img.shields.io/badge/Vaadin-24-blue.svg)](https://vaadin.com/)

## Two Gradle modules in one repository

The build is split into two subprojects, so the app can never leak into the published library:

| Module | Package | What it is | Shipped to Maven? |
|---|---|---|---|
| `:library` | `de.makno.vaadinexcelexport` | the reusable **library** (`GridExcelExporter`, `ExcelMeta`, …), published as `de.makno.vaadinexcelexport:VaadinExcelExport` | **yes** |
| `:app` | `de.makno.vaadinexcelexport.app` | the **demo app** (Spring Boot + Vaadin) comparing both exporters; `implementation project(':library')` | no – this module has no `maven-publish` |

## Highlights

- **Declarative, per-column config** – annotate `Grid` columns directly with `ExcelMeta` (type,
  value provider, Excel format, converter, header group). No separate column model to keep in sync;
  columns without `ExcelMeta.type(...)` are simply skipped.
- **Out-of-core** – export straight from a JDBC `ResultSet` (`DataProviders.ofResultSet`) via
  xlsxBuilder's SXSSF streaming: constant memory, independent of the row count.
- **Follows the table** – respects the grid's **sort order** (column headers) and its **active
  filter** automatically; the exported rows match what the user sees.
- **Rich cells** – typed cells (`INTEGER, LONG, DOUBLE, DECIMAL, BOOLEAN, DATE, DATETIME, TIME`),
  real Excel formulas (`=E2*0.19`) and clickable `HYPERLINK(...)` cells.
- **Layout** – title/footer rows with resolved placeholders (`{datetime}`, `{rowCount}`,
  `{sum:Column}`), a summary row (`sumColumn`), and **joined (grouped) headers** merged across
  column ranges.
- **Override the export column order** independently of the on-screen order.
- **Optional pipeline parallelism** (passed through to xlsxBuilder).

In the bundled benchmark (25,000 rows, streamed from an H2 database) the exporter is **~28× faster**
than the Flowingcode exporter and uses **constant memory** instead of loading every row into the
heap. See the full comparison in
[`excel-export-vergleich.pdf`](app/src/main/resources/META-INF/resources/excel-export-vergleich.pdf).

## Requirements

- **Java 21** (Gradle toolchain)
- **Vaadin 24.5.3**
- **xlsxBuilder** (`de.makno.xlsxbuilder:xlsxbuilder`) – consumed as a binary dependency from
  **mavenLocal** (see *Build & run*). Brings Apache POI transitively.
- Demo-only: Flowingcode `grid-exporter-addon:2.5.0`, `com.h2database:h2:2.3.232`.

## Quick start (using the library)

Annotate the grid columns once, then export:

```java
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import de.makno.vaadinexcelexport.ExcelMeta;
import de.makno.vaadinexcelexport.GridExcelExporter;
import de.makno.xlsxbuilder.ColumnType;

Grid<Employee> grid = new Grid<>();

Column<Employee> name = grid.addColumn(Employee::name).setKey("Name").setHeader("Name");
ExcelMeta.type(name, ColumnType.STRING);

Column<Employee> salary = grid.addColumn(Employee::salary).setKey("Salary").setHeader("Salary");
ExcelMeta.type(salary, ColumnType.DECIMAL, Employee::salary).format("#,##0.00 \"€\"");

// In-memory data provider → write the visible (sorted) rows:
GridExcelExporter.from("Employees", grid)
        .export(grid.getDataProvider(), grid.getDataCommunicator().getInMemorySorting(), outputStream);
```

### Out-of-core: stream straight from a JDBC `ResultSet`

The same column definitions, but the rows are streamed from the database (constant memory). The
exporter consumes an xlsxBuilder `DataProvider`; it closes the `ResultSet`, while you keep the
`Statement`/`Connection` in a `try-with-resources`:

```java
import de.makno.vaadinexcelexport.ExportOptions;
import de.makno.xlsxbuilder.DataProviders;

try (Connection conn = dataSource.getConnection();
     Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
    st.setFetchSize(1_000);
    ResultSet rs = st.executeQuery("SELECT name, salary FROM employee ORDER BY salary DESC");

    GridExcelExporter.from("Employees", grid)
            .export(
                    DataProviders.ofResultSet(rs, r -> new Employee(r.getString("name"), r.getBigDecimal("salary"))),
                    outputStream,
                    ExportOptions.none()
                            .withSumColumns("Salary")
                            .withFooter("Generated {datetime} – {rowCount} rows – total {sum:Salary} €"));
}
```

## Concepts

### `ExcelMeta` – per-column export metadata

`ExcelMeta` attaches export metadata to a Vaadin `Grid.Column` (stored on the column via
`ComponentUtil`), so the grid stays the single source of truth:

```java
ExcelMeta.type(column, ColumnType.DATE, Employee::hired).format("dd.mm.yyyy");   // type + value provider + format
ExcelMeta.type(column, ColumnType.TIME, Employee::seconds)
        .converter(s -> java.time.LocalTime.ofSecondOfDay((Integer) s));         // raw value → cell value
ExcelMeta.group(column, "Personal data");                                       // joined-header group label
```

- The 2-arg `type(column, ColumnType)` is enough for `STRING` (value read from the renderer);
  for every typed column pass the value provider explicitly via `type(column, type, valueProvider)`.
- Columns **without** `ExcelMeta.type(...)` (or without a key) are skipped on export.
- `FORMULA` columns return the formula text (e.g. `"E{row}*0.19"`); `{row}` becomes the real row.

### `GridExcelExporter<T>`

Reads the exportable columns from the grid and writes the sheet.

| Method | Purpose |
|---|---|
| `from(sheet, grid)` | export all `ExcelMeta`-annotated columns, in grid order |
| `from(sheet, grid, columnKeyOrder)` | override the export column order / subset (list of column keys) |
| `export(DataProvider<T,?>, out)` | export unsorted, from a Vaadin data provider |
| `export(DataProvider<T,?>, Comparator<T>, out)` | export following an in-memory sort (e.g. the grid's) |
| `export(xlsxbuilder DataProvider<T>, out[, ExportOptions])` | **out-of-core** export from an xlsxBuilder source |

`ExportOptions` (immutable) bundles optional extras: `withFooter(...)`, `withSumColumns(...)`
(enables the summary row and `{sum:Column}` placeholders) and `withParallel(boolean)`. Joined
headers are derived automatically from the columns' `ExcelMeta.group(...)` labels.

> **CSV is intentionally not supported.** `FORMULA`/`HYPERLINK` cells cannot be represented in CSV
> without evaluating them in a full in-memory workbook, which conflicts with the out-of-core design.

The actual `.xlsx` writing is done by **xlsxBuilder** (`XlsxBuilder` + `WorkbookBuilder`, Apache POI
SXSSF). See its [README](https://github.com/MaKnoNet/xlsxBuilder#readme) for sorting, summary rows,
placeholders, temp-dir handling and the underlying out-of-core engine.

## Demo app

`./gradlew :app:bootRun` starts a Vaadin app at <http://localhost:8080> that compares both exporters on
one table:

- The grid is backed by a **lazy, SQL-backed** data source (embedded **H2** via plain JDBC; rows are
  generated once into the database) – the display never holds all rows in the heap.
- A **search filter** (over the text / website columns), interactive **multi-column sorting**, and
  configurable **row count** + **page size**.
- **Run** the MaKnos (`GridExcelExporter`) and/or the Vaadin (Flowingcode) export and see **time,
  allocated memory and file size** side by side; both downloads are produced.
- A **“Comparison (PDF)”** button embeds the feature/benchmark comparison.

## Build & run

xlsxBuilder is consumed as a binary dependency from your local Maven repository, so publish it once
first:

```bash
./gradlew -p ../xlsbuilder publishToMavenLocal   # provide de.makno.xlsxbuilder:xlsxbuilder in ~/.m2
```

Then:

```bash
./gradlew :app:bootRun                 # demo app at http://localhost:8080
./gradlew :library:test                # library unit tests (JUnit 5) + Jacoco report
./gradlew :app:benchmark [-Prows=N]    # H2-backed benchmark: both exporters from the database
./gradlew :library:javadoc             # API docs (library/build/docs/javadoc/index.html)
```

> The benchmark (`@Tag("benchmark")`, class `ExcelExporterBenchmarkTest`) is excluded from the normal
> test run; `-Prows=N` sets the row count (default 25,000).

## Use as a library (Maven)

The published artifact `de.makno.vaadinexcelexport:VaadinExcelExport` is the `:library` module
(package `de.makno.vaadinexcelexport`) and declares only its real dependencies (xlsxBuilder + Vaadin
Grid – the demo's Spring Boot, Flowingcode and H2 dependencies live in `:app` and are **not**
propagated):

```gradle
repositories { mavenLocal(); mavenCentral() }

dependencies {
    implementation 'de.makno.vaadinexcelexport:VaadinExcelExport:1.0.0-SNAPSHOT'
    // transitively brings: de.makno.xlsxbuilder:xlsxbuilder and com.vaadin:vaadin-grid-flow
}
```

Publish it with `./gradlew :library:publishToMavenLocal`. Because `:app` is a separate Gradle module
without `maven-publish`, no app classes/resources can end up in the artifact by construction.

## Concurrency / server operation

`GridExcelExporter` holds only **immutable** fields and writes to a caller-provided `OutputStream`;
`export(...)` keeps no shared, mutable state and is safe to call from many threads (each with its own
stream and its own forward-only data source – see xlsxBuilder's
[concurrency notes](https://github.com/MaKnoNet/xlsxBuilder#concurrency--server-operation)).

It deliberately has **no built-in download throttling**. Flowingcode's exporter ships a global
`Semaphore` because it owns the whole download/serving lifecycle; this bridge does not, and a global
mutable `static` would violate the project's “no shared mutable static state” rule. Out-of-core
streaming also keeps each export cheap in memory, lowering the need. On a multi-user server, bound
export concurrency **in the application layer** – wrap `export(...)` in your own `Semaphore` or a
bounded `Executor` (permits + acquire timeout), exactly where you already manage threads and the
JDBC connection pool:

```java
private final Semaphore exportPermits = new Semaphore(4); // at most 4 concurrent exports

void exportThrottled(...) throws InterruptedException, TimeoutException {
    if (!exportPermits.tryAcquire(30, TimeUnit.SECONDS)) {
        throw new TimeoutException("Server busy, try again later");
    }
    try {
        GridExcelExporter.from("Data", grid).export(data, out, options);
    } finally {
        exportPermits.release();
    }
}
```

## Eclipse

Import as an **Existing Gradle Project** (Buildship). Java 21 compliance and UTF-8 encoding are
configured via the build; the project consumes xlsxBuilder from mavenLocal (publish it first, see
above).
