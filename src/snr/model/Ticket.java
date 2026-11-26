package snr.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Ticket {
    private final int id;
    private final String description;
    private final Priority priority;
    private final Device device;
    private final Reporter reporter;
    private final LocalDateTime createdAt;
    private final List<RepairNote> notes = new ArrayList<>();
    private Status status = Status.OPEN;
    private Technician assignee;
    private LocalDateTime assignedAt;
    private LocalDateTime resolvedAt;

    protected Ticket(int id, String description, Priority priority, Device device,
                     Reporter reporter, LocalDateTime createdAt) {
        this.id = id;
        this.description = description;
        this.priority = priority;
        this.device = device;
        this.reporter = reporter;
        this.createdAt = createdAt;
    }

    public abstract Category getCategory();

    public int getSlaHours() { return priority.getSlaHours(); }

    public LocalDateTime getDueAt() { return createdAt.plusHours(getSlaHours()); }

    public boolean isOverdue() {
        boolean done = status == Status.RESOLVED || status == Status.CLOSED;
        return !done && LocalDateTime.now().isAfter(getDueAt());
    }

    public void assignTo(Technician technician, LocalDateTime when) {
        this.assignee = technician;
        this.assignedAt = when;
    }

    public void moveTo(Status next, LocalDateTime when) throws InvalidStatusChangeException {
        if (!status.canMoveTo(next)) throw new InvalidStatusChangeException(status, next);
        status = next;
        if (next == Status.RESOLVED) resolvedAt = when;
    }

    public void addNote(RepairNote note) { notes.add(note); }

    public int getId() { return id; }
    public String getDescription() { return description; }
    public Priority getPriority() { return priority; }
    public Device getDevice() { return device; }
    public Reporter getReporter() { return reporter; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Status getStatus() { return status; }
    public Technician getAssignee() { return assignee; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public List<RepairNote> getNotes() { return Collections.unmodifiableList(notes); }
}
