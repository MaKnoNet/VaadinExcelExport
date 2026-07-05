---
type: API Reference
title: ExportOptions.none(...)
description: Method none of ExportOptions - see signature(s) below.
resource: library/src/main/java/de/makno/vaadinexcelexport/ExportOptions.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `public static ExportOptions none()`


Returns the shared "no extra options" instance.

- **Parameters:** none.
- **Return value:** `ExportOptions`, never `null` — a pre-built constant (`footerLines=List.of()`,
  `sumColumns=List.of()`, `parallel=false`), NOT a fresh instance per call (verified: `NONE` is
  a `private static final ExportOptions` field, returned directly).
- **Exceptions actually thrown:** none.

# Citations

[1] [ExportOptions (Overview)](./export-options.md)
