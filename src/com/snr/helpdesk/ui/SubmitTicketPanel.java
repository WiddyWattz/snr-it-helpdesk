package com.snr.helpdesk.ui;

import com.snr.helpdesk.data.HelpdeskRepository;
import com.snr.helpdesk.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;

/** Form for a reporter to submit a new ticket. */
public class SubmitTicketPanel extends JPanel {
    private final HelpdeskRepository repo;
    private final User reporter;
    private final JComboBox<Category> categoryBox = new JComboBox<>(Category.values());
    private final JComboBox<Priority> priorityBox = new JComboBox<>(Priority.values());
    private final JComboBox<Device> deviceBox;
    private final JTextArea descriptionArea = new JTextArea(5, 30);

    public SubmitTicketPanel(HelpdeskRepository repo, User reporter) {
        super(new BorderLayout());
        this.repo = repo;
        this.reporter = reporter;
        deviceBox = new JComboBox<>(repo.getDevices().toArray(new Device[0]));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setFont(Theme.BODY);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.CARD);
        form.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        addRow(form, c, 0, "Category", categoryBox);
        addRow(form, c, 1, "Priority", priorityBox);
        addRow(form, c, 2, "Device", deviceBox);
        addRow(form, c, 3, "Description", new JScrollPane(descriptionArea));

        JButton submit = Theme.primaryButton("Submit Ticket");
        submit.addActionListener(e -> submit());
        c.gridx = 1; c.gridy = 4; c.fill = GridBagConstraints.NONE;
        form.add(submit, c);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(form, BorderLayout.NORTH);
        add(Theme.page("Submit a Ticket", wrapper), BorderLayout.CENTER);
    }

    private void addRow(JPanel form, GridBagConstraints c, int row, String label, JComponent field) {
        c.gridx = 0; c.gridy = row; c.weightx = 0;
        form.add(new JLabel(label), c);
        c.gridx = 1; c.weightx = 1;
        form.add(field, c);
    }

    private void submit() {
        String description = descriptionArea.getText().trim();
        Device device = (Device) deviceBox.getSelectedItem();
        if (description.isEmpty() || device == null) {
            JOptionPane.showMessageDialog(this, "Please choose a device and describe the problem.",
                    "Missing information", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = repo.nextTicketId();
        Priority priority = (Priority) priorityBox.getSelectedItem();
        LocalDateTime now = LocalDateTime.now();
        Ticket ticket = switch ((Category) categoryBox.getSelectedItem()) {
            case HARDWARE -> new HardwareTicket(id, description, priority, device, reporter, now);
            case SOFTWARE -> new SoftwareTicket(id, description, priority, device, reporter, now);
            case NETWORK -> new NetworkTicket(id, description, priority, device, reporter, now);
        };
        repo.addTicket(ticket);
        descriptionArea.setText("");
        JOptionPane.showMessageDialog(this, "Ticket #" + id + " submitted. Status: Open.");
    }
}
