# Konzepte

* [Demo-App (MainView, TestDataDatabase, Benchmark)](/components/demo-app.md) - Spring-Boot-Demo mit lazy/seitenweisem Grid auf einer H2-Datenbank; vergleicht GridExcelExporter gegen den Flowingcode-Exporter und misst Laufzeit + Allokation.
* [ExcelFormulas](/components/excel-formulas.md) - Helper for safely building Excel formula text (HYPERLINK) for FORMULA columns — escapes embedded quotes and restricts the URL scheme, preventing formula injection.
* [ExcelMeta](/components/excel-meta.md) - Per-column export metadata attached directly to a Vaadin Grid.Column (via ComponentUtil) — type, value provider, Excel format, converter, header group. The grid stays the single source of truth.
* [GridExcelExporter](/components/grid-excel-exporter.md) - Fluent exporter that writes a Vaadin Grid to .xlsx via xlsxBuilder — respects sort order and active filter, supports in-memory and out-of-core (ResultSet) sources.
