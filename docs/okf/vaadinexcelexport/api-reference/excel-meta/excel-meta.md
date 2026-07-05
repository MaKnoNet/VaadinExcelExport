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
[ColumnValueExtractor](/api-reference/column-value-extractor/column-value-extractor.md); read access after setup is
thread-safe. Concurrent *writes* to the same column (e.g. one thread calling `type(...)` while
another reads) are not synchronized and are not this class's contract to guard against — it
relies on Vaadin UI code running single-threaded per `UI`/session, consistent with normal
Vaadin usage.

# Fields

Verified against the source: five namespace-prefixed `ComponentUtil` data keys, no instance
fields (the class has a private constructor and is never instantiated). `ExcelMeta.Builder<T>`
has one instance field, listed separately below.

| Field | Type | Meaning | Nullable |
|---|---|---|---|
| `KEY_TYPE` | `private static final String` | `ComponentUtil` data key `"de.makno.excel.type"` under which the column's `ColumnType` is stored. | no |
| `KEY_FORMAT` | `private static final String` | Data key `"de.makno.excel.format"` for the Excel format code. | no |
| `KEY_CONVERTER` | `private static final String` | Data key `"de.makno.excel.converter"` for the value converter. | no |
| `KEY_VALUE_PROVIDER` | `private static final String` | Data key `"de.makno.excel.valueProvider"` for the explicit `ValueProvider`. | no |
| `KEY_GROUP` | `private static final String` | Data key `"de.makno.excel.group"` for the joined-header group label. | no |

**`ExcelMeta.Builder<T>` field:**

| Field | Type | Meaning | Nullable |
|---|---|---|---|
| `column` | `private final Column<T>` | The grid column this builder configures; stored verbatim in the package-private constructor, no null-check. | no in intended usage — not defensively checked in the constructor itself; see [Builder constructor](./builder-constructor.md) |

# Thread-Safety

Verified — values are written once during setup (`type(...)`, `group(...)`, and the `Builder`
fluent setters) and only read afterwards by
[GridExcelExporter](/components/grid-excel-exporter.md) /
[ColumnValueExtractor](/api-reference/column-value-extractor/column-value-extractor.md); read
access after setup is thread-safe. Concurrent *writes* to the same column (e.g. one thread
calling `type(...)` while another reads) are not synchronized and are not this class's contract
to guard against — it relies on Vaadin UI code running single-threaded per `UI`/session,
consistent with normal Vaadin usage. `ExcelMeta.Builder<T>` instances are short-lived,
single-use fluent builders returned from `type(...)` and are not meant to be shared across
threads.

# Serialization

Not `Serializable` — neither `ExcelMeta` nor the nested `ExcelMeta.Builder<T>` implements any
serialization interface (verified against both class declarations, no `implements` clause on
either). No serialization contract.

# equals/hashCode/toString

None of these methods are overridden on `ExcelMeta` (verified: no declaration in the source;
not applicable in practice, since the class has a private constructor and is never
instantiated) or on `ExcelMeta.Builder<T>` (verified: no declaration either) — `Builder`
instances therefore use `java.lang.Object`'s identity-based `equals`/`hashCode`/`toString`. This
has little practical relevance since `Builder` is a short-lived fluent-chaining object, not
meant to be compared or collected.

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

- [see constructor.md](./constructor.md)

# Methods

- [`public static <T> Builder<T> type(Column<T> column, ColumnType type, ValueProvider<T, ?> valueProvider)`](./type.md) — associates a `ColumnType` and an explicit `ValueProvider` with the column (both overloads of `type(...)` live in this one file).
- [`static ColumnType getType(Column<?> column)`](./get-type.md) — package-private accessor for the column's `ColumnType`.
- [`static String getFormat(Column<?> column)`](./get-format.md) — package-private accessor for the Excel format code.
- [`static <T> Function<Object, ?> getConverter(Column<T> column)`](./get-converter.md) — package-private accessor for the value converter.
- [`static <T> ValueProvider<T, ?> getValueProvider(Column<T> column)`](./get-value-provider.md) — package-private accessor for the explicit `ValueProvider`.
- [`public static void group(Column<?> column, String label)`](./group.md) — assigns a group label for the joined-header cell.
- [`static String getGroup(Column<?> column)`](./get-group.md) — package-private accessor for the group label.

# Nested class: `ExcelMeta.Builder<T>`

Fluent builder returned by both `type(...)` overloads for setting the remaining optional
metadata (format, converter, group) on the same column.

**Constructor:**

- [`Builder(Column<T> column)` (package-private)](./builder-constructor.md)

**Methods:**

- [`public Builder<T> format(String format)`](./builder-format.md) — sets the Excel number/date format code.
- [`public Builder<T> converter(Function<Object, ?> converter)`](./builder-converter.md) — sets a value converter applied before writing to the Excel cell.
- [`public Builder<T> group(String label)`](./builder-group.md) — delegates to `ExcelMeta.group(column, label)`.

# Citations

[1] `library/src/main/java/de/makno/vaadinexcelexport/ExcelMeta.java`
[2] Higher-level narrative: [ExcelMeta](/components/excel-meta.md)
