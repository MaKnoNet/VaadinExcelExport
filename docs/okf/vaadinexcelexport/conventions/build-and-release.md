---
type: Convention
title: Build, test and release
description: Gradle commands, the mavenLocal prerequisite for xlsxBuilder, and the release checklist including knowledge-base refresh before tagging.
resource: build.gradle
tags: [convention, gradle, build, release]
timestamp: '2026-07-04T17:30:00+02:00'
---

# Commands

| Purpose | Command |
|---|---|
| Provide the xlsxBuilder dependency (once) | `./gradlew -p libs/xlsxBuilder publishToMavenLocal` (path per current layout) |
| Build (both modules + tests) | `./gradlew build` |
| Run the demo app | `./gradlew :app:bootRun` |
| Activate team git hooks | `./gradlew installGitHooks` (runs automatically with the first build) |

# Rules

- **Java 21** (Gradle toolchain), **Vaadin 24**.
- The published artifact is `:library` only; `:app` has no `maven-publish`.
- **Release:** refresh the knowledge base before tagging (update OKF concepts +
  `log.md`, run `graphify update .`), commit, then tag — every release tag carries its
  matching knowledge state.

# Citations

[1] [README - Requirements, Build & run](https://github.com/MaKnoNet/VaadinExcelExport/blob/master/README.md)
