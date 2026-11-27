package snr.data;

import snr.model.*;
import snr.util.Dates;

import java.time.LocalDateTime;
import java.util.*;

// Keeps a copy of the Supabase data in memory; reload() fetches the latest rows.
public class DataStore {
    private final SupabaseClient db;
    private final Map<String, User> users = new LinkedHashMap<>();
    private final Map<String, Device> devices = new LinkedHashMap<>();
    private final List<Ticket> tickets = new ArrayList<>();

    public DataStore() throws HelpdeskException {
        db = new SupabaseClient();
        reload();
    }

    public void reload() throws HelpdeskException {
        List<Map<String, Object>> userRows = db.select("app_user", "select=*&order=username");
        List<Map<String, Object>> deviceRows = db.select("device", "select=*&order=code");
        List<Map<String, Object>> ticketRows = db.select("ticket", "select=*&order=id");
        List<Map<String, Object>> noteRows = db.select("repair_note", "select=*&order=created_at");

        users.clear();
        for (Map<String, Object> r : userRows) {
            User u = toUser(r);
            users.put(u.getUsername(), u);
        }
        devices.clear();
        for (Map<String, Object> r : deviceRows) {
            Device d = new Device(str(r, "code"), DeviceType.valueOf(str(r, "type")), str(r, "location"), str(r, "condition"));
            devices.put(d.getCode(), d);
        }
        Map<Integer, Ticket> byId = new LinkedHashMap<>();
        for (Map<String, Object> r : ticketRows) {
            Ticket t = Ticket.create(Category.valueOf(str(r, "category")), num(r, "id"), str(r, "description"),
                    Priority.valueOf(str(r, "priority")), devices.get(str(r, "device_code")),
                    (Reporter) users.get(str(r, "reporter")), Dates.fromDb(r.get("created_at")));
            t.restore(Status.valueOf(str(r, "status")), (Technician) users.get(str(r, "assignee")),
                    Dates.fromDb(r.get("assigned_at")), Dates.fromDb(r.get("resolved_at")));
            byId.put(t.getId(), t);
        }
        for (Map<String, Object> r : noteRows) {
            Ticket t = byId.get(num(r, "ticket_id"));
            if (t != null) {
                t.addNote(new RepairNote((Technician) users.get(str(r, "technician")), str(r, "note"),
                        Dates.fromDb(r.get("created_at"))));
            }
        }
        tickets.clear();
        tickets.addAll(byId.values());
    }

    public User findUser(String username) { return users.get(username == null ? null : username.trim().toLowerCase()); }
    public Device findDevice(String code) { return devices.get(code == null ? null : code.trim().toUpperCase()); }
    public List<Device> getAllDevices() { return new ArrayList<>(devices.values()); }
    public List<Ticket> getAllTickets() { return Collections.unmodifiableList(tickets); }

    public List<Technician> getTechnicians() {
        List<Technician> list = new ArrayList<>();
        for (User u : users.values()) {
            if (u instanceof Technician) list.add((Technician) u);
        }
        return list;
    }

    public Ticket insertTicket(Category category, Priority priority, Device device, Reporter reporter,
                               String description, LocalDateTime createdAt) throws HelpdeskException {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("category", category.name());
        row.put("priority", priority.name());
        row.put("status", Status.OPEN.name());
        row.put("description", description);
        row.put("device_code", device.getCode());
        row.put("reporter", reporter.getUsername());
        row.put("created_at", Dates.toDb(createdAt));
        int id = num(db.insert("ticket", row), "id");

        Ticket ticket = Ticket.create(category, id, description, priority, device, reporter, createdAt);
        tickets.add(ticket);
        return ticket;
    }

    public void updateTicket(Ticket ticket) throws HelpdeskException {
        Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("status", ticket.getStatus().name());
        changes.put("assignee", ticket.getAssignee() == null ? null : ticket.getAssignee().getUsername());
        changes.put("assigned_at", Dates.toDb(ticket.getAssignedAt()));
        changes.put("resolved_at", Dates.toDb(ticket.getResolvedAt()));
        db.update("ticket", "id", ticket.getId(), changes);
    }

    public void insertNote(Ticket ticket, RepairNote note) throws HelpdeskException {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("ticket_id", ticket.getId());
        row.put("technician", note.getTechnician().getUsername());
        row.put("note", note.getNote());
        row.put("created_at", Dates.toDb(note.getDateTime()));
        db.insert("repair_note", row);
    }

    public void insertDevice(Device device) throws HelpdeskException {
        db.insert("device", deviceRow(device));
        devices.put(device.getCode(), device);
    }

    public void updateDevice(Device device) throws HelpdeskException {
        db.update("device", "code", device.getCode(), deviceRow(device));
    }

    private Map<String, Object> deviceRow(Device d) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("code", d.getCode());
        row.put("type", d.getType().name());
        row.put("location", d.getLocation());
        row.put("condition", d.getCondition());
        return row;
    }

    private User toUser(Map<String, Object> r) {
        String username = str(r, "username"), password = str(r, "password"), name = str(r, "full_name");
        switch (str(r, "role")) {
            case "TECHNICIAN": return new Technician(username, password, name);
            case "ADMIN": return new ITAdmin(username, password, name);
            default: return new Reporter(username, password, name);
        }
    }

    private static String str(Map<String, Object> row, String column) {
        Object v = row.get(column);
        return v == null ? null : v.toString();
    }

    private static int num(Map<String, Object> row, String column) {
        return ((Number) row.get(column)).intValue();
    }
}
