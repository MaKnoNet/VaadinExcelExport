---
type: API Reference
title: ExcelMeta.type(...)
description: Method type of ExcelMeta - see signature(s) below.
resource: library/src/main/java/de/makno/vaadinexcelexport/ExcelMeta.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `public static <T> Builder<T> type(Column<T> column, ColumnType type, ValueProvider<T, ?> valueProvider)`


Associates a `ColumnType` and an explicit `ValueProvider` with `column` — the recommended
overload for every typed (non-`STRING`) column.

- **Parameters:**
  - `column` (`Column<T>`) — **null allowed: no**. Verified: not itself null-checked by this
    method, but `ComponentUtil.setData(column, ...)` is called with it directly; a `null`
    column throws `NullPointerException` from Vaadin's `ComponentUtil`, not from an explicit
    check in `ExcelMeta`.
  - `type` (`ColumnType`) — **null allowed: no**. Verified:
    `Objects.requireNonNull(type, "type")`.
  - `valueProvider` (`ValueProvider<T, ?>`) — **null allowed: no**. Verified:
    `Objects.requireNonNull(valueProvider, "valueProvider")`.
- **Return value:** `Builder<T>`, never `null` — a fresh builder wrapping `column`.
- **Exceptions actually thrown:**
  - `NullPointerException` — if `type` is `null` (message `"type"`) or `valueProvider` is
    `null` (message `"valueProvider"`). Note: `type` is null-checked and stored *before*
    `valueProvider` is null-checked, so passing `column=validColumn, type=null,
    valueProvider=null` throws for `"type"` first (order matters if a caller inspects the
    message).
  - `NullPointerException` (from Vaadin) if `column` is `null`.

## `public static <T> Builder<T> type(Column<T> column, ColumnType type)`


Associates only a `ColumnType` with `column`, no explicit value provider. Suitable for
`ColumnType.STRING` (the fallback chain in
[ColumnValueExtractor](/api-reference/column-value-extractor/column-value-extractor.md) reads the string-formatted
renderer value in that case).

- **Parameters:**
  - `column` (`Column<T>`) — **null allowed: no** (same as above — not explicitly checked here,
    but `ComponentUtil.setData` dereferences it).
  - `type` (`ColumnType`) — **null allowed: no**. Verified:
    `Objects.requireNonNull(type, "type")`.
- **Return value:** `Builder<T>`, never `null`.
- **Exceptions actually thrown:** `NullPointerException` if `type` is `null` (message
  `"type"`), or if `column` is `null` (from Vaadin's `ComponentUtil`).

# Citations

[1] [ExcelMeta (Overview)](./excel-meta.md)
