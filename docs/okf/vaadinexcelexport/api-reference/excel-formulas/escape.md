---
type: API Reference
title: ExcelFormulas.escape(...)
description: Method escape of ExcelFormulas - see signature(s) below.
resource: library/src/main/java/de/makno/vaadinexcelexport/ExcelFormulas.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `private static String escape(String value)`


Doubles embedded double quotes so `value` remains a valid Excel string literal.

- **Parameters:** `value` (`String`) — **null allowed: no**, not checked; called internally
  only with non-null arguments (the sanitized url, always a `String`, and the
  already-null-checked `displayName`). Passing `null` directly would throw
  `NullPointerException` from `value.replace(...)`, but this path is unreachable from the
  public API given current call sites.
- **Return value:** `String`, never `null` given a non-null input; `"` replaced by `""`.
- **Exceptions actually thrown:** none for the arguments it is actually called with; would
  throw `NullPointerException` only if called with `null` (not reachable via the public API).

# Citations

[1] [ExcelFormulas (Overview)](./excel-formulas.md)
