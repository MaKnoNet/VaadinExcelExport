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

# Constructors

## `private GridExcelExporter(String sheetName, List<Column<T>> columns)`

Not part of the public API — instances are only created via the static `from(...)` factory
methods.

- **Parameters:**
  - `sheetName` (`String`) — **null allowed: no** — verified:
    `Objects.requireNonNull(sheetName, "sheetName")`.
  - `columns` (`List<Column<T>>`) — **null allowed: no** — verified:
    `Objects.requireNonNull(columns, "columns")`, then defensively copied with
    `List.copyOf(...)` (which additionally rejects a list containing `null` elements).
- **On invalid input:**
  - `NullPointerException` if `sheetName` is `null` (message `"sheetName"`), if `columns` is
    `null` (message `"columns"`), or if `columns` contains a `null` element (from
    `List.copyOf`).
  - `IllegalArgumentException` — verified: thrown immediately after copying if
    `this.columns.isEmpty()`, message `"Mindestens eine Spalte mit ExcelMeta.type(...) und
    Column.setKey() erforderlich"`.

# Static factory methods

## `public static <T> GridExcelExporter<T> from(String sheetName, Grid<T> grid)`

Convenience overload; delegates to the three-argument overload with an empty column-key order
(meaning: use the grid's own visible column order).

- **Parameters:**
  - `sheetName` (`String`) — becomes the Excel sheet name. **Null allowed: no** (see
    constructor above — the `null` check happens downstream in the private constructor, not in
    this method itself).
  - `grid` (`Grid<T>`) — **null allowed: no** — verified in the three-arg overload via
    `Objects.requireNonNull(grid, "grid")` (this two-arg overload has no null-check of its own,
    but immediately calls the three-arg overload which does).
- **Return value:** `GridExcelExporter<T>`, never `null` on success.
- **Exceptions actually thrown:**
  - `NullPointerException` — if `grid` is `null` (from the delegate, message `"grid"`); if
    `sheetName` is `null` (from the private constructor, message `"sheetName"`).
  - `IllegalArgumentException` — verified: thrown when, after filtering the grid's columns down
    to those with both a non-null `Column.getKey()` **and** a non-null `ExcelMeta.getType(col)`,
    zero columns remain. Message: `"Mindestens eine Spalte mit ExcelMeta.type(...) und
    Column.setKey() erforderlich"`. Confirmed by `GridExcelExporterTest.rejectsGridWithoutAnnotatedColumns`
    and `rejectsGridWithoutKeys`.

## `public static <T> GridExcelExporter<T> from(String sheetName, Grid<T> grid, List<String> columnKeyOrder)`

Same as the two-arg overload, but exports columns in the order given by `columnKeyOrder`
(a list of `Column.getKey()` values) instead of the grid's visible order.

- **Parameters:**
  - `sheetName` (`String`) — **null allowed: no**, same as above (checked downstream in the
    private constructor).
  - `grid` (`Grid<T>`) — **null allowed: no** — verified:
    `Objects.requireNonNull(grid, "grid")`.
  - `columnKeyOrder` (`List<String>`) — **null allowed: no** — verified:
    `Objects.requireNonNull(columnKeyOrder, "columnKeyOrder")`. Individual `null` elements
    inside the list are **not rejected by this method** — they are used as map keys in
    `.map(exportableByKey::get)`; `exportableByKey.get(null)` on a `LinkedHashMap` simply
    returns `null` (no `NullPointerException`, `LinkedHashMap` permits a `null` key lookup),
    and the subsequent `.filter(Objects::nonNull)` silently drops that entry. So a `null`
    element in `columnKeyOrder` is effectively ignored, not an error. **Unknown keys** (keys
    present in the list but not matching any exportable column) are likewise silently ignored
    (verified: `.filter(Objects::nonNull)` after the `Map::get` lookup, which returns `null`
    for unknown keys too) — this matches the Javadoc's claim "unbekannte Keys werden ignoriert."
    An **empty** `columnKeyOrder` list falls back to grid order (verified:
    `columnKeyOrder.isEmpty() ? List.copyOf(exportableByKey.values()) : ...`).
- **Return value:** `GridExcelExporter<T>`, never `null` on success.
- **Exceptions actually thrown:**
  - `NullPointerException` — if `grid` is `null` (message `"grid"`) or `columnKeyOrder` is
    `null` (message `"columnKeyOrder"`); if `sheetName` is `null` (from the private
    constructor).
  - `IllegalArgumentException` — thrown by the private constructor if the resulting ordered
    column list is empty. This can happen two ways: (a) the grid had no exportable columns at
    all, or (b) `columnKeyOrder` was non-empty but matched none of the grid's exportable column
    keys — both surface the identical message
    `"Mindestens eine Spalte mit ExcelMeta.type(...) und Column.setKey() erforderlich"`, which
    reads slightly misleadingly for case (b) since the grid *did* have annotated columns; the
    message doesn't distinguish "no exportable columns exist" from "your key order matched
    none of them."

# Instance methods

## `public void export(DataProvider<T, ?> dataProvider, OutputStream out) throws IOException`

Shortcut for `export(dataProvider, null, out)` — unsorted export.

- **Parameters:**
  - `dataProvider` (`DataProvider<T, ?>`) — Vaadin data provider. **Null allowed:** not checked
    in this overload directly, but the delegate (`export(DataProvider, Comparator, OutputStream)`)
    does check it — see below.
  - `out` (`OutputStream`) — **null allowed:** checked in the delegate, not here directly.
- **Return value:** `void`.
- **Exceptions actually thrown:** whatever the two-argument-plus-comparator overload throws
  (see next section) — this method adds no behavior or checks of its own.
- **Memory note (verified against the delegate):** materializes **all** rows of `dataProvider`
  via an unbounded `Query` (`Integer.MAX_VALUE` limit) — matches the Javadoc's stated caveat.

## `public void export(DataProvider<T, ?> dataProvider, Comparator<T> inMemorySort, OutputStream out) throws IOException`

Writes the grid's data to `out`, applying `inMemorySort` to determine row order.

- **Parameters:**
  - `dataProvider` (`DataProvider<T, ?>`) — **null allowed: no** — verified:
    `Objects.requireNonNull(dataProvider, "dataProvider")`.
  - `inMemorySort` (`Comparator<T>`) — **null allowed: yes** — verified: passed straight into
    `fetchAll(dataProvider, inMemorySort)`, which puts it into `new Query<>(0,
    Integer.MAX_VALUE, null, inMemorySort, null)` — Vaadin's `Query` accepts a `null`
    `sortOrders`/comparator argument to mean "unsorted." No null-check exists for this
    parameter, and none is needed since `null` is a valid, meaningful value here (matches
    Javadoc: "`null` = unsortiert").
  - `out` (`OutputStream`) — **null allowed: no** — verified:
    `Objects.requireNonNull(out, "out")`.
- **Return value:** `void`.
- **Exceptions actually thrown:**
  - `NullPointerException` — if `dataProvider` is `null` (message `"dataProvider"`) or `out` is
    `null` (message `"out"`).
  - `IOException` — declared and propagated from the delegate `export(xlsxbuilder.DataProvider,
    OutputStream)`, ultimately from xlsxBuilder's `WorkbookBuilder.write(OutputStream)` (I/O
    failure while writing to `out`).
  - Any `RuntimeException` thrown by `inMemorySort` itself (e.g. a `ClassCastException` from an
    incompatible `Comparator`) propagates unmodified, since it runs inside the `fetch(...)` call
    triggered by Vaadin's `DataProvider`.
- **Memory note (verified):** the Javadoc's claim that this overload "fragt den Vaadin-
  DataProvider mit unbeschränktem Limit ab" is accurate — verified in `fetchAll`: `new
  Query<>(0, Integer.MAX_VALUE, null, inMemorySort, null)`.

## `public void export(de.makno.xlsxbuilder.DataProvider<T> data, OutputStream out) throws IOException`

Shortcut for `export(data, out, ExportOptions.none())`.

- **Parameters:**
  - `data` (`xlsxbuilder.DataProvider<T>`) — **null allowed:** not checked in this overload
    directly; checked in the three-argument delegate.
  - `out` (`OutputStream`) — same as above, checked in the delegate.
- **Return value:** `void`.
- **Exceptions actually thrown:** whatever the three-argument overload throws (see below); adds
  no behavior of its own.

## `public void export(de.makno.xlsxbuilder.DataProvider<T> data, OutputStream out, ExportOptions options) throws IOException`

The out-of-core export entry point — data comes directly from an xlsxBuilder `DataProvider`
(e.g. `DataProviders.ofResultSet(...)`), columns from the grid.

- **Parameters:**
  - `data` (`xlsxbuilder.DataProvider<T>`) — **null allowed: no** — verified:
    `Objects.requireNonNull(data, "data")`.
  - `out` (`OutputStream`) — **null allowed: no** — verified: `Objects.requireNonNull(out,
    "out")`.
  - `options` (`ExportOptions`) — **null allowed: no** — verified:
    `Objects.requireNonNull(options, "options")`. Note: [ExportOptions](/api-reference/export-options.md)`.none()`
    is the safe default the two-argument overload passes; a caller of this three-argument
    overload directly must not pass `null` here, unlike `inMemorySort` in the earlier overload
    which does tolerate `null`.
- **Return value:** `void`.
- **Exceptions actually thrown:**
  - `NullPointerException` — if `data`, `out`, or `options` is `null` (respective messages
    `"data"` / `"out"` / `"options"`).
  - `IOException` — declared, and propagated from `WorkbookBuilder.write(OutputStream)`
    (verified in xlsxBuilder source: wraps the actual sheet-rendering + POI-writing step;
    thrown on genuine I/O failure writing to `out`).
  - `IllegalArgumentException` — **not thrown by this method or class directly**, but verified
    to propagate from xlsxBuilder's `XlsxBuilder.renderInto(...)` (invoked transitively via
    `WorkbookBuilder.write`) in these cases relevant to how `GridExcelExporter` uses it:
    - an entry in `ExportOptions.sumColumns()` that does not match any column name on the sheet
      (`"Unknown sum column: " + name`);
    - a sum column whose values are not numeric (`"Sum column is not numeric: " + name`);
    - (less likely from `GridExcelExporter`'s own usage, but part of the same call chain) an
      unknown sort column or unknown summary-label column, if those xlsxBuilder features were
      ever wired through — currently `GridExcelExporter` does not call `sortBy`/`summaryLabel`
      itself, so these specific messages are not reachable through this class today.
  - `IllegalStateException` — verified to propagate from xlsxBuilder if, hypothetically, the
    constructed `XlsxBuilder`/`WorkbookBuilder` were reused for a second write or had no columns
    — not reachable in practice through `GridExcelExporter`, since `newSheetWithColumns` always
    builds a fresh `XlsxBuilder` with at least one column (enforced by the constructor's
    non-empty-columns check) on every `export` call.
  - Any exception thrown by [ColumnValueExtractor.extract](/api-reference/column-value-extractor.md)
    while xlsxBuilder pulls values row-by-row (e.g. `IllegalStateException` if a column's value
    cannot be resolved) propagates through `write` unmodified, since `write` re-throws
    `IOException | RuntimeException | Error` after cleanup (verified in
    `WorkbookBuilder.write`).
- **Resource handling (verified against xlsxBuilder):** on any failure during `write`,
  `WorkbookBuilder` closes the data sources of **all** supplied sheets — including `data` itself
  if it was never reached — so `data` (e.g. a JDBC-`ResultSet`-backed provider) will not leak
  even on error. On success, xlsxBuilder closes `data` after fully consuming it (per
  `DataProviders.ofResultSet`'s contract: "closes only the ResultSet" — a wrapping `Statement`/
  `Connection` remains the caller's responsibility). `out` itself is never closed by this method
  or by xlsxBuilder (matches Javadoc: "Ziel-Stream (wird nicht geschlossen)").

## `private XlsxBuilder<T> newSheetWithColumns(ExportOptions options)`

Builds the sheet definition (headers, types, formats, converters, groups, sum columns, footer,
parallelism) without data.

- **Parameters:** `options` (`ExportOptions`) — **null allowed: no** in practice (calls
  `options.sumColumns()`/`options.footerLines()`/`options.parallel()` unconditionally); not
  independently null-checked in this private method since its only caller
  (`export(DataProvider, OutputStream, ExportOptions)`) already guarantees non-null via
  `Objects.requireNonNull`.
- **Return value:** `XlsxBuilder<T>`, never `null`.
- **Exceptions actually thrown:** none directly from this method's own logic; delegates to
  `ExcelMeta.getType`/`getFormat`/`getConverter` (all tolerate columns without metadata,
  returning `null`, checked with `if (x != null)` before use) and to `buildColumnGroups()`
  (see below). Validation of e.g. duplicate/unknown sum columns happens later, at
  `XlsxBuilder.renderInto(...)` time (inside `write`), not here.

## `private List<ColumnGroup> buildColumnGroups()`

Builds the joined-header `ColumnGroup` list from each column's `ExcelMeta` group label,
merging contiguous columns that share the same non-null label.

- **Parameters:** none (reads `this.columns`).
- **Return value:** `List<ColumnGroup>`. **Can be empty** (verified: returns `List.of()`
  immediately if no column has a group label — `anyMatch(col -> ExcelMeta.getGroup(col) !=
  null)` is `false`) but **never `null`**.
- **Exceptions actually thrown:** none directly. Constructing each `ColumnGroup(label, span)`
  uses `currentLabel == null ? "" : currentLabel` before the constructor call, so xlsxBuilder's
  `ColumnGroup` compact constructor (which itself does `Objects.requireNonNull(label, "label")`
  and rejects `span < 1`) never actually receives a `null` label from this call site — the
  `IllegalArgumentException`/`NullPointerException` that `ColumnGroup`'s own constructor could
  throw are not reachable through this code path given `span` is always incremented to at least
  `1` for every column processed.

## `private static <T> Stream<T> fetchAll(DataProvider<T, ?> dataProvider, Comparator<T> inMemorySort)`

Fetches all rows from a Vaadin `DataProvider` as a `Stream`, using an unbounded `Query`.

- **Parameters:**
  - `dataProvider` (`DataProvider<T, ?>`) — **null allowed: no** in practice (unconditionally
    cast and dereferenced); not explicitly checked in this method itself (the public caller
    already enforces non-null).
  - `inMemorySort` (`Comparator<T>`) — **null allowed: yes**, passed straight into `Query`
    (see above).
- **Return value:** `Stream<T>`, never `null`. **Important (verified):** this is a *lazy*
  stream backed directly by `typed.fetch(...)` — `GridExcelExporter` does not eagerly collect
  it into a `List` anywhere; it is wrapped by `DataProviders.ofStream(...)` in the caller, which
  closes the stream when the resulting `xlsxbuilder.DataProvider.close()` is called. Despite
  being lazily streamed, the *query itself* requests `Integer.MAX_VALUE` rows up front from the
  Vaadin `DataProvider`, so for callback/lazy-loading Vaadin data providers (e.g. backed by a
  DB), this still forces materialization of the entire result set into memory at the Vaadin
  layer — matching the documented memory caveat.
- **Exceptions actually thrown:** none directly; the `@SuppressWarnings("unchecked")` cast
  `(DataProvider<T, Object>) dataProvider` is unchecked but safe in practice because the
  `Query`'s filter is always `null` — no `ClassCastException` is possible from this cast alone,
  though a `ClassCastException` could theoretically surface later if the underlying
  `DataProvider` implementation itself misbehaves with a `null` filter (not observed, not
  expected).

# Citations

[1] `library/src/main/java/de/makno/vaadinexcelexport/GridExcelExporter.java`
[2] `library/src/test/java/de/makno/vaadinexcelexport/GridExcelExporterTest.java` (behavioral verification for column ordering, skip-without-key/meta, sort order, footer placeholders, joined headers)
[3] xlsxBuilder source consulted for verified exception/close behavior: `WorkbookBuilder.java`, `XlsxBuilder.java`, `DataProviders.java`, `ColumnGroup.java` (external dependency, not part of this repository)
[4] Higher-level narrative: [GridExcelExporter](/components/grid-excel-exporter.md), [Out-of-core export](/architecture/out-of-core-export.md)
