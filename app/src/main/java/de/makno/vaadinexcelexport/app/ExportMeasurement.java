package de.makno.vaadinexcelexport.app;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * Führt einen Excel-Export vermessen aus: erfasst Laufzeit und – sofern auf einer HotSpot-JVM
 * verfügbar – die vom ausführenden Thread allozierten Bytes.
 *
 * <p>Die Speichermessung nutzt {@link com.sun.management.ThreadMXBean#getThreadAllocatedBytes(long)}:
 * eine monoton steigende, GC-unabhängige Allokationssumme des aktuellen Threads. Damit ist die
 * „Speicherauslastung" eines Exports stabil reproduzierbar, anders als eine Heap-Differenz, die von
 * paralleler Garbage Collection verfälscht würde.
 *
 * <p><b>Thread-Sicherheit:</b> Die Messung läuft synchron im aufrufenden Thread; es wird kein
 * geteilter, veränderlicher Zustand gehalten. Die allozierten Bytes beziehen sich ausschließlich
 * auf den aufrufenden Thread.
 */
final class ExportMeasurement {

    /** Schreibt einen Excel-Export in den gegebenen Stream. */
    @FunctionalInterface
    interface ExcelExport {
        void writeTo(OutputStream out) throws IOException;
    }

    /**
     * Ergebnis einer Messung: die erzeugten Bytes (für den Download) und die zugehörigen Metriken.
     */
    record Result(byte[] bytes, ExportMetrics metrics) {}

    private ExportMeasurement() {}

    /**
     * Führt {@code export} aus und misst Laufzeit, allozierte Bytes und Ausgabegröße.
     *
     * @param engine   Bezeichner der Engine (für die Anzeige)
     * @param rowCount Anzahl der exportierten Zeilen (für die Anzeige)
     * @param export   der auszuführende Export
     * @return erzeugte Bytes samt {@link ExportMetrics}
     */
    static Result run(String engine, int rowCount, ExcelExport export) {
        ThreadMeter meter = ThreadMeter.start();
        long startNanos = System.nanoTime();

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            export.writeTo(buffer);
        } catch (IOException e) {
            throw new UncheckedIOException("Excel-Export fehlgeschlagen", e);
        }

        long durationNanos = System.nanoTime() - startNanos;
        long allocatedBytes = meter.allocatedSinceStart();
        byte[] bytes = buffer.toByteArray();

        ExportMetrics metrics = new ExportMetrics(engine, rowCount, durationNanos, allocatedBytes, bytes.length);
        return new Result(bytes, metrics);
    }

    /**
     * Kapselt die threadbezogene Allokationsmessung und degradiert sauber, falls die JVM die
     * HotSpot-Erweiterung {@link com.sun.management.ThreadMXBean} nicht bereitstellt.
     */
    private static final class ThreadMeter {

        private static final long UNAVAILABLE = -1L;

        private final com.sun.management.ThreadMXBean bean;
        private final long threadId;
        private final long startBytes;

        private ThreadMeter(com.sun.management.ThreadMXBean bean, long threadId, long startBytes) {
            this.bean = bean;
            this.threadId = threadId;
            this.startBytes = startBytes;
        }

        static ThreadMeter start() {
            ThreadMXBean base = ManagementFactory.getThreadMXBean();
            if (base instanceof com.sun.management.ThreadMXBean sun && sun.isThreadAllocatedMemorySupported()) {
                sun.setThreadAllocatedMemoryEnabled(true);
                long id = Thread.currentThread().threadId();
                return new ThreadMeter(sun, id, sun.getThreadAllocatedBytes(id));
            }
            return new ThreadMeter(null, 0L, UNAVAILABLE);
        }

        long allocatedSinceStart() {
            if (bean == null) {
                return UNAVAILABLE;
            }
            return bean.getThreadAllocatedBytes(threadId) - startBytes;
        }
    }
}
