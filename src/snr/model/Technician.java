package snr.model;

public class Technician extends User {
    public Technician(String username, String password, String fullName) {
        super(username, password, fullName);
    }

    @Override
    public String getRole() { return "Technician"; }
}
