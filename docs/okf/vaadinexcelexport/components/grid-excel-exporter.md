---
type: Library Component
title: GridExcelExporter
description: Fluent exporter that writes a Vaadin Grid to .xlsx via xlsxBuilder — respects sort order and active filter, supports in-memory and out-of-core (ResultSet) sources.
resource: library/src/main/java/de/makno/vaadinexcelexport/GridExcelExporter.java
tags: [component, vaadin, excel, export, streaming]
timestamp: '2026-07-04T17:30:00+02:00'
---

# Overview

`GridExcelExporter.from("Sheet", grid)` builds an exporter from the grid's columns and their
[ExcelMeta](/components/excel-meta.md) annotations, then `export(...)` writes the rows to an
`OutputStream`. It follows the table: the grid's **sort order** and its **active filter** are
respected automatically, so the exported rows match what the user sees.

Two source modes:

- **In-memory:** `export(grid.getDataProvider(), grid.getDataCommunicator().getInMemorySorting(), out)`
- **Out-of-core:** `export(DataProviders.ofResultSet(rs, mapper), out, options)` — streamed via
  xlsxBuilder's SXSSF writer with constant memory, see
  [Out-of-core export](/architecture/out-of-core-export.md).

# Schema

| Feature | How |
|---|---|
| Title/footer rows | `ExportOptions` with placeholders `{datetime}`, `{rowCount}`, `{sum:Column}` |
| Summary row | `withSumColumns("Salary")` |
| Joined (grouped) headers | `ExcelMeta.group(column, "Label")`, merged across ranges |
| Export column order | overridable independently of the on-screen order |
| Formulas / hyperlinks | `FORMULA` columns (`"E{row}*0.19"`), `HYPERLINK(...)` via [ExcelFormulas](/components/excel-formulas.md) |
| Parallelism | optional pipeline parallelism, passed through to xlsxBuilder |

## ExportOptions (immutable)

`ExportOptions` bundles the export extras as a record (`footerLines`, `sumColumns`,
`parallel`) — passed per call rather than held as mutable state, so `GridExcelExporter`
keeps its thread-safety contract. `ExportOptions.none()` is the empty default;
`withFooter(...)`/`withSumColumns(...)`/`withParallel(...)` return a fresh copy each
(defensive `List.copyOf`, non-null checks).

## ColumnValueExtractor (internal fallback chain)

Package-private helper that resolves a column's export value in three steps, tried in
order: (1) the explicit `ExcelMeta` value provider (the recommended path for every typed
column), (2) a `LitRenderer`'s value provider (for columns built via
`Grid.addColumn(LitRenderer.of(...))`), (3) a reflection-based fallback into Vaadin's
internal `ColumnPathRenderer.provider` field for plain `Grid.addColumn(ValueProvider)`
columns (string-formatted, so only suitable for `ColumnType.STRING`). Stateless (only
static methods), so it is safe under concurrency. The reflective field lookup happens
once and is cached; if it's unavailable (Vaadin version change, Java module system
blocking access) the fallback fails clearly at first use instead of retrying reflection
on every call.

# Examples

```java
GridExcelExporter.from("Employees", grid)
        .export(DataProviders.ofResultSet(rs, r -> new Employee(r.getString("name"), r.getBigDecimal("salary"))),
                outputStream,
                ExportOptions.none()
                        .withSumColumns("Salary")
                        .withFooter("Generated {datetime} - {rowCount} rows - total {sum:Salary} EUR"));
```

# Citations

[1] [README - Quick start, Concepts](https://github.com/MaKnoNet/VaadinExcelExport/blob/master/README.md)
