package waferscope.datastructures;

import java.util.Comparator;
import java.util.Locale;

/**
 * Course 1: Topic 4 - Searching and Sorting.
 * High-performance QuickSort algorithm (O(n log n)) and Binary Search (O(log n)).
 * Directly wired to the live lot inspection dashboard.
 */
public class WaferSearchSort {

    public static class WaferRecord {
        public String waferId;
        public String label;
        public int defectCount;
        public int validCount;
        public double defectDensity;
        public double yieldPercentage;

        public WaferRecord(String id, String label, int defects, int valid, double density, double yield) {
            this.waferId = id;
            this.label = label;
            this.defectCount = defects;
            this.validCount = valid;
            this.defectDensity = density;
            this.yieldPercentage = yield;
        }

        @Override
        public String toString() {
            return String.format(Locale.US, "%-22s | %-10s | Defects: %3d | Density: %5.1f%% | Yield: %5.1f%%",
                waferId, label, defectCount, defectDensity, yieldPercentage);
        }
    }

    public static class SortReport {
        public double latencyMs;
        public int comparisons;
        public int swaps;
        public String sortCriteria;

        public SortReport(double ms, int comps, int swps, String crit) {
            this.latencyMs = ms;
            this.comparisons = comps;
            this.swaps = swps;
            this.sortCriteria = crit;
        }
    }

    // Binary Search on sorted wafer IDs (O(log n))
    public static int binarySearch(WaferRecord[] arr, String targetId) {
        if (arr == null || arr.length == 0 || targetId == null) return -1;
        int low = 0;
        int high = arr.length - 1;

        while (low <= high) {
            int mid = low + (high - low) / 2;
            int cmp = arr[mid].waferId.compareToIgnoreCase(targetId);

            if (cmp == 0) return mid;
            if (cmp < 0) low = mid + 1;
            else high = mid - 1;
        }
        return -1; // Not found
    }

    // QuickSort by Defect Density Ascending
    public static SortReport quickSortByDensity(WaferRecord[] arr) {
        if (arr == null || arr.length <= 1) return new SortReport(0, 0, 0, "Defect Density (Ascending)");
        int[] stats = new int[]{0, 0}; // [comparisons, swaps]
        long start = System.nanoTime();
        quickSort(arr, 0, arr.length - 1, Comparator.comparingDouble(w -> w.defectDensity), stats);
        double ms = (System.nanoTime() - start) / 1_000_000.0;
        return new SortReport(ms, stats[0], stats[1], "Defect Density (Ascending)");
    }

    // QuickSort by Defect Density Descending
    public static SortReport quickSortByDensityDesc(WaferRecord[] arr) {
        if (arr == null || arr.length <= 1) return new SortReport(0, 0, 0, "Defect Density (Descending)");
        int[] stats = new int[]{0, 0};
        long start = System.nanoTime();
        quickSort(arr, 0, arr.length - 1, (a, b) -> Double.compare(b.defectDensity, a.defectDensity), stats);
        double ms = (System.nanoTime() - start) / 1_000_000.0;
        return new SortReport(ms, stats[0], stats[1], "Defect Density (Descending)");
    }

    // QuickSort by Die Yield
    public static SortReport quickSortByYield(WaferRecord[] arr) {
        if (arr == null || arr.length <= 1) return new SortReport(0, 0, 0, "Functional Die Yield (Descending)");
        int[] stats = new int[]{0, 0};
        long start = System.nanoTime();
        quickSort(arr, 0, arr.length - 1, (a, b) -> Double.compare(b.yieldPercentage, a.yieldPercentage), stats);
        double ms = (System.nanoTime() - start) / 1_000_000.0;
        return new SortReport(ms, stats[0], stats[1], "Functional Die Yield (Descending)");
    }

    // QuickSort by Wafer ID
    public static SortReport quickSortById(WaferRecord[] arr) {
        if (arr == null || arr.length <= 1) return new SortReport(0, 0, 0, "Wafer SEMI ID (A-Z)");
        int[] stats = new int[]{0, 0};
        long start = System.nanoTime();
        quickSort(arr, 0, arr.length - 1, (a, b) -> a.waferId.compareToIgnoreCase(b.waferId), stats);
        double ms = (System.nanoTime() - start) / 1_000_000.0;
        return new SortReport(ms, stats[0], stats[1], "Wafer SEMI ID (A-Z)");
    }

    private static void quickSort(WaferRecord[] arr, int low, int high, Comparator<WaferRecord> comp, int[] stats) {
        if (low < high) {
            int pi = partition(arr, low, high, comp, stats);
            quickSort(arr, low, pi - 1, comp, stats);
            quickSort(arr, pi + 1, high, comp, stats);
        }
    }

    private static int partition(WaferRecord[] arr, int low, int high, Comparator<WaferRecord> comp, int[] stats) {
        WaferRecord pivot = arr[high];
        int i = low - 1;

        for (int j = low; j < high; j++) {
            stats[0]++; // comparison
            if (comp.compare(arr[j], pivot) <= 0) {
                i++;
                WaferRecord temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
                stats[1]++; // swap
            }
        }

        WaferRecord temp = arr[i + 1];
        arr[i + 1] = arr[high];
        arr[high] = temp;
        stats[1]++;
        return i + 1;
    }
}
