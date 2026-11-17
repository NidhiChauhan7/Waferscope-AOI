package waferscope.ui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Enumeration;

/**
 * Modern industrial dashboard header for WaferScope AOI.
 * Displays platform branding, fab lot status pills, and customer navigation tabs.
 * Clean, high-visibility, and 100% customer-oriented.
 */
public class HeaderPanel extends JPanel {

    private final JLabel modeBadge = new JLabel("MODE: AI ACTIVE");
    private final JLabel lotBadge = new JLabel("LOT: DETECTING...");
    private final JLabel sampleBadge = new JLabel("SCANNING WAFERS...");
    private final JLabel statusPill = new JLabel(" SYSTEM INITIALIZING ");

    private final JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    private final ButtonGroup navGroup = new ButtonGroup();

    public interface NavSelectionListener {
        void onNavSelected(String tabName);
    }

    private NavSelectionListener navListener;

    public HeaderPanel() {
        setLayout(new BorderLayout());
        setBackground(UiKit.HEADER_BG);
        setBorder(new EmptyBorder(14, 20, 12, 20));

        // Top Row: Title & Status Badges
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        // Title and Subtitle
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 3));
        titlePanel.setOpaque(false);

        JLabel brand = new JLabel("WAFERSCOPE AOI");
        brand.setForeground(Color.WHITE);
        brand.setFont(new Font("SansSerif", Font.BOLD, 22));

        JLabel sub = new JLabel("Semiconductor Wafer Defect Detection Platform | Automated Optical Inspection & Yield Analytics");
        sub.setForeground(new Color(203, 213, 225)); // High-contrast light slate #cbd5e1
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12));

        titlePanel.add(brand);
        titlePanel.add(sub);
        topRow.add(titlePanel, BorderLayout.WEST);

        // Status Badges (Guaranteed high-contrast text on dark header)
        JPanel badges = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        badges.setOpaque(false);

        styleBadge(statusPill, new Color(6, 78, 59), new Color(74, 222, 128), new Color(22, 163, 74));
        badges.add(statusPill);

        styleBadge(lotBadge, new Color(30, 41, 59), Color.WHITE, new Color(71, 85, 105));
        badges.add(lotBadge);

        styleBadge(sampleBadge, new Color(30, 41, 59), Color.WHITE, new Color(71, 85, 105));
        badges.add(sampleBadge);

        styleBadge(modeBadge, new Color(180, 83, 9), Color.WHITE, new Color(245, 158, 11));
        badges.add(modeBadge);

        topRow.add(badges, BorderLayout.EAST);
        add(topRow, BorderLayout.NORTH);

        // Bottom Row: Customer Navigation Tabs (Custom painted, zero washed-out colors!)
        navBar.setOpaque(false);
        navBar.setBorder(new EmptyBorder(14, 0, 2, 0));

        addNavButton("DASHBOARD", "Executive Lot Yield & Defect Overview", true);
        addNavButton("ANALYZE WAFER", "Upload Wafer Image & Run Instant AI Diagnosis", false);
        addNavButton("DEFECT KNOWLEDGE HUB", "Customer Defect Encyclopedia (All 9 Patterns, Root Causes & Remedies)", false);
        addNavButton("WAFER VISUALIZER", "High-Resolution Silicon Wafer Map & C++ Graphics Engine", false);
        addNavButton("INSPECTION HISTORY", "Inspection Session Audit Trail & CSV Export", false);

        add(navBar, BorderLayout.SOUTH);
    }

    public void setNavSelectionListener(NavSelectionListener listener) {
        this.navListener = listener;
    }

    private void styleBadge(JLabel label, Color bg, Color fg, Color borderCol) {
        label.setOpaque(true);
        label.setBackground(bg);
        label.setForeground(fg);
        label.setFont(new Font("SansSerif", Font.BOLD, 11));
        label.setBorder(new CompoundBorder(
            new LineBorder(borderCol, 1, true),
            new EmptyBorder(4, 10, 4, 10)
        ));
    }

    private void addNavButton(String id, String tooltip, boolean isDefault) {
        UiKit.NavTabButton btn = new UiKit.NavTabButton(id, tooltip);

        btn.addActionListener(e -> {
            updateNavButtonStyles();
            if (navListener != null) {
                navListener.onNavSelected(id);
            }
        });

        navGroup.add(btn);
        navBar.add(btn);
        if (isDefault) {
            btn.setSelected(true);
        }
    }

    public void selectTab(String tabName) {
        Enumeration<AbstractButton> buttons = navGroup.getElements();
        while (buttons.hasMoreElements()) {
            AbstractButton b = buttons.nextElement();
            if (b.getText().equalsIgnoreCase(tabName)) {
                b.setSelected(true);
                updateNavButtonStyles();
                if (navListener != null) {
                    navListener.onNavSelected(b.getText());
                }
                break;
            }
        }
    }

    private void updateNavButtonStyles() {
        navBar.repaint();
    }

    public void setSystemStatus(String text, Color col) {
        statusPill.setText(" " + text + " ");
        if (col.getGreen() > 150 && col.getRed() < 100) {
            styleBadge(statusPill, new Color(6, 78, 59), new Color(74, 222, 128), new Color(22, 163, 74));
        } else if (col.getRed() > 150 && col.getGreen() < 100) {
            styleBadge(statusPill, new Color(127, 29, 29), new Color(254, 202, 202), new Color(220, 38, 38));
        } else {
            styleBadge(statusPill, new Color(180, 83, 9), Color.WHITE, new Color(245, 158, 11));
        }
    }

    public void updateSampleCount(int count) {
        sampleBadge.setText(count + " WAFERS");
    }

    public void updateMode(String modeName, boolean active) {
        setAiMode(modeName);
    }

    public void setOperationalStatus(String text, boolean operational) {
        statusPill.setText(" " + text + " ");
        if (operational) {
            styleBadge(statusPill, new Color(6, 78, 59), new Color(74, 222, 128), new Color(22, 163, 74));
        } else {
            styleBadge(statusPill, new Color(127, 29, 29), new Color(254, 202, 202), new Color(220, 38, 38));
        }
    }

    public void setAiMode(String modeName) {
        modeBadge.setText("MODE: " + modeName.toUpperCase());
        if ("TRAINED".equalsIgnoreCase(modeName) || "ACTIVE".equalsIgnoreCase(modeName)) {
            styleBadge(modeBadge, new Color(6, 78, 59), Color.WHITE, new Color(22, 163, 74));
        } else {
            styleBadge(modeBadge, new Color(180, 83, 9), Color.WHITE, new Color(245, 158, 11));
        }
    }

    public void setLotInfo(String lotId, int count) {
        lotBadge.setText("LOT: " + lotId);
        sampleBadge.setText(count + " WAFERS");
    }
}
