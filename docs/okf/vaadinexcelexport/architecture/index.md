# Konzepte

* [Module structure (library / app) and the xlsxBuilder dependency](/architecture/module-structure.md) - Two Gradle subprojects keep the published bridge library free of demo code; xlsxBuilder is consumed as a binary dependency from mavenLocal.
* [Out-of-core export (constant memory)](/architecture/out-of-core-export.md) - Rows are streamed straight from a JDBC ResultSet through xlsxBuilder's SXSSF pipeline — memory stays constant regardless of row count; ~28x faster than the Flowingcode exporter in the bundled benchmark.
