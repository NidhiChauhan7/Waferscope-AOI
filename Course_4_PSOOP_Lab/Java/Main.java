/**
 * Demonstrates the Complete Academic Object-Oriented Programming (OOP) Architecture
 * for the AI-Based Semiconductor Wafer Defect Detection System.
 *
 * Designed for Presentation & Viva demonstration to faculty.
 * 100% DYNAMIC - ZERO HARDCODED / MANUAL VALUES.
 * Binds directly to the active wafer selected in the GUI and the live wafer dataset.
 *
 * OOP CONCEPTS DEMONSTRATED STEP-BY-STEP:
 * 1. Class & Object
 * 2. Encapsulation & Constructors
 * 3. Composition (HAS-A Relationship: InspectionResult HAS-A Defect)
 * 4. Abstraction (Abstract class Analyzer)
 * 5. Method Overriding
 * 6. Method Overloading (Compile-Time Polymorphism)
 * 7. Runtime Polymorphism (Parent reference Analyzer referring to child WaferAnalyzer)
 * 8. Multilevel Inheritance (Analyzer -> WaferAnalyzer -> AIWaferAnalyzer)
 * 9. Multiple Interface Implementation (ReportGenerator, DataExporter)
 * 10. Hierarchical Inheritance (Analyzer -> WaferAnalyzer, Analyzer -> ImageAnalyzer)
 * 11. Backend HTTP IPC (FlaskClient)
 */
public class Main {

    public static void main(String[] args) {
        // DYNAMICALLY LOAD ACTIVE WAFER DATA (ZERO MANUAL VALUES)
        ActiveWaferState active = ActiveWaferState.loadActive();

        System.out.println("=================================================================");
        System.out.println("   SEMICONDUCTOR WAFER DEFECT DETECTION - OOP ARCHITECTURE DEMO");
        System.out.println("   Department of Computer Engineering (Software Engineering)");
        System.out.println("=================================================================");
        System.out.println(" ACTIVE WAFER DYNAMICALLY LOADED: " + active.waferId);
        System.out.println(" Defect Pattern : " + active.defectLabel + " (Confidence: " + active.confidence + "%)");
        System.out.println(" Defect Rate    : " + active.defectDensity + "% | Status: " + active.status);
        System.out.println("=================================================================\n");

        // 1. CLASS, OBJECT & ENCAPSULATION
        System.out.println(">>> 1. OBJECT CREATION & ENCAPSULATION");
        Wafer wafer = new Wafer(active.waferId, active.imageName);
        wafer.displayWaferInfo();

        Defect defect = new Defect(active.defectLabel, active.confidence);
        defect.displayDefect();

        // 2. COMPOSITION (HAS-A Relationship: InspectionResult HAS-A Defect)
        System.out.println("\n>>> 2. COMPOSITION (InspectionResult HAS-A Defect)");
        InspectionResult result = new InspectionResult(defect, active.status);
        result.displayResult();

        // 3. RUNTIME POLYMORPHISM & METHOD OVERRIDING
        // Parent class reference (Analyzer) holding a Child class object (WaferAnalyzer)
        System.out.println("\n>>> 3. RUNTIME POLYMORPHISM & METHOD OVERRIDING");
        Analyzer analyzer = new WaferAnalyzer();
        analyzer.showSystemMessage(); // Inherited concrete method
        analyzer.analyze(wafer, result); // Runtime polymorphic call to overridden method

        // 4. COMPILE-TIME POLYMORPHISM (METHOD OVERLOADING)
        System.out.println("\n>>> 4. COMPILE-TIME POLYMORPHISM (METHOD OVERLOADING)");
        WaferAnalyzer basicAnalyzer = new WaferAnalyzer();
        basicAnalyzer.analyze(wafer); // Overloaded version with 1 parameter

        // 5. MULTILEVEL INHERITANCE (Analyzer -> WaferAnalyzer -> AIWaferAnalyzer)
        // 6. MULTIPLE INTERFACE IMPLEMENTATION (ReportGenerator, DataExporter)
        System.out.println("\n>>> 5. MULTILEVEL INHERITANCE & INTERFACES (AIWaferAnalyzer)");
        AIWaferAnalyzer aiAnalyzer = new AIWaferAnalyzer();
        aiAnalyzer.analyze(wafer, result);
        aiAnalyzer.generateReport(); // From ReportGenerator interface
        aiAnalyzer.exportData();     // From DataExporter interface

        // 7. HIERARCHICAL INHERITANCE (Analyzer extended by both WaferAnalyzer & ImageAnalyzer)
        System.out.println("\n>>> 6. HIERARCHICAL INHERITANCE (ImageAnalyzer extends Analyzer)");
        ImageAnalyzer imageAnalyzer = new ImageAnalyzer();
        imageAnalyzer.analyze(wafer, result);

        // 8. FLASK BACKEND CONNECTIVITY DEMONSTRATION
        System.out.println("\n>>> 7. HTTP BACKEND CONNECTIVITY (FlaskClient)");
        FlaskClient flaskClient = new FlaskClient();
        flaskClient.checkBackendStatus();

        System.out.println("\n=================================================================");
        System.out.println("   [SUCCESS] ALL OOP CONCEPTS DEMONSTRATED DYNAMICALLY!");
        System.out.println("=================================================================");
    }
}
