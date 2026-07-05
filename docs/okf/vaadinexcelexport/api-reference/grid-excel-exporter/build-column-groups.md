---
type: API Reference
title: GridExcelExporter.buildColumnGroups(...)
description: Method buildColumnGroups of GridExcelExporter - see signature(s) below.
resource: library/src/main/java/de/makno/vaadinexcelexport/GridExcelExporter.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

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

# Citations

[1] [GridExcelExporter (Overview)](./grid-excel-exporter.md)
