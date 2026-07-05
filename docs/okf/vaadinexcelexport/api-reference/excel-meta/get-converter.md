---
type: API Reference
title: ExcelMeta.getConverter(...)
description: Method getConverter of ExcelMeta - see signature(s) below.
resource: library/src/main/java/de/makno/vaadinexcelexport/ExcelMeta.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `static <T> Function<Object, ?> getConverter(Column<T> column)`


- **Parameters:** `column` (`Column<T>`) — same null-handling as `getType`.
- **Return value:** `Function<Object, ?>`, **can be null** — `null` when `.converter(...)` was
  never called.
- **Exceptions actually thrown:** none directly from this method. Carries
  `@SuppressWarnings("unchecked")` for the cast from `Object` (as stored by `ComponentUtil`) to
  `Function<Object, ?>`; a `ClassCastException` is theoretically possible if something else
  wrote a non-`Function` value under the same key, but that cannot happen through this class's
  own public API.

# Citations

[1] [ExcelMeta (Overview)](./excel-meta.md)
