---
type: API Reference
title: ExcelFormulas.sanitizeUrl(...)
description: Method sanitizeUrl of ExcelFormulas - see signature(s) below.
resource: library/src/main/java/de/makno/vaadinexcelexport/ExcelFormulas.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `private static String sanitizeUrl(String url)`


Restricts `url` to `http://`, `https://`, or `mailto:` (case-insensitive scheme check via
`Locale.ROOT`).

- **Parameters:** `url` (`String`) — **null allowed: yes**, verified: explicit `if (url ==
  null) return "";` guard.
- **Return value:** `String`, never `null`. Returns `url` unchanged if it starts with one of
  the three allowed schemes (case-insensitive); returns `""` (empty string) for `null` or any
  other scheme (e.g. `file:`, `smb:`, relative paths, or a bare string with no scheme at all).
- **Exceptions actually thrown:** none.

# Citations

[1] [ExcelFormulas (Overview)](./excel-formulas.md)
