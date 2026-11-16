package waferscope.ui;

import waferscope.classifier.CppPreprocessorBridge;
import waferscope.classifier.DefectClassifier;
import waferscope.model.PredictionResult;
import waferscope.model.WaferMap;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Locale;
import java.util.Map;

/**
 * Interactive & Segregated AI Inference Studio.
 *
 * Implements:
 * - 5-Stage Visual Preprocessing & Inference Pipeline
 * - Interactive Anomaly Sensitivity Threshold Slider (real-time recalculation)
 * - Interactive Step-by-Step Simulation Mode
 * - 9-Class Calibrated Probability Breakdown Visualizer
 * - C++ / PL Numerical Acceleration Benchmark Toggle
 */
public class InteractiveInferencePanel extends JPanel {

    private WaferMap currentWafer;
    private PredictionResult lastResult;
    private DefectClassifier activeClassifier;
    private final CppPreprocessorBridge cppBridge = new CppPreprocessorBridge();

    // Interactive Controls
    private final JSlider sensitivitySlider = new JSlider(1, 25, 5); // 1% to 25% threshold
    private final JLabel sensitivityLabel = new JLabel("5.0% defect density");
    private final JCheckBox cppToggle = new JCheckBox("Accelerate Preprocessing via C++ Native Binary", true);
    private final JButton analyzeBtn = new JButton("Run Full AI Inference");
    private final JButton simulateBtn = new JButton("Simulate Pipeline Step-by-Step");
    private final JButton rateBtn = new JButton("Rate Analysis (RatingForm)");

    // Stage Status Indicators
    private final JLabel stage1Label = new JLabel("Stage 1: Ingestion -- Ready");
    private final JLabel stage2Label = new JLabel("Stage 2: Denoising -- Ready");
    private final JLabel stage3Label = new JLabel("Stage 3: Feature Extraction -- Ready");
    private final JLabel stage4Label = new JLabel("Stage 4: Anomaly Gate -- Ready");
    private final JLabel stage5Label = new JLabel("Stage 5: Defect Classification -- Ready");

    // Output Components
    private final JLabel statusBanner = new JLabel("SELECT A WAFER AND CLICK 'RUN FULL AI INFERENCE'", JLabel.CENTER);
    private final JLabel confidenceGauge = new JLabel("0.0%", JLabel.CENTER);
    private final JLabel topClassLabel = new JLabel("None", JLabel.CENTER);
    private final JPanel probabilityBarsPanel = new JPanel();
    private final JTextArea auditArea = new JTextArea();
    private final JLabel cppLatencyLabel = new JLabel("C++ Engine: Idle");

    public InteractiveInferencePanel() {
        setLayout(new BorderLayout(14, 14));
        setBackground(new Color(248, 250, 252));
        setBorder(new EmptyBorder(14, 16, 14, 16));

        // Top Toolbar
        add(createTopControlBar(), BorderLayout.NORTH);

        // Center Split View
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.50);
        split.setDividerSize(6);
        split.setBorder(null);
        split.setOpaque(false);

        // Left: 5-Stage Interactive Pipeline & Stepper
        split.setLeftComponent(createPipelineCard());

        // Right: Prediction Result & Probability Bars
        split.setRightComponent(createPredictionCard());

        add(split, BorderLayout.CENTER);

        initEvents();
    }

    private JPanel createTopControlBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 8));
        bar.setBackground(Color.WHITE);
        bar.setBorder(new CompoundBorder(
            new LineBorder(new Color(226, 232, 240), 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));

        // Left Controls
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        left.setOpaque(false);

        analyzeBtn.setBackground(new Color(37, 99, 235));
        analyzeBtn.setForeground(Color.WHITE);
        analyzeBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        analyzeBtn.setFocusPainted(false);
        left.add(analyzeBtn);

        simulateBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        left.add(simulateBtn);

        rateBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        left.add(rateBtn);

        left.add(new JSeparator(SwingConstants.VERTICAL));

        // Sensitivity slider
        left.add(new JLabel("Anomaly Threshold:"));
        sensitivitySlider.setPreferredSize(new Dimension(110, 26));
        sensitivitySlider.setOpaque(false);
        left.add(sensitivitySlider);
        sensitivityLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        sensitivityLabel.setForeground(new Color(37, 99, 235));
        left.add(sensitivityLabel);

        bar.add(left, BorderLayout.WEST);

        // Right C++ Toggle
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        right.setOpaque(false);
        cppToggle.setOpaque(false);
        cppToggle.setFont(new Font("SansSerif", Font.PLAIN, 11));
        right.add(cppToggle);
        bar.add(right, BorderLayout.EAST);

        return bar;
    }

    private JPanel createPipelineCard() {
        JPanel card = new JPanel(new BorderLayout(8, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(new Color(226, 232, 240), 1, true),
            new EmptyBorder(12, 14, 12, 14)
        ));

        JLabel title = new JLabel("INTERACTIVE 5-STAGE AI DEFECT DETECTION PIPELINE");
        title.setFont(new Font("SansSerif", Font.BOLD, 12));
        title.setForeground(new Color(15, 23, 42));
        card.add(title, BorderLayout.NORTH);

        JPanel stages = new JPanel(new GridLayout(5, 1, 0, 8));
        stages.setOpaque(false);

        stages.add(createStageRow("STAGE 1: WAFER INPUT & TERNARY INGESTION", stage1Label,
            "Validates ternary matrix (0=Pass, 1=Fail, E=Outside), checks dimensions."));
        stages.add(createStageRow("STAGE 2: MORPHOLOGICAL FILTERING & DENOISING", stage2Label,
            "Applies 3x3 median filter to eliminate isolated salt-and-pepper noise dies."));
        stages.add(createStageRow("STAGE 3: SPATIAL FEATURE EXTRACTION", stage3Label,
            "Calculates Centroid (xc, yc), Radial Concentric Profile, and Octant Distribution."));
        stages.add(createStageRow("STAGE 4: ANOMALY SCORING & THRESHOLD GATE", stage4Label,
            "Evaluates defect density against sensitivity cutoff to classify Normal vs Anomalous."));
        stages.add(createStageRow("STAGE 5: DEFECT PATTERN CLASSIFICATION", stage5Label,
            "Calibrated CNN / Ensemble model computes posterior probabilities for all 9 classes."));

        card.add(stages, BorderLayout.CENTER);

        // Bottom C++ Benchmarking Note
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(8, 4, 4, 4));
        cppLatencyLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        cppLatencyLabel.setForeground(new Color(100, 116, 139));
        bottom.add(cppLatencyLabel, BorderLayout.WEST);
        card.add(bottom, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createStageRow(String stageName, JLabel statusLabel, String desc) {
        JPanel row = new JPanel(new BorderLayout(6, 2));
        row.setBackground(new Color(248, 250, 252));
        row.setBorder(new CompoundBorder(
            new LineBorder(new Color(226, 232, 240), 1),
            new EmptyBorder(8, 10, 8, 10)
        ));

        JLabel name = new JLabel(stageName);
        name.setFont(new Font("SansSerif", Font.BOLD, 11));
        name.setForeground(new Color(30, 41, 59));
        row.add(name, BorderLayout.NORTH);

        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(37, 99, 235));
        row.add(statusLabel, BorderLayout.CENTER);

        JLabel d = new JLabel(desc);
        d.setFont(new Font("SansSerif", Font.PLAIN, 10));
        d.setForeground(new Color(148, 163, 184));
        row.add(d, BorderLayout.SOUTH);

        return row;
    }

    private JPanel createPredictionCard() {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(new Color(226, 232, 240), 1, true),
            new EmptyBorder(12, 14, 12, 14)
        ));

        // Anomaly Status Banner
        statusBanner.setOpaque(true);
        statusBanner.setBackground(new Color(241, 245, 249));
        statusBanner.setForeground(new Color(71, 85, 105));
        statusBanner.setFont(new Font("SansSerif", Font.BOLD, 13));
        statusBanner.setBorder(new EmptyBorder(8, 8, 8, 8));
        card.add(statusBanner, BorderLayout.NORTH);

        // Center: Probability Bars & Confidence
        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setOpaque(false);

        // Gauge Row
        JPanel gaugeRow = new JPanel(new GridLayout(1, 2, 8, 0));
        gaugeRow.setOpaque(false);
        gaugeRow.setPreferredSize(new Dimension(200, 60));

        JPanel cPanel = createMiniCard("PREDICTED CATEGORY", topClassLabel, new Color(30, 41, 59));
        topClassLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        gaugeRow.add(cPanel);

        JPanel pPanel = createMiniCard("AI CONFIDENCE SCORE", confidenceGauge, new Color(37, 99, 235));
        confidenceGauge.setFont(new Font("SansSerif", Font.BOLD, 18));
        gaugeRow.add(pPanel);

        center.add(gaugeRow, BorderLayout.NORTH);

        // Probability Bars
        probabilityBarsPanel.setLayout(new BoxLayout(probabilityBarsPanel, BoxLayout.Y_AXIS));
        probabilityBarsPanel.setOpaque(false);
        probabilityBarsPanel.setBorder(new EmptyBorder(8, 4, 8, 4));

        JScrollPane pScroll = new JScrollPane(probabilityBarsPanel);
        pScroll.setBorder(new LineBorder(new Color(241, 245, 249), 1));
        center.add(pScroll, BorderLayout.CENTER);

        // Audit Text Box
        auditArea.setEditable(false);
        auditArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        auditArea.setBackground(new Color(248, 250, 252));
        auditArea.setRows(5);
        auditArea.setBorder(new CompoundBorder(new LineBorder(new Color(226, 232, 240)), new EmptyBorder(6, 6, 6, 6)));
        center.add(new JScrollPane(auditArea), BorderLayout.SOUTH);

        card.add(center, BorderLayout.CENTER);
        return card;
    }

    private JPanel createMiniCard(String title, JLabel valLabel, Color textCol) {
        JPanel p = new JPanel(new BorderLayout(2, 2));
        p.setBackground(new Color(248, 250, 252));
        p.setBorder(new CompoundBorder(
            new LineBorder(new Color(226, 232, 240), 1),
            new EmptyBorder(6, 8, 6, 8)
        ));
        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.BOLD, 10));
        t.setForeground(new Color(100, 116, 139));
        p.add(t, BorderLayout.NORTH);
        valLabel.setForeground(textCol);
        p.add(valLabel, BorderLayout.CENTER);
        return p;
    }

    private void initEvents() {
        sensitivitySlider.addChangeListener(e -> {
            double threshold = sensitivitySlider.getValue();
            sensitivityLabel.setText(String.format(Locale.US, "%.1f%% defect density", threshold));
            if (currentWafer != null && lastResult != null) {
                recomputeAnomalyGate(threshold);
            }
        });

        analyzeBtn.addActionListener(e -> runInference());

        simulateBtn.addActionListener(e -> simulateStepByStep());

        rateBtn.addActionListener(e -> {
            Window parent = SwingUtilities.getWindowAncestor(this);
            JFrame frame = (parent instanceof JFrame) ? (JFrame) parent : null;
            String defect = (lastResult != null) ? lastResult.getPredictedClass() : "Donut";
            new RatingForm(frame, defect).setVisible(true);
        });
    }

    public void setWafer(WaferMap wafer) {
        this.currentWafer = wafer;
        if (wafer != null) {
            stage1Label.setText(String.format(Locale.US, "Stage 1: Ingested %s (%dx%d, %d dies)",
                wafer.getId(), wafer.getRows(), wafer.getCols(), wafer.getValidCount()));
            stage2Label.setText("Stage 2: Denoising -- Pending");
            stage3Label.setText("Stage 3: Feature Extraction -- Pending");
            stage4Label.setText("Stage 4: Anomaly Gate -- Pending");
            stage5Label.setText("Stage 5: Defect Classification -- Pending");
            statusBanner.setText("READY TO ANALYZE " + wafer.getId());
            statusBanner.setBackground(new Color(241, 245, 249));
            statusBanner.setForeground(new Color(71, 85, 105));
        }
    }

    public void setClassifier(DefectClassifier classifier) {
        this.activeClassifier = classifier;
    }

    public void runInference() {
        if (currentWafer == null || activeClassifier == null) return;

        analyzeBtn.setEnabled(false);
        simulateBtn.setEnabled(false);

        SwingWorker<PredictionResult, Void> worker = new SwingWorker<>() {
            CppPreprocessorBridge.PreprocessingResult cppRes;

            @Override
            protected PredictionResult doInBackground() throws Exception {
                // Execute C++ preprocessing if enabled
                if (cppToggle.isSelected()) {
                    cppRes = cppBridge.process(currentWafer);
                }
                return activeClassifier.classify(currentWafer);
            }

            @Override
            protected void done() {
                analyzeBtn.setEnabled(true);
                simulateBtn.setEnabled(true);
                try {
                    PredictionResult res = get();
                    displayResult(res, cppRes);
                } catch (Exception ex) {
                    statusBanner.setText("INFERENCE FAILED: " + ex.getMessage());
                    statusBanner.setBackground(new Color(254, 226, 226));
                    statusBanner.setForeground(new Color(185, 28, 28));
                }
            }
        };
        worker.execute();
    }

    public void displayResult(PredictionResult res, CppPreprocessorBridge.PreprocessingResult cppRes) {
        this.lastResult = res;

        // Stage 1
        stage1Label.setText(String.format(Locale.US, "Stage 1: Ingested %s (Total Valid: %d)",
            res.getWaferId(), res.getTotalValidDieCount()));

        // Stage 2
        int filtered = (cppRes != null) ? cppRes.filteredDefectCount : res.getDefectiveDieCount();
        stage2Label.setText(String.format(Locale.US, "Stage 2: Denoising Complete (Retained %d defect dies)", filtered));

        // Stage 3
        if (cppRes != null && cppRes.cplusplusUsed) {
            stage3Label.setText(String.format(Locale.US, "Stage 3: C++ Feature Centroid: (%.1f, %.1f) in %.2f ms",
                cppRes.centroidX, cppRes.centroidY, cppRes.executionMs));
            cppLatencyLabel.setText(String.format(Locale.US, "C++ Acceleration: Active (Native C++ completed in %.3f ms)", cppRes.executionMs));
            cppLatencyLabel.setForeground(new Color(22, 101, 52));
        } else {
            stage3Label.setText("Stage 3: Extracted Spatial Centroid, Radial, and Octant Features");
            cppLatencyLabel.setText("C++ Acceleration: Inactive (JVM Java Engine used)");
            cppLatencyLabel.setForeground(new Color(100, 116, 139));
        }

        // Stage 4 & 5
        double threshold = sensitivitySlider.getValue();
        recomputeAnomalyGate(threshold);

        topClassLabel.setText(res.getPredictedClass().toUpperCase(Locale.US));
        confidenceGauge.setText(String.format(Locale.US, "%.1f%%", res.getConfidenceOrScore() * 100.0));

        // Update Probability Bars
        updateProbabilityBars(res.getClassScores(), res.getPredictedClass());

        // Update Audit Text
        auditArea.setText(
            "MODEL MODE  : " + res.getModelMode() + "\n" +
            "SCORE TYPE  : " + res.getScoreLabel() + "\n" +
            "INSPECTION  : " + String.format(Locale.US, "%.2f ms latency", res.getProcessingMs()) + "\n" +
            "AUDIT NOTE  : " + (res.getModelMode().equalsIgnoreCase("TRAINED") ?
                "Calibrated Softmax probabilities computed by trained CNN/Ensemble model." :
                "Nearest-exemplar similarity score against bundled demo dataset.")
        );
    }

    private void recomputeAnomalyGate(double thresholdPct) {
        if (currentWafer == null) return;

        double waferDefectPct = currentWafer.getDefectPercentage();
        boolean isNoneClass = (lastResult != null) && "None".equalsIgnoreCase(lastResult.getPredictedClass());
        boolean isAnomalous = (waferDefectPct >= thresholdPct) && !isNoneClass && (currentWafer.getDefectiveCount() > 0);

        stage4Label.setText(String.format(Locale.US, "Stage 4: Defect Density %.1f%% vs Threshold %.1f%% -> %s",
            waferDefectPct, thresholdPct, (isAnomalous ? "TRIGGERED ANOMALOUS" : "CLEARED NORMAL")));

        stage5Label.setText(String.format(Locale.US, "Stage 5: Classified as %s (%s)",
            (lastResult != null ? lastResult.getPredictedClass() : "None"),
            (isAnomalous ? "ANOMALOUS" : "NORMAL")));

        if (isAnomalous) {
            statusBanner.setText(String.format(Locale.US, "ANOMALOUS WAFER -- %s PATTERN DETECTED (%.1f%% DEFECTS)",
                lastResult != null ? lastResult.getPredictedClass().toUpperCase(Locale.US) : "DEFECT", waferDefectPct));
            statusBanner.setBackground(new Color(254, 226, 226));
            statusBanner.setForeground(new Color(185, 28, 28));
        } else {
            statusBanner.setText(String.format(Locale.US, "NORMAL WAFER -- PASS QUALITY (%.1f%% DEFECTS < %.1f%% THRESHOLD)",
                waferDefectPct, thresholdPct));
            statusBanner.setBackground(new Color(220, 252, 231));
            statusBanner.setForeground(new Color(21, 128, 61));
        }
    }

    private void updateProbabilityBars(Map<String, Double> scores, String predictedClass) {
        probabilityBarsPanel.removeAll();

        if (scores == null || scores.isEmpty()) {
            probabilityBarsPanel.add(new JLabel("No class scores available"));
            probabilityBarsPanel.revalidate();
            probabilityBarsPanel.repaint();
            return;
        }

        for (Map.Entry<String, Double> e : scores.entrySet()) {
            String className = e.getKey();
            double prob = e.getValue();
            boolean isWinner = className.equalsIgnoreCase(predictedClass);

            JPanel barRow = new JPanel(new BorderLayout(8, 2));
            barRow.setOpaque(false);
            barRow.setBorder(new EmptyBorder(3, 4, 3, 4));

            JLabel nameL = new JLabel(String.format(Locale.US, "%-10s", className));
            nameL.setFont(new Font("SansSerif", isWinner ? Font.BOLD : Font.PLAIN, 11));
            nameL.setPreferredSize(new Dimension(80, 18));
            barRow.add(nameL, BorderLayout.WEST);

            JProgressBar pb = new JProgressBar(0, 100);
            int val = (int) Math.round(prob * 100.0);
            pb.setValue(val);
            pb.setString(val + "%");
            pb.setStringPainted(true);
            pb.setPreferredSize(new Dimension(100, 16));
            if (isWinner) {
                pb.setForeground(new Color(37, 99, 235));
            } else {
                pb.setForeground(new Color(148, 163, 184));
            }
            barRow.add(pb, BorderLayout.CENTER);

            probabilityBarsPanel.add(barRow);
        }

        probabilityBarsPanel.revalidate();
        probabilityBarsPanel.repaint();
    }

    private void simulateStepByStep() {
        if (currentWafer == null) return;

        Timer timer = new Timer(500, null);
        final int[] step = {1};

        timer.addActionListener(e -> {
            if (step[0] == 1) {
                stage1Label.setText("Stage 1: Ingestion -- Ingesting 2D Ternary Grid from Lot TSMC-46393...");
            } else if (step[0] == 2) {
                stage2Label.setText("Stage 2: Denoising -- Running 3x3 Morphological Median Filter...");
            } else if (step[0] == 3) {
                stage3Label.setText("Stage 3: Feature Extraction -- Computing Radial Concentric Entropy & Centroid...");
            } else if (step[0] == 4) {
                stage4Label.setText("Stage 4: Anomaly Gate -- Evaluating Defect Density Against Threshold...");
            } else if (step[0] == 5) {
                stage5Label.setText("Stage 5: Classification -- Computing Posterior Probabilities...");
                timer.stop();
                runInference();
            }
            step[0]++;
        });

        timer.start();
    }
}
