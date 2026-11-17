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
    private final JLabel creditsLabel = new JLabel("WaferScope AOI Platform");

    public FooterPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new CompoundBorder(
            new LineBorder(UiKit.BORDER_COLOR, 1),
            new EmptyBorder(8, 18, 8, 18)
        ));

        waferInfoLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        waferInfoLabel.setForeground(UiKit.TEXT_PRIMARY);
        add(waferInfoLabel, BorderLayout.WEST);

        perfInfoLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        perfInfoLabel.setForeground(UiKit.TEXT_SECONDARY);
        add(perfInfoLabel, BorderLayout.CENTER);
        perfInfoLabel.setHorizontalAlignment(SwingConstants.CENTER);

        creditsLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        creditsLabel.setForeground(UiKit.TEXT_MUTED);
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
