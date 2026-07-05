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

# Fields

`ExportOptions` is a Java `record` — its fields are the record components, not separately
declared fields. Fields = record components, see [Constructor](./constructor.md) for the full
null-handling/validation contract of each component.

| Component | Type | Meaning |
|---|---|---|
| `footerLines` | `List<String>` | Footer lines rendered under the data, merged across the full sheet width. Defensively copied to an unmodifiable list via `List.copyOf` in the compact constructor. |
| `sumColumns` | `List<String>` | Column names to sum (activates xlsxBuilder's summary row). Defensively copied the same way. |
| `parallel` | `boolean` | Enables xlsxBuilder's pipeline parallelism (producer/consumer decoupling). |

# Thread-Safety

Immutable by construction — verified: the compact constructor defensively copies both list
components via `List.copyOf(...)`, so a caller's original mutable list can never be observed or
mutated through an `ExportOptions` instance afterwards. Safe to share and reuse across threads
and requests without synchronization. The shared `NONE` singleton (backing `none()`) is
likewise safe for concurrent access since it is fully immutable.

# Serialization

Not `Serializable` — `ExportOptions` implements no serialization interface (verified against
the record declaration, no `implements` clause). Being a `record` does not imply
`Serializable` — the JDK only auto-generates that support if the record explicitly declares
`implements Serializable`, which this one does not. No serialization contract.

# equals/hashCode/toString

Component-based (JDK-generated record behavior, verified: no explicit override in the source):
`equals`/`hashCode` compare/hash all three components (`footerLines`, `sumColumns`, `parallel`)
structurally — two `ExportOptions` instances with equal component values are `equals()`-equal
regardless of identity. `toString()` returns the JDK-generated
`ExportOptions[footerLines=..., sumColumns=..., parallel=...]` representation.

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

- [see constructor.md](./constructor.md)

# Methods

- [`public static ExportOptions none()`](./none.md) — returns the shared "no extra options" singleton instance.
- [`public ExportOptions withFooter(String... lines)`](./with-footer.md) — returns a copy with `footerLines` replaced.
- [`public ExportOptions withSumColumns(String... columns)`](./with-sum-columns.md) — returns a copy with `sumColumns` replaced.
- [`public ExportOptions withParallel(boolean enabled)`](./with-parallel.md) — returns a copy with `parallel` set.

# Citations

[1] `library/src/main/java/de/makno/vaadinexcelexport/ExportOptions.java`
[2] Higher-level narrative: [GridExcelExporter](/components/grid-excel-exporter.md) — "ExportOptions (immutable)" section
