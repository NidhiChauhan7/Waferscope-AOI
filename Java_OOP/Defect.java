/**
 * Represents an individual defect pattern detected on a semiconductor wafer.
 *
 * OOP CONCEPTS DEMONSTRATED:
 * 1. Encapsulation: Private fields (type, confidence) accessible through public getters.
 * 2. Constructor: Explicit initialization of defect state upon object instantiation.
 */
public class Defect {

    private String type;
    private double confidence;

    public Defect(String type, double confidence) {
        this.type = type;
        this.confidence = confidence;
    }

    public String getType() {
        return type;
    }

    public double getConfidence() {
        return confidence;
    }

    public void displayDefect() {
        System.out.println("Defect Type : " + type);
        System.out.println("Confidence  : " + confidence + "%");
    }
}
