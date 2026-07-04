---
type: Library Component
title: ExcelFormulas
description: Helper for safely building Excel formula text (HYPERLINK) for FORMULA columns — escapes embedded quotes and restricts the URL scheme, preventing formula injection.
resource: library/src/main/java/de/makno/vaadinexcelexport/ExcelFormulas.java
tags: [component, excel, security, formula-injection]
timestamp: '2026-07-05T09:00:00+02:00'
---

# Overview

Formula cells are evaluated by Excel, so values embedded from external/user data into a
formula must be escaped — otherwise they can break out of the formula string
(**formula injection**). `ExcelFormulas` encapsulates correct escaping so callers don't
repeat the fragile string-concatenation pattern (`"HYPERLINK(\"" + url + "\",...)"`) that
produces invalid or injected formulas when quotes aren't escaped.

# Schema

| Method | Behavior |
|---|---|
| `hyperlink(url, displayName)` | builds `HYPERLINK("target","label")` (no leading `=`) with properly escaped string literals |

- Embedded quotes are doubled (`"` → `""`), so neither `url` nor `displayName` can break out
  of the formula string.
- The target URL is restricted to the `http`, `https` and `mailto` schemes; anything else
  (e.g. `file:`/`smb:`, which can trigger automatic network access such as SMB hash capture
  when the file is opened) is discarded (empty target).
- **Stateless and thread-safe** — safe for concurrent use.

# Examples

```java
String formula = ExcelFormulas.hyperlink("https://example.com/invoice/42", "Invoice #42");
// -> HYPERLINK("https://example.com/invoice/42","Invoice #42")
```

# Citations

[1] [README - ExcelMeta: FORMULA columns and HYPERLINK cells](https://github.com/MaKnoNet/VaadinExcelExport/blob/master/README.md)
