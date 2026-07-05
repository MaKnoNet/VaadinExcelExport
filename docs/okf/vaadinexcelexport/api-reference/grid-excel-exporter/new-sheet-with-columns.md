---
type: API Reference
title: GridExcelExporter.newSheetWithColumns(...)
description: Method newSheetWithColumns of GridExcelExporter - see signature(s) below.
resource: library/src/main/java/de/makno/vaadinexcelexport/GridExcelExporter.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

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

# Citations

[1] [GridExcelExporter (Overview)](./grid-excel-exporter.md)
