---
type: API Reference
title: ExcelMeta.Builder - Constructor
description: The package-private constructor of the nested ExcelMeta.Builder<T> class.
resource: library/src/main/java/de/makno/vaadinexcelexport/ExcelMeta.java
tags: [api-reference, constructor, nested-class]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `Builder(Column<T> column)` (package-private constructor)

- **Parameters:** `column` (`Column<T>`) — **null allowed: no** in practice; stored directly
  into a `final` field with no null-check. A `null` column would not throw here, but the very
  next call into `ComponentUtil` from any of `format`/`converter`/`group` would throw
  `NullPointerException` — this constructor itself is not defensive.
- **Exceptions actually thrown:** none from this constructor's own body.

# Citations

[1] [ExcelMeta (Overview)](./excel-meta.md)
