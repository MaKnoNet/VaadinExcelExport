---
type: Architecture Concept
title: Out-of-core export (constant memory)
description: Rows are streamed straight from a JDBC ResultSet through xlsxBuilder's SXSSF pipeline — memory stays constant regardless of row count; ~28x faster than the Flowingcode exporter in the bundled benchmark.
resource: library/src/main/java/de/makno/vaadinexcelexport/GridExcelExporter.java
tags: [architecture, performance, streaming, jdbc, benchmark]
timestamp: '2026-07-04T17:30:00+02:00'
---

# Overview

The exporter consumes an xlsxBuilder `DataProvider`; with `DataProviders.ofResultSet(rs, mapper)`
the rows are streamed from the database through SXSSF — **constant memory, independent of the
row count**. The exporter closes the `ResultSet`; the caller keeps `Statement`/`Connection` in
a `try-with-resources` and should set a fetch size (e.g. `st.setFetchSize(1_000)`).

# Benchmark

In the bundled benchmark (25,000 rows, streamed from an H2 database) this exporter is
**~28x faster** than the Flowingcode exporter and uses constant memory instead of loading
every row into the heap. Full comparison:
`app/src/main/resources/META-INF/resources/excel-export-vergleich.pdf`.

# Examples

```java
try (Connection conn = dataSource.getConnection();
     Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
    st.setFetchSize(1_000);
    ResultSet rs = st.executeQuery("SELECT name, salary FROM employee ORDER BY salary DESC");
    GridExcelExporter.from("Employees", grid)
            .export(DataProviders.ofResultSet(rs, r -> new Employee(r.getString("name"), r.getBigDecimal("salary"))),
                    outputStream, ExportOptions.none());
}
```

# Citations

[1] [README - Out-of-core: stream straight from a JDBC ResultSet](https://github.com/MaKnoNet/VaadinExcelExport/blob/master/README.md)
