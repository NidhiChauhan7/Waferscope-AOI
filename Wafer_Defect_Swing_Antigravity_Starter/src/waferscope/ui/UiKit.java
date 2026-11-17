package waferscope.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * High-Contrast, Industrial UI Design System for WaferScope AOI.
 * Ensures 100% visibility, crisp contrast (WCAG AA/AAA compliant),
 * and immune to OS Look & Feel color washed-out overrides.
 */
public final class UiKit {

    private UiKit() {}

    // Core Brand & Theme Colors
    public static final Color HEADER_BG = new Color(15, 23, 42);       // #0f172a Deep Slate
    public static final Color PAGE_BG = new Color(241, 245, 249);      // #f1f5f9 Crisp Surface
    public static final Color CARD_BG = Color.WHITE;                   // #ffffff
    public static final Color BORDER_COLOR = new Color(203, 213, 225); // #cbd5e1 Visible Border
    public static final Color BORDER_DARK = new Color(148, 163, 184);  // #94a3b8 Stronger Border

    // High-Contrast Text Colors
    public static final Color TEXT_PRIMARY = new Color(15, 23, 42);    // #0f172a (14:1 contrast on white)
    public static final Color TEXT_SECONDARY = new Color(51, 65, 85);  // #334155 (9:1 contrast on white)
    public static final Color TEXT_MUTED = new Color(100, 116, 139);   // #64748b (5:1 contrast on white)
    public static final Color TEXT_LIGHT = new Color(248, 250, 252);   // #f8fafc (for dark backgrounds)

    // Action & Status Colors
    public static final Color PRIMARY = new Color(37, 99, 235);        // #2563eb Royal Blue
    public static final Color PRIMARY_HOVER = new Color(29, 78, 216);  // #1d4ed8 Darker Blue
    public static final Color SUCCESS = new Color(22, 163, 74);        // #16a34a Emerald Green
    public static final Color SUCCESS_HOVER = new Color(21, 128, 61);  // #15803d
    public static final Color DANGER = new Color(220, 38, 38);         // #dc2626 Crimson Red
    public static final Color DANGER_HOVER = new Color(185, 28, 28);   // #b91c1c
    public static final Color WARNING = new Color(217, 119, 6);        // #d97706 Amber

    // High-Contrast Defect Badge Colors (Guaranteed >= 4.5:1 contrast with pure white text)
    private static final Map<String, Color> DEFECT_COLORS = new HashMap<>();
    static {
        DEFECT_COLORS.put("CENTER", new Color(185, 28, 28));     // Crimson Red #b91c1c
        DEFECT_COLORS.put("DONUT", new Color(194, 65, 12));      // Rust Orange #c2410c
        DEFECT_COLORS.put("EDGE-LOC", new Color(161, 98, 7));    // Dark Amber #a16207
        DEFECT_COLORS.put("EDGE-RING", new Color(21, 128, 61));  // Forest Green #15803d
        DEFECT_COLORS.put("LOC", new Color(14, 116, 144));       // Deep Cyan/Teal #0e7490
        DEFECT_COLORS.put("NEAR-FULL", new Color(127, 29, 29));  // Dark Maroon #7f1d1d
        DEFECT_COLORS.put("RANDOM", new Color(109, 40, 217));    // Royal Purple #6d28d9
        DEFECT_COLORS.put("SCRATCH", new Color(190, 24, 93));    // Deep Magenta #be185d
        DEFECT_COLORS.put("NONE", new Color(4, 120, 87));        // Deep Emerald #047857
    }

    public static Color getDefectColor(String category) {
        if (category == null) return DEFECT_COLORS.get("NONE");
        Color c = DEFECT_COLORS.get(category.trim().toUpperCase());
        return (c != null) ? c : new Color(71, 85, 105);
    }

    /**
     * Card Border Helper: visible gray border with internal padding.
     */
    public static Border createCardBorder(int top, int left, int bottom, int right) {
        return new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(top, left, bottom, right)
        );
    }

    /**
     * Header Navigation Tab Button.
     * Custom rendered to avoid Windows Look and Feel bleaching.
     */
    public static class NavTabButton extends JToggleButton {
        private boolean hovered = false;

        public NavTabButton(String text, String tooltip) {
            super(text);
            setToolTipText(tooltip);
            setFont(new Font("SansSerif", Font.BOLD, 12));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            int w = text.length() > 16 ? 195 : (text.length() > 11 ? 150 : 125);
            setPreferredSize(new Dimension(w, 34));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            if (isSelected()) {
                g2.setColor(PRIMARY);
                g2.fillRoundRect(0, 0, width, height, 8, 8);
                g2.setColor(new Color(147, 197, 253));
                g2.drawRoundRect(0, 0, width - 1, height - 1, 8, 8);
                g2.setColor(Color.WHITE);
            } else {
                if (hovered) {
                    g2.setColor(new Color(51, 65, 85));
                    g2.fillRoundRect(0, 0, width, height, 8, 8);
                    g2.setColor(new Color(100, 116, 139));
                    g2.drawRoundRect(0, 0, width - 1, height - 1, 8, 8);
                    g2.setColor(Color.WHITE);
                } else {
                    g2.setColor(new Color(30, 41, 59));
                    g2.fillRoundRect(0, 0, width, height, 8, 8);
                    g2.setColor(new Color(71, 85, 105));
                    g2.drawRoundRect(0, 0, width - 1, height - 1, 8, 8);
                    g2.setColor(new Color(241, 245, 249));
                }
            }

            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int strW = fm.stringWidth(getText());
            int strH = fm.getAscent();
            int x = (width - strW) / 2;
            int y = (height - fm.getHeight()) / 2 + strH;
            g2.drawString(getText(), x, y);

            g2.dispose();
        }
    }

    /**
     * Category Pill Filter Button for Wafer View.
     */
    public static class CategoryPillButton extends JToggleButton {
        private boolean hovered = false;

        public CategoryPillButton(String text) {
            super(text);
            setFont(new Font("SansSerif", Font.BOLD, 11));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(text.equals("ALL") ? 55 : (text.length() > 6 ? 90 : 75), 26));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            if (isSelected()) {
                g2.setColor(PRIMARY);
                g2.fillRoundRect(0, 0, w, h, h, h);
                g2.setColor(new Color(147, 197, 253));
                g2.drawRoundRect(0, 0, w - 1, h - 1, h, h);
                g2.setColor(Color.WHITE);
            } else {
                g2.setColor(hovered ? new Color(241, 245, 249) : Color.WHITE);
                g2.fillRoundRect(0, 0, w, h, h, h);
                g2.setColor(hovered ? TEXT_PRIMARY : BORDER_DARK);
                g2.drawRoundRect(0, 0, w - 1, h - 1, h, h);
                g2.setColor(TEXT_PRIMARY);
            }

            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int strW = fm.stringWidth(getText());
            int x = (w - strW) / 2;
            int y = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(getText(), x, y);

            g2.dispose();
        }
    }

    /**
     * List Item Selector Button (for Knowledge Hub).
     */
    public static class ListSelectorButton extends JToggleButton {
        private boolean hovered = false;

        public ListSelectorButton(String text) {
            super(text);
            setFont(new Font("SansSerif", Font.BOLD, 12));
            setHorizontalAlignment(SwingConstants.LEFT);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(220, 36));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            if (isSelected()) {
                g2.setColor(PRIMARY);
                g2.fillRoundRect(0, 0, w, h, 6, 6);
                g2.setColor(new Color(147, 197, 253));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 6, 6);
                g2.setColor(Color.WHITE);
            } else {
                g2.setColor(hovered ? new Color(241, 245, 249) : Color.WHITE);
                g2.fillRoundRect(0, 0, w, h, 6, 6);
                g2.setColor(hovered ? TEXT_PRIMARY : BORDER_DARK);
                g2.drawRoundRect(0, 0, w - 1, h - 1, 6, 6);
                g2.setColor(TEXT_PRIMARY);
            }

            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int x = 12;
            int y = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(getText(), x, y);

            g2.dispose();
        }
    }

    /**
     * Primary High-Contrast Action Button (Royal Blue).
     */
    public static class PrimaryButton extends JButton {
        private boolean hovered = false;

        public PrimaryButton(String text) {
            super(text);
            setFont(new Font("SansSerif", Font.BOLD, 12));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(hovered ? PRIMARY_HOVER : PRIMARY);
            g2.fillRoundRect(0, 0, w, h, 6, 6);

            g2.setColor(new Color(147, 197, 253));
            g2.drawRoundRect(0, 0, w - 1, h - 1, 6, 6);

            g2.setFont(getFont());
            g2.setColor(Color.WHITE);
            FontMetrics fm = g2.getFontMetrics();
            int x = (w - fm.stringWidth(getText())) / 2;
            int y = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(getText(), x, y);

            g2.dispose();
        }
    }

    /**
     * Success High-Contrast Action Button (Emerald Green).
     */
    public static class SuccessButton extends JButton {
        private boolean hovered = false;

        public SuccessButton(String text) {
            super(text);
            setFont(new Font("SansSerif", Font.BOLD, 12));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(hovered ? SUCCESS_HOVER : SUCCESS);
            g2.fillRoundRect(0, 0, w, h, 6, 6);

            g2.setColor(new Color(187, 247, 208));
            g2.drawRoundRect(0, 0, w - 1, h - 1, 6, 6);

            g2.setFont(getFont());
            g2.setColor(Color.WHITE);
            FontMetrics fm = g2.getFontMetrics();
            int x = (w - fm.stringWidth(getText())) / 2;
            int y = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(getText(), x, y);

            g2.dispose();
        }
    }

    /**
     * Secondary / Outline Button with high-contrast text and crisp border.
     */
    public static class SecondaryButton extends JButton {
        private boolean hovered = false;

        public SecondaryButton(String text) {
            super(text);
            setFont(new Font("SansSerif", Font.BOLD, 12));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(hovered ? new Color(241, 245, 249) : Color.WHITE);
            g2.fillRoundRect(0, 0, w, h, 6, 6);

            g2.setColor(hovered ? TEXT_PRIMARY : BORDER_DARK);
            g2.drawRoundRect(0, 0, w - 1, h - 1, 6, 6);

            g2.setFont(getFont());
            g2.setColor(TEXT_PRIMARY);
            FontMetrics fm = g2.getFontMetrics();
            int x = (w - fm.stringWidth(getText())) / 2;
            int y = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(getText(), x, y);

            g2.dispose();
        }
    }

    /**
     * Pill Badge Component with guaranteed high-contrast text.
     */
    public static JLabel createBadge(String text, Color bg, Color fg) {
        JLabel badge = new JLabel(" " + text.trim() + " ");
        badge.setOpaque(true);
        badge.setBackground(bg);
        badge.setForeground(fg);
        badge.setFont(new Font("SansSerif", Font.BOLD, 11));
        badge.setBorder(new CompoundBorder(
            new LineBorder(bg.darker(), 1, true),
            new EmptyBorder(3, 8, 3, 8)
        ));
        return badge;
    }
}
