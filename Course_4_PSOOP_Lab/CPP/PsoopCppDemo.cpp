/**
 * Course 4: Problem Solving Using Object Oriented Programming (C++ - 2310261L)
 * Master Demonstration for C++ OOP Examination Panel
 *
 * Demonstrates:
 * CO1: 4 Pillars of OOP in C++ (Classes, Encapsulation, Inheritance, Polymorphism, Abstraction)
 * CO2: Clean class design, virtual destructors, and complexity reduction
 * CO3: Exception Handling (try, catch, throw std::runtime_error)
 * CO4: Logic building with decision making & C++ Templates
 */

#include <iostream>
#include <string>
#include <vector>
#include <memory>
#include <stdexcept>

// -----------------------------------------------------------------
// CO1: 1. ENCAPSULATION & CLASS/OBJECT
// -----------------------------------------------------------------
class Wafer {
private:
    std::string waferId;
    std::string imageName;

public:
    Wafer(const std::string& id, const std::string& image)
        : waferId(id), imageName(image) {}

    std::string getWaferId() const { return waferId; }
    std::string getImageName() const { return imageName; }

    void displayWafer() const {
        std::cout << "Wafer ID   : " << waferId << "\n";
        std::cout << "Image Name : " << imageName << "\n";
    }
};

class Defect {
private:
    std::string type;
    double confidence;

public:
    Defect(const std::string& t, double conf)
        : type(t), confidence(conf) {}

    std::string getType() const { return type; }
    double getConfidence() const { return confidence; }
};

// COMPOSITION: InspectionResult HAS-A Defect
class InspectionResult {
private:
    Defect defect;
    std::string status;

public:
    InspectionResult(const Defect& d, const std::string& s)
        : defect(d), status(s) {}

    Defect getDefect() const { return defect; }
    std::string getStatus() const { return status; }
};

// -----------------------------------------------------------------
// CO1: 2. ABSTRACTION & INHERITANCE
// -----------------------------------------------------------------
class BaseAnalyzer {
public:
    virtual ~BaseAnalyzer() = default; // Virtual destructor

    // Pure Virtual Function -> Abstract Base Class
    virtual void analyze(const Wafer& wafer, const InspectionResult& result) = 0;

    void showSystemMessage() const {
        std::cout << "Semiconductor Wafer Inspection System (C++ Engine)\n";
    }
};

// Derived Class -> Single Inheritance
class WaferAnalyzer : public BaseAnalyzer {
public:
    // Method Overriding (Runtime Polymorphism via vtable)
    void analyze(const Wafer& wafer, const InspectionResult& result) override {
        std::cout << "\n===== C++ WAFER ANALYSIS REPORT =====\n";
        wafer.displayWafer();
        std::cout << "Defect Type : " << result.getDefect().getType() << "\n";
        std::cout << "Confidence  : " << result.getDefect().getConfidence() << "%\n";
        std::cout << "Status      : " << result.getStatus() << "\n";
    }

    // Method Overloading (Compile-Time Polymorphism)
    void analyze(const Wafer& wafer) {
        std::cout << "\n===== PRELIMINARY INSPECTION (C++) =====\n";
        wafer.displayWafer();
        std::cout << "Status: Queued.\n";
    }
};

// -----------------------------------------------------------------
// CO4: C++ TEMPLATES (Generic Programming)
// -----------------------------------------------------------------
template <typename T>
class WaferBatch {
private:
    std::vector<T> items;

public:
    void add(const T& item) {
        items.push_back(item);
    }

    size_t size() const {
        return items.size();
    }
};

// -----------------------------------------------------------------
// CO3: EXCEPTION HANDLING
// -----------------------------------------------------------------
double computeDefectDensity(int defectCount, int totalDies) {
    if (totalDies <= 0) {
        throw std::invalid_argument("Total dies must be strictly positive!");
    }
    return (double)defectCount / totalDies * 100.0;
}

int main() {
    std::cout << "==========================================================================\n";
    std::cout << "   COURSE 4: PSOOP LAB (C++ - 2310261L) - DEMONSTRATION\n";
    std::cout << "   MIT Academy of Engineering, Alandi, Pune | Dept of CE\n";
    std::cout << "==========================================================================\n\n";

    // 1. Class, Encapsulation, Composition
    std::cout << ">>> [CO1] 4 PILLARS OF OOP IN C++\n";
    Wafer wafer("TSMC-L46393-W001", "Center_001.png");
    Defect defect("Center", 98.4);
    InspectionResult result(defect, "DEFECTIVE");

    // 2. Runtime Polymorphism via Base Class Pointer
    std::unique_ptr<BaseAnalyzer> analyzer = std::make_unique<WaferAnalyzer>();
    analyzer->showSystemMessage();
    analyzer->analyze(wafer, result); // Polymorphic dispatch

    // 3. Compile-Time Polymorphism (Overloading)
    WaferAnalyzer concreteAnalyzer;
    concreteAnalyzer.analyze(wafer);

    // 4. Exception Handling (CO3)
    std::cout << "\n>>> [CO3] C++ EXCEPTION HANDLING (try / catch / throw)\n";
    try {
        std::cout << "Calculating defect density (Normal Case: 25 / 200):\n";
        double d = computeDefectDensity(25, 200);
        std::cout << "  Result = " << d << "%\n";

        std::cout << "Testing Invalid Input (Total Dies = 0):\n";
        computeDefectDensity(10, 0); // Throws std::invalid_argument
    } catch (const std::exception& ex) {
        std::cout << "  [Caught Exception]: " << ex.what() << " (Handled properly)\n";
    }

    // 5. C++ Templates (CO4)
    std::cout << "\n>>> [CO4] C++ TEMPLATES (Generic Container)\n";
    WaferBatch<Wafer> batch;
    batch.add(wafer);
    std::cout << "Generic WaferBatch<Wafer> successfully holds " << batch.size() << " wafer items.\n";

    std::cout << "\n==========================================================================\n";
    std::cout << "   [SUCCESS] ALL PSOOP (C++) LAB OUTCOMES DEMONSTRATED!\n";
    std::cout << "==========================================================================\n";
    return 0;
}
