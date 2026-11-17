package waferscope.ui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Customer Defect Knowledge Hub & Engineering Encyclopedia.
 * 
 * Provides comprehensive industrial knowledge for each of the 9 defect categories:
 * Visual characteristics, root causes, yield risk, and recommended fab corrective actions.
 * High-contrast, WCAG-compliant customer presentation.
 */
public class DefectKnowledgePanel extends JPanel {

    public interface OnSampleSelectedListener {
        void onSampleSelected(String categoryName);
    }

    private OnSampleSelectedListener sampleListener;

    private static class DefectInfo {
        String name;
        String badge;
        String tagLine;
        String description;
        String rootCause;
        String yieldImpact;
        String fabRemedy;
        Color accentColor;
        String[][] patternGrid; // 9x9 sample pattern for preview
    }

    private final Map<String, DefectInfo> catalog = new HashMap<>();
    private final JPanel detailPanel = new JPanel();
    private final ButtonGroup categoryGroup = new ButtonGroup();
    private String currentSelectedDefect = "Center";

    public DefectKnowledgePanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(UiKit.PAGE_BG);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        initCatalogData();

        // Top Banner
        add(createTopBanner(), BorderLayout.NORTH);

        // Center Split View: Left List / Right Details
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.26);
        split.setDividerSize(6);
        split.setBorder(null);
        split.setOpaque(false);

        split.setLeftComponent(createCategorySelectorCard());
        split.setRightComponent(createDetailCard());

        add(split, BorderLayout.CENTER);

        showDefectDetail("Center");
    }

    public void setOnSampleSelectedListener(OnSampleSelectedListener listener) {
        this.sampleListener = listener;
    }

    private JPanel createTopBanner() {
        JPanel banner = new JPanel(new BorderLayout(10, 4));
        banner.setBackground(Color.WHITE);
        banner.setBorder(UiKit.createCardBorder(12, 16, 12, 16));

        JLabel title = new JLabel("SEMICONDUCTOR DEFECT KNOWLEDGE HUB");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(UiKit.TEXT_PRIMARY);

        JLabel sub = new JLabel("Comprehensive customer guide to semiconductor wafer defect patterns, root causes, yield impacts, and corrective actions.");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sub.setForeground(UiKit.TEXT_SECONDARY);

        banner.add(title, BorderLayout.NORTH);
        banner.add(sub, BorderLayout.SOUTH);
        return banner;
    }

    private JPanel createCategorySelectorCard() {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(UiKit.createCardBorder(12, 12, 12, 12));

        JLabel title = new JLabel("DEFECT CATEGORIES (9)");
        title.setFont(new Font("SansSerif", Font.BOLD, 13));
        title.setForeground(UiKit.TEXT_PRIMARY);
        card.add(title, BorderLayout.NORTH);

        JPanel listPanel = new JPanel(new GridLayout(9, 1, 0, 8));
        listPanel.setOpaque(false);

        String[] order = {"Center", "Donut", "Edge-Loc", "Edge-Ring", "Loc", "Near-Full", "Random", "Scratch", "None"};

        for (String defName : order) {
            DefectInfo info = catalog.get(defName);
            UiKit.ListSelectorButton btn = new UiKit.ListSelectorButton(info.name + " (" + info.badge + ")");

            if (defName.equals("Center")) {
                btn.setSelected(true);
            }

            btn.addActionListener(e -> {
                listPanel.repaint();
                showDefectDetail(defName);
            });

            categoryGroup.add(btn);
            listPanel.add(btn);
        }

        card.add(listPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createDetailCard() {
        detailPanel.setLayout(new BorderLayout());
        detailPanel.setOpaque(false);
        return detailPanel;
    }

    private void showDefectDetail(String categoryName) {
        currentSelectedDefect = categoryName;
        detailPanel.removeAll();

        DefectInfo info = catalog.get(categoryName);
        if (info == null) return;

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(UiKit.createCardBorder(16, 20, 16, 20));

        // Header with Tagline & Quick Action
        JPanel headRow = new JPanel(new BorderLayout(10, 4));
        headRow.setOpaque(false);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBlock.setOpaque(false);
        JLabel nameL = new JLabel(info.name.toUpperCase() + " PATTERN (" + info.badge + ")");
        nameL.setFont(new Font("SansSerif", Font.BOLD, 20));
        nameL.setForeground(info.accentColor);
        JLabel tagL = new JLabel(info.tagLine);
        tagL.setFont(new Font("SansSerif", Font.ITALIC, 13));
        tagL.setForeground(UiKit.TEXT_SECONDARY);
        titleBlock.add(nameL);
        titleBlock.add(tagL);
        headRow.add(titleBlock, BorderLayout.WEST);

        UiKit.PrimaryButton loadSampleBtn = new UiKit.PrimaryButton("Load " + info.name + " into Wafer Analyzer");
        loadSampleBtn.setPreferredSize(new Dimension(260, 34));
        loadSampleBtn.addActionListener(e -> {
            if (sampleListener != null) {
                sampleListener.onSampleSelected(info.name);
            }
        });
        headRow.add(loadSampleBtn, BorderLayout.EAST);
        content.add(headRow);
        content.add(Box.createVerticalStrut(16));

        // Visual Pattern Preview & Quick Stats
        JPanel visualRow = new JPanel(new BorderLayout(14, 0));
        visualRow.setOpaque(false);
        visualRow.setMaximumSize(new Dimension(3000, 140));

        // Mini Wafer Preview Canvas
        JPanel previewCanvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int sz = Math.min(getWidth(), getHeight()) - 10;
                int ox = (getWidth() - sz) / 2;
                int oy = (getHeight() - sz) / 2;

                // Silicon disc
                g2.setColor(new Color(226, 232, 240));
                g2.fillOval(ox, oy, sz, sz);
                g2.setColor(new Color(100, 116, 139));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(ox, oy, sz, sz);

                // Draw mock dies
                if (info.patternGrid != null) {
                    int rows = info.patternGrid.length;
                    int cols = info.patternGrid[0].length;
                    int dSz = sz / (rows + 2);
                    int startX = ox + dSz;
                    int startY = oy + dSz;

                    for (int r = 0; r < rows; r++) {
                        for (int c = 0; c < cols; c++) {
                            String v = info.patternGrid[r][c];
                            if ("1".equals(v)) {
                                g2.setColor(info.accentColor);
                                g2.fillRect(startX + c * dSz, startY + r * dSz, dSz - 1, dSz - 1);
                            } else if ("0".equals(v)) {
                                g2.setColor(new Color(22, 163, 74)); // Good die green
                                g2.fillRect(startX + c * dSz, startY + r * dSz, dSz - 1, dSz - 1);
                            }
                        }
                    }
                }
            }
        };
        previewCanvas.setPreferredSize(new Dimension(140, 130));
        previewCanvas.setBackground(new Color(248, 250, 252));
        previewCanvas.setBorder(new LineBorder(UiKit.BORDER_COLOR, 1, true));
        visualRow.add(previewCanvas, BorderLayout.WEST);

        // Section: Description
        JPanel descCard = createSectionCard("1. PHYSICAL CHARACTERISTICS & PATTERN SIGNATURE", info.description, UiKit.PRIMARY);
        visualRow.add(descCard, BorderLayout.CENTER);
        content.add(visualRow);
        content.add(Box.createVerticalStrut(14));

        // Section: Root Cause
        content.add(createSectionCard("2. SEMICONDUCTOR FAB MANUFACTURING ROOT CAUSE", info.rootCause, UiKit.WARNING));
        content.add(Box.createVerticalStrut(12));

        // Section: Yield Impact
        content.add(createSectionCard("3. WAFER YIELD & RELIABILITY IMPACT", info.yieldImpact, UiKit.DANGER));
        content.add(Box.createVerticalStrut(12));

        // Section: Fab Remedy
        content.add(createSectionCard("4. RECOMMENDED ENGINEERING ACTION & CORRECTIVE MEASURES", info.fabRemedy, UiKit.SUCCESS));

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        detailPanel.add(scroll, BorderLayout.CENTER);

        detailPanel.revalidate();
        detailPanel.repaint();
    }

    private JPanel createSectionCard(String title, String body, Color accentCol) {
        JPanel card = new JPanel(new BorderLayout(6, 6));
        card.setBackground(new Color(248, 250, 252));
        card.setBorder(new CompoundBorder(
            new MatteBorder(0, 5, 0, 0, accentCol),
            new CompoundBorder(
                new LineBorder(UiKit.BORDER_COLOR, 1, false),
                new EmptyBorder(10, 14, 10, 14)
            )
        ));

        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.BOLD, 12));
        t.setForeground(accentCol);
        card.add(t, BorderLayout.NORTH);

        JTextArea b = new JTextArea(body);
        b.setEditable(false);
        b.setLineWrap(true);
        b.setWrapStyleWord(true);
        b.setFont(new Font("SansSerif", Font.PLAIN, 12));
        b.setForeground(UiKit.TEXT_PRIMARY);
        b.setOpaque(false);
        card.add(b, BorderLayout.CENTER);

        return card;
    }

    private void initCatalogData() {
        // 1. Center
        DefectInfo c = new DefectInfo();
        c.name = "Center";
        c.badge = "CTR";
        c.tagLine = "Cluster of failed dies concentrated around the central axis of the silicon substrate.";
        c.accentColor = UiKit.getDefectColor("CENTER");
        c.description = "Center defects present as a solid or dense circular cluster of functional failures centered on the wafer. Dies in peripheral zones usually function normally, but central dies exhibit parametric failure or open/short faults.";
        c.rootCause = "Typically caused by non-uniform Chemical Mechanical Planarization (CMP) polishing pressure, central furnace thermal gradients during chemical vapor deposition (CVD), or excessive gas nozzle flow concentrated at the spin chuck center.";
        c.yieldImpact = "High severity for central high-value dies. Wafer functional die yield typically drops by 15% to 35%.";
        c.fabRemedy = "Inspect and recalibrate CMP polishing head pad pressure distribution. Check thermal profile inside diffusion furnaces to eliminate radial temperature disparities.";
        c.patternGrid = new String[][]{
            {"0","0","0","0","0","0","0"},
            {"0","0","0","1","0","0","0"},
            {"0","0","1","1","1","0","0"},
            {"0","1","1","1","1","1","0"},
            {"0","0","1","1","1","0","0"},
            {"0","0","0","1","0","0","0"},
            {"0","0","0","0","0","0","0"}
        };
        catalog.put(c.name, c);

        // 2. Donut
        DefectInfo d = new DefectInfo();
        d.name = "Donut";
        d.badge = "DNT";
        d.tagLine = "Toroidal ring of defective dies surrounding an unaffected, healthy center.";
        d.accentColor = UiKit.getDefectColor("DONUT");
        d.description = "Donut defects exhibit a characteristic ring-like morphology. The exact center and outer perimeter of the wafer remain functional, while an annular ring of dies fails parametric testing.";
        d.rootCause = "Plasma etching vortices in reactive ion etching (RIE) chambers, uneven radio-frequency (RF) plasma power distribution, or rotational non-uniformity in spin-rinse-dry (SRD) drying cycles.";
        d.patternGrid = new String[][]{
            {"0","0","0","0","0","0","0"},
            {"0","0","1","1","1","0","0"},
            {"0","1","0","0","0","1","0"},
            {"0","1","0","0","0","1","0"},
            {"0","1","0","0","0","1","0"},
            {"0","0","1","1","1","0","0"},
            {"0","0","0","0","0","0","0"}
        };
        d.yieldImpact = "Moderate to High. Significant die yield loss proportional to the ring circumference and thickness.";
        d.fabRemedy = "Calibrate RF matching network in plasma etcher. Check electrostatic chuck (ESC) temperature uniformity and helium backside cooling channels.";
        catalog.put(d.name, d);

        // 3. Edge-Loc
        DefectInfo el = new DefectInfo();
        el.name = "Edge-Loc";
        el.badge = "ELOC";
        el.tagLine = "Localized peripheral cluster on the wafer edge, often crescent or localized sector shaped.";
        el.accentColor = UiKit.getDefectColor("EDGE-LOC");
        el.description = "Edge-Loc defects are tightly clustered near the wafer edge, typically spanning 15 to 45 degrees of arc along the outer rim.";
        el.rootCause = "Physical clamp pressure during robotic transfer wands, improper cassette slot spacing, or localized thermal gradients at wafer edge.";
        el.patternGrid = new String[][]{
            {"0","0","0","0","0","0","0"},
            {"0","0","0","0","0","1","1"},
            {"0","0","0","0","1","1","1"},
            {"0","0","0","0","0","1","1"},
            {"0","0","0","0","0","0","0"},
            {"0","0","0","0","0","0","0"},
            {"0","0","0","0","0","0","0"}
        };
        el.yieldImpact = "Low to Moderate. Affects edge dies, preserving central high-speed dies.";
        el.fabRemedy = "Realine robotic wafer handling end-effectors and check cassette clearance.";
        catalog.put(el.name, el);

        // 4. Edge-Ring
        DefectInfo er = new DefectInfo();
        er.name = "Edge-Ring";
        er.badge = "ERING";
        er.tagLine = "Continuous ring of defects bordering the entire outer perimeter of the silicon wafer.";
        er.accentColor = UiKit.getDefectColor("EDGE-RING");
        er.description = "Edge-Ring appears as a complete or nearly complete halo of failing dies around the outermost ring of the wafer circumference.";
        er.rootCause = "Improper Edge Bead Removal (EBR) solvent dispense angle, photoresist buildup on the bevel edge, or plasma sheath curling near the edge ring of the etcher chuck.";
        er.patternGrid = new String[][]{
            {"0","1","1","1","1","1","0"},
            {"1","0","0","0","0","0","1"},
            {"1","0","0","0","0","0","1"},
            {"1","0","0","0","0","0","1"},
            {"1","0","0","0","0","0","1"},
            {"1","0","0","0","0","0","1"},
            {"0","1","1","1","1","1","0"}
        };
        er.yieldImpact = "Moderate. Consistently destroys the entire outer ring of silicon dies.";
        er.fabRemedy = "Adjust EBR dispense nozzle position and flow rate. Replace worn focus ring on etch reactor.";
        catalog.put(er.name, er);

        // 5. Loc
        DefectInfo loc = new DefectInfo();
        loc.name = "Loc";
        loc.badge = "LOC";
        loc.tagLine = "Isolated, dense cluster appearing in arbitrary intermediate regions away from center and edge.";
        loc.accentColor = UiKit.getDefectColor("LOC");
        loc.description = "Loc defects form a dense localized spot of failed dies in an arbitrary sector of the wafer, surrounded by healthy, working dies.";
        loc.rootCause = "Cleanroom particulate drop (dust/flake), liquid droplet splatter from rinse nozzles, or localized contamination on photomask reticle.";
        loc.patternGrid = new String[][]{
            {"0","0","0","0","0","0","0"},
            {"0","0","0","0","0","0","0"},
            {"0","1","1","0","0","0","0"},
            {"0","1","1","1","0","0","0"},
            {"0","0","1","0","0","0","0"},
            {"0","0","0","0","0","0","0"},
            {"0","0","0","0","0","0","0"}
        };
        loc.yieldImpact = "Localized. Typically localized yield loss confined to the contaminated region.";
        loc.fabRemedy = "Inspect cleanroom HEPA airflow, run laser particle counter, and clean chemical dispensing nozzles.";
        catalog.put(loc.name, loc);

        // 6. Near-Full
        DefectInfo nf = new DefectInfo();
        nf.name = "Near-Full";
        nf.badge = "NFULL";
        nf.tagLine = "Catastrophic yield collapse covering almost the entire surface of the wafer.";
        nf.accentColor = UiKit.getDefectColor("NEAR-FULL");
        nf.description = "Near-Full represents comprehensive wafer failure where 70% to 100% of all dies fail testing.";
        nf.rootCause = "Catastrophic manufacturing errors: severe misprocessing, missed ion implantation step, gross photolithographic exposure error, or cracked silicon substrate.";
        nf.patternGrid = new String[][]{
            {"0","1","1","1","1","1","0"},
            {"1","1","1","1","1","1","1"},
            {"1","1","1","1","1","1","1"},
            {"1","1","1","1","1","1","1"},
            {"1","1","1","1","1","1","1"},
            {"1","1","1","1","1","1","1"},
            {"0","1","1","1","1","1","0"}
        };
        nf.yieldImpact = "Critical (Total Loss). 0% - 15% functional yield.";
        nf.fabRemedy = "Immediate line halt. Quarantine the entire lot and perform spectroscopic layer thickness analysis.";
        catalog.put(nf.name, nf);

        // 7. Random
        DefectInfo rnd = new DefectInfo();
        rnd.name = "Random";
        rnd.badge = "RND";
        rnd.tagLine = "Uniformly dispersed single-die failures across the wafer with no clustering.";
        rnd.accentColor = UiKit.getDefectColor("RANDOM");
        rnd.description = "Random defects follow a Poisson distribution across the wafer without identifiable geometric patterns or clustering.";
        rnd.rootCause = "Intrinsic silicon substrate point defects, thermal oxide pinholes, or baseline airborne particulate density.";
        rnd.patternGrid = new String[][]{
            {"0","0","0","1","0","0","0"},
            {"0","1","0","0","0","1","0"},
            {"0","0","0","0","0","0","0"},
            {"1","0","0","1","0","0","0"},
            {"0","0","0","0","0","1","0"},
            {"0","1","0","0","0","0","0"},
            {"0","0","0","0","1","0","0"}
        };
        rnd.yieldImpact = "Low to Moderate. Baseline semiconductor yield degradation.";
        rnd.fabRemedy = "Maintain cleanroom ISO standards and inspect incoming raw silicon wafer quality from crystal growth suppliers.";
        catalog.put(rnd.name, rnd);

        // 8. Scratch
        DefectInfo sc = new DefectInfo();
        sc.name = "Scratch";
        sc.badge = "SCR";
        sc.tagLine = "Linear, curvilinear, or intersecting line of damaged dies across the wafer.";
        sc.accentColor = UiKit.getDefectColor("SCRATCH");
        sc.description = "Scratch defects form continuous tracks of failed dies along straight or gently curved trajectories across multiple die columns or rows.";
        sc.rootCause = "Abrasive diamond or silica particle dragged across wafer surface during CMP polishing, or mechanical handling tool / tweezers contact.";
        sc.patternGrid = new String[][]{
            {"0","0","0","0","0","0","1"},
            {"0","0","0","0","0","1","0"},
            {"0","0","0","0","1","0","0"},
            {"0","0","0","1","0","0","0"},
            {"0","0","1","0","0","0","0"},
            {"0","1","0","0","0","0","0"},
            {"1","0","0","0","0","0","0"}
        };
        sc.yieldImpact = "Moderate. Destroys all dies intersected by the mechanical track.";
        sc.fabRemedy = "Replace CMP pad, purge and filter slurry lines, and inspect robotic arm vacuum cups.";
        catalog.put(sc.name, sc);

        // 9. None
        DefectInfo none = new DefectInfo();
        none.name = "None";
        none.badge = "PASS";
        none.tagLine = "Golden reference wafer meeting all semiconductor fab electrical and visual specifications.";
        none.accentColor = UiKit.getDefectColor("NONE");
        none.description = "No systematic defect clusters detected. The wafer exhibits standard high functional yield suitable for commercial packaging.";
        none.rootCause = "Normal fab processing operating within Six Sigma statistical process control limits.";
        none.patternGrid = new String[][]{
            {"0","0","0","0","0","0","0"},
            {"0","0","0","0","0","0","0"},
            {"0","0","0","0","0","0","0"},
            {"0","0","0","0","0","0","0"},
            {"0","0","0","0","0","0","0"},
            {"0","0","0","0","0","0","0"},
            {"0","0","0","0","0","0","0"}
        };
        none.yieldImpact = "Negligible. High yield (> 95%).";
        none.fabRemedy = "Approved for automatic lot dispatch to backend packaging & test.";
        catalog.put(none.name, none);
    }
}
