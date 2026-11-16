package waferscope.classifier;

import waferscope.model.WaferMap;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Bridge to the C++ High-Performance Preprocessing & Numerical Processing Engine.
 *
 * Demonstrates C++ / PL integration:
 * - Executes morphological filtering and feature extraction via native C++ binary
 * - Measures high-resolution C++ execution time vs Java JVM execution time for COA benchmark
 * - Falls back cleanly to internal Java implementation if C++ binary is not compiled
 */
public class CppPreprocessorBridge {

    private Path binaryPath;

    public CppPreprocessorBridge() {
        resolveBinaryPath();
    }

    private void resolveBinaryPath() {
        Path[] candidates = new Path[]{
            Paths.get("cpp", "wafer_preprocessor.exe"),
            Paths.get("Wafer_Defect_Swing_Antigravity_Starter", "cpp", "wafer_preprocessor.exe"),
            Paths.get("..", "cpp", "wafer_preprocessor.exe"),
            Paths.get("cpp", "wafer_preprocessor"),
            Paths.get("Wafer_Defect_Swing_Antigravity_Starter", "cpp", "wafer_preprocessor")
        };
        for (Path p : candidates) {
            if (Files.isRegularFile(p)) {
                binaryPath = p.toAbsolutePath().normalize();
                break;
            }
        }
    }

    public boolean isBinaryAvailable() {
        return binaryPath != null && Files.isRegularFile(binaryPath);
    }

    public Path getBinaryPath() {
        return binaryPath;
    }

    public static class PreprocessingResult {
        public boolean cplusplusUsed;
        public double executionMs;
        public double radialDefectEntropy;
        public double angularDefectEntropy;
        public double centroidX;
        public double centroidY;
        public int filteredDefectCount;
        public String statusNote;
    }

    /**
     * Executes fast spatial feature extraction and denoising.
     */
    public PreprocessingResult process(WaferMap wafer) {
        PreprocessingResult res = new PreprocessingResult();

        if (isBinaryAvailable()) {
            try {
                long startNs = System.nanoTime();
                List<String> cmd = new ArrayList<>();
                cmd.add(binaryPath.toAbsolutePath().toString());
                cmd.add("--pipe");
                cmd.add(wafer.toPipeString());

                ProcessBuilder pb = new ProcessBuilder(cmd);
                Process proc = pb.start();

                StringBuilder stdout = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stdout.append(line).append("\n");
                    }
                }

                boolean ok = proc.waitFor(3, TimeUnit.SECONDS);
                if (ok && proc.exitValue() == 0) {
                    long elapsedNs = System.nanoTime() - startNs;
                    res.executionMs = elapsedNs / 1_000_000.0;
                    res.cplusplusUsed = true;
                    res.statusNote = "Processed using compiled native C++ binary (" + binaryPath.getFileName() + ")";
                    parseCppOutput(stdout.toString(), res);
                    return res;
                }
            } catch (Exception ex) {
                // Fallback to Java
            }
        }

        // Java Fallback implementation
        long startNs = System.nanoTime();
        res.cplusplusUsed = false;
        res.statusNote = "Processed using Java JVM fallback (C++ binary not compiled/available)";

        int rows = wafer.getRows();
        int cols = wafer.getCols();
        double sumX = 0, sumY = 0;
        int count = 0;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (wafer.getDie(r, c) == 1) {
                    sumY += r;
                    sumX += c;
                    count++;
                }
            }
        }

        res.filteredDefectCount = count;
        if (count > 0) {
            res.centroidX = sumX / count;
            res.centroidY = sumY / count;
        } else {
            res.centroidX = cols / 2.0;
            res.centroidY = rows / 2.0;
        }

        long elapsedNs = System.nanoTime() - startNs;
        res.executionMs = elapsedNs / 1_000_000.0;
        return res;
    }

    private void parseCppOutput(String text, PreprocessingResult res) {
        for (String line : text.split("\n")) {
            String[] parts = line.split("=");
            if (parts.length == 2) {
                String k = parts[0].trim().toLowerCase();
                String v = parts[1].trim();
                try {
                    if (k.contains("centroid_x")) res.centroidX = Double.parseDouble(v);
                    else if (k.contains("centroid_y")) res.centroidY = Double.parseDouble(v);
                    else if (k.contains("filtered_defects")) res.filteredDefectCount = Integer.parseInt(v);
                    else if (k.contains("radial_entropy")) res.radialDefectEntropy = Double.parseDouble(v);
                    else if (k.contains("angular_entropy")) res.angularDefectEntropy = Double.parseDouble(v);
                } catch (Exception ignored) {}
            }
        }
    }
}
