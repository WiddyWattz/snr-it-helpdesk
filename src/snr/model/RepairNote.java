package snr.model;

import java.time.LocalDateTime;

public class RepairNote {
    private final User author;
    private final String note;
    private final LocalDateTime dateTime;

    public RepairNote(User author, String note, LocalDateTime dateTime) {
        this.author = author;
        this.note = note;
        this.dateTime = dateTime;
    }

    public User getAuthor() { return author; }
    public String getNote() { return note; }
    public LocalDateTime getDateTime() { return dateTime; }
}
