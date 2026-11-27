package snr.ui;

import snr.model.Status;
import snr.model.User;
import snr.service.DashboardSummary;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class DashboardPanel extends JPanel {

    public DashboardPanel(AppContext app, User user) {
        super(new BorderLayout());
        DashboardSummary summary = app.reports().getDashboard();

        JPanel cards = new JPanel(new GridLayout(1, 4, 12, 0));
        cards.setOpaque(false);
        cards.add(statCard("Open", summary.getCount(Status.OPEN), Theme.PRIMARY));
        cards.add(statCard("In Progress", summary.getCount(Status.IN_PROGRESS), new Color(0xB7791F)));
        cards.add(statCard("Resolved", summary.getCount(Status.RESOLVED), new Color(0x2F855A)));
        cards.add(statCard("Overdue", summary.getOverdueCount(), Theme.DANGER));

        JPanel overduePanel = new TicketsPanel(app, user, "Overdue Tickets",
                () -> app.reports().getDashboard().getOverdueTickets(), TicketsPanel.Mode.ADMIN);

        JPanel body = new JPanel(new BorderLayout(0, 8));
        body.setOpaque(false);
        body.add(cards, BorderLayout.NORTH);
        body.add(overduePanel, BorderLayout.CENTER);
        add(Theme.page("Dashboard", body), BorderLayout.CENTER);
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
