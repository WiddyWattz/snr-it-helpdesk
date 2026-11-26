package com.snr.helpdesk.ui;

import com.snr.helpdesk.data.HelpdeskRepository;
import com.snr.helpdesk.model.Status;
import com.snr.helpdesk.model.Ticket;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

/** Admin overview: ticket counts by status and the list of overdue tickets. */
public class DashboardPanel extends JPanel {

    public DashboardPanel(HelpdeskRepository repo) {
        super(new BorderLayout());
        List<Ticket> tickets = repo.getTickets();

        JPanel cards = new JPanel(new GridLayout(1, 4, 12, 0));
        cards.setOpaque(false);
        cards.add(statCard("Open", count(tickets, Status.OPEN), Theme.PRIMARY));
        cards.add(statCard("In Progress", count(tickets, Status.IN_PROGRESS), new Color(0xB7791F)));
        cards.add(statCard("Resolved", count(tickets, Status.RESOLVED), new Color(0x2F855A)));
        cards.add(statCard("Overdue", tickets.stream().filter(Ticket::isOverdue).count(), Theme.DANGER));

        List<Ticket> overdue = tickets.stream().filter(Ticket::isOverdue).toList();
        JPanel overduePanel = new TicketsPanel("Overdue Tickets", overdue, TicketsPanel.Mode.ADMIN);

        JPanel body = new JPanel(new BorderLayout(0, 8));
        body.setOpaque(false);
        body.add(cards, BorderLayout.NORTH);
        body.add(overduePanel, BorderLayout.CENTER);
        add(Theme.page("Dashboard", body), BorderLayout.CENTER);
    }

    private long count(List<Ticket> tickets, Status status) {
        return tickets.stream().filter(t -> t.getStatus() == status).count();
    }

    private JPanel statCard(String label, long value, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Theme.CARD);
        card.setBorder(new CompoundBorder(new LineBorder(new Color(0xE2E8F0)), new EmptyBorder(14, 16, 14, 16)));
        JLabel number = new JLabel(String.valueOf(value));
        number.setFont(Theme.BIG_NUMBER);
        number.setForeground(accent);
        JLabel name = new JLabel(label);
        name.setForeground(Theme.MUTED);
        card.add(number, BorderLayout.CENTER);
        card.add(name, BorderLayout.SOUTH);
        return card;
    }
}
