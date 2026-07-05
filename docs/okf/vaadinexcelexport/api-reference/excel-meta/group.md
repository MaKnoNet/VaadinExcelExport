---
type: API Reference
title: ExcelMeta.group(...)
description: Method group of ExcelMeta - see signature(s) below.
resource: library/src/main/java/de/makno/vaadinexcelexport/ExcelMeta.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `public static void group(Column<?> column, String label)`


Assigns a group label to `column`; contiguous columns sharing the same label are merged into
one joined-header cell on export.

- **Parameters:**
  - `column` (`Column<?>`) — **null allowed: no** in practice; not explicitly checked, but
    `ComponentUtil.setData` dereferences it.
  - `label` (`String`) — **null allowed: yes** — verified: no `Objects.requireNonNull` call;
    `ComponentUtil.setData(column, KEY_GROUP, label)` stores whatever is passed, including
    `null`. Passing `null` effectively un-sets any previously assigned group (subsequent
    `getGroup` calls return `null` again), since it is indistinguishable from "never grouped".
- **Return value:** `void`.
- **Exceptions actually thrown:** none from this method's own logic; `NullPointerException`
  only if `column` itself is `null` (from Vaadin's `ComponentUtil`).

**Note:** the class Javadoc for `group` does not mention `null`-handling for `label` at all;
verified behavior (silently accepted, acts as "clear group") is undocumented but not
contradicted — no discrepancy, just an omission worth stating precisely here.

# Citations

[1] [ExcelMeta (Overview)](./excel-meta.md)
