---
type: API Reference
title: GridExcelExporter
description: Verified constructor/method-level reference for GridExcelExporter, the fluent bridge from a Vaadin Grid to an xlsxBuilder-backed .xlsx export.
resource: library/src/main/java/de/makno/vaadinexcelexport/GridExcelExporter.java
tags: [api-reference, vaadin, excel, export, streaming]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Overview

`GridExcelExporter<T>` bridges a Vaadin `Grid<T>` to xlsxBuilder. See
[GridExcelExporter](/components/grid-excel-exporter.md) for the conceptual overview (in-memory
vs. out-of-core, sort/filter respect, joined headers). This file documents every public and
private member exhaustively, including the exact exceptions surfaced from the underlying
xlsxBuilder dependency (`WorkbookBuilder`/`XlsxBuilder`), verified by reading both this class
and the xlsxBuilder source it delegates to.

**Thread-safety:** the instance holds only immutable fields (`sheetName: String`,
`columns: List<Column<T>>`, defensively copied via `List.copyOf` in the private constructor)
and is safely shareable after construction. Each `export(...)` call creates its own
`WorkbookBuilder`/`XlsxBuilder` internally (verified: `newSheetWithColumns` builds a fresh
`XlsxBuilder` every call) — no shared mutable state across concurrent `export` calls on the
same `GridExcelExporter` instance.

# Fields

Verified against the source: exactly two instance fields, both `final` and set only in the
private constructor.

| Field | Type | Meaning | Nullable |
|---|---|---|---|
| `sheetName` | `private final String` | Name of the Excel sheet to export into. | no — `Objects.requireNonNull(sheetName, "sheetName")` in the private constructor |
| `columns` | `private final List<Column<T>>` | Exportable grid columns in export order, defensively copied via `List.copyOf` in the private constructor. | field itself never `null`; guaranteed non-empty by the constructor's `IllegalArgumentException` check — see [Constructor](./constructor.md) |

# Thread-Safety

The instance holds only immutable fields (`sheetName: String`, `columns: List<Column<T>>`,
defensively copied via `List.copyOf` in the private constructor) and is safely shareable across
threads after construction. Each `export(...)` call creates its own
`WorkbookBuilder`/`XlsxBuilder` internally (verified: `newSheetWithColumns` builds a fresh
`XlsxBuilder` every call) — no shared mutable state across concurrent `export` calls on the same
`GridExcelExporter` instance. The Vaadin `Grid`/`Column` objects referenced by `columns` are
themselves not thread-safe (ordinary Vaadin UI components); this class's thread-safety contract
covers only its own state, not concurrent mutation of the underlying `Grid`.

# Serialization

Not `Serializable` — `GridExcelExporter` implements no serialization interface (verified against
the class declaration `public final class GridExcelExporter<T>`, no `implements` clause). No
serialization contract; would also be awkward to serialize meaningfully since it holds live
Vaadin `Grid.Column` references.

# equals/hashCode/toString

None of these methods are overridden (verified: no `equals`/`hashCode`/`toString` declaration in
the source) — identity semantics from `java.lang.Object` apply (`==` comparison,
identity-based hashcode, `toString()` returns class name + hashcode). Each `GridExcelExporter`
instance is typically created once via `from(...)` and used directly, so value-based equality is
not expected to matter in practice.

# Inheritance Hierarchy


**Forward (own declaration):** verified declaration line —

```java
public final class GridExcelExporter<T> {
```

No `extends` clause (implicit `java.lang.Object` only) and no `implements` clause —
`GridExcelExporter` does **not** implement any xlsxBuilder interface. It uses several
xlsxBuilder types heavily (`ColumnGroup`, `DataProviders`, `WorkbookBuilder`, `XlsxBuilder`,
and `de.makno.xlsxbuilder.DataProvider`, plus `ResultSetRowMapper` mentioned in Javadoc), but
verified in the imports and method bodies: every one of these appears only as a method
parameter, return type, or local variable type — pure composition/collaboration, never as a
supertype in a `class ... extends`/`implements` clause. Likewise it neither extends nor
implements any Vaadin type (`Grid`, `Grid.Column`, `DataProvider` from
`com.vaadin.flow.data.provider` are all used the same collaborator way, not inherited from).

**Backward (project-internal subtypes):** none. Verified by grep across
`library/src/main/java/de/makno/vaadinexcelexport/` and `library/src/test/java/...` for
`extends GridExcelExporter` / `implements GridExcelExporter` — no matches. `final` also
precludes it.

**Summary:** keine Ober-/Unterklassen; `final class`, erweitert nur `java.lang.Object`,
implementiert keine Interfaces — including none from its own `de.makno.xlsxbuilder` dependency,
despite deep collaboration with `WorkbookBuilder`/`XlsxBuilder`/`DataProvider` at the method
level. Its only constructor is `private`, reachable solely through the static `from(...)`
factory methods, further underscoring that this is a closed, non-extensible bridge type rather
than a base class meant for subclassing.

# Constructors

- [see constructor.md](./constructor.md)

# Methods

**Static factory methods:**

- [`public static <T> GridExcelExporter<T> from(String sheetName, Grid<T> grid)`](./from.md) — creates an exporter for all `ExcelMeta`-annotated columns in grid visible order (both `from(...)` overloads live in this one file).

**Instance methods:**

- [`public void export(DataProvider<T, ?> dataProvider, OutputStream out) throws IOException`](./export.md) — writes the table as `.xlsx`; all 4 `export(...)` overloads live in this one file (unsorted in-memory, sorted in-memory, out-of-core via xlsxBuilder `DataProvider`, and out-of-core with `ExportOptions`).
- [`private XlsxBuilder<T> newSheetWithColumns(ExportOptions options)`](./new-sheet-with-columns.md) — builds the sheet definition (headers, types, formats, converters, groups, sum columns, footer, parallelism) without data.
- [`private List<ColumnGroup> buildColumnGroups()`](./build-column-groups.md) — builds the joined-header `ColumnGroup` list from column group labels.
- [`private static <T> Stream<T> fetchAll(DataProvider<T, ?> dataProvider, Comparator<T> inMemorySort)`](./fetch-all.md) — fetches all rows from a Vaadin `DataProvider` as a lazy `Stream`.

# Citations

[1] `library/src/main/java/de/makno/vaadinexcelexport/GridExcelExporter.java`
[2] `library/src/test/java/de/makno/vaadinexcelexport/GridExcelExporterTest.java` (behavioral verification for column ordering, skip-without-key/meta, sort order, footer placeholders, joined headers)
[3] xlsxBuilder source consulted for verified exception/close behavior: `WorkbookBuilder.java`, `XlsxBuilder.java`, `DataProviders.java`, `ColumnGroup.java` (external dependency, not part of this repository)
[4] Higher-level narrative: [GridExcelExporter](/components/grid-excel-exporter.md), [Out-of-core export](/architecture/out-of-core-export.md)
