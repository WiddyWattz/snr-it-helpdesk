package snr.model;

public abstract class User {
    private final String username;
    private final String password;
    private final String fullName;

    protected User(String username, String password, String fullName) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
    }

    public boolean checkPassword(String attempt) {
        return password.equals(attempt);
    }

    public String getUsername() { return username; }
    public String getFullName() { return fullName; }

    public abstract String getRole();

    @Override
    public String toString() { return fullName; }
}
