package snr.ui;

import snr.model.HelpdeskException;
import snr.model.Status;
import snr.model.Technician;
import snr.model.Ticket;
import snr.model.User;
import snr.util.Dates;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class TicketsPanel extends JPanel {
    public enum Mode { VIEW, TECHNICIAN, ADMIN }

    private static final String[] COLUMNS =
            {"ID", "Category", "Device", "Description", "Priority", "Status", "Assigned to", "Due"};

    private final AppContext app;
    private final User user;
    private final Supplier<List<Ticket>> source;
    private final DefaultTableModel model;
    private final JTable table;
    private List<Ticket> tickets = new ArrayList<>();

    public TicketsPanel(AppContext app, User user, String title, Supplier<List<Ticket>> source, Mode mode) {
        super(new BorderLayout());
        this.app = app;
        this.user = user;
        this.source = source;

        model = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) showDetails();
            }
        });
        table.setRowHeight(28);
        table.setFont(Theme.BODY);
        table.getTableHeader().setFont(Theme.HEADING.deriveFont(13f));
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(3).setPreferredWidth(220);
        table.getColumnModel().getColumn(7).setPreferredWidth(170);
        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean selected,
                                                           boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, value, selected, focus, row, col);
                if (!selected) {
                    c.setForeground(value.toString().startsWith("OVERDUE") ? Theme.DANGER : Theme.TEXT);
                }
                return c;
            }
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 12));
        actions.setOpaque(false);
        switch (mode) {
            case VIEW -> addAction(actions, "View Details", this::showDetails);
            case TECHNICIAN -> {
                addAction(actions, "Update Status", this::updateStatus);
                addAction(actions, "Add Repair Note", this::addRepairNote);
                addAction(actions, "View Details", this::showDetails);
            }
            case ADMIN -> {
                addAction(actions, "Assign Technician", this::assignTechnician);
                addAction(actions, "View Details", this::showDetails);
            }
        }

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Theme.CARD);
        body.add(scroll, BorderLayout.CENTER);
        body.add(actions, BorderLayout.SOUTH);
        add(Theme.page(title, body), BorderLayout.CENTER);
        refresh();
    }

    public void refresh() {
        tickets = source.get();
        model.setRowCount(0);
        for (Ticket t : tickets) {
            model.addRow(new Object[]{
                    t.getId(), t.getCategory(), t.getDevice(), t.getDescription(), t.getPriority(),
                    t.getStatus(), t.getAssignee() == null ? "Unassigned" : t.getAssignee(),
                    (t.isOverdue() ? "OVERDUE " : "") + Dates.format(t.getDueAt())});
        }
    }

    private Ticket getSelectedTicket() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a ticket first.");
            return null;
        }
        return tickets.get(table.convertRowIndexToModel(row));
    }

    private void showDetails() {
        Ticket ticket = getSelectedTicket();
        if (ticket != null) new TicketDetailsDialog(SwingUtilities.getWindowAncestor(this), ticket).setVisible(true);
    }

    private void assignTechnician() {
        Ticket ticket = getSelectedTicket();
        if (ticket == null) return;
        AssignDialog dialog = new AssignDialog(SwingUtilities.getWindowAncestor(this), app, ticket);
        dialog.setVisible(true);
        if (dialog.isAssigned()) refresh();
    }

    private void updateStatus() {
        Ticket ticket = getSelectedTicket();
        if (ticket == null) return;
        Status next = (Status) JOptionPane.showInputDialog(this,
                "Ticket #" + ticket.getId() + " is " + ticket.getStatus() + ".\nChange status to:",
                "Update Status", JOptionPane.PLAIN_MESSAGE, null, Status.values(), ticket.getStatus());
        if (next == null || next == ticket.getStatus()) return;
        try {
            app.tickets().changeStatus(ticket, (Technician) user, next);
            refresh();
            JOptionPane.showMessageDialog(this, "Ticket #" + ticket.getId() + " is now " + next + ".");
        } catch (HelpdeskException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Cannot update status", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void addRepairNote() {
        Ticket ticket = getSelectedTicket();
        if (ticket == null) return;
        JTextArea area = new JTextArea(5, 30);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        int choice = JOptionPane.showConfirmDialog(this, new JScrollPane(area),
                "Repair note for Ticket #" + ticket.getId(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) return;
        try {
            app.tickets().addNote(ticket, (Technician) user, area.getText());
            JOptionPane.showMessageDialog(this, "Repair note added.");
        } catch (HelpdeskException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Cannot add note", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void addAction(JPanel actions, String label, Runnable action) {
        JButton button = Theme.primaryButton(label);
        button.addActionListener(e -> action.run());
        actions.add(button);
        actions.add(Box.createHorizontalStrut(8));
    }
}
