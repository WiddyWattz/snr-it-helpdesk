package snr.ui;

import snr.model.HelpdeskException;
import snr.model.Technician;
import snr.model.Ticket;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AssignDialog extends JDialog {
    private final AppContext app;
    private final Ticket ticket;
    private final JComboBox<Technician> technicianBox;
    private boolean assigned;

    public AssignDialog(Window owner, AppContext app, Ticket ticket) {
        super(owner, "Assign Ticket #" + ticket.getId(), ModalityType.APPLICATION_MODAL);
        this.app = app;
        this.ticket = ticket;

        technicianBox = new JComboBox<>(app.tickets().getTechnicians().toArray(new Technician[0]));
        technicianBox.setSelectedItem(ticket.getAssignee());
        if (ticket.getAssignee() == null) technicianBox.setSelectedIndex(-1);
        technicianBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                if (value instanceof Technician t) {
                    setText(t.getFullName() + "  (" + app.tickets().countActiveTickets(t) + " active)");
                }
                return this;
            }
        });

        JPanel content = new JPanel(new GridLayout(0, 1, 0, 6));
        content.setBackground(Theme.CARD);
        content.setBorder(new EmptyBorder(16, 20, 8, 20));
        content.add(label(ticket.getCategory() + " · " + ticket.getPriority() + " priority · " + ticket.getDevice()));
        content.add(label(ticket.getDescription()));
        content.add(label("Currently: " + (ticket.getAssignee() == null ? "Unassigned" : ticket.getAssignee())));
        content.add(new JLabel("Assign to"));
        content.add(technicianBox);

        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dispose());
        JButton assign = Theme.primaryButton("Assign");
        assign.addActionListener(e -> assign());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setBackground(Theme.CARD);
        buttons.add(cancel);
        buttons.add(assign);

        setLayout(new BorderLayout());
        add(content, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(assign);
        setSize(420, 300);
        setLocationRelativeTo(owner);
    }

    public boolean isAssigned() { return assigned; }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.BODY);
        return label;
    }

    private void assign() {
        Technician technician = (Technician) technicianBox.getSelectedItem();
        try {
            app.tickets().assign(ticket, technician);
            assigned = true;
            JOptionPane.showMessageDialog(this, "Ticket #" + ticket.getId() + " assigned to "
                    + technician.getFullName() + ".");
            dispose();
        } catch (HelpdeskException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Cannot assign", JOptionPane.WARNING_MESSAGE);
        }
    }
}
