package waferscope.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Encapsulates the complete result of wafer defect analysis.
 * Adheres to project honesty rules:
 * - Distinguishes between DEMO (nearest-example similarity) and TRAINED (CNN/AI model).
 * - Calibrated model confidence is only reported when produced by a trained model.
 * - Records latency, hardware timing, and class scores.
 */
public class PredictionResult {
    private final String waferId;
    private final String predictedClass;
    private final boolean anomalous;
    private final double confidenceOrScore;
    private final String scoreLabel;
    private final Map<String, Double> classScores;
    private final int defectiveDieCount;
    private final int totalValidDieCount;
    private final double processingMs;
    private final String modelMode; // "DEMO" or "TRAINED"
    private final String methodExplanation;
    private final String referenceId;
    private final String timestamp;
    private final Map<String, Object> hardwareMetrics;

    public PredictionResult(Builder builder) {
        this.waferId = builder.waferId;
        this.predictedClass = builder.predictedClass;
        this.anomalous = builder.anomalous;
        this.confidenceOrScore = builder.confidenceOrScore;
        this.scoreLabel = builder.scoreLabel;
        this.classScores = (builder.classScores != null) ? Collections.unmodifiableMap(new LinkedHashMap<>(builder.classScores)) : Collections.emptyMap();
        this.defectiveDieCount = builder.defectiveDieCount;
        this.totalValidDieCount = builder.totalValidDieCount;
        this.processingMs = builder.processingMs;
        this.modelMode = builder.modelMode;
        this.methodExplanation = builder.methodExplanation;
        this.referenceId = builder.referenceId;
        this.timestamp = (builder.timestamp != null) ? builder.timestamp : LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.hardwareMetrics = (builder.hardwareMetrics != null) ? Collections.unmodifiableMap(new LinkedHashMap<>(builder.hardwareMetrics)) : Collections.emptyMap();
    }

    public String getWaferId() {
        return waferId;
    }

    public String getPredictedClass() {
        return predictedClass;
    }

    public boolean isAnomalous() {
        return anomalous;
    }

    public double getConfidenceOrScore() {
        return confidenceOrScore;
    }

    public String getScoreLabel() {
        return scoreLabel;
    }

    public Map<String, Double> getClassScores() {
        return classScores;
    }

    public int getDefectiveDieCount() {
        return defectiveDieCount;
    }

    public int getTotalValidDieCount() {
        return totalValidDieCount;
    }

    public int getGoodDieCount() {
        return Math.max(0, totalValidDieCount - defectiveDieCount);
    }

    public double getDefectPercentage() {
        return (totalValidDieCount > 0) ? ((double) defectiveDieCount / totalValidDieCount) * 100.0 : 0.0;
    }

    public double getYieldPercentage() {
        return 100.0 - getDefectPercentage();
    }

    public double getProcessingMs() {
        return processingMs;
    }

    public String getModelMode() {
        return modelMode;
    }

    public String getMethodExplanation() {
        return methodExplanation;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getHardwareMetrics() {
        return hardwareMetrics;
    }

    public String getStatusDescription() {
        if ("None".equalsIgnoreCase(predictedClass) || (!anomalous && defectiveDieCount == 0)) {
            return "NORMAL / NO SYSTEMATIC DEFECT PATTERN";
        }
        return "ANOMALOUS / " + predictedClass.toUpperCase(Locale.US) + " PATTERN DETECTED";
    }

    /**
     * Generates a clear, beautifully structured plaintext summary for the report card.
     */
    public String toReportText() {
        StringBuilder sb = new StringBuilder();
        sb.append("================================================================\n");
        sb.append("              WAFER DEFECT ANALYSIS REPORT\n");
        sb.append("================================================================\n\n");
        sb.append(String.format(Locale.US, "WAFER IDENTIFIER : %s\n", waferId));
        sb.append(String.format(Locale.US, "INSPECTION TIME  : %s\n", timestamp));
        sb.append(String.format(Locale.US, "ANOMALY STATUS   : %s\n", getStatusDescription()));
        sb.append(String.format(Locale.US, "PREDICTED CLASS  : %s\n", predictedClass));
        sb.append(String.format(Locale.US, "%-17s: %.2f%%\n", scoreLabel.toUpperCase(Locale.US), confidenceOrScore * 100.0));
        sb.append(String.format(Locale.US, "MODEL MODE       : %s\n\n", modelMode));

        sb.append("---------------- DIE COUNT & YIELD METRICS --------------------\n");
        sb.append(String.format(Locale.US, "TOTAL VALID DIES : %,d\n", totalValidDieCount));
        sb.append(String.format(Locale.US, "GOOD DIES (PASS) : %,d (%.2f%%)\n", getGoodDieCount(), getYieldPercentage()));
        sb.append(String.format(Locale.US, "DEFECT DIES(FAIL): %,d (%.2f%%)\n", defectiveDieCount, getDefectPercentage()));
        sb.append(String.format(Locale.US, "INSPECTION SPEED : %.2f ms (monotonic high-resolution timer)\n\n", processingMs));

        if (referenceId != null && !referenceId.isEmpty()) {
            sb.append(String.format(Locale.US, "CLOSEST EXEMPLAR : %s\n", referenceId));
        }

        if (!classScores.isEmpty()) {
            sb.append("---------------- CLASS SCORE DISTRIBUTION ---------------------\n");
            for (Map.Entry<String, Double> entry : classScores.entrySet()) {
                String bar = makeProgressBar(entry.getValue(), 20);
                sb.append(String.format(Locale.US, "%-12s : %6.2f%%  %s\n", entry.getKey(), entry.getValue() * 100.0, bar));
            }
            sb.append("\n");
        }

        sb.append("---------------- METHOD & ACADEMIC AUDIT NOTE -----------------\n");
        sb.append(methodExplanation).append("\n");
        sb.append("================================================================\n");
        return sb.toString();
    }

    private String makeProgressBar(double fraction, int width) {
        int filled = (int) Math.round(Math.max(0.0, Math.min(1.0, fraction)) * width);
        StringBuilder b = new StringBuilder("[");
        for (int i = 0; i < width; i++) {
            b.append(i < filled ? '#' : '.');
        }
        b.append("]");
        return b.toString();
    }

    public static class Builder {
        private String waferId = "WAFER";
        private String predictedClass = "None";
        private boolean anomalous = false;
        private double confidenceOrScore = 0.0;
        private String scoreLabel = "Score";
        private Map<String, Double> classScores = new LinkedHashMap<>();
        private int defectiveDieCount = 0;
        private int totalValidDieCount = 0;
        private double processingMs = 0.0;
        private String modelMode = "DEMO";
        private String methodExplanation = "";
        private String referenceId = null;
        private String timestamp = null;
        private Map<String, Object> hardwareMetrics = new LinkedHashMap<>();

        public Builder waferId(String id) { this.waferId = id; return this; }
        public Builder predictedClass(String c) { this.predictedClass = c; return this; }
        public Builder anomalous(boolean a) { this.anomalous = a; return this; }
        public Builder confidenceOrScore(double s) { this.confidenceOrScore = s; return this; }
        public Builder scoreLabel(String l) { this.scoreLabel = l; return this; }
        public Builder classScores(Map<String, Double> cs) { this.classScores = cs; return this; }
        public Builder defectiveDieCount(int c) { this.defectiveDieCount = c; return this; }
        public Builder totalValidDieCount(int c) { this.totalValidDieCount = c; return this; }
        public Builder processingMs(double ms) { this.processingMs = ms; return this; }
        public Builder modelMode(String m) { this.modelMode = m; return this; }
        public Builder methodExplanation(String exp) { this.methodExplanation = exp; return this; }
        public Builder referenceId(String r) { this.referenceId = r; return this; }
        public Builder timestamp(String ts) { this.timestamp = ts; return this; }
        public Builder hardwareMetrics(Map<String, Object> hm) { this.hardwareMetrics = hm; return this; }

        public PredictionResult build() {
            return new PredictionResult(this);
        }
    }
}

