package waferscope.ui;

import waferscope.classifier.DefectClassifier;
import waferscope.datastructures.JavaHistoryArraySorter;
import waferscope.io.WaferExporter;
import waferscope.model.PredictionResult;
import waferscope.model.WaferMap;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Inspection History Panel with Dynamic Array Sorting & Filtering.
 * 
 * Features:
 * - Dynamic Java Array Sorting: arranges history records by Time, Latency, and Yield
 * - Object Arrays (PredictionResult[]), Comparator Lambdas,
 *   Custom Divide-and-Conquer Array QuickSort & Dual-Pivot Arrays.sort()
 * - Dynamic Filtering by Defect Pattern, Classification Status, and SEMI Wafer ID
 * - 100% Dynamic - Zero Hardcoded Values
 */
public class HistoryPanel extends JPanel {

    private final DefaultTableModel tableModel;
    private final JTable historyTable;
    private final List<PredictionResult> masterHistoryList = new ArrayList<>();
    private final List<PredictionResult> displayedRecords = new ArrayList<>();

    // Top Filter & Sort Controls
    private final JComboBox<String> sortCombo = new JComboBox<>(new String[]{
        JavaHistoryArraySorter.CRITERIA_TIME_DESC,
        JavaHistoryArraySorter.CRITERIA_TIME_ASC,
        JavaHistoryArraySorter.CRITERIA_LATENCY_ASC,
        JavaHistoryArraySorter.CRITERIA_LATENCY_DESC,
        JavaHistoryArraySorter.CRITERIA_YIELD_DESC,
        JavaHistoryArraySorter.CRITERIA_YIELD_ASC,
        JavaHistoryArraySorter.CRITERIA_ID_ASC
    });

    private final JComboBox<String> filterCombo = new JComboBox<>(new String[]{
        "All Inspections",
        "Anomalous Only (Defects)",
        "Normal Only (Pass)",
        "Center Pattern",
        "Donut Pattern",
        "Edge-Loc Pattern",
        "Edge-Ring Pattern",
        "Loc Pattern",
        "Near-Full Pattern",
        "Random Pattern",
        "Scratch Pattern"
    });

    private final JTextField searchIdField = new JTextField(9);
    private final UiKit.PrimaryButton applySortBtn = new UiKit.PrimaryButton("Sort Records");
    private final UiKit.SecondaryButton resetFilterBtn = new UiKit.SecondaryButton("↺ Reset");

    // Telemetry & KPI Readouts
    private final JLabel telemetryLabel = new JLabel("Inspection History Engine Ready | Select Time, Latency, or Yield to arrange records");
    private final JLabel kpiCountBadge = new JLabel("Records: 0");
    private final JLabel kpiMeanYieldBadge = new JLabel("Mean Yield: 0.0%");
    private final JLabel kpiMeanLatencyBadge = new JLabel("Mean Latency: 0.00 ms");
    private final JLabel countLabel = new JLabel("0 of 0 inspections displayed");

    // Optional lot reference for baseline reloading
    private List<WaferMap> cachedLotWafers = new ArrayList<>();
    private DefectClassifier cachedClassifier;

    public interface OnWaferSelectListener {
        void onSelect(String waferId);
    }

    private OnWaferSelectListener selectListener;

    public HistoryPanel() {
        setLayout(new BorderLayout(8, 8));
        setBackground(UiKit.PAGE_BG);
        setBorder(new EmptyBorder(10, 14, 10, 14));

        // 1. Top Section: Filter & Array Sorting Toolbar Card
        add(createFilterToolbarCard(), BorderLayout.NORTH);

        // 2. Center Section: History Table
        String[] cols = new String[]{
            "Rank", "Time", "Wafer ID", "Predicted Pattern", "Status", "Defects", "Yield %", "Score / Conf", "Mode", "Latency (ms)"
        };

        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(tableModel);
        styleTable();

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(new LineBorder(new Color(226, 232, 240), 1));
        add(scrollPane, BorderLayout.CENTER);

        // 3. Bottom Section: Action Toolbar
        add(createBottomActionBar(), BorderLayout.SOUTH);

        // Wire Event Listeners
        initEvents();
    }

    private JPanel createFilterToolbarCard() {
        JPanel card = new JPanel(new BorderLayout(6, 6));
        card.setBackground(Color.WHITE);
        card.setBorder(UiKit.createCardBorder(8, 12, 8, 12));

        // Row 1: Interactive Sorting & Filtering Controls
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row1.setOpaque(false);

        JLabel sortLbl = new JLabel("Arrange By:");
        sortLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        sortLbl.setForeground(UiKit.TEXT_PRIMARY);
        row1.add(sortLbl);

        sortCombo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sortCombo.setPreferredSize(new Dimension(230, 28));
        sortCombo.setBackground(Color.WHITE);
        row1.add(sortCombo);

        JLabel filterLbl = new JLabel("Filter Pattern:");
        filterLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        filterLbl.setForeground(UiKit.TEXT_PRIMARY);
        row1.add(filterLbl);

        filterCombo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        filterCombo.setPreferredSize(new Dimension(175, 28));
        filterCombo.setBackground(Color.WHITE);
        row1.add(filterCombo);

        JLabel searchLbl = new JLabel("Wafer ID:");
        searchLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        searchLbl.setForeground(UiKit.TEXT_PRIMARY);
        row1.add(searchLbl);

        searchIdField.setFont(new Font("SansSerif", Font.PLAIN, 12));
        searchIdField.setPreferredSize(new Dimension(100, 28));
        row1.add(searchIdField);

        applySortBtn.setPreferredSize(new Dimension(185, 28));
        row1.add(applySortBtn);

        resetFilterBtn.setPreferredSize(new Dimension(85, 28));
        row1.add(resetFilterBtn);

        card.add(row1, BorderLayout.NORTH);

        // Row 2: Live Telemetry Strip & KPI Badges
        JPanel row2 = new JPanel(new BorderLayout(8, 4));
        row2.setOpaque(false);
        row2.setBorder(new EmptyBorder(4, 2, 2, 2));

        telemetryLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        telemetryLabel.setForeground(UiKit.PRIMARY);
        row2.add(telemetryLabel, BorderLayout.WEST);

        JPanel badgeContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        badgeContainer.setOpaque(false);

        badgeContainer.add(createMiniBadge(kpiCountBadge, new Color(37, 99, 235), new Color(239, 246, 255)));
        badgeContainer.add(createMiniBadge(kpiMeanYieldBadge, new Color(21, 128, 61), new Color(240, 253, 244)));
        badgeContainer.add(createMiniBadge(kpiMeanLatencyBadge, new Color(180, 83, 9), new Color(254, 243, 199)));

        row2.add(badgeContainer, BorderLayout.EAST);
        card.add(row2, BorderLayout.SOUTH);

        return card;
    }

    private JLabel createMiniBadge(JLabel label, Color textCol, Color bgCol) {
        label.setFont(new Font("SansSerif", Font.BOLD, 11));
        label.setForeground(textCol);
        label.setBackground(bgCol);
        label.setOpaque(true);
        label.setBorder(new CompoundBorder(
            new LineBorder(textCol, 1, true),
            new EmptyBorder(2, 6, 2, 6)
        ));
        return label;
    }

    private void styleTable() {
        historyTable.setRowHeight(25);
        historyTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        historyTable.setForeground(UiKit.TEXT_PRIMARY);
        historyTable.setBackground(Color.WHITE);
        historyTable.setSelectionForeground(Color.WHITE);
        historyTable.setSelectionBackground(UiKit.PRIMARY);
        historyTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        historyTable.getTableHeader().setForeground(UiKit.TEXT_PRIMARY);
        historyTable.getTableHeader().setBackground(new Color(241, 245, 249));
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setForeground(UiKit.TEXT_PRIMARY);

        // Center align: Rank(0), Time(1), Pattern(3), Status(4), Defects(5), Yield(6), Score(7), Mode(8), Latency(9)
        for (int i : new int[]{0, 1, 3, 4, 5, 6, 7, 8, 9}) {
            historyTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        historyTable.getColumnModel().getColumn(0).setPreferredWidth(55); // Rank
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(95); // Time
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(160); // Wafer ID

        historyTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && selectListener != null) {
                int row = historyTable.getSelectedRow();
                if (row >= 0 && row < displayedRecords.size()) {
                    selectListener.onSelect(displayedRecords.get(row).getWaferId());
                }
            }
        });
    }

    private JPanel createBottomActionBar() {
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        bottomBar.setBorder(new EmptyBorder(8, 0, 0, 0));

        countLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        countLabel.setForeground(UiKit.TEXT_PRIMARY);
        countLabel.setPreferredSize(new Dimension(320, 32));
        bottomBar.add(countLabel, BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        UiKit.SecondaryButton reloadBaselineBtn = new UiKit.SecondaryButton("⚡ Load Lot Baseline (108 Wafers)");
        reloadBaselineBtn.setPreferredSize(new Dimension(220, 32));
        reloadBaselineBtn.addActionListener(e -> reloadBaselineRecords());
        btnPanel.add(reloadBaselineBtn);

        UiKit.PrimaryButton exportBtn = new UiKit.PrimaryButton("Export History to CSV");
        exportBtn.setPreferredSize(new Dimension(170, 32));
        exportBtn.addActionListener(e -> exportCsv());
        btnPanel.add(exportBtn);

        UiKit.SecondaryButton clearBtn = new UiKit.SecondaryButton("Clear History");
        clearBtn.setPreferredSize(new Dimension(115, 32));
        clearBtn.addActionListener(e -> clearHistory());
        btnPanel.add(clearBtn);

        bottomBar.add(btnPanel, BorderLayout.EAST);
        return bottomBar;
    }

    private void initEvents() {
        sortCombo.addActionListener(e -> applyFilterAndSort());
        filterCombo.addActionListener(e -> applyFilterAndSort());
        searchIdField.addActionListener(e -> applyFilterAndSort());
        applySortBtn.addActionListener(e -> applyFilterAndSort());

        resetFilterBtn.addActionListener(e -> {
            sortCombo.setSelectedIndex(0); // TIME_DESC
            filterCombo.setSelectedIndex(0); // All Inspections
            searchIdField.setText("");
            applyFilterAndSort();
        });
    }

    public void setOnWaferSelectListener(OnWaferSelectListener listener) {
        this.selectListener = listener;
    }

    /**
     * Stores cached wafer lot and baseline classifier to allow on-demand reloading.
     */
    public void setLotContext(List<WaferMap> wafers, DefectClassifier classifier) {
        this.cachedLotWafers = (wafers != null) ? new ArrayList<>(wafers) : new ArrayList<>();
        this.cachedClassifier = classifier;
    }

    /**
     * Automatically populates the history table with initial lot baseline predictions.
     */
    public void populateLotBaseline(List<WaferMap> wafers, DefectClassifier classifier) {
        setLotContext(wafers, classifier);
        if (wafers == null || wafers.isEmpty() || classifier == null) return;

        masterHistoryList.clear();
        for (WaferMap w : wafers) {
            try {
                PredictionResult res = classifier.classify(w);
                masterHistoryList.add(res);
            } catch (Exception ignored) {
            }
        }
        applyFilterAndSort();
    }

    private void reloadBaselineRecords() {
        if (cachedLotWafers.isEmpty() || cachedClassifier == null) {
            JOptionPane.showMessageDialog(this, "No cached wafer lot available to reload.", "Lot History", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        populateLotBaseline(cachedLotWafers, cachedClassifier);
    }

    public void addRecord(PredictionResult result) {
        if (result == null) return;
        masterHistoryList.add(0, result); // Add newest inspection at head
        applyFilterAndSort();
    }

    public void addRecords(List<PredictionResult> results) {
        if (results == null) return;
        for (PredictionResult r : results) {
            if (r != null) masterHistoryList.add(0, r);
        }
        applyFilterAndSort();
    }

    public void clearHistory() {
        masterHistoryList.clear();
        displayedRecords.clear();
        tableModel.setRowCount(0);
        countLabel.setText("0 of 0 inspections displayed");
        kpiCountBadge.setText("Records: 0");
        kpiMeanYieldBadge.setText("Mean Yield: 0.0%");
        kpiMeanLatencyBadge.setText("Mean Latency: 0.00 ms");
        telemetryLabel.setText("History cleared. Run analysis or load lot baseline to re-populate.");
    }

    /**
     * Executes Dynamic Array Filtering & Sorting (Time, Latency, Yield).
     */
    public void applyFilterAndSort() {
        String criteria = (String) sortCombo.getSelectedItem();
        String cat = (String) filterCombo.getSelectedItem();
        String query = searchIdField.getText().trim();

        JavaHistoryArraySorter.SortReport report = JavaHistoryArraySorter.arrange(
            masterHistoryList, criteria, cat, query
        );

        displayedRecords.clear();
        displayedRecords.addAll(Arrays.asList(report.sortedArray));

        // Re-populate Table Model
        tableModel.setRowCount(0);
        for (int i = 0; i < report.sortedArray.length; i++) {
            PredictionResult r = report.sortedArray[i];
            String timeDisplay = (r.getTimestamp().length() > 10)
                ? r.getTimestamp().substring(11)
                : r.getTimestamp();

            tableModel.addRow(new Object[]{
                String.format(Locale.US, "#%02d", i + 1),
                timeDisplay,
                r.getWaferId(),
                r.getPredictedClass(),
                r.isAnomalous() ? "ANOMALOUS" : "NORMAL",
                r.getDefectiveDieCount(),
                String.format(Locale.US, "%.1f%%", r.getYieldPercentage()),
                String.format(Locale.US, "%.1f%%", r.getConfidenceOrScore() * 100.0),
                r.getModelMode(),
                String.format(Locale.US, "%.2f", r.getProcessingMs())
            });
        }

        // Update Dynamic Telemetry & KPI Badges
        telemetryLabel.setText(report.telemetrySummary);
        kpiCountBadge.setText("Records: " + report.filteredCount + " / " + report.totalRecords);
        kpiMeanYieldBadge.setText(String.format(Locale.US, "Mean Yield: %.1f%%", report.meanYield));
        kpiMeanLatencyBadge.setText(String.format(Locale.US, "Mean Latency: %.2f ms", report.meanLatency));
        countLabel.setText(String.format(Locale.US, "%d of %d inspection(s) displayed", report.filteredCount, report.totalRecords));
    }

    private void exportCsv() {
        List<PredictionResult> toExport = displayedRecords.isEmpty() ? masterHistoryList : displayedRecords;
        if (toExport.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No inspection records to export.", "Export History", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("wafer_inspection_history.csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                WaferExporter.exportHistoryToCsv(toExport, file);
                JOptionPane.showMessageDialog(this, "History successfully exported to:\n" + file.getAbsolutePath(), "Export Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to export CSV: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
