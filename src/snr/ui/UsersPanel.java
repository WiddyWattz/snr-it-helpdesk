package snr.ui;

import snr.model.HelpdeskException;
import snr.model.Technician;
import snr.model.User;
import snr.service.UserService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class UsersPanel extends JPanel {
    private static final String[] COLUMNS = {"Username", "Full name", "Role", "Status", "Active tickets"};

    private final AppContext app;
    private final User admin;
    private final DefaultTableModel model;
    private final JTable table;
    private List<User> users = new ArrayList<>();

    public UsersPanel(AppContext app, User admin) {
        super(new BorderLayout());
        this.app = app;
        this.admin = admin;

        model = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(28);
        table.setFont(Theme.BODY);
        table.getTableHeader().setFont(Theme.HEADING.deriveFont(13f));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 12));
        actions.setOpaque(false);
        addAction(actions, "Add User", this::addUser);
        addAction(actions, "Activate / Deactivate", this::toggleActive);

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Theme.CARD);
        body.add(scroll, BorderLayout.CENTER);
        body.add(actions, BorderLayout.SOUTH);
        add(Theme.page("User Accounts", body), BorderLayout.CENTER);
        refresh();
    }

    private void refresh() {
        users = app.users().getAllUsers();
        model.setRowCount(0);
        for (User u : users) {
            String active = u instanceof Technician ? String.valueOf(app.tickets().countActiveTickets((Technician) u)) : "-";
            model.addRow(new Object[]{u.getUsername(), u.getFullName(), u.getRole(),
                    u.isActive() ? "Active" : "Deactivated", active});
        }
    }

    private void addUser() {
        JTextField username = new JTextField();
        JTextField fullName = new JTextField();
        JComboBox<String> role = new JComboBox<>(UserService.ROLES);
        role.setSelectedIndex(-1);
        JPasswordField password = new JPasswordField();
        Object[] form = {"Username", username, "Full name", fullName, "Role", role,
                "Password (at least 6 characters)", password};

        if (JOptionPane.showConfirmDialog(this, form, "Add User", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        try {
            User u = app.users().addUser(username.getText(), fullName.getText(),
                    (String) role.getSelectedItem(), new String(password.getPassword()));
            refresh();
            JOptionPane.showMessageDialog(this, u.getRole() + " account '" + u.getUsername() + "' created.");
        } catch (HelpdeskException e) {
            refresh();
            JOptionPane.showMessageDialog(this, e.getMessage(), "Cannot add user", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void toggleActive() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a user first.");
            return;
        }
        User user = users.get(table.convertRowIndexToModel(row));
        boolean activate = !user.isActive();
        String action = activate ? "Activate" : "Deactivate";
        if (JOptionPane.showConfirmDialog(this, action + " " + user.getFullName() + "?", action + " User",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            app.users().setActive(user, admin, activate);
            refresh();
        } catch (HelpdeskException e) {
            refresh();
            JOptionPane.showMessageDialog(this, e.getMessage(), "Cannot " + action.toLowerCase() + " user",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void addAction(JPanel actions, String label, Runnable action) {
        JButton button = Theme.primaryButton(label);
        button.addActionListener(e -> action.run());
        actions.add(button);
        actions.add(Box.createHorizontalStrut(8));
    }
}
