# Update Log

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
