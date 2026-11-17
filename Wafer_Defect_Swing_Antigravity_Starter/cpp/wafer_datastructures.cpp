#include "wafer_datastructures.h"
#include <iostream>
#include <string>
#include <vector>
#include <iomanip>

using namespace WaferDS;

int main(int argc, char* argv[]) {
    ActiveWafer active = loadActiveWafer();
    std::vector<LotWafer> lotWafers = loadRealWafers();

    if (argc > 1) {
        std::string mode = argv[1];

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
            for (size_t i = 0; i < records.size(); ++i) {
                std::cout << "ITEM=" << records[i].waferId << "," << records[i].defectLabel << "," << records[i].defectDensity << "\n";
            }
            return 0;
        }

        if (mode == "--binarysearch" && argc > 2) {
            std::string targetId = argv[2];
            std::vector<WaferSearchSort::WaferRecord> records;
            for (const auto& w : lotWafers) {
                records.push_back({w.id, w.label, w.density, w.yield});
            }
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

    std::cout << "WaferScope C++ Data Structures Engine Active.\n";
    std::cout << "Active Wafer: " << active.waferId << " (" << active.defectLabel << ")\n";
    std::cout << "Loaded lot count: " << lotWafers.size() << "\n";
    return 0;
}
