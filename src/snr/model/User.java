package snr.model;

public abstract class User {
    private final String username;
    private String password;
    private final String fullName;
    private boolean active = true;

    protected User(String username, String password, String fullName) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
    }

    public void setPassword(String password) { this.password = password; }

    public boolean checkPassword(String attempt) {
        return password.equals(attempt);
    }

    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public abstract String getRole();

    @Override
    public boolean equals(Object o) {
        return o instanceof User && ((User) o).username.equals(username);
    }

    @Override
    public int hashCode() { return username.hashCode(); }

    @Override
    public String toString() { return fullName; }
}
