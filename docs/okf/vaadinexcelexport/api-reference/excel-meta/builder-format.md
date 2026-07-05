---
type: API Reference
title: ExcelMeta.Builder.format(...)
description: Method format of the nested ExcelMeta.Builder<T> class - see signature below.
resource: library/src/main/java/de/makno/vaadinexcelexport/ExcelMeta.java
tags: [api-reference, method, nested-class]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `public Builder<T> format(String format)`

Sets the Excel number/date format code for the column, forwarded verbatim to xlsxBuilder's
`formatForType()`.

- **Parameters:** `format` (`String`) — **null allowed: yes** — verified: no null-check;
  `ComponentUtil.setData(column, KEY_FORMAT, format)` stores whatever is given, including
  `null` (equivalent to never having set a format).
- **Return value:** `Builder<T>` — always returns `this` (never `null`), enabling further
  chaining.
- **Exceptions actually thrown:** none directly.

# Citations

[1] [ExcelMeta (Overview)](./excel-meta.md)
