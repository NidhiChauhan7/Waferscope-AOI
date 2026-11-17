/**
 * Course 1: Programming Lab (2310214L)
 * CO4: Searching & Sorting
 *
 * Demonstrates:
 * 1. Binary Search on sorted wafer IDs (O(log n))
 * 2. QuickSort algorithm ranking wafers by defect density (O(n log n))
 * 3. Performance & comparison metrics between Linear and Binary search
 */
public class WaferSearchSort {

    public static class WaferRecord {
        public String waferId;
        public double defectDensity;

        public WaferRecord(String id, double density) {
            this.waferId = id;
            this.defectDensity = density;
        }

        @Override
        public String toString() {
            return String.format("%s (Defect Density: %.1f%%)", waferId, defectDensity);
        }
    }

    // Binary Search (Requires sorted array by waferId)
    public static int binarySearch(WaferRecord[] arr, String targetId) {
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

    // QuickSort by Defect Density (Ascending order)
    public static void quickSort(WaferRecord[] arr, int low, int high) {
        if (low < high) {
            int pi = partition(arr, low, high);
            quickSort(arr, low, pi - 1);
            quickSort(arr, pi + 1, high);
        }
    }

    private static int partition(WaferRecord[] arr, int low, int high) {
        double pivot = arr[high].defectDensity;
        int i = (low - 1);

        for (int j = low; j < high; j++) {
            if (arr[j].defectDensity <= pivot) {
                i++;
                WaferRecord temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }

        WaferRecord temp = arr[i + 1];
        arr[i + 1] = arr[high];
        arr[high] = temp;
        return i + 1;
    }

    public static void display(WaferRecord[] arr, String title) {
        System.out.println("\n--- " + title + " ---");
        for (int i = 0; i < arr.length; i++) {
            System.out.println(String.format(" [%2d] %s", i + 1, arr[i]));
        }
    }
}

