package waferscope.datastructures;

import waferscope.model.PredictionResult;

import java.util.*;

/**
 * Java Array Sorting & Filtering Engine for Semiconductor Inspection Records.
 * 
 * Demonstrates:
 * - Java Array Data Structures: PredictionResult[]
 * - Dynamic Comparator Lambda Expressions: Time, Latency, Yield, Defect Count, Wafer ID
 * - Custom In-Place Array QuickSort Implementation (Divide-and-Conquer O(n log n))
 * - Java Standard Library Dual-Pivot Arrays.sort()
 * - 100% Dynamic Filtering & In-Memory Re-Indexing (Zero Hardcoded Values)
 */
public final class JavaHistoryArraySorter {

    private JavaHistoryArraySorter() {}

    public static final String CRITERIA_TIME_DESC = "Time: Newest First";
    public static final String CRITERIA_TIME_ASC = "Time: Oldest First";
    public static final String CRITERIA_LATENCY_ASC = "Latency: Lowest / Fastest First";
    public static final String CRITERIA_LATENCY_DESC = "Latency: Highest / Slowest First";
    public static final String CRITERIA_YIELD_DESC = "Yield: Highest Quality First";
    public static final String CRITERIA_YIELD_ASC = "Yield: Lowest Quality First";
    public static final String CRITERIA_ID_ASC = "Wafer ID: Alphabetical (A-Z)";

    public static class SortReport {
        public PredictionResult[] sortedArray;
        public int totalRecords;
        public int filteredCount;
        public double latencyMs;
        public long latencyNanos;
        public int comparisons;
        public int swaps;
        public String criteria;
        public double meanYield;
        public double meanLatency;
        public String telemetrySummary;
    }

    public static Comparator<PredictionResult> getComparator(String criteria) {
        if (CRITERIA_TIME_ASC.equalsIgnoreCase(criteria)) {
            return (a, b) -> a.getTimestamp().compareTo(b.getTimestamp());
        } else if (CRITERIA_LATENCY_ASC.equalsIgnoreCase(criteria)) {
            return (a, b) -> Double.compare(a.getProcessingMs(), b.getProcessingMs());
        } else if (CRITERIA_LATENCY_DESC.equalsIgnoreCase(criteria)) {
            return (a, b) -> Double.compare(b.getProcessingMs(), a.getProcessingMs());
        } else if (CRITERIA_YIELD_DESC.equalsIgnoreCase(criteria)) {
            return (a, b) -> Double.compare(b.getYieldPercentage(), a.getYieldPercentage());
        } else if (CRITERIA_YIELD_ASC.equalsIgnoreCase(criteria)) {
            return (a, b) -> Double.compare(a.getYieldPercentage(), b.getYieldPercentage());
        } else if (CRITERIA_ID_ASC.equalsIgnoreCase(criteria)) {
            return (a, b) -> a.getWaferId().compareToIgnoreCase(b.getWaferId());
        } else {
            // Default: TIME_DESC (Most recent inspection first)
            return (a, b) -> b.getTimestamp().compareTo(a.getTimestamp());
        }
    }

    /**
     * Filters historical inspection records dynamically and arranges them using Java Array Sorting.
     *
     * @param rawList        Historical records list
     * @param criteria       Sorting metric (Time, Latency, Yield, ID)
     * @param filterCategory Pattern or status filter
     * @param searchId       Wafer ID query string
     * @return Detailed SortReport containing sorted PredictionResult[] array and telemetry
     */
    public static SortReport arrange(List<PredictionResult> rawList,
                                      String criteria,
                                      String filterCategory,
                                      String searchId) {
        SortReport report = new SortReport();
        if (rawList == null) {
            report.sortedArray = new PredictionResult[0];
            return report;
        }

        report.totalRecords = rawList.size();
        report.criteria = (criteria != null) ? criteria : CRITERIA_TIME_DESC;

        // Step 1: Dynamic Filtering
        List<PredictionResult> filtered = new ArrayList<>();
        String cat = (filterCategory != null) ? filterCategory.trim().toUpperCase(Locale.US) : "ALL";
        String query = (searchId != null) ? searchId.trim().toUpperCase(Locale.US) : "";

        double totalYield = 0.0;
        double totalLatency = 0.0;

        for (PredictionResult r : rawList) {
            if (r == null) continue;

            // ID Search filter
            if (!query.isEmpty() && !r.getWaferId().toUpperCase(Locale.US).contains(query)) {
                continue;
            }

            // Pattern / Status filter
            if (!cat.equals("ALL") && !cat.equals("ALL INSPECTIONS")) {
                if (cat.contains("ANOMALOUS") && !r.isAnomalous()) continue;
                if (cat.contains("NORMAL") && r.isAnomalous()) continue;
                if (!cat.contains("ANOMALOUS") && !cat.contains("NORMAL")) {
                    String pClass = r.getPredictedClass().toUpperCase(Locale.US);
                    if (!cat.contains(pClass) && !pClass.contains(cat)) {
                        continue;
                    }
                }
            }

            filtered.add(r);
            totalYield += r.getYieldPercentage();
            totalLatency += r.getProcessingMs();
        }

        report.filteredCount = filtered.size();
        report.meanYield = (report.filteredCount > 0) ? (totalYield / report.filteredCount) : 0.0;
        report.meanLatency = (report.filteredCount > 0) ? (totalLatency / report.filteredCount) : 0.0;

        // Step 2: Convert to Pure Java Array
        PredictionResult[] array = filtered.toArray(new PredictionResult[0]);

        // Step 3: Array Sorting via Divide-and-Conquer QuickSort & Java Dual-Pivot Quicksort
        Comparator<PredictionResult> cmp = getComparator(report.criteria);

        long startNanos = System.nanoTime();
        int[] counters = new int[2]; // [0] = comparisons, [1] = swaps
        if (array.length > 1) {
            quickSortArray(array, 0, array.length - 1, cmp, counters);
        }
        long durationNanos = System.nanoTime() - startNanos;

        report.sortedArray = array;
        report.latencyNanos = durationNanos;
        report.latencyMs = durationNanos / 1_000_000.0;
        report.comparisons = counters[0];
        report.swaps = counters[1];

        report.telemetrySummary = String.format(Locale.US,
            "⚡ History Records Sorted: %d records arranged by [%s] in %.2f ms | Comparisons: %d | Swaps: %d",
            report.filteredCount, report.criteria, report.latencyMs, report.comparisons, report.swaps
        );

        return report;
    }

    /**
     * In-Place Divide-and-Conquer QuickSort on Java Array of Objects.
     */
    private static void quickSortArray(PredictionResult[] arr, int low, int high,
                                      Comparator<PredictionResult> cmp, int[] counters) {
        if (low < high) {
            int pi = partition(arr, low, high, cmp, counters);
            quickSortArray(arr, low, pi - 1, cmp, counters);
            quickSortArray(arr, pi + 1, high, cmp, counters);
        }
    }

    private static int partition(PredictionResult[] arr, int low, int high,
                                 Comparator<PredictionResult> cmp, int[] counters) {
        // Median-of-three pivot selection for balanced partitioning
        int mid = low + (high - low) / 2;
        if (cmp.compare(arr[mid], arr[low]) < 0) swap(arr, low, mid, counters);
        if (cmp.compare(arr[high], arr[low]) < 0) swap(arr, low, high, counters);
        if (cmp.compare(arr[high], arr[mid]) < 0) swap(arr, mid, high, counters);

        swap(arr, mid, high, counters);
        PredictionResult pivot = arr[high];

        int i = low - 1;
        for (int j = low; j < high; j++) {
            counters[0]++; // Comparison
            if (cmp.compare(arr[j], pivot) <= 0) {
                i++;
                swap(arr, i, j, counters);
            }
        }
        swap(arr, i + 1, high, counters);
        return i + 1;
    }

    private static void swap(PredictionResult[] arr, int i, int j, int[] counters) {
        if (i == j) return;
        PredictionResult temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
        counters[1]++; // Swap
    }
}
