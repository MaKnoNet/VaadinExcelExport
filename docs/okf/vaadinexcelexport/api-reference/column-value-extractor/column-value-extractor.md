---
type: API Reference
title: ColumnValueExtractor
description: Verified constructor/method-level reference for the package-private ColumnValueExtractor fallback chain that resolves a Grid column's export value.
resource: library/src/main/java/de/makno/vaadinexcelexport/ColumnValueExtractor.java
tags: [api-reference, vaadin, excel, reflection, internal]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Overview

`ColumnValueExtractor` is a package-private, stateless (no instance fields, only static
members) helper used by [GridExcelExporter](/components/grid-excel-exporter.md) to read the
export value of a `Grid.Column` for a given row item. It tries three strategies in order: the
explicit `ExcelMeta` value provider, a `LitRenderer` value provider, and a reflection-based
fallback into Vaadin's internal `ColumnPathRenderer.provider` field. See
[ExcelMeta](/components/excel-meta.md) for the recommended, explicit path.

**Thread-safety:** stateless apart from one lazily-resolved-once-at-class-load `static final
Field` cache (`COLUMN_PATH_PROVIDER_FIELD`), which is set exactly once during class
initialization and only read afterwards — safe for concurrent use.

This class is package-private; it has no public API surface. It is documented here because
`GridExcelExporter` calls directly into it and its null/exception behavior is part of the
export contract.

# Fields

Verified against the source: the class has exactly one field, a static cache — there are no
instance fields at all (consistent with the class being a stateless, non-instantiable helper).

| Field | Type | Meaning | Nullable |
|---|---|---|---|
| `COLUMN_PATH_PROVIDER_FIELD` | `private static final Field` | Vaadin-internal `ColumnPathRenderer.provider` field, resolved once via reflection at class-load time (`resolveColumnPathProviderField()`) and cached. | yes — `null` when the field does not exist in the running Vaadin version or cannot be made accessible (reflection failure swallowed at resolution time, see `resolveColumnPathProviderField()`); a `null` value here is what makes `extractFromColumnPathRenderer` throw `IllegalStateException` at first use |

# Thread-Safety

Stateless apart from the one lazily-resolved-once-at-class-load `static final Field` cache
(`COLUMN_PATH_PROVIDER_FIELD`), which is set exactly once during class initialization (JVM
guarantees safe publication of `static final` fields set during `<clinit>`) and only read
afterwards — safe for unsynchronized concurrent use. All methods are `static` with no shared
mutable state, and parameters (`item`, `column`, renderer instances) are used read-only, never
mutated or cached across calls.

# Serialization

Not `Serializable` — `ColumnValueExtractor` implements no serialization interface (verified
against the class declaration `final class ColumnValueExtractor`, no `implements` clause). No
serialization contract; not applicable for a non-instantiable static-utility class.

# equals/hashCode/toString

None of these methods are overridden (verified: no `equals`/`hashCode`/`toString` declaration
in the source) — **not applicable in practice**, since the class has a private constructor and
is never instantiated; only `java.lang.Class` identity is ever observable (e.g. via
reflection), not instance identity.

# Inheritance Hierarchy


**Forward (own declaration):** verified declaration line —

```java
final class ColumnValueExtractor {
```

No `extends` clause (implicit `java.lang.Object` only) and no `implements` clause — this class
implements no interface, project-internal or JDK/framework.

**Backward (project-internal subtypes):** none. Verified by grep across
`library/src/main/java/de/makno/vaadinexcelexport/` and `library/src/test/java/...` for
`extends ColumnValueExtractor` / `implements ColumnValueExtractor` — no matches. The class is
also package-private, so it could not be subclassed from outside this package even if it were
not `final`.

**Summary:** keine Ober-/Unterklassen; `final class`, erweitert nur `java.lang.Object`,
implementiert keine Interfaces. It also carries a private no-arg constructor, reinforcing that
it is a non-instantiable static-utility class rather than a type meant to participate in any
hierarchy.

# Constructors

- [see constructor.md](./constructor.md)

# Methods

- [`static <T> Object extract(T item, Column<T> column)`](./extract.md) — resolves the export value for a column on a row item, trying all three strategies in order.
- [`private static Field resolveColumnPathProviderField()`](./resolve-column-path-provider-field.md) — resolves and caches the Vaadin-internal `ColumnPathRenderer.provider` field via reflection.
- [`private static <T> Object extractFromLitRenderer(T item, LitRenderer<T> renderer)`](./extract-from-lit-renderer.md) — extracts a value from a `LitRenderer`'s registered value providers.
- [`private static <T> Object extractFromColumnPathRenderer(T item, ColumnPathRenderer<T> renderer)`](./extract-from-column-path-renderer.md) — reflection-based fallback reading `ColumnPathRenderer`'s private `provider` field.

# Citations

[1] `library/src/main/java/de/makno/vaadinexcelexport/ColumnValueExtractor.java`
[2] Higher-level narrative: [GridExcelExporter](/components/grid-excel-exporter.md) — "ColumnValueExtractor (internal fallback chain)" section
