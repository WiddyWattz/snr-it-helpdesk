package snr.data;

import snr.model.*;

import java.time.LocalDateTime;
import java.util.*;

// In-memory for now, replace with Supabase.
public class DataStore {
    private final Map<String, User> users = new LinkedHashMap<>();
    private final Map<String, Device> devices = new LinkedHashMap<>();
    private final List<Ticket> tickets = new ArrayList<>();
    private int nextTicketId = 1;

    public DataStore() {
        seedUsers();
        seedDevices();
        seedTickets();
    }

    public User findUser(String username) { return users.get(username == null ? null : username.trim().toLowerCase()); }
    public Device findDevice(String code) { return devices.get(normalize(code)); }
    public List<Device> getAllDevices() { return new ArrayList<>(devices.values()); }
    public List<Ticket> getAllTickets() { return Collections.unmodifiableList(tickets); }

    public List<Technician> getTechnicians() {
        List<Technician> list = new ArrayList<>();
        for (User u : users.values()) {
            if (u instanceof Technician) list.add((Technician) u);
        }
        return list;
    }

    public void saveDevice(Device device) { devices.put(normalize(device.getCode()), device); }
    public void saveTicket(Ticket ticket) { tickets.add(ticket); }
    public int generateTicketId() { return nextTicketId++; }

    private static String normalize(String key) {
        return key == null ? null : key.trim().toUpperCase();
    }

    private void seedUsers() {
        addUser(new Reporter("ana", "ana123", "Ana Reyes"));
        addUser(new Reporter("santos", "santos123", "Prof. Santos"));
        addUser(new Technician("carlo", "tech123", "Carlo Lim"));
        addUser(new Technician("jessa", "tech123", "Jessa Uy"));
        addUser(new ITAdmin("admin", "admin123", "Admin Cruz"));
    }

    private void addUser(User user) { users.put(user.getUsername(), user); }

    private void seedDevices() {
        saveDevice(new Device("PC-12", DeviceType.PC, "Lab 3", "Working"));
        saveDevice(new Device("PC-07", DeviceType.PC, "Lab 3", "Needs repair"));
        saveDevice(new Device("PRN-01", DeviceType.PRINTER, "Faculty Room", "Working"));
        saveDevice(new Device("RTR-02", DeviceType.ROUTER, "2nd Floor", "Working"));
    }

    private void seedTickets() {
        LocalDateTime now = LocalDateTime.now();
        Reporter ana = (Reporter) users.get("ana");
        Reporter santos = (Reporter) users.get("santos");
        Technician carlo = (Technician) users.get("carlo");
        Technician jessa = (Technician) users.get("jessa");

        Ticket t1 = new HardwareTicket(generateTicketId(), "PC won't turn on", Priority.HIGH,
                findDevice("PC-07"), ana, now.minusHours(6));
        t1.assignTo(carlo, now.minusHours(5));

        Ticket t2 = new NetworkTicket(generateTicketId(), "No internet on 2nd floor", Priority.MEDIUM,
                findDevice("RTR-02"), santos, now.minusHours(3));
        t2.assignTo(jessa, now.minusHours(2));

        Ticket t3 = new SoftwareTicket(generateTicketId(), "NetBeans missing", Priority.LOW,
                findDevice("PC-12"), ana, now.minusHours(1));

        Ticket t4 = new HardwareTicket(generateTicketId(), "Printer paper jam", Priority.MEDIUM,
                findDevice("PRN-01"), santos, now.minusDays(2));
        t4.assignTo(carlo, now.minusDays(2));
        t4.addNote(new RepairNote(carlo, "Removed jammed paper and cleaned rollers.", now.minusDays(1)));

        try {
            t2.moveTo(Status.IN_PROGRESS, now.minusHours(2));
            t4.moveTo(Status.IN_PROGRESS, now.minusDays(2));
            t4.moveTo(Status.RESOLVED, now.minusDays(1));
        } catch (InvalidStatusChangeException e) {
            throw new IllegalStateException("Bad sample data", e);
        }
        tickets.addAll(List.of(t1, t2, t3, t4));
    }
}
