---
type: API Reference
title: GridExcelExporter.from(...)
description: Method from of GridExcelExporter - see signature(s) below.
resource: library/src/main/java/de/makno/vaadinexcelexport/GridExcelExporter.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `public static <T> GridExcelExporter<T> from(String sheetName, Grid<T> grid)`


Convenience overload; delegates to the three-argument overload with an empty column-key order
(meaning: use the grid's own visible column order).

- **Parameters:**
  - `sheetName` (`String`) — becomes the Excel sheet name. **Null allowed: no** (see
    constructor above — the `null` check happens downstream in the private constructor, not in
    this method itself).
  - `grid` (`Grid<T>`) — **null allowed: no** — verified in the three-arg overload via
    `Objects.requireNonNull(grid, "grid")` (this two-arg overload has no null-check of its own,
    but immediately calls the three-arg overload which does).
- **Return value:** `GridExcelExporter<T>`, never `null` on success.
- **Exceptions actually thrown:**
  - `NullPointerException` — if `grid` is `null` (from the delegate, message `"grid"`); if
    `sheetName` is `null` (from the private constructor, message `"sheetName"`).
  - `IllegalArgumentException` — verified: thrown when, after filtering the grid's columns down
    to those with both a non-null `Column.getKey()` **and** a non-null `ExcelMeta.getType(col)`,
    zero columns remain. Message: `"Mindestens eine Spalte mit ExcelMeta.type(...) und
    Column.setKey() erforderlich"`. Confirmed by `GridExcelExporterTest.rejectsGridWithoutAnnotatedColumns`
    and `rejectsGridWithoutKeys`.

## `public static <T> GridExcelExporter<T> from(String sheetName, Grid<T> grid, List<String> columnKeyOrder)`


Same as the two-arg overload, but exports columns in the order given by `columnKeyOrder`
(a list of `Column.getKey()` values) instead of the grid's visible order.

- **Parameters:**
  - `sheetName` (`String`) — **null allowed: no**, same as above (checked downstream in the
    private constructor).
  - `grid` (`Grid<T>`) — **null allowed: no** — verified:
    `Objects.requireNonNull(grid, "grid")`.
  - `columnKeyOrder` (`List<String>`) — **null allowed: no** — verified:
    `Objects.requireNonNull(columnKeyOrder, "columnKeyOrder")`. Individual `null` elements
    inside the list are **not rejected by this method** — they are used as map keys in
    `.map(exportableByKey::get)`; `exportableByKey.get(null)` on a `LinkedHashMap` simply
    returns `null` (no `NullPointerException`, `LinkedHashMap` permits a `null` key lookup),
    and the subsequent `.filter(Objects::nonNull)` silently drops that entry. So a `null`
    element in `columnKeyOrder` is effectively ignored, not an error. **Unknown keys** (keys
    present in the list but not matching any exportable column) are likewise silently ignored
    (verified: `.filter(Objects::nonNull)` after the `Map::get` lookup, which returns `null`
    for unknown keys too) — this matches the Javadoc's claim "unbekannte Keys werden ignoriert."
    An **empty** `columnKeyOrder` list falls back to grid order (verified:
    `columnKeyOrder.isEmpty() ? List.copyOf(exportableByKey.values()) : ...`).
- **Return value:** `GridExcelExporter<T>`, never `null` on success.
- **Exceptions actually thrown:**
  - `NullPointerException` — if `grid` is `null` (message `"grid"`) or `columnKeyOrder` is
    `null` (message `"columnKeyOrder"`); if `sheetName` is `null` (from the private
    constructor).
  - `IllegalArgumentException` — thrown by the private constructor if the resulting ordered
    column list is empty. This can happen two ways: (a) the grid had no exportable columns at
    all, or (b) `columnKeyOrder` was non-empty but matched none of the grid's exportable column
    keys — both surface the identical message
    `"Mindestens eine Spalte mit ExcelMeta.type(...) und Column.setKey() erforderlich"`, which
    reads slightly misleadingly for case (b) since the grid *did* have annotated columns; the
    message doesn't distinguish "no exportable columns exist" from "your key order matched
    none of them."

# Citations

[1] [GridExcelExporter (Overview)](./grid-excel-exporter.md)
