package snr.util;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class Dates {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("MMM d, h:mm a");

    private Dates() { }

    public static String toDb(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.atZone(ZoneId.systemDefault()).toOffsetDateTime().toString();
    }

    public static LocalDateTime fromDb(Object value) {
        if (value == null) return null;
        return OffsetDateTime.parse(value.toString()).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static String format(LocalDateTime dateTime) {
        return dateTime == null ? "-" : dateTime.format(DATE_TIME);
    }
}
