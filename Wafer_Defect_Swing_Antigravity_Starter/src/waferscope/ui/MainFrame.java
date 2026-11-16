package waferscope.ui;

import waferscope.classifier.DefectClassifier;
import waferscope.classifier.DemoNearestNeighborClassifier;
import waferscope.classifier.PythonInferenceEngine;
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
 * Integrates Header, Footer, Navigation, Dashboard, Wafer Visualizer,
 * Interactive AI Inference Studio, Historical Logs, and COA Profiling.
 */
public class MainFrame extends JFrame {

    // Models & Classifiers
    private final List<WaferMap> loadedWafers = new ArrayList<>();
    private WaferMap currentWafer;
    private final DemoNearestNeighborClassifier demoClassifier = new DemoNearestNeighborClassifier();
    private final PythonInferenceEngine pythonClassifier = new PythonInferenceEngine();
    private boolean preferPythonAi = true;

    // Segregated View Panels
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel viewsCardPanel = new JPanel(cardLayout);

    private final HeaderPanel headerPanel = new HeaderPanel();
    private final FooterPanel footerPanel = new FooterPanel();

    private final DashboardPanel dashboardPanel = new DashboardPanel();
    private final WaferViewPanel waferViewPanel = new WaferViewPanel();
    private final InteractiveInferencePanel inferencePanel = new InteractiveInferencePanel();
    private final HistoryPanel historyPanel = new HistoryPanel();
    private final CoaMetricsPanel coaPanel = new CoaMetricsPanel();
    private final DatasetGuidePanel guidePanel = new DatasetGuidePanel();
    private final OopVivaPanel oopVivaPanel = new OopVivaPanel();

    public MainFrame() {
        super("WaferScope AOI | AI-Based Semiconductor Wafer Defect Detection System");
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

        // 1. Header (Branding, Status Pills & Navigation Tabs)
        add(headerPanel, BorderLayout.NORTH);

        // 2. Segregated Content Card Views
        viewsCardPanel.setOpaque(false);
        viewsCardPanel.add(dashboardPanel, "DASHBOARD");
        viewsCardPanel.add(waferViewPanel, "VISUALIZER");
        viewsCardPanel.add(inferencePanel, "INFERENCE");
        viewsCardPanel.add(historyPanel, "HISTORY");
        viewsCardPanel.add(coaPanel, "COA METRICS");
        viewsCardPanel.add(guidePanel, "DATASET GUIDE");
        viewsCardPanel.add(oopVivaPanel, "OOP VIVA GUIDE");

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
            footerPanel.updateWafer(wafer);
            inferencePanel.setWafer(wafer);
        });

        // Dashboard category select
        dashboardPanel.setOnCategorySelectListener(category -> {
            waferViewPanel.filterByCategory(category);
            cardLayout.show(viewsCardPanel, "VISUALIZER");
        });

        // Dashboard batch scan
        dashboardPanel.setOnBatchScanListener(this::runFullLotBatchScan);

        // History wafer re-select
        historyPanel.setOnWaferSelectListener(waferId -> {
            waferViewPanel.selectWaferById(waferId);
            cardLayout.show(viewsCardPanel, "VISUALIZER");
        });
    }

    private void initKeyShortcuts() {
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    inferencePanel.runInference();
                } else if (e.getKeyCode() == KeyEvent.VK_D) {
                    cardLayout.show(viewsCardPanel, "DASHBOARD");
                } else if (e.getKeyCode() == KeyEvent.VK_V) {
                    cardLayout.show(viewsCardPanel, "VISUALIZER");
                } else if (e.getKeyCode() == KeyEvent.VK_I) {
                    cardLayout.show(viewsCardPanel, "INFERENCE");
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
                    headerPanel.updateSampleCount(loadedWafers.size());
                    dashboardPanel.updateData(loadedWafers);
                    waferViewPanel.setWafers(loadedWafers);

                    updateClassifierSelection();

                    if (!loadedWafers.isEmpty()) {
                        currentWafer = loadedWafers.get(0);
                        footerPanel.updateWafer(currentWafer);
                        inferencePanel.setWafer(currentWafer);
                        headerPanel.setSystemStatus("SYSTEM READY", new Color(34, 197, 94));
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

        inferencePanel.setClassifier(active);
    }

    private void runFullLotBatchScan() {
        if (loadedWafers.isEmpty()) return;

        DefectClassifier classifier = (preferPythonAi && pythonClassifier.isAvailable() && pythonClassifier.hasTrainedModelWeights())
            ? pythonClassifier : demoClassifier;

        JDialog progressDialog = new JDialog(this, "Batch Anomaly Scanning", true);
        progressDialog.setSize(380, 140);
        progressDialog.setLocationRelativeTo(this);
        progressDialog.setLayout(new BorderLayout(10, 10));

        JLabel pLabel = new JLabel("Scanning wafer lot: 0 / " + loadedWafers.size(), JLabel.CENTER);
        pLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        JProgressBar pb = new JProgressBar(0, loadedWafers.size());
        pb.setStringPainted(true);

        JPanel pContent = new JPanel(new GridLayout(2, 1, 6, 6));
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
                        coaPanel.updateMetrics(last);
                    }
                    JOptionPane.showMessageDialog(MainFrame.this,
                        "Batch scan complete!\n" + results.size() + " wafers analyzed across all 9 defect categories.\nCheck the 'History' tab for detailed log.",
                        "Batch Scan Complete", JOptionPane.INFORMATION_MESSAGE);
                    cardLayout.show(viewsCardPanel, "HISTORY");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MainFrame.this, "Batch scan failed: " + ex.getMessage(), "Scan Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
        progressDialog.setVisible(true);
    }
}
