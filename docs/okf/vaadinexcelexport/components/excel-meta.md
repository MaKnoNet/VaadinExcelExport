---
type: Library Component
title: ExcelMeta
description: Per-column export metadata attached directly to a Vaadin Grid.Column (via ComponentUtil) — type, value provider, Excel format, converter, header group. The grid stays the single source of truth.
resource: library/src/main/java/de/makno/vaadinexcelexport/ExcelMeta.java
tags: [component, vaadin, excel, metadata, annotation]
timestamp: '2026-07-04T17:30:00+02:00'
---

# Overview

`ExcelMeta` annotates `Grid` columns in place — no separate column model to keep in sync.
Columns **without** `ExcelMeta.type(...)` (or without a key) are simply skipped on export.

# Schema

| Call | Effect |
|---|---|
| `ExcelMeta.type(column, ColumnType)` | mark column for export; enough for `STRING` (value read from the renderer) |
| `ExcelMeta.type(column, type, valueProvider)` | typed column with explicit value provider (required for all non-STRING types) |
| `.format("#,##0.00 \"EUR\"")` | Excel number/date format for the cell |
| `.converter(raw -> cellValue)` | convert the raw value into the cell value |
| `ExcelMeta.group(column, "Label")` | joined-header group label, merged across column ranges |

Supported `ColumnType`s: `STRING, INTEGER, LONG, DOUBLE, DECIMAL, BOOLEAN, DATE, DATETIME,
TIME, FORMULA, HYPERLINK` (from xlsxBuilder). `FORMULA` columns return the formula text
(e.g. `"E{row}*0.19"`); `{row}` becomes the real row number.

# Examples

```java
Column<Employee> salary = grid.addColumn(Employee::salary).setKey("Salary").setHeader("Salary");
ExcelMeta.type(salary, ColumnType.DECIMAL, Employee::salary).format("#,##0.00 \"EUR\"");
ExcelMeta.group(salary, "Personal data");
```

# Citations

[1] [README - Concepts: ExcelMeta](https://github.com/MaKnoNet/VaadinExcelExport/blob/master/README.md)
