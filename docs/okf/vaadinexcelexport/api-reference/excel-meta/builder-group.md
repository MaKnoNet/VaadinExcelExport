---
type: API Reference
title: ExcelMeta.Builder.group(...)
description: Method group of the nested ExcelMeta.Builder<T> class - see signature below.
resource: library/src/main/java/de/makno/vaadinexcelexport/ExcelMeta.java
tags: [api-reference, method, nested-class]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `public Builder<T> group(String label)`

Delegates to `ExcelMeta.group(column, label)`.

- **Parameters:** `label` (`String`) — **null allowed: yes**, same semantics as
  `ExcelMeta.group`.
- **Return value:** `Builder<T>` — always `this`, never `null`.
- **Exceptions actually thrown:** none directly (delegate has none either, barring a `null`
  `column`, not reachable once the `Builder` was constructed with a valid one).

# Citations

[1] [ExcelMeta (Overview)](./excel-meta.md)
