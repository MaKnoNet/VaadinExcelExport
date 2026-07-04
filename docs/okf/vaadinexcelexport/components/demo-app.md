---
type: Demo Application
title: Demo-App (MainView, TestDataDatabase, Benchmark)
description: Spring-Boot-Demo mit lazy/seitenweisem Grid auf einer H2-Datenbank; vergleicht GridExcelExporter gegen den Flowingcode-Exporter und misst Laufzeit + Allokation.
resource: app/src/main/java/de/makno/vaadinexcelexport/app/MainView.java
tags: [demo, vaadin, spring-boot, benchmark]
timestamp: '2026-07-05T09:15:00+02:00'
---

# Überblick

`Application` ist der `@SpringBootApplication`-Einstiegspunkt; `@Push` aktiviert
Server-Push, damit der Benchmark im Hintergrund-Thread Fortschritt/Ergebnisse an den
Browser zurückspielt.

`MainView` (Hauptroute) baut ein `Grid` über `SampleGrid`
([ExcelMeta](/components/excel-meta.md)-annotierte Spalten inkl. einer `FORMULA`-Spalte
und eines `HYPERLINK` via [ExcelFormulas](/components/excel-formulas.md)) und bindet es
lazy/seitenweise an `TestDataDatabase` an (**file-basierte H2-Datenbank, nicht im
JVM-Heap**). `SampleDataProvider` fordert nur die sichtbaren Seiten an
(Offset/Limit + `ORDER BY` aus der Grid-Sortierung); `SampleSearch` übersetzt den
Freitext-Suchbegriff in eine parametrisierte, SQL-injektionssichere `WHERE`-Klausel.
`SampleRowMapper` (implementiert `ResultSetRowMapper<SampleRow>` aus xlsxBuilder) bildet
`ResultSet`-Zeilen auf den `SampleRow`-Record ab — von der Grid-Anzeige **und** vom
out-of-core-Export gemeinsam genutzt. `SampleData` erzeugt die Testdaten deterministisch
und rein funktional.

# Benchmark-Architektur

`ExportRunner` führt zwei Export-Wege vermessen aus — beide lesen aus derselben
Datenbank: den [GridExcelExporter](/components/grid-excel-exporter.md) (out-of-core,
`DataProviders.ofResultSet` über `TestDataDatabase#openStream`) gegen den lazy/seitenweise
lesenden Flowingcode-Exporter. `ExportMeasurement` misst Laufzeit und — sofern auf einer
HotSpot-JVM verfügbar — die vom Thread allozierten Bytes über
`ThreadMXBean#getThreadAllocatedBytes` (monoton, GC-unabhängig, anders als eine von
paralleler GC verfälschte Heap-Differenz). `ExportMetrics` ist der resultierende
Record (Engine, Zeilenzahl, Dauer, Allokation, Ausgabegröße). Siehe auch
[Out-of-core export](/architecture/out-of-core-export.md) für den zugrunde liegenden
28x-Benchmark aus dem README.

# Schema

| Klasse | Aufgabe |
|---|---|
| `Application` | Spring-Boot-Start mit `@Push` |
| `MainView` | Hauptroute: Grid + Steuerung des Benchmarks |
| `SampleGrid` | Grid-Aufbau mit `ExcelMeta`-annotierten Spalten |
| `TestDataDatabase` | file-basierte H2-Testdatenbank (`AutoCloseable`) |
| `SampleDataProvider` | lazy/seitenweiser `DataProvider` fürs Grid |
| `SampleSearch` | Freitext → parametrisierte `WHERE`-Klausel |
| `SampleRowMapper` | `ResultSet` → `SampleRow` (Grid **und** Export gemeinsam) |
| `SampleData` / `SampleRow` | deterministische Testdatengenerierung / Datensatz-Record |
| `ExportRunner` / `ExportMeasurement` / `ExportMetrics` | Benchmark-Orchestrierung, Messung, Ergebnis-Record |

# Citations

[1] [README - Demo app, Out-of-core / benchmark](https://github.com/MaKnoNet/VaadinExcelExport/blob/master/README.md)
