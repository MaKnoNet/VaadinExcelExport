---
type: API Reference
title: ColumnValueExtractor.extractFromLitRenderer(...)
description: Method extractFromLitRenderer of ColumnValueExtractor - see signature(s) below.
resource: library/src/main/java/de/makno/vaadinexcelexport/ColumnValueExtractor.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `private static <T> Object extractFromLitRenderer(T item, LitRenderer<T> renderer)`


Extracts a value from a `LitRenderer`'s registered value providers.

- **Parameters:**
  - `item` (`T`) — row object, passed to the resolved provider. **Null allowed:** not checked;
    forwarded as-is.
  - `renderer` (`LitRenderer<T>`) — **null allowed: no**, `renderer.getValueProviders()` is
    called unconditionally; `null` throws `NullPointerException` from that call, not a
    documented/intentional contract.
- **Return value:** `Object`, **can be null**. Verified logic:
  - if there is exactly one value provider, that provider's result is returned (whatever it
    is, including `null`);
  - else if a provider keyed `"name"` exists (the hierarchical-column case), its result is
    returned;
  - else (zero or ambiguous multiple providers with no `"name"` key) returns `null` — this
    `null` is a **sentinel meaning "no usable provider found here"**, distinguished by `extract`
    from a legitimate `null` cell value only by the fact that `extract` then continues probing
    `ColumnPathRenderer` instead of accepting the `null`. This means: if a `LitRenderer` has
    exactly one provider and that provider's real return value is `null`, `extract` cannot tell
    the difference from "no provider found" and will still fall through to try
    `ColumnPathRenderer` next (which likely does not apply, since the column already had a
    `LitRenderer`), ultimately still yielding `null` back to the caller in that case — output
    is the same either way, but worth noting the ambiguity is unresolved in the implementation.
- **Exceptions actually thrown:** none directly; whatever the invoked provider throws
  propagates unmodified.

# Citations

[1] [ColumnValueExtractor (Overview)](./column-value-extractor.md)
