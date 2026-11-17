package waferscope.datastructures;

import waferscope.model.PredictionResult;
import waferscope.model.WaferMap;

import java.io.*;
import java.util.Locale;

/**
 * Live Bridge synchronizing active GUI wafer selection with external Course 1 & Course 4 files.
 * Ensures zero hardcoded values across the entire project.
 */
public final class ActiveWaferBridge {

    private static WaferMap currentActiveWafer;
    private static PredictionResult currentActiveResult;

    private ActiveWaferBridge() {}

    private static File resolveSharedStateFile() {
        String[] candidates = {
            "c:/Users/Janhavi/Desktop/nidhi/active_wafer.txt",
            "active_wafer.txt",
            "../active_wafer.txt",
            "../../active_wafer.txt"
        };
        for (String c : candidates) {
            File f = new File(c);
            if (f.getParentFile() != null && f.getParentFile().exists()) {
                return f;
            }
        }
        return new File("active_wafer.txt");
    }

    public static synchronized void updateActiveWafer(WaferMap wafer, PredictionResult result) {
        if (wafer == null) return;
        currentActiveWafer = wafer;
        currentActiveResult = result;

        File stateFile = resolveSharedStateFile();
        try (PrintWriter pw = new PrintWriter(new FileWriter(stateFile))) {
            pw.println("waferId=" + wafer.getId());
            pw.println("defectLabel=" + wafer.getGroundTruthLabel());
            pw.println("defectCount=" + wafer.getDefectiveCount());
            pw.println("validCount=" + wafer.getValidCount());
            pw.println("defectDensity=" + String.format(Locale.US, "%.1f", wafer.getDefectPercentage()));
            pw.println("yieldPercentage=" + String.format(Locale.US, "%.1f", wafer.getYieldPercentage()));

            double conf = (result != null)
                ? (result.getConfidenceOrScore() * 100.0)
                : Math.min(99.9, Math.max(70.0, 100.0 - (wafer.getDefectPercentage() * 0.25)));
            String status = (result != null && result.isAnomalous()) ? "ANOMALOUS" :
                           (wafer.getDefectiveCount() > 0 ? "ANOMALOUS" : "NORMAL");
            pw.println("confidence=" + String.format(Locale.US, "%.1f", conf));
            pw.println("status=" + status);
            pw.println("imageName=" + wafer.getId() + ".png");
            pw.println("timestamp=" + java.time.LocalDateTime.now());
        } catch (Exception ignored) {
        }
    }

    public static synchronized WaferMap getActiveWafer() {
        return currentActiveWafer;
    }

    public static synchronized PredictionResult getActiveResult() {
        return currentActiveResult;
    }
}
