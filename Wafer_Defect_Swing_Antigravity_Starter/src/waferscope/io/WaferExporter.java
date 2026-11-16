package waferscope.io;

import waferscope.model.PredictionResult;
import waferscope.model.WaferMap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;

/**
 * Handles exporting analysis history and wafer map representations to disk.
 */
public class WaferExporter {

    /**
     * Exports a list of PredictionResult records to a CSV file.
     */
    public static void exportHistoryToCsv(List<PredictionResult> history, File targetFile) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(targetFile.toPath(), StandardCharsets.UTF_8)) {
            writer.write("Timestamp,Wafer_ID,Predicted_Class,Status,Defective_Dies,Total_Valid_Dies,Defect_Percentage,Yield_Percentage,Score_Or_Confidence,Score_Type,Model_Mode,Latency_ms,Reference_ID\n");
            for (PredictionResult r : history) {
                writer.write(String.format(Locale.US,
                    "\"%s\",\"%s\",\"%s\",\"%s\",%d,%d,%.2f,%.2f,%.4f,\"%s\",\"%s\",%.2f,\"%s\"\n",
                    r.getTimestamp(),
                    escapeCsv(r.getWaferId()),
                    escapeCsv(r.getPredictedClass()),
                    escapeCsv(r.isAnomalous() ? "ANOMALOUS" : "NORMAL"),
                    r.getDefectiveDieCount(),
                    r.getTotalValidDieCount(),
                    r.getDefectPercentage(),
                    r.getYieldPercentage(),
                    r.getConfidenceOrScore(),
                    escapeCsv(r.getScoreLabel()),
                    escapeCsv(r.getModelMode()),
                    r.getProcessingMs(),
                    escapeCsv(r.getReferenceId() == null ? "" : r.getReferenceId())
                ));
            }
        }
    }

    /**
     * Exports a WaferMap grid to a plain text file (matrix rows of 0, 1, E).
     */
    public static void exportWaferMatrix(WaferMap wafer, File targetFile) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(targetFile.toPath(), StandardCharsets.UTF_8)) {
            int rows = wafer.getRows();
            int cols = wafer.getCols();
            for (int r = 0; r < rows; r++) {
                StringBuilder sb = new StringBuilder();
                for (int c = 0; c < cols; c++) {
                    int val = wafer.getDie(r, c);
                    sb.append(val < 0 ? 'E' : (val == 1 ? '1' : '0'));
                }
                sb.append("\n");
                writer.write(sb.toString());
            }
        }
    }

    private static String escapeCsv(String str) {
        if (str == null) return "";
        return str.replace("\"", "\"\"");
    }
}
