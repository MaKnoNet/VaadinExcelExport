---
type: API Reference
title: ExportOptions
description: Verified constructor/method-level reference for ExportOptions, the immutable record bundling footer lines, sum columns, and parallelism for GridExcelExporter.
resource: library/src/main/java/de/makno/vaadinexcelexport/ExportOptions.java
tags: [api-reference, excel, export, immutable, record]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Overview

`ExportOptions` is an immutable `record` (`footerLines`, `sumColumns`, `parallel`) passed per
call to [GridExcelExporter](/components/grid-excel-exporter.md)`.export(...)`, rather than held
as mutable exporter state — this is what lets `GridExcelExporter` remain safely shareable
across threads. See [GridExcelExporter](/components/grid-excel-exporter.md) for the narrative
overview ("ExportOptions (immutable)" section).

**Thread-safety:** immutable by construction (compact constructor defensively copies both
lists via `List.copyOf`); safe to share and reuse across threads and requests.

# Inheritance Hierarchy

**Forward (own declaration):** verified declaration line —

```java
public record ExportOptions(List<String> footerLines, List<String> sumColumns, boolean parallel) {
```

No explicit `extends` clause is written, but this is a Java `record`: every record implicitly
and non-negotiably extends `java.lang.Record` (an abstract class in `java.lang`) — this is a
JDK language rule, not a project decision, and cannot be overridden (records cannot extend any
other class, and `Record` itself is not further subclassable outside the record mechanism). No
`implements` clause — `ExportOptions` implements no interface, project-internal or
JDK/framework.

**Backward (project-internal subtypes):** none, and none possible. Verified by grep across
`library/src/main/java/de/makno/vaadinexcelexport/` and `library/src/test/java/...` for
`extends ExportOptions` — no matches. This is additionally enforced by the JDK itself: all
Java records are implicitly `final` and can never be extended by any class, so no subclass
could exist even if one were attempted.

**Summary:** keine Ober-/Unterklassen im Projektsinn; `record` (implizit `final`, erweitert
zwingend `java.lang.Record`), implementiert keine Interfaces. The record's own accessor methods
(`footerLines()`, `sumColumns()`, `parallel()`) and `equals`/`hashCode`/`toString` are
JDK-generated from the record components, not inherited business logic.

# Constructors

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

# Methods

## `public static ExportOptions none()`

Returns the shared "no extra options" instance.

- **Parameters:** none.
- **Return value:** `ExportOptions`, never `null` — a pre-built constant (`footerLines=List.of()`,
  `sumColumns=List.of()`, `parallel=false`), NOT a fresh instance per call (verified: `NONE` is
  a `private static final ExportOptions` field, returned directly).
- **Exceptions actually thrown:** none.

## `public ExportOptions withFooter(String... lines)`

Returns a copy with `footerLines` replaced by `lines`.

- **Parameters:** `lines` (`String...`) — **null allowed for the array itself: no** — verified:
  `Objects.requireNonNull(lines, "lines")`. **Null allowed for individual elements: no** —
  `List.of(lines)` (JDK) throws `NullPointerException` if any element is `null`; this happens
  before the returned `ExportOptions`'s own compact constructor even runs (though that
  constructor would reject `null` elements too, via its own `List.copyOf`).
- **Return value:** `ExportOptions`, never `null` — a new instance; `sumColumns` and `parallel`
  are carried over unchanged from `this`.
- **Exceptions actually thrown:** `NullPointerException` if `lines` is `null` (message
  `"lines"`) or if any element of `lines` is `null` (message from `List.of`, not custom).

## `public ExportOptions withSumColumns(String... columns)`

Returns a copy with `sumColumns` replaced by `columns`.

- **Parameters:** `columns` (`String...`) — same null-handling as `lines` in `withFooter`:
  **null allowed: no** for the array (`Objects.requireNonNull(columns, "columns")`) and **no**
  for individual elements (`List.of(columns)` rejects `null` elements).
- **Return value:** `ExportOptions`, never `null` — a new instance; `footerLines` and
  `parallel` carried over unchanged.
- **Exceptions actually thrown:** `NullPointerException` if `columns` is `null` (message
  `"columns"`) or contains a `null` element (message from `List.of`).
- **Note:** this method does not verify that the named columns actually exist on the grid /
  sheet at call time — that validation happens later, inside xlsxBuilder's `XlsxBuilder`
  (`"Unknown sum column: " + name` as an `IllegalArgumentException`, thrown from
  `renderInto(...)` during `GridExcelExporter.export(...)`, not from this method).

## `public ExportOptions withParallel(boolean enabled)`

Returns a copy with `parallel` set to `enabled`.

- **Parameters:** `enabled` (`boolean`) — primitive, cannot be `null`.
- **Return value:** `ExportOptions`, never `null` — a new instance; `footerLines` and
  `sumColumns` carried over unchanged.
- **Exceptions actually thrown:** none.

# Citations

[1] `library/src/main/java/de/makno/vaadinexcelexport/ExportOptions.java`
[2] Higher-level narrative: [GridExcelExporter](/components/grid-excel-exporter.md) — "ExportOptions (immutable)" section
