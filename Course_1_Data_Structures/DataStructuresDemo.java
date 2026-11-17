import java.util.List;
import java.util.Locale;

/**
 * DATA STRUCTURES LABORATORY (2310214L)
 * AI-Based Semiconductor Wafer Defect Detection System
 *
 * ZERO HARDCODED VALUES:
 * Dynamically synchronizes with the active wafer selected in the GUI and the live wafer dataset.
 *
 * Demonstrates the 5 Core Data Structure Topics:
 * 1. Arrays: Dynamic insertion, linear search, update, deletion with shifting
 * 2. Singly Linked List: Dynamic memory allocation & pointer traversal
 * 3. Stack and Queue: LIFO inspection rollback stack & FIFO wafer dispatch queue
 * 4. Searching and Sorting: QuickSort by defect density & Binary Search on wafer IDs
 * 5. Hashing Techniques: Hash Table with chaining collision resolution & O(1) lookup
 */
public class DataStructuresDemo {

    public static void main(String[] args) {
        // 1. DYNAMICALLY LOAD CURRENT ACTIVE WAFER (NO HARDCODED VALUES)
        ActiveWaferState active = ActiveWaferState.loadActive();
        List<ActiveWaferState.LotWafer> lotWafers = ActiveWaferState.loadRealWafers();

        System.out.println("==========================================================================");
        System.out.println("                   DATA STRUCTURES LABORATORY DEMO");
        System.out.println("        AI-Based Semiconductor Wafer Inspection Data Structures");
        System.out.println("==========================================================================");
        System.out.println(String.format(Locale.US,
            " ACTIVE WAFER LOADED: %s [%s Defect Pattern]\n" +
            " Defective Dies     : %d / %d dies (Defect Density: %.1f%%)\n" +
            " Functional Yield   : %.1f%% | Status: %s | Confidence: %.1f%%\n" +
            " Total Lot Wafers   : %d wafers dynamically loaded from dataset",
            active.waferId, active.defectLabel,
            active.defectCount, active.validCount, active.defectDensity,
            active.yieldPercentage, active.status, active.confidence,
            lotWafers.size()));
        System.out.println("==========================================================================\n");

        // -------------------------------------------------------------
        // TOPIC 1: ARRAYS (INSERT, SEARCH, UPDATE, DELETE, DISPLAY)
        // -------------------------------------------------------------
        System.out.println(">>> [TOPIC 1] ARRAYS: INSERT, SEARCH, UPDATE, DELETE, DISPLAY");
        WaferArrayOps arrayOps = new WaferArrayOps(6);

        // Insert the active wafer as element 0
        arrayOps.insert(new WaferArrayOps.WaferItem(active.waferId, active.defectLabel, active.defectCount, active.defectDensity));

        // Insert real sibling wafers from dataset
        int inserted = 1;
        for (ActiveWaferState.LotWafer w : lotWafers) {
            if (inserted >= 5) break;
            if (!w.id.equalsIgnoreCase(active.waferId)) {
                arrayOps.insert(new WaferArrayOps.WaferItem(w.id, w.label, w.defectCount, w.density));
                inserted++;
            }
        }
        arrayOps.display();

        System.out.println("\nTesting Linear Search for Active Wafer (" + active.waferId + "):");
        int idx = arrayOps.search(active.waferId);
        System.out.println("Found active wafer '" + active.waferId + "' at array index: [" + idx + "]");

        System.out.println("\nTesting Update Operation on Active Wafer:");
        arrayOps.update(active.waferId, active.defectLabel, active.defectCount + 1, active.defectDensity + 0.5);

        System.out.println("\nTesting Delete Operation (removing slot 2 with shifting):");
        if (lotWafers.size() > 1) {
            String toDelete = lotWafers.get(1).id;
            arrayOps.delete(toDelete);
        }
        arrayOps.display();

        // -------------------------------------------------------------
        // TOPIC 2: SINGLY LINKED LIST (DYNAMIC MEMORY ALLOCATION)
        // -------------------------------------------------------------
        System.out.println("\n>>> [TOPIC 2] SINGLY LINKED LIST (DYNAMIC POINTER TRAVERSAL)");
        WaferLinkedList linkedList = new WaferLinkedList();

        // Dynamically insert active wafer at head
        linkedList.insertHead(active.waferId, active.defectLabel, active.defectDensity);

        // Append real sibling wafers
        int count = 0;
        for (ActiveWaferState.LotWafer w : lotWafers) {
            if (count >= 4) break;
            if (!w.id.equalsIgnoreCase(active.waferId)) {
                linkedList.insertTail(w.id, w.label, w.density);
                count++;
            }
        }
        linkedList.display();

        System.out.println("Traversing dynamic linked nodes containing active wafer: SUCCESS (O(n) traversal).");

        // -------------------------------------------------------------
        // TOPIC 3: STACK AND QUEUE (SCENARIO PROCESSING)
        // -------------------------------------------------------------
        System.out.println("\n>>> [TOPIC 3] SCENARIO PROCESSING: STACK AND QUEUE");
        WaferStackQueue.InspectionStack stack = new WaferStackQueue.InspectionStack(5);
        stack.push(active.waferId);
        for (int i = 0; i < Math.min(3, lotWafers.size()); i++) {
            if (!lotWafers.get(i).id.equalsIgnoreCase(active.waferId)) {
                stack.push(lotWafers.get(i).id);
            }
        }
        stack.display();
        System.out.println("LIFO Pop (Inspection Audit Rollback): " + stack.pop());
        stack.display();

        WaferStackQueue.LotQueue queue = new WaferStackQueue.LotQueue(5);
        queue.enqueue(active.waferId);
        for (int i = 0; i < Math.min(3, lotWafers.size()); i++) {
            if (!lotWafers.get(i).id.equalsIgnoreCase(active.waferId)) {
                queue.enqueue(lotWafers.get(i).id);
            }
        }
        queue.display();
        System.out.println("FIFO Dequeue (Dispatched to Fab Inspection Chamber): " + queue.dequeue());
        queue.display();

        // -------------------------------------------------------------
        // TOPIC 4: SEARCHING AND SORTING (QUICKSORT & BINARY SEARCH)
        // -------------------------------------------------------------
        System.out.println("\n>>> [TOPIC 4] SEARCHING AND SORTING (QUICKSORT & BINARY SEARCH)");
        int sortSampleCount = Math.min(8, lotWafers.size());
        WaferSearchSort.WaferRecord[] sortArr = new WaferSearchSort.WaferRecord[sortSampleCount];
        sortArr[0] = new WaferSearchSort.WaferRecord(active.waferId, active.defectDensity);

        int sIdx = 1;
        for (ActiveWaferState.LotWafer w : lotWafers) {
            if (sIdx >= sortSampleCount) break;
            if (!w.id.equalsIgnoreCase(active.waferId)) {
                sortArr[sIdx++] = new WaferSearchSort.WaferRecord(w.id, w.density);
            }
        }

        WaferSearchSort.display(sortArr, "Unsorted Wafers from Active Lot (Real Data)");

        long startQuickSort = System.nanoTime();
        WaferSearchSort.quickSort(sortArr, 0, sortArr.length - 1);
        long elapsedQuickSort = System.nanoTime() - startQuickSort;

        WaferSearchSort.display(sortArr, "QuickSorted Wafers (Ascending Defect Density - O(n log n))");
        System.out.println(String.format(Locale.US, "QuickSort Execution Latency: %.3f microseconds", elapsedQuickSort / 1000.0));

        // Binary Search for Active Wafer ID in sorted ID list
        WaferSearchSort.WaferRecord[] idSortArr = new WaferSearchSort.WaferRecord[sortSampleCount];
        System.arraycopy(sortArr, 0, idSortArr, 0, sortSampleCount);
        // Sort by waferId for binary search
        java.util.Arrays.sort(idSortArr, (a, b) -> a.waferId.compareToIgnoreCase(b.waferId));

        int bIdx = WaferSearchSort.binarySearch(idSortArr, active.waferId);
        System.out.println(String.format(Locale.US,
            "\nBinary Search O(log n) for Active Wafer '%s': Found at sorted index [%d]",
            active.waferId, bIdx));

        // -------------------------------------------------------------
        // TOPIC 5: HASHING TECHNIQUES WITH COLLISION RESOLUTION
        // -------------------------------------------------------------
        System.out.println("\n>>> [TOPIC 5] HASHING TECHNIQUES (DIRECT ADDRESSING & CHAINING)");
        WaferHashTable hashTable = new WaferHashTable(5);

        // Put active wafer into hash table
        hashTable.put(active.waferId, active.defectLabel + " Pattern [" + active.defectDensity + "%]");

        // Put real sibling wafers into hash table
        for (int i = 0; i < Math.min(6, lotWafers.size()); i++) {
            ActiveWaferState.LotWafer w = lotWafers.get(i);
            if (!w.id.equalsIgnoreCase(active.waferId)) {
                hashTable.put(w.id, w.label + " Pattern [" + String.format(Locale.US, "%.1f%%", w.density) + "]");
            }
        }
        hashTable.display();

        System.out.println("Fast O(1) Hash Table Lookup for Active Wafer '" + active.waferId + "':");
        System.out.println(" -> " + hashTable.get(active.waferId));

        System.out.println("\n==========================================================================");
        System.out.println("   [SUCCESS] ALL 5 DATA STRUCTURE TOPICS PROCESSED DYNAMICALLY!");
        System.out.println("==========================================================================");
    }
}
