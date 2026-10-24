package dev.mclavo.conduit.common;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public final class Timestamps {
    private static final DateTimeFormatter FORMAT = new DateTimeFormatterBuilder().appendInstant(3).toFormatter();
    private Timestamps() {}
    public static String format(Instant timestamp) { return FORMAT.format(timestamp); }
}
