---
type: API Reference
title: GridExcelExporter - Constructors
description: All constructors of GridExcelExporter.
resource: library/src/main/java/de/makno/vaadinexcelexport/GridExcelExporter.java
tags: [api-reference, constructor]
timestamp: '2026-07-08T09:00:00+02:00'
---


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

# Citations

[1] [GridExcelExporter (Overview)](./grid-excel-exporter.md)
