---
type: API Reference
title: ExcelMeta.getValueProvider(...)
description: Method getValueProvider of ExcelMeta - see signature(s) below.
resource: library/src/main/java/de/makno/vaadinexcelexport/ExcelMeta.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `static <T> ValueProvider<T, ?> getValueProvider(Column<T> column)`


- **Parameters:** `column` (`Column<T>`) — same null-handling as `getType`.
- **Return value:** `ValueProvider<T, ?>`, **can be null** — `null` when the column was
  annotated via the two-argument `type(Column, ColumnType)` overload (no explicit provider) or
  not annotated at all. This is exactly the signal
  [ColumnValueExtractor.extract](/api-reference/column-value-extractor/extract.md) uses to decide
  whether to try the explicit-provider path first.
- **Exceptions actually thrown:** none directly. Same unchecked-cast caveat as
  `getConverter`.

# Citations

[1] [ExcelMeta (Overview)](./excel-meta.md)
