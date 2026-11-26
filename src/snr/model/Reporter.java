package snr.model;

public class Reporter extends User {
    public Reporter(String username, String password, String fullName) {
        super(username, password, fullName);
    }

    @Override
    public String getRole() { return "Reporter"; }
}
