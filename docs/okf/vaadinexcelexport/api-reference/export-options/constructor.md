---
type: API Reference
title: ExportOptions - Constructors
description: All constructors of ExportOptions.
resource: library/src/main/java/de/makno/vaadinexcelexport/ExportOptions.java
tags: [api-reference, constructor]
timestamp: '2026-07-08T09:00:00+02:00'
---


## Canonical constructor (compact form): `public ExportOptions(List<String> footerLines, List<String> sumColumns, boolean parallel)`

The record declares an explicit **compact constructor** (`public ExportOptions { ... }`) that
runs validation/normalization before the implicit field assignment.

- **Parameters:**
  - `footerLines` (`List<String>`) — footer lines rendered under the data, merged across the
    full sheet width. **Null allowed: no** — verified:
    `Objects.requireNonNull(footerLines, "footerLines")`, then wrapped in `List.copyOf(...)`.
    Note: `List.copyOf` itself additionally rejects a list that *contains* `null` elements
    (throws `NullPointerException` from `List.copyOf`'s own internal check) — so individual
    `null` footer lines are also rejected, just not with a custom message from this class.
  - `sumColumns` (`List<String>`) — column names to sum (activates xlsxBuilder's summary row).
    **Null allowed: no** — verified: `Objects.requireNonNull(sumColumns, "sumColumns")`, then
    `List.copyOf(...)` (same "no null elements" consequence as above).
  - `parallel` (`boolean`) — primitive, cannot be `null`.
- **On invalid input:** `NullPointerException` if `footerLines` or `sumColumns` is `null`
  (explicit messages `"footerLines"` / `"sumColumns"` respectively), or if either list
  *contains* a `null` element (message from the JDK's `List.copyOf`, not a custom one from this
  class).
- **Note on record accessors:** because the compact constructor reassigns `footerLines` and
  `sumColumns` to their `List.copyOf(...)` results, the auto-generated accessor methods
  `footerLines()` and `sumColumns()` always return the defensively-copied, unmodifiable list —
  never the caller's original mutable list. `parallel()` returns the primitive `boolean`
  as-is.

# Citations

[1] [ExportOptions (Overview)](./export-options.md)
