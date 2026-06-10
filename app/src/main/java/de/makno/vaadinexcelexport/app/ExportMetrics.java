package de.makno.vaadinexcelexport.app;

import java.util.Locale;

/**
 * Messergebnis eines einzelnen Excel-Exports.
 *
 * @param engine         Bezeichner der Export-Engine (z. B. "MaKnos Export")
 * @param rowCount       Anzahl exportierter Zeilen
 * @param durationNanos  Laufzeit in Nanosekunden
 * @param allocatedBytes durch den Export allozierte Bytes ({@code -1} = nicht verfügbar)
 * @param outputBytes    Größe der erzeugten {@code .xlsx} in Bytes
 */
record ExportMetrics(String engine, int rowCount, long durationNanos, long allocatedBytes, long outputBytes) {

    private static final double NANOS_PER_MILLI = 1_000_000.0;
    private static final double BYTES_PER_MIB = 1024.0 * 1024.0;
    private static final double BYTES_PER_KIB = 1024.0;

    String durationText() {
        return String.format(Locale.GERMANY, "%.1f ms", durationNanos / NANOS_PER_MILLI);
    }

    String allocatedText() {
        if (allocatedBytes < 0) {
            return "n/a";
        }
        return String.format(Locale.GERMANY, "%.1f MB", allocatedBytes / BYTES_PER_MIB);
    }

    String outputText() {
        return String.format(Locale.GERMANY, "%.0f KB", outputBytes / BYTES_PER_KIB);
    }
}
