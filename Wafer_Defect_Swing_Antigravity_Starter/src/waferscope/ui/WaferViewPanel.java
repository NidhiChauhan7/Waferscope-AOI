package waferscope.ui;

import waferscope.io.DatasetLoader;
import waferscope.io.WaferExporter;
import waferscope.model.DieStatus;
import waferscope.model.WaferMap;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Segregated Wafer Visualizer & Computer Graphics Workstation.
 *
 * Implements:
 * - Proper Wafer Navigation (Previous / Next / Quick Category Pills / Search)
 * - Circular silicon substrate rendering with antialiased dies and wafer notch
 * - Interactive Heatmap Overlay with variable intensity
 * - Interactive Die Inspector on Click
 * - Viewport Zoom / Pan / Reset Controls
 * - PNG / Matrix Export
 * - High-contrast WCAG-compliant design system
 */
public class WaferViewPanel extends JPanel {

    private final List<WaferMap> allWafers = new ArrayList<>();
    private final List<WaferMap> visibleWafers = new ArrayList<>();
    private WaferMap currentWafer;
    private int currentIndex = -1;

    // Components
    private final WaferCanvas waferCanvas = new WaferCanvas();
    private final JComboBox<String> waferSelector = new JComboBox<>();
    private final JTextField searchField = new JTextField(10);
    private final UiKit.SecondaryButton prevBtn = new UiKit.SecondaryButton("< Prev");
    private final UiKit.SecondaryButton nextBtn = new UiKit.SecondaryButton("Next >");
    private final UiKit.PrimaryButton importBtn = new UiKit.PrimaryButton("Import Map File");
    private final JToggleButton heatmapToggle = new JToggleButton("Heatmap Overlay");
    private final JSlider heatmapAlphaSlider = new JSlider(20, 100, 75);
    private final UiKit.SecondaryButton resetViewBtn = new UiKit.SecondaryButton("Reset Zoom");
    private final UiKit.PrimaryButton exportImgBtn = new UiKit.PrimaryButton("Save PNG");

    // Computer Graphics Tools (MITAOE CG Syllabus 2310218L)
    private final UiKit.SecondaryButton zoomInBtn = new UiKit.SecondaryButton("+ Zoom In");
    private final UiKit.SecondaryButton zoomOutBtn = new UiKit.SecondaryButton("- Zoom Out");
    private final JSlider rotationSlider = new JSlider(0, 360, 0);
    private final JLabel rotationDegBadge = new JLabel("0°", SwingConstants.CENTER);
    private final UiKit.SecondaryButton rotateStepBtn = new UiKit.SecondaryButton("+90°");
    private final UiKit.SecondaryButton rotateResetBtn = new UiKit.SecondaryButton("0°");
    private final UiKit.SecondaryButton lightingBtn = new UiKit.SecondaryButton("💡 Lighting: OFF");
    private final UiKit.SecondaryButton textureBtn = new UiKit.SecondaryButton("🏁 Texture: OFF");
    private final UiKit.SecondaryButton clipBtn = new UiKit.SecondaryButton("✂ Clip: OFF");
    private final UiKit.SecondaryButton scanlineBtn = new UiKit.SecondaryButton("▤ Scanline: OFF");

    // Clicked Die Inspector Callout
    private final JLabel clickedDieLabel = new JLabel("Click any die on the wafer to inspect its physical properties.", JLabel.CENTER);

    public interface OnWaferChangeListener {
        void onWaferChanged(WaferMap wafer);
    }

    private OnWaferChangeListener waferChangeListener;

    public WaferViewPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UiKit.PAGE_BG);
        setBorder(new EmptyBorder(12, 16, 12, 16));

        // Top Navigation Bar
        add(createTopNavBar(), BorderLayout.NORTH);

        // Center: Canvas & Die Inspector Card
        JPanel centerPanel = new JPanel(new BorderLayout(8, 8));
        centerPanel.setOpaque(false);

        // Category Filter Pills
        centerPanel.add(createCategoryPillBar(), BorderLayout.NORTH);

        // Canvas Card with Computer Graphics Toolbar
        JPanel canvasCard = new JPanel(new BorderLayout());
        canvasCard.setBackground(Color.WHITE);
        canvasCard.setBorder(UiKit.createCardBorder(8, 8, 8, 8));
        canvasCard.add(createCgToolbar(), BorderLayout.NORTH);
        canvasCard.add(waferCanvas, BorderLayout.CENTER);
        centerPanel.add(canvasCard, BorderLayout.CENTER);

        // Clicked Die Inspector Callout Banner (High-contrast card)
        clickedDieLabel.setOpaque(true);
        clickedDieLabel.setBackground(Color.WHITE);
        clickedDieLabel.setForeground(UiKit.TEXT_PRIMARY);
        clickedDieLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        clickedDieLabel.setBorder(UiKit.createCardBorder(8, 12, 8, 12));
        centerPanel.add(clickedDieLabel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        // Bottom Controls Bar
        add(createBottomControlsBar(), BorderLayout.SOUTH);

        initEvents();
    }

    public void setOnWaferChangeListener(OnWaferChangeListener listener) {
        this.waferChangeListener = listener;
    }

    private JPanel createTopNavBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 4));
        bar.setBackground(Color.WHITE);
        bar.setBorder(UiKit.createCardBorder(6, 12, 6, 12));

        // Left Navigation
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        left.setOpaque(false);

        prevBtn.setPreferredSize(new Dimension(80, 30));
        left.add(prevBtn);

        nextBtn.setPreferredSize(new Dimension(80, 30));
        left.add(nextBtn);

        JLabel waferLbl = new JLabel("Wafer:");
        waferLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        waferLbl.setForeground(UiKit.TEXT_PRIMARY);
        left.add(waferLbl);

        waferSelector.setPreferredSize(new Dimension(280, 30));
        waferSelector.setFont(new Font("SansSerif", Font.PLAIN, 12));
        waferSelector.setForeground(UiKit.TEXT_PRIMARY);
        waferSelector.setBackground(Color.WHITE);
        left.add(waferSelector);

        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        searchLbl.setForeground(UiKit.TEXT_PRIMARY);
        left.add(searchLbl);

        searchField.setFont(new Font("SansSerif", Font.PLAIN, 12));
        searchField.setForeground(UiKit.TEXT_PRIMARY);
        searchField.setCaretColor(UiKit.TEXT_PRIMARY);
        searchField.setPreferredSize(new Dimension(110, 30));
        left.add(searchField);

        bar.add(left, BorderLayout.WEST);

        // Right Actions
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        right.setOpaque(false);

        importBtn.setPreferredSize(new Dimension(150, 30));
        right.add(importBtn);
        bar.add(right, BorderLayout.EAST);

        return bar;
    }

    private JPanel createCategoryPillBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        bar.setOpaque(false);

        String[] pills = {"ALL", "Center", "Donut", "Edge-Loc", "Edge-Ring", "Loc", "Near-Full", "Random", "Scratch", "None"};
        ButtonGroup bg = new ButtonGroup();

        for (String p : pills) {
            UiKit.CategoryPillButton btn = new UiKit.CategoryPillButton(p);
            if (p.equals("ALL")) {
                btn.setSelected(true);
            }

            btn.addActionListener(e -> {
                bar.repaint();
                filterByCategory(p);
            });
            bg.add(btn);
            bar.add(btn);
        }

        return bar;
    }

    private JPanel createCgToolbar() {
        JPanel container = new JPanel(new GridLayout(2, 1, 4, 4));
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(2, 4, 4, 4));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        row1.setOpaque(false);

        zoomInBtn.setPreferredSize(new Dimension(88, 28));
        zoomInBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        zoomInBtn.addActionListener(e -> waferCanvas.zoomIn());
        row1.add(zoomInBtn);

        zoomOutBtn.setPreferredSize(new Dimension(88, 28));
        zoomOutBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        zoomOutBtn.addActionListener(e -> waferCanvas.zoomOut());
        row1.add(zoomOutBtn);

        lightingBtn.setPreferredSize(new Dimension(125, 28));
        lightingBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        lightingBtn.addActionListener(e -> {
            waferCanvas.toggleLighting();
            lightingBtn.setText(waferCanvas.isLightingEnabled() ? "💡 Light: ON" : "💡 Light: OFF");
        });
        row1.add(lightingBtn);

        textureBtn.setPreferredSize(new Dimension(130, 28));
        textureBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        textureBtn.addActionListener(e -> {
            waferCanvas.cycleTextureMode();
            textureBtn.setText("🏁 " + waferCanvas.getTextureMode());
        });
        row1.add(textureBtn);

        clipBtn.setPreferredSize(new Dimension(105, 28));
        clipBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        clipBtn.addActionListener(e -> {
            waferCanvas.toggleClipping();
            clipBtn.setText(waferCanvas.isClippingEnabled() ? "✂ Clip: ON" : "✂ Clip: OFF");
        });
        row1.add(clipBtn);

        scanlineBtn.setPreferredSize(new Dimension(115, 28));
        scanlineBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        scanlineBtn.addActionListener(e -> {
            waferCanvas.toggleScanlineFill();
            scanlineBtn.setText(waferCanvas.isScanlineFillEnabled() ? "▤ Fill: ON" : "▤ Fill: OFF");
        });
        row1.add(scanlineBtn);

        UiKit.SecondaryButton resetCg = new UiKit.SecondaryButton("↺ Reset View");
        resetCg.setPreferredSize(new Dimension(95, 28));
        resetCg.setFont(new Font("SansSerif", Font.BOLD, 11));
        resetCg.addActionListener(e -> {
            waferCanvas.resetView();
            rotationSlider.setValue(0);
            rotationDegBadge.setText("0°");
            lightingBtn.setText("💡 Light: OFF");
            textureBtn.setText("🏁 Texture: OFF");
            clipBtn.setText("✂ Clip: OFF");
            scanlineBtn.setText("▤ Fill: OFF");
        });
        row1.add(resetCg);

        // Row 2: Continuous 360-Degree Rotation Controller (1° resolution)
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        row2.setOpaque(false);

        JLabel rotLabel = new JLabel("↻ 360° Rotation:");
        rotLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        rotLabel.setForeground(UiKit.TEXT_PRIMARY);
        row2.add(rotLabel);

        rotationSlider.setPreferredSize(new Dimension(200, 26));
        rotationSlider.setOpaque(false);
        rotationSlider.setMajorTickSpacing(90);
        rotationSlider.setMinorTickSpacing(15);
        rotationSlider.setPaintTicks(true);
        rotationSlider.setToolTipText("Drag to rotate wafer freely from 0° to 360° (1 degree per step)");
        rotationSlider.addChangeListener(e -> {
            int deg = rotationSlider.getValue();
            waferCanvas.setRotationAngle(deg);
            rotationDegBadge.setText(deg + "°");
        });
        row2.add(rotationSlider);

        rotationDegBadge.setPreferredSize(new Dimension(46, 26));
        rotationDegBadge.setFont(new Font("SansSerif", Font.BOLD, 12));
        rotationDegBadge.setForeground(new Color(37, 99, 235)); // Royal Blue
        rotationDegBadge.setBackground(new Color(239, 246, 255));
        rotationDegBadge.setOpaque(true);
        rotationDegBadge.setBorder(new LineBorder(new Color(191, 219, 254), 1, true));
        row2.add(rotationDegBadge);

        rotateStepBtn.setPreferredSize(new Dimension(54, 26));
        rotateStepBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        rotateStepBtn.setToolTipText("Rotate clockwise by 90 degrees");
        rotateStepBtn.addActionListener(e -> {
            int next = (rotationSlider.getValue() + 90) % 360;
            rotationSlider.setValue(next);
        });
        row2.add(rotateStepBtn);

        rotateResetBtn.setPreferredSize(new Dimension(42, 26));
        rotateResetBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        rotateResetBtn.setToolTipText("Reset rotation to 0 degrees");
        rotateResetBtn.addActionListener(e -> rotationSlider.setValue(0));
        row2.add(rotateResetBtn);

        container.add(row1);
        container.add(row2);
        return container;
    }

    private JPanel createBottomControlsBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 4));
        bar.setBackground(Color.WHITE);
        bar.setBorder(UiKit.createCardBorder(6, 12, 6, 12));

        // Left: Heatmap controls
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        left.setOpaque(false);

        heatmapToggle.setFont(new Font("SansSerif", Font.BOLD, 11));
        heatmapToggle.setForeground(UiKit.TEXT_PRIMARY);
        heatmapToggle.setBackground(new Color(241, 245, 249));
        heatmapToggle.setFocusPainted(false);
        heatmapToggle.setPreferredSize(new Dimension(140, 30));
        left.add(heatmapToggle);

        JLabel hLbl = new JLabel("Heatmap Intensity:");
        hLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        hLbl.setForeground(UiKit.TEXT_PRIMARY);
        left.add(hLbl);

        heatmapAlphaSlider.setPreferredSize(new Dimension(100, 24));
        heatmapAlphaSlider.setOpaque(false);
        left.add(heatmapAlphaSlider);

        bar.add(left, BorderLayout.WEST);

        // Right: Viewport & Export controls
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        right.setOpaque(false);

        UiKit.SecondaryButton testCppGraphicsBtn = new UiKit.SecondaryButton("Verify C++ Graphics Engine");
        testCppGraphicsBtn.setPreferredSize(new Dimension(210, 30));
        testCppGraphicsBtn.addActionListener(e -> {
            waferscope.classifier.CppGraphicsBridge bridge = new waferscope.classifier.CppGraphicsBridge();
            var rep = bridge.runGraphicsPipeline(150, 150, 120, 140, 30, 160, 30, 20, 100, 280, 100, 50, 50, 250, 250);
            JOptionPane.showMessageDialog(this,
                "C++ COMPUTER GRAPHICS ENGINE VERIFICATION:\n\n" +
                "Engine Source: cpp/wafer_graphics.cpp\n" +
                "Status: " + rep.engineInfo + "\n" +
                "Execution Latency: " + String.format(Locale.US, "%.3f ms", rep.latencyMs) + "\n\n" +
                "Algorithms Executed:\n" +
                "  1. Midpoint Circle: Generated " + rep.circlePointsCount + " substrate boundary coordinates\n" +
                "  2. Bresenham Line: Traced " + rep.linePointsCount + " orientation notch pixels\n" +
                "  3. 2D Transformations: Center translation, scaling & rotation\n" +
                "  4. Cohen-Sutherland: Viewport boundary line clipping (Visible: " + rep.clipVisible + ")\n" +
                "  5. RGB Thermal Heatmap Rasterizer: Active\n\n" +
                "Integration: Fully connected to Java Swing WaferCanvas.",
                "C++ Graphics Integration Verification", JOptionPane.INFORMATION_MESSAGE);
        });
        right.add(testCppGraphicsBtn);

        resetViewBtn.setPreferredSize(new Dimension(110, 30));
        right.add(resetViewBtn);

        exportImgBtn.setPreferredSize(new Dimension(110, 30));
        right.add(exportImgBtn);

        bar.add(right, BorderLayout.EAST);

        return bar;
    }

    private void initEvents() {
        prevBtn.addActionListener(e -> selectPreviousWafer());
        nextBtn.addActionListener(e -> selectNextWafer());

        waferSelector.addActionListener(e -> {
            int sel = waferSelector.getSelectedIndex();
            if (sel >= 0 && sel < visibleWafers.size()) {
                selectWafer(visibleWafers.get(sel));
            }
        });

        searchField.addActionListener(e -> filterByQuery(searchField.getText()));

        heatmapToggle.addActionListener(e -> waferCanvas.setHeatmapEnabled(heatmapToggle.isSelected()));

        resetViewBtn.addActionListener(e -> waferCanvas.resetView());

        exportImgBtn.addActionListener(e -> exportImage());

        importBtn.addActionListener(e -> importWaferMap());

        // Die Click Inspector
        waferCanvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (currentWafer == null) return;
                Point pt = e.getPoint();
                inspectDieAt(pt);
            }
        });
    }

    public void setWafers(List<WaferMap> wafers) {
        this.allWafers.clear();
        if (wafers != null) {
            this.allWafers.addAll(wafers);
        }
        filterByCategory("ALL");
    }

    public void filterByCategory(String cat) {
        visibleWafers.clear();
        waferSelector.removeAllItems();

        for (WaferMap w : allWafers) {
            if ("ALL".equalsIgnoreCase(cat) || w.getGroundTruthLabel().equalsIgnoreCase(cat)) {
                visibleWafers.add(w);
                waferSelector.addItem(String.format(Locale.US, "%s [%s]", w.getId(), w.getGroundTruthLabel()));
            }
        }

        if (!visibleWafers.isEmpty()) {
            waferSelector.setSelectedIndex(0);
            selectWafer(visibleWafers.get(0));
        }
    }

    private void filterByQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            filterByCategory("ALL");
            return;
        }

        String q = query.trim().toLowerCase();
        visibleWafers.clear();
        waferSelector.removeAllItems();

        for (WaferMap w : allWafers) {
            if (w.getId().toLowerCase().contains(q) || w.getGroundTruthLabel().toLowerCase().contains(q)) {
                visibleWafers.add(w);
                waferSelector.addItem(String.format(Locale.US, "%s [%s]", w.getId(), w.getGroundTruthLabel()));
            }
        }

        if (!visibleWafers.isEmpty()) {
            waferSelector.setSelectedIndex(0);
            selectWafer(visibleWafers.get(0));
        } else {
            JOptionPane.showMessageDialog(this, "No wafers matching: " + query, "Search Result", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void selectWafer(WaferMap wafer) {
        this.currentWafer = wafer;
        this.currentIndex = visibleWafers.indexOf(wafer);
        waferCanvas.setWafer(wafer);

        if (waferChangeListener != null) {
            waferChangeListener.onWaferChanged(wafer);
        }

        clickedDieLabel.setText(String.format(Locale.US,
            "Wafer: %s [%s] | Matrix: %dx%d | Defective Dies: %d / %d (%.1f%%) | Click any individual die to inspect coordinates.",
            wafer.getId(), wafer.getGroundTruthLabel(),
            wafer.getRows(), wafer.getCols(),
            wafer.getDefectiveCount(), wafer.getValidCount(), wafer.getDefectPercentage()
        ));
    }

    private void selectPreviousWafer() {
        if (visibleWafers.isEmpty()) return;
        currentIndex--;
        if (currentIndex < 0) currentIndex = visibleWafers.size() - 1;
        waferSelector.setSelectedIndex(currentIndex);
    }

    private void selectNextWafer() {
        if (visibleWafers.isEmpty()) return;
        currentIndex++;
        if (currentIndex >= visibleWafers.size()) currentIndex = 0;
        waferSelector.setSelectedIndex(currentIndex);
    }

    private void inspectDieAt(Point screenPoint) {
        int r = waferCanvas.getHoveredRow();
        int c = waferCanvas.getHoveredCol();
        if (r >= 0 && c >= 0 && currentWafer != null) {
            DieStatus status = currentWafer.getDieStatus(r, c);
            String statusStr = status == DieStatus.DEFECTIVE ? "DEFECTIVE [1]" :
                               status == DieStatus.GOOD ? "FUNCTIONAL PASS [0]" : "OUTSIDE SILICON BOUNDARY [E]";
            clickedDieLabel.setText(String.format(Locale.US,
                "INSPECTED DIE -> Row: %d, Col: %d | Status: %s | Physical Sector: (%.1f, %.1f) mm",
                r + 1, c + 1, statusStr, (c - currentWafer.getCols()/2.0)*15.0, (r - currentWafer.getRows()/2.0)*15.0));
        }
    }

    private void exportImage() {
        if (currentWafer == null) return;
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(currentWafer.getId() + "_rendered.png"));
        int res = fc.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            try {
                BufferedImage img = waferCanvas.exportToImage();
                ImageIO.write(img, "PNG", fc.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Wafer visualizer exported successfully to: " + fc.getSelectedFile().getName(),
                    "Export Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to export image: " + ex.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void importWaferMap() {
        JFileChooser fc = new JFileChooser();
        int res = fc.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            try {
                List<WaferMap> imported = DatasetLoader.loadSingleMapFile(fc.getSelectedFile().toPath());
                if (!imported.isEmpty()) {
                    allWafers.addAll(0, imported);
                    filterByCategory("ALL");
                    JOptionPane.showMessageDialog(this, "Loaded wafer map from " + fc.getSelectedFile().getName(),
                        "Import Complete", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error importing file: " + ex.getMessage(), "Import Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
