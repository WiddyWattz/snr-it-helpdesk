package snr.ui;

import snr.model.HelpdeskException;
import snr.model.ITAdmin;
import snr.model.Reporter;
import snr.model.Status;
import snr.model.Technician;
import snr.model.Ticket;
import snr.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class MainFrame extends JFrame {
    private final AppContext app;
    private final User user;
    private final JPanel content = new JPanel(new BorderLayout());
    private final JLabel crumb = new JLabel();
    private final List<NavItem> navItems = new ArrayList<>();

    public MainFrame(AppContext app, User user) {
        super("SnR Warehouse Club IT Helpdesk - " + user.getRole());
        this.app = app;
        this.user = user;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1320, 780);
        setMinimumSize(new Dimension(1000, 620));
        setLocationRelativeTo(null);
        build();
        navItems.get(0).doClick();
    }

    // Creates the sidebar and page area; called again after a theme change.
    private void build() {
        getContentPane().removeAll();
        navItems.clear();
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setOpaque(false);

        if (user instanceof Reporter) {
            section(nav, "NAVIGATION");
            addNav(nav, "Submit Ticket", null, () -> new SubmitTicketPanel(app, (Reporter) user));
            addNav(nav, "My Tickets", () -> countActive(app.tickets().getTicketsFor(user)),
                    () -> new TicketsPanel(app, user, "My Tickets",
                            () -> app.tickets().getTicketsFor(user), TicketsPanel.Mode.REPORTER));
        } else if (user instanceof ITAdmin) {
            section(nav, "NAVIGATION");
            addNav(nav, "Dashboard", null, () -> new DashboardPanel(app, user));
            addNav(nav, "Unassigned", () -> app.tickets().getUnassigned().size(),
                    () -> new TicketsPanel(app, user, "Unassigned Tickets",
                            () -> app.tickets().getUnassigned(), TicketsPanel.Mode.ADMIN));
            addNav(nav, "All Tickets", () -> countActive(app.tickets().getTicketsFor(user)),
                    () -> new TicketsPanel(app, user, "All Tickets",
                            () -> app.tickets().getTicketsFor(user), TicketsPanel.Mode.ADMIN));
            section(nav, "MANAGEMENT");
            addNav(nav, "Devices", () -> app.devices().getActiveDevices().size(), () -> new DevicesPanel(app, user));
            addNav(nav, "Team", () -> app.tickets().getTechnicians().size(), () -> new TeamPanel(app));
            addNav(nav, "Users", null, () -> new UsersPanel(app, user));
        } else {
            section(nav, "NAVIGATION");
            addNav(nav, "Assigned Tickets", () -> app.tickets().countActiveTickets((Technician) user),
                    () -> new TicketsPanel(app, user, "Assigned Tickets",
                            () -> app.tickets().getTicketsFor(user), TicketsPanel.Mode.TECHNICIAN));
        }

        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(Theme.SIDEBAR);
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBorder(new MatteBorder(0, 0, 0, 1, Theme.BORDER));
        sidebar.add(brand(), BorderLayout.NORTH);
        sidebar.add(nav, BorderLayout.CENTER);
        sidebar.add(userCard(), BorderLayout.SOUTH);

        content.setBackground(Theme.BACKGROUND);
        JPanel main = new JPanel(new BorderLayout());
        main.add(topBar(), BorderLayout.NORTH);
        main.add(content, BorderLayout.CENTER);

        add(sidebar, BorderLayout.WEST);
        add(main, BorderLayout.CENTER);
    }

    private static int countActive(List<Ticket> tickets) {
        int n = 0;
        for (Ticket t : tickets) {
            if (t.getStatus() == Status.OPEN || t.getStatus() == Status.IN_PROGRESS) n++;
        }
        return n;
    }

    private JComponent brand() {
        JLabel logo = new JLabel("S", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logo.setPreferredSize(new Dimension(34, 34));
        logo.setForeground(Color.WHITE);
        logo.setFont(Theme.HEADING.deriveFont(16f));

        JLabel name = new JLabel("SnR Warehouse Club");
        name.setFont(Theme.HEADING);
        JPanel text = new JPanel(new GridLayout(2, 1));
        text.setOpaque(false);
        text.add(name);
        text.add(Theme.muted("IT Helpdesk"));

        JPanel brand = new JPanel(new BorderLayout(10, 0));
        brand.setOpaque(false);
        brand.setBorder(new EmptyBorder(16, 16, 12, 16));
        brand.add(logo, BorderLayout.WEST);
        brand.add(text, BorderLayout.CENTER);
        return brand;
    }

    private JComponent userCard() {
        JLabel who = new JLabel("<html><b>" + user.getFullName() + "</b><br><span style='color:" + Theme.hex(Theme.MUTED) + "'>"
                + user.getRole() + " · @" + user.getUsername() + "</span></html>");
        who.setIcon(new Theme.Avatar(user.getFullName(), 32));
        who.setIconTextGap(10);
        who.setFont(Theme.BODY);
        JLabel chevron = new JLabel("⌃");
        chevron.setForeground(Theme.MUTED);
        chevron.setFont(Theme.HEADING);

        SlideMenu menu = new SlideMenu();
        menu.addItem("Profile", this::showProfile);
        menu.addItem("Preferences", this::showPreferences);

        JPanel profile = new JPanel(new BorderLayout(8, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                if (menu.isOpen()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Theme.SIDEBAR_ACTIVE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        profile.setOpaque(false);
        profile.setBorder(new EmptyBorder(6, 6, 6, 8));
        profile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        profile.setToolTipText("Profile and preferences");
        profile.add(who, BorderLayout.CENTER);
        profile.add(chevron, BorderLayout.EAST);
        profile.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                menu.toggle();
                chevron.setText(menu.isOpen() ? "⌄" : "⌃");
                profile.repaint();
            }
        });

        JButton logout = Theme.secondaryButton("Log out");
        logout.addActionListener(e -> {
            dispose();
            new LoginFrame(app).setVisible(true);
        });

        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(new MatteBorder(1, 0, 0, 0, Theme.BORDER),
                new EmptyBorder(8, 8, 14, 8)));
        card.add(menu, BorderLayout.NORTH);
        card.add(profile, BorderLayout.CENTER);
        card.add(logout, BorderLayout.SOUTH);
        return card;
    }

    private JComponent topBar() {
        crumb.setFont(Theme.BODY);
        crumb.setForeground(Theme.TEXT);
        JLabel live = new JLabel("<html><span style='color:" + Theme.hex(Theme.SUCCESS) + "'>●</span> Live · "
                + LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, MMM d")) + "</html>");
        live.setForeground(Theme.MUTED);
        live.setFont(Theme.SMALL);

        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.BACKGROUND);
        bar.setPreferredSize(new Dimension(0, 52));
        bar.setBorder(BorderFactory.createCompoundBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER),
                new EmptyBorder(0, 26, 0, 26)));
        bar.add(crumb, BorderLayout.WEST);
        bar.add(live, BorderLayout.EAST);
        return bar;
    }

    private void section(JPanel nav, String text) {
        JLabel label = Theme.muted(text);
        label.setFont(Theme.LABEL);
        label.setBorder(new EmptyBorder(14, 18, 6, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        nav.add(label);
    }

    private void addNav(JPanel nav, String label, Supplier<Integer> badge, Supplier<JComponent> page) {
        NavItem item = new NavItem(label, badge);
        item.addActionListener(e -> showPage(label, page, item));
        navItems.add(item);
        nav.add(item);
    }

    private void showPage(String label, Supplier<JComponent> page, NavItem active) {
        try {
            app.reload();
        } catch (HelpdeskException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Could not load data", JOptionPane.WARNING_MESSAGE);
        }
        navItems.forEach(n -> n.setActive(n == active));
        navItems.forEach(NavItem::updateBadge);
        crumb.setText("<html><span style='color:" + Theme.hex(Theme.MUTED) + "'>SnR Warehouse Club &nbsp;›&nbsp;</span> " + label + "</html>");
        content.removeAll();
        content.add(page.get(), BorderLayout.CENTER);
        content.revalidate();
        content.repaint();
    }

    public void showProfile() {
        showPage("Profile", () -> new SettingsPanel(app, user, this, false), null);
    }

    public void showPreferences() {
        showPage("Preferences", () -> new SettingsPanel(app, user, this, true), null);
    }

    // Repaint this same window in the new colors (no new window) and stay on Preferences.
    public void switchTheme(boolean dark) {
        Theme.setDark(dark);
        SwingUtilities.updateComponentTreeUI(this);
        build();
        showPreferences();
        revalidate();
        repaint();
    }

    // Small menu above the profile card that slides up when opened.
    private static class SlideMenu extends JPanel {
        private final JPanel items = new JPanel(new GridLayout(0, 1, 0, 2));
        private final Timer timer = new Timer(12, null);
        private int height;
        private boolean open;

        SlideMenu() {
            super(new BorderLayout());
            setOpaque(false);
            items.setOpaque(false);
            items.setBorder(new EmptyBorder(6, 0, 6, 0));
            add(items, BorderLayout.SOUTH);
            timer.addActionListener(e -> step());
        }

        void addItem(String label, Runnable action) {
            NavItem item = new NavItem(label, null);
            item.addActionListener(e -> {
                toggle();
                action.run();
            });
            items.add(item);
        }

        boolean isOpen() { return open; }

        void toggle() {
            open = !open;
            timer.start();
        }

        private void step() {
            int target = open ? items.getPreferredSize().height : 0;
            height += Math.max(1, Math.abs(target - height) / 3) * Integer.signum(target - height);
            if (height == target) timer.stop();
            revalidate();
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(super.getPreferredSize().width, height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (height > 0) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.CARD);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.setColor(Theme.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        }
    }

    // Sidebar link: rounded highlight when active, optional count badge on the right.
    private static class NavItem extends JButton {
        private final Supplier<Integer> badge;
        private final JLabel badgeLabel = new JLabel();
        private boolean active;

        NavItem(String text, Supplier<Integer> badge) {
            this.badge = badge;
            setLayout(new BorderLayout());
            JLabel name = new JLabel(text);
            name.setFont(Theme.BODY.deriveFont(14f));
            badgeLabel.setFont(Theme.LABEL);
            badgeLabel.setForeground(Theme.MUTED);
            add(name, BorderLayout.CENTER);
            add(badgeLabel, BorderLayout.EAST);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            setPreferredSize(new Dimension(240, 40));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        void setActive(boolean active) {
            this.active = active;
            repaint();
        }

        void updateBadge() {
            Integer n = badge == null ? null : badge.get();
            badgeLabel.setText(n == null || n == 0 ? "" : String.valueOf(n));
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (active || getModel().isRollover()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(active ? Theme.SIDEBAR_ACTIVE : Theme.CARD);
                g2.fillRoundRect(8, 1, getWidth() - 16, getHeight() - 2, 10, 10);
                g2.dispose();
            }
            super.paintComponent(g);
        }

        @Override
        public Insets getInsets() {
            return new Insets(9, 20, 9, 22);
        }
    }
}
