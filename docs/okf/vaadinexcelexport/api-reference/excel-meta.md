---
type: API Reference
title: ExcelMeta
description: Verified constructor/method-level reference for ExcelMeta and its nested Builder — attaches Excel export metadata to a Grid.Column via ComponentUtil.
resource: library/src/main/java/de/makno/vaadinexcelexport/ExcelMeta.java
tags: [api-reference, vaadin, excel, metadata, annotation]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Overview

`ExcelMeta` attaches export metadata (type, value provider, format, converter, group label)
directly to a `Grid.Column` via Vaadin's `ComponentUtil.setData`/`getData`, keyed by
namespace-prefixed string constants (`"de.makno.excel.*"`). See
[ExcelMeta](/components/excel-meta.md) for the conceptual overview. This file documents every
public and package-private member exhaustively.

**Thread-safety:** verified — values are written once during setup (`type(...)`,
`group(...)`, and the `Builder` fluent setters) and only read afterwards by
[GridExcelExporter](/components/grid-excel-exporter.md) /
[ColumnValueExtractor](/api-reference/column-value-extractor.md); read access after setup is
thread-safe. Concurrent *writes* to the same column (e.g. one thread calling `type(...)` while
another reads) are not synchronized and are not this class's contract to guard against — it
relies on Vaadin UI code running single-threaded per `UI`/session, consistent with normal
Vaadin usage.

# Inheritance Hierarchy

**Forward (own declaration):** verified declaration line —

```java
public final class ExcelMeta {
```

No `extends` clause (implicit `java.lang.Object` only) and no `implements` clause. The nested
`Builder<T>` has its own, separate declaration:

```java
public static final class Builder<T> {
```

`Builder<T>` likewise has no `extends` and no `implements` clause — it is a standalone static
nested class, not a subclass of `ExcelMeta` itself (Java nested classes do not inherit from
their enclosing class merely by nesting).

**Backward (project-internal subtypes):** none for either `ExcelMeta` or `ExcelMeta.Builder`.
Verified by grep across `library/src/main/java/de/makno/vaadinexcelexport/` and
`library/src/test/java/...` for `extends ExcelMeta` / `implements ExcelMeta` — no matches.
Both classes are `final`, ruling out subclassing entirely regardless.

**Summary:** keine Ober-/Unterklassen für `ExcelMeta` und `ExcelMeta.Builder`; beide sind
`final class`, erweitern nur `java.lang.Object`, implementieren keine Interfaces. `ExcelMeta`
also has a private no-arg constructor (non-instantiable static-utility class); `Builder` is
instantiated only internally, via a package-private constructor called from `ExcelMeta.type(...)`.

# Constructors

| Signature | Parameters | Null allowed | On invalid input |
|---|---|---|---|
| `private ExcelMeta()` | none | n/a | n/a — utility class, never instantiated |

# Methods

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
[ColumnValueExtractor](/api-reference/column-value-extractor.md) reads the string-formatted
renderer value in that case).

- **Parameters:**
  - `column` (`Column<T>`) — **null allowed: no** (same as above — not explicitly checked here,
    but `ComponentUtil.setData` dereferences it).
  - `type` (`ColumnType`) — **null allowed: no**. Verified:
    `Objects.requireNonNull(type, "type")`.
- **Return value:** `Builder<T>`, never `null`.
- **Exceptions actually thrown:** `NullPointerException` if `type` is `null` (message
  `"type"`), or if `column` is `null` (from Vaadin's `ComponentUtil`).

## `static ColumnType getType(Column<?> column)`

Package-private accessor.

- **Parameters:** `column` (`Column<?>`) — **null allowed: no** in practice (dereferenced by
  `ComponentUtil.getData`); no explicit check in this class.
- **Return value:** `ColumnType`, **can be null** — verified: returns whatever
  `ComponentUtil.getData` yields, which is `null` if `type(...)` was never called on this
  column. This `null` is exactly how
  [GridExcelExporter.from](/api-reference/grid-excel-exporter.md) decides a column is
  **not** exportable (`ExcelMeta.getType(col) != null` is the inclusion test).
- **Exceptions actually thrown:** none directly (would propagate a `NullPointerException` from
  `ComponentUtil.getData` only if `column` itself were `null`).

## `static String getFormat(Column<?> column)`

- **Parameters:** `column` (`Column<?>`) — same null-handling as `getType`.
- **Return value:** `String`, **can be null** — `null` when `.format(...)` was never called via
  the `Builder`.
- **Exceptions actually thrown:** none directly.

## `static <T> Function<Object, ?> getConverter(Column<T> column)`

- **Parameters:** `column` (`Column<T>`) — same null-handling as `getType`.
- **Return value:** `Function<Object, ?>`, **can be null** — `null` when `.converter(...)` was
  never called.
- **Exceptions actually thrown:** none directly from this method. Carries
  `@SuppressWarnings("unchecked")` for the cast from `Object` (as stored by `ComponentUtil`) to
  `Function<Object, ?>`; a `ClassCastException` is theoretically possible if something else
  wrote a non-`Function` value under the same key, but that cannot happen through this class's
  own public API.

## `static <T> ValueProvider<T, ?> getValueProvider(Column<T> column)`

- **Parameters:** `column` (`Column<T>`) — same null-handling as `getType`.
- **Return value:** `ValueProvider<T, ?>`, **can be null** — `null` when the column was
  annotated via the two-argument `type(Column, ColumnType)` overload (no explicit provider) or
  not annotated at all. This is exactly the signal
  [ColumnValueExtractor.extract](/api-reference/column-value-extractor.md) uses to decide
  whether to try the explicit-provider path first.
- **Exceptions actually thrown:** none directly. Same unchecked-cast caveat as
  `getConverter`.

## `public static void group(Column<?> column, String label)`

Assigns a group label to `column`; contiguous columns sharing the same label are merged into
one joined-header cell on export.

- **Parameters:**
  - `column` (`Column<?>`) — **null allowed: no** in practice; not explicitly checked, but
    `ComponentUtil.setData` dereferences it.
  - `label` (`String`) — **null allowed: yes** — verified: no `Objects.requireNonNull` call;
    `ComponentUtil.setData(column, KEY_GROUP, label)` stores whatever is passed, including
    `null`. Passing `null` effectively un-sets any previously assigned group (subsequent
    `getGroup` calls return `null` again), since it is indistinguishable from "never grouped".
- **Return value:** `void`.
- **Exceptions actually thrown:** none from this method's own logic; `NullPointerException`
  only if `column` itself is `null` (from Vaadin's `ComponentUtil`).

**Note:** the class Javadoc for `group` does not mention `null`-handling for `label` at all;
verified behavior (silently accepted, acts as "clear group") is undocumented but not
contradicted — no discrepancy, just an omission worth stating precisely here.

## `static String getGroup(Column<?> column)`

- **Parameters:** `column` (`Column<?>`) — same null-handling as `getType`.
- **Return value:** `String`, **can be null** — `null` when no group label was ever assigned
  (or was explicitly cleared via `group(column, null)`, see above).
- **Exceptions actually thrown:** none directly.

# Nested class: `ExcelMeta.Builder<T>`

Fluent builder returned by both `type(...)` overloads for setting the remaining optional
metadata (format, converter, group) on the same column.

## `Builder(Column<T> column)` (package-private constructor)

- **Parameters:** `column` (`Column<T>`) — **null allowed: no** in practice; stored directly
  into a `final` field with no null-check. A `null` column would not throw here, but the very
  next call into `ComponentUtil` from any of `format`/`converter`/`group` would throw
  `NullPointerException` — this constructor itself is not defensive.
- **Exceptions actually thrown:** none from this constructor's own body.

## `public Builder<T> format(String format)`

Sets the Excel number/date format code for the column, forwarded verbatim to xlsxBuilder's
`formatForType()`.

- **Parameters:** `format` (`String`) — **null allowed: yes** — verified: no null-check;
  `ComponentUtil.setData(column, KEY_FORMAT, format)` stores whatever is given, including
  `null` (equivalent to never having set a format).
- **Return value:** `Builder<T>` — always returns `this` (never `null`), enabling further
  chaining.
- **Exceptions actually thrown:** none directly.

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

## `public Builder<T> group(String label)`

Delegates to `ExcelMeta.group(column, label)`.

- **Parameters:** `label` (`String`) — **null allowed: yes**, same semantics as
  `ExcelMeta.group`.
- **Return value:** `Builder<T>` — always `this`, never `null`.
- **Exceptions actually thrown:** none directly (delegate has none either, barring a `null`
  `column`, not reachable once the `Builder` was constructed with a valid one).

# Citations

[1] `library/src/main/java/de/makno/vaadinexcelexport/ExcelMeta.java`
[2] Higher-level narrative: [ExcelMeta](/components/excel-meta.md)
