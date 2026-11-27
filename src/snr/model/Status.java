package snr.model;

public enum Status {
    OPEN("Open"),
    IN_PROGRESS("In Progress"),
    RESOLVED("Resolved"),
    CLOSED("Closed");

    private final String label;

    Status(String label) { this.label = label; }

    // One step forward at a time, or Resolved back to In Progress when reopened.
    public boolean canMoveTo(Status next) {
        return next.ordinal() == ordinal() + 1 || (this == RESOLVED && next == IN_PROGRESS);
    }

    @Override
    public String toString() { return label; }
}
