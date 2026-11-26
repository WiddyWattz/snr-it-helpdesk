package com.snr.helpdesk.model;

public enum Priority {
    LOW("Low", 72),
    MEDIUM("Medium", 24),
    HIGH("High", 4);

    private final String label;
    private final int slaHours;

    Priority(String label, int slaHours) {
        this.label = label;
        this.slaHours = slaHours;
    }

    public int getSlaHours() { return slaHours; }

    @Override
    public String toString() {
        return label;
    }
}
