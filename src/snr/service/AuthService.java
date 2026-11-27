package snr.service;

import snr.data.DataStore;
import snr.model.HelpdeskException;
import snr.model.User;

public class AuthService {
    private final DataStore store;

    public AuthService(DataStore store) { this.store = store; }

    public User login(String username, String password) throws HelpdeskException {
        User user = store.findUser(username);
        if (user == null || !user.checkPassword(password)) throw new HelpdeskException("Wrong username or password.");
        if (!user.isActive()) throw new HelpdeskException("This account is deactivated. Ask the IT admin.");
        return user;
    }
}
