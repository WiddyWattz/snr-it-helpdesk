package snr.model;

public class Device {
    public static final String WORKING = "Working";
    public static final String NEEDS_REPAIR = "Needs repair";
    public static final String RETIRED = "Retired";

    private final String code;
    private final DeviceType type;
    private String location;
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
    public void setLocation(String location) { this.location = location; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    public boolean isRetired() { return RETIRED.equals(condition); }

    @Override
    public boolean equals(Object o) {
        return o instanceof Device && ((Device) o).code.equals(code);
    }

    @Override
    public int hashCode() { return code.hashCode(); }

    @Override
    public String toString() { return location + " - " + code; }
}
