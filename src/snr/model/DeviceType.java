package snr.model;

public enum DeviceType {
    POS_TERMINAL("POS Terminal"),
    BARCODE_SCANNER("Barcode Scanner"),
    RECEIPT_PRINTER("Receipt Printer"),
    PC("PC"),
    PRINTER("Printer"),
    ROUTER("Router");

    private final String label;

    DeviceType(String label) { this.label = label; }

    @Override
    public String toString() { return label; }
}
