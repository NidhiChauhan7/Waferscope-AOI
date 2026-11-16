package waferscope.ui;

import waferscope.model.PredictionResult;
import waferscope.model.WaferMap;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Locale;

/**
 * Modern status footer displaying real-time inspection breadcrumbs,
 * hardware memory utilization, and system performance metrics.
 */
public class FooterPanel extends JPanel {

    private final JLabel waferInfoLabel = new JLabel("No wafer selected");
    private final JLabel perfInfoLabel = new JLabel("Latency: - | Heap: - | Cores: " + Runtime.getRuntime().availableProcessors());
    private final JLabel creditsLabel = new JLabel("MIT AOE Pune | Java - C++ - CG - COA");

    public FooterPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(241, 245, 249));
        setBorder(new CompoundBorder(
            new LineBorder(new Color(226, 232, 240), 1),
            new EmptyBorder(6, 16, 6, 16)
        ));

        waferInfoLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        waferInfoLabel.setForeground(new Color(30, 41, 59));
        add(waferInfoLabel, BorderLayout.WEST);

        perfInfoLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        perfInfoLabel.setForeground(new Color(71, 85, 105));
        add(perfInfoLabel, BorderLayout.CENTER);
        perfInfoLabel.setHorizontalAlignment(SwingConstants.CENTER);

        creditsLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        creditsLabel.setForeground(new Color(100, 116, 139));
        add(creditsLabel, BorderLayout.EAST);

        refreshMemoryStatus(0.0);
    }

    public void updateWafer(WaferMap wafer) {
        if (wafer == null) {
            waferInfoLabel.setText("No wafer loaded");
            return;
        }
        waferInfoLabel.setText(String.format(Locale.US,
            "Wafer: %s [%s] - %dx%d - Valid Dies: %d (Defective: %d, %.1f%%)",
            wafer.getId(), wafer.getGroundTruthLabel(),
            wafer.getRows(), wafer.getCols(),
            wafer.getValidCount(), wafer.getDefectiveCount(), wafer.getDefectPercentage()
        ));
    }

    public void updateResult(PredictionResult result) {
        if (result != null) {
            refreshMemoryStatus(result.getProcessingMs());
        }
    }

    public void refreshMemoryStatus(double latencyMs) {
        Runtime rt = Runtime.getRuntime();
        double usedMb = (rt.totalMemory() - rt.freeMemory()) / (1024.0 * 1024.0);
        double maxMb = rt.maxMemory() / (1024.0 * 1024.0);

        String latStr = (latencyMs > 0) ? String.format(Locale.US, "Latency: %.2f ms | ", latencyMs) : "";
        perfInfoLabel.setText(String.format(Locale.US,
            "%sHeap: %.1f MB / %.1f MB | Logical Cores: %d",
            latStr, usedMb, maxMb, rt.availableProcessors()
        ));
    }
}
