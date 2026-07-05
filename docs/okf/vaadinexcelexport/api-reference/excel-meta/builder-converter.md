---
type: API Reference
title: ExcelMeta.Builder.converter(...)
description: Method converter of the nested ExcelMeta.Builder<T> class - see signature below.
resource: library/src/main/java/de/makno/vaadinexcelexport/ExcelMeta.java
tags: [api-reference, method, nested-class]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `public Builder<T> converter(Function<Object, ?> converter)`

Sets a converter applied to the extracted raw value before it is written to the Excel cell.

- **Parameters:** `converter` (`Function<Object, ?>`) — **null allowed: yes** — verified: no
  null-check; storing `null` clears any previously set converter (indistinguishable from never
  set).
- **Return value:** `Builder<T>` — always `this`, never `null`.
- **Exceptions actually thrown:** none directly. (If a `null` converter were later invoked by
  `GridExcelExporter`/xlsxBuilder that would be that call site's concern — `getConverter`
  already returns `null` and `GridExcelExporter` checks `if (converter != null)` before use, so
  in practice a `null` converter is simply never applied.)

# Citations

[1] [ExcelMeta (Overview)](./excel-meta.md)
