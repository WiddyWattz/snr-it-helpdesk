package snr.model;

public enum DeviceType {
    PC("PC"),
    PRINTER("Printer"),
    ROUTER("Router");

    private final String label;

    DeviceType(String label) { this.label = label; }

    @Override
    public String toString() { return label; }
}
