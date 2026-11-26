package snr.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class Dates {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("MMM d, h:mm a");

    private Dates() { }

    public static String format(LocalDateTime dateTime) {
        return dateTime == null ? "-" : dateTime.format(DATE_TIME);
    }
}
