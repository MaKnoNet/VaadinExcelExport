---
type: Architecture Concept
title: Module structure (library / app) and the xlsxBuilder dependency
description: Two Gradle subprojects keep the published bridge library free of demo code; xlsxBuilder is consumed as a binary dependency from mavenLocal.
resource: settings.gradle
tags: [architecture, gradle, module, maven, xlsxbuilder]
timestamp: '2026-07-04T17:30:00+02:00'
---

# Overview

| Module | Package | What it is | Shipped to Maven? |
|---|---|---|---|
| `:library` | `de.makno.vaadinexcelexport` | the reusable bridge library ([GridExcelExporter](/components/grid-excel-exporter.md), [ExcelMeta](/components/excel-meta.md), ...) published as `de.makno.vaadinexcelexport:VaadinExcelExport` | **yes** |
| `:app` | `de.makno.vaadinexcelexport.app` | demo app (Spring Boot + Vaadin) comparing this exporter against the Flowingcode Grid Exporter | no |

# The xlsxBuilder dependency

The library builds on **xlsxBuilder** (`de.makno.xlsxbuilder:xlsxbuilder`, brings Apache POI
transitively), consumed as a **binary dependency from mavenLocal**. Historically it was a
Gradle composite build (`includeBuild ../xlsbuilder`); in the current layout it is published
locally first:

```bash
./gradlew -p libs/xlsxBuilder publishToMavenLocal
```

# Guardrails

- Demo-only dependencies (Flowingcode `grid-exporter-addon`, H2) never leak into `:library` —
  the app module has no `maven-publish`.
- Java 21 (Gradle toolchain), Vaadin 24.

# Citations

[1] [README - Two Gradle modules, Requirements](https://github.com/MaKnoNet/VaadinExcelExport/blob/master/README.md)
[2] [xlsxBuilder repository](https://github.com/MaKnoNet/xlsxBuilder)
