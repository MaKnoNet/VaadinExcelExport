package de.makno.vaadinexcelexport.app;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/** Liefert eine kleine, feste Menge an Beispielzeilen für die Demo-Tabelle. */
final class SampleData {

    private SampleData() {}

    static List<SampleRow> rows() {
        return List.of(
                new SampleRow(
                        "Alpha",
                        42,
                        9_000_000_111L,
                        3.14159,
                        new BigDecimal("1234.50"),
                        true,
                        LocalDate.of(2026, 1, 15),
                        LocalDateTime.of(2026, 1, 15, 8, 30),
                        seconds(8, 15, 0)),
                new SampleRow(
                        "Bravo",
                        -7,
                        12_345_678_901L,
                        2.71828,
                        new BigDecimal("0.99"),
                        false,
                        LocalDate.of(2026, 2, 28),
                        LocalDateTime.of(2026, 2, 28, 17, 5),
                        seconds(9, 2, 30)),
                new SampleRow(
                        "Charlie",
                        1000,
                        42L,
                        0.5,
                        new BigDecimal("99999.95"),
                        true,
                        LocalDate.of(2026, 3, 1),
                        LocalDateTime.of(2026, 3, 1, 0, 0),
                        seconds(7, 45, 10)),
                new SampleRow(
                        "Delta",
                        0,
                        -5_000_000_000L,
                        -123.456,
                        new BigDecimal("-250.00"),
                        false,
                        LocalDate.of(2026, 12, 31),
                        LocalDateTime.of(2026, 12, 31, 23, 59),
                        seconds(10, 30, 0)),
                new SampleRow(
                        "Echo",
                        256,
                        7_777_777_777L,
                        100.0,
                        new BigDecimal("4200.75"),
                        true,
                        LocalDate.of(2027, 6, 30),
                        LocalDateTime.of(2027, 6, 30, 12, 0),
                        seconds(8, 0, 45)));
    }

    private static int seconds(int hours, int minutes, int secs) {
        return (int) LocalTime.of(hours, minutes, secs).toSecondOfDay();
    }
}
