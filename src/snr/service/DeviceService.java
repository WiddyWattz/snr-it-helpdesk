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

    public Device addDevice(String code, DeviceType type, String location) throws HelpdeskException {
        if (code == null || code.trim().isEmpty()) throw new HelpdeskException("Enter a device code.");
        if (type == null) throw new HelpdeskException("Choose a device type.");
        if (location == null || location.trim().isEmpty()) throw new HelpdeskException("Enter a location.");
        if (store.findDevice(code) != null) throw new HelpdeskException("Device code already exists: " + code.trim());

        Device device = new Device(code.trim().toUpperCase(), type, location.trim(), "Working");
        store.saveDevice(device);
        return device;
    }

    public List<Ticket> getRepairHistory(Device device) {
        List<Ticket> list = new ArrayList<>();
        for (Ticket t : store.getAllTickets()) {
            if (t.getDevice() == device) list.add(t);
        }
        list.sort(Comparator.comparing(Ticket::getCreatedAt).reversed());
        return list;
    }
}
