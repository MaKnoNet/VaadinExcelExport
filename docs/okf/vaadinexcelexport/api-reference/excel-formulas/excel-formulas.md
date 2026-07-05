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

# Fields

Verified against the source: three private constant scheme prefixes, no instance fields (the
class has a private constructor and is never instantiated).

| Field | Type | Meaning | Nullable |
|---|---|---|---|
| `SCHEME_HTTPS` | `private static final String` | Allowed URL scheme prefix `"https://"`, checked case-insensitively in `sanitizeUrl`. | no |
| `SCHEME_HTTP` | `private static final String` | Allowed URL scheme prefix `"http://"`. | no |
| `SCHEME_MAILTO` | `private static final String` | Allowed URL scheme prefix `"mailto:"`. | no |

# Thread-Safety

Stateless — the only fields are `private static final String` constants, safe for
unsynchronized concurrent use. All methods are `static`, operate purely on their arguments, and
hold no shared mutable state.

# Serialization

Not `Serializable` — `ExcelFormulas` implements no serialization interface (verified against
the class declaration `public final class ExcelFormulas`, no `implements` clause). No
serialization contract; not applicable for a non-instantiable static-utility class.

# equals/hashCode/toString

None of these methods are overridden (verified: no `equals`/`hashCode`/`toString` declaration
in the source) — not applicable in practice, since the class has a private constructor and is
never instantiated.

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

- [see constructor.md](./constructor.md)

# Methods

- [`public static String hyperlink(String url, String displayName)`](./hyperlink.md) — builds a safe `HYPERLINK("target","label")` formula fragment.
- [`private static String escape(String value)`](./escape.md) — doubles embedded double quotes so a value remains a valid Excel string literal.
- [`private static String sanitizeUrl(String url)`](./sanitize-url.md) — restricts a URL to `http://`/`https://`/`mailto:` schemes, else empty string.

# Citations

[1] `library/src/main/java/de/makno/vaadinexcelexport/ExcelFormulas.java`
[2] Higher-level narrative and security rationale: [ExcelFormulas](/components/excel-formulas.md)
