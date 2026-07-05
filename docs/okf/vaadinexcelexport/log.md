# Update Log

## 2026-07-07

* **Creation**: new `api-reference/` category (5 files, one per class in
  `library/src/main/java/de/makno/vaadinexcelexport/`) — exhaustive, code-verified
  constructor/method reference (parameters, null-handling, return semantics,
  actually-thrown exceptions), complementing the narrative `components/` docs.
  Cross-checked against the real implementation and the xlsxBuilder dependency source
  it delegates to, rather than trusting Javadoc comments blindly; found 4
  discrepancies/omissions — e.g. `ColumnValueExtractor.extract`'s Javadoc documents
  only one `IllegalStateException` trigger while the code has a second, distinct one;
  `ExcelFormulas.hyperlink`'s Javadoc doesn't state that `url` may be `null`;
  `GridExcelExporter.from(...)` throws the identical exception message for two
  different root causes (no exportable columns vs. non-matching `columnKeyOrder`).
  Full details in the respective `api-reference/*.md` files.
* **Update**: [developer guide](/conventions/okf-entwicklerdoku.md) and `AGENTS.md`
  extended with the `api-reference/` convention (purpose, distinction from
  `components/`, mandatory code verification).

## 2026-07-06 (2)

* **Update**: project conventions migrated from `CLAUDE.md` to `AGENTS.md` (vendor-neutral
  standard, keeps instructions portable across different AI coding tools). `CLAUDE.md` is
  now just a thin `@AGENTS.md` import. Affects the "single source of truth" references in
  this [developer guide](/conventions/okf-entwicklerdoku.md).

## 2026-07-06

* **Update**: `CLAUDE.md` routine tightened from "end-of-session" to "pre-commit" —
  affected OKF concepts are now updated before every single commit with code/
  architecture changes instead of batched at session end;
  [developer guide](/conventions/okf-entwicklerdoku.md) adjusted accordingly.
* **Creation**: new concept
  [Entwicklerdoku – OKF-Wissensdatenbank pflegen](/conventions/okf-entwicklerdoku.md) —
  bundle structure, frontmatter convention, step-by-step "add a new concept",
  automation/hybrid strategy, known pitfalls.

## 2026-07-04

* **Initialization**: OKF bundle created — components
  ([GridExcelExporter](/components/grid-excel-exporter.md),
  [ExcelMeta](/components/excel-meta.md)), architecture
  ([module structure](/architecture/module-structure.md),
  [out-of-core export](/architecture/out-of-core-export.md)) and conventions
  ([build & release](/conventions/build-and-release.md)) derived from README.md.
* **Creation**: graphify knowledge graph built for the first time; kept current
  automatically by the pre-commit hook from now on.

## 2026-07-05

* **Update**: semantic gap-fill against graphify-out/GRAPH_REPORT.md —
  [grid-excel-exporter.md](/components/grid-excel-exporter.md) extended with
  `ColumnValueExtractor` (fallback chain) and `ExportOptions` (immutable extras
  record); [excel-meta.md](/components/excel-meta.md) cross-references the new
  concept below for `HYPERLINK` cells.
* **Creation**: new concept [excel-formulas.md](/components/excel-formulas.md) —
  `ExcelFormulas`, security-relevant (formula-injection prevention).
* **Creation**: new concept [demo-app.md](/components/demo-app.md) — `MainView`,
  `TestDataDatabase`, `SampleDataProvider`/`SampleSearch`/`SampleRowMapper`, and the
  benchmark architecture (`ExportRunner`/`ExportMeasurement`/`ExportMetrics`).
