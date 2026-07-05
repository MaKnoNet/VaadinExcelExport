---
type: API Reference
title: ColumnValueExtractor.extract(...)
description: Method extract of ColumnValueExtractor - see signature(s) below.
resource: library/src/main/java/de/makno/vaadinexcelexport/ColumnValueExtractor.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

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

# Citations

[1] [ColumnValueExtractor (Overview)](./column-value-extractor.md)
