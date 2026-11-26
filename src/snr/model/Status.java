package snr.model;

public enum Status {
    OPEN("Open"),
    IN_PROGRESS("In Progress"),
    RESOLVED("Resolved"),
    CLOSED("Closed");

    private final String label;

    Status(String label) { this.label = label; }

    // Only one step forward at a time.
    public boolean canMoveTo(Status next) {
        return next.ordinal() == ordinal() + 1;
    }

    @Override
    public String toString() { return label; }
}
