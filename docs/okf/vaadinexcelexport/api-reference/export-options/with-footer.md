---
type: API Reference
title: ExportOptions.withFooter(...)
description: Method withFooter of ExportOptions - see signature(s) below.
resource: library/src/main/java/de/makno/vaadinexcelexport/ExportOptions.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

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

# Citations

[1] [ExportOptions (Overview)](./export-options.md)
