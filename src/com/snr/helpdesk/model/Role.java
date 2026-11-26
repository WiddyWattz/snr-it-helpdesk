package com.snr.helpdesk.model;

public enum Role {
    REPORTER("Reporter"),
    TECHNICIAN("Technician"),
    ADMIN("IT Admin");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
