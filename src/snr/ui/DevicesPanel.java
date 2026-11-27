package snr.ui;

import snr.model.Device;
import snr.model.DeviceType;
import snr.model.HelpdeskException;
import snr.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DevicesPanel extends JPanel {
    private static final String[] COLUMNS = {"Code", "Type", "Location", "Condition", "Tickets"};

    private final AppContext app;
    private final User user;
    private final DefaultTableModel model;
    private final JTable table;
    private List<Device> devices = new ArrayList<>();

    public DevicesPanel(AppContext app, User user) {
        super(new BorderLayout());
        this.app = app;
        this.user = user;

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
        addAction(actions, "Add Device", this::addDevice);
        addAction(actions, "Edit Device", this::editDevice);
        addAction(actions, "Retire Device", this::retireDevice);
        addAction(actions, "Repair History", this::showRepairHistory);

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Theme.CARD);
        body.add(scroll, BorderLayout.CENTER);
        body.add(actions, BorderLayout.SOUTH);
        add(Theme.page("Device Inventory", body), BorderLayout.CENTER);
        refresh();
    }

    private void refresh() {
        devices = app.devices().getAllDevices();
        model.setRowCount(0);
        for (Device d : devices) {
            model.addRow(new Object[]{d.getCode(), d.getType(), d.getLocation(), d.getCondition(),
                    app.devices().getRepairHistory(d).size()});
        }
    }

    private Device getSelectedDevice() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a device first.");
            return null;
        }
        return devices.get(table.convertRowIndexToModel(row));
    }

    private void addDevice() {
        JTextField code = new JTextField();
        JComboBox<DeviceType> type = new JComboBox<>(DeviceType.values());
        JTextField location = new JTextField();
        type.setSelectedIndex(-1);
        Object[] form = {"Device code (e.g. PC-15)", code, "Type", type, "Location (e.g. Lab 3)", location};

        if (JOptionPane.showConfirmDialog(this, form, "Add Device", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        try {
            Device d = app.devices().addDevice(code.getText(), (DeviceType) type.getSelectedItem(), location.getText());
            refresh();
            JOptionPane.showMessageDialog(this, "Device " + d.getCode() + " added.");
        } catch (HelpdeskException e) {
            refresh();
            JOptionPane.showMessageDialog(this, e.getMessage(), "Cannot add device", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void editDevice() {
        Device device = getSelectedDevice();
        if (device == null) return;
        JTextField location = new JTextField(device.getLocation());
        JComboBox<String> condition = new JComboBox<>(new String[]{Device.WORKING, Device.NEEDS_REPAIR});
        condition.setSelectedItem(device.getCondition());
        Object[] form = {"Location", location, "Condition", condition};

        if (JOptionPane.showConfirmDialog(this, form, "Edit " + device.getCode(), JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        try {
            app.devices().updateDevice(device, location.getText(), (String) condition.getSelectedItem());
            refresh();
        } catch (HelpdeskException e) {
            refresh();
            JOptionPane.showMessageDialog(this, e.getMessage(), "Cannot edit device", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void retireDevice() {
        Device device = getSelectedDevice();
        if (device == null) return;
        if (JOptionPane.showConfirmDialog(this, "Retire " + device.getCode() + "? It will no longer appear "
                + "when submitting tickets.", "Retire Device", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            app.devices().retireDevice(device);
            refresh();
        } catch (HelpdeskException e) {
            refresh();
            JOptionPane.showMessageDialog(this, e.getMessage(), "Cannot retire device", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void showRepairHistory() {
        Device device = getSelectedDevice();
        if (device == null) return;
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Repair History",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.add(new TicketsPanel(app, user, "Repair History: " + device,
                () -> app.devices().getRepairHistory(device), TicketsPanel.Mode.VIEW), BorderLayout.CENTER);
        JButton close = Theme.primaryButton("Close");
        close.addActionListener(e -> dialog.dispose());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 24, 12));
        bottom.setBackground(Theme.BACKGROUND);
        bottom.add(close);
        dialog.add(bottom, BorderLayout.SOUTH);
        Theme.closeOnEscape(dialog);
        dialog.setSize(900, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void addAction(JPanel actions, String label, Runnable action) {
        JButton button = Theme.primaryButton(label);
        button.addActionListener(e -> action.run());
        actions.add(button);
        actions.add(Box.createHorizontalStrut(8));
    }
}
