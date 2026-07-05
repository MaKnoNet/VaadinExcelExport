---
type: API Reference
title: GridExcelExporter.fetchAll(...)
description: Method fetchAll of GridExcelExporter - see signature(s) below.
resource: library/src/main/java/de/makno/vaadinexcelexport/GridExcelExporter.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `private static <T> Stream<T> fetchAll(DataProvider<T, ?> dataProvider, Comparator<T> inMemorySort)`


Fetches all rows from a Vaadin `DataProvider` as a `Stream`, using an unbounded `Query`.

- **Parameters:**
  - `dataProvider` (`DataProvider<T, ?>`) — **null allowed: no** in practice (unconditionally
    cast and dereferenced); not explicitly checked in this method itself (the public caller
    already enforces non-null).
  - `inMemorySort` (`Comparator<T>`) — **null allowed: yes**, passed straight into `Query`
    (see above).
- **Return value:** `Stream<T>`, never `null`. **Important (verified):** this is a *lazy*
  stream backed directly by `typed.fetch(...)` — `GridExcelExporter` does not eagerly collect
  it into a `List` anywhere; it is wrapped by `DataProviders.ofStream(...)` in the caller, which
  closes the stream when the resulting `xlsxbuilder.DataProvider.close()` is called. Despite
  being lazily streamed, the *query itself* requests `Integer.MAX_VALUE` rows up front from the
  Vaadin `DataProvider`, so for callback/lazy-loading Vaadin data providers (e.g. backed by a
  DB), this still forces materialization of the entire result set into memory at the Vaadin
  layer — matching the documented memory caveat.
- **Exceptions actually thrown:** none directly; the `@SuppressWarnings("unchecked")` cast
  `(DataProvider<T, Object>) dataProvider` is unchecked but safe in practice because the
  `Query`'s filter is always `null` — no `ClassCastException` is possible from this cast alone,
  though a `ClassCastException` could theoretically surface later if the underlying
  `DataProvider` implementation itself misbehaves with a `null` filter (not observed, not
  expected).

# Citations

[1] [GridExcelExporter (Overview)](./grid-excel-exporter.md)
