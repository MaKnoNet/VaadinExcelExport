---
type: API Reference
title: ExcelMeta.getGroup(...)
description: Method getGroup of ExcelMeta - see signature(s) below.
resource: library/src/main/java/de/makno/vaadinexcelexport/ExcelMeta.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `static String getGroup(Column<?> column)`


- **Parameters:** `column` (`Column<?>`) — same null-handling as `getType`.
- **Return value:** `String`, **can be null** — `null` when no group label was ever assigned
  (or was explicitly cleared via `group(column, null)`, see above).
- **Exceptions actually thrown:** none directly.

# Citations

[1] [ExcelMeta (Overview)](./excel-meta.md)
