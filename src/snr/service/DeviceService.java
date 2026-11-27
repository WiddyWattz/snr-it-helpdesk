package snr.service;

import snr.data.DataStore;
import snr.model.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DeviceService {
    private final DataStore store;

    public DeviceService(DataStore store) { this.store = store; }

    public List<Device> getAllDevices() { return store.getAllDevices(); }

    public List<Device> getActiveDevices() {
        List<Device> list = new ArrayList<>();
        for (Device d : store.getAllDevices()) {
            if (!d.isRetired()) list.add(d);
        }
        return list;
    }

    public Device addDevice(String code, DeviceType type, String location) throws HelpdeskException {
        if (code == null || code.trim().isEmpty()) throw new HelpdeskException("Enter a device code.");
        if (type == null) throw new HelpdeskException("Choose a device type.");
        if (location == null || location.trim().isEmpty()) throw new HelpdeskException("Enter a location.");
        if (store.findDevice(code) != null) throw new HelpdeskException("Device code already exists: " + code.trim());

        Device device = new Device(code.trim().toUpperCase(), type, location.trim(), Device.WORKING);
        store.insertDevice(device);
        return device;
    }

    public void updateDevice(Device device, String location, String condition) throws HelpdeskException {
        if (device.isRetired()) throw new HelpdeskException("Retired devices cannot be edited.");
        if (location == null || location.trim().isEmpty()) throw new HelpdeskException("Enter a location.");
        if (condition == null) throw new HelpdeskException("Choose a condition.");
        device.setLocation(location.trim());
        device.setCondition(condition);
        store.updateDevice(device);
    }

    public void retireDevice(Device device) throws HelpdeskException {
        if (device.isRetired()) throw new HelpdeskException("This device is already retired.");
        for (Ticket t : getRepairHistory(device)) {
            if (t.getStatus() == Status.OPEN || t.getStatus() == Status.IN_PROGRESS) {
                throw new HelpdeskException("Resolve this device's open tickets before retiring it.");
            }
        }
        device.setCondition(Device.RETIRED);
        store.updateDevice(device);
    }

    public List<Ticket> getRepairHistory(Device device) {
        List<Ticket> list = new ArrayList<>();
        for (Ticket t : store.getAllTickets()) {
            if (device.equals(t.getDevice())) list.add(t);
        }
        list.sort(Comparator.comparing(Ticket::getCreatedAt).reversed());
        return list;
    }
}
