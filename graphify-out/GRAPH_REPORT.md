# Graph Report - VaadinExcelExport  (2026-07-04)

## Corpus Check
- 43 files · ~18,456 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 384 nodes · 715 edges · 36 communities (30 shown, 6 thin omitted)
- Extraction: 87% EXTRACTED · 13% INFERRED · 0% AMBIGUOUS · INFERRED: 92 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `11ba617b`
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

## God Nodes (most connected - your core abstractions)
1. `MainView` - 21 edges
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
- None detected - all connections are within the same source files.

## Import Cycles
- None detected.

## Communities (36 total, 6 thin omitted)

### Community 0 - "Community 0"
Cohesion: 0.08
Nodes (20): SampleData, SampleRow, List, Object, Override, Path, ResultSet, SampleRow (+12 more)

### Community 1 - "Community 1"
Cohesion: 0.11
Nodes (14): Anchor, EngineTask, MainView, List, Override, Result, String, VaadinSession (+6 more)

### Community 2 - "Community 2"
Cohesion: 0.14
Nodes (16): allocatedText(), durationText(), outputText(), String, Builder, ExportMetrics, Function, Column (+8 more)

### Community 3 - "Community 3"
Cohesion: 0.20
Nodes (14): ColumnGroup, Comparator, DataProvider, Column, ExportOptions, Grid, List, OutputStream (+6 more)

### Community 4 - "Community 4"
Cohesion: 0.20
Nodes (9): Grid, List, String, Test, Person, Row, Sheet, V (+1 more)

### Community 5 - "Community 5"
Cohesion: 0.19
Nodes (13): avgMillis(), ExcelExporterBenchmarkTest, ExportEngine, medianMillis(), rowsPerSecond(), Grid, OutputStream, SampleRow (+5 more)

### Community 6 - "Community 6"
Cohesion: 0.21
Nodes (11): SampleGrid, ColumnType, Grid, List, SampleRow, String, ValueProvider, GridSortOrder (+3 more)

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
Cohesion: 0.24
Nodes (6): isEmpty(), SampleSearch, SampleSearchTest, String, Test, WhereClause

### Community 11 - "Community 11"
Cohesion: 0.28
Nodes (12): bundle_relative_link(), check_conformance(), main(), Warn-only OKF-Checks: type-Pflichtfeld, keine relativen ../-Links., Liest title/description/type aus dem YAML-Frontmatter (naiver Zeilen-Parser)., Bundle-root-absoluter Link gemaess OKF-Spec (Abschnitt 5.1)., Erzeugt den index.md-Inhalt fuer ein Verzeichnis (deterministisch sortiert)., Schreibt nur bei Aenderung (haelt Hook-Ausgabe und git status ruhig). (+4 more)

### Community 12 - "Community 12"
Cohesion: 0.35
Nodes (10): band(), benchmark_table(), build(), callout(), feature_table(), kv_table(), mark(), value: True -> gruener Haken, False -> rotes Kreuz, sonst Text (z. B. '~' oder ' (+2 more)

### Community 13 - "Community 13"
Cohesion: 0.36
Nodes (7): ColumnPathRenderer, Column, Object, SuppressWarnings, T, LitRenderer, ColumnValueExtractor

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
Cohesion: 0.24
Nodes (7): SampleDataProvider, Grid, SampleRow, String, TestDataDatabase, ConfigurableFilterDataProvider, Void

### Community 25 - "Community 25"
Cohesion: 0.40
Nodes (4): Citations, Guardrails, Overview, The xlsxBuilder dependency

### Community 26 - "Community 26"
Cohesion: 0.40
Nodes (4): Benchmark, Citations, Examples, Overview

### Community 27 - "Community 27"
Cohesion: 0.40
Nodes (4): End-of-Session-Routine (Pflicht bei Code-/Architekturänderungen), Graph-First-Regel, Knowledge Base (graphify + OKF), Projekt-Konventionen - VaadinExcelExport

### Community 28 - "Community 28"
Cohesion: 0.40
Nodes (4): Citations, Examples, Overview, Schema

### Community 29 - "Community 29"
Cohesion: 0.40
Nodes (4): Citations, Examples, Overview, Schema

### Community 30 - "Community 30"
Cohesion: 0.50
Nodes (3): Citations, Commands, Rules

## Knowledge Gaps
- **76 isolated node(s):** `version`, `configurations`, `String`, `String`, `Result` (+71 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **6 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainView` connect `Community 1` to `Community 2`?**
  _High betweenness centrality (0.056) - this node is a cross-community bridge._
- **Why does `Stream` connect `Community 3` to `Community 0`, `Community 4`, `Community 5`, `Community 6`, `Community 24`?**
  _High betweenness centrality (0.054) - this node is a cross-community bridge._
- **What connects `version`, `configurations`, `String` to the rest of the system?**
  _82 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Community 0` be split into smaller, more focused modules?**
  _Cohesion score 0.08194905869324474 - nodes in this community are weakly interconnected._
- **Should `Community 1` be split into smaller, more focused modules?**
  _Cohesion score 0.1140819964349376 - nodes in this community are weakly interconnected._
- **Should `Community 2` be split into smaller, more focused modules?**
  _Cohesion score 0.13548387096774195 - nodes in this community are weakly interconnected._
- **Should `Community 8` be split into smaller, more focused modules?**
  _Cohesion score 0.125 - nodes in this community are weakly interconnected._