package com.snr.helpdesk.ui;

import com.snr.helpdesk.data.HelpdeskRepository;
import com.snr.helpdesk.model.Role;
import com.snr.helpdesk.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Barebones login: pick a role and a user. Real accounts and passwords come with Supabase. */
public class LoginFrame extends JFrame {
    private final HelpdeskRepository repo;
    private final JComboBox<Role> roleBox = new JComboBox<>(Role.values());
    private final JComboBox<User> userBox = new JComboBox<>();

    public LoginFrame(HelpdeskRepository repo) {
        super("SnR IT Helpdesk - Log in");
        this.repo = repo;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(420, 400);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.CARD);
        form.setBorder(new EmptyBorder(30, 40, 30, 40));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.insets = new Insets(4, 0, 4, 0);

        JLabel title = new JLabel("SnR IT Helpdesk", SwingConstants.CENTER);
        title.setFont(Theme.TITLE);
        title.setForeground(Theme.SIDEBAR);
        JLabel subtitle = new JLabel("Log in to continue", SwingConstants.CENTER);
        subtitle.setForeground(Theme.MUTED);

        c.gridy = 0; form.add(title, c);
        c.gridy = 1; c.insets = new Insets(0, 0, 20, 0); form.add(subtitle, c);
        c.insets = new Insets(4, 0, 4, 0);
        c.gridy = 2; form.add(new JLabel("Role"), c);
        c.gridy = 3; form.add(roleBox, c);
        c.gridy = 4; form.add(new JLabel("User"), c);
        c.gridy = 5; form.add(userBox, c);

        JButton login = Theme.primaryButton("Log in");
        c.gridy = 6; c.insets = new Insets(20, 0, 0, 0); form.add(login, c);

        roleBox.addActionListener(e -> loadUsers());
        login.addActionListener(e -> logIn());
        getRootPane().setDefaultButton(login);
        loadUsers();
        setContentPane(form);
    }

    private void loadUsers() {
        userBox.removeAllItems();
        for (User u : repo.getUsers((Role) roleBox.getSelectedItem())) {
            userBox.addItem(u);
        }
    }

    private void logIn() {
        User user = (User) userBox.getSelectedItem();
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Please select a user.");
            return;
        }
        new MainFrame(repo, user).setVisible(true);
        dispose();
    }
}
