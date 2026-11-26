package snr.model;

public class Device {
    private final String code;
    private final DeviceType type;
    private final String location;
    private String condition;

    public Device(String code, DeviceType type, String location, String condition) {
        this.code = code;
        this.type = type;
        this.location = location;
        this.condition = condition;
    }

    public String getCode() { return code; }
    public DeviceType getType() { return type; }
    public String getLocation() { return location; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    @Override
    public String toString() { return location + " - " + code; }
}
