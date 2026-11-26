package snr.model;

public enum Category {
    HARDWARE("Hardware"),
    SOFTWARE("Software"),
    NETWORK("Network");

    private final String label;

    Category(String label) { this.label = label; }

    @Override
    public String toString() { return label; }
}
