---
type: API Reference
title: ExcelFormulas.hyperlink(...)
description: Method hyperlink of ExcelFormulas - see signature(s) below.
resource: library/src/main/java/de/makno/vaadinexcelexport/ExcelFormulas.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `public static String hyperlink(String url, String displayName)`


Builds an Excel `HYPERLINK("target","label")` formula fragment (no leading `=`).

- **Parameters:**
  - `url` (`String`) — the target URL. **Null allowed: yes** — verified: `sanitizeUrl(url)`
    explicitly checks `if (url == null) return "";` before anything else, so a `null` url
    silently becomes an empty (discarded) target rather than throwing. The Javadoc's `@param
    url` line does not mention this — see discrepancy note below.
  - `displayName` (`String`) — the visible link text. **Null allowed: no** — verified:
    `Objects.requireNonNull(displayName, "displayName")` is the first statement in the method
    body, so a `null` `displayName` throws immediately.
- **Return value:** `String`, never `null`. Always of the shape
  `HYPERLINK("<escaped-target>","<escaped-displayName>")`; `<escaped-target>` is empty string
  when `url` was `null` or used a disallowed scheme.
- **Exceptions actually thrown:**
  - `NullPointerException` — if `displayName` is `null` (via `Objects.requireNonNull`, message
    `"displayName"`).
  - No exception for a `null` or disallowed-scheme `url` — it is silently sanitized to an empty
    target instead (verified in `sanitizeUrl`).

**Verified discrepancy vs. Javadoc:** the method Javadoc documents `@param url Ziel-URL; nur
http/https/mailto, sonst leeres Ziel` and `@param displayName ... (nicht {@code null})` — the
scheme-restriction behavior is accurately documented, but the Javadoc never states that `url`
itself may be `null` (it only implies non-null strings with the "wrong" scheme are emptied).
Verified in code: `null` is explicitly handled the same as an unrecognized scheme. This is not
a *wrong* claim, just an omission worth stating explicitly for API consumers who might
otherwise assume they must not pass `null` for `url`.

# Citations

[1] [ExcelFormulas (Overview)](./excel-formulas.md)
