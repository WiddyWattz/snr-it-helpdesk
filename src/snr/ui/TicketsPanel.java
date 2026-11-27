package snr.ui;

import snr.model.Category;
import snr.model.HelpdeskException;
import snr.model.Priority;
import snr.model.Status;
import snr.model.Technician;
import snr.model.Ticket;
import snr.model.User;
import snr.util.Dates;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class TicketsPanel extends JPanel {
    public enum Mode { VIEW, REPORTER, TECHNICIAN, ADMIN }

    private static final String[] COLUMNS =
            {"ID", "Category", "Device", "Description", "Priority", "Status", "Assigned to", "Due"};

    private final AppContext app;
    private final User user;
    private final Supplier<List<Ticket>> source;
    private final DefaultTableModel model;
    private final JTable table;
    private final JTextField searchField = new JTextField(12);
    private final JComboBox<Object> statusFilter = filterBox("Any status", Status.values());
    private final JComboBox<Object> categoryFilter = filterBox("Any category", Category.values());
    private final JComboBox<Object> priorityFilter = filterBox("Any priority", Priority.values());
    private final JLabel countLabel = new JLabel();
    private final JPanel filters;
    private final JPanel page;
    private List<Ticket> allTickets = new ArrayList<>();
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
        Theme.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(0).setMaxWidth(90);
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(190);
        table.getColumnModel().getColumn(3).setPreferredWidth(260);
        table.getColumnModel().getColumn(6).setPreferredWidth(130);
        table.getColumnModel().getColumn(7).setPreferredWidth(150);
        table.getColumnModel().getColumn(0).setCellRenderer(new Theme.CellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean selected,
                                                           boolean focus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tbl, value, selected, focus, row, col);
                l.setText(String.format("TKT-%04d", (Integer) value));
                l.setFont(Theme.MONO);
                l.setForeground(Theme.MUTED);
                return l;
            }
        });
        table.getColumnModel().getColumn(6).setCellRenderer(new Theme.CellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean selected,
                                                           boolean focus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tbl, value, selected, focus, row, col);
                boolean none = "Unassigned".equals(String.valueOf(value));
                l.setIcon(none ? null : new Theme.Avatar(String.valueOf(value), 24));
                l.setIconTextGap(8);
                l.setForeground(none ? Theme.MUTED : Theme.TEXT);
                return l;
            }
        });
        table.getColumnModel().getColumn(7).setCellRenderer(new Theme.CellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean selected,
                                                           boolean focus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tbl, value, selected, focus, row, col);
                l.setForeground(value.toString().startsWith("OVERDUE") ? Theme.DANGER : Theme.MUTED);
                return l;
            }
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 12));
        actions.setOpaque(false);
        switch (mode) {
            case VIEW -> addAction(actions, "View Details", this::showDetails);
            case REPORTER -> {
                addAction(actions, "View Details", this::showDetails);
                addAction(actions, "Confirm Fixed", this::closeTicket);
                addAction(actions, "Reopen", this::reopen);
            }
            case TECHNICIAN -> {
                addAction(actions, "Update Status", this::updateStatus);
                addAction(actions, "Add Repair Note", this::addRepairNote);
                addAction(actions, "View Details", this::showDetails);
            }
            case ADMIN -> {
                addAction(actions, "Assign Technician", this::assignTechnician);
                addAction(actions, "Close Ticket", this::closeTicket);
                addAction(actions, "View Details", this::showDetails);
            }
        }

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        filters = filterBar();
        body.add(filters, BorderLayout.NORTH);
        body.add(Theme.tableCard(table), BorderLayout.CENTER);
        body.add(actions, BorderLayout.SOUTH);
        page = Theme.page(title, body);
        add(page, BorderLayout.CENTER);
        refresh();
    }

    // Smaller version for the dashboard: no filter bar, no page padding.
    public TicketsPanel compact() {
        filters.setVisible(false);
        page.setBorder(BorderFactory.createEmptyBorder());
        JLabel heading = (JLabel) page.getComponent(0);
        heading.setFont(Theme.HEADING.deriveFont(15f));
        heading.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        return this;
    }

    public void refresh() {
        allTickets = source.get();
        applyFilters();
    }

    private void applyFilters() {
        String text = searchField.getText().trim().toLowerCase();
        tickets = new ArrayList<>();
        for (Ticket t : allTickets) {
            if (matches(statusFilter, t.getStatus()) && matches(categoryFilter, t.getCategory())
                    && matches(priorityFilter, t.getPriority()) && matchesText(t, text)) {
                tickets.add(t);
            }
        }
        countLabel.setText("Showing " + tickets.size() + " of " + allTickets.size());
        model.setRowCount(0);
        for (Ticket t : tickets) {
            model.addRow(new Object[]{
                    t.getId(), t.getCategory(), t.getDevice(), t.getDescription(), t.getPriority(),
                    t.getStatus(), t.getAssignee() == null ? "Unassigned" : t.getAssignee(),
                    (t.isOverdue() ? "OVERDUE " : "") + Dates.format(t.getDueAt())});
        }
    }

    private static boolean matches(JComboBox<Object> filter, Object value) {
        return filter.getSelectedIndex() == 0 || filter.getSelectedItem() == value;
    }

    private static boolean matchesText(Ticket t, String text) {
        if (text.isEmpty()) return true;
        String haystack = ("#" + t.getId() + " " + t.getDescription() + " " + t.getDevice() + " "
                + t.getCategory() + " " + t.getStatus() + " " + t.getPriority() + " "
                + t.getReporter() + " " + (t.getAssignee() == null ? "unassigned" : t.getAssignee())).toLowerCase();
        return haystack.contains(text);
    }

    private static JComboBox<Object> filterBox(String allLabel, Object[] values) {
        JComboBox<Object> box = new JComboBox<>();
        box.addItem(allLabel);
        for (Object v : values) box.addItem(v);
        return box;
    }

    private JPanel filterBar() {
        searchField.setToolTipText("Search by ID, description, device, category, status or person");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });
        JButton clear = Theme.secondaryButton("Clear");
        clear.addActionListener(e -> {
            searchField.setText("");
            statusFilter.setSelectedIndex(0);
            categoryFilter.setSelectedIndex(0);
            priorityFilter.setSelectedIndex(0);
        });
        countLabel.setForeground(Theme.MUTED);

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        searchField.putClientProperty("JTextField.placeholderText", "Search tickets...");
        bar.add(Theme.muted("Search"));
        bar.add(searchField);
        for (JComboBox<Object> box : List.of(statusFilter, categoryFilter, priorityFilter)) {
            box.setPreferredSize(new Dimension(box.getPreferredSize().width, searchField.getPreferredSize().height));
            box.addActionListener(e -> applyFilters());
            bar.add(box);
        }
        bar.add(clear);
        bar.add(countLabel);
        return bar;
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
        refresh();
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
            refresh();
            JOptionPane.showMessageDialog(this, e.getMessage(), "Cannot update status", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void closeTicket() {
        Ticket ticket = getSelectedTicket();
        if (ticket == null) return;
        if (JOptionPane.showConfirmDialog(this, "Close Ticket #" + ticket.getId() + "? Only do this if the problem is fixed.",
                "Close Ticket", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            app.tickets().closeTicket(ticket, user);
            refresh();
        } catch (HelpdeskException e) {
            refresh();
            JOptionPane.showMessageDialog(this, e.getMessage(), "Cannot close ticket", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void reopen() {
        Ticket ticket = getSelectedTicket();
        if (ticket == null) return;
        JTextArea area = new JTextArea(4, 30);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        Object[] form = {"What is still wrong?", new JScrollPane(area)};
        if (JOptionPane.showConfirmDialog(this, form, "Reopen Ticket #" + ticket.getId(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        try {
            app.tickets().reopen(ticket, user, area.getText());
            refresh();
            JOptionPane.showMessageDialog(this, "Ticket #" + ticket.getId() + " reopened and sent back to "
                    + ticket.getAssignee() + ".");
        } catch (HelpdeskException e) {
            refresh();
            JOptionPane.showMessageDialog(this, e.getMessage(), "Cannot reopen ticket", JOptionPane.WARNING_MESSAGE);
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
        JButton button = actions.getComponentCount() == 0 ? Theme.primaryButton(label) : Theme.secondaryButton(label);
        button.addActionListener(e -> action.run());
        actions.add(button);
        actions.add(Box.createHorizontalStrut(8));
    }
}
