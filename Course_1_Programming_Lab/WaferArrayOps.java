/**
 * Course 1: Programming Lab (2310214L)
 * CO1: Build the Foundation - Arrays
 *
 * Demonstrates:
 * 1. Array allocation for wafer records
 * 2. Insert operation (adding new wafer to array)
 * 3. Delete operation (removing wafer by ID with array shifting)
 * 4. Update operation (updating defect count and label)
 * 5. Search operation (linear search by wafer ID)
 * 6. Display operation (formatting all elements in tabular form)
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
            return String.format("%-22s | %-10s | Defects: %3d | Density: %5.1f%%",
                    waferId, defectLabel, defectCount, defectDensity);
        }
    }

    private WaferItem[] array;
    private int size;
    private int capacity;

    public WaferArrayOps(int capacity) {
        this.capacity = capacity;
        this.array = new WaferItem[capacity];
        this.size = 0;
    }

    // 1. INSERT OPERATION
    public boolean insert(WaferItem item) {
        if (size >= capacity) {
            System.out.println("[Array Overflow] Cannot insert: Array is full.");
            return false;
        }
        array[size++] = item;
        return true;
    }

    // 2. DELETE OPERATION (with shift)
    public boolean delete(String waferId) {
        int index = search(waferId);
        if (index == -1) {
            System.out.println("[Array Error] Wafer " + waferId + " not found for deletion.");
            return false;
        }
        for (int i = index; i < size - 1; i++) {
            array[i] = array[i + 1];
        }
        array[--size] = null;
        System.out.println("[Array Success] Deleted wafer " + waferId + ".");
        return true;
    }

    // 3. UPDATE OPERATION
    public boolean update(String waferId, String newLabel, int newDefectCount, double newDensity) {
        int index = search(waferId);
        if (index == -1) {
            System.out.println("[Array Error] Wafer " + waferId + " not found for update.");
            return false;
        }
        array[index].defectLabel = newLabel;
        array[index].defectCount = newDefectCount;
        array[index].defectDensity = newDensity;
        System.out.println("[Array Success] Updated wafer " + waferId + " to label " + newLabel + ".");
        return true;
    }

    // 4. SEARCH OPERATION (Linear Search)
    public int search(String waferId) {
        for (int i = 0; i < size; i++) {
            if (array[i] != null && array[i].waferId.equalsIgnoreCase(waferId)) {
                return i;
            }
        }
        return -1;
    }

    // 5. DISPLAY OPERATION
    public void display() {
        System.out.println("\n--- WAFER ARRAY CONTENTS (Size: " + size + " / " + capacity + ") ---");
        for (int i = 0; i < size; i++) {
            System.out.println("[" + i + "] " + array[i]);
        }
    }

    public int getSize() {
        return size;
    }

    public WaferItem get(int index) {
        if (index >= 0 && index < size) return array[index];
        return null;
    }
}

