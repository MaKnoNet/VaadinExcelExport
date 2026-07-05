---
type: API Reference
title: ExcelMeta.getType(...)
description: Method getType of ExcelMeta - see signature(s) below.
resource: library/src/main/java/de/makno/vaadinexcelexport/ExcelMeta.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `static ColumnType getType(Column<?> column)`


Package-private accessor.

- **Parameters:** `column` (`Column<?>`) — **null allowed: no** in practice (dereferenced by
  `ComponentUtil.getData`); no explicit check in this class.
- **Return value:** `ColumnType`, **can be null** — verified: returns whatever
  `ComponentUtil.getData` yields, which is `null` if `type(...)` was never called on this
  column. This `null` is exactly how
  [GridExcelExporter.from](/api-reference/grid-excel-exporter/from.md) decides a column is
  **not** exportable (`ExcelMeta.getType(col) != null` is the inclusion test).
- **Exceptions actually thrown:** none directly (would propagate a `NullPointerException` from
  `ComponentUtil.getData` only if `column` itself were `null`).

# Citations

[1] [ExcelMeta (Overview)](./excel-meta.md)
