package snr.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public final class Theme {
    public static final Color SIDEBAR = new Color(0x1F2A44);
    public static final Color SIDEBAR_ACTIVE = new Color(0x2F3E63);
    public static final Color BACKGROUND = new Color(0xF4F6FA);
    public static final Color CARD = Color.WHITE;
    public static final Color PRIMARY = new Color(0x2B6CB0);
    public static final Color TEXT = new Color(0x1A202C);
    public static final Color MUTED = new Color(0x718096);
    public static final Color DANGER = new Color(0xC53030);

    public static final Font TITLE = new Font(Font.SANS_SERIF, Font.BOLD, 20);
    public static final Font HEADING = new Font(Font.SANS_SERIF, Font.BOLD, 14);
    public static final Font BODY = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
    public static final Font BIG_NUMBER = new Font(Font.SANS_SERIF, Font.BOLD, 28);

    private Theme() {
    }

    public static JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(TITLE);
        label.setForeground(TEXT);
        label.setBorder(new EmptyBorder(0, 0, 12, 0));
        return label;
    }

    public static JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(HEADING);
        button.setBackground(PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 16, 8, 16));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    public static void closeOnEscape(JDialog dialog) {
        dialog.getRootPane().registerKeyboardAction(e -> dialog.dispose(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    public static JPanel page(String title, JComponent content) {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(BACKGROUND);
        page.setBorder(new EmptyBorder(20, 24, 20, 24));
        page.add(title(title), BorderLayout.NORTH);
        page.add(content, BorderLayout.CENTER);
        return page;
    }
}
