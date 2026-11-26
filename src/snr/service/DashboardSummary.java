package snr.service;

import snr.model.Device;
import snr.model.Status;
import snr.model.Ticket;

import java.util.*;

public class DashboardSummary {
    private final Map<Status, Integer> countByStatus = new EnumMap<>(Status.class);
    private final List<Ticket> overdue = new ArrayList<>();
    private final Map<Device, Integer> ticketsPerDevice = new LinkedHashMap<>();

    public DashboardSummary(List<Ticket> tickets) {
        for (Status s : Status.values()) countByStatus.put(s, 0);
        for (Ticket t : tickets) {
            countByStatus.merge(t.getStatus(), 1, Integer::sum);
            ticketsPerDevice.merge(t.getDevice(), 1, Integer::sum);
            if (t.isOverdue()) overdue.add(t);
        }
    }

    public int getCount(Status status) { return countByStatus.get(status); }
    public int getOverdueCount() { return overdue.size(); }
    public List<Ticket> getOverdueTickets() { return Collections.unmodifiableList(overdue); }

    public List<Map.Entry<Device, Integer>> getTopDevices() {
        List<Map.Entry<Device, Integer>> list = new ArrayList<>(ticketsPerDevice.entrySet());
        list.sort(Map.Entry.<Device, Integer>comparingByValue().reversed());
        return list;
    }
}
