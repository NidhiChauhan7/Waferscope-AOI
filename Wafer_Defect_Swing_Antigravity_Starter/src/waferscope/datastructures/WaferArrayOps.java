package waferscope.datastructures;

import java.util.Locale;

/**
 * Course 1: Topic 1 - Arrays.
 * Linear memory allocation, insert, delete with shifting, update, search, and formatted display.
 */
public class WaferArrayOps {

    public static class WaferItem {
        public String waferId;
        public String defectLabel;
        public int defectCount;
        public double defectDensity;

        public WaferItem(String id, String label, int defects, double density) {
            this.waferId = id;
            this.defectLabel = label;
            this.defectCount = defects;
            this.defectDensity = density;
        }

        @Override
        public String toString() {
            return String.format(Locale.US, "%-22s | %-10s | Defects: %3d | Density: %5.1f%%",
                waferId, defectLabel, defectCount, defectDensity);
        }
    }

    private final WaferItem[] array;
    private int size;
    private final int capacity;

    public WaferArrayOps(int capacity) {
        this.capacity = capacity;
        this.array = new WaferItem[capacity];
        this.size = 0;
    }

    public boolean insert(WaferItem item) {
        if (size >= capacity) return false;
        array[size++] = item;
        return true;
    }

    public boolean delete(String waferId) {
        int idx = search(waferId);
        if (idx == -1) return false;
        for (int i = idx; i < size - 1; i++) {
            array[i] = array[i + 1];
        }
        array[--size] = null;
        return true;
    }

    public boolean update(String waferId, String newLabel, int newDefects, double newDensity) {
        int idx = search(waferId);
        if (idx == -1) return false;
        array[idx].defectLabel = newLabel;
        array[idx].defectCount = newDefects;
        array[idx].defectDensity = newDensity;
        return true;
    }

    public int search(String waferId) {
        for (int i = 0; i < size; i++) {
            if (array[i] != null && array[i].waferId.equalsIgnoreCase(waferId)) {
                return i;
            }
        }
        return -1;
    }

    public int getSize() {
        return size;
    }

    public int getCapacity() {
        return capacity;
    }

    public WaferItem get(int index) {
        if (index < 0 || index >= size) return null;
        return array[index];
    }
}
