---
type: API Reference
title: ExcelFormulas
description: Verified constructor/method-level reference for ExcelFormulas, the safe Excel HYPERLINK formula builder.
resource: library/src/main/java/de/makno/vaadinexcelexport/ExcelFormulas.java
tags: [api-reference, excel, security, formula-injection]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Overview

`ExcelFormulas` is a stateless utility class (private constructor, only static members) for
building safe Excel formula text. See [ExcelFormulas](/components/excel-formulas.md) for the
security rationale (formula injection). This file adds the exhaustive, verified
parameter/exception contract.

**Thread-safety:** stateless, safe for concurrent use (verified: no fields other than
`private static final String` scheme constants).

# Inheritance Hierarchy

**Forward (own declaration):** verified declaration line —

```java
public final class ExcelFormulas {
```

No `extends` clause (implicit `java.lang.Object` only) and no `implements` clause.

**Backward (project-internal subtypes):** none. Verified by grep across
`library/src/main/java/de/makno/vaadinexcelexport/` and `library/src/test/java/...` for
`extends ExcelFormulas` / `implements ExcelFormulas` — no matches. `final` also makes this
impossible outside reflection tricks.

**Summary:** keine Ober-/Unterklassen; `final class`, erweitert nur `java.lang.Object`,
implementiert keine Interfaces — a private no-arg constructor also prevents instantiation
entirely (pure static-method utility class).

# Constructors

| Signature | Parameters | Null allowed | On invalid input |
|---|---|---|---|
| `private ExcelFormulas()` | none | n/a | n/a — utility class, never instantiated |

# Methods

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

[1] `library/src/main/java/de/makno/vaadinexcelexport/ExcelFormulas.java`
[2] Higher-level narrative and security rationale: [ExcelFormulas](/components/excel-formulas.md)
