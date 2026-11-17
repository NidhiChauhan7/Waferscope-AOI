package waferscope.ui;

import waferscope.model.WaferMap;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Executive Fab Quality Dashboard.
 * 
 * Displays overall wafer lot metrics, defect category distribution (WM-811K Standard),
 * yield statistics, semiconductor fab knowledge, and quick navigation shortcuts.
 * Designed to fit completely within the display without requiring scrollbars.
 */
public class DashboardPanel extends JPanel {

    // Models
    private final List<WaferMap> allWafers = new ArrayList<>();
    private WaferMap activeWafer;

    // KPI Card Labels
    private final JLabel totalWafersLabel = new JLabel("0");
    private final JLabel normalWafersLabel = new JLabel("0");
    private final JLabel anomalyWafersLabel = new JLabel("0");
    private final JLabel avgYieldLabel = new JLabel("0.0%");
    private final JLabel lotSubtitleLabel = new JLabel("Scanning Lot...");

    // Category Distribution Card Container
    private final JPanel categoryListPanel = new JPanel();

    public interface OnCategorySelectListener {
        void onCategorySelected(String categoryName);
    }

    public interface OnBatchScanListener {
        void onRunBatchScan();
    }

    public interface OnWaferSelectListener {
        void onWaferSelected(WaferMap wafer);
    }

    private OnCategorySelectListener categoryListener;
    private OnBatchScanListener batchScanListener;
    private OnWaferSelectListener waferSelectListener;

    public DashboardPanel() {
        setLayout(new BorderLayout(0, 8));
        setBackground(UiKit.PAGE_BG);
        setBorder(new EmptyBorder(10, 16, 10, 16));

        // 1. Top Section: KPI Row + Action Center
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setOpaque(false);

        topSection.add(createKpiRow());
        topSection.add(Box.createVerticalStrut(8));
        topSection.add(createActionCenter());

        add(topSection, BorderLayout.NORTH);

        // 2. Lower Section: 2 Columns Side-by-Side (fits display dynamically)
        // Left Column: Semiconductor Wafer & AOI Intelligence Guide
        // Right Column: Defect Pattern Distribution Card (WM-811K Standard)
        JPanel lowerSection = new JPanel(new GridLayout(1, 2, 12, 0));
        lowerSection.setOpaque(false);
        lowerSection.add(createSemiconductorKnowledgeCard());
        lowerSection.add(createCategoryDistributionCard());

        add(lowerSection, BorderLayout.CENTER);
    }

    public void setOnCategorySelectListener(OnCategorySelectListener listener) {
        this.categoryListener = listener;
    }

    public void setOnBatchScanListener(OnBatchScanListener listener) {
        this.batchScanListener = listener;
    }

    public void setOnWaferSelectListener(OnWaferSelectListener listener) {
        this.waferSelectListener = listener;
    }

    private JPanel createKpiRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(3000, 80));
        row.setPreferredSize(new Dimension(800, 80));

        row.add(createKpiCard("TOTAL LOT WAFERS", totalWafersLabel, lotSubtitleLabel,
            new Color(51, 65, 85), UiKit.TEXT_PRIMARY, new Color(71, 85, 105)));
        row.add(createKpiCard("NORMAL WAFERS", normalWafersLabel, new JLabel("Class: None (Pass)"),
            new Color(22, 163, 74), new Color(21, 128, 61), UiKit.TEXT_SECONDARY));
        row.add(createKpiCard("ANOMALOUS WAFERS", anomalyWafersLabel, new JLabel("Defect Patterns Present"),
            new Color(220, 38, 38), new Color(185, 28, 28), UiKit.TEXT_SECONDARY));
        row.add(createKpiCard("AVERAGE DIE YIELD", avgYieldLabel, new JLabel("Mean Functional Die Rate"),
            new Color(37, 99, 235), new Color(29, 78, 216), UiKit.TEXT_SECONDARY));

        return row;
    }

    private JPanel createKpiCard(String title, JLabel valLabel, JLabel subLabel, Color accentColor, Color valueColor, Color subColor) {
        JPanel card = new JPanel(new BorderLayout(4, 2));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
            new MatteBorder(0, 4, 0, 0, accentColor),
            new CompoundBorder(
                new LineBorder(UiKit.BORDER_COLOR, 1, false),
                new EmptyBorder(7, 12, 7, 12)
            )
        ));

        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.BOLD, 11));
        t.setForeground(UiKit.TEXT_PRIMARY);
        card.add(t, BorderLayout.NORTH);

        valLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        valLabel.setForeground(valueColor);
        card.add(valLabel, BorderLayout.CENTER);

        subLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        subLabel.setForeground(subColor);
        card.add(subLabel, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createActionCenter() {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(UiKit.createCardBorder(5, 12, 5, 12));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        JLabel title = new JLabel("FAB QUALITY ACTIONS:");
        title.setFont(new Font("SansSerif", Font.BOLD, 11));
        title.setForeground(UiKit.TEXT_PRIMARY);
        left.add(title);

        UiKit.PrimaryButton scanBtn = new UiKit.PrimaryButton("Run Full Lot Batch Anomaly Scan");
        scanBtn.setPreferredSize(new Dimension(215, 27));
        scanBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        scanBtn.addActionListener(e -> {
            if (batchScanListener != null) batchScanListener.onRunBatchScan();
        });
        left.add(scanBtn);

        UiKit.SecondaryButton inspectBtn = new UiKit.SecondaryButton("Inspect Lot in Visualizer");
        inspectBtn.setPreferredSize(new Dimension(150, 27));
        inspectBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        inspectBtn.addActionListener(e -> {
            if (categoryListener != null) categoryListener.onCategorySelected("ALL");
        });
        left.add(inspectBtn);

        panel.add(left, BorderLayout.WEST);

        // Right side: Quick Search Wafer shortcut
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);

        JLabel searchLbl = new JLabel("Quick Find Wafer ID:");
        searchLbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        searchLbl.setForeground(UiKit.TEXT_PRIMARY);
        right.add(searchLbl);

        JTextField searchField = new JTextField(10);
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 11));
        searchField.setPreferredSize(new Dimension(100, 25));
        right.add(searchField);

        UiKit.SecondaryButton findBtn = new UiKit.SecondaryButton("Analyze ID");
        findBtn.setPreferredSize(new Dimension(95, 25));
        findBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        findBtn.addActionListener(e -> {
            String q = searchField.getText().trim();
            if (!q.isEmpty()) {
                WaferMap found = null;
                for (WaferMap w : allWafers) {
                    if (w.getId().equalsIgnoreCase(q) || w.getId().toUpperCase().contains(q.toUpperCase())) {
                        found = w;
                        break;
                    }
                }
                if (found != null && waferSelectListener != null) {
                    waferSelectListener.onWaferSelected(found);
                } else {
                    JOptionPane.showMessageDialog(this, "Wafer ID not found: " + q, "Wafer Search", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        searchField.addActionListener(e -> findBtn.doClick());
        right.add(findBtn);

        panel.add(right, BorderLayout.EAST);
        panel.setMaximumSize(new Dimension(3000, 39));
        panel.setPreferredSize(new Dimension(800, 39));
        return panel;
    }

    private JPanel createSemiconductorKnowledgeCard() {
        JPanel card = new JPanel(new BorderLayout(6, 6));
        card.setBackground(Color.WHITE);
        card.setBorder(UiKit.createCardBorder(8, 12, 8, 12));

        // Header
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);

        JLabel title = new JLabel("SEMICONDUCTOR WAFER & AOI INTELLIGENCE GUIDE");
        title.setFont(new Font("SansSerif", Font.BOLD, 12));
        title.setForeground(UiKit.TEXT_PRIMARY);
        head.add(title, BorderLayout.WEST);

        JLabel subtitle = new JLabel("Fab Science, AOI Imperative & The AI Advantage");
        subtitle.setFont(new Font("SansSerif", Font.ITALIC, 10));
        subtitle.setForeground(UiKit.TEXT_SECONDARY);
        head.add(subtitle, BorderLayout.EAST);

        card.add(head, BorderLayout.NORTH);

        // Center: 3 Educational Modules in equal partitions
        JPanel container = new JPanel(new GridLayout(3, 1, 0, 5));
        container.setOpaque(false);

        // 1. What actually is a semiconductor wafer?
        container.add(createKnowledgeSubCard(
            "1. WHAT IS A SEMICONDUCTOR WAFER?",
            "Ultra-pure crystalline silicon disc (200/300mm) serving as the substrate foundation for hundreds to thousands of integrated microchip dies.",
            new String[]{
                "Die Matrix: Precision N x N die grid separated by scribe streets, diced after fabrication.",
                "Fabrication Complexity: Built via 500+ nanometer steps (lithography, ion implant, etching, CMP).",
                "High Fab Value: Finished wafers represent $10,000 to $100,000+ in production value."
            },
            new Color(37, 99, 235) // Royal Blue
        ));

        // 2. Why do we need WaferScope AOI?
        container.add(createKnowledgeSubCard(
            "2. WHY DO WE NEED WAFERSCOPE AOI (AUTOMATED OPTICAL INSPECTION)?",
            "Sub-micron particles, chemical films, and mechanical scratches cause chip shorts/opens. Automated Optical Inspection (AOI) catches defects early.",
            new String[]{
                "Prevent Costly Packaging: Catching defects early prevents 10x downstream packaging loss.",
                "100% Non-Destructive: High-speed optics scan all dies without contacting delicate structures.",
                "Yield Excursion Defense: Immediately flags equipment drift before ruining full production lots."
            },
            new Color(22, 163, 74) // Emerald Green
        ));

        // 3. Manual Inspection vs. AI Wafer Analysis (User Advantage)
        container.add(createKnowledgeSubCard(
            "3. MANUAL INSPECTION VS. AI WAFER ANALYSIS (USER ADVANTAGE)",
            "Replacing slow, error-prone manual microscope inspections with high-speed AI intelligence:",
            new String[]{
                "Speed & Scale: 2 ms AI triage vs. 15-30 min manual technician inspection per wafer.",
                "Zero Fatigue & Bias: 100% objective, repeatable defect classification across 289+ dies.",
                "Root-Cause Attribution: Direct mapping to failing fab equipment (CMP, etcher, robot clamp, coater)."
            },
            new Color(124, 58, 237) // Deep Violet
        ));

        card.add(container, BorderLayout.CENTER);
        return card;
    }

    private JPanel createKnowledgeSubCard(String title, String summary, String[] bulletPoints, Color accentColor) {
        JPanel p = new JPanel(new BorderLayout(2, 2));
        p.setBackground(new Color(248, 250, 252));
        p.setBorder(new CompoundBorder(
            new MatteBorder(0, 4, 0, 0, accentColor),
            new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(4, 8, 4, 8)
            )
        ));

        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.BOLD, 10));
        t.setForeground(accentColor);
        p.add(t, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        JTextArea sumArea = new JTextArea(summary);
        sumArea.setEditable(false);
        sumArea.setLineWrap(true);
        sumArea.setWrapStyleWord(true);
        sumArea.setOpaque(false);
        sumArea.setFont(new Font("SansSerif", Font.PLAIN, 10));
        sumArea.setForeground(UiKit.TEXT_PRIMARY);
        body.add(sumArea);
        body.add(Box.createVerticalStrut(2));

        for (String bp : bulletPoints) {
            JTextArea bpArea = new JTextArea("• " + bp);
            bpArea.setEditable(false);
            bpArea.setLineWrap(true);
            bpArea.setWrapStyleWord(true);
            bpArea.setOpaque(false);
            bpArea.setFont(new Font("SansSerif", Font.PLAIN, 10));
            bpArea.setForeground(new Color(51, 65, 85));
            body.add(bpArea);
            body.add(Box.createVerticalStrut(1));
        }

        p.add(body, BorderLayout.CENTER);
        return p;
    }

    private JPanel createCategoryDistributionCard() {
        JPanel card = new JPanel(new BorderLayout(6, 6));
        card.setBackground(Color.WHITE);
        card.setBorder(UiKit.createCardBorder(8, 12, 8, 12));

        JLabel title = new JLabel("DEFECT PATTERN DISTRIBUTION & ARCHETYPES (WM-811K STANDARD)");
        title.setFont(new Font("SansSerif", Font.BOLD, 12));
        title.setForeground(UiKit.TEXT_PRIMARY);
        card.add(title, BorderLayout.NORTH);

        categoryListPanel.setLayout(new GridLayout(9, 1, 0, 3));
        categoryListPanel.setOpaque(false);

        card.add(categoryListPanel, BorderLayout.CENTER);
        return card;
    }

    public void updateData(List<WaferMap> wafers) {
        if (wafers == null || wafers.isEmpty()) return;

        allWafers.clear();
        allWafers.addAll(wafers);

        totalWafersLabel.setText(String.valueOf(wafers.size()));
        String lotId = (!wafers.isEmpty() && wafers.get(0).getId().contains("-"))
            ? wafers.get(0).getId().split("-")[1]
            : "FAB-ACTIVE";
        lotSubtitleLabel.setText("Active Lot: " + lotId);

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
        categoryListPanel.setLayout(new GridLayout(9, 1, 0, 3));

        String[][] categories = new String[][]{
            {"Center", "Cluster concentrated in circular core (CMP / furnace unevenness)"},
            {"Donut", "Toroidal ring around center with clear center (etcher vortex defect)"},
            {"Edge-Loc", "Localized peripheral cluster on wafer border (clamp / robotic handling)"},
            {"Edge-Ring", "Continuous defect ring around wafer circumference (edge bead removal)"},
            {"Loc", "Isolated cluster away from center and edge (particle contamination)"},
            {"Near-Full", "Widespread failure covering almost all dies (litho exposure failure)"},
            {"Random", "Uniformly scattered noise dies (raw silicon substrate impurities)"},
            {"Scratch", "Linear or curvilinear defect track (mechanical handling tool scratch)"},
            {"None", "Normal production wafer without systematic failure patterns"}
        };

        for (String[] cat : categories) {
            String name = cat[0];
            String desc = cat[1];
            Color col = UiKit.getDefectColor(name);

            int count = 0;
            for (WaferMap w : wafers) {
                if (w.getGroundTruthLabel().equalsIgnoreCase(name)) count++;
            }

            double pct = (wafers.size() > 0) ? (count * 100.0 / wafers.size()) : 0;
            categoryListPanel.add(createCategoryRow(name, desc, count, pct, col));
        }

        categoryListPanel.revalidate();
        categoryListPanel.repaint();
    }

    private JPanel createCategoryRow(String name, String desc, int count, double pct, Color col) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setOpaque(false);
        row.setBorder(new CompoundBorder(
            new LineBorder(new Color(241, 245, 249), 1),
            new EmptyBorder(2, 6, 2, 6)
        ));

        // Left: Badge & Name
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 1));
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(142, 24));

        JLabel badge = new JLabel(" " + name.toUpperCase(Locale.US) + " ");
        badge.setOpaque(true);
        badge.setBackground(col);
        badge.setForeground(Color.WHITE);
        badge.setFont(new Font("SansSerif", Font.BOLD, 10));
        badge.setBorder(new EmptyBorder(1, 4, 1, 4));
        left.add(badge);

        JLabel countLabel = new JLabel(count + " (" + String.format(Locale.US, "%.1f%%", pct) + ")");
        countLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
        countLabel.setForeground(UiKit.TEXT_PRIMARY);
        left.add(countLabel);

        row.add(left, BorderLayout.WEST);

        // Center: Description
        JLabel descLabel = new JLabel(desc);
        descLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        descLabel.setForeground(UiKit.TEXT_SECONDARY);
        descLabel.setToolTipText(desc);
        row.add(descLabel, BorderLayout.CENTER);

        // Right: Inspect button
        UiKit.SecondaryButton btn = new UiKit.SecondaryButton("Inspect");
        btn.setPreferredSize(new Dimension(66, 22));
        btn.setFont(new Font("SansSerif", Font.BOLD, 10));
        btn.addActionListener(e -> {
            if (categoryListener != null) {
                categoryListener.onCategorySelected(name);
            }
        });
        row.add(btn, BorderLayout.EAST);

        return row;
    }

    public void setActiveWafer(WaferMap wafer) {
        if (wafer == null) return;
        this.activeWafer = wafer;
    }

    public WaferMap getActiveWafer() {
        return activeWafer;
    }
}
