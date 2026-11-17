import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;

/**
 * Course 3: Computer Graphics Lab (2310218L)
 * Master Demonstration for Computer Graphics Panel Members
 * Instructors: Mrs. Gauri Thite, Mrs. Mayura Kulkarni, Mr. Vaibhav Rewaskar
 *
 * Demonstrates:
 * CO1, CO2: Midpoint Circle Algorithm & Bresenham Line Algorithm
 * CO3, CO5: 2D Transformations (Translate, Scale, Rotate) & Cohen-Sutherland Clipping
 * CO6: RGB Color Models & Thermal Shading
 * CO4: Application rendering verification
 */
public class ComputerGraphicsDemo {

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("   COURSE 3: COMPUTER GRAPHICS LAB (2310218L) - DEMONSTRATION");
        System.out.println("   MIT Academy of Engineering, Alandi, Pune | Dept of CE");
        System.out.println("==========================================================================\n");

        // -------------------------------------------------------------
        // CO1 & CO2: MIDPOINT CIRCLE & BRESENHAM LINE
        // -------------------------------------------------------------
        System.out.println(">>> [CO1 & CO2] DRAWING ALGORITHMS (MIDPOINT CIRCLE & BRESENHAM)");
        int xc = 200, yc = 200, r = 100;
        List<Point> circlePixels = WaferCircleAlgorithms.midpointCircle(xc, yc, r);
        System.out.println(String.format("Midpoint Circle (Center: %d,%d, Radius: %d): Generated %d perimeter pixels.",
                xc, yc, r, circlePixels.size()));
        System.out.println("Sample 8-way symmetric points:");
        for (int i = 0; i < Math.min(8, circlePixels.size()); i++) {
            Point p = circlePixels.get(i);
            System.out.println(String.format("  Pixel [%d]: (%d, %d)", i, p.x, p.y));
        }

        List<Point> notchLine = WaferCircleAlgorithms.bresenhamLine(190, 300, 210, 300);
        System.out.println(String.format("\nBresenham Notch Line (190,300 to 210,300): Generated %d rasterized pixels.",
                notchLine.size()));

        // -------------------------------------------------------------
        // CO3: 2D GEOMETRIC TRANSFORMATIONS
        // -------------------------------------------------------------
        System.out.println("\n>>> [CO3] 2D GEOMETRIC TRANSFORMATIONS");
        Point2D.Double original = new Point2D.Double(150, 150);
        Point2D.Double center = new Point2D.Double(200, 200);

        Point2D.Double translated = WaferTransformations.translate(original, 50, 30);
        Point2D.Double scaled = WaferTransformations.scale(original, 1.5, 1.5, center);
        Point2D.Double rotated = WaferTransformations.rotate(original, Math.toRadians(45), center);

        System.out.println(String.format("Original Die Coordinate : (%.1f, %.1f)", original.x, original.y));
        System.out.println(String.format("1. Translated (+50, +30): (%.1f, %.1f)", translated.x, translated.y));
        System.out.println(String.format("2. Scaled (Zoom 1.5x)   : (%.1f, %.1f)", scaled.x, scaled.y));
        System.out.println(String.format("3. Rotated (45 degrees) : (%.1f, %.1f)", rotated.x, rotated.y));

        // -------------------------------------------------------------
        // CO5: COHEN-SUTHERLAND CLIPPING ALGORITHM
        // -------------------------------------------------------------
        System.out.println("\n>>> [CO5] COHEN-SUTHERLAND VIEWPORT CLIPPING ALGORITHM");
        double xmin = 50, ymin = 50, xmax = 250, ymax = 250;
        System.out.println(String.format("Viewport Window: [%.0f, %.0f] to [%.0f, %.0f]", xmin, ymin, xmax, ymax));

        // Test 1: Inside line
        var clip1 = WaferTransformations.cohenSutherlandClip(100, 100, 200, 200, xmin, ymin, xmax, ymax);
        System.out.println(String.format("Line (100,100 to 200,200): Visible = %b (Trivial Accept)", clip1.visible));

        // Test 2: Crossing line
        var clip2 = WaferTransformations.cohenSutherlandClip(20, 100, 300, 100, xmin, ymin, xmax, ymax);
        System.out.println(String.format("Line (20,100 to 300,100) : Clipped to (%.1f, %.1f to %.1f, %.1f)",
                clip2.x0, clip2.y0, clip2.x1, clip2.y1));

        // Test 3: Completely outside
        var clip3 = WaferTransformations.cohenSutherlandClip(300, 300, 400, 400, xmin, ymin, xmax, ymax);
        System.out.println(String.format("Line (300,300 to 400,400): Visible = %b (Trivial Reject)", clip3.visible));

        // -------------------------------------------------------------
        // CO6: COLOR MODELS & SHADING
        // -------------------------------------------------------------
        System.out.println("\n>>> [CO6] RGB COLOR MODELS & THERMAL SHADING GRADIENTS");
        System.out.println("RGB Silicon Substrate: " + WaferSurfaceShading.SILICON_SUBSTRATE);
        System.out.println("RGB Good Die Pass    : " + WaferSurfaceShading.GOOD_DIE_PASS);
        System.out.println("RGB Defective Die    : " + WaferSurfaceShading.DEFECT_DIE_FAIL);
        System.out.println("Thermal Gradient at 10% Defect Density : " + WaferSurfaceShading.getThermalColor(0.10, 255));
        System.out.println("Thermal Gradient at 50% Defect Density : " + WaferSurfaceShading.getThermalColor(0.50, 255));
        System.out.println("Thermal Gradient at 90% Defect Density : " + WaferSurfaceShading.getThermalColor(0.90, 255));

        System.out.println("\n==========================================================================");
        System.out.println("   [SUCCESS] ALL COMPUTER GRAPHICS OUTCOMES DEMONSTRATED!");
        System.out.println("==========================================================================");
    }
}
