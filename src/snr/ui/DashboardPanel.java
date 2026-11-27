package snr.ui;

import snr.model.Category;
import snr.model.Device;
import snr.model.HelpdeskException;
import snr.model.Priority;
import snr.model.Status;
import snr.model.User;
import snr.service.DashboardSummary;
import snr.util.Dates;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class DashboardPanel extends JPanel {
    private static final int TOP_DEVICES = 5;

    private final AppContext app;
    private final User user;
    private final JPanel column = new Theme.WidthTrackingPanel();
    private final Timer timer = new Timer(60_000, e -> reloadAndRefresh());

    public DashboardPanel(AppContext app, User user) {
        super(new BorderLayout());
        this.app = app;
        this.user = user;
        JScrollPane scroll = new JScrollPane(column);
        scroll.getViewport().setBackground(Theme.BACKGROUND);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(Theme.page("Command Center", scroll), BorderLayout.CENTER);
        refresh();
    }

    private void refresh() {
        DashboardSummary s = app.reports().getDashboard();
        column.removeAll();
        add(column, hero(s));
        add(column, statusRow(s));
        add(column, row(priorityCard(s), categoryCard(s)));
        add(column, overdueCard());
        add(column, row(activityCard(s), devicesCard(s)));
        column.revalidate();
        column.repaint();
    }

    private void add(JPanel column, JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
        column.add(c);
        column.add(Box.createVerticalStrut(14));
    }

    private void reloadAndRefresh() {
        try {
            app.reload();
            refresh();
        } catch (HelpdeskException ignored) {
            // keep showing the last loaded data until the connection is back
        }
    }

    // Re-check overdue tickets every minute while the dashboard is on screen.
    @Override
    public void addNotify() {
        super.addNotify();
        timer.start();
    }

    @Override
    public void removeNotify() {
        timer.stop();
        super.removeNotify();
    }

    private JComponent hero(DashboardSummary s) {
        JLabel live = new JLabel("<html><span style='color:#22C55E;font-size:14px'>●</span>&nbsp; <b>Store IT Live</b></html>");
        live.setFont(Theme.HEADING.deriveFont(16f));
        JLabel health = new JLabel(s.getOnTimePercent() + "% on time");
        health.setFont(Theme.SMALL);
        health.setForeground(Theme.MUTED);
        JPanel left = new JPanel(new GridLayout(2, 1, 0, 4));
        left.setOpaque(false);
        left.add(live);
        left.add(health);

        JPanel stats = new JPanel(new GridLayout(1, 5, 18, 0));
        stats.setOpaque(false);
        stats.add(bigStat("OPEN TICKETS", s.getCount(Status.OPEN), Theme.TEXT));
        stats.add(bigStat("IN PROGRESS", s.getCount(Status.IN_PROGRESS), Theme.TEXT));
        stats.add(bigStat("OVERDUE", s.getOverdueCount(), s.getOverdueCount() > 0 ? Theme.DANGER : Theme.TEXT));
        stats.add(bigStat("TECHNICIANS", app.tickets().getTechnicians().size(), Theme.TEXT));
        stats.add(bigStat("ON-TIME", s.getOnTimePercent() + "%", Theme.SUCCESS));

        Theme.Card card = new Theme.Card(new BorderLayout(30, 0));
        card.setBorder(new EmptyBorder(20, 24, 20, 24));
        card.add(left, BorderLayout.WEST);
        card.add(stats, BorderLayout.CENTER);
        return card;
    }

    private JComponent bigStat(String label, Object value, Color color) {
        JLabel name = Theme.muted(label);
        name.setFont(Theme.LABEL);
        JLabel number = new JLabel(String.valueOf(value));
        number.setFont(Theme.BIG_NUMBER);
        number.setForeground(color);
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.add(name, BorderLayout.NORTH);
        p.add(number, BorderLayout.CENTER);
        return p;
    }

    private JComponent statusRow(DashboardSummary s) {
        JPanel row = new JPanel(new GridLayout(1, 5, 12, 0));
        row.setOpaque(false);
        row.add(accentTile("Open", s.getCount(Status.OPEN), Theme.INFO));
        row.add(accentTile("In Progress", s.getCount(Status.IN_PROGRESS), Theme.PURPLE));
        row.add(accentTile("Resolved", s.getCount(Status.RESOLVED), Theme.SUCCESS));
        row.add(accentTile("Closed", s.getCount(Status.CLOSED), Theme.GRAY));
        row.add(accentTile("Overdue", s.getOverdueCount(), Theme.DANGER));
        return row;
    }

    private JComponent accentTile(String label, int value, Color accent) {
        JLabel number = new JLabel(String.valueOf(value));
        number.setFont(Theme.BIG_NUMBER.deriveFont(24f));
        Theme.Card tile = new Theme.Card(new BorderLayout(0, 2)).accent(accent).fill(Theme.CARD_ALT);
        tile.setBorder(new EmptyBorder(12, 16, 12, 12));
        tile.add(number, BorderLayout.CENTER);
        tile.add(Theme.muted(label), BorderLayout.SOUTH);
        return tile;
    }

    private JComponent row(JComponent left, JComponent right) {
        JPanel row = new JPanel(new GridLayout(1, 2, 14, 0));
        row.setOpaque(false);
        row.add(left);
        row.add(right);
        return row;
    }

    private Theme.Card titledCard(String title, JComponent body) {
        JLabel heading = new JLabel(title);
        heading.setFont(Theme.HEADING.deriveFont(15f));
        heading.setBorder(new EmptyBorder(0, 0, 12, 0));
        Theme.Card card = new Theme.Card(new BorderLayout());
        card.add(heading, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JComponent priorityCard(DashboardSummary s) {
        JPanel grid = new JPanel(new GridLayout(2, 2, 10, 10));
        grid.setOpaque(false);
        grid.add(accentTile("High", s.getActiveCount(Priority.HIGH), Theme.DANGER));
        grid.add(accentTile("Medium", s.getActiveCount(Priority.MEDIUM), Theme.WARNING));
        grid.add(accentTile("Low", s.getActiveCount(Priority.LOW), Theme.INFO));
        grid.add(accentTile("Unassigned", s.getUnassignedCount(), Theme.GRAY));
        return titledCard("Active Tickets by Priority", grid);
    }

    private JComponent categoryCard(DashboardSummary s) {
        Category[] cats = Category.values();
        Color[] colors = {Theme.INFO, Theme.PURPLE, Theme.SUCCESS};
        int[] values = new int[cats.length];
        JPanel legend = new JPanel(new GridLayout(cats.length, 1, 0, 8));
        legend.setOpaque(false);
        for (int i = 0; i < cats.length; i++) {
            values[i] = s.getCount(cats[i]);
            int pct = s.getTotal() == 0 ? 0 : Math.round(100f * values[i] / s.getTotal());
            String hex = String.format("#%06X", colors[i].getRGB() & 0xFFFFFF);
            JLabel l = new JLabel("<html><span style='color:" + hex + "'>●</span>&nbsp; " + cats[i]
                    + " &nbsp;<b>" + pct + "%</b></html>");
            l.setFont(Theme.BODY);
            legend.add(l);
        }
        JPanel body = new JPanel(new BorderLayout(24, 0));
        body.setOpaque(false);
        body.add(new Donut(values, colors, s.getTotal()), BorderLayout.WEST);
        body.add(legend, BorderLayout.CENTER);
        return titledCard("Category Mix", body);
    }

    private JComponent overdueCard() {
        TicketsPanel overdue = new TicketsPanel(app, user, "Overdue Tickets",
                () -> app.reports().getDashboard().getOverdueTickets(), TicketsPanel.Mode.ADMIN).compact();
        overdue.setPreferredSize(new Dimension(600, 260));
        return overdue;
    }

    private JComponent devicesCard(DashboardSummary s) {
        JPanel list = new JPanel(new GridLayout(TOP_DEVICES, 1, 0, 6));
        list.setOpaque(false);
        List<Map.Entry<Device, Integer>> top = s.getTopDevices();
        for (int i = 0; i < Math.min(TOP_DEVICES, top.size()); i++) {
            Device d = top.get(i).getKey();
            JLabel name = new JLabel("<html><b>" + d.getCode() + "</b> &nbsp;<span style='color:#8B92A3'>"
                    + d.getType() + " · " + d.getLocation() + "</span></html>");
            name.setFont(Theme.BODY);
            int n = top.get(i).getValue();
            JLabel count = new JLabel(n + (n == 1 ? " ticket" : " tickets"));
            count.setFont(Theme.MONO);
            count.setForeground(Theme.MUTED);
            JPanel item = new JPanel(new BorderLayout());
            item.setOpaque(false);
            item.add(name, BorderLayout.CENTER);
            item.add(count, BorderLayout.EAST);
            list.add(item);
        }
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(0, 0, 0, 0));
        wrap.add(titledCard("Most Tickets by Device", list), BorderLayout.NORTH);
        return wrap;
    }

    private JComponent activityCard(DashboardSummary s) {
        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setOpaque(false);
        for (DashboardSummary.Activity a : s.getRecentActivity(6)) {
            Color c = switch (a.tag()) {
                case "Created" -> Theme.INFO;
                case "Assigned" -> Theme.PURPLE;
                case "Resolved" -> Theme.SUCCESS;
                case "Reopened" -> Theme.DANGER;
                default -> Theme.WARNING;
            };
            String hex = String.format("#%06X", c.getRGB() & 0xFFFFFF);
            JLabel text = new JLabel("<html><span style='color:" + hex + "'>●</span>&nbsp; " + escape(a.text())
                    + "<br><span style='color:#8B92A3;font-family:monospace'>&nbsp;&nbsp;&nbsp;&nbsp;"
                    + String.format("TKT-%04d", a.ticketId()) + " · " + Dates.format(a.time()) + "</span></html>");
            text.setFont(Theme.BODY);
            JPanel item = new JPanel(new BorderLayout(10, 0));
            item.setOpaque(false);
            item.setBorder(new EmptyBorder(6, 0, 6, 0));
            item.add(text, BorderLayout.CENTER);
            Theme.Pill pill = new Theme.Pill(a.tag(), c, Theme.CARD);
            item.add(pill, BorderLayout.EAST);
            item.setAlignmentX(Component.LEFT_ALIGNMENT);
            list.add(item);
        }
        return titledCard("Recent Activity", list);
    }

    private static String escape(String s) {
        String t = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        return t.length() > 70 ? t.substring(0, 67) + "..." : t;
    }

    // Ring chart: one arc per value, total in the middle.
    private static class Donut extends JComponent {
        private final int[] values;
        private final Color[] colors;
        private final int total;

        Donut(int[] values, Color[] colors, int total) {
            this.values = values;
            this.colors = colors;
            this.total = total;
            setPreferredSize(new Dimension(150, 150));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int size = Math.min(getWidth(), getHeight()) - 8, x = 4, y = 4;
            g2.setStroke(new BasicStroke(18f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
            g2.setColor(Theme.CARD_ALT);
            g2.drawOval(x + 9, y + 9, size - 18, size - 18);
            double start = 90;
            for (int i = 0; i < values.length && total > 0; i++) {
                double extent = -360.0 * values[i] / total;
                g2.setColor(colors[i]);
                g2.drawArc(x + 9, y + 9, size - 18, size - 18, (int) Math.round(start), (int) Math.round(extent));
                start += extent;
            }
            g2.setColor(Theme.TEXT);
            g2.setFont(Theme.BIG_NUMBER.deriveFont(24f));
            String n = String.valueOf(total);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(n, x + (size - fm.stringWidth(n)) / 2, y + size / 2 + 4);
            g2.setFont(Theme.SMALL);
            g2.setColor(Theme.MUTED);
            fm = g2.getFontMetrics();
            g2.drawString("Total", x + (size - fm.stringWidth("Total")) / 2, y + size / 2 + 20);
            g2.dispose();
        }
    }
}
