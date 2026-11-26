package com.snr.helpdesk.model;

import java.time.LocalDateTime;

/** Base class for every ticket. Each subclass decides its own category and time limit. */
public abstract class Ticket {
    private final int id;
    private final String description;
    private final Priority priority;
    private final Device device;
    private final User reporter;
    private final LocalDateTime createdAt;
    private Status status = Status.OPEN;
    private User assignee;

    protected Ticket(int id, String description, Priority priority, Device device,
                     User reporter, LocalDateTime createdAt) {
        this.id = id;
        this.description = description;
        this.priority = priority;
        this.device = device;
        this.reporter = reporter;
        this.createdAt = createdAt;
    }

    public abstract Category getCategory();

    /** Hours allowed before the ticket is overdue. Subclasses may override. */
    public int getSlaHours() {
        return priority.getSlaHours();
    }

    public LocalDateTime getDueAt() {
        return createdAt.plusHours(getSlaHours());
    }

    public boolean isOverdue() {
        boolean done = status == Status.RESOLVED || status == Status.CLOSED;
        return !done && LocalDateTime.now().isAfter(getDueAt());
    }

    public void moveTo(Status next) {
        if (!status.canMoveTo(next)) {
            throw new IllegalStateException("Cannot change status from " + status + " to " + next);
        }
        status = next;
    }

    public int getId() { return id; }
    public String getDescription() { return description; }
    public Priority getPriority() { return priority; }
    public Device getDevice() { return device; }
    public User getReporter() { return reporter; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Status getStatus() { return status; }
    public User getAssignee() { return assignee; }
    public void setAssignee(User assignee) { this.assignee = assignee; }
}
