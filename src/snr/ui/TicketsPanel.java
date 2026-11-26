package snr.ui;

import snr.model.Ticket;
import snr.util.Dates;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TicketsPanel extends JPanel {
    public enum Mode { REPORTER, TECHNICIAN, ADMIN }

    private static final String[] COLUMNS =
            {"ID", "Category", "Device", "Description", "Priority", "Status", "Assigned to", "Due"};

    public TicketsPanel(String title, List<Ticket> tickets, Mode mode) {
        super(new BorderLayout());

        DefaultTableModel model = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (Ticket t : tickets) {
            model.addRow(new Object[]{
                    t.getId(), t.getCategory(), t.getDevice(), t.getDescription(), t.getPriority(),
                    t.getStatus(), t.getAssignee() == null ? "Unassigned" : t.getAssignee(),
                    (t.isOverdue() ? "OVERDUE " : "") + Dates.format(t.getDueAt())});
        }

        JTable table = new JTable(model);
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
            case REPORTER -> addAction(actions, "View Details");
            case TECHNICIAN -> {
                addAction(actions, "Update Status");
                addAction(actions, "Add Repair Note");
            }
            case ADMIN -> {
                addAction(actions, "Assign Technician");
                addAction(actions, "View Details");
            }
        }

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Theme.CARD);
        body.add(scroll, BorderLayout.CENTER);
        body.add(actions, BorderLayout.SOUTH);
        add(Theme.page(title, body), BorderLayout.CENTER);
    }

    private void addAction(JPanel actions, String label) {
        JButton button = Theme.primaryButton(label);
        button.addActionListener(e -> Theme.comingSoon(this, label));
        actions.add(button);
        actions.add(Box.createHorizontalStrut(8));
    }
}
