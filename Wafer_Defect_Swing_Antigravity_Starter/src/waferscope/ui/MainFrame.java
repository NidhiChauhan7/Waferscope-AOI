package waferscope.ui;

import waferscope.classifier.DefectClassifier;
import waferscope.classifier.DemoNearestNeighborClassifier;
import waferscope.classifier.PythonInferenceEngine;
import waferscope.datastructures.ActiveWaferBridge;
import waferscope.io.DatasetLoader;
import waferscope.model.PredictionResult;
import waferscope.model.WaferMap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Main application window for WaferScope AOI.
 * 
 * Customer-Facing Architecture:
 * - Header: Platform Branding, Live Lot Badges & Primary Navigation
 * - Dashboard: Executive KPIs, Yield Analytics & Batch Lot Audits
 * - Analyze Wafer: Customer Image Upload, Die Digitizer & Instant AI Diagnosis
 * - Defect Knowledge Hub: Customer Defect Encyclopedia for all 9 Patterns
 * - Wafer Visualizer: High-Res Silicon Wafer Map & C++ Graphics Engine Canvas
 * - Inspection History: Tabular Session Audit Trail & CSV Report Exporter
 * - Footer: Hardware Telemetry, Memory Footprint & Monotonic Latency
 */
public class MainFrame extends JFrame {

    // Models & Classifiers
    private final List<WaferMap> loadedWafers = new ArrayList<>();
    private WaferMap currentWafer;
    private final DemoNearestNeighborClassifier demoClassifier = new DemoNearestNeighborClassifier();
    private final PythonInferenceEngine pythonClassifier = new PythonInferenceEngine();
    private boolean preferPythonAi = true;

    // View Panels
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel viewsCardPanel = new JPanel(cardLayout);

    private final HeaderPanel headerPanel = new HeaderPanel();
    private final FooterPanel footerPanel = new FooterPanel();

    private final DashboardPanel dashboardPanel = new DashboardPanel();
    private final CustomerAnalyzerPanel analyzerPanel = new CustomerAnalyzerPanel();
    private final DefectKnowledgePanel knowledgePanel = new DefectKnowledgePanel();
    private final WaferViewPanel waferViewPanel = new WaferViewPanel();
    private final HistoryPanel historyPanel = new HistoryPanel();

    public MainFrame() {
        super("WaferScope AOI | Intelligent Semiconductor Wafer Defect Detection Platform");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1320, 860);
        setMinimumSize(new Dimension(1040, 720));
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(241, 245, 249));

        initUI();
        initNavigation();
        initInterPanelWiring();
        initKeyShortcuts();

        // Load Initial Dataset
        loadInitialDataset();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // 1. Header
        add(headerPanel, BorderLayout.NORTH);

        // 2. Customer View Cards
        viewsCardPanel.setOpaque(false);
        viewsCardPanel.add(dashboardPanel, "DASHBOARD");
        viewsCardPanel.add(analyzerPanel, "ANALYZE WAFER");
        viewsCardPanel.add(knowledgePanel, "DEFECT KNOWLEDGE HUB");
        viewsCardPanel.add(waferViewPanel, "WAFER VISUALIZER");
        viewsCardPanel.add(historyPanel, "INSPECTION HISTORY");

        add(viewsCardPanel, BorderLayout.CENTER);

        // 3. Status Footer
        add(footerPanel, BorderLayout.SOUTH);
    }

    private void initNavigation() {
        headerPanel.setNavSelectionListener(tabName -> {
            cardLayout.show(viewsCardPanel, tabName);
        });
    }

    private void initInterPanelWiring() {
        // Wafer View selection change
        waferViewPanel.setOnWaferChangeListener(wafer -> {
            currentWafer = wafer;
            ActiveWaferBridge.updateActiveWafer(wafer, null);
            dashboardPanel.setActiveWafer(wafer);
            footerPanel.updateWafer(wafer);
            analyzerPanel.setWafer(wafer);
        });

        // Dashboard wafer selection change (from QuickSort table / Binary Search)
        dashboardPanel.setOnWaferSelectListener(wafer -> {
            currentWafer = wafer;
            ActiveWaferBridge.updateActiveWafer(wafer, null);
            waferViewPanel.selectWafer(wafer);
            footerPanel.updateWafer(wafer);
            analyzerPanel.setWafer(wafer);
        });

        // Dashboard category select -> Filter and show
        dashboardPanel.setOnCategorySelectListener(category -> {
            waferViewPanel.filterByCategory(category);
            cardLayout.show(viewsCardPanel, "WAFER VISUALIZER");
            headerPanel.selectTab("WAFER VISUALIZER");
        });

        // Dashboard batch scan
        dashboardPanel.setOnBatchScanListener(this::runFullLotBatchScan);

        // Knowledge Hub sample selected -> Load into Analyzer and switch to it!
        knowledgePanel.setOnSampleSelectedListener(categoryName -> {
            WaferMap match = null;
            for (WaferMap w : loadedWafers) {
                if (categoryName.equalsIgnoreCase(w.getGroundTruthLabel())) {
                    match = w;
                    break;
                }
            }
            if (match != null) {
                currentWafer = match;
                ActiveWaferBridge.updateActiveWafer(match, null);
                dashboardPanel.setActiveWafer(match);
                analyzerPanel.setWafer(match);
                cardLayout.show(viewsCardPanel, "ANALYZE WAFER");
                headerPanel.selectTab("ANALYZE WAFER");
            } else {
                JOptionPane.showMessageDialog(this, "No wafer sample found for category: " + categoryName,
                    "Sample Search", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // Analyzer completed callback -> Update footer, history, and active wafer bridge
        analyzerPanel.setOnAnalysisCompleteCallback(result -> {
            if (result != null) {
                historyPanel.addRecord(result);
                footerPanel.updateResult(result);
                ActiveWaferBridge.updateActiveWafer(currentWafer, result);
                dashboardPanel.setActiveWafer(currentWafer);
            }
        });

        // Analyzer wafer selection change (e.g. from Detailed Analysis live lot table or search)
        analyzerPanel.setOnWaferSelectListener(wafer -> {
            currentWafer = wafer;
            ActiveWaferBridge.updateActiveWafer(wafer, null);
            dashboardPanel.setActiveWafer(wafer);
            footerPanel.updateWafer(wafer);
            waferViewPanel.selectWafer(wafer);
        });

        // History wafer re-select
        historyPanel.setOnWaferSelectListener(waferId -> {
            for (WaferMap w : loadedWafers) {
                if (w.getId().equalsIgnoreCase(waferId)) {
                    currentWafer = w;
                    ActiveWaferBridge.updateActiveWafer(w, null);
                    dashboardPanel.setActiveWafer(w);
                    analyzerPanel.setWafer(w);
                    cardLayout.show(viewsCardPanel, "ANALYZE WAFER");
                    headerPanel.selectTab("ANALYZE WAFER");
                    break;
                }
            }
        });
    }

    private void initKeyShortcuts() {
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    analyzerPanel.runAnalysis();
                } else if (e.getKeyCode() == KeyEvent.VK_D) {
                    cardLayout.show(viewsCardPanel, "DASHBOARD");
                    headerPanel.selectTab("DASHBOARD");
                } else if (e.getKeyCode() == KeyEvent.VK_A) {
                    cardLayout.show(viewsCardPanel, "ANALYZE WAFER");
                    headerPanel.selectTab("ANALYZE WAFER");
                } else if (e.getKeyCode() == KeyEvent.VK_K) {
                    cardLayout.show(viewsCardPanel, "DEFECT KNOWLEDGE HUB");
                    headerPanel.selectTab("DEFECT KNOWLEDGE HUB");
                } else if (e.getKeyCode() == KeyEvent.VK_V) {
                    cardLayout.show(viewsCardPanel, "WAFER VISUALIZER");
                    headerPanel.selectTab("WAFER VISUALIZER");
                } else if (e.getKeyCode() == KeyEvent.VK_H) {
                    cardLayout.show(viewsCardPanel, "INSPECTION HISTORY");
                    headerPanel.selectTab("INSPECTION HISTORY");
                }
            }
        });
    }

    private void loadInitialDataset() {
        headerPanel.setSystemStatus("LOADING DATASET...", new Color(245, 158, 11));

        SwingWorker<DatasetLoader.LoadReport, Void> worker = new SwingWorker<>() {
            @Override
            protected DatasetLoader.LoadReport doInBackground() throws Exception {
                Path path = DatasetLoader.resolveDefaultDatasetPath();
                return DatasetLoader.loadCsv(path);
            }

            @Override
            protected void done() {
                try {
                    DatasetLoader.LoadReport report = get();
                    loadedWafers.clear();
                    loadedWafers.addAll(report.getWafers());

                    demoClassifier.setReferences(loadedWafers);

                    // Update UI views
                    String lotId = !loadedWafers.isEmpty() && loadedWafers.get(0).getId().contains("-")
                        ? loadedWafers.get(0).getId().split("-")[1]
                        : "FAB-LOT";
                    headerPanel.setLotInfo(lotId, loadedWafers.size());
                    dashboardPanel.updateData(loadedWafers);
                    waferViewPanel.setWafers(loadedWafers);
                    analyzerPanel.setWafersList(loadedWafers);
                    historyPanel.populateLotBaseline(loadedWafers, demoClassifier);

                    updateClassifierSelection();

                    if (!loadedWafers.isEmpty()) {
                        currentWafer = loadedWafers.get(0);
                        ActiveWaferBridge.updateActiveWafer(currentWafer, null);
                        dashboardPanel.setActiveWafer(currentWafer);
                        footerPanel.updateWafer(currentWafer);
                        analyzerPanel.setWafer(currentWafer);
                        headerPanel.setSystemStatus("SYSTEM OPERATIONAL", new Color(34, 197, 94));
                    }
                } catch (Exception ex) {
                    headerPanel.setSystemStatus("LOAD ERROR", new Color(239, 68, 68));
                    JOptionPane.showMessageDialog(MainFrame.this,
                        "Could not load dataset:\n" + ex.getMessage(),
                        "Dataset Load Warning", JOptionPane.WARNING_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void updateClassifierSelection() {
        DefectClassifier active;
        boolean trainedAvailable = pythonClassifier.isAvailable() && pythonClassifier.hasTrainedModelWeights();

        if (preferPythonAi && trainedAvailable) {
            active = pythonClassifier;
            headerPanel.updateMode(active.getModelMode(), true);
        } else {
            active = demoClassifier;
            headerPanel.updateMode(active.getModelMode(), false);
        }

        analyzerPanel.setClassifier(active);
    }

    private void runFullLotBatchScan() {
        if (loadedWafers.isEmpty()) return;

        DefectClassifier classifier = (preferPythonAi && pythonClassifier.isAvailable() && pythonClassifier.hasTrainedModelWeights())
            ? pythonClassifier : demoClassifier;

        JDialog progressDialog = new JDialog(this, "Batch Anomaly Scanning", true);
        progressDialog.setSize(400, 150);
        progressDialog.setLocationRelativeTo(this);
        progressDialog.setLayout(new BorderLayout(10, 10));

        JLabel pLabel = new JLabel("Scanning wafer lot: 0 / " + loadedWafers.size(), JLabel.CENTER);
        pLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        pLabel.setForeground(new Color(15, 23, 42));
        JProgressBar pb = new JProgressBar(0, loadedWafers.size());
        pb.setStringPainted(true);

        JPanel pContent = new JPanel(new GridLayout(2, 1, 8, 8));
        pContent.setBackground(Color.WHITE);
        pContent.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        pContent.add(pLabel);
        pContent.add(pb);
        progressDialog.add(pContent, BorderLayout.CENTER);

        SwingWorker<List<PredictionResult>, Integer> worker = new SwingWorker<>() {
            @Override
            protected List<PredictionResult> doInBackground() throws Exception {
                List<PredictionResult> results = new ArrayList<>();
                for (int i = 0; i < loadedWafers.size(); i++) {
                    WaferMap w = loadedWafers.get(i);
                    PredictionResult res = classifier.classify(w);
                    results.add(res);
                    publish(i + 1);
                }
                return results;
            }

            @Override
            protected void process(List<Integer> chunks) {
                int latest = chunks.get(chunks.size() - 1);
                pb.setValue(latest);
                pLabel.setText("Scanning wafer lot: " + latest + " / " + loadedWafers.size());
            }

            @Override
            protected void done() {
                progressDialog.dispose();
                try {
                    List<PredictionResult> results = get();
                    for (PredictionResult r : results) {
                        historyPanel.addRecord(r);
                    }
                    if (!results.isEmpty()) {
                        PredictionResult last = results.get(results.size() - 1);
                        footerPanel.updateResult(last);
                    }
                    JOptionPane.showMessageDialog(MainFrame.this,
                        "Batch scan complete!\n" + results.size() + " wafers analyzed across all 9 defect categories.\nCheck the 'Inspection History' tab for detailed records.",
                        "Batch Scan Complete", JOptionPane.INFORMATION_MESSAGE);
                    cardLayout.show(viewsCardPanel, "INSPECTION HISTORY");
                    headerPanel.selectTab("INSPECTION HISTORY");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MainFrame.this, "Batch scan failed: " + ex.getMessage(), "Scan Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
        progressDialog.setVisible(true);
    }
}
