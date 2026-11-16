package waferscope.ui;

import java.awt.*;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import waferscope.model.WaferMap;

/**
 * Executive Fab Quality Dashboard.
 * Displays overall wafer lot metrics, defect category distribution,
 * yield statistics, and quick navigation shortcuts.
 */
public class DashboardPanel extends JPanel {

    private final JLabel totalWafersLabel = new JLabel("0");
    private final JLabel normalWafersLabel = new JLabel("0");
    private final JLabel anomalyWafersLabel = new JLabel("0");
    private final JLabel avgYieldLabel = new JLabel("0.0%");

    private final JPanel categoryListPanel = new JPanel();

    public interface OnCategorySelectListener {
        void onCategorySelected(String categoryName);
    }

    public interface OnBatchScanListener {
        void onRunBatchScan();
    }

    private OnCategorySelectListener categoryListener;
    private OnBatchScanListener batchScanListener;

    public DashboardPanel() {
        setLayout(new BorderLayout(14, 14));
        setBackground(new Color(248, 250, 252));
        setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        // 1. KPI Cards Row
        content.add(createKpiRow());
        content.add(Box.createVerticalStrut(16));

        // 2. Action Center
        content.add(createActionCenter());
        content.add(Box.createVerticalStrut(16));

        // 3. Category Distribution Card
        content.add(createCategoryDistributionCard());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        add(scroll, BorderLayout.CENTER);
    }

    public void setOnCategorySelectListener(OnCategorySelectListener listener) {
        this.categoryListener = listener;
    }

    public void setOnBatchScanListener(OnBatchScanListener listener) {
        this.batchScanListener = listener;
    }

    private JPanel createKpiRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 14, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(3000, 110));
        row.setPreferredSize(new Dimension(800, 110));

        row.add(createKpiCard("TOTAL LOT WAFERS", totalWafersLabel, "Active Lot: TSMC-46393", new Color(30, 41, 59), new Color(226, 232, 240)));
        row.add(createKpiCard("NORMAL WAFERS", normalWafersLabel, "Class: None (Pass)", new Color(22, 101, 52), new Color(220, 252, 231)));
        row.add(createKpiCard("ANOMALOUS WAFERS", anomalyWafersLabel, "Defect Patterns Present", new Color(185, 28, 28), new Color(254, 226, 226)));
        row.add(createKpiCard("AVERAGE DIE YIELD", avgYieldLabel, "Mean Functional Die Rate", new Color(37, 99, 235), new Color(219, 234, 254)));

        return row;
    }

    private JPanel createKpiCard(String title, JLabel valLabel, String subtitle, Color textCol, Color bgCol) {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBackground(bgCol);
        card.setBorder(new CompoundBorder(
            new LineBorder(new Color(203, 213, 225), 1, true),
            new EmptyBorder(12, 14, 12, 14)
        ));

        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.BOLD, 11));
        t.setForeground(textCol);
        card.add(t, BorderLayout.NORTH);

        valLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        valLabel.setForeground(textCol);
        card.add(valLabel, BorderLayout.CENTER);

        JLabel s = new JLabel(subtitle);
        s.setFont(new Font("SansSerif", Font.PLAIN, 11));
        s.setForeground(textCol);
        card.add(s, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createActionCenter() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        bar.setBackground(Color.WHITE);
        bar.setMaximumSize(new Dimension(3000, 52));
        bar.setBorder(new CompoundBorder(
            new LineBorder(new Color(226, 232, 240), 1, true),
            new EmptyBorder(4, 10, 4, 10)
        ));

        JLabel title = new JLabel("FAB QUALITY ACTIONS:");
        title.setFont(new Font("SansSerif", Font.BOLD, 12));
        title.setForeground(new Color(71, 85, 105));
        bar.add(title);

        JButton scanBtn = new JButton("Run Full Lot Batch Anomaly Scan");
        scanBtn.setBackground(new Color(37, 99, 235));
        scanBtn.setForeground(Color.WHITE);
        scanBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        scanBtn.setFocusPainted(false);
        scanBtn.addActionListener(e -> {
            if (batchScanListener != null) batchScanListener.onRunBatchScan();
        });
        bar.add(scanBtn);

        JButton inspectBtn = new JButton("Inspect Lot in Visualizer");
        inspectBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        inspectBtn.addActionListener(e -> {
            if (categoryListener != null) categoryListener.onCategorySelected("ALL");
        });
        bar.add(inspectBtn);

        return bar;
    }

    private JPanel createCategoryDistributionCard() {
        JPanel card = new JPanel(new BorderLayout(8, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(new Color(226, 232, 240), 1, true),
            new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel title = new JLabel("DEFECT PATTERN DISTRIBUTION & ARCHETYPES (WM-811K STANDARD)");
        title.setFont(new Font("SansSerif", Font.BOLD, 13));
        title.setForeground(new Color(15, 23, 42));
        card.add(title, BorderLayout.NORTH);

        categoryListPanel.setLayout(new BoxLayout(categoryListPanel, BoxLayout.Y_AXIS));
        categoryListPanel.setOpaque(false);

        card.add(categoryListPanel, BorderLayout.CENTER);
        return card;
    }

    public void updateData(List<WaferMap> wafers) {
        if (wafers == null || wafers.isEmpty()) return;

        totalWafersLabel.setText(String.valueOf(wafers.size()));

        int normal = 0;
        int anomalous = 0;
        double yieldSum = 0;

        for (WaferMap w : wafers) {
            if ("None".equalsIgnoreCase(w.getGroundTruthLabel()) || w.getDefectiveCount() == 0) {
                normal++;
            } else {
                anomalous++;
            }
            yieldSum += w.getYieldPercentage();
        }

        normalWafersLabel.setText(String.valueOf(normal));
        anomalyWafersLabel.setText(String.valueOf(anomalous));
        avgYieldLabel.setText(String.format(Locale.US, "%.1f%%", yieldSum / wafers.size()));

        // Populate Category Breakdown
        categoryListPanel.removeAll();

        String[][] categories = new String[][]{
            {"Center", "Cluster concentrated in circular core (CMP / furnace unevenness)", "#ef4444"},
            {"Donut", "Toroidal ring around center with clear center (etcher vortex defect)", "#f97316"},
            {"Edge-Loc", "Localized peripheral cluster on wafer border (clamp / robotic handling)", "#eab308"},
            {"Edge-Ring", "Continuous defect ring around wafer circumference (edge bead removal)", "#84cc16"},
            {"Loc", "Isolated cluster away from center and edge (particle contamination)", "#06b6d4"},
            {"Near-Full", "Widespread failure covering almost all dies (litho exposure failure)", "#dc2626"},
            {"Random", "Uniformly scattered noise dies (raw silicon substrate impurities)", "#8b5cf6"},
            {"Scratch", "Linear or curvilinear defect track (mechanical handling tool scratch)", "#ec4899"},
            {"None", "Normal production wafer without systematic failure patterns", "#10b981"}
        };

        for (String[] cat : categories) {
            String name = cat[0];
            String desc = cat[1];
            Color col = Color.decode(cat[2]);

            int count = 0;
            for (WaferMap w : wafers) {
                if (w.getGroundTruthLabel().equalsIgnoreCase(name)) count++;
            }

            double pct = (wafers.size() > 0) ? (count * 100.0 / wafers.size()) : 0;
            categoryListPanel.add(createCategoryRow(name, desc, count, pct, col));
            categoryListPanel.add(Box.createVerticalStrut(8));
        }

        categoryListPanel.revalidate();
        categoryListPanel.repaint();
    }

    private JPanel createCategoryRow(String name, String desc, int count, double pct, Color col) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setBorder(new CompoundBorder(
            new LineBorder(new Color(241, 245, 249), 1),
            new EmptyBorder(6, 10, 6, 10)
        ));

        // Left: Badge & Name
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(280, 28));

        JLabel badge = new JLabel(" " + name.toUpperCase(Locale.US) + " ");
        badge.setOpaque(true);
        badge.setBackground(col);
        badge.setForeground(Color.WHITE);
        badge.setFont(new Font("SansSerif", Font.BOLD, 10));
        badge.setBorder(new EmptyBorder(2, 6, 2, 6));
        left.add(badge);

        JLabel countLabel = new JLabel(count + " wafers (" + String.format(Locale.US, "%.1f%%", pct) + ")");
        countLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        countLabel.setForeground(new Color(30, 41, 59));
        left.add(countLabel);

        row.add(left, BorderLayout.WEST);

        // Center: Description
        JLabel descLabel = new JLabel(desc);
        descLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        descLabel.setForeground(new Color(100, 116, 139));
        row.add(descLabel, BorderLayout.CENTER);

        // Right: Inspect button
        JButton btn = new JButton("Inspect Pattern");
        btn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        btn.setFocusPainted(false);
        btn.addActionListener(e -> {
            if (categoryListener != null) {
                categoryListener.onCategorySelected(name);
            }
        });
        row.add(btn, BorderLayout.EAST);

        return row;
    }
}

