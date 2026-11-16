package waferscope.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Modern industrial dashboard header for WaferScope AOI.
 * Displays system branding, fab lot status pills, and primary navigation tabs.
 */
public class HeaderPanel extends JPanel {

    private final JLabel modeBadge = new JLabel("MODE: DEMO");
    private final JLabel lotBadge = new JLabel("LOT: TSMC-46393");
    private final JLabel sampleBadge = new JLabel("108 WAFERS");
    private final JLabel statusPill = new JLabel("[ONLINE] SYSTEM READY");

    private final JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
    private final ButtonGroup navGroup = new ButtonGroup();

    public interface NavSelectionListener {
        void onNavSelected(String tabName);
    }

    private NavSelectionListener navListener;

    public HeaderPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(15, 23, 42)); // Modern Deep Slate
        setBorder(new EmptyBorder(14, 20, 10, 20));

        // Top Row: Title & Status Badges
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        // Title and Subtitle
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titlePanel.setOpaque(false);

        JLabel brand = new JLabel("WAFERSCOPE AOI");
        brand.setForeground(Color.WHITE);
        brand.setFont(new Font("SansSerif", Font.BOLD, 22));

        JLabel sub = new JLabel("AI-Based Semiconductor Wafer Defect Detection System | Java - C++ - Computer Graphics - COA");
        sub.setForeground(new Color(148, 163, 184));
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12));

        titlePanel.add(brand);
        titlePanel.add(sub);
        topRow.add(titlePanel, BorderLayout.WEST);

        // Status Badges
        JPanel badges = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        badges.setOpaque(false);

        statusPill.setFont(new Font("SansSerif", Font.BOLD, 11));
        statusPill.setForeground(new Color(34, 197, 94)); // Emerald green
        badges.add(statusPill);

        styleBadge(lotBadge, new Color(51, 65, 85), Color.WHITE);
        badges.add(lotBadge);

        styleBadge(sampleBadge, new Color(51, 65, 85), Color.WHITE);
        badges.add(sampleBadge);

        styleBadge(modeBadge, new Color(245, 158, 11), Color.BLACK);
        badges.add(modeBadge);

        topRow.add(badges, BorderLayout.EAST);
        add(topRow, BorderLayout.NORTH);

        // Bottom Row: Navigation Tabs
        navBar.setOpaque(false);
        navBar.setBorder(new EmptyBorder(12, 0, 2, 0));

        addNavButton("DASHBOARD", "Dashboard Overview", true);
        addNavButton("VISUALIZER", "Wafer Map & Computer Graphics", false);
        addNavButton("INFERENCE", "AI Defect Classification Studio", false);
        addNavButton("HISTORY", "Inspection Logs & CSV Audit", false);
        addNavButton("COA METRICS", "COA Hardware & Cache Profiling", false);
        addNavButton("DATASET GUIDE", "WM-811K Documentation & Setup", false);
        addNavButton("OOP VIVA GUIDE", "Faculty OOP Concepts & Live Demo", false);

        add(navBar, BorderLayout.SOUTH);
    }

    private void styleBadge(JLabel label, Color bg, Color fg) {
        label.setOpaque(true);
        label.setBackground(bg);
        label.setForeground(fg);
        label.setFont(new Font("SansSerif", Font.BOLD, 11));
        label.setBorder(new EmptyBorder(4, 10, 4, 10));
    }

    private void addNavButton(String id, String tooltip, boolean isDefault) {
        JToggleButton btn = new JToggleButton(id);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(30, 41, 59));
        btn.setFocusPainted(false);
        btn.setToolTipText(tooltip);
        btn.setPreferredSize(new Dimension(135, 32));

        btn.addActionListener(e -> {
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

    public void setNavSelectionListener(NavSelectionListener listener) {
        this.navListener = listener;
    }

    public void updateMode(String modeName, boolean isTrained) {
        if (isTrained) {
            modeBadge.setText("MODE: TRAINED (AI / CNN)");
            modeBadge.setBackground(new Color(34, 197, 94));
            modeBadge.setForeground(Color.WHITE);
        } else {
            modeBadge.setText("MODE: DEMO (Baseline)");
            modeBadge.setBackground(new Color(245, 158, 11));
            modeBadge.setForeground(Color.BLACK);
        }
    }

    public void updateSampleCount(int count) {
        sampleBadge.setText(count + " WAFERS");
    }

    public void setSystemStatus(String status, Color color) {
        statusPill.setText("[" + status + "]");
        statusPill.setForeground(color);
    }
}
