package snr.ui;

import snr.model.RepairNote;
import snr.model.Ticket;
import snr.util.Dates;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TicketDetailsDialog extends JDialog {

    public TicketDetailsDialog(Window owner, Ticket ticket) {
        super(owner, "Ticket #" + ticket.getId(), ModalityType.APPLICATION_MODAL);

        JPanel info = new JPanel(new GridLayout(0, 2, 12, 6));
        info.setOpaque(false);
        info.setAlignmentX(LEFT_ALIGNMENT);
        addField(info, "Category", ticket.getCategory().toString());
        addField(info, "Priority", ticket.getPriority().toString());
        addField(info, "Status", ticket.getStatus().toString());
        addField(info, "Device", ticket.getDevice().toString());
        addField(info, "Reported by", ticket.getReporter().getFullName());
        addField(info, "Assigned to", ticket.getAssignee() == null ? "Unassigned" : ticket.getAssignee().getFullName());
        addField(info, "Submitted", Dates.format(ticket.getCreatedAt()));
        addField(info, "Assigned on", Dates.format(ticket.getAssignedAt()));
        addField(info, "Due", Dates.format(ticket.getDueAt()) + (ticket.isOverdue() ? " (OVERDUE)" : ""));
        addField(info, "Resolved", Dates.format(ticket.getResolvedAt()));

        JTextArea description = readOnlyArea(ticket.getDescription());

        StringBuilder notes = new StringBuilder();
        for (RepairNote n : ticket.getNotes()) {
            notes.append(Dates.format(n.getDateTime())).append(" - ").append((n.getAuthor() == null ? "Unknown" : n.getAuthor().getFullName()))
                    .append(":\n").append(n.getNote()).append("\n\n");
        }
        JTextArea notesArea = readOnlyArea(notes.length() == 0 ? "No repair notes yet." : notes.toString().trim());

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Theme.CARD);
        content.setBorder(new EmptyBorder(16, 20, 16, 20));
        content.add(info);
        content.add(section("Description"));
        content.add(description);
        content.add(section("Repair notes"));
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setAlignmentX(LEFT_ALIGNMENT);
        content.add(notesScroll);

        JButton close = Theme.primaryButton("Close");
        close.addActionListener(e -> dispose());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(Theme.CARD);
        bottom.add(close);

        setLayout(new BorderLayout());
        add(content, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
        Theme.closeOnEscape(this);
        setSize(480, 550);
        setLocationRelativeTo(owner);
    }

    private void addField(JPanel panel, String label, String value) {
        JLabel l = new JLabel(label);
        l.setForeground(Theme.MUTED);
        JLabel v = new JLabel(value);
        v.setFont(Theme.BODY);
        panel.add(l);
        panel.add(v);
    }

    private JLabel section(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.HEADING);
        label.setAlignmentX(LEFT_ALIGNMENT);
        label.setBorder(new EmptyBorder(14, 0, 6, 0));
        return label;
    }

    private JTextArea readOnlyArea(String text) {
        JTextArea area = new JTextArea(text, 3, 30);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(Theme.BODY);
        area.setAlignmentX(LEFT_ALIGNMENT);
        return area;
    }
}
