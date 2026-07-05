---
type: API Reference
title: ExportOptions.withSumColumns(...)
description: Method withSumColumns of ExportOptions - see signature(s) below.
resource: library/src/main/java/de/makno/vaadinexcelexport/ExportOptions.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `public ExportOptions withSumColumns(String... columns)`


Returns a copy with `sumColumns` replaced by `columns`.

- **Parameters:** `columns` (`String...`) — same null-handling as `lines` in `withFooter`:
  **null allowed: no** for the array (`Objects.requireNonNull(columns, "columns")`) and **no**
  for individual elements (`List.of(columns)` rejects `null` elements).
- **Return value:** `ExportOptions`, never `null` — a new instance; `footerLines` and
  `parallel` carried over unchanged.
- **Exceptions actually thrown:** `NullPointerException` if `columns` is `null` (message
  `"columns"`) or contains a `null` element (message from `List.of`).
- **Note:** this method does not verify that the named columns actually exist on the grid /
  sheet at call time — that validation happens later, inside xlsxBuilder's `XlsxBuilder`
  (`"Unknown sum column: " + name` as an `IllegalArgumentException`, thrown from
  `renderInto(...)` during `GridExcelExporter.export(...)`, not from this method).

# Citations

[1] [ExportOptions (Overview)](./export-options.md)
