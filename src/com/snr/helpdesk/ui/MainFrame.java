package com.snr.helpdesk.ui;

import com.snr.helpdesk.data.HelpdeskRepository;
import com.snr.helpdesk.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Main window: a sidebar with the pages for the logged-in user's role, and the page on the right. */
public class MainFrame extends JFrame {
    private final HelpdeskRepository repo;
    private final User user;
    private final JPanel content = new JPanel(new BorderLayout());
    private final List<JButton> navButtons = new ArrayList<>();

    public MainFrame(HelpdeskRepository repo, User user) {
        super("SnR IT Helpdesk - " + user.getRole());
        this.repo = repo;
        this.user = user;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 680);
        setMinimumSize(new Dimension(900, 560));
        setLocationRelativeTo(null);

        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Theme.SIDEBAR);
        sidebar.setPreferredSize(new Dimension(210, 0));
        sidebar.setBorder(new EmptyBorder(20, 0, 20, 0));

        JLabel brand = new JLabel("SnR IT Helpdesk");
        brand.setFont(Theme.HEADING.deriveFont(16f));
        brand.setForeground(Color.WHITE);
        brand.setBorder(new EmptyBorder(0, 20, 4, 0));
        JLabel who = new JLabel(user.getName() + " · " + user.getRole());
        who.setForeground(new Color(0xA0AEC0));
        who.setBorder(new EmptyBorder(0, 20, 20, 0));
        sidebar.add(brand);
        sidebar.add(who);

        switch (user.getRole()) {
            case REPORTER -> {
                addNav(sidebar, "Submit Ticket", () -> new SubmitTicketPanel(repo, user));
                addNav(sidebar, "My Tickets", () -> new TicketsPanel("My Tickets",
                        repo.getTicketsByReporter(user), TicketsPanel.Mode.REPORTER));
            }
            case TECHNICIAN -> addNav(sidebar, "Assigned Tickets", () -> new TicketsPanel(
                    "Assigned Tickets", repo.getTicketsByAssignee(user), TicketsPanel.Mode.TECHNICIAN));
            case ADMIN -> {
                addNav(sidebar, "Dashboard", () -> new DashboardPanel(repo));
                addNav(sidebar, "All Tickets", () -> new TicketsPanel("All Tickets",
                        repo.getTickets(), TicketsPanel.Mode.ADMIN));
                addNav(sidebar, "Devices", () -> new DevicesPanel(repo));
            }
        }

        sidebar.add(Box.createVerticalGlue());
        JButton logout = navButton("Log out");
        logout.addActionListener(e -> {
            dispose();
            new LoginFrame(repo).setVisible(true);
        });
        sidebar.add(logout);

        add(sidebar, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);
        navButtons.get(0).doClick();
    }

    private void addNav(JPanel sidebar, String label, Supplier<JComponent> page) {
        JButton button = navButton(label);
        button.addActionListener(e -> {
            navButtons.forEach(b -> b.setBackground(Theme.SIDEBAR));
            button.setBackground(Theme.SIDEBAR_ACTIVE);
            content.removeAll();
            content.add(page.get(), BorderLayout.CENTER);
            content.revalidate();
            content.repaint();
        });
        navButtons.add(button);
        sidebar.add(button);
    }

    private JButton navButton(String label) {
        JButton button = new JButton(label);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFont(Theme.BODY.deriveFont(14f));
        button.setForeground(Color.WHITE);
        button.setBackground(Theme.SIDEBAR);
        button.setBorder(new EmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }
}
