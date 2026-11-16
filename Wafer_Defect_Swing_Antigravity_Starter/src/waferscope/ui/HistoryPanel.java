package waferscope.ui;

import waferscope.io.WaferExporter;
import waferscope.model.PredictionResult;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Inspection History Panel.
 * Tracks analyzed wafers during the session, displays tabular metrics,
 * and allows exporting historical inspection records to CSV.
 */
public class HistoryPanel extends JPanel {

    private final DefaultTableModel tableModel;
    private final JTable historyTable;
    private final List<PredictionResult> historyList = new ArrayList<>();
    private final JLabel countLabel = new JLabel("0 inspections recorded");

    public interface OnWaferSelectListener {
        void onSelect(String waferId);
    }

    private OnWaferSelectListener selectListener;

    public HistoryPanel() {
        setLayout(new BorderLayout(8, 8));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        String[] cols = new String[]{
            "Time", "Wafer ID", "Predicted Pattern", "Status", "Defects", "Yield %", "Score / Conf", "Mode", "Latency (ms)"
        };

        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(tableModel);
        historyTable.setRowHeight(24);
        historyTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        historyTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Center align numerical columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i : new int[]{0, 2, 3, 4, 5, 6, 7, 8}) {
            historyTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        historyTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && selectListener != null) {
                int row = historyTable.getSelectedRow();
                if (row >= 0 && row < historyList.size()) {
                    selectListener.onSelect(historyList.get(row).getWaferId());
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(historyTable);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom toolbar
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        bottomBar.setBorder(new EmptyBorder(8, 0, 0, 0));

        countLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        countLabel.setForeground(new Color(100, 116, 139));
        bottomBar.add(countLabel, BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        JButton exportBtn = new JButton("Export History to CSV");
        exportBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        exportBtn.addActionListener(e -> exportCsv());
        btnPanel.add(exportBtn);

        JButton clearBtn = new JButton("Clear History");
        clearBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        clearBtn.addActionListener(e -> clearHistory());
        btnPanel.add(clearBtn);

        bottomBar.add(btnPanel, BorderLayout.EAST);
        add(bottomBar, BorderLayout.SOUTH);
    }

    public void setOnWaferSelectListener(OnWaferSelectListener listener) {
        this.selectListener = listener;
    }

    public void addRecord(PredictionResult result) {
        historyList.add(result);
        tableModel.addRow(new Object[]{
            result.getTimestamp().length() > 10 ? result.getTimestamp().substring(11) : result.getTimestamp(),
            result.getWaferId(),
            result.getPredictedClass(),
            result.isAnomalous() ? "ANOMALOUS" : "NORMAL",
            result.getDefectiveDieCount(),
            String.format(Locale.US, "%.1f%%", result.getYieldPercentage()),
            String.format(Locale.US, "%.1f%%", result.getConfidenceOrScore() * 100.0),
            result.getModelMode(),
            String.format(Locale.US, "%.2f", result.getProcessingMs())
        });

        countLabel.setText(historyList.size() + " inspection(s) recorded");
    }

    public void clearHistory() {
        historyList.clear();
        tableModel.setRowCount(0);
        countLabel.setText("0 inspections recorded");
    }

    private void exportCsv() {
        if (historyList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No inspection records to export.", "Export History", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("wafer_inspection_history.csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                WaferExporter.exportHistoryToCsv(historyList, file);
                JOptionPane.showMessageDialog(this, "History successfully exported to:\n" + file.getAbsolutePath(), "Export Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to export CSV: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
