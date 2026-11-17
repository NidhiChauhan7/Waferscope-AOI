/**
 * Optical Image Analysis module extending Analyzer.
 *
 * OOP CONCEPTS DEMONSTRATED:
 * 1. Hierarchical Inheritance:
 *    A single parent class (Analyzer) is extended by multiple sibling subclasses:
 *    Analyzer -> WaferAnalyzer
 *    Analyzer -> ImageAnalyzer
 * 2. Method Overriding: Implements abstract analyze() with image-specific processing.
 */
public class ImageAnalyzer extends Analyzer {

    @Override
    public void analyze(Wafer wafer, InspectionResult result) {
        System.out.println("\n----- IMAGE ANALYSIS -----");
        System.out.println("Wafer ID   : " + wafer.getWaferId());
        System.out.println("Image Name : " + wafer.getImageName());
        System.out.println("Image analysis completed.");
        System.out.println("Detected pattern: " + result.getDefectType());
    }
}

