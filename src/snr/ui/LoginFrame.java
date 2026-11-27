package snr.ui;

import snr.model.HelpdeskException;
import snr.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final AppContext app;
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();

    public LoginFrame(AppContext app) {
        super("SnR IT Helpdesk - Log in");
        this.app = app;
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
        c.gridy = 2; form.add(new JLabel("Username"), c);
        c.gridy = 3; form.add(usernameField, c);
        c.gridy = 4; form.add(new JLabel("Password"), c);
        c.gridy = 5; form.add(passwordField, c);

        JButton login = Theme.primaryButton("Log in");
        c.gridy = 6; c.insets = new Insets(20, 0, 0, 0); form.add(login, c);

        login.addActionListener(e -> logIn());
        getRootPane().setDefaultButton(login);
        setContentPane(form);
    }

    private void logIn() {
        try {
            app.reload();
            User user = app.auth().login(usernameField.getText(), new String(passwordField.getPassword()));
            new MainFrame(app, user).setVisible(true);
            dispose();
        } catch (HelpdeskException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Log in failed", JOptionPane.WARNING_MESSAGE);
            passwordField.setText("");
        }
    }
}
