/**
 * Encapsulates the overall result of a wafer inspection cycle.
 *
 * OOP CONCEPTS DEMONSTRATED:
 * 1. Composition (HAS-A Relationship):
 *    InspectionResult HAS-A Defect. Rather than inheriting from Defect,
 *    an InspectionResult contains a reference to a Defect object,
 *    demonstrating strong composition.
 * 2. Encapsulation: Private member variables with public accessors.
 */
public class InspectionResult {

    private Defect defect;
    private String status;

    public InspectionResult(Defect defect, String status) {
        this.defect = defect;
        this.status = status;
    }

    public Defect getDefect() {
        return defect;
    }

    public String getStatus() {
        return status;
    }

    public String getDefectType() {
        return defect.getType();
    }

    public double getConfidence() {
        return defect.getConfidence();
    }

    public void displayResult() {
        System.out.println("\n----- AI INSPECTION RESULT -----");
        defect.displayDefect();
        System.out.println("Status      : " + status);
    }
}

