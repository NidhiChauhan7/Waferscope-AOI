/**
 * Course 1: Programming Lab (2310214L)
 * Master Demonstration for Examination Panel
 *
 * Demonstrates:
 * CO1: Arrays (Insert, Delete, Update, Search, Display)
 * CO2: Linked List (Dynamic nodes, insertion, traversal)
 * CO3: Stack & Queue (LIFO Undo rollback, FIFO Lot ingestion queue)
 * CO4: Searching & Sorting (Binary Search, QuickSort ranking)
 * CO5: Hashing (Hash table with chaining for O(1) lookup)
 */
public class ProgrammingLabDemo {

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("   COURSE 1: PROGRAMMING LAB (2310214L) - DEMONSTRATION");
        System.out.println("   MIT Academy of Engineering, Alandi, Pune | Dept of CE");
        System.out.println("==========================================================================\n");

        // -------------------------------------------------------------
        // CO1: ARRAYS
        // -------------------------------------------------------------
        System.out.println(">>> [CO1] BUILD THE FOUNDATION - ARRAYS");
        WaferArrayOps arrayOps = new WaferArrayOps(5);
        arrayOps.insert(new WaferArrayOps.WaferItem("TSMC-L46393-W001-CTR", "Center", 19, 9.8));
        arrayOps.insert(new WaferArrayOps.WaferItem("TSMC-L46393-W013-DNT", "Donut", 28, 14.5));
        arrayOps.insert(new WaferArrayOps.WaferItem("TSMC-L46393-W025-ELC", "Edge-Loc", 15, 7.8));
        arrayOps.display();

        System.out.println("\nTesting Search:");
        int idx = arrayOps.search("TSMC-L46393-W013-DNT");
        System.out.println("Found 'TSMC-L46393-W013-DNT' at index: " + idx);

        System.out.println("\nTesting Update:");
        arrayOps.update("TSMC-L46393-W001-CTR", "Center", 24, 12.4);

        System.out.println("\nTesting Delete:");
        arrayOps.delete("TSMC-L46393-W025-ELC");
        arrayOps.display();

        // -------------------------------------------------------------
        // CO2: LINKED LIST
        // -------------------------------------------------------------
        System.out.println("\n>>> [CO2] EXTEND THE APPLICATION - LINKED LIST");
        WaferLinkedList linkedList = new WaferLinkedList();
        linkedList.insertTail("TSMC-L46393-W001-CTR", "Center", 9.8);
        linkedList.insertTail("TSMC-L46393-W013-DNT", "Donut", 14.5);
        linkedList.insertHead("TSMC-L46393-W037-ERG", "Edge-Ring", 22.1); // Insert at head O(1)
        linkedList.display();
        System.out.println("Deleting W013-DNT:");
        linkedList.delete("TSMC-L46393-W013-DNT");
        linkedList.display();

        // -------------------------------------------------------------
        // CO3: STACK & QUEUE
        // -------------------------------------------------------------
        System.out.println("\n>>> [CO3] SCENARIO PROCESSING - STACK & QUEUE");
        WaferStackQueue.InspectionStack stack = new WaferStackQueue.InspectionStack(4);
        stack.push("TSMC-L46393-W001-CTR");
        stack.push("TSMC-L46393-W002-CTR");
        stack.push("TSMC-L46393-W003-CTR");
        stack.display();
        System.out.println("Popped (Rollback): " + stack.pop());
        stack.display();

        WaferStackQueue.LotQueue queue = new WaferStackQueue.LotQueue(4);
        queue.enqueue("TSMC-L46393-W010-CTR");
        queue.enqueue("TSMC-L46393-W011-CTR");
        queue.enqueue("TSMC-L46393-W012-CTR");
        queue.display();
        System.out.println("Dequeued (Sent to Inspection): " + queue.dequeue());
        queue.display();

        // -------------------------------------------------------------
        // CO4: SEARCHING & SORTING
        // -------------------------------------------------------------
        System.out.println("\n>>> [CO4] OPTIMIZE & COMPLETE - SEARCHING & SORTING");
        WaferSearchSort.WaferRecord[] sortArr = new WaferSearchSort.WaferRecord[]{
            new WaferSearchSort.WaferRecord("TSMC-L46393-W013-DNT", 18.5),
            new WaferSearchSort.WaferRecord("TSMC-L46393-W001-CTR", 9.8),
            new WaferSearchSort.WaferRecord("TSMC-L46393-W061-NFL", 85.2),
            new WaferSearchSort.WaferRecord("TSMC-L46393-W097-NON", 0.0),
            new WaferSearchSort.WaferRecord("TSMC-L46393-W085-SCR", 12.3)
        };
        WaferSearchSort.display(sortArr, "Unsorted Wafers by Defect Rate");
        WaferSearchSort.quickSort(sortArr, 0, sortArr.length - 1);
        WaferSearchSort.display(sortArr, "QuickSorted Wafers (Ascending Defect Density)");

        // Binary search on sorted IDs
        WaferSearchSort.WaferRecord[] idArr = new WaferSearchSort.WaferRecord[]{
            new WaferSearchSort.WaferRecord("TSMC-L46393-W001-CTR", 9.8),
            new WaferSearchSort.WaferRecord("TSMC-L46393-W013-DNT", 18.5),
            new WaferSearchSort.WaferRecord("TSMC-L46393-W061-NFL", 85.2),
            new WaferSearchSort.WaferRecord("TSMC-L46393-W085-SCR", 12.3),
            new WaferSearchSort.WaferRecord("TSMC-L46393-W097-NON", 0.0)
        };
        int bIdx = WaferSearchSort.binarySearch(idArr, "TSMC-L46393-W061-NFL");
        System.out.println("\nBinary Search for 'TSMC-L46393-W061-NFL': Found at index " + bIdx);

        // -------------------------------------------------------------
        // CO5: HASHING
        // -------------------------------------------------------------
        System.out.println("\n>>> [CO5] HASHING TECHNIQUES WITH COLLISION RESOLUTION");
        WaferHashTable hashTable = new WaferHashTable(5);
        hashTable.put("TSMC-L46393-W001", "Center Pattern [9.8%]");
        hashTable.put("TSMC-L46393-W013", "Donut Pattern [14.5%]");
        hashTable.put("TSMC-L46393-W025", "Edge-Loc Pattern [7.8%]");
        hashTable.put("TSMC-L46393-W037", "Edge-Ring Pattern [22.1%]");
        hashTable.put("TSMC-L46393-W097", "Normal Pass [0.0%]");
        hashTable.display();

        System.out.println("Fast O(1) Lookup for 'TSMC-L46393-W013': " + hashTable.get("TSMC-L46393-W013"));

        System.out.println("\n==========================================================================");
        System.out.println("   [SUCCESS] ALL PROGRAMMING LAB (DSA) OUTCOMES DEMONSTRATED!");
        System.out.println("==========================================================================");
    }
}

