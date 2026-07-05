---
type: API Reference
title: GridExcelExporter.export(...)
description: Method export of GridExcelExporter - see signature(s) below.
resource: library/src/main/java/de/makno/vaadinexcelexport/GridExcelExporter.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

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
    `Objects.requireNonNull(options, "options")`. Note: [ExportOptions.none](/api-reference/export-options/none.md)`()`
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
  - Any exception thrown by [ColumnValueExtractor.extract](/api-reference/column-value-extractor/extract.md)
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

# Citations

[1] [GridExcelExporter (Overview)](./grid-excel-exporter.md)
