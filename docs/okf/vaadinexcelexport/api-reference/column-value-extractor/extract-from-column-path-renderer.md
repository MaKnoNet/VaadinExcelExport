---
type: API Reference
title: ColumnValueExtractor.extractFromColumnPathRenderer(...)
description: Method extractFromColumnPathRenderer of ColumnValueExtractor - see signature(s) below.
resource: library/src/main/java/de/makno/vaadinexcelexport/ColumnValueExtractor.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

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

[1] [ColumnValueExtractor (Overview)](./column-value-extractor.md)
