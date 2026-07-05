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

| Signature | Parameters | Null allowed | On invalid input |
|---|---|---|---|
| `private ColumnValueExtractor()` | none | n/a | n/a — utility class, never instantiated |

# Methods

## `static <T> Object extract(T item, Column<T> column)`

Resolves the export value for `column` on `item`, trying the three strategies described above
in order.

- **Parameters:**
  - `item` (`T`) — the row object. **Null allowed**: not verified against `null` in this
    method; it is passed straight into whichever `ValueProvider`/renderer-provider is invoked.
    If that provider dereferences `item`, a `NullPointerException` from the provider itself
    (not from `extract`) would surface. `extract` does not itself null-check `item`.
  - `column` (`Column<T>`) — the grid column. **Null allowed: no** (not enforced by an explicit
    check, but `column.getRenderer()` / `column.getKey()` are called unconditionally — passing
    `null` throws `NullPointerException` from that dereference, not a documented contract).
- **Return value:** `Object`, the extracted cell value. **Can be null**: yes, in one specific
  case — `extractFromLitRenderer` returns `null` when the `LitRenderer` has more than one value
  provider and none of them is keyed `"name"` (see below); when that happens, `extract` falls
  through to the `ColumnPathRenderer` check rather than returning `null` itself. If none of the
  three strategies find a provider, `extract` throws instead of returning `null`. The
  *provider's* own return value (e.g. a genuinely `null` cell value from user data) is returned
  as-is and can legitimately be `null`.
- **Exceptions actually thrown:**
  - `IllegalStateException` — thrown in exactly two places, both verified in the code:
    1. no explicit `ExcelMeta` value provider, no usable `LitRenderer` value, and the column's
       renderer is not a `ColumnPathRenderer` either → message
       `"Wert für Spalte '<key-or-(kein Key)>' konnte nicht ermittelt werden. ExcelMeta.type(col,
       type, valueProvider) explizit setzen."`
    2. the column *is* a `ColumnPathRenderer`, but the cached reflective field lookup
       (`COLUMN_PATH_PROVIDER_FIELD`) is `null` (field missing in the running Vaadin version, or
       blocked by the Java module system) → message `"ColumnPathRenderer-Fallback nicht
       verfügbar: ..."`.
  - No checked exceptions are declared or thrown.

**Verified discrepancy vs. Javadoc:** the class Javadoc's `@throws IllegalStateException wenn
kein Wert ermittelt werden konnte` only documents case 1 above. Case 2 (reflection field
unavailable) is a second, distinct trigger for the same exception type that the Javadoc does
not mention at all — not wrong, but incomplete: a caller catching `IllegalStateException` to
detect "no explicit provider set" would also silently catch the very different "Vaadin internal
API changed" failure mode.

## `private static Field resolveColumnPathProviderField()`

Resolves and returns the `ColumnPathRenderer.provider` field via reflection, called once at
class-initialization time to populate `COLUMN_PATH_PROVIDER_FIELD`.

- **Parameters:** none.
- **Return value:** `Field`, or **`null`** if the field does not exist (`NoSuchFieldException`)
  or cannot be made accessible (`RuntimeException` — this catch is deliberately broad to also
  cover `InaccessibleObjectException` from the Java module system, and `SecurityException`).
  Verified: both exception branches return `null` rather than propagating, so this method never
  throws.
- **Exceptions actually thrown:** none — all failure paths are swallowed and converted to a
  `null` return value, deferring the actual failure to first use inside
  `extractFromColumnPathRenderer`.

## `private static <T> Object extractFromLitRenderer(T item, LitRenderer<T> renderer)`

Extracts a value from a `LitRenderer`'s registered value providers.

- **Parameters:**
  - `item` (`T`) — row object, passed to the resolved provider. **Null allowed:** not checked;
    forwarded as-is.
  - `renderer` (`LitRenderer<T>`) — **null allowed: no**, `renderer.getValueProviders()` is
    called unconditionally; `null` throws `NullPointerException` from that call, not a
    documented/intentional contract.
- **Return value:** `Object`, **can be null**. Verified logic:
  - if there is exactly one value provider, that provider's result is returned (whatever it
    is, including `null`);
  - else if a provider keyed `"name"` exists (the hierarchical-column case), its result is
    returned;
  - else (zero or ambiguous multiple providers with no `"name"` key) returns `null` — this
    `null` is a **sentinel meaning "no usable provider found here"**, distinguished by `extract`
    from a legitimate `null` cell value only by the fact that `extract` then continues probing
    `ColumnPathRenderer` instead of accepting the `null`. This means: if a `LitRenderer` has
    exactly one provider and that provider's real return value is `null`, `extract` cannot tell
    the difference from "no provider found" and will still fall through to try
    `ColumnPathRenderer` next (which likely does not apply, since the column already had a
    `LitRenderer`), ultimately still yielding `null` back to the caller in that case — output
    is the same either way, but worth noting the ambiguity is unresolved in the implementation.
- **Exceptions actually thrown:** none directly; whatever the invoked provider throws
  propagates unmodified.

## `private static <T> Object extractFromColumnPathRenderer(T item, ColumnPathRenderer<T> renderer)`

Reflection-based fallback: reads the private `provider` field of `ColumnPathRenderer` and
invokes it.

- **Parameters:**
  - `item` (`T`) — forwarded to the resolved provider. **Null allowed:** not checked.
  - `renderer` (`ColumnPathRenderer<T>`) — **null allowed: no** (dereferenced by
    `COLUMN_PATH_PROVIDER_FIELD.get(renderer)`; passing `null` throws `NullPointerException`
    from `Field.get`, not from an explicit check in this method).
- **Return value:** `Object`, the value returned by the resolved `ValueProvider`. **Can be
  null**: yes, if the underlying provider itself returns `null`.
- **Exceptions actually thrown:**
  - `IllegalStateException` — if `COLUMN_PATH_PROVIDER_FIELD` is `null` (reflection lookup
    failed earlier), with the message documented above under `extract`.
  - `IllegalStateException` — wrapping an `IllegalAccessException` if `Field.get(renderer)`
    fails at invocation time (message: `"Wert konnte nicht via ColumnPathRenderer-Reflexion
    ermittelt werden"`, cause = the original `IllegalAccessException`).
  - The `@SuppressWarnings("unchecked")` cast to `ValueProvider<T, ?>` can theoretically throw
    `ClassCastException` if the field's runtime value is not actually a `ValueProvider` — not
    expected in practice given how Vaadin populates the field, but not defended against in code
    either.

# Citations

[1] `library/src/main/java/de/makno/vaadinexcelexport/ColumnValueExtractor.java`
[2] Higher-level narrative: [GridExcelExporter](/components/grid-excel-exporter.md) — "ColumnValueExtractor (internal fallback chain)" section
