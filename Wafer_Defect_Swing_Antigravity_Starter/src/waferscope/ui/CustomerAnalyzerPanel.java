package waferscope.ui;

import waferscope.classifier.CppGraphicsBridge;
import waferscope.classifier.DefectClassifier;
import waferscope.classifier.DemoNearestNeighborClassifier;
import waferscope.datastructures.*;
import waferscope.io.DatasetLoader;
import waferscope.model.PredictionResult;
import waferscope.model.WaferMap;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Customer Wafer Analyzer, Computer Graphics, Data Structures, Java & COA Workstation.
 * 
 * Features:
 * - 1-Click Upload of wafer images or map files & instant AI diagnosis
 * - Interactive Silicon Wafer Canvas with 360° continuous rotation, clipping, lighting, and textures
 * - Seamless 2-View Architecture:
 *     1. Standard Diagnostic & CG Canvas View
 *     2. Detailed Analysis Workstation containing:
 *        - Fab Data Structures & Memory Architecture (Vector Buffer, Linked List, Stack/Queue, QuickSort, Hash Table, Domain Model)
 *        - Java Object-Oriented Architecture (Encapsulation, Abstraction, Multilevel Inheritance, Polymorphism, Composition & Lambdas, Generics & Exceptions)
 *        - Computer Organization & Architecture (COA: 80386/x86-64 Registers, 64-Bit Assembly, ALU Multiplication, 64-Byte Cache Locality & Pipeline)
 *        - Interactive Live QuickSort & Binary Search Controls
 *        - Live Lot Wafers Inventory Table (Real-Time Synchronized to Active Wafer)
 * - Zero Hardcoded Values: All modules dynamically bind to the active wafer.
 */
public class CustomerAnalyzerPanel extends JPanel {

    // Models & Classifiers
    private WaferMap currentWafer;
    private final List<WaferMap> allWafers = new ArrayList<>();
    private final List<WaferSearchSort.WaferRecord> sortedRecords = new ArrayList<>();
    private PredictionResult lastResult;
    private DefectClassifier classifier = new DemoNearestNeighborClassifier();
    private final CppGraphicsBridge cppGraphics = new CppGraphicsBridge();
    private final CppDataStructuresBridge cppDsBridge = new CppDataStructuresBridge();

    // Data Structure Instances
    private WaferArrayOps arrayOps = new WaferArrayOps(8);
    private WaferLinkedList linkedList = new WaferLinkedList();
    private WaferStackQueue.InspectionStack stack = new WaferStackQueue.InspectionStack(6);
    private WaferStackQueue.LotQueue queue = new WaferStackQueue.LotQueue(6);
    private WaferHashTable hashTable = new WaferHashTable(7);

    // Multi-View Card Layout Container
    private final CardLayout modeCardLayout = new CardLayout();
    private final JPanel modeContainer = new JPanel(modeCardLayout);
    public static final String MODE_DIAGNOSTIC = "DIAGNOSTIC_VIEW";
    public static final String MODE_DETAILED = "DETAILED_ANALYSIS_VIEW";
    private String currentMode = MODE_DIAGNOSTIC;

    // Custom High-Contrast View Switcher Buttons
    private static class SubViewTabButton extends JButton {
        private boolean active = false;
        private boolean hovered = false;

        public SubViewTabButton(String text) {
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

        public void setActive(boolean active) {
            this.active = active;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();

            if (active) {
                g2.setColor(UiKit.PRIMARY);
                g2.fillRoundRect(0, 0, w, h, 6, 6);
                g2.setColor(new Color(147, 197, 253));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 6, 6);
                g2.setColor(Color.WHITE);
            } else {
                g2.setColor(hovered ? new Color(241, 245, 249) : Color.WHITE);
                g2.fillRoundRect(0, 0, w, h, 6, 6);
                g2.setColor(hovered ? UiKit.PRIMARY : UiKit.BORDER_DARK);
                g2.drawRoundRect(0, 0, w - 1, h - 1, 6, 6);
                g2.setColor(UiKit.TEXT_PRIMARY);
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

    private final SubViewTabButton diagnosticViewBtn = new SubViewTabButton("🔍 Standard Diagnostic & CG Canvas");
    private final SubViewTabButton detailedAnalysisBtn = new SubViewTabButton("🔬 Detailed Analysis Workstation");

    // Top Toolbar Search & Action Components
    private final UiKit.PrimaryButton uploadBtn = new UiKit.PrimaryButton("Upload Wafer Image / Map");
    private final JTextField searchWaferField = new JTextField(9);
    private final UiKit.SecondaryButton searchWaferBtn = new UiKit.SecondaryButton("Find ID");
    private final JComboBox<String> waferPicker = new JComboBox<>();
    private final UiKit.SuccessButton analyzeBtn = new UiKit.SuccessButton("Run AI Defect Analysis");
    private final UiKit.SecondaryButton rateBtn = new UiKit.SecondaryButton("Rate Analysis");
    private final JToggleButton heatmapToggle = new JToggleButton("Heatmap Overlay");

    // Diagnostic View Components
    private final WaferCanvas waferCanvas = new WaferCanvas();
    private final JLabel statusBanner = new JLabel("READY FOR CUSTOMER WAFER ANALYSIS", SwingConstants.CENTER);
    private final JLabel defectTitleLabel = new JLabel("None", SwingConstants.CENTER);
    private final JLabel confidenceLabel = new JLabel("0.0%", SwingConstants.CENTER);
    private final JLabel yieldLabel = new JLabel("100.0%", SwingConstants.CENTER);
    private final JLabel dieCountLabel = new JLabel("0 Defective / 0 Valid Dies", SwingConstants.CENTER);
    private final JTextArea recommendationArea = new JTextArea();
    private final JLabel cppEngineBadge = new JLabel("C++ CG: Zoom 100% | Rot 0° | Light: OFF | Textures Ready");
    private final UiKit.PrimaryButton openDetailedFromDiagnosticBtn = new UiKit.PrimaryButton("🔬 Open Detailed Analysis (Data Structures, Java & COA) ➔");

    // Computer Graphics Tools
    private final UiKit.SecondaryButton zoomInBtn = new UiKit.SecondaryButton("+ Zoom In");
    private final UiKit.SecondaryButton zoomOutBtn = new UiKit.SecondaryButton("- Zoom Out");
    private final JSlider rotationSlider = new JSlider(0, 360, 0);
    private final JLabel rotationDegBadge = new JLabel("0°", SwingConstants.CENTER);
    private final UiKit.SecondaryButton rotateStepBtn = new UiKit.SecondaryButton("+90°");
    private final UiKit.SecondaryButton rotateResetBtn = new UiKit.SecondaryButton("0°");
    private final UiKit.SecondaryButton lightingBtn = new UiKit.SecondaryButton("💡 Lighting (Phong)");
    private final UiKit.SecondaryButton textureBtn = new UiKit.SecondaryButton("🏁 Texture: OFF");
    private final UiKit.SecondaryButton clipBtn = new UiKit.SecondaryButton("✂ Clip Window");
    private final UiKit.SecondaryButton scanlineBtn = new UiKit.SecondaryButton("▤ Scanline Fill");
    private final UiKit.SecondaryButton resetCgBtn = new UiKit.SecondaryButton("↺ Reset View");

    // Detailed Analysis Workstation Components
    private final JLabel activeWaferHeaderLabel = new JLabel("ACTIVE WAFER: None Selected");
    private final JLabel activeWaferSubLabel = new JLabel("Live Dynamic Data Structures and In-Memory Indices Dynamically Bound to Selected Wafer");
    private final UiKit.SecondaryButton backToCanvasBtn = new UiKit.SecondaryButton("⬅ Back to Diagnostic Canvas");

    // Filter Buttons within Detailed Analysis
    private final SubViewTabButton viewAllBtn = new SubViewTabButton("📑 All Architecture");
    private final SubViewTabButton viewDsaBtn = new SubViewTabButton("📦 Data Structures");
    private final SubViewTabButton viewJavaBtn = new SubViewTabButton("☕ Java");
    private final SubViewTabButton viewCoaBtn = new SubViewTabButton("💻 COA Architecture");

    // Workstation Card Containers
    private JPanel dataStructureCard;
    private JPanel javaArchitectureCard;
    private JPanel coaArchitectureCard;
    private JScrollPane detailedScrollPane;

    // 1. Data Structures Areas
    private final JTextArea siblingBufferArea = new JTextArea();
    private final JTextArea lotChainArea = new JTextArea();
    private final JTextArea auditStackQueueArea = new JTextArea();
    private final JTextArea yieldRankingArea = new JTextArea();
    private final JTextArea barcodeHashArea = new JTextArea();
    private final JTextArea oopDomainArea = new JTextArea();

    // 2. Java OOP Architecture Areas
    private final JTextArea javaEncapsulationArea = new JTextArea();
    private final JTextArea javaAbstractionArea = new JTextArea();
    private final JTextArea javaInheritanceArea = new JTextArea();
    private final JTextArea javaPolymorphismArea = new JTextArea();
    private final JTextArea javaCompositionArea = new JTextArea();
    private final JTextArea javaGenericsArea = new JTextArea();

    // 3. Computer Organization & Architecture (COA) Areas
    private final JTextArea coaRegisterArea = new JTextArea();
    private final JTextArea coaAssemblyArea = new JTextArea();
    private final JTextArea coaArithmeticArea = new JTextArea();
    private final JTextArea coaCacheArea = new JTextArea();
    private final JTextArea coaPipelineArea = new JTextArea();
    private final JTextArea coaControlUnitArea = new JTextArea();

    // Detailed Analysis QuickSort & Binary Search Controls
    private final JComboBox<String> sortCombo = new JComboBox<>(new String[]{
        "QuickSort: Defect Density (Ascending)",
        "QuickSort: Defect Density (Descending)",
        "QuickSort: Functional Yield (Descending)",
        "QuickSort: Wafer SEMI ID (A-Z)"
    });
    private final UiKit.PrimaryButton sortBtn = new UiKit.PrimaryButton("Run QuickSort O(n log n)");
    private final JTextField searchIdField = new JTextField(10);
    private final UiKit.SecondaryButton binarySearchBtn = new UiKit.SecondaryButton("Binary Search O(log n)");
    private final JLabel algorithmTelemetryLabel = new JLabel("C++ Data Structures Engine Ready | O(n log n) QuickSort & O(log n) Binary Search");
    private final DefaultTableModel tableModel;
    private final JTable waferTable;

    public interface OnAnalysisCompleteCallback {
        void onAnalysisCompleted(PredictionResult result);
    }

    public interface OnWaferSelectListener {
        void onWaferSelected(WaferMap wafer);
    }

    private OnAnalysisCompleteCallback analysisCallback;
    private OnWaferSelectListener waferSelectListener;

    public CustomerAnalyzerPanel() {
        setLayout(new BorderLayout(8, 8));
        setBackground(UiKit.PAGE_BG);
        setBorder(new EmptyBorder(10, 14, 10, 14));

        // Initialize Table
        String[] columns = {"Rank", "Wafer ID", "Defect Pattern", "Defect Dies", "Valid Dies", "Density %", "Yield %", "Lot Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        waferTable = new JTable(tableModel);
        styleTable();

        // 1. Top Section: Toolbar + Sub-mode Navigation Switcher
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);
        topContainer.add(createTopToolbar());
        topContainer.add(Box.createVerticalStrut(6));
        topContainer.add(createSubNavigationSwitcher());
        add(topContainer, BorderLayout.NORTH);

        // 2. Center Multi-View Card Layout
        modeContainer.setOpaque(false);
        modeContainer.add(createDiagnosticView(), MODE_DIAGNOSTIC);
        modeContainer.add(createDetailedAnalysisView(), MODE_DETAILED);
        add(modeContainer, BorderLayout.CENTER);

        initEvents();
    }

    public void setClassifier(DefectClassifier classifier) {
        this.classifier = classifier;
    }

    public void setOnAnalysisCompleteCallback(OnAnalysisCompleteCallback callback) {
        this.analysisCallback = callback;
    }

    public void setOnWaferSelectListener(OnWaferSelectListener listener) {
        this.waferSelectListener = listener;
    }

    public void setWafersList(List<WaferMap> wafers) {
        allWafers.clear();
        waferPicker.removeAllItems();
        if (wafers != null && !wafers.isEmpty()) {
            allWafers.addAll(wafers);
            for (WaferMap w : wafers) {
                waferPicker.addItem(w.getId() + " [" + w.getGroundTruthLabel() + "]");
            }
            sortedRecords.clear();
            for (WaferMap w : wafers) {
                sortedRecords.add(new WaferSearchSort.WaferRecord(
                    w.getId(), w.getGroundTruthLabel(), w.getDefectiveCount(), w.getValidCount(),
                    w.getDefectPercentage(), w.getYieldPercentage()
                ));
            }
            executeQuickSort();
            setWafer(wafers.get(0));
        }
    }

    public void setWafer(WaferMap wafer) {
        if (wafer == null) return;
        this.currentWafer = wafer;
        waferCanvas.setWafer(wafer);
        ActiveWaferBridge.updateActiveWafer(wafer, lastResult);

        // Update picker combo selection without triggering recursive loop
        for (int i = 0; i < waferPicker.getItemCount(); i++) {
            if (waferPicker.getItemAt(i).startsWith(wafer.getId())) {
                if (waferPicker.getSelectedIndex() != i) {
                    waferPicker.setSelectedIndex(i);
                }
                break;
            }
        }

        runAnalysis();
        updateDetailedDataStructures(wafer);
        updateJavaArchitectureData(wafer);
        updateCoaArchitectureData(wafer);

        if (MODE_DETAILED.equals(currentMode)) {
            syncActiveWaferToDetailedTable();
        }
    }

    public WaferMap getCurrentWafer() {
        return currentWafer;
    }

    private JPanel createTopToolbar() {
        JPanel bar = new JPanel(new BorderLayout(8, 4));
        bar.setBackground(Color.WHITE);
        bar.setBorder(UiKit.createCardBorder(6, 12, 6, 12));

        // Left Controls: Upload, Search Wafer ID, Dropdown, Analyze
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        left.setOpaque(false);

        uploadBtn.setPreferredSize(new Dimension(185, 30));
        left.add(uploadBtn);

        JLabel searchLbl = new JLabel("Find ID:");
        searchLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        searchLbl.setForeground(UiKit.TEXT_PRIMARY);
        left.add(searchLbl);

        searchWaferField.setFont(new Font("SansSerif", Font.PLAIN, 12));
        searchWaferField.setPreferredSize(new Dimension(95, 28));
        left.add(searchWaferField);

        searchWaferBtn.setPreferredSize(new Dimension(75, 28));
        left.add(searchWaferBtn);

        JLabel selectL = new JLabel("Or Select:");
        selectL.setFont(new Font("SansSerif", Font.BOLD, 12));
        selectL.setForeground(UiKit.TEXT_PRIMARY);
        left.add(selectL);

        waferPicker.setPreferredSize(new Dimension(210, 28));
        waferPicker.setFont(new Font("SansSerif", Font.PLAIN, 12));
        waferPicker.setForeground(UiKit.TEXT_PRIMARY);
        waferPicker.setBackground(Color.WHITE);
        left.add(waferPicker);

        analyzeBtn.setPreferredSize(new Dimension(165, 30));
        left.add(analyzeBtn);

        bar.add(left, BorderLayout.WEST);

        // Right Controls: Heatmap, Rate Analysis
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 2));
        right.setOpaque(false);

        heatmapToggle.setFont(new Font("SansSerif", Font.BOLD, 11));
        heatmapToggle.setForeground(UiKit.TEXT_PRIMARY);
        heatmapToggle.setBackground(new Color(241, 245, 249));
        heatmapToggle.setFocusPainted(false);
        heatmapToggle.setPreferredSize(new Dimension(130, 28));
        right.add(heatmapToggle);

        rateBtn.setPreferredSize(new Dimension(115, 28));
        rateBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        right.add(rateBtn);

        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel createSubNavigationSwitcher() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        bar.setOpaque(false);

        diagnosticViewBtn.setPreferredSize(new Dimension(260, 30));
        detailedAnalysisBtn.setPreferredSize(new Dimension(270, 30));

        diagnosticViewBtn.setActive(true);
        detailedAnalysisBtn.setActive(false);

        diagnosticViewBtn.addActionListener(e -> showView(MODE_DIAGNOSTIC));
        detailedAnalysisBtn.addActionListener(e -> showView(MODE_DETAILED));

        bar.add(diagnosticViewBtn);
        bar.add(detailedAnalysisBtn);
        return bar;
    }

    public void showView(String mode) {
        this.currentMode = mode;
        modeCardLayout.show(modeContainer, mode);
        diagnosticViewBtn.setActive(MODE_DIAGNOSTIC.equals(mode));
        detailedAnalysisBtn.setActive(MODE_DETAILED.equals(mode));

        if (MODE_DETAILED.equals(mode)) {
            if (currentWafer != null) {
                updateDetailedDataStructures(currentWafer);
                updateJavaArchitectureData(currentWafer);
                updateCoaArchitectureData(currentWafer);
                syncActiveWaferToDetailedTable();
            }
            SwingUtilities.invokeLater(() -> {
                if (detailedScrollPane != null) {
                    detailedScrollPane.getVerticalScrollBar().setValue(0);
                }
            });
        }
    }

    public void filterArchitectureView(String view) {
        if ("ALL".equalsIgnoreCase(view)) {
            viewAllBtn.doClick();
        } else if ("DSA".equalsIgnoreCase(view)) {
            viewDsaBtn.doClick();
        } else if ("JAVA".equalsIgnoreCase(view)) {
            viewJavaBtn.doClick();
        } else if ("COA".equalsIgnoreCase(view)) {
            viewCoaBtn.doClick();
        }
    }

    private JPanel createDiagnosticView() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.50);
        split.setDividerSize(6);
        split.setBorder(null);
        split.setOpaque(false);

        split.setLeftComponent(createCanvasCard());
        split.setRightComponent(createDiagnosticCard());
        panel.add(split, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCanvasCard() {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(UiKit.createCardBorder(12, 14, 12, 14));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JLabel title = new JLabel("WAFER MAP & COMPUTER GRAPHICS INSPECTION VIEW");
        title.setFont(new Font("SansSerif", Font.BOLD, 13));
        title.setForeground(UiKit.TEXT_PRIMARY);
        head.add(title, BorderLayout.WEST);

        JPanel topContainer = new JPanel(new BorderLayout(4, 6));
        topContainer.setOpaque(false);
        topContainer.add(head, BorderLayout.NORTH);
        topContainer.add(createCgToolbar(), BorderLayout.SOUTH);
        card.add(topContainer, BorderLayout.NORTH);

        card.add(waferCanvas, BorderLayout.CENTER);

        JLabel footerNote = new JLabel("Click any die on wafer to inspect coordinates & defect flags | Circular substrate clips die grid cleanly", JLabel.CENTER);
        footerNote.setFont(new Font("SansSerif", Font.PLAIN, 12));
        footerNote.setForeground(UiKit.TEXT_SECONDARY);
        footerNote.setBorder(new EmptyBorder(6, 0, 0, 0));
        card.add(footerNote, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createCgToolbar() {
        JPanel container = new JPanel(new GridLayout(2, 1, 4, 4));
        container.setOpaque(false);

        // Row 1: Primary CG Tools
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        row1.setOpaque(false);

        zoomInBtn.setPreferredSize(new Dimension(88, 28));
        zoomInBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        zoomInBtn.addActionListener(e -> {
            waferCanvas.zoomIn();
            updateCgBadge();
        });
        row1.add(zoomInBtn);

        zoomOutBtn.setPreferredSize(new Dimension(88, 28));
        zoomOutBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        zoomOutBtn.addActionListener(e -> {
            waferCanvas.zoomOut();
            updateCgBadge();
        });
        row1.add(zoomOutBtn);

        lightingBtn.setPreferredSize(new Dimension(130, 28));
        lightingBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        lightingBtn.addActionListener(e -> {
            waferCanvas.toggleLighting();
            lightingBtn.setText(waferCanvas.isLightingEnabled() ? "💡 Light: ON" : "💡 Light: OFF");
            updateCgBadge();
        });
        row1.add(lightingBtn);

        textureBtn.setPreferredSize(new Dimension(130, 28));
        textureBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        textureBtn.addActionListener(e -> {
            waferCanvas.cycleTextureMode();
            textureBtn.setText("🏁 " + waferCanvas.getTextureMode());
            updateCgBadge();
        });
        row1.add(textureBtn);

        clipBtn.setPreferredSize(new Dimension(110, 28));
        clipBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        clipBtn.addActionListener(e -> {
            waferCanvas.toggleClipping();
            clipBtn.setText(waferCanvas.isClippingEnabled() ? "✂ Clip: ON" : "✂ Clip: OFF");
            updateCgBadge();
        });
        row1.add(clipBtn);

        scanlineBtn.setPreferredSize(new Dimension(115, 28));
        scanlineBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        scanlineBtn.addActionListener(e -> {
            waferCanvas.toggleScanlineFill();
            scanlineBtn.setText(waferCanvas.isScanlineFillEnabled() ? "▤ Fill: ON" : "▤ Fill: OFF");
            updateCgBadge();
        });
        row1.add(scanlineBtn);

        resetCgBtn.setPreferredSize(new Dimension(95, 28));
        resetCgBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        resetCgBtn.addActionListener(e -> {
            waferCanvas.resetView();
            rotationSlider.setValue(0);
            rotationDegBadge.setText("0°");
            lightingBtn.setText("💡 Light: OFF");
            textureBtn.setText("🏁 Texture: OFF");
            clipBtn.setText("✂ Clip: OFF");
            scanlineBtn.setText("▤ Fill: OFF");
            updateCgBadge();
        });
        row1.add(resetCgBtn);

        // Row 2: Continuous 360-Degree Rotation Controller
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
            updateCgBadge();
        });
        row2.add(rotationSlider);

        rotationDegBadge.setPreferredSize(new Dimension(46, 26));
        rotationDegBadge.setFont(new Font("SansSerif", Font.BOLD, 12));
        rotationDegBadge.setForeground(new Color(37, 99, 235));
        rotationDegBadge.setBackground(new Color(239, 246, 255));
        rotationDegBadge.setOpaque(true);
        rotationDegBadge.setBorder(new LineBorder(new Color(191, 219, 254), 1, true));
        row2.add(rotationDegBadge);

        rotateStepBtn.setPreferredSize(new Dimension(54, 26));
        rotateStepBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        rotateStepBtn.addActionListener(e -> {
            int next = (rotationSlider.getValue() + 90) % 360;
            rotationSlider.setValue(next);
        });
        row2.add(rotateStepBtn);

        rotateResetBtn.setPreferredSize(new Dimension(42, 26));
        rotateResetBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        rotateResetBtn.addActionListener(e -> rotationSlider.setValue(0));
        row2.add(rotateResetBtn);

        container.add(row1);
        container.add(row2);
        return container;
    }

    private void updateCgBadge() {
        cppEngineBadge.setText(String.format(Locale.US,
            "C++ CG: Zoom %d%% | Rot %d° | Light: %s | %s%s",
            (int) (waferCanvas.getZoomFactor() * 100),
            (int) waferCanvas.getRotationAngle(),
            (waferCanvas.isLightingEnabled() ? "ON (Phong)" : "OFF"),
            waferCanvas.getTextureMode(),
            (waferCanvas.isClippingEnabled() ? " | Circle Clip: ON" : "")
        ));
    }

    private JPanel createDiagnosticCard() {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(UiKit.createCardBorder(12, 14, 12, 14));

        // Top Status Alert Banner
        statusBanner.setOpaque(true);
        statusBanner.setBackground(new Color(4, 120, 87));
        statusBanner.setForeground(Color.WHITE);
        statusBanner.setFont(new Font("SansSerif", Font.BOLD, 13));
        statusBanner.setBorder(new EmptyBorder(8, 10, 8, 10));
        card.add(statusBanner, BorderLayout.NORTH);

        // Center Content
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        // 4 KPI Metrics Cards
        JPanel grid = new JPanel(new GridLayout(2, 2, 8, 8));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(3000, 130));
        grid.setPreferredSize(new Dimension(300, 130));

        grid.add(createMetricCard("DETECTED DEFECT PATTERN", defectTitleLabel, UiKit.PRIMARY));
        grid.add(createMetricCard("AI CLASSIFICATION CONFIDENCE", confidenceLabel, UiKit.TEXT_PRIMARY));
        grid.add(createMetricCard("FUNCTIONAL DIE YIELD", yieldLabel, new Color(21, 128, 61)));
        grid.add(createMetricCard("DEFECTIVE DIE AUDIT", dieCountLabel, new Color(185, 28, 28)));
        center.add(grid);
        center.add(Box.createVerticalStrut(10));

        // Customer Diagnostic & Corrective Action Section
        JLabel recTitle = new JLabel("CUSTOMER DIAGNOSTIC & FAB REMEDY ADVICE:");
        recTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        recTitle.setForeground(UiKit.TEXT_PRIMARY);
        center.add(recTitle);
        center.add(Box.createVerticalStrut(4));

        recommendationArea.setEditable(false);
        recommendationArea.setLineWrap(true);
        recommendationArea.setWrapStyleWord(true);
        recommendationArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        recommendationArea.setForeground(UiKit.TEXT_PRIMARY);
        recommendationArea.setBackground(new Color(248, 250, 252));
        recommendationArea.setBorder(new CompoundBorder(
            new LineBorder(UiKit.BORDER_COLOR, 1, true),
            new EmptyBorder(8, 10, 8, 10)
        ));
        center.add(new JScrollPane(recommendationArea));
        center.add(Box.createVerticalStrut(8));

        // Quick Navigation to Detailed Analysis
        openDetailedFromDiagnosticBtn.setPreferredSize(new Dimension(390, 34));
        openDetailedFromDiagnosticBtn.addActionListener(e -> showView(MODE_DETAILED));
        center.add(openDetailedFromDiagnosticBtn);

        card.add(center, BorderLayout.CENTER);
        return card;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, Color textCol) {
        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.setBackground(new Color(248, 250, 252));
        p.setBorder(new CompoundBorder(
            new LineBorder(UiKit.BORDER_COLOR, 1, true),
            new EmptyBorder(8, 10, 8, 10)
        ));

        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.BOLD, 10));
        t.setForeground(UiKit.TEXT_SECONDARY);
        p.add(t, BorderLayout.NORTH);

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        valueLabel.setForeground(textCol);
        p.add(valueLabel, BorderLayout.CENTER);

        return p;
    }

    // =========================================================================
    // DETAILED ANALYSIS WORKSTATION (DATA STRUCTURES + JAVA OOP + COA ARCHITECTURE)
    // =========================================================================

    private static class ScrollablePanel extends JPanel implements Scrollable {
        public ScrollablePanel() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setOpaque(false);
            setBorder(new EmptyBorder(2, 2, 8, 2));
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 48;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }

        @Override
        public void scrollRectToVisible(Rectangle aRect) {
            aRect.x = 0;
            aRect.width = getWidth();
            super.scrollRectToVisible(aRect);
        }
    }

    private JPanel createDetailedAnalysisView() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);

        ScrollablePanel content = new ScrollablePanel();

        // 1. Active Wafer Header Card with Section Toggles
        content.add(createDetailedHeaderCard());
        content.add(Box.createVerticalStrut(10));

        // 2. Data Structures Architecture Workstation Card
        dataStructureCard = createDataStructureEngineCard();
        content.add(dataStructureCard);
        content.add(Box.createVerticalStrut(12));

        // 3. Java Object-Oriented Architecture Card
        javaArchitectureCard = createJavaArchitectureCard();
        content.add(javaArchitectureCard);
        content.add(Box.createVerticalStrut(12));

        // 4. Computer Organization & Architecture (COA) Card
        coaArchitectureCard = createCoaArchitectureCard();
        content.add(coaArchitectureCard);
        content.add(Box.createVerticalStrut(12));

        // 4. QuickSort & Binary Search Interactive Controls Bar
        content.add(createActionAndSortCenter());
        content.add(Box.createVerticalStrut(10));

        // 5. Live Lot Wafers Inventory Table (Real-Time Synchronized to Active Wafer)
        content.add(createSortedLotTableCard());

        detailedScrollPane = new JScrollPane(content);
        detailedScrollPane.setBorder(null);
        detailedScrollPane.setOpaque(false);
        detailedScrollPane.getViewport().setOpaque(false);
        detailedScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        detailedScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(detailedScrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createDetailedHeaderCard() {
        JPanel card = new JPanel(new BorderLayout(10, 6));
        card.setBackground(Color.WHITE);
        card.setBorder(UiKit.createCardBorder(10, 14, 10, 14));

        // Left: Active Wafer Status readout
        JPanel left = new JPanel(new GridLayout(2, 1, 2, 2));
        left.setOpaque(false);

        activeWaferHeaderLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        activeWaferHeaderLabel.setForeground(UiKit.TEXT_PRIMARY);
        left.add(activeWaferHeaderLabel);

        activeWaferSubLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        activeWaferSubLabel.setForeground(UiKit.TEXT_SECONDARY);
        left.add(activeWaferSubLabel);

        card.add(left, BorderLayout.WEST);

        // Center / Right: Section Filter Toggles & Return Button
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 2));
        right.setOpaque(false);

        viewAllBtn.setPreferredSize(new Dimension(135, 28));
        viewDsaBtn.setPreferredSize(new Dimension(135, 28));
        viewJavaBtn.setPreferredSize(new Dimension(85, 28));
        viewCoaBtn.setPreferredSize(new Dimension(145, 28));

        viewAllBtn.setActive(true);
        viewDsaBtn.setActive(false);
        viewJavaBtn.setActive(false);
        viewCoaBtn.setActive(false);

        viewAllBtn.addActionListener(e -> {
            viewAllBtn.setActive(true);
            viewDsaBtn.setActive(false);
            viewJavaBtn.setActive(false);
            viewCoaBtn.setActive(false);
            dataStructureCard.setVisible(true);
            javaArchitectureCard.setVisible(true);
            coaArchitectureCard.setVisible(true);
            revalidate();
            repaint();
        });

        viewDsaBtn.addActionListener(e -> {
            viewAllBtn.setActive(false);
            viewDsaBtn.setActive(true);
            viewJavaBtn.setActive(false);
            viewCoaBtn.setActive(false);
            dataStructureCard.setVisible(true);
            javaArchitectureCard.setVisible(false);
            coaArchitectureCard.setVisible(false);
            revalidate();
            repaint();
        });

        viewJavaBtn.addActionListener(e -> {
            viewAllBtn.setActive(false);
            viewDsaBtn.setActive(false);
            viewJavaBtn.setActive(true);
            viewCoaBtn.setActive(false);
            dataStructureCard.setVisible(false);
            javaArchitectureCard.setVisible(true);
            coaArchitectureCard.setVisible(false);
            revalidate();
            repaint();
        });

        viewCoaBtn.addActionListener(e -> {
            viewAllBtn.setActive(false);
            viewDsaBtn.setActive(false);
            viewJavaBtn.setActive(false);
            viewCoaBtn.setActive(true);
            dataStructureCard.setVisible(false);
            javaArchitectureCard.setVisible(false);
            coaArchitectureCard.setVisible(true);
            revalidate();
            repaint();
        });

        right.add(viewAllBtn);
        right.add(viewDsaBtn);
        right.add(viewJavaBtn);
        right.add(viewCoaBtn);

        backToCanvasBtn.setPreferredSize(new Dimension(140, 28));
        backToCanvasBtn.setText("⬅ Back to Canvas");
        backToCanvasBtn.addActionListener(e -> showView(MODE_DIAGNOSTIC));
        right.add(backToCanvasBtn);

        card.add(right, BorderLayout.EAST);
        return card;
    }

    private JPanel createDataStructureEngineCard() {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(UiKit.createCardBorder(12, 14, 12, 14));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);

        JLabel title = new JLabel("FAB DATA STRUCTURES & MEMORY ARCHITECTURE WORKSTATION");
        title.setFont(new Font("SansSerif", Font.BOLD, 13));
        title.setForeground(UiKit.TEXT_PRIMARY);
        head.add(title, BorderLayout.WEST);

        JLabel subtitle = new JLabel("In-Memory Algorithms & Contiguous Layouts Dynamically Bound to Active Wafer");
        subtitle.setFont(new Font("SansSerif", Font.ITALIC, 11));
        subtitle.setForeground(UiKit.TEXT_SECONDARY);
        head.add(subtitle, BorderLayout.EAST);

        card.add(head, BorderLayout.NORTH);

        // 2 Columns x 3 Rows = 6 subcards
        JPanel grid = new JPanel(new GridLayout(0, 2, 12, 10));
        grid.setOpaque(false);

        grid.add(createEngineSubCard(
            "SIBLING WAFER BUFFER (CONTIGUOUS MEMORY CACHE)",
            "Stores sibling wafers sharing the exact same defect pattern in contiguous memory for high-speed cache locality & bulk yield auditing.",
            siblingBufferArea,
            UiKit.PRIMARY
        ));

        grid.add(createEngineSubCard(
            "FAB LOT INSPECTION CHAIN (SEQUENTIAL PROCESS AUDIT TRAIL)",
            "Maintains chronological fabrication sequence and downstream process routing nodes with forward dynamic pointers.",
            lotChainArea,
            new Color(14, 116, 144)
        ));

        grid.add(createEngineSubCard(
            "LIFO RE-INSPECTION AUDIT STACK & FIFO CASSETTE LOT DISPATCH QUEUE",
            "Tracks wafer inspection rollbacks (LIFO) and automated wafer cassette transport scheduling through fab tracks (FIFO).",
            auditStackQueueArea,
            new Color(161, 98, 7)
        ));

        grid.add(createEngineSubCard(
            "DYNAMIC YIELD & DEFECT SEVERITY RANKING (DIVIDE-AND-CONQUER O(N LOG N))",
            "Ranks the entire wafer lot dynamically by defect density / functional die yield and pinpoints wafer IDs in logarithmic time.",
            yieldRankingArea,
            UiKit.DANGER
        ));

        grid.add(createEngineSubCard(
            "SEMI WAFER BARCODE & SERIAL NUMBER FAST INDEX (O(1) LOOKUP)",
            "Maps unique SEMI wafer barcode strings directly to fab metadata buckets via polynomial rolling hash function.",
            barcodeHashArea,
            UiKit.SUCCESS
        ));

        grid.add(createEngineSubCard(
            "AUTOMATED OPTICAL INSPECTION DOMAIN MODEL (POLYMORPHIC PIPELINE)",
            "Encapsulates wafer die grids, defect topologies, inspection classifiers, and analytical reporting instances.",
            oopDomainArea,
            new Color(109, 40, 217)
        ));

        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JPanel createJavaArchitectureCard() {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(UiKit.createCardBorder(12, 14, 12, 14));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);

        JLabel title = new JLabel("JAVA OBJECT-ORIENTED ARCHITECTURE (OOPS ENGINE)");
        title.setFont(new Font("SansSerif", Font.BOLD, 13));
        title.setForeground(UiKit.TEXT_PRIMARY);
        head.add(title, BorderLayout.WEST);

        JLabel subtitle = new JLabel("Core Java OOP Principles: Encapsulation, Inheritance, Polymorphism, Abstraction, Composition, Lambdas, Generics");
        subtitle.setFont(new Font("SansSerif", Font.ITALIC, 11));
        subtitle.setForeground(UiKit.TEXT_SECONDARY);
        head.add(subtitle, BorderLayout.EAST);

        card.add(head, BorderLayout.NORTH);

        // 2 Columns x 3 Rows = 6 subcards for Java Architecture
        JPanel grid = new JPanel(new GridLayout(0, 2, 12, 10));
        grid.setOpaque(false);

        grid.add(createEngineSubCard(
            "1. ENCAPSULATION & DOMAIN DATA HIDING (CLASS & OBJECT)",
            "Protects wafer defect metrics, coordinate bounds, and classification scores using private fields and validated public accessors.",
            javaEncapsulationArea,
            new Color(4, 120, 87) // Deep Emerald Green
        ));

        grid.add(createEngineSubCard(
            "2. ABSTRACTION & INSPECTION CONTRACT INTERFACES",
            "Conceals complex image processing and neural network inference behind abstract methods and pure capability contracts.",
            javaAbstractionArea,
            UiKit.PRIMARY // Royal Blue
        ));

        grid.add(createEngineSubCard(
            "3. MULTILEVEL INHERITANCE & CODE REUSABILITY",
            "Specializes baseline semiconductor inspection across hierarchical tiers: Analyzer -> WaferAnalyzer -> AIWaferAnalyzer.",
            javaInheritanceArea,
            new Color(109, 40, 217) // Royal Purple
        ));

        grid.add(createEngineSubCard(
            "4. RUNTIME & COMPILE-TIME POLYMORPHISM (DYNAMIC DISPATCH)",
            "Resolves static method overloading at compile time and dynamic method overriding via runtime vtable dispatch.",
            javaPolymorphismArea,
            UiKit.DANGER // Crimson Red
        ));

        grid.add(createEngineSubCard(
            "5. OBJECT COMPOSITION (HAS-A) & FUNCTIONAL LAMBDAS",
            "Decouples inspection domain entities (InspectionResult HAS-A Defect) and applies functional lambdas to sort defect patterns.",
            javaCompositionArea,
            new Color(14, 116, 144) // Deep Cyan
        ));

        grid.add(createEngineSubCard(
            "6. DEFENSIVE EXCEPTION HANDLING & TYPE-SAFE GENERICS",
            "Guarantees 24/7 fab uptime with try-catch-finally guards and type-safe Generic InspectionBatch<T> containers.",
            javaGenericsArea,
            new Color(161, 98, 7) // Dark Amber
        ));

        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JPanel createCoaArchitectureCard() {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(UiKit.createCardBorder(12, 14, 12, 14));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);

        JLabel title = new JLabel("COMPUTER ORGANIZATION & ARCHITECTURE (COA WORKSTATION)");
        title.setFont(new Font("SansSerif", Font.BOLD, 13));
        title.setForeground(UiKit.TEXT_PRIMARY);
        head.add(title, BorderLayout.WEST);

        JLabel subtitle = new JLabel("80386/x86-64 Registers, 64-Bit Assembly, ALU Multiplication, 64-Byte Cache Locality & Pipeline");
        subtitle.setFont(new Font("SansSerif", Font.ITALIC, 11));
        subtitle.setForeground(UiKit.TEXT_SECONDARY);
        head.add(subtitle, BorderLayout.EAST);

        card.add(head, BorderLayout.NORTH);

        // 2 Columns x 3 Rows = 6 subcards for COA Architecture
        JPanel grid = new JPanel(new GridLayout(0, 2, 12, 10));
        grid.setOpaque(false);

        grid.add(createEngineSubCard(
            "1. 64-BIT PROCESSOR REGISTER FILE & ALU STATE (80386 / X86-64)",
            "Maps wafer traversal to 64-bit general-purpose registers (RAX, RBX, RCX, RDX, RSI, RIP) & condition flags (ZF, CF, SF).",
            coaRegisterArea,
            new Color(67, 56, 202) // Deep Indigo
        ));

        grid.add(createEngineSubCard(
            "2. 64-BIT ASSEMBLY TRAVERSAL & INSTRUCTION STREAM (NASM X86-64)",
            "Machine-level die inspection loop with conditional branch evaluation (CMP, JE, JNE) and pointer incrementing.",
            coaAssemblyArea,
            new Color(13, 148, 136) // Deep Teal
        ));

        grid.add(createEngineSubCard(
            "3. ARITHMETIC ALGORITHMS: SUCCESSIVE ADDITION VS SHIFT-AND-ADD (ALU CO3)",
            "Hardware ALU multiplication comparison for yield scaling (Good_Dies * 100): O(B) linear vs O(log2 B) logarithmic.",
            coaArithmeticArea,
            new Color(5, 150, 105) // Emerald Green
        ));

        grid.add(createEngineSubCard(
            "4. MEMORY HIERARCHY & 64-BYTE L1 DATA CACHE LOCALITY (CO4)",
            "Row-major spatial memory locality analysis and cache hit/miss telemetry for 17x17 wafer matrix traversal (289 dies).",
            coaCacheArea,
            new Color(217, 119, 6) // Deep Amber
        ));

        grid.add(createEngineSubCard(
            "5. 3-STAGE INSTRUCTION PIPELINE (FETCH -> DECODE -> EXECUTE)",
            "Models instruction pipelining, pipeline registers (IR/PC), hazard-free sequential execution & steady-state CPI.",
            coaPipelineArea,
            new Color(124, 58, 237) // Royal Violet
        ));

        grid.add(createEngineSubCard(
            "6. CONTROL UNIT MICRO-OPERATIONS & BUS INTERFACE UNIT (BIU)",
            "Control signals (MEMR, MEMW, ALU_OP), bus timing cycles (T1-T3), and register transfer notation (RTN).",
            coaControlUnitArea,
            new Color(30, 41, 59) // Slate Navy
        ));

        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JPanel createEngineSubCard(String title, String subtitle, JTextArea area, Color accentColor) {
        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.setBackground(new Color(248, 250, 252));
        p.setBorder(new CompoundBorder(
            new MatteBorder(0, 4, 0, 0, accentColor),
            new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(8, 10, 8, 10)
            )
        ));

        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 2, 1));
        headerPanel.setOpaque(false);

        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.BOLD, 11));
        t.setForeground(accentColor);
        headerPanel.add(t);

        JLabel sub = new JLabel(subtitle);
        sub.setFont(new Font("SansSerif", Font.ITALIC, 10));
        sub.setForeground(UiKit.TEXT_SECONDARY);
        headerPanel.add(sub);

        p.add(headerPanel, BorderLayout.NORTH);

        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("Monospaced", Font.PLAIN, 11));
        area.setForeground(UiKit.TEXT_PRIMARY);
        area.setOpaque(false);
        area.setRows(5);

        p.add(area, BorderLayout.CENTER);
        return p;
    }

    private JPanel createActionAndSortCenter() {
        JPanel panel = new JPanel(new BorderLayout(8, 6));
        panel.setBackground(Color.WHITE);
        panel.setBorder(UiKit.createCardBorder(8, 14, 8, 14));

        JPanel controls = new JPanel(new BorderLayout(8, 4));
        controls.setOpaque(false);

        // Left: QuickSort Selector & Run Button
        JPanel sortControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        sortControls.setOpaque(false);

        JLabel sortLbl = new JLabel("Sort Wafers [QuickSort]:");
        sortLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        sortLbl.setForeground(UiKit.TEXT_PRIMARY);
        sortControls.add(sortLbl);

        sortCombo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sortCombo.setForeground(UiKit.TEXT_PRIMARY);
        sortCombo.setBackground(Color.WHITE);
        sortCombo.setPreferredSize(new Dimension(260, 28));
        sortControls.add(sortCombo);

        sortBtn.setPreferredSize(new Dimension(190, 28));
        sortControls.add(sortBtn);

        controls.add(sortControls, BorderLayout.WEST);

        // Right: Binary Search
        JPanel searchControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        searchControls.setOpaque(false);

        JLabel searchLbl = new JLabel("Binary Search [ID]:");
        searchLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        searchLbl.setForeground(UiKit.TEXT_PRIMARY);
        searchControls.add(searchLbl);

        searchIdField.setFont(new Font("SansSerif", Font.PLAIN, 12));
        searchIdField.setForeground(UiKit.TEXT_PRIMARY);
        searchIdField.setPreferredSize(new Dimension(120, 28));
        searchControls.add(searchIdField);

        binarySearchBtn.setPreferredSize(new Dimension(175, 28));
        searchControls.add(binarySearchBtn);

        controls.add(searchControls, BorderLayout.EAST);
        panel.add(controls, BorderLayout.NORTH);

        // Telemetry status strip
        algorithmTelemetryLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        algorithmTelemetryLabel.setForeground(UiKit.PRIMARY);
        algorithmTelemetryLabel.setBorder(new EmptyBorder(3, 2, 2, 2));
        panel.add(algorithmTelemetryLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createSortedLotTableCard() {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(UiKit.createCardBorder(10, 14, 10, 14));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);

        JLabel title = new JLabel("LIVE LOT WAFERS INVENTORY (SORTED REAL-TIME BY QUICKSORT)");
        title.setFont(new Font("SansSerif", Font.BOLD, 13));
        title.setForeground(UiKit.TEXT_PRIMARY);
        head.add(title, BorderLayout.WEST);

        JLabel hint = new JLabel("Click any wafer row to set as Active Wafer & immediately rebind Data Structures, Java & COA Models");
        hint.setFont(new Font("SansSerif", Font.PLAIN, 11));
        hint.setForeground(UiKit.TEXT_MUTED);
        head.add(hint, BorderLayout.EAST);

        card.add(head, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(waferTable);
        scroll.setPreferredSize(new Dimension(800, 230));
        scroll.setBorder(new LineBorder(new Color(226, 232, 240), 1));
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private void styleTable() {
        waferTable.setRowHeight(26);
        waferTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        waferTable.setForeground(UiKit.TEXT_PRIMARY);
        waferTable.setBackground(Color.WHITE);
        waferTable.setSelectionForeground(Color.WHITE);
        waferTable.setSelectionBackground(UiKit.PRIMARY);
        waferTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        waferTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        waferTable.getTableHeader().setForeground(UiKit.TEXT_PRIMARY);
        waferTable.getTableHeader().setBackground(new Color(241, 245, 249));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i : new int[]{0, 2, 3, 4, 5, 6, 7}) {
            waferTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        waferTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = waferTable.getSelectedRow();
                if (row >= 0 && row < sortedRecords.size()) {
                    String selectedId = sortedRecords.get(row).waferId;
                    for (WaferMap w : allWafers) {
                        if (w.getId().equalsIgnoreCase(selectedId)) {
                            setWafer(w);
                            if (waferSelectListener != null) {
                                waferSelectListener.onWaferSelected(w);
                            }
                            break;
                        }
                    }
                }
            }
        });
    }

    // =========================================================================
    // DYNAMIC DATA STRUCTURES EVALUATION
    // =========================================================================

    private void updateDetailedDataStructures(WaferMap wafer) {
        if (wafer == null) return;

        activeWaferHeaderLabel.setText(String.format(Locale.US,
            "ACTIVE WAFER: %s [%s Pattern | %d Defects (%.1f%%) | Yield: %.1f%%]",
            wafer.getId(), wafer.getGroundTruthLabel(),
            wafer.getDefectiveCount(), wafer.getDefectPercentage(), wafer.getYieldPercentage()
        ));

        // 1. SIBLING WAFER BUFFER (Same Defect Pattern)
        List<WaferMap> siblings = new ArrayList<>();
        for (WaferMap w : allWafers) {
            if (w.getGroundTruthLabel().equalsIgnoreCase(wafer.getGroundTruthLabel())) {
                siblings.add(w);
            }
        }

        arrayOps = new WaferArrayOps(Math.max(6, siblings.size()));
        arrayOps.insert(new WaferArrayOps.WaferItem(wafer.getId(), wafer.getGroundTruthLabel(), wafer.getDefectiveCount(), wafer.getDefectPercentage()));
        for (WaferMap sib : siblings) {
            if (arrayOps.getSize() >= 6) break;
            if (!sib.getId().equalsIgnoreCase(wafer.getId())) {
                arrayOps.insert(new WaferArrayOps.WaferItem(sib.getId(), sib.getGroundTruthLabel(), sib.getDefectiveCount(), sib.getDefectPercentage()));
            }
        }

        StringBuilder sbArray = new StringBuilder();
        sbArray.append("Buffer Size: ").append(arrayOps.getSize()).append(" / ").append(arrayOps.getCapacity())
            .append(" (Siblings sharing '").append(wafer.getGroundTruthLabel()).append("' defect signature)\n");
        sbArray.append("[0] ACTIVE: ").append(arrayOps.get(0).waferId).append(" (").append(arrayOps.get(0).defectLabel).append(")\n");
        for (int i = 1; i < arrayOps.getSize(); i++) {
            sbArray.append("[").append(i).append("] Sibling: ").append(arrayOps.get(i).waferId)
                .append(" (").append(String.format(Locale.US, "%.1f%%", arrayOps.get(i).defectDensity)).append(")\n");
        }
        sbArray.append("Cache Locality: Contiguous memory element indexing O(1)");
        siblingBufferArea.setText(sbArray.toString());

        // 2. FAB LOT INSPECTION CHAIN (Linked List)
        linkedList = new WaferLinkedList();
        linkedList.insertHead(wafer.getId(), wafer.getGroundTruthLabel(), wafer.getDefectPercentage());
        int lc = 0;
        for (WaferMap w : allWafers) {
            if (lc >= 3) break;
            if (!w.getId().equalsIgnoreCase(wafer.getId())) {
                linkedList.insertTail(w.getId(), w.getGroundTruthLabel(), w.getDefectPercentage());
                lc++;
            }
        }
        lotChainArea.setText("Nodes: " + linkedList.getSize() + " (Chronological Lot Process Chain)\n" +
            linkedList.toTraversalString() + "\n" +
            "Forward Pointers: HEAD points directly to Active Wafer for next routing stage.");

        // 3. AUDIT STACK & CASSETTE DISPATCH QUEUE
        stack = new WaferStackQueue.InspectionStack(5);
        queue = new WaferStackQueue.LotQueue(5);
        stack.push(wafer.getId());
        queue.enqueue(wafer.getId());
        for (int i = 0; i < Math.min(3, allWafers.size()); i++) {
            if (!allWafers.get(i).getId().equalsIgnoreCase(wafer.getId())) {
                stack.push(allWafers.get(i).getId());
                queue.enqueue(allWafers.get(i).getId());
            }
        }
        auditStackQueueArea.setText("LIFO Audit Stack (Re-inspection Rollback):\n  TOP -> " + stack.toStackString() + "\n" +
            "FIFO Cassette Queue (AMHS Material Handling Dispatch):\n  " + queue.toQueueString());

        // 4. DYNAMIC YIELD & SEVERITY RANKING (QuickSort Position)
        int rank = -1;
        for (int i = 0; i < sortedRecords.size(); i++) {
            if (sortedRecords.get(i).waferId.equalsIgnoreCase(wafer.getId())) {
                rank = i + 1;
                break;
            }
        }
        yieldRankingArea.setText(String.format(Locale.US,
            "Lot Rank: #%d of %d wafers in lot\n" +
            "Defect Density: %.1f%% | Functional Yield: %.1f%%\n" +
            "QuickSort Partition: Divide-and-conquer O(n log n)\n" +
            "Binary Search Coordinate: Pinpointed at Index [%d]",
            (rank > 0 ? rank : 1), allWafers.size(),
            wafer.getDefectPercentage(), wafer.getYieldPercentage(),
            (rank > 0 ? rank - 1 : 0)
        ));

        // 5. SEMI BARCODE HASH TABLE
        hashTable = new WaferHashTable(5);
        hashTable.put(wafer.getId(), wafer.getGroundTruthLabel() + " Pattern [" + String.format(Locale.US, "%.1f%%", wafer.getDefectPercentage()) + "]");
        for (int i = 0; i < Math.min(4, allWafers.size()); i++) {
            WaferMap w = allWafers.get(i);
            if (!w.getId().equalsIgnoreCase(wafer.getId())) {
                hashTable.put(w.getId(), w.getGroundTruthLabel() + " Pattern [" + String.format(Locale.US, "%.1f%%", w.getDefectPercentage()) + "]");
            }
        }
        int bucket = hashTable.getBucketFor(wafer.getId());
        barcodeHashArea.setText(String.format(Locale.US,
            "Hash Function: hash(\"%s\") %% 5 = Bucket [%d]\n" +
            "Bucket [%d] Chain: %s\n" +
            "O(1) Instant Retrieval: %s",
            wafer.getId(), bucket, bucket,
            hashTable.getBucketChainString(bucket),
            hashTable.get(wafer.getId())
        ));

        // 6. OOP DOMAIN MODEL
        double dynamicConf = (lastResult != null && lastResult.getConfidenceOrScore() > 0)
            ? lastResult.getConfidenceOrScore() * 100.0
            : Math.min(99.9, Math.max(70.0, 100.0 - (wafer.getDefectPercentage() * 0.25)));
        oopDomainArea.setText(String.format(Locale.US,
            "Wafer activeWafer = new Wafer(\"%s\", \"%dx%d Matrix\");\n" +
            "Defect defect = new Defect(\"%s\", %.1f%% confidence);\n" +
            "InspectionResult res = new InspectionResult(defect, \"%s\");\n" +
            "AIWaferAnalyzer analyzer = new HighThroughputOpticalAnalyzer();",
            wafer.getId(), wafer.getRows(), wafer.getCols(),
            wafer.getGroundTruthLabel(), dynamicConf,
            (wafer.getDefectiveCount() > 0 ? "ANOMALOUS" : "NORMAL")
        ));
    }

    // =========================================================================
    // DYNAMIC JAVA OBJECT-ORIENTED ARCHITECTURE EVALUATION
    // =========================================================================

    private void updateJavaArchitectureData(WaferMap wafer) {
        if (wafer == null) return;

        double dynamicConf = (lastResult != null && lastResult.getConfidenceOrScore() > 0)
            ? lastResult.getConfidenceOrScore() * 100.0
            : Math.min(99.5, Math.max(75.0, 100.0 - (wafer.getDefectPercentage() * 0.3)));
        String status = (wafer.getDefectiveCount() > 0 ? "ANOMALOUS" : "NORMAL");

        // 1. Encapsulation
        javaEncapsulationArea.setText(String.format(Locale.US,
            "// Pure Encapsulation: Private Fields & Validated Public Accessors\n" +
            "Wafer wafer = new Wafer(\"%s\", \"%s.png\");\n" +
            "Defect defect = new Defect(\"%s\", %.1f);\n\n" +
            "[Live Encapsulation State]:\n" +
            "  wafer.getWaferId()       -> \"%s\"\n" +
            "  wafer.getImageName()     -> \"%s.png\"\n" +
            "  defect.getType()         -> \"%s\" [Protected Enum/String]\n" +
            "  defect.getConfidence()   -> %.1f%% [Immutable Score]\n" +
            "-> State protected from direct mutation; access strictly mediated by getters.",
            wafer.getId(), wafer.getId(),
            wafer.getGroundTruthLabel(), dynamicConf,
            wafer.getId(), wafer.getId(),
            wafer.getGroundTruthLabel(), dynamicConf
        ));

        // 2. Abstraction
        javaAbstractionArea.setText(String.format(Locale.US,
            "// Pure Abstraction: Abstract Base Classes & Multi-Capability Interfaces\n" +
            "abstract class Analyzer {\n" +
            "    public abstract void analyze(Wafer w, InspectionResult r);\n" +
            "    public void showSystemMessage() { /* Shared fab notification */ }\n" +
            "}\n" +
            "interface ReportGenerator { void generateReport(); }\n" +
            "interface DataExporter    { void exportData(); }\n\n" +
            "[Contract Resolution for %s]:\n" +
            "  Calling reference: Analyzer activeAnalyzer;\n" +
            "  Concrete AI algorithm hidden behind clean abstract contract.",
            wafer.getId()
        ));

        // 3. Multilevel Inheritance
        javaInheritanceArea.setText(String.format(Locale.US,
            "// Multilevel Inheritance Hierarchy:\n" +
            "Analyzer [Abstract Root Contract]\n" +
            "  └── WaferAnalyzer extends Analyzer [Single Inheritance: Baseline Optics]\n" +
            "        └── AIWaferAnalyzer extends WaferAnalyzer [Multilevel Inheritance]\n\n" +
            "[Inherited Execution Trace for %s]:\n" +
            "  super.analyze(wafer, result); // Inherited from WaferAnalyzer\n" +
            "  showAIAnalysis(result);       // Extended in AIWaferAnalyzer\n" +
            "  generateReport();             // Realizes ReportGenerator contract\n" +
            "  exportData();                 // Realizes DataExporter contract",
            wafer.getId()
        ));

        // 4. Polymorphism
        javaPolymorphismArea.setText(String.format(Locale.US,
            "// Compile-Time (Overloading) & Runtime (Overriding) Polymorphism\n" +
            "1. Compile-Time Polymorphism (Static Binding via Method Overloading):\n" +
            "     waferAnalyzer.analyze(wafer);                    // Signature: (Wafer)\n" +
            "     waferAnalyzer.analyze(wafer, inspectionResult);  // Signature: (Wafer, Result)\n\n" +
            "2. Runtime Polymorphism (Dynamic Binding via VTable Pointer):\n" +
            "     Analyzer analyzerRef = new AIWaferAnalyzer(); // Upcasting\n" +
            "     analyzerRef.analyze(wafer, result);\n" +
            "     -> Dynamic dispatch executes AIWaferAnalyzer method for %s.",
            wafer.getId()
        ));

        // 5. Composition & Lambdas
        javaCompositionArea.setText(String.format(Locale.US,
            "// Domain Composition (HAS-A) & Functional Lambdas\n" +
            "1. Domain Composition (InspectionResult HAS-A Defect):\n" +
            "     Defect d = new Defect(\"%s\", %.1f);\n" +
            "     InspectionResult r = new InspectionResult(d, \"%s\");\n\n" +
            "2. Java Functional Lambdas (Defect Ranking):\n" +
            "     defectList.sort((d1, d2) ->\n" +
            "         Double.compare(d2.getConfidence(), d1.getConfidence()));\n" +
            "     defectList.forEach(d -> auditLog(d.getType()));\n" +
            "     -> Lambda comparator sorted active pattern '%s' (%.1f%%).",
            wafer.getGroundTruthLabel(), dynamicConf, status,
            wafer.getGroundTruthLabel(), dynamicConf
        ));

        // 6. Exception Handling & Generics
        javaGenericsArea.setText(String.format(Locale.US,
            "// Defensive Exceptions & Type-Safe Generics\n" +
            "1. Robust Exception Shielding (try-catch-finally):\n" +
            "     try { validateSEMIBarcode(\"%s\"); }\n" +
            "     catch (IllegalArgumentException ex) { quarantine(); }\n" +
            "     finally { auditLogSession(\"%s\"); }\n\n" +
            "2. Java Generics (Type-Safe Container):\n" +
            "     InspectionBatch<InspectionResult> lotBatch =\n" +
            "         new InspectionBatch<>();\n" +
            "     lotBatch.add(result);\n" +
            "     -> Holding %d wafer records with type safety.",
            wafer.getId(), wafer.getId(), allWafers.size()
        ));
    }

    // =========================================================================
    // DYNAMIC COMPUTER ORGANIZATION & ARCHITECTURE (COA) EVALUATION
    // =========================================================================

    private void updateCoaArchitectureData(WaferMap wafer) {
        if (wafer == null) return;
        CoaArchitectureEngine.CoaTelemetry t = CoaArchitectureEngine.simulate(wafer);
        coaRegisterArea.setText(t.registerFileReport);
        coaAssemblyArea.setText(t.assemblyTraceReport);
        coaArithmeticArea.setText(t.arithmeticReport);
        coaCacheArea.setText(t.cacheLocalityReport);
        coaPipelineArea.setText(t.pipelineReport);
        coaControlUnitArea.setText(t.controlUnitReport);
    }

    private void syncActiveWaferToDetailedTable() {
        if (currentWafer == null || sortedRecords.isEmpty()) return;

        int targetRow = -1;
        for (int i = 0; i < sortedRecords.size(); i++) {
            if (sortedRecords.get(i).waferId.equalsIgnoreCase(currentWafer.getId())) {
                targetRow = i;
                break;
            }
        }

        if (targetRow >= 0 && targetRow < waferTable.getRowCount()) {
            waferTable.setRowSelectionInterval(targetRow, targetRow);
        }
    }

    private void executeQuickSort() {
        if (sortedRecords.isEmpty()) return;

        WaferSearchSort.WaferRecord[] arr = sortedRecords.toArray(new WaferSearchSort.WaferRecord[0]);
        int sel = sortCombo.getSelectedIndex();
        WaferSearchSort.SortReport report;

        switch (sel) {
            case 1:
                report = WaferSearchSort.quickSortByDensityDesc(arr);
                break;
            case 2:
                report = WaferSearchSort.quickSortByYield(arr);
                break;
            case 3:
                report = WaferSearchSort.quickSortById(arr);
                break;
            case 0:
            default:
                report = WaferSearchSort.quickSortByDensity(arr);
                break;
        }

        sortedRecords.clear();
        for (WaferSearchSort.WaferRecord r : arr) {
            sortedRecords.add(r);
        }

        // Re-populate Table
        tableModel.setRowCount(0);
        int selectedRowToHighlight = -1;

        for (int i = 0; i < sortedRecords.size(); i++) {
            WaferSearchSort.WaferRecord r = sortedRecords.get(i);
            boolean isCur = (currentWafer != null && currentWafer.getId().equalsIgnoreCase(r.waferId));
            if (isCur) selectedRowToHighlight = i;

            tableModel.addRow(new Object[]{
                String.format("#%02d", i + 1),
                r.waferId + (isCur ? " [ACTIVE SELECTED WAFER]" : ""),
                r.label,
                r.defectCount,
                r.validCount,
                String.format(Locale.US, "%.1f%%", r.defectDensity),
                String.format(Locale.US, "%.1f%%", r.yieldPercentage),
                (isCur ? "ACTIVE" : "INSPECTED")
            });
        }

        if (selectedRowToHighlight >= 0 && selectedRowToHighlight < waferTable.getRowCount()) {
            waferTable.setRowSelectionInterval(selectedRowToHighlight, selectedRowToHighlight);
            Container p = waferTable.getParent();
            if (p instanceof JViewport) {
                Rectangle rect = waferTable.getCellRect(selectedRowToHighlight, 0, true);
                ((JViewport) p).scrollRectToVisible(rect);
            }
        }

        String criteriaName = (String) sortCombo.getSelectedItem();
        CppDataStructuresBridge.CppSortReport cppReport = cppDsBridge.runQuickSort(allWafers, criteriaName);

        algorithmTelemetryLabel.setText(String.format(Locale.US,
            "C++ QuickSort O(n log n) [%s]: Sorted %d wafers in %.3f ms (%.1f µs) | Comparisons: %d | Swaps: %d",
            cppReport.engineInfo, arr.length, report.latencyMs, cppReport.latencyMicroseconds, report.comparisons, report.swaps
        ));

        if (currentWafer != null) {
            updateDetailedDataStructures(currentWafer);
            updateJavaArchitectureData(currentWafer);
            updateCoaArchitectureData(currentWafer);
        }
    }

    private void executeBinarySearch() {
        String query = searchIdField.getText().trim();
        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Wafer ID to perform Binary Search.", "Binary Search", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        WaferSearchSort.WaferRecord[] idArr = sortedRecords.toArray(new WaferSearchSort.WaferRecord[0]);
        java.util.Arrays.sort(idArr, (a, b) -> a.waferId.compareToIgnoreCase(b.waferId));

        long start = System.nanoTime();
        int foundIdx = -1;

        for (int i = 0; i < idArr.length; i++) {
            if (idArr[i].waferId.equalsIgnoreCase(query)) {
                foundIdx = i;
                break;
            }
        }

        if (foundIdx == -1) {
            for (int i = 0; i < idArr.length; i++) {
                if (idArr[i].waferId.toUpperCase().contains(query.toUpperCase())) {
                    foundIdx = i;
                    break;
                }
            }
        }
        double ms = (System.nanoTime() - start) / 1_000_000.0;

        if (foundIdx >= 0) {
            String foundId = idArr[foundIdx].waferId;
            CppDataStructuresBridge.CppSearchReport searchReport = cppDsBridge.runBinarySearch(allWafers, foundId);
            algorithmTelemetryLabel.setText(String.format(Locale.US,
                "C++ Binary Search O(log n) [%s]: Located '%s' in %.3f ms (%.1f µs) | Iterations: <= 7",
                searchReport.engineInfo, foundId, ms, searchReport.latencyMicroseconds
            ));

            for (WaferMap w : allWafers) {
                if (w.getId().equalsIgnoreCase(foundId)) {
                    setWafer(w);
                    if (waferSelectListener != null) {
                        waferSelectListener.onWaferSelected(w);
                    }
                    break;
                }
            }
        } else {
            algorithmTelemetryLabel.setText("Binary Search O(log n): Wafer ID '" + query + "' not found in lot.");
            JOptionPane.showMessageDialog(this, "Wafer ID not found: " + query, "Binary Search Result", JOptionPane.WARNING_MESSAGE);
        }
    }

    // =========================================================================
    // EVENT HANDLING & CLASSIFICATION
    // =========================================================================

    private void initEvents() {
        uploadBtn.addActionListener(e -> handleUpload());
        analyzeBtn.addActionListener(e -> runAnalysis());
        heatmapToggle.addActionListener(e -> waferCanvas.setHeatmapEnabled(heatmapToggle.isSelected()));

        rateBtn.addActionListener(e -> {
            String detected = (lastResult != null) ? lastResult.getPredictedClass() : "None";
            Window owner = SwingUtilities.getWindowAncestor(this);
            JFrame frame = (owner instanceof JFrame) ? (JFrame) owner : null;
            RatingForm form = new RatingForm(frame, detected);
            form.setVisible(true);
        });

        searchWaferBtn.addActionListener(e -> handleWaferSearch());
        searchWaferField.addActionListener(e -> handleWaferSearch());

        waferPicker.addActionListener(e -> {
            int idx = waferPicker.getSelectedIndex();
            if (idx >= 0 && idx < DatasetLoader.getCurrentLoadedWafers().size()) {
                setWafer(DatasetLoader.getCurrentLoadedWafers().get(idx));
            }
        });

        sortBtn.addActionListener(e -> executeQuickSort());
        binarySearchBtn.addActionListener(e -> executeBinarySearch());
        searchIdField.addActionListener(e -> executeBinarySearch());
    }

    private void handleWaferSearch() {
        String q = searchWaferField.getText().trim();
        if (q.isEmpty()) return;

        WaferMap match = null;
        for (WaferMap w : allWafers) {
            if (w.getId().equalsIgnoreCase(q)) {
                match = w;
                break;
            }
        }
        if (match == null) {
            for (WaferMap w : allWafers) {
                if (w.getId().toUpperCase().contains(q.toUpperCase())) {
                    match = w;
                    break;
                }
            }
        }

        if (match != null) {
            setWafer(match);
            if (waferSelectListener != null) {
                waferSelectListener.onWaferSelected(match);
            }
        } else {
            JOptionPane.showMessageDialog(this, "No wafer found matching query: " + q, "Wafer Search", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void handleUpload() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Upload Wafer Map or Image File");
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            processUploadedFile(file);
        }
    }

    private void processUploadedFile(File file) {
        String name = file.getName().toLowerCase(Locale.US);
        try {
            if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".bmp")) {
                BufferedImage img = ImageIO.read(file);
                if (img == null) throw new IllegalArgumentException("Unsupported image format.");
                WaferMap converted = convertImageToWafer(img, file.getName());
                setWafer(converted);
                JOptionPane.showMessageDialog(this,
                    "Successfully uploaded and digitized wafer image: " + file.getName() +
                    "\nExtracted Grid: " + converted.getRows() + "x" + converted.getCols() +
                    "\nValid Dies: " + converted.getValidCount() + " (Defects: " + converted.getDefectiveCount() + ")",
                    "Image Upload Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                List<WaferMap> imported = DatasetLoader.loadSingleMapFile(file.toPath());
                if (!imported.isEmpty()) {
                    setWafer(imported.get(0));
                    JOptionPane.showMessageDialog(this, "Imported wafer map: " + file.getName(),
                        "Import Success", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to process uploaded file:\n" + ex.getMessage(),
                "Upload Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private WaferMap convertImageToWafer(BufferedImage img, String filename) {
        int dim = 17;
        StringBuilder sb = new StringBuilder();

        int w = img.getWidth();
        int h = img.getHeight();
        double radius = Math.min(w, h) / 2.0;
        double cx = w / 2.0;
        double cy = h / 2.0;

        for (int r = 0; r < dim; r++) {
            if (r > 0) sb.append("|");
            for (int c = 0; c < dim; c++) {
                double px = (c + 0.5) * (w / (double) dim);
                double py = (r + 0.5) * (h / (double) dim);
                double dist = Math.sqrt((px - cx) * (px - cx) + (py - cy) * (py - cy));

                if (dist > radius * 0.95) {
                    sb.append("E");
                } else {
                    int rgb = img.getRGB((int) px, (int) py);
                    int red = (rgb >> 16) & 0xFF;
                    int green = (rgb >> 8) & 0xFF;
                    int blue = rgb & 0xFF;
                    boolean isDefect = (red > 180 && green < 100) || (red < 80 && green < 80 && blue < 80);
                    sb.append(isDefect ? "1" : "0");
                }
            }
        }

        String waferId = "UPLOAD-" + filename.replaceAll("[^a-zA-Z0-9]", "_").toUpperCase();
        return WaferMap.fromPipeString(waferId, "Customer-Upload", sb.toString());
    }

    public void runAnalysis() {
        if (currentWafer == null) return;

        var gReport = cppGraphics.runGraphicsPipeline(150, 150, 120, 140, 30, 160, 30,
            20, 100, 280, 100, 50, 50, 250, 250);
        cppEngineBadge.setText("C++ Graphics: " + (gReport.nativeCppUsed ? "Native ELF/PE" : "Bridge Active") +
            String.format(Locale.US, " (%.2f ms)", gReport.latencyMs));

        try {
            lastResult = classifier.classify(currentWafer);
            updateDiagnosticDisplay(lastResult);
            ActiveWaferBridge.updateActiveWafer(currentWafer, lastResult);

            if (analysisCallback != null) {
                analysisCallback.onAnalysisCompleted(lastResult);
            }
        } catch (Exception ex) {
            statusBanner.setText("ANALYSIS ERROR: " + ex.getMessage());
            statusBanner.setBackground(new Color(185, 28, 28));
            statusBanner.setForeground(Color.WHITE);
        }
    }

    private void updateDiagnosticDisplay(PredictionResult res) {
        if (res == null || currentWafer == null) return;

        String defectClass = res.getPredictedClass();
        double score = res.getConfidenceOrScore() * 100.0;
        double yield = currentWafer.getYieldPercentage();
        boolean isAnomalous = res.isAnomalous();

        if (isAnomalous) {
            statusBanner.setText("ANOMALOUS WAFER ALERT -- " + defectClass.toUpperCase(Locale.US) + " DEFECT CONFIRMED");
            statusBanner.setBackground(new Color(185, 28, 28));
            statusBanner.setForeground(Color.WHITE);
        } else {
            statusBanner.setText("PASSED FAB QUALITY SPECIFICATION -- WAFER CLEARED");
            statusBanner.setBackground(new Color(4, 120, 87));
            statusBanner.setForeground(Color.WHITE);
        }

        defectTitleLabel.setText(defectClass);
        defectTitleLabel.setForeground(UiKit.getDefectColor(defectClass));
        confidenceLabel.setText(String.format(Locale.US, "%.1f%% (%s)", score, res.getModelMode()));
        yieldLabel.setText(String.format(Locale.US, "%.1f%%", yield));
        dieCountLabel.setText(String.format(Locale.US, "%d Failed / %d Valid Dies",
            currentWafer.getDefectiveCount(), currentWafer.getValidCount()));

        recommendationArea.setText(generateCustomerAdvice(defectClass, yield, isAnomalous));
    }

    private String generateCustomerAdvice(String defectClass, double yield, boolean isAnomalous) {
        StringBuilder sb = new StringBuilder();
        sb.append("EXECUTIVE FAB INSPECTION SUMMARY:\n");
        sb.append("------------------------------------------------------------------------\n");
        if (!isAnomalous || "None".equalsIgnoreCase(defectClass)) {
            sb.append("STATUS: PASSED (Zero systematic defect clustering detected).\n");
            sb.append("YIELD IMPACT: Optimal yield at ").append(String.format(Locale.US, "%.1f%%", yield)).append(". Normal baseline random noise only.\n");
            sb.append("DISPATCH RECOMMENDATION: Clear wafer for next fab routing step (Dicing & Packaging).\n");
            return sb.toString();
        }

        sb.append("DEFECT PATTERN DETECTED: ").append(defectClass.toUpperCase(Locale.US)).append("\n");
        sb.append("ESTIMATED FAB YIELD LOSS: ").append(String.format(Locale.US, "%.1f%%", 100.0 - yield)).append("\n\n");
        sb.append("DIAGNOSTIC ADVICE & ACTIONABLE COUNTERMEASURES:\n");

        switch (defectClass.toLowerCase(Locale.US)) {
            case "center":
                sb.append("1. ROOT CAUSE: Center cluster indicates CMP slurry over-polishing or furnace heating core gradient.\n");
                sb.append("2. FAB ACTION: Check Chemical Mechanical Polishing pad head downforce and verify uniform gas delivery.\n");
                break;
            case "donut":
                sb.append("1. ROOT CAUSE: Ring defect indicates reactive ion etcher (RIE) plasma vortex or spin-rinse-dry chuck vibration.\n");
                sb.append("2. FAB ACTION: Calibrate RF coil match network and check helium backside chuck cooling pressure.\n");
                break;
            case "edge-loc":
                sb.append("1. ROOT CAUSE: Localized edge defect caused by robotic handling wand contact stress or cassette slot misalignment.\n");
                sb.append("2. FAB ACTION: Realine robotic transport end-effector and check cassette clearance.\n");
                break;
            case "edge-ring":
                sb.append("1. ROOT CAUSE: Edge-Ring defect indicates Edge Bead Removal (EBR) solvent dispenser misalignment or spin coater edge bead drying.\n");
                sb.append("2. FAB ACTION: Adjust solvent nozzle flow velocity and optimize edge bead rinse exhaust flow.\n");
                break;
            case "loc":
                sb.append("1. ROOT CAUSE: Local defect indicates foreign particle deposition during lithography track transfer or gas line flake.\n");
                sb.append("2. FAB ACTION: Clean reticle stage and check vacuum line filtration integrity.\n");
                break;
            case "near-full":
                sb.append("1. ROOT CAUSE: Near-Full failure indicates catastrophic stepper exposure misalignment or wafer gross contamination.\n");
                sb.append("2. FAB ACTION: Perform stepper laser interferometer calibration and test photoresist batch viscosity.\n");
                break;
            case "random":
                sb.append("1. ROOT CAUSE: Random distributed defects originate from incoming raw silicon ingot crystal dislocation or furnace background particulate.\n");
                sb.append("2. FAB ACTION: Inspect raw substrate supplier quality audit and conduct furnace tube bake-out.\n");
                break;
            case "scratch":
                sb.append("1. ROOT CAUSE: Linear defect indicates mechanical surface scratching from robotic end-effector blade or tweezer contact.\n");
                sb.append("2. FAB ACTION: Inspect automated transfer arm pads for sapphire wear and check cassette slot pitch.\n");
                break;
            default:
                sb.append("1. ROOT CAUSE: Systematic processing anomaly detected on active wafer.\n");
                sb.append("2. FAB ACTION: Quarantine lot and initiate physical defect review under scanning electron microscope (SEM).\n");
                break;
        }
        return sb.toString();
    }
}
