# Upgrade auf Vaadin 24.10.6 + Spring Boot 3.5.3 (mit DownloadHandler)

Der aktuelle Stand baut gegen **Vaadin 24.5.3 / Spring Boot 3.3.5**, weil das in der
Build-Umgebung ohne Internet aus dem lokalen Gradle-Cache verfügbar war. Sobald ein
Online-Build möglich ist, kann auf die im Plan vorgesehene Version 24.10.6 mit der aktuellen
`DownloadHandler`-API hochgezogen werden. Dazu sind genau drei Stellen anzupassen.

## 1. `gradle.properties`
```properties
vaadinVersion=24.10.6
```

## 2. `app/build.gradle` – Plugin-Version
```groovy
id 'org.springframework.boot' version '3.5.3'
```
Optional (online wieder möglich, aber nicht nötig):
- Der Block `dependencyManagement { dependencies { dependency 'org.apache.logging.log4j:...:2.24.3' } }`
  kann entfernt werden (die vom Spring-BOM verwaltete Log4j-Version wird dann online geladen).
- Statt `org.junit.jupiter:junit-jupiter` kann wieder `org.springframework.boot:spring-boot-starter-test`
  verwendet werden (lädt online die volle Test-Infrastruktur).

## 3. `MainView.java` – Download über `DownloadHandler`
Ab Vaadin 24.8 ist `StreamResource` als Download-Quelle veraltet; stattdessen die
`DownloadHandler`-API nutzen (schreibt direkt in den Antwort-`OutputStream`).

**Imports** anpassen – entfernen:
`com.vaadin.flow.server.StreamResource`, `java.io.ByteArrayInputStream`,
`java.io.ByteArrayOutputStream`, `java.io.InputStream`, `java.io.UncheckedIOException`.
**Hinzufügen:** `com.vaadin.flow.server.streams.DownloadHandler`.
(`java.io.IOException` wird weiter benötigt.)

**Den `StreamResource`-Download (`triggerDownload(...)`, `hiddenDownloadAnchor()`) durch einen
`DownloadHandler`-Anchor ersetzen, schematisch:**
```java
private Anchor createExportButton(Grid<SampleRow> grid, ListDataProvider<SampleRow> dataProvider) {
    GridExcelExporter<SampleRow> exporter = GridExcelExporter.from(SHEET_NAME, grid);
    DownloadHandler handler = event -> {
        event.setFileName(FILE_NAME);
        event.setContentType(XLSX_MIME_TYPE);
        exporter.export(dataProvider, event.getOutputStream());
    };
    Anchor downloadLink = new Anchor(handler, "");
    downloadLink.add(new Button("Excel-Export", VaadinIcon.DOWNLOAD.create()));
    return downloadLink;
}
```
`event.setFileName(...)` setzt die Content-Disposition automatisch auf `attachment` (Download).
Die geprüfte checked `IOException` aus `export(...)` darf hier propagieren, da
`DownloadHandler.handleDownloadRequest` `IOException` deklariert.

## 4. Bauen & verifizieren
```
gradlew spotlessApply
gradlew test
gradlew bootRun        # http://localhost:8080  -> Button "Excel-Export"
```

> Der **Kern** (`de.makno.vaadinexcelexport.GridExcelExporter` und `ExcelMeta`) bleibt beim
> Upgrade **unverändert** – nur die Demo-View und die Versionsnummern ändern sich.
