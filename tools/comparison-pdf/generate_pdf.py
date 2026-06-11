#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Erzeugt die Vergleichs-PDF (GridExcelExporter vs. Flowingcode GridExporter).

Reproduzierbarer Generator fuer
``app/src/main/resources/META-INF/resources/excel-export-vergleich.pdf`` – die PDF, die die App ueber
den Button „Vergleich (PDF)" einbettet und die auf GitHub sichtbar ist.

Benoetigt reportlab (>= 4). Auf dieser Maschine vorhanden im Interpreter
``%LOCALAPPDATA%\\Python\\pythoncore-3.14-64\\python.exe``. Aufruf siehe README.md.

Die Benchmark-Zahlen (BENCH_*) stammen aus ``./gradlew benchmark`` (gegen H2, 25.000 Zeilen) und
werden hier zentral gepflegt.
"""

from datetime import date
from pathlib import Path

from reportlab.lib.colors import HexColor, white
from reportlab.lib.enums import TA_CENTER, TA_LEFT
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import mm
from reportlab.platypus import (
    BaseDocTemplate,
    Frame,
    KeepTogether,
    PageTemplate,
    Paragraph,
    Spacer,
    Table,
    TableStyle,
)

# --------------------------------------------------------------------------- Daten / Konstanten

TODAY = date.today()
_MONTHS_DE = ["Januar", "Februar", "März", "April", "Mai", "Juni", "Juli",
              "August", "September", "Oktober", "November", "Dezember"]
DATE_DE = f"{TODAY.day}. {_MONTHS_DE[TODAY.month - 1]} {TODAY.year}"
DATE_ISO = TODAY.isoformat()

ROWS = "25.000"

# Benchmark-Ergebnis (./gradlew benchmark, H2, 25.000 Zeilen, 2 Warmup + 5 Messlaeufe).
BENCH_MAKNOS = {"median": "358 ms", "avg": "361 ms", "rows_s": "69.747", "size": "2.143 KB"}
BENCH_FLOW = {"median": "10.182 ms", "avg": "9.805 ms", "rows_s": "2.455", "size": "2.146 KB"}
BENCH_FACTOR = "~28x"

OUTPUT = (
    Path(__file__).resolve().parents[2]
    / "app"
    / "src"
    / "main"
    / "resources"
    / "META-INF"
    / "resources"
    / "excel-export-vergleich.pdf"
)

# --------------------------------------------------------------------------- Farben

NAVY = HexColor(0x1F3A5F)
NAVY_LIGHT = HexColor(0x2C4A6E)
GREEN = HexColor(0x5B8A5B)
GREEN_BG = HexColor(0xEAF1EA)
BLUE_BG = HexColor(0xEAF0F6)
ZEBRA = HexColor(0xF3F5F7)
BORDER = HexColor(0xD9DEE3)
TEXT = HexColor(0x333333)
MUTED = HexColor(0x6B7682)
CHECK = HexColor(0x2E8B57)
CROSS = HexColor(0xC0392B)
PARTIAL = HexColor(0x999999)

LEFT_LABEL = "GridExcelExporter"
RIGHT_LABEL = "GridExporter"
DOC_TITLE = "GridExcelExporter vs. Flowingcode GridExporter"

USABLE_WIDTH = A4[0] - 40 * mm  # zwei 20mm-Raender
COL_MARK = 33 * mm
COL_FEATURE = USABLE_WIDTH - 2 * COL_MARK

# --------------------------------------------------------------------------- Styles

styles = getSampleStyleSheet()


def style(name, **kw):
    base = kw.pop("parent", styles["Normal"])
    return ParagraphStyle(name, parent=base, **kw)


S_BODY = style("body", fontName="Helvetica", fontSize=9, leading=12.5, textColor=TEXT)
S_NOTE = style("note", fontName="Helvetica-Oblique", fontSize=8, leading=11, textColor=MUTED)
S_H1 = style("h1", fontName="Helvetica-Bold", fontSize=15, leading=18, textColor=NAVY, spaceBefore=10, spaceAfter=6)
S_H2 = style("h2", fontName="Helvetica-Bold", fontSize=11, leading=14, textColor=NAVY, spaceBefore=8, spaceAfter=3)
S_CELL = style("cell", fontName="Helvetica", fontSize=8.5, leading=11, textColor=TEXT)
S_CELL_HDR = style("cellhdr", fontName="Helvetica-Bold", fontSize=8, leading=9.5, textColor=white, alignment=TA_CENTER)
S_CELL_SUB = style("cellsub", fontName="Helvetica", fontSize=7, leading=8.5, textColor=HexColor(0xC9D4E0), alignment=TA_CENTER)
S_MARK = style("mark", fontName="Helvetica-Bold", fontSize=10, leading=11, alignment=TA_CENTER)
S_BAND = style("band", fontName="Helvetica-Bold", fontSize=12, leading=15, textColor=GREEN, alignment=TA_CENTER)
S_KV_KEY = style("kvkey", fontName="Helvetica-Bold", fontSize=8.5, leading=11, textColor=NAVY)
S_REC_HDR = style("rechdr", fontName="Helvetica-Bold", fontSize=10, leading=13)


def mark(value):
    """value: True -> gruener Haken, False -> rotes Kreuz, sonst Text (z. B. '~' oder 'Streaming')."""
    if value is True:
        return Paragraph('<font color="#2E8B57">&#10003;</font>', S_MARK)
    if value is False:
        return Paragraph('<font color="#C0392B">&#10007;</font>', S_MARK)
    return Paragraph(f'<font color="#666666">{value}</font>', style("marktxt", parent=S_CELL, alignment=TA_CENTER))


# --------------------------------------------------------------------------- Tabellen-Helfer


def feature_table(rows):
    head = [
        Paragraph("Feature", style("fh", parent=S_CELL_HDR, alignment=TA_LEFT)),
        [Paragraph(LEFT_LABEL, S_CELL_HDR), Paragraph("auf xlsxbuilder", S_CELL_SUB)],
        [Paragraph(RIGHT_LABEL, S_CELL_HDR), Paragraph("Flowingcode", S_CELL_SUB)],
    ]
    data = [head]
    for feat, left, right in rows:
        data.append([Paragraph(feat, S_CELL), mark(left), mark(right)])

    t = Table(data, colWidths=[COL_FEATURE, COL_MARK, COL_MARK], repeatRows=1)
    st = [
        ("BACKGROUND", (0, 0), (-1, 0), NAVY),
        ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
        ("ALIGN", (1, 0), (2, -1), "CENTER"),
        ("LEFTPADDING", (0, 0), (-1, -1), 7),
        ("RIGHTPADDING", (0, 0), (-1, -1), 7),
        ("TOPPADDING", (0, 0), (-1, -1), 5),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 5),
        ("LINEBELOW", (0, 0), (-1, -1), 0.5, BORDER),
        ("BOX", (0, 0), (-1, -1), 0.5, BORDER),
    ]
    for i in range(1, len(data)):
        if i % 2 == 0:
            st.append(("BACKGROUND", (0, i), (-1, i), ZEBRA))
    t.setStyle(TableStyle(st))
    return t


def benchmark_table():
    head = ["Engine", "Median", "Avg", "Zeilen/s", "Groesse"]
    data = [
        [Paragraph(h, S_CELL_HDR if i else style("eh", parent=S_CELL_HDR, alignment=TA_LEFT)) for i, h in enumerate(head)],
        [
            [Paragraph(LEFT_LABEL, style("be", parent=S_CELL, fontName="Helvetica-Bold", textColor=NAVY)),
             Paragraph("auf xlsxbuilder", S_NOTE)],
            BENCH_MAKNOS["median"], BENCH_MAKNOS["avg"], BENCH_MAKNOS["rows_s"], BENCH_MAKNOS["size"],
        ],
        [
            [Paragraph(RIGHT_LABEL, style("be2", parent=S_CELL)), Paragraph("Flowingcode", S_NOTE)],
            BENCH_FLOW["median"], BENCH_FLOW["avg"], BENCH_FLOW["rows_s"], BENCH_FLOW["size"],
        ],
    ]
    w = [COL_FEATURE - 25 * mm, 27 * mm, 27 * mm, 27 * mm, 27 * mm]
    # Normalisiere Breiten auf USABLE_WIDTH
    total = sum(w)
    w = [x * USABLE_WIDTH / total for x in w]
    t = Table(data, colWidths=w)
    t.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, 0), NAVY),
        ("TEXTCOLOR", (0, 0), (-1, 0), white),
        ("FONTNAME", (0, 1), (-1, -1), "Helvetica"),
        ("FONTSIZE", (1, 1), (-1, -1), 9),
        ("ALIGN", (1, 0), (-1, -1), "CENTER"),
        ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
        ("BACKGROUND", (0, 2), (-1, 2), ZEBRA),
        ("LINEBELOW", (0, 0), (-1, -1), 0.5, BORDER),
        ("BOX", (0, 0), (-1, -1), 0.5, BORDER),
        ("TOPPADDING", (0, 0), (-1, -1), 6),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 6),
        ("LEFTPADDING", (0, 0), (-1, -1), 8),
    ]))
    return t


def band(text):
    t = Table([[Paragraph(text, S_BAND)]], colWidths=[USABLE_WIDTH])
    t.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, -1), GREEN_BG),
        ("LINEABOVE", (0, 0), (-1, -1), 2, GREEN),
        ("LINEBELOW", (0, 0), (-1, -1), 2, GREEN),
        ("TOPPADDING", (0, 0), (-1, -1), 7),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 7),
    ]))
    return t


def callout(title, body, accent=NAVY, bg=BLUE_BG):
    inner = [Paragraph(f'<b>{title}</b>', style("cot", parent=S_BODY, textColor=accent)),
             Spacer(1, 2),
             Paragraph(body, S_BODY)]
    t = Table([[inner]], colWidths=[USABLE_WIDTH])
    t.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, -1), bg),
        ("LINEBEFORE", (0, 0), (0, -1), 3, accent),
        ("LEFTPADDING", (0, 0), (-1, -1), 10),
        ("RIGHTPADDING", (0, 0), (-1, -1), 10),
        ("TOPPADDING", (0, 0), (-1, -1), 8),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 8),
    ]))
    return t


def kv_table(pairs):
    data = [[Paragraph(k, S_KV_KEY), Paragraph(v, S_CELL)] for k, v in pairs]
    t = Table(data, colWidths=[42 * mm, USABLE_WIDTH - 42 * mm])
    st = [
        ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
        ("LINEBELOW", (0, 0), (-1, -1), 0.4, BORDER),
        ("BOX", (0, 0), (-1, -1), 0.5, BORDER),
        ("TOPPADDING", (0, 0), (-1, -1), 4),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 4),
        ("LEFTPADDING", (0, 0), (-1, -1), 8),
    ]
    for i in range(len(data)):
        if i % 2 == 1:
            st.append(("BACKGROUND", (0, i), (-1, i), ZEBRA))
    t.setStyle(TableStyle(st))
    return t


def recommendation():
    def col(items):
        return [Paragraph("&#8226; " + it, style("rec", parent=S_BODY, leftIndent=4, leading=13)) for it in items]

    left = [Paragraph(f"{LEFT_LABEL} (xlsxbuilder)", style("rl", parent=S_REC_HDR, textColor=GREEN))]
    left += col([
        f"Performance zaehlt ({BENCH_FACTOR} schneller)",
        "Sehr grosse Datenmengen out-of-core streamen (direkt aus der DB)",
        "Echte typisierte Zellen, Formeln, klickbare Hyperlinks",
        "Sortierung der Tabelle wird uebernommen",
        "Fusszeile/Summenzeile mit Platzhaltern ({rowCount}, {sum:Spalte})",
    ])
    right = [Paragraph(f"{RIGHT_LABEL} (Flowingcode)", style("rr", parent=S_REC_HDR, textColor=NAVY))]
    right += col([
        "Hierarchische Daten (TreeGrid) exportieren",
        "Mehrere Formate anbieten (CSV, DOCX, PDF)",
        "Excel-Vorlage mit Platzhaltern befuellen",
        "Mehrzeilige / verbundene Header",
        "Fertige UI-Buttons ohne eigenen Code",
    ])
    t = Table([[left, right]], colWidths=[USABLE_WIDTH / 2, USABLE_WIDTH / 2])
    t.setStyle(TableStyle([
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ("BACKGROUND", (0, 0), (0, 0), GREEN_BG),
        ("BACKGROUND", (1, 0), (1, 0), BLUE_BG),
        ("LINEBEFORE", (0, 0), (0, 0), 3, GREEN),
        ("LINEBEFORE", (1, 0), (1, 0), 3, NAVY),
        ("LEFTPADDING", (0, 0), (-1, -1), 10),
        ("RIGHTPADDING", (0, 0), (-1, -1), 10),
        ("TOPPADDING", (0, 0), (-1, -1), 8),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 10),
    ]))
    return t


# --------------------------------------------------------------------------- Seiten-Dekoration


def on_page(canvas, doc):
    canvas.saveState()
    w, h = A4
    # Kopfband
    canvas.setFillColor(NAVY)
    canvas.rect(0, h - 22 * mm, w, 14 * mm, fill=1, stroke=0)
    canvas.setFillColor(GREEN)
    canvas.rect(0, h - 22 * mm, 6 * mm, 14 * mm, fill=1, stroke=0)
    canvas.setFillColor(white)
    canvas.setFont("Helvetica-Bold", 11)
    canvas.drawRightString(w - 20 * mm, h - 17 * mm, "GridExcelExporter vs. Flowingcode GridExporter")
    # Fusszeile
    canvas.setFillColor(MUTED)
    canvas.setFont("Helvetica", 7.5)
    canvas.drawString(20 * mm, 12 * mm, f"GridExcelExporter vs. Flowingcode  |  VaadinExcelExport  |  {DATE_ISO}")
    canvas.drawRightString(w - 20 * mm, 12 * mm, f"Seite {doc.page}")
    canvas.setStrokeColor(BORDER)
    canvas.setLineWidth(0.5)
    canvas.line(20 * mm, 15 * mm, w - 20 * mm, 15 * mm)
    canvas.restoreState()


# --------------------------------------------------------------------------- Dokument


def build():
    doc = BaseDocTemplate(
        str(OUTPUT),
        pagesize=A4,
        leftMargin=20 * mm,
        rightMargin=20 * mm,
        topMargin=28 * mm,
        bottomMargin=18 * mm,
        title=DOC_TITLE,
        author="VaadinExcelExport",
    )
    frame = Frame(doc.leftMargin, doc.bottomMargin, doc.width, doc.height, id="main")
    doc.addPageTemplates([PageTemplate(id="all", frames=[frame], onPage=on_page)])

    story = []
    story.append(Paragraph(f"Datum: {DATE_DE} &nbsp;|&nbsp; Projekt: VaadinExcelExport (github.com/MaKnoNet/VaadinExcelExport)", S_NOTE))
    story.append(Spacer(1, 8))

    # Architektur-Kasten
    story.append(callout(
        "Worum geht es?",
        f"<b>{LEFT_LABEL}</b> (<font name='Helvetica'>de.makno.vaadinexcelexport</font>) ist eine schlanke "
        "Bruecke: sie liest die Spalten des Vaadin-<i>Grid</i> und delegiert das Schreiben an die Bibliothek "
        "<b>xlsxbuilder</b> (<font name='Helvetica'>de.makno.xlsxbuilder</font> &ndash; <font name='Helvetica'>XlsxBuilder</font> "
        "+ <font name='Helvetica'>WorkbookBuilder</font>, auf Apache POI SXSSF, out-of-core streamend). "
        f"Flowingcodes <b>{RIGHT_LABEL}</b> (<font name='Helvetica'>com.flowingcode.vaadin.addons.gridexporter</font>) baut "
        "direkt auf Apache POI auf, bietet mehrere Ausgabeformate und benoetigt eine Vaadin-Session.",
    ))
    story.append(Spacer(1, 12))

    # 1 Benchmark
    story.append(Paragraph("1 Benchmark-Ergebnis (aus SQL-Datenbank)", S_H1))
    story.append(Paragraph(
        f"Gemessen gegen eine file-basierte H2-Datenbank mit {ROWS} Zeilen: {LEFT_LABEL} streamt <b>out-of-core</b> "
        "aus einem forward-only JDBC-ResultSet (FetchSize 1000); Flowingcode liest ueber das lazy, seitenweise Grid "
        "(Apache POI im RAM). 2 Warmup- und 5 Messlaeufe.", S_BODY))
    story.append(Spacer(1, 6))
    story.append(benchmark_table())
    story.append(Spacer(1, 8))
    story.append(band(f"{LEFT_LABEL} ist {BENCH_FACTOR} schneller"))
    story.append(Spacer(1, 5))
    story.append(Paragraph(
        "Konfiguration: Java 21, Gradle 8.9, Vaadin 24.5.3, Apache POI 5.4.0, H2 2.3.232  |  Ausfuehrung: "
        "./gradlew benchmark [-Prows=N]  |  Klasse: ExcelExporterBenchmarkTest (misst beide Engines aus der H2-Datenbank). "
        "Der optionale Pipeline-Parallelismus (parallel()) bringt bei POI-dominierter Last wenig &ndash; sein Nutzen "
        "liegt bei einem Producer-Flaschenhals.", S_NOTE))
    story.append(Spacer(1, 12))

    # 2 Feature-Vergleich
    story.append(Paragraph("2 Feature-Vergleich", S_H1))

    story.append(Paragraph("2.1 Ausgabeformate", S_H2))
    story.append(feature_table([
        ("Excel (.xlsx)", True, True),
        ("CSV", False, True),
        ("DOCX (Word)", False, True),
        ("PDF", False, True),
    ]))
    story.append(Spacer(1, 3))
    story.append(Paragraph(
        "xlsxbuilder erzeugt bewusst nur .xlsx: CSV wurde wieder entfernt, weil sich FORMULA-/HYPERLINK-Zellen in CSV "
        "nicht abbilden lassen, ohne ein vollstaendiges In-Memory-Workbook auszuwerten (Konflikt mit dem "
        "Out-of-core-Design). DOCX/PDF bietet nur Flowingcode.", S_NOTE))
    story.append(Spacer(1, 8))

    story.append(Paragraph("2.2 Datenquelle, Sortierung &amp; Filter", S_H2))
    story.append(feature_table([
        ("Respektiert Grid-Sortierung (Spaltenkoepfe)", True, True),
        ("Respektiert aktiven Grid-Filter automatisch", True, True),
        ("Programmatischer Daten-Filter (Predicate)", True, False),
        ("Out-of-core-Stream direkt aus JDBC-ResultSet", True, False),
        ("Out-of-core External-Merge-Sort (sortBy)", True, False),
        ("Streaming / out-of-core (konstanter Speicher)", True, False),
        ("TreeGrid / hierarchische Daten", False, True),
    ]))
    story.append(Spacer(1, 3))
    story.append(Paragraph(
        "Beide Exporte spiegeln den aktiven Grid-Filter automatisch: Flowingcode liest den Filter aus dem "
        "DataProvider; GridExcelExporter erhaelt den Suchbegriff als parametrisiertes SQL-WHERE und streamt nur die "
        "passenden Zeilen weiterhin out-of-core (ein Suchfeld, eine Quelle der Wahrheit fuer Anzeige + beide Exporte).", S_NOTE))
    story.append(Spacer(1, 8))

    story.append(Paragraph("2.3 Zelltypen &amp; Formatierung", S_H2))
    story.append(feature_table([
        ("Typisierte Zellen (INTEGER, LONG, DOUBLE, DECIMAL, BOOLEAN, DATE, DATETIME, TIME)", True, False),
        ("Echte Excel-Formeln (z. B. =E2*0.19)", True, False),
        ("Klickbare Hyperlinks in Excel", "HYPERLINK", "URL-Text"),
        ("Excel-Formatcode pro Spalte (#,##0.00)", True, True),
        ("Java DecimalFormat/DateFormat + Excel-Code kombiniert", False, True),
        ("Null-Wert-Handler (Platzhalter-Text / leere Zelle)", True, True),
    ]))
    story.append(Spacer(1, 8))

    story.append(Paragraph("2.4 Layout, Footer &amp; Aggregation", S_H2))
    story.append(feature_table([
        ("Einfacher Sheet-Name", True, True),
        ("Titelzeile ueber alle Spalten, auto-merge (header)", True, True),
        ("Fusszeile ueber alle Spalten, auto-merge (footer)", True, True),
        ("Berechnete Footer-Platzhalter {rowCount} / {sum:Spalte} / {datetime}", True, False),
        ("Summenzeile (sumColumn, optional als Excel-Formel)", True, "~"),
        ("Auto-Size Columns (beim Streamen gemessen)", True, True),
        ("Mehrzeilige Header / Joined Headers", True, True),
        ("Spalten-Reihenfolge im Export ueberschreiben", True, True),
        ("Template-basierter Export (.xlsx-Vorlage)", False, True),
        ("Eigene Platzhalter im Template", False, True),
    ]))
    story.append(Spacer(1, 3))
    story.append(Paragraph(
        "Neu in xlsxbuilder: Titel-/Fusszeilen (ueber die Breite gemerged) mit Platzhaltern, die beim Schreiben "
        "aufgeloest werden &ndash; eingebaut {date}/{datetime}, sowie im Footer {rowCount} und {sum:Spaltenname}; "
        "die Summenzeile aktiviert die Summenverfolgung. Die Demo nutzt eine Fusszeile "
        "&bdquo;Erzeugt am {datetime} &ndash; {rowCount} Zeilen &ndash; Summe Betrag: {sum:Betrag} EUR&ldquo;. "
        "Verbundene Header (joined headers) entstehen aus gruppierten Spalten "
        "(<font name='Helvetica'>columnGroups(...)</font>, gemergte Gruppenzeile ueber dem Spaltenkopf); die "
        "Export-Spaltenreihenfolge laesst sich unabhaengig von der Anzeige ueberschreiben "
        "(<font name='Helvetica'>GridExcelExporter.from(sheet, grid, columnOrder)</font>).", S_NOTE))
    story.append(Spacer(1, 8))

    story.append(Paragraph("2.5 UI-Integration, Server-Tuning &amp; Performance", S_H2))
    story.append(feature_table([
        ("Fertige Download-Buttons (auto an Grid-Footer)", False, True),
        ("Concurrent-Download-Throttling (Semaphore)", False, True),
        ("Pipeline-Parallelismus (parallel())", True, False),
        ("Server-Tuning: Temp-Verzeichnis + SXSSF-Fenster", True, False),
        ("Single-Use-Guard (Schutz vor Wiederverwendung)", True, False),
        ("Performance (25.000 Zeilen aus DB, Median)", BENCH_MAKNOS["median"], BENCH_FLOW["median"]),
        ("Arbeitsspeicher", "Streaming", "RAM"),
        ("Benoetigt Vaadin-Session/UI", "Nein", "Ja"),
    ]))
    story.append(Spacer(1, 12))

    # 3 Empfehlung
    story.append(Paragraph("3 Empfehlung", S_H1))
    story.append(recommendation())
    story.append(Spacer(1, 12))

    # 4 Technische Details
    story.append(KeepTogether([
        Paragraph("4 Technische Details", S_H1),
        kv_table([
            ("Projekt", "VaadinExcelExport (github.com/MaKnoNet/VaadinExcelExport)"),
            ("Stack", "Java 21, Spring Boot 3.3.5, Vaadin 24.5.3"),
            ("Brueckenklasse", "de.makno.vaadinexcelexport.GridExcelExporter (liest Grid-Spalten, delegiert an xlsxbuilder)"),
            ("xlsxbuilder", "de.makno.xlsxbuilder:xlsxbuilder:1.0.0-SNAPSHOT - XlsxBuilder + WorkbookBuilder (Apache POI SXSSF), Binaer-Abhaengigkeit aus mavenLocal"),
            ("Flowingcode", "com.flowingcode.vaadin.addons.gridexporter.GridExporter (org.vaadin.addons.flowingcode:grid-exporter-addon:2.5.0)"),
            ("Testdaten", "H2 2.3.232 (file-basiert, reines JDBC) - lazy geladen / gestreamt"),
            ("Apache POI", "5.4.0"),
            ("Benchmark", 'ExcelExporterBenchmarkTest (@Tag("benchmark"), aus H2)'),
            ("Ausfuehrung", "./gradlew benchmark [-Prows=N]"),
            ("Datum", DATE_DE),
        ]),
    ]))

    doc.build(story)
    print(f"PDF geschrieben: {OUTPUT}")


if __name__ == "__main__":
    build()
