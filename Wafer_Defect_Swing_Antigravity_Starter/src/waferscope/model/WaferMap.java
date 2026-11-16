package waferscope.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a semiconductor wafer map containing a 2D ternary grid of dies.
 * Encapsulates wafer geometry, die status metrics, spatial defect distribution,
 * and local density calculations for computer graphics heatmaps.
 */
public class WaferMap {
    private final String id;
    private final String groundTruthLabel;
    private final int[][] grid;
    private final int rows;
    private final int cols;

    private int goodCount;
    private int defectiveCount;
    private int outsideCount;
    private double defectDensity;

    public WaferMap(String id, String groundTruthLabel, int[][] grid) {
        if (grid == null || grid.length == 0 || grid[0].length == 0) {
            throw new IllegalArgumentException("Grid must be a non-empty 2D array");
        }
        this.id = (id == null || id.trim().isEmpty()) ? "WAFER_UNKNOWN" : id.trim();
        this.groundTruthLabel = (groundTruthLabel == null || groundTruthLabel.trim().isEmpty()) ? "Unknown" : groundTruthLabel.trim();
        this.rows = grid.length;
        this.cols = grid[0].length;
        this.grid = new int[rows][cols];

        for (int r = 0; r < rows; r++) {
            if (grid[r].length != cols) {
                throw new IllegalArgumentException("Irregular grid: row " + r + " has length " + grid[r].length + ", expected " + cols);
            }
            System.arraycopy(grid[r], 0, this.grid[r], 0, cols);
        }

        recomputeMetrics();
    }

    private void recomputeMetrics() {
        goodCount = 0;
        defectiveCount = 0;
        outsideCount = 0;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int val = grid[r][c];
                if (val < 0) {
                    outsideCount++;
                } else if (val == 1) {
                    defectiveCount++;
                } else {
                    goodCount++;
                }
            }
        }

        int validDies = goodCount + defectiveCount;
        defectDensity = (validDies > 0) ? ((double) defectiveCount / validDies) : 0.0;
    }

    public String getId() {
        return id;
    }

    public String getGroundTruthLabel() {
        return groundTruthLabel;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getDie(int r, int c) {
        if (r < 0 || r >= rows || c < 0 || c >= cols) {
            return -1;
        }
        return grid[r][c];
    }

    public DieStatus getDieStatus(int r, int c) {
        return DieStatus.fromCode(getDie(r, c));
    }

    public int[][] getGridCopy() {
        int[][] copy = new int[rows][cols];
        for (int r = 0; r < rows; r++) {
            System.arraycopy(grid[r], 0, copy[r], 0, cols);
        }
        return copy;
    }

    public int getGoodCount() {
        return goodCount;
    }

    public int getDefectiveCount() {
        return defectiveCount;
    }

    public int getOutsideCount() {
        return outsideCount;
    }

    public int getValidCount() {
        return goodCount + defectiveCount;
    }

    public double getDefectDensity() {
        return defectDensity;
    }

    public double getDefectPercentage() {
        return defectDensity * 100.0;
    }

    public double getYieldPercentage() {
        int valid = getValidCount();
        return valid > 0 ? ((double) goodCount / valid) * 100.0 : 0.0;
    }

    /**
     * Computes a normalized local defect density matrix (range 0.0 to 1.0)
     * using a circular kernel of specified radius.
     * Used by Computer Graphics renderer to draw smooth defect heatmaps.
     */
    public double[][] computeDensityMap(int radius) {
        double[][] density = new double[rows][cols];
        if (radius <= 0) radius = 1;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] < 0) {
                    density[r][c] = -1.0; // Outside wafer marker
                    continue;
                }

                int defectSum = 0;
                int validSum = 0;

                for (int dr = -radius; dr <= radius; dr++) {
                    int nr = r + dr;
                    if (nr < 0 || nr >= rows) continue;
                    for (int dc = -radius; dc <= radius; dc++) {
                        int nc = c + dc;
                        if (nc < 0 || nc >= cols) continue;
                        if (dr * dr + dc * dc <= radius * radius) {
                            int val = grid[nr][nc];
                            if (val >= 0) {
                                validSum++;
                                if (val == 1) defectSum++;
                            }
                        }
                    }
                }

                density[r][c] = (validSum > 0) ? ((double) defectSum / validSum) : 0.0;
            }
        }
        return density;
    }

    /**
     * Converts the wafer map into a pipe-separated string format.
     * E.g. "EEEE0000EEEE|EE001100EE|..."
     */
    public String toPipeString() {
        StringBuilder sb = new StringBuilder(rows * (cols + 1));
        for (int r = 0; r < rows; r++) {
            if (r > 0) sb.append('|');
            for (int c = 0; c < cols; c++) {
                int val = grid[r][c];
                sb.append(val < 0 ? 'E' : (val == 1 ? '1' : '0'));
            }
        }
        return sb.toString();
    }

    /**
     * Parses a pipe-separated string into a WaferMap.
     */
    public static WaferMap fromPipeString(String id, String label, String pipeText) {
        if (pipeText == null || pipeText.trim().isEmpty()) {
            throw new IllegalArgumentException("Empty wafer map string");
        }
        String[] lines = pipeText.trim().split("\\|");
        if (lines.length == 0) {
            throw new IllegalArgumentException("No rows found in wafer map string");
        }

        int rows = lines.length;
        int cols = lines[0].length();
        int[][] grid = new int[rows][cols];

        for (int r = 0; r < rows; r++) {
            String line = lines[r].trim();
            if (line.length() != cols) {
                throw new IllegalArgumentException(String.format("Row %d has %d characters, expected %d", r + 1, line.length(), cols));
            }
            for (int c = 0; c < cols; c++) {
                char ch = line.charAt(c);
                if (ch == 'E' || ch == 'e' || ch == '2') {
                    grid[r][c] = -1;
                } else if (ch == '1') {
                    grid[r][c] = 1;
                } else if (ch == '0') {
                    grid[r][c] = 0;
                } else {
                    throw new IllegalArgumentException("Invalid character '" + ch + "' at (" + r + "," + c + "). Allowed: 0, 1, E");
                }
            }
        }

        return new WaferMap(id, label, grid);
    }

    /**
     * Parses a list of lines (e.g. from a raw text file or 17x17 grid).
     */
    public static WaferMap fromLines(String id, String label, List<String> rawLines) {
        List<String> cleanLines = new ArrayList<>();
        for (String line : rawLines) {
            String s = line.trim().replace(",", "").replace(" ", "").replace("\t", "");
            if (!s.isEmpty()) {
                cleanLines.add(s);
            }
        }

        if (cleanLines.isEmpty()) {
            throw new IllegalArgumentException("File contains no valid matrix lines.");
        }

        int rows = cleanLines.size();
        int cols = cleanLines.get(0).length();
        int[][] grid = new int[rows][cols];

        for (int r = 0; r < rows; r++) {
            String line = cleanLines.get(r);
            if (line.length() != cols) {
                throw new IllegalArgumentException(String.format("Row %d has %d columns, expected %d", r + 1, line.length(), cols));
            }
            for (int c = 0; c < cols; c++) {
                char ch = line.charAt(c);
                if (ch == 'E' || ch == 'e' || ch == '2') {
                    grid[r][c] = -1;
                } else if (ch == '1') {
                    grid[r][c] = 1;
                } else if (ch == '0') {
                    grid[r][c] = 0;
                } else {
                    throw new IllegalArgumentException("Invalid character '" + ch + "' at (" + r + "," + c + "). Allowed: 0, 1, E");
                }
            }
        }

        return new WaferMap(id, label, grid);
    }
}

