package waferscope.io;

import waferscope.model.WaferMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles dataset reading, schema validation, and file parsing.
 * Supports CSV datasets (bundled synthetic samples or custom exports)
 * and single wafer map text/csv files.
 */
public class DatasetLoader {

    public static class LoadReport {
        private final List<WaferMap> wafers = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();
        private int totalRowsProcessed = 0;
        private Path loadedFrom;

        public List<WaferMap> getWafers() { return wafers; }
        public List<String> getWarnings() { return warnings; }
        public int getTotalRowsProcessed() { return totalRowsProcessed; }
        public Path getLoadedFrom() { return loadedFrom; }
    }

    /**
     * Attempts to find the default dataset path in the workspace.
     */
    public static Path resolveDefaultDatasetPath() {
        Path[] candidates = new Path[]{
            Paths.get("dataset", "wafer_samples.csv"),
            Paths.get("Wafer_Defect_Swing_Antigravity_Starter", "dataset", "wafer_samples.csv"),
            Paths.get("..", "dataset", "wafer_samples.csv"),
            Paths.get("c:", "Users", "Janhavi", "Desktop", "nidhi", "Wafer_Defect_Swing_Antigravity_Starter", "dataset", "wafer_samples.csv")
        };

        for (Path p : candidates) {
            if (Files.isRegularFile(p)) {
                return p.toAbsolutePath().normalize();
            }
        }
        return Paths.get("dataset", "wafer_samples.csv");
    }

    /**
     * Loads wafer records from a CSV file.
     * Handles malformed rows by capturing warnings rather than aborting.
     */
    public static LoadReport loadCsv(Path path) throws IOException {
        if (!Files.exists(path)) {
            throw new IOException("File does not exist: " + path.toAbsolutePath());
        }

        LoadReport report = new LoadReport();
        report.loadedFrom = path;

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null) {
                throw new IOException("CSV file is empty: " + path.getFileName());
            }

            int idCol = 0;
            int labelCol = 1;
            int gridCol = 2;

            String[] headerParts = header.split(",");
            for (int i = 0; i < headerParts.length; i++) {
                String h = headerParts[i].trim().toLowerCase();
                if (h.contains("grid") || h.contains("pipe") || h.contains("matrix") || h.equals("map")) {
                    gridCol = i;
                } else if (h.contains("label") || h.contains("class") || h.contains("type")) {
                    labelCol = i;
                } else if (h.contains("wafer_id") || h.equals("id") || h.startsWith("id") || h.contains("wafer")) {
                    idCol = i;
                }
            }

            String line;
            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                report.totalRowsProcessed++;
                String[] parts = parseCsvLine(line);

                if (parts.length < 3) {
                    report.warnings.add("Line " + lineNum + ": Insufficient columns (" + parts.length + "), skipped.");
                    continue;
                }

                String id = (idCol < parts.length) ? parts[idCol].trim() : "W_" + lineNum;
                String label = (labelCol < parts.length) ? parts[labelCol].trim() : "Unknown";
                String gridStr = (gridCol < parts.length) ? parts[gridCol].trim() : parts[parts.length - 1].trim();

                try {
                    WaferMap wm = WaferMap.fromPipeString(id, label, gridStr);
                    report.wafers.add(wm);
                } catch (Exception ex) {
                    report.warnings.add("Line " + lineNum + " (ID: " + id + "): Parse error: " + ex.getMessage());
                }
            }
        }

        return report;
    }

    /**
     * Loads a single wafer map from a text or CSV file.
     * Expects rows of 0, 1, and E (or comma/space separated values).
     */
    public static WaferMap loadSingleWaferFile(File file) throws IOException {
        if (!file.exists() || !file.isFile()) {
            throw new IOException("Selected file does not exist: " + file.getAbsolutePath());
        }

        List<String> rawLines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        String name = file.getName();
        if (name.contains(".")) {
            name = name.substring(0, name.lastIndexOf('.'));
        }

        return WaferMap.fromLines(name, "Imported", rawLines);
    }

    /**
     * Minimal CSV splitter respecting basic quotes.
     */
    private static String[] parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString());
        return tokens.toArray(new String[0]);
    }
}
