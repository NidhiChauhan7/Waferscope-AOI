/**
 * Concrete Wafer Analyzer extending Analyzer.
 *
 * OOP CONCEPTS DEMONSTRATED:
 * 1. Single Inheritance: WaferAnalyzer extends Analyzer.
 * 2. Method Overriding: Implements parent's abstract analyze(Wafer, InspectionResult).
 * 3. Method Overloading (Compile-Time Polymorphism):
 *    - analyze(Wafer wafer)
 *    - analyze(Wafer wafer, InspectionResult result)
 *    Same method name with distinct parameter lists.
 */
public class WaferAnalyzer extends Analyzer {

    /**
     * Method Overriding: Overrides Analyzer's abstract method with full reporting logic.
     */
    @Override
    public void analyze(Wafer wafer, InspectionResult result) {
        System.out.println("\n===== WAFER ANALYSIS REPORT =====");
        wafer.displayWaferInfo();
        System.out.println();
        result.displayResult();

        double conf = result.getConfidence();
        String level = (conf >= 80.0) ? "HIGH" : (conf >= 50.0 ? "MODERATE" : "LOW");
        System.out.println("Prediction confidence: " + level);
    }

    /**
     * Method Overloading: Preliminary analysis accepting only a Wafer entity.
     */
    public void analyze(Wafer wafer) {
        System.out.println("\n===== PRELIMINARY WAFER INSPECTION =====");
        wafer.displayWaferInfo();
        System.out.println("Status: Inspection pending / queued in automated pipeline.");
    }
}

