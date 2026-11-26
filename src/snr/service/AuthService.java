package snr.service;

import snr.data.DataStore;
import snr.model.User;

public class AuthService {
    private final DataStore store;

    public AuthService(DataStore store) { this.store = store; }

    public User login(String username, String password) {
        User user = store.findUser(username);
        return (user != null && user.checkPassword(password)) ? user : null;
    }
}
