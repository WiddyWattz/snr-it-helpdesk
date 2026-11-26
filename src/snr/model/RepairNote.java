package snr.model;

import java.time.LocalDateTime;

public class RepairNote {
    private final Technician technician;
    private final String note;
    private final LocalDateTime dateTime;

    public RepairNote(Technician technician, String note, LocalDateTime dateTime) {
        this.technician = technician;
        this.note = note;
        this.dateTime = dateTime;
    }

    public Technician getTechnician() { return technician; }
    public String getNote() { return note; }
    public LocalDateTime getDateTime() { return dateTime; }
}
