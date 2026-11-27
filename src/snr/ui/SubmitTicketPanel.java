package snr.ui;

import snr.model.*;
import snr.util.Dates;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SubmitTicketPanel extends JPanel {
    private final AppContext app;
    private final Reporter reporter;
    private final JComboBox<Category> categoryBox = new JComboBox<>(Category.values());
    private final JComboBox<Priority> priorityBox = new JComboBox<>(Priority.values());
    private final JComboBox<Device> deviceBox;
    private final JTextArea descriptionArea = new JTextArea(5, 30);

    public SubmitTicketPanel(AppContext app, Reporter reporter) {
        super(new BorderLayout());
        this.app = app;
        this.reporter = reporter;
        deviceBox = new JComboBox<>(app.devices().getAllDevices().toArray(new Device[0]));
        clearForm();
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

    private void clearForm() {
        categoryBox.setSelectedIndex(-1);
        priorityBox.setSelectedIndex(-1);
        deviceBox.setSelectedIndex(-1);
        descriptionArea.setText("");
    }

    private void addRow(JPanel form, GridBagConstraints c, int row, String label, JComponent field) {
        c.gridx = 0; c.gridy = row; c.weightx = 0;
        form.add(new JLabel(label), c);
        c.gridx = 1; c.weightx = 1;
        form.add(field, c);
    }

    private void submit() {
        try {
            Ticket ticket = app.tickets().submit(reporter, (Category) categoryBox.getSelectedItem(),
                    (Priority) priorityBox.getSelectedItem(), (Device) deviceBox.getSelectedItem(),
                    descriptionArea.getText());
            clearForm();
            JOptionPane.showMessageDialog(this, "Ticket #" + ticket.getId() + " submitted.\n"
                    + "Status: " + ticket.getStatus() + "\n"
                    + "Due: " + Dates.format(ticket.getDueAt()) + "\n\n"
                    + "You can track it in My Tickets.", "Ticket submitted", JOptionPane.INFORMATION_MESSAGE);
        } catch (HelpdeskException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Missing information", JOptionPane.WARNING_MESSAGE);
        }
    }
}
