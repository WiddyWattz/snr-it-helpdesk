package snr.ui;

import snr.model.Priority;
import snr.model.Status;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

public final class Theme {
    public static final Color BACKGROUND = new Color(0x0B0D12);
    public static final Color SIDEBAR = new Color(0x0F1117);
    public static final Color SIDEBAR_ACTIVE = new Color(0x1E2230);
    public static final Color CARD = new Color(0x14171F);
    public static final Color CARD_ALT = new Color(0x1A1E28);
    public static final Color BORDER = new Color(0x262B37);
    public static final Color FIELD = new Color(0x1A1E28);
    public static final Color PRIMARY = new Color(0x6366F1);
    public static final Color TEXT = new Color(0xE6E8EE);
    public static final Color MUTED = new Color(0x8B92A3);
    public static final Color DANGER = new Color(0xEF4444);
    public static final Color SUCCESS = new Color(0x22C55E);
    public static final Color WARNING = new Color(0xEAB308);
    public static final Color INFO = new Color(0x3B82F6);
    public static final Color PURPLE = new Color(0xA78BFA);
    public static final Color GRAY = new Color(0x6B7280);

    public static final Font TITLE = new Font(Font.SANS_SERIF, Font.BOLD, 24);
    public static final Font HEADING = new Font(Font.SANS_SERIF, Font.BOLD, 14);
    public static final Font BODY = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
    public static final Font SMALL = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
    public static final Font LABEL = new Font(Font.SANS_SERIF, Font.BOLD, 11);
    public static final Font MONO = new Font(Font.MONOSPACED, Font.PLAIN, 12);
    public static final Font BIG_NUMBER = new Font(Font.MONOSPACED, Font.BOLD, 30);

    private Theme() {
    }

    // Dark Metal theme so fields, combos, scrollbars and dialogs match the cards.
    public static void install() {
        MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme() {
            @Override protected ColorUIResource getPrimary1() { return new ColorUIResource(PRIMARY); }
            @Override protected ColorUIResource getPrimary2() { return new ColorUIResource(0x3B3F8F); }
            @Override protected ColorUIResource getPrimary3() { return new ColorUIResource(0x2A2F45); }
            @Override protected ColorUIResource getSecondary1() { return new ColorUIResource(BORDER); }
            @Override protected ColorUIResource getSecondary2() { return new ColorUIResource(0x3A404D); }
            @Override protected ColorUIResource getSecondary3() { return new ColorUIResource(CARD); }
            @Override protected ColorUIResource getBlack() { return new ColorUIResource(TEXT); }
            @Override protected ColorUIResource getWhite() { return new ColorUIResource(FIELD); }
        });
        try {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (UnsupportedLookAndFeelException ignored) {
        }
        UIManager.put("Button.gradient", null);
        UIManager.put("Button.background", CARD_ALT);
        UIManager.put("Button.foreground", TEXT);
        UIManager.put("Button.select", SIDEBAR_ACTIVE);
        UIManager.put("Button.border", BorderFactory.createCompoundBorder(
                new LineBorder(BORDER), new EmptyBorder(6, 14, 6, 14)));
        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("OptionPane.background", CARD);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("Table.background", CARD);
        UIManager.put("Table.foreground", TEXT);
        UIManager.put("Table.selectionBackground", new Color(0x252A3D));
        UIManager.put("Table.selectionForeground", TEXT);
        UIManager.put("ScrollPane.border", BorderFactory.createEmptyBorder());
        UIManager.put("ScrollBarUI", ThinScrollBarUI.class.getName());
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("ComboBox.background", FIELD);
        UIManager.put("ComboBox.selectionBackground", SIDEBAR_ACTIVE);
        UIManager.put("ComboBox.selectionForeground", TEXT);
        UIManager.put("ToolTip.background", CARD_ALT);
        UIManager.put("ToolTip.foreground", TEXT);
        for (String key : new String[]{"TextField", "PasswordField", "TextArea"}) {
            UIManager.put(key + ".border", BorderFactory.createCompoundBorder(
                    new LineBorder(BORDER), new EmptyBorder(6, 8, 6, 8)));
            UIManager.put(key + ".caretForeground", TEXT);
        }
    }

    public static JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(TITLE);
        label.setForeground(TEXT);
        label.setBorder(new EmptyBorder(0, 0, 14, 0));
        return label;
    }

    public static JLabel muted(String text) {
        JLabel label = new JLabel(text);
        label.setFont(SMALL);
        label.setForeground(MUTED);
        return label;
    }

    public static JButton primaryButton(String text) {
        return new RoundButton(text, PRIMARY, Color.WHITE);
    }

    public static JButton secondaryButton(String text) {
        return new RoundButton(text, CARD_ALT, TEXT);
    }

    public static void closeOnEscape(JDialog dialog) {
        dialog.getRootPane().registerKeyboardAction(e -> dialog.dispose(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    public static JPanel page(String title, JComponent content) {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(BACKGROUND);
        page.setBorder(new EmptyBorder(22, 26, 22, 26));
        page.add(title(title), BorderLayout.NORTH);
        page.add(content, BorderLayout.CENTER);
        return page;
    }

    public static JScrollPane tableScroll(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(CARD);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        return scroll;
    }

    public static Card tableCard(JTable table) {
        Card card = new Card(new BorderLayout());
        card.setBorder(new EmptyBorder(6, 6, 6, 6));
        card.add(tableScroll(table), BorderLayout.CENTER);
        return card;
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(38);
        table.setFont(BODY);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(BORDER);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(Object.class, new CellRenderer());
        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 34));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
                                                           boolean focus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, value, false, false, row, col);
                l.setFont(LABEL);
                l.setForeground(MUTED);
                l.setBackground(CARD_ALT);
                l.setBorder(new EmptyBorder(0, 12, 0, 8));
                return l;
            }
        });
    }

    public static Color statusColor(Status s) {
        switch (s) {
            case OPEN: return INFO;
            case IN_PROGRESS: return PURPLE;
            case RESOLVED: return SUCCESS;
            default: return GRAY;
        }
    }

    public static Color priorityColor(Priority p) {
        switch (p) {
            case HIGH: return DANGER;
            case MEDIUM: return WARNING;
            default: return INFO;
        }
    }

    public static Color colorFor(Object value) {
        if (value instanceof Status) return statusColor((Status) value);
        if (value instanceof Priority) return priorityColor((Priority) value);
        if ("Working".equals(value) || "Active".equals(value)) return SUCCESS;
        if ("Needs repair".equals(value)) return WARNING;
        if ("Retired".equals(value) || "Deactivated".equals(value)) return GRAY;
        return null;
    }

    public static Color blend(Color c, Color background, double amount) {
        return new Color((int) (c.getRed() * amount + background.getRed() * (1 - amount)),
                (int) (c.getGreen() * amount + background.getGreen() * (1 - amount)),
                (int) (c.getBlue() * amount + background.getBlue() * (1 - amount)));
    }

    // Default table cell: padded text, Status/Priority values drawn as colored pills.
    public static class CellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
                                                       boolean focus, int row, int col) {
            Color pill = colorFor(value);
            if (pill != null) return new Pill(String.valueOf(value), pill, selected ? t.getSelectionBackground() : t.getBackground());
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, value, selected, false, row, col);
            l.setBorder(new EmptyBorder(0, 12, 0, 8));
            l.setForeground(TEXT);
            return l;
        }
    }

    public static class Pill extends JComponent {
        private final String text;
        private final Color color;
        private final Color background;

        public Pill(String text, Color color, Color background) {
            this.text = text;
            this.color = color;
            this.background = background;
            setFont(LABEL);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(background);
            g2.fillRect(0, 0, getWidth(), getHeight());
            FontMetrics fm = g2.getFontMetrics(getFont());
            int w = fm.stringWidth(text) + 18, h = 22, x = 12, y = (getHeight() - h) / 2;
            g2.setColor(blend(color, background, 0.22));
            g2.fillRoundRect(x, y, w, h, h, h);
            g2.setColor(color);
            g2.setFont(getFont());
            g2.drawString(text, x + 9, y + (h + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(getFontMetrics(getFont()).stringWidth(text) + 30, 26);
        }
    }

    // Circle with a person's initials.
    public static class Avatar implements Icon {
        private final String initials;
        private final int size;

        public Avatar(String name, int size) {
            StringBuilder sb = new StringBuilder();
            for (String part : name.replace(".", "").trim().split("\\s+")) {
                if (!part.isEmpty() && sb.length() < 2) sb.append(Character.toUpperCase(part.charAt(0)));
            }
            this.initials = sb.toString();
            this.size = size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(new Color(0x2A2F3E));
            g2.fillOval(x, y, size, size);
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.max(9, size / 3)));
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(MUTED);
            g2.drawString(initials, x + (size - fm.stringWidth(initials)) / 2,
                    y + (size + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }

        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }
    }

    // Rounded dark panel with a thin border; an optional accent paints a colored bar on the left.
    public static class Card extends JPanel {
        private Color accent;
        private Color fill = CARD;

        public Card(LayoutManager layout) {
            super(layout);
            setOpaque(false);
            setBorder(new EmptyBorder(16, 18, 16, 18));
        }

        public Card accent(Color accent) {
            this.accent = accent;
            return this;
        }

        public Card fill(Color fill) {
            this.fill = fill;
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth() - 1, h = getHeight() - 1;
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, w, h, 14, 14);
            if (accent != null) {
                g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, w, h, 14, 14));
                g2.setColor(accent);
                g2.fillRect(0, 0, 3, h);
                g2.setClip(null);
            }
            g2.setColor(BORDER);
            g2.drawRoundRect(0, 0, w, h, 14, 14);
            g2.dispose();
        }
    }

    // Thin rounded scrollbar without arrow buttons.
    public static class ThinScrollBarUI extends BasicScrollBarUI {
        public static ComponentUI createUI(JComponent c) {
            return new ThinScrollBarUI();
        }

        @Override
        protected void configureScrollBarColors() {
            thumbColor = new Color(0x2D3240);
            trackColor = BACKGROUND;
        }

        @Override
        protected JButton createDecreaseButton(int orientation) { return zeroButton(); }

        @Override
        protected JButton createIncreaseButton(int orientation) { return zeroButton(); }

        private static JButton zeroButton() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            return b;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            g.setColor(trackColor);
            g.fillRect(r.x, r.y, r.width, r.height);
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (r.isEmpty()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isThumbRollover() ? new Color(0x3D4355) : thumbColor);
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8);
            g2.dispose();
        }
    }

    // Panel for scroll panes that always fits the viewport width (only scrolls vertically).
    public static class WidthTrackingPanel extends JPanel implements Scrollable {
        public WidthTrackingPanel() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setOpaque(false);
        }

        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return r.height; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    public static class RoundButton extends JButton {
        private final Color fill;

        public RoundButton(String text, Color fill, Color textColor) {
            super(text);
            this.fill = fill;
            setFont(HEADING.deriveFont(13f));
            setForeground(textColor);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setBorder(new EmptyBorder(9, 16, 9, 16));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color c = getModel().isPressed() ? fill.darker() : getModel().isRollover() ? fill.brighter() : fill;
            g2.setColor(c);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            if (fill.equals(CARD_ALT)) {
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
