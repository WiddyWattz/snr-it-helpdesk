package snr.ui;

import snr.model.Device;
import snr.model.Status;
import snr.model.User;
import snr.service.DashboardSummary;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class DashboardPanel extends JPanel {
    private static final int TOP_DEVICES = 5;

    private final AppContext app;
    private final JLabel openCount = new JLabel();
    private final JLabel inProgressCount = new JLabel();
    private final JLabel resolvedCount = new JLabel();
    private final JLabel overdueCount = new JLabel();
    private final TicketsPanel overduePanel;
    private final DefaultTableModel topDevicesModel;
    private final Timer timer = new Timer(60_000, e -> refresh());

    public DashboardPanel(AppContext app, User user) {
        super(new BorderLayout());
        this.app = app;

        JPanel cards = new JPanel(new GridLayout(1, 4, 12, 0));
        cards.setOpaque(false);
        cards.add(statCard("Open", openCount, Theme.PRIMARY));
        cards.add(statCard("In Progress", inProgressCount, new Color(0xB7791F)));
        cards.add(statCard("Resolved", resolvedCount, new Color(0x2F855A)));
        cards.add(statCard("Overdue", overdueCount, Theme.DANGER));

        overduePanel = new TicketsPanel(app, user, "Overdue Tickets",
                () -> app.reports().getDashboard().getOverdueTickets(), TicketsPanel.Mode.ADMIN);

        topDevicesModel = new DefaultTableModel(new String[]{"Device", "Tickets"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable topDevices = new JTable(topDevicesModel);
        topDevices.setRowHeight(28);
        topDevices.setFont(Theme.BODY);
        topDevices.getTableHeader().setFont(Theme.HEADING.deriveFont(13f));
        topDevices.getColumnModel().getColumn(1).setMaxWidth(70);
        JScrollPane topScroll = new JScrollPane(topDevices);
        topScroll.getViewport().setBackground(Theme.CARD);

        JLabel topTitle = new JLabel("Most Tickets by Device");
        topTitle.setFont(Theme.HEADING);
        topTitle.setBorder(new EmptyBorder(20, 0, 12, 0));
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setPreferredSize(new Dimension(240, 0));
        topPanel.setBorder(new EmptyBorder(0, 0, 64, 0));
        topPanel.add(topTitle, BorderLayout.NORTH);
        topPanel.add(topScroll, BorderLayout.CENTER);

        JPanel lower = new JPanel(new BorderLayout());
        lower.setOpaque(false);
        lower.add(overduePanel, BorderLayout.CENTER);
        lower.add(topPanel, BorderLayout.EAST);

        JPanel body = new JPanel(new BorderLayout(0, 8));
        body.setOpaque(false);
        body.add(cards, BorderLayout.NORTH);
        body.add(lower, BorderLayout.CENTER);
        add(Theme.page("Dashboard", body), BorderLayout.CENTER);
        refresh();
    }

    private void refresh() {
        DashboardSummary summary = app.reports().getDashboard();
        openCount.setText(String.valueOf(summary.getCount(Status.OPEN)));
        inProgressCount.setText(String.valueOf(summary.getCount(Status.IN_PROGRESS)));
        resolvedCount.setText(String.valueOf(summary.getCount(Status.RESOLVED)));
        overdueCount.setText(String.valueOf(summary.getOverdueCount()));
        overduePanel.refresh();

        topDevicesModel.setRowCount(0);
        List<Map.Entry<Device, Integer>> top = summary.getTopDevices();
        for (int i = 0; i < Math.min(TOP_DEVICES, top.size()); i++) {
            topDevicesModel.addRow(new Object[]{top.get(i).getKey(), top.get(i).getValue()});
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

    private JPanel statCard(String label, JLabel number, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Theme.CARD);
        card.setBorder(new CompoundBorder(new LineBorder(new Color(0xE2E8F0)), new EmptyBorder(14, 16, 14, 16)));
        number.setFont(Theme.BIG_NUMBER);
        number.setForeground(accent);
        JLabel name = new JLabel(label);
        name.setForeground(Theme.MUTED);
        card.add(number, BorderLayout.CENTER);
        card.add(name, BorderLayout.SOUTH);
        return card;
    }
}
