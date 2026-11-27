package snr.ui;

import snr.model.ITAdmin;
import snr.model.Technician;
import snr.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class TeamPanel extends JPanel {
    private static final int FULL_LOAD = 5;

    private final AppContext app;

    public TeamPanel(AppContext app) {
        super(new BorderLayout());
        this.app = app;
        List<User> team = app.users().getTeam();

        long techs = team.stream().filter(u -> u instanceof Technician).count();
        long available = team.stream().filter(u -> u instanceof Technician && u.isActive()).count();
        JLabel summary = Theme.muted(team.size() + " members · " + techs + " technicians · "
                + available + " available for tickets");
        summary.setFont(Theme.BODY);
        summary.setBorder(new EmptyBorder(0, 0, 14, 0));

        JPanel grid = new JPanel(new GridLayout(0, 3, 14, 14));
        grid.setOpaque(false);
        for (User u : team) grid.add(memberCard(u));

        JPanel column = new Theme.WidthTrackingPanel();
        column.setLayout(new BorderLayout());
        column.add(summary, BorderLayout.NORTH);
        column.add(grid, BorderLayout.CENTER);
        JScrollPane scroll = new JScrollPane(column);
        scroll.getViewport().setBackground(Theme.BACKGROUND);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(Theme.page("IT Team", scroll), BorderLayout.CENTER);
    }

    private JComponent memberCard(User u) {
        JLabel name = new JLabel("<html><b>" + u.getFullName() + "</b><br><span style='color:#8B92A3'>@"
                + u.getUsername() + "</span></html>");
        name.setFont(Theme.BODY.deriveFont(14f));
        name.setIcon(new Theme.Avatar(u.getFullName(), 44));
        name.setIconTextGap(12);

        JPanel pills = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pills.setOpaque(false);
        pills.add(new Theme.Pill(u.getRole(), u instanceof ITAdmin ? Theme.PRIMARY : Theme.PURPLE, Theme.CARD).inset(0));
        pills.add(new Theme.Pill(u.isActive() ? "Active" : "Deactivated", u.isActive() ? Theme.SUCCESS : Theme.GRAY, Theme.CARD).inset(8));

        JPanel top = new JPanel(new BorderLayout(0, 10));
        top.setOpaque(false);
        top.add(name, BorderLayout.NORTH);
        top.add(pills, BorderLayout.CENTER);

        Theme.Card card = new Theme.Card(new BorderLayout(0, 14))
                .accent(u instanceof ITAdmin ? Theme.PRIMARY : Theme.PURPLE);
        card.add(top, BorderLayout.NORTH);
        card.add(u instanceof Technician ? workload((Technician) u) : adminNote(), BorderLayout.CENTER);
        return card;
    }

    private JComponent workload(Technician t) {
        int active = app.tickets().countActiveTickets(t);
        int resolved = app.tickets().countResolvedTickets(t);
        int overdue = app.tickets().countOverdueTickets(t);

        JPanel stats = new JPanel(new GridLayout(1, 3, 8, 0));
        stats.setOpaque(false);
        stats.add(stat("Active", active, Theme.TEXT));
        stats.add(stat("Resolved", resolved, Theme.SUCCESS));
        stats.add(stat("Overdue", overdue, overdue > 0 ? Theme.DANGER : Theme.TEXT));

        JLabel loadLabel = Theme.muted("Workload · " + active + " of " + FULL_LOAD + " tickets");
        JPanel bottom = new JPanel(new BorderLayout(0, 6));
        bottom.setOpaque(false);
        bottom.add(loadLabel, BorderLayout.NORTH);
        bottom.add(new LoadBar(Math.min(1.0, active / (double) FULL_LOAD)), BorderLayout.CENTER);

        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);
        p.add(stats, BorderLayout.NORTH);
        p.add(bottom, BorderLayout.CENTER);
        return p;
    }

    private JComponent adminNote() {
        JLabel note = new JLabel("<html><span style='color:#8B92A3'>Assigns tickets, manages devices, "
                + "user accounts and the IT team.</span></html>");
        note.setFont(Theme.BODY);
        note.setVerticalAlignment(SwingConstants.TOP);
        return note;
    }

    private JComponent stat(String label, int value, Color color) {
        JLabel number = new JLabel(String.valueOf(value));
        number.setFont(Theme.BIG_NUMBER.deriveFont(22f));
        number.setForeground(color);
        JPanel tile = new Theme.Card(new BorderLayout()).fill(Theme.CARD_ALT);
        tile.setBorder(new EmptyBorder(8, 12, 8, 12));
        tile.add(number, BorderLayout.CENTER);
        tile.add(Theme.muted(label), BorderLayout.SOUTH);
        return tile;
    }

    // Horizontal bar: green when light, amber when busy, red when full.
    private static class LoadBar extends JComponent {
        private final double value;

        LoadBar(double value) {
            this.value = value;
            setPreferredSize(new Dimension(100, 8));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Theme.CARD_ALT);
            g2.fillRoundRect(0, 0, getWidth(), 8, 8, 8);
            g2.setColor(value >= 0.8 ? Theme.DANGER : value >= 0.5 ? Theme.WARNING : Theme.SUCCESS);
            g2.fillRoundRect(0, 0, (int) (getWidth() * value), 8, 8, 8);
            g2.dispose();
        }
    }
}
