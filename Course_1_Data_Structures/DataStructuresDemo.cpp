/**
 * ============================================================================
 * DATA STRUCTURES LABORATORY IN C++ (COURSE 1 - 2310214L)
 * Master Demonstration Program
 * 
 * 100% DYNAMIC - ZERO HARDCODED VALUES:
 * Directly synchronizes with the active wafer in active_wafer.txt and dataset CSV.
 * 
 * Demonstrates:
 * 1. Arrays: Dynamic heap allocation, insert, linear search, update, delete with shifting
 * 2. Singly Linked List: Dynamic pointer allocation (Node*), O(1) head/tail insert, traversal
 * 3. Stack and Queue: LIFO inspection rollback stack & FIFO wafer dispatch queue
 * 4. Searching and Sorting: QuickSort O(n log n) by defect density & Binary Search O(log n)
 * 5. Hashing Techniques: Hash table with chaining collision resolution & O(1) lookup
 * ============================================================================
 */

#include "wafer_datastructures.h"
#include <iostream>
#include <string>
#include <vector>
#include <iomanip>

using namespace WaferDS;

int main(int argc, char* argv[]) {
    // 1. DYNAMICALLY LOAD CURRENT ACTIVE WAFER (NO HARDCODED VALUES)
    ActiveWafer active = loadActiveWafer();
    std::vector<LotWafer> lotWafers = loadRealWafers();

    // Check CLI flags for GUI IPC Bridge
    if (argc > 1) {
        std::string mode = argv[1];

        // IPC MODE: JSON SNAPSHOT FOR JAVA GUI BRIDGE
        if (mode == "--json") {
            std::cout << "{\n";
            std::cout << "  \"waferId\": \"" << active.waferId << "\",\n";
            std::cout << "  \"defectLabel\": \"" << active.defectLabel << "\",\n";
            std::cout << "  \"defectCount\": " << active.defectCount << ",\n";
            std::cout << "  \"validCount\": " << active.validCount << ",\n";
            std::cout << "  \"defectDensity\": " << std::fixed << std::setprecision(1) << active.defectDensity << ",\n";
            std::cout << "  \"yieldPercentage\": " << std::fixed << std::setprecision(1) << active.yieldPercentage << ",\n";
            std::cout << "  \"confidence\": " << std::fixed << std::setprecision(1) << active.confidence << ",\n";
            std::cout << "  \"status\": \"" << active.status << "\",\n";
            std::cout << "  \"lotCount\": " << lotWafers.size() << "\n";
            std::cout << "}\n";
            return 0;
        }

        // IPC MODE: QUICKSORT EXECUTION
        if (mode == "--quicksort") {
            bool asc = true;
            if (argc > 2 && std::string(argv[2]) == "desc") {
                asc = false;
            }
            std::vector<WaferSearchSort::WaferRecord> records;
            for (const auto& w : lotWafers) {
                records.push_back({w.id, w.label, w.density, w.yield});
            }
            auto telem = WaferSearchSort::quickSort(records, asc);
            std::cout << "QUICKSORT_RESULT\n";
            std::cout << "LATENCY_US=" << telem.latencyMicroseconds << "\n";
            std::cout << "COMPARISONS=" << telem.comparisons << "\n";
            std::cout << "SWAPS=" << telem.swaps << "\n";
            std::cout << "COUNT=" << records.size() << "\n";
            for (size_t i = 0; i < std::min((size_t)8, records.size()); ++i) {
                std::cout << "ITEM=" << records[i].waferId << "," << records[i].defectLabel << "," << records[i].defectDensity << "\n";
            }
            return 0;
        }

        // IPC MODE: BINARY SEARCH EXECUTION
        if (mode == "--binarysearch" && argc > 2) {
            std::string targetId = argv[2];
            std::vector<WaferSearchSort::WaferRecord> records;
            for (const auto& w : lotWafers) {
                records.push_back({w.id, w.label, w.density, w.yield});
            }
            // Sort by waferId for binary search
            std::sort(records.begin(), records.end(), [](const WaferSearchSort::WaferRecord& a, const WaferSearchSort::WaferRecord& b) {
                return a.waferId < b.waferId;
            });
            int idx = WaferSearchSort::binarySearch(records, targetId);
            std::cout << "BINARY_SEARCH_RESULT\n";
            std::cout << "TARGET=" << targetId << "\n";
            std::cout << "INDEX=" << idx << "\n";
            std::cout << "FOUND=" << (idx != -1 ? "true" : "false") << "\n";
            return 0;
        }
    }

    // DEFAULT MODE: COMPLETE 5-TOPIC MASTER DEMONSTRATION
    std::cout << "==========================================================================\n";
    std::cout << "             DATA STRUCTURES LABORATORY IN C++ (COURSE 1)\n";
    std::cout << "          AI-Based Semiconductor Wafer Defect Detection System\n";
    std::cout << "==========================================================================\n";
    std::cout << " ACTIVE WAFER LOADED: " << active.waferId << " [" << active.defectLabel << " Defect Pattern]\n";
    std::cout << " Defective Dies     : " << active.defectCount << " / " << active.validCount
              << " dies (Defect Density: " << std::fixed << std::setprecision(1) << active.defectDensity << "%)\n";
    std::cout << " Functional Yield   : " << active.yieldPercentage << "% | Status: " << active.status
              << " | Confidence: " << active.confidence << "%\n";
    std::cout << " Total Lot Wafers   : " << lotWafers.size() << " wafers dynamically loaded from dataset\n";
    std::cout << "==========================================================================\n";

    // -------------------------------------------------------------
    // TOPIC 1: ARRAYS (INSERT, SEARCH, UPDATE, DELETE, DISPLAY)
    // -------------------------------------------------------------
    std::cout << "\n>>> [TOPIC 1] ARRAYS: INSERT, SEARCH, UPDATE, DELETE, DISPLAY (C++ Heap Buffer)\n";
    WaferArrayOps arrayOps(6);

    // Dynamic insert active wafer
    arrayOps.insert(WaferArrayOps::WaferItem(active.waferId, active.defectLabel, active.defectCount, active.defectDensity));

    // Dynamic insert real sibling lot wafers
    int inserted = 1;
    for (const auto& w : lotWafers) {
        if (inserted >= 5) break;
        if (w.id != active.waferId) {
            arrayOps.insert(WaferArrayOps::WaferItem(w.id, w.label, w.defectCount, w.density));
            inserted++;
        }
    }
    arrayOps.display();

    std::cout << "\nTesting C++ Linear Search for Active Wafer (" << active.waferId << "):\n";
    int idx = arrayOps.search(active.waferId);
    std::cout << "Found active wafer '" << active.waferId << "' at array index: [" << idx << "]\n";

    std::cout << "\nTesting C++ Update Operation on Active Wafer:\n";
    arrayOps.update(active.waferId, active.defectLabel, active.defectCount + 1, active.defectDensity + 0.5);

    std::cout << "\nTesting C++ Delete Operation (removing slot 2 with element shifting):\n";
    if (lotWafers.size() > 1) {
        std::string toDelete = lotWafers[1].id;
        arrayOps.remove(toDelete);
        std::cout << "[Array Success] Deleted wafer " << toDelete << " with contiguous shifting.\n";
    }
    arrayOps.display();

    // -------------------------------------------------------------
    // TOPIC 2: SINGLY LINKED LIST (DYNAMIC HEAP MEMORY ALLOCATION)
    // -------------------------------------------------------------
    std::cout << "\n>>> [TOPIC 2] SINGLY LINKED LIST (DYNAMIC POINTER TRAVERSAL IN C++)\n";
    WaferLinkedList linkedList;

    // Dynamic insert active wafer at head
    linkedList.insertHead(active.waferId, active.defectLabel, active.defectDensity);

    // Append real sibling wafers at tail
    int count = 0;
    for (const auto& w : lotWafers) {
        if (count >= 4) break;
        if (w.id != active.waferId) {
            linkedList.insertTail(w.id, w.label, w.density);
            count++;
        }
    }
    linkedList.display();
    std::cout << "Traversing dynamic linked nodes containing active wafer: SUCCESS (O(n) pointer traversal).\n";

    // -------------------------------------------------------------
    // TOPIC 3: STACK AND QUEUE (SCENARIO PROCESSING)
    // -------------------------------------------------------------
    std::cout << "\n>>> [TOPIC 3] SCENARIO PROCESSING: STACK AND QUEUE (C++ Heap Buffers)\n";
    WaferStackQueue::InspectionStack stack(5);
    stack.push(active.waferId);
    for (size_t i = 0; i < std::min((size_t)3, lotWafers.size()); ++i) {
        if (lotWafers[i].id != active.waferId) {
            stack.push(lotWafers[i].id);
        }
    }
    stack.display();
    std::cout << "LIFO Pop (Inspection Audit Rollback): " << stack.pop() << "\n";
    stack.display();

    WaferStackQueue::LotQueue queue(5);
    queue.enqueue(active.waferId);
    for (size_t i = 0; i < std::min((size_t)3, lotWafers.size()); ++i) {
        if (lotWafers[i].id != active.waferId) {
            queue.enqueue(lotWafers[i].id);
        }
    }
    queue.display();
    std::cout << "FIFO Dequeue (Dispatched to Fab Inspection Chamber): " << queue.dequeue() << "\n";
    queue.display();

    // -------------------------------------------------------------
    // TOPIC 4: SEARCHING AND SORTING (QUICKSORT & BINARY SEARCH)
    // -------------------------------------------------------------
    std::cout << "\n>>> [TOPIC 4] SEARCHING AND SORTING (QUICKSORT & BINARY SEARCH IN C++)\n";
    std::vector<WaferSearchSort::WaferRecord> sortArr;
    sortArr.push_back({active.waferId, active.defectLabel, active.defectDensity, active.yieldPercentage});

    int sCount = 1;
    for (const auto& w : lotWafers) {
        if (sCount >= 8) break;
        if (w.id != active.waferId) {
            sortArr.push_back({w.id, w.label, w.density, w.yield});
            sCount++;
        }
    }

    WaferSearchSort::display(sortArr, "Unsorted Wafers from Active Lot (Real Data)");

    auto telem = WaferSearchSort::quickSort(sortArr, true);
    WaferSearchSort::display(sortArr, "QuickSorted Wafers (Ascending Defect Density - O(n log n))");
    std::cout << "QuickSort Latency    : " << telem.latencyMicroseconds << " microseconds\n";
    std::cout << "QuickSort Comparisons: " << telem.comparisons << " comparisons\n";
    std::cout << "QuickSort Swaps      : " << telem.swaps << " swaps\n";

    // Binary Search on Wafer IDs
    std::sort(sortArr.begin(), sortArr.end(), [](const WaferSearchSort::WaferRecord& a, const WaferSearchSort::WaferRecord& b) {
        return a.waferId < b.waferId;
    });

    int bIdx = WaferSearchSort::binarySearch(sortArr, active.waferId);
    std::cout << "\nBinary Search O(log n) for Active Wafer '" << active.waferId << "': Found at sorted index [" << bIdx << "]\n";

    // -------------------------------------------------------------
    // TOPIC 5: HASHING TECHNIQUES WITH COLLISION RESOLUTION
    // -------------------------------------------------------------
    std::cout << "\n>>> [TOPIC 5] HASHING TECHNIQUES (DIRECT ADDRESSING & CHAINING IN C++)\n";
    WaferHashTable hashTable(5);

    // Put active wafer
    hashTable.put(active.waferId, active.defectLabel + " Pattern [" + std::to_string((int)active.defectDensity) + "%]");

    // Put real sibling lot wafers
    for (size_t i = 0; i < std::min((size_t)6, lotWafers.size()); ++i) {
        if (lotWafers[i].id != active.waferId) {
            hashTable.put(lotWafers[i].id, lotWafers[i].label + " Pattern [" + std::to_string((int)lotWafers[i].density) + "%]");
        }
    }
    hashTable.display();

    std::cout << "Fast O(1) Hash Table Lookup for Active Wafer '" << active.waferId << "':\n";
    std::cout << " -> " << hashTable.get(active.waferId) << "\n";

    std::cout << "\n==========================================================================\n";
    std::cout << "   [SUCCESS] ALL 5 DATA STRUCTURE TOPICS PROCESSED IN C++ DYNAMICALLY!\n";
    std::cout << "==========================================================================\n";

    return 0;
}
