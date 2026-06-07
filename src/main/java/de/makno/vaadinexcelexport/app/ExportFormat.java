package de.makno.vaadinexcelexport.app;

/**
 * Auswählbares Ausgabeformat des Vergleichs. Bündelt Anzeigelabel, Dateiendung und MIME-Type, sodass
 * UI-Umschalter, Dateiname und Download-Content-Type aus einer Quelle stammen.
 */
enum ExportFormat {
    XLSX("Excel (.xlsx)", "xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    CSV("CSV (.csv)", "csv", "text/csv; charset=UTF-8");

    private final String label;
    private final String extension;
    private final String mimeType;

    ExportFormat(String label, String extension, String mimeType) {
        this.label = label;
        this.extension = extension;
        this.mimeType = mimeType;
    }

    String label() {
        return label;
    }

    String extension() {
        return extension;
    }

    String mimeType() {
        return mimeType;
    }
}
