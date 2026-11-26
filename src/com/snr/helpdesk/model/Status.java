package com.snr.helpdesk.model;

public enum Status {
    OPEN("Open"),
    IN_PROGRESS("In Progress"),
    RESOLVED("Resolved"),
    CLOSED("Closed");

    private final String label;

    Status(String label) {
        this.label = label;
    }

    /** Tickets can only move forward one step: Open -> In Progress -> Resolved -> Closed. */
    public boolean canMoveTo(Status next) {
        return next.ordinal() == this.ordinal() + 1;
    }

    @Override
    public String toString() {
        return label;
    }
}
