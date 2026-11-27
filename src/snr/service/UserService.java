package snr.service;

import snr.data.DataStore;
import snr.model.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class UserService {
    public static final String[] ROLES = {"Store Staff", "Technician", "IT Admin"};

    private final DataStore store;

    public UserService(DataStore store) { this.store = store; }

    public List<User> getAllUsers() {
        List<User> list = store.getAllUsers();
        list.sort(Comparator.comparing(User::getRole).thenComparing(User::getFullName));
        return list;
    }

    // The IT team: admins first, then technicians. Store staff are not included.
    public List<User> getTeam() {
        List<User> team = new ArrayList<>();
        for (User u : getAllUsers()) {
            if (u instanceof ITAdmin || u instanceof Technician) team.add(u);
        }
        team.sort(Comparator.comparing((User u) -> u instanceof Technician).thenComparing(User::getFullName));
        return team;
    }

    public User addUser(String username, String fullName, String role, String password) throws HelpdeskException {
        String name = username == null ? "" : username.trim().toLowerCase();
        if (!name.matches("[a-z0-9_.]{3,20}")) {
            throw new HelpdeskException("Username must be 3-20 letters, numbers, dots or underscores.");
        }
        if (store.findUser(name) != null) throw new HelpdeskException("Username already taken: " + name);
        if (fullName == null || fullName.trim().isEmpty()) throw new HelpdeskException("Enter the full name.");
        if (role == null) throw new HelpdeskException("Choose a role.");
        if (password == null || password.length() < 6) throw new HelpdeskException("Password must be at least 6 characters.");

        User user;
        switch (role) {
            case "Technician": user = new Technician(name, password, fullName.trim()); break;
            case "IT Admin":   user = new ITAdmin(name, password, fullName.trim()); break;
            default:           user = new Reporter(name, password, fullName.trim()); break;
        }
        store.insertUser(user, password);
        return user;
    }

    public void setActive(User user, User admin, boolean active) throws HelpdeskException {
        if (user.equals(admin)) throw new HelpdeskException("You cannot deactivate your own account.");
        if (!active && user instanceof Technician) {
            for (Ticket t : store.getAllTickets()) {
                if (user.equals(t.getAssignee())
                        && (t.getStatus() == Status.OPEN || t.getStatus() == Status.IN_PROGRESS)) {
                    throw new HelpdeskException("Reassign " + user.getFullName() + "'s open tickets first.");
                }
            }
        }
        user.setActive(active);
        store.updateUser(user);
    }
}
