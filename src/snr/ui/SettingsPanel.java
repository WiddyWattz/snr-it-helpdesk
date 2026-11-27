package snr.ui;

import snr.model.HelpdeskException;
import snr.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SettingsPanel extends JPanel {
    private final AppContext app;
    private final User user;
    private final JPasswordField current = new JPasswordField();
    private final JPasswordField newPassword = new JPasswordField();
    private final JPasswordField confirm = new JPasswordField();

    public SettingsPanel(AppContext app, User user, MainFrame frame, boolean preferences) {
        super(new BorderLayout());
        this.app = app;
        this.user = user;

        JPanel column = new Theme.WidthTrackingPanel();
        if (preferences) {
            add(column, themeCard(frame));
        } else {
            add(column, profileCard());
            add(column, passwordCard());
        }

        JScrollPane scroll = new JScrollPane(column);
        scroll.getViewport().setBackground(Theme.BACKGROUND);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(Theme.page(preferences ? "Preferences" : "Profile", scroll), BorderLayout.CENTER);
    }

    private void add(JPanel column, JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(720, c.getPreferredSize().height));
        column.add(c);
        column.add(Box.createVerticalStrut(14));
    }

    private Theme.Card card(String title, String subtitle, JComponent body) {
        JLabel heading = new JLabel(title);
        heading.setFont(Theme.HEADING.deriveFont(15f));
        JLabel sub = Theme.muted(subtitle);
        sub.setFont(Theme.BODY);
        JPanel head = new JPanel(new GridLayout(2, 1, 0, 2));
        head.setOpaque(false);
        head.setBorder(new EmptyBorder(0, 0, 14, 0));
        head.add(heading);
        head.add(sub);
        Theme.Card card = new Theme.Card(new BorderLayout());
        card.setBorder(new EmptyBorder(18, 20, 20, 20));
        card.add(head, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JComponent profileCard() {
        JLabel who = new JLabel("<html><span style='font-size:13px'><b>" + user.getFullName() + "</b></span><br>"
                + "<span style='color:" + Theme.hex(Theme.MUTED) + "'>@" + user.getUsername() + "</span></html>");
        who.setFont(Theme.BODY.deriveFont(14f));
        who.setIcon(new Theme.Avatar(user.getFullName(), 56));
        who.setIconTextGap(14);
        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.add(who, BorderLayout.WEST);
        body.add(new Theme.Pill(user.getRole(), Theme.PRIMARY, Theme.CARD), BorderLayout.EAST);
        return card("Profile", "Your account in the SnR Warehouse Club IT Helpdesk.", body);
    }

    private JComponent themeCard(MainFrame frame) {
        JPanel options = new JPanel(new GridLayout(1, 2, 12, 0));
        options.setOpaque(false);
        options.add(themeOption("Dark", "Easy on the eyes at night", true, frame));
        options.add(themeOption("Light", "Bright, for well-lit rooms", false, frame));
        return card("Appearance", "Choose how the helpdesk looks on this computer.", options);
    }

    private JComponent themeOption(String name, String hint, boolean dark, MainFrame frame) {
        boolean selected = Theme.isDark() == dark;
        JButton option = new JButton("<html><b>" + name + "</b>" + (selected ? " &nbsp;✓" : "")
                + "<br><span style='color:" + Theme.hex(Theme.MUTED) + "'>" + hint + "</span></html>") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.CARD_ALT);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                // small preview swatch of the theme
                g2.setColor(dark ? new Color(0x14171F) : Color.WHITE);
                g2.fillRoundRect(12, 12, 28, getHeight() - 25, 6, 6);
                g2.setColor(new Color(0x6366F1));
                g2.fillRoundRect(16, 16, 20, 5, 4, 4);
                g2.setColor(selected ? Theme.PRIMARY : Theme.BORDER);
                g2.setStroke(new BasicStroke(selected ? 2f : 1f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        option.setFont(Theme.BODY);
        option.setHorizontalAlignment(SwingConstants.LEFT);
        option.setBorder(new EmptyBorder(12, 54, 12, 12));
        option.setContentAreaFilled(false);
        option.setFocusPainted(false);
        option.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        option.addActionListener(e -> {
            if (!selected) frame.switchTheme(dark);
        });
        return option;
    }

    private JComponent passwordCard() {
        JPanel form = new JPanel(new GridLayout(0, 1, 0, 6));
        form.setOpaque(false);
        form.add(Theme.muted("Current password"));
        form.add(current);
        form.add(Theme.muted("New password (at least 6 characters)"));
        form.add(newPassword);
        form.add(Theme.muted("Confirm new password"));
        form.add(confirm);

        JButton save = Theme.primaryButton("Change Password");
        save.addActionListener(e -> changePassword());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttons.setOpaque(false);
        buttons.setBorder(new EmptyBorder(14, 0, 0, 0));
        buttons.add(save);

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.add(form, BorderLayout.CENTER);
        body.add(buttons, BorderLayout.SOUTH);
        return card("Change Password", "Use a password you don't use anywhere else.", body);
    }

    private void changePassword() {
        try {
            app.users().changePassword(user, new String(current.getPassword()),
                    new String(newPassword.getPassword()), new String(confirm.getPassword()));
            JOptionPane.showMessageDialog(this, "Password changed. Use the new password next time you log in.");
        } catch (HelpdeskException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Cannot change password", JOptionPane.WARNING_MESSAGE);
        } finally {
            current.setText("");
            newPassword.setText("");
            confirm.setText("");
        }
    }
}
