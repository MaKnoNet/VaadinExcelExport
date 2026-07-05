# Graph Report - VaadinExcelExport  (2026-07-05)

## Corpus Check
- 92 files · ~31,300 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 601 nodes · 906 edges · 84 communities (44 shown, 40 thin omitted)
- Extraction: 89% EXTRACTED · 11% INFERRED · 0% AMBIGUOUS · INFERRED: 101 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `b8f69337`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- [[_COMMUNITY_Community 0|Community 0]]
- [[_COMMUNITY_Community 1|Community 1]]
- [[_COMMUNITY_Community 2|Community 2]]
- [[_COMMUNITY_Community 3|Community 3]]
- [[_COMMUNITY_Community 4|Community 4]]
- [[_COMMUNITY_Community 5|Community 5]]
- [[_COMMUNITY_Community 6|Community 6]]
- [[_COMMUNITY_Community 7|Community 7]]
- [[_COMMUNITY_Community 8|Community 8]]
- [[_COMMUNITY_Community 9|Community 9]]
- [[_COMMUNITY_Community 10|Community 10]]
- [[_COMMUNITY_Community 11|Community 11]]
- [[_COMMUNITY_Community 12|Community 12]]
- [[_COMMUNITY_Community 13|Community 13]]
- [[_COMMUNITY_Community 14|Community 14]]
- [[_COMMUNITY_Community 15|Community 15]]
- [[_COMMUNITY_Community 16|Community 16]]
- [[_COMMUNITY_Community 17|Community 17]]
- [[_COMMUNITY_Community 18|Community 18]]
- [[_COMMUNITY_Community 24|Community 24]]
- [[_COMMUNITY_Community 25|Community 25]]
- [[_COMMUNITY_Community 26|Community 26]]
- [[_COMMUNITY_Community 27|Community 27]]
- [[_COMMUNITY_Community 28|Community 28]]
- [[_COMMUNITY_Community 29|Community 29]]
- [[_COMMUNITY_Community 30|Community 30]]
- [[_COMMUNITY_Community 31|Community 31]]
- [[_COMMUNITY_Community 32|Community 32]]
- [[_COMMUNITY_Community 33|Community 33]]
- [[_COMMUNITY_Community 34|Community 34]]
- [[_COMMUNITY_Community 35|Community 35]]
- [[_COMMUNITY_Community 36|Community 36]]
- [[_COMMUNITY_Community 37|Community 37]]
- [[_COMMUNITY_Community 38|Community 38]]
- [[_COMMUNITY_Community 39|Community 39]]
- [[_COMMUNITY_Community 40|Community 40]]
- [[_COMMUNITY_Community 41|Community 41]]
- [[_COMMUNITY_Community 42|Community 42]]
- [[_COMMUNITY_Community 43|Community 43]]
- [[_COMMUNITY_Community 44|Community 44]]
- [[_COMMUNITY_Community 45|Community 45]]
- [[_COMMUNITY_Community 46|Community 46]]
- [[_COMMUNITY_Community 47|Community 47]]
- [[_COMMUNITY_Community 48|Community 48]]
- [[_COMMUNITY_Community 49|Community 49]]
- [[_COMMUNITY_Community 50|Community 50]]
- [[_COMMUNITY_Community 51|Community 51]]
- [[_COMMUNITY_Community 52|Community 52]]
- [[_COMMUNITY_Community 53|Community 53]]
- [[_COMMUNITY_Community 54|Community 54]]
- [[_COMMUNITY_Community 55|Community 55]]
- [[_COMMUNITY_Community 56|Community 56]]
- [[_COMMUNITY_Community 57|Community 57]]
- [[_COMMUNITY_Community 58|Community 58]]
- [[_COMMUNITY_Community 59|Community 59]]
- [[_COMMUNITY_Community 60|Community 60]]
- [[_COMMUNITY_Community 61|Community 61]]
- [[_COMMUNITY_Community 62|Community 62]]
- [[_COMMUNITY_Community 63|Community 63]]
- [[_COMMUNITY_Community 64|Community 64]]
- [[_COMMUNITY_Community 65|Community 65]]
- [[_COMMUNITY_Community 66|Community 66]]
- [[_COMMUNITY_Community 67|Community 67]]
- [[_COMMUNITY_Community 68|Community 68]]
- [[_COMMUNITY_Community 69|Community 69]]
- [[_COMMUNITY_Community 70|Community 70]]
- [[_COMMUNITY_Community 71|Community 71]]
- [[_COMMUNITY_Community 72|Community 72]]
- [[_COMMUNITY_Community 73|Community 73]]
- [[_COMMUNITY_Community 74|Community 74]]
- [[_COMMUNITY_Community 75|Community 75]]
- [[_COMMUNITY_Community 76|Community 76]]
- [[_COMMUNITY_Community 77|Community 77]]
- [[_COMMUNITY_Community 78|Community 78]]
- [[_COMMUNITY_Community 79|Community 79]]
- [[_COMMUNITY_Community 80|Community 80]]
- [[_COMMUNITY_Community 81|Community 81]]
- [[_COMMUNITY_Community 82|Community 82]]
- [[_COMMUNITY_Community 83|Community 83]]

## God Nodes (most connected - your core abstractions)
1. `MainView` - 22 edges
2. `GridExcelExporterTest` - 18 edges
3. `SampleGrid` - 13 edges
4. `TestDataDatabase` - 12 edges
5. `Test` - 12 edges
6. `VaadinExcelExport` - 12 edges
7. `ExcelExporterBenchmarkTest` - 11 edges
8. `ExcelMeta` - 9 edges
9. `Column` - 9 edges
10. `Stream` - 9 edges

## Surprising Connections (you probably didn't know these)
- `TestDataDatabase` --implements--> `AutoCloseable`  [EXTRACTED]
  app/src/main/java/de/makno/vaadinexcelexport/app/TestDataDatabase.java →   _Bridges community 0 → community 3_

## Import Cycles
- None detected.

## Communities (84 total, 40 thin omitted)

### Community 0 - "Community 0"
Cohesion: 0.09
Nodes (16): SampleData, SampleRow, List, Object, Override, Path, SampleRow, String (+8 more)

### Community 1 - "Community 1"
Cohesion: 0.09
Nodes (20): Anchor, allocatedText(), durationText(), outputText(), EngineTask, MainView, String, List (+12 more)

### Community 2 - "Community 2"
Cohesion: 0.18
Nodes (11): Builder, Function, Column, ColumnType, Object, String, SuppressWarnings, T (+3 more)

### Community 3 - "Community 3"
Cohesion: 0.13
Nodes (19): isEmpty(), ResultSet, StreamingResult, AutoCloseable, ColumnGroup, Comparator, DataProvider, Column (+11 more)

### Community 4 - "Community 4"
Cohesion: 0.20
Nodes (9): Grid, List, String, Test, Person, Row, Sheet, V (+1 more)

### Community 5 - "Community 5"
Cohesion: 0.19
Nodes (13): avgMillis(), ExcelExporterBenchmarkTest, ExportEngine, medianMillis(), rowsPerSecond(), Grid, OutputStream, SampleRow (+5 more)

### Community 6 - "Community 6"
Cohesion: 0.22
Nodes (10): SampleGrid, ColumnType, Grid, List, SampleRow, String, ValueProvider, Query (+2 more)

### Community 7 - "Community 7"
Cohesion: 0.18
Nodes (13): ExportRunner, Grid, List, Result, SampleRow, String, TestDataDatabase, VaadinSession (+5 more)

### Community 8 - "Community 8"
Cohesion: 0.12
Nodes (15): Build & run, Concepts, Concurrency / server operation, Demo app, Eclipse, `ExcelMeta` – per-column export metadata, `GridExcelExporter<T>`, Highlights (+7 more)

### Community 9 - "Community 9"
Cohesion: 0.21
Nodes (7): ExcelExport, ExportMeasurement, ThreadMeter, OutputStream, Result, String, ThreadMXBean

### Community 10 - "Community 10"
Cohesion: 0.26
Nodes (5): SampleSearch, SampleSearchTest, String, Test, WhereClause

### Community 11 - "Community 11"
Cohesion: 0.28
Nodes (12): bundle_relative_link(), check_conformance(), main(), Warn-only OKF-Checks: type-Pflichtfeld, keine relativen ../-Links., Liest title/description/type aus dem YAML-Frontmatter (naiver Zeilen-Parser)., Bundle-root-absoluter Link gemaess OKF-Spec (Abschnitt 5.1)., Erzeugt den index.md-Inhalt fuer ein Verzeichnis (deterministisch sortiert)., Schreibt nur bei Aenderung (haelt Hook-Ausgabe und git status ruhig). (+4 more)

### Community 12 - "Community 12"
Cohesion: 0.35
Nodes (10): band(), benchmark_table(), build(), callout(), feature_table(), kv_table(), mark(), value: True -> gruener Haken, False -> rotes Kreuz, sonst Text (z. B. '~' oder ' (+2 more)

### Community 13 - "Community 13"
Cohesion: 0.26
Nodes (8): ColumnPathRenderer, Field, Column, Object, SuppressWarnings, T, LitRenderer, ColumnValueExtractor

### Community 14 - "Community 14"
Cohesion: 0.40
Nodes (4): SampleRowMapper, Override, ResultSet, SampleRow

### Community 15 - "Community 15"
Cohesion: 0.33
Nodes (5): 1. `gradle.properties`, 2. `app/build.gradle` – Plugin-Version, 3. `MainView.java` – Download über `DownloadHandler`, 4. Bauen & verifizieren, Upgrade auf Vaadin 24.10.6 + Spring Boot 3.5.3 (mit DownloadHandler)

### Community 16 - "Community 16"
Cohesion: 0.50
Nodes (3): Application, String, AppShellConfigurator

### Community 17 - "Community 17"
Cohesion: 0.40
Nodes (4): Aufruf, Pflege, Vergleichs-PDF-Generator, Voraussetzung

### Community 24 - "Community 24"
Cohesion: 0.25
Nodes (4): String, Test, ExcelFormulas, ExcelFormulasTest

### Community 25 - "Community 25"
Cohesion: 0.29
Nodes (6): Citations, ColumnValueExtractor (internal fallback chain), Examples, ExportOptions (immutable), Overview, Schema

### Community 26 - "Community 26"
Cohesion: 0.40
Nodes (4): Citations, Guardrails, Overview, The xlsxBuilder dependency

### Community 27 - "Community 27"
Cohesion: 0.40
Nodes (4): Benchmark, Citations, Examples, Overview

### Community 28 - "Community 28"
Cohesion: 0.33
Nodes (5): Claude Code – Projekt-Instruktionen, End-of-Session-Routine (Pflicht bei Code-/Architekturänderungen), Graph-First-Regel, Knowledge Base (graphify + OKF), Projekt-Konventionen - VaadinExcelExport

### Community 29 - "Community 29"
Cohesion: 0.40
Nodes (4): Benchmark-Architektur, Citations, Schema, Überblick

### Community 30 - "Community 30"
Cohesion: 0.40
Nodes (4): Citations, Examples, Overview, Schema

### Community 31 - "Community 31"
Cohesion: 0.40
Nodes (4): Citations, Examples, Overview, Schema

### Community 32 - "Community 32"
Cohesion: 0.50
Nodes (3): Citations, Commands, Rules

### Community 33 - "Community 33"
Cohesion: 0.22
Nodes (8): 2026-07-04, 2026-07-05, 2026-07-06, 2026-07-06 (2), 2026-07-07, 2026-07-07 (2), 2026-07-08, Update Log

### Community 38 - "Community 38"
Cohesion: 0.17
Nodes (11): api-reference/ vs. components/, Automatisierung, Bekannte Stolpersteine, Bundle-Struktur dieses Projekts, Citations, Frontmatter-Konvention, Graphify-Zusammenspiel, Neues Konzept anlegen (+3 more)

### Community 39 - "Community 39"
Cohesion: 0.18
Nodes (10): Citations, Constructors, equals/hashCode/toString, Fields, Inheritance Hierarchy, Methods, Nested class: `ExcelMeta.Builder<T>`, Overview (+2 more)

### Community 40 - "Community 40"
Cohesion: 0.24
Nodes (7): SampleDataProvider, Grid, SampleRow, String, TestDataDatabase, ConfigurableFilterDataProvider, Void

### Community 41 - "Community 41"
Cohesion: 0.20
Nodes (9): Citations, Constructors, equals/hashCode/toString, Fields, Inheritance Hierarchy, Methods, Overview, Serialization (+1 more)

### Community 42 - "Community 42"
Cohesion: 0.20
Nodes (9): Citations, Constructors, equals/hashCode/toString, Fields, Inheritance Hierarchy, Methods, Overview, Serialization (+1 more)

### Community 43 - "Community 43"
Cohesion: 0.20
Nodes (9): Citations, Constructors, equals/hashCode/toString, Fields, Inheritance Hierarchy, Methods, Overview, Serialization (+1 more)

### Community 44 - "Community 44"
Cohesion: 0.20
Nodes (9): Citations, Constructors, equals/hashCode/toString, Fields, Inheritance Hierarchy, Methods, Overview, Serialization (+1 more)

### Community 45 - "Community 45"
Cohesion: 0.33
Nodes (5): Citations, `public void export(DataProvider<T, ?> dataProvider, Comparator<T> inMemorySort, OutputStream out) throws IOException`, `public void export(DataProvider<T, ?> dataProvider, OutputStream out) throws IOException`, `public void export(de.makno.xlsxbuilder.DataProvider<T> data, OutputStream out, ExportOptions options) throws IOException`, `public void export(de.makno.xlsxbuilder.DataProvider<T> data, OutputStream out) throws IOException`

### Community 46 - "Community 46"
Cohesion: 0.40
Nodes (4): Graph-First-Regel, Knowledge Base (graphify + OKF), Pre-Commit-Routine (Pflicht vor JEDEM Commit mit Code-/Architekturänderungen), Projekt-Konventionen - VaadinExcelExport

### Community 47 - "Community 47"
Cohesion: 0.50
Nodes (3): Citations, `public static <T> Builder<T> type(Column<T> column, ColumnType type)`, `public static <T> Builder<T> type(Column<T> column, ColumnType type, ValueProvider<T, ?> valueProvider)`

### Community 48 - "Community 48"
Cohesion: 0.50
Nodes (3): Citations, `public static <T> GridExcelExporter<T> from(String sheetName, Grid<T> grid)`, `public static <T> GridExcelExporter<T> from(String sheetName, Grid<T> grid, List<String> columnKeyOrder)`

## Knowledge Gaps
- **223 isolated node(s):** `version`, `configurations`, `String`, `String`, `Result` (+218 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **40 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Stream` connect `Community 3` to `Community 40`, `Community 4`, `Community 5`, `Community 6`?**
  _High betweenness centrality (0.024) - this node is a cross-community bridge._
- **Why does `TestDataDatabase` connect `Community 0` to `Community 3`?**
  _High betweenness centrality (0.014) - this node is a cross-community bridge._
- **What connects `version`, `configurations`, `String` to the rest of the system?**
  _229 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Community 0` be split into smaller, more focused modules?**
  _Cohesion score 0.09365079365079365 - nodes in this community are weakly interconnected._
- **Should `Community 1` be split into smaller, more focused modules?**
  _Cohesion score 0.09302325581395349 - nodes in this community are weakly interconnected._
- **Should `Community 3` be split into smaller, more focused modules?**
  _Cohesion score 0.1349206349206349 - nodes in this community are weakly interconnected._
- **Should `Community 8` be split into smaller, more focused modules?**
  _Cohesion score 0.125 - nodes in this community are weakly interconnected._