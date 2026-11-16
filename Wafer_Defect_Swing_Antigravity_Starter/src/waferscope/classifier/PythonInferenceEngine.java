package waferscope.classifier;

import waferscope.model.PredictionResult;
import waferscope.model.WaferMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Executes AI model inference by delegating to the Python machine-learning pipeline.
 *
 * Adheres to:
 * - IPC isolation via ProcessBuilder
 * - Configurable Python executable and script paths (no hardcoded machine paths)
 * - Safe parameter passing (no shell injection)
 * - Process timeout handling (prevents UI freeze)
 * - Pure Java JSON parser (zero external JAR dependencies)
 */
public class PythonInferenceEngine implements DefectClassifier {

    private String pythonExecutable = "python";
    private Path scriptPath = Paths.get("ml", "predict.py");
    private Path modelWeightsPath = Paths.get("ml", "models", "wafer_model.joblib");
    private int timeoutSeconds = 8;

    public PythonInferenceEngine() {
        resolveDefaultPaths();
    }

    public void setPythonExecutable(String pythonExecutable) {
        if (pythonExecutable != null && !pythonExecutable.trim().isEmpty()) {
            this.pythonExecutable = pythonExecutable.trim();
        }
    }

    public String getPythonExecutable() {
        return pythonExecutable;
    }

    public void setScriptPath(Path scriptPath) {
        if (scriptPath != null) {
            this.scriptPath = scriptPath;
        }
    }

    public Path getScriptPath() {
        return scriptPath;
    }

    public void setModelWeightsPath(Path modelWeightsPath) {
        if (modelWeightsPath != null) {
            this.modelWeightsPath = modelWeightsPath;
        }
    }

    public Path getModelWeightsPath() {
        return modelWeightsPath;
    }

    private void resolveDefaultPaths() {
        String envPy = System.getenv("PYTHON_EXE");
        if (envPy != null && !envPy.trim().isEmpty()) {
            pythonExecutable = envPy.trim();
        }

        Path[] scriptCandidates = new Path[]{
            Paths.get("ml", "predict.py"),
            Paths.get("Wafer_Defect_Swing_Antigravity_Starter", "ml", "predict.py"),
            Paths.get("..", "ml", "predict.py")
        };
        for (Path p : scriptCandidates) {
            if (Files.isRegularFile(p)) {
                scriptPath = p.toAbsolutePath().normalize();
                break;
            }
        }

        Path[] modelCandidates = new Path[]{
            Paths.get("ml", "models", "wafer_model.joblib"),
            Paths.get("Wafer_Defect_Swing_Antigravity_Starter", "ml", "models", "wafer_model.joblib"),
            Paths.get("..", "ml", "models", "wafer_model.joblib"),
            Paths.get("ml", "models", "wafer_model.pt"),
            Paths.get("Wafer_Defect_Swing_Antigravity_Starter", "ml", "models", "wafer_model.pt")
        };
        for (Path p : modelCandidates) {
            if (Files.isRegularFile(p)) {
                modelWeightsPath = p.toAbsolutePath().normalize();
                break;
            }
        }
    }

    @Override
    public String getName() {
        return "Python Machine Learning Inference Engine (AI / CNN)";
    }

    @Override
    public String getModelMode() {
        return "TRAINED";
    }

    @Override
    public boolean isAvailable() {
        return Files.isRegularFile(scriptPath);
    }

    public boolean hasTrainedModelWeights() {
        return Files.isRegularFile(modelWeightsPath);
    }

    @Override
    public String getStatusDescription() {
        if (!Files.isRegularFile(scriptPath)) {
            return "Prediction script not found at " + scriptPath.toString() + ". Use DEMO mode or configure ML pipeline.";
        }
        if (!Files.isRegularFile(modelWeightsPath)) {
            return "Python script available, but trained model weights not yet generated. Run 'python ml/train.py' first.";
        }
        return "Ready. Connected to Python ML model (" + modelWeightsPath.getFileName() + ").";
    }

    @Override
    public PredictionResult classify(WaferMap wafer) throws Exception {
        if (wafer == null) {
            throw new IllegalArgumentException("Cannot classify null wafer.");
        }
        if (!Files.isRegularFile(scriptPath)) {
            throw new IllegalStateException("Python prediction script not found at: " + scriptPath.toAbsolutePath());
        }

        long startNs = System.nanoTime();

        // Prepare CLI command arguments safely
        List<String> command = new ArrayList<>();
        command.add(pythonExecutable);
        command.add(scriptPath.toAbsolutePath().toString());
        command.add("--pipe");
        command.add(wafer.toPipeString());
        command.add("--wafer-id");
        command.add(wafer.getId());

        if (Files.isRegularFile(modelWeightsPath)) {
            command.add("--model-path");
            command.add(modelWeightsPath.toAbsolutePath().toString());
        }

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(false);

        Process process = pb.start();

        StringBuilder stdout = new StringBuilder();
        StringBuilder stderr = new StringBuilder();

        Thread outReader = new Thread(() -> {
            try (BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = r.readLine()) != null) {
                    stdout.append(line).append("\n");
                }
            } catch (Exception ignored) {}
        });

        Thread errReader = new Thread(() -> {
            try (BufferedReader r = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = r.readLine()) != null) {
                    stderr.append(line).append("\n");
                }
            } catch (Exception ignored) {}
        });

        outReader.start();
        errReader.start();

        boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("Python inference timed out after " + timeoutSeconds + " seconds.");
        }

        outReader.join(1000);
        errReader.join(1000);

        int exitCode = process.exitValue();
        if (exitCode != 0) {
            throw new RuntimeException("Python inference failed with exit code " + exitCode + ": " + stderr.toString().trim());
        }

        long elapsedNs = System.nanoTime() - startNs;
        double elapsedMs = elapsedNs / 1_000_000.0;

        String jsonText = stdout.toString().trim();
        return parseJsonResult(jsonText, wafer, elapsedMs);
    }

    /**
     * Parses the structured JSON returned by ml/predict.py without external library dependencies.
     */
    private PredictionResult parseJsonResult(String json, WaferMap wafer, double fallbackLatencyMs) {
        // Strip markdown blocks if any
        if (json.startsWith("```json")) {
            json = json.substring(7);
        }
        if (json.endsWith("```")) {
            json = json.substring(0, json.length() - 3);
        }
        json = json.trim();

        String waferId = extractJsonString(json, "wafer_id", wafer.getId());
        String predictedClass = extractJsonString(json, "predicted_class", "Unknown");
        boolean isAnomalous = extractJsonBoolean(json, "is_anomalous", !"None".equalsIgnoreCase(predictedClass));
        double confidence = extractJsonDouble(json, "confidence", 0.0);
        if (confidence == 0.0) {
            confidence = extractJsonDouble(json, "score", 0.0);
        }
        String modelMode = extractJsonString(json, "model_mode", "TRAINED");
        double processingMs = extractJsonDouble(json, "processing_ms", fallbackLatencyMs);
        int defectiveCount = extractJsonInt(json, "defective_die_count", wafer.getDefectiveCount());
        int totalValid = extractJsonInt(json, "total_valid_die_count", wafer.getValidCount());

        Map<String, Double> scores = extractJsonScores(json);

        String explanation =
            "- CLASSIFIER TYPE : Python AI / Deep Learning Subprocess Pipeline.\n" +
            "- MODEL MODE     : " + modelMode + "\n" +
            "- PREDICTION     : Evaluated via " + scriptPath.getFileName() + "\n" +
            "- WEIGHTS        : " + (Files.isRegularFile(modelWeightsPath) ? modelWeightsPath.getFileName().toString() : "Trained feature model") + "\n" +
            "- LATENCY BREAKDOWN:\n" +
            "  - End-to-end IPC Latency: " + String.format(Locale.US, "%.2f ms\n", fallbackLatencyMs) +
            "  - Python Internal Inference: " + String.format(Locale.US, "%.2f ms", processingMs);

        Map<String, Object> hw = new LinkedHashMap<>();
        hw.put("python_exe", pythonExecutable);
        hw.put("script_path", scriptPath.toString());
        hw.put("ipc_roundtrip_ms", fallbackLatencyMs);

        return new PredictionResult.Builder()
            .waferId(waferId)
            .predictedClass(predictedClass)
            .anomalous(isAnomalous)
            .confidenceOrScore(confidence)
            .scoreLabel("AI Model Confidence")
            .classScores(scores)
            .defectiveDieCount(defectiveCount)
            .totalValidDieCount(totalValid)
            .processingMs(fallbackLatencyMs)
            .modelMode(modelMode)
            .methodExplanation(explanation)
            .hardwareMetrics(hw)
            .build();
    }

    private String extractJsonString(String json, String key, String defaultVal) {
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx < 0) return defaultVal;
        int colon = json.indexOf(':', idx + pattern.length());
        if (colon < 0) return defaultVal;
        int qStart = json.indexOf('\"', colon);
        if (qStart < 0) return defaultVal;
        int qEnd = json.indexOf('\"', qStart + 1);
        if (qEnd < 0) return defaultVal;
        return json.substring(qStart + 1, qEnd);
    }

    private boolean extractJsonBoolean(String json, String key, boolean defaultVal) {
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx < 0) return defaultVal;
        int colon = json.indexOf(':', idx + pattern.length());
        if (colon < 0) return defaultVal;
        String rest = json.substring(colon + 1).trim();
        if (rest.startsWith("true")) return true;
        if (rest.startsWith("false")) return false;
        return defaultVal;
    }

    private double extractJsonDouble(String json, String key, double defaultVal) {
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx < 0) return defaultVal;
        int colon = json.indexOf(':', idx + pattern.length());
        if (colon < 0) return defaultVal;
        int end = colon + 1;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.' || json.charAt(end) == '-' || json.charAt(end) == 'e' || json.charAt(end) == 'E' || Character.isWhitespace(json.charAt(end)))) {
            end++;
        }
        try {
            return Double.parseDouble(json.substring(colon + 1, end).trim());
        } catch (Exception ex) {
            return defaultVal;
        }
    }

    private int extractJsonInt(String json, String key, int defaultVal) {
        return (int) Math.round(extractJsonDouble(json, key, defaultVal));
    }

    private Map<String, Double> extractJsonScores(String json) {
        Map<String, Double> scores = new LinkedHashMap<>();
        int scoresIdx = json.indexOf("\"scores\"");
        if (scoresIdx < 0) return scores;
        int braceStart = json.indexOf('{', scoresIdx);
        if (braceStart < 0) return scores;
        int braceEnd = json.indexOf('}', braceStart);
        if (braceEnd < 0) return scores;

        String body = json.substring(braceStart + 1, braceEnd);
        String[] entries = body.split(",");
        for (String entry : entries) {
            String[] kv = entry.split(":");
            if (kv.length == 2) {
                String k = kv[0].replace("\"", "").trim();
                try {
                    double v = Double.parseDouble(kv[1].trim());
                    scores.put(k, v);
                } catch (Exception ignored) {}
            }
        }
        return scores;
    }
}
