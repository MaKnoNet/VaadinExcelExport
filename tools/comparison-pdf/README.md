# Vergleichs-PDF-Generator

Erzeugt `app/src/main/resources/META-INF/resources/excel-export-vergleich.pdf` – die PDF, die die App
über den Button **„Vergleich (PDF)"** einbettet und die auf GitHub sichtbar ist.

Verglichen werden die beiden im Projekt genutzten Klassen:

- `de.makno.vaadinexcelexport.GridExcelExporter` (baut auf **xlsxbuilder** auf:
  `de.makno.xlsxbuilder` – `XlsxBuilder` + `WorkbookBuilder`, Apache POI SXSSF, out-of-core)
- `com.flowingcode.vaadin.addons.gridexporter.GridExporter` (Flowingcode, direkt auf Apache POI)

## Voraussetzung

[reportlab](https://pypi.org/project/reportlab/) (>= 4). Auf dieser Maschine vorhanden im
Python-3.14-Interpreter unter `%LOCALAPPDATA%\Python\pythoncore-3.14-64\python.exe`
(die Offline-venv des Agents hat reportlab **nicht**).

## Aufruf

PowerShell:

```powershell
& "$env:LOCALAPPDATA\Python\pythoncore-3.14-64\python.exe" tools/comparison-pdf/generate_pdf.py
```

Das Skript schreibt die PDF direkt an den Resources-Pfad.

## Pflege

- **Benchmark-Zahlen** stammen aus `./gradlew benchmark` (gegen H2, 25.000 Zeilen) und werden oben
  im Skript unter `BENCH_MAKNOS` / `BENCH_FLOW` / `BENCH_FACTOR` gepflegt.
- Das **Datum** wird beim Lauf automatisch auf den aktuellen Tag gesetzt.
- Feature-Zeilen werden in `build()` über `feature_table([...])` definiert (`True` = Haken,
  `False` = Kreuz, String = freier Text wie `"~"` oder `"Streaming"`).
