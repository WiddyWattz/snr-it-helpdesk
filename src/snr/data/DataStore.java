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
            u.setActive(!Boolean.FALSE.equals(r.get("active")));
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
                    userAs(Reporter.class, str(r, "reporter")), Dates.fromDb(r.get("created_at")));
            t.restore(Status.valueOf(str(r, "status")), userAs(Technician.class, str(r, "assignee")),
                    Dates.fromDb(r.get("assigned_at")), Dates.fromDb(r.get("resolved_at")));
            byId.put(t.getId(), t);
        }
        for (Map<String, Object> r : noteRows) {
            Ticket t = byId.get(num(r, "ticket_id"));
            if (t != null) {
                t.addNote(new RepairNote(userAs(Technician.class, str(r, "technician")), str(r, "note"),
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

    public List<User> getAllUsers() { return new ArrayList<>(users.values()); }

    public List<Technician> getTechnicians() {
        List<Technician> list = new ArrayList<>();
        for (User u : users.values()) {
            if (u instanceof Technician && u.isActive()) list.add((Technician) u);
        }
        return list;
    }

    public void insertUser(User user, String password) throws HelpdeskException {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("username", user.getUsername());
        row.put("password", password);
        row.put("full_name", user.getFullName());
        row.put("role", roleCode(user));
        row.put("active", user.isActive());
        write(() -> db.insert("app_user", row));
        users.put(user.getUsername(), user);
    }

    public void updateUser(User user) throws HelpdeskException {
        Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("active", user.isActive());
        write(() -> { db.update("app_user", "username", user.getUsername(), changes); return null; });
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
        int id = num(write(() -> db.insert("ticket", row)), "id");

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
        write(() -> { db.update("ticket", "id", ticket.getId(), changes); return null; });
    }

    public void insertNote(Ticket ticket, RepairNote note) throws HelpdeskException {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("ticket_id", ticket.getId());
        row.put("technician", note.getTechnician().getUsername());
        row.put("note", note.getNote());
        row.put("created_at", Dates.toDb(note.getDateTime()));
        write(() -> db.insert("repair_note", row));
    }

    public void insertDevice(Device device) throws HelpdeskException {
        write(() -> db.insert("device", deviceRow(device)));
        devices.put(device.getCode(), device);
    }

    public void updateDevice(Device device) throws HelpdeskException {
        write(() -> { db.update("device", "code", device.getCode(), deviceRow(device)); return null; });
    }

    private interface Write<T> { T run() throws HelpdeskException; }

    // If a save fails, reload so the app shows what is really in the database.
    private <T> T write(Write<T> action) throws HelpdeskException {
        try {
            return action.run();
        } catch (HelpdeskException e) {
            try {
                reload();
            } catch (HelpdeskException ignored) {
            }
            throw e;
        }
    }

    private <T extends User> T userAs(Class<T> type, String username) {
        User u = users.get(username);
        return type.isInstance(u) ? type.cast(u) : null;
    }

    private Map<String, Object> deviceRow(Device d) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("code", d.getCode());
        row.put("type", d.getType().name());
        row.put("location", d.getLocation());
        row.put("condition", d.getCondition());
        return row;
    }

    private static String roleCode(User user) {
        if (user instanceof Technician) return "TECHNICIAN";
        if (user instanceof ITAdmin) return "ADMIN";
        return "REPORTER";
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
