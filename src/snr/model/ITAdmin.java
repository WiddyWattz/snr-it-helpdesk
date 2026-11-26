package snr.model;

public class ITAdmin extends User {
    public ITAdmin(String username, String password, String fullName) {
        super(username, password, fullName);
    }

    @Override
    public String getRole() { return "IT Admin"; }
}
