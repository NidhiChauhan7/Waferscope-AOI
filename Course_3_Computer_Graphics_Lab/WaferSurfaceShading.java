import java.awt.Color;

/**
 * Course 3: Computer Graphics Lab (2310218L)
 * CO6: RGB Color Models, Realistic Shading & Thermal Heatmap Color Gradients
 */
public class WaferSurfaceShading {

    // RGB Color Model Definition for Semiconductor Materials
    public static final Color SILICON_SUBSTRATE = new Color(226, 232, 240); // Base polished silicon
    public static final Color GOOD_DIE_PASS     = new Color(34, 197, 94);   // Emerald green pass
    public static final Color DEFECT_DIE_FAIL   = new Color(239, 68, 68);   // Alert red fail
    public static final Color ALIGNMENT_NOTCH   = new Color(30, 41, 59);    // Dark orientation notch

    /**
     * Maps normalized defect density (0.0 to 1.0) to an RGB thermal gradient:
     * Translucent Blue -> Cyan -> Yellow -> Orange -> Vivid Red
     */
    public static Color getThermalColor(double density, int alpha) {
        alpha = Math.max(0, Math.min(255, alpha));
        if (density <= 0.0) {
            return new Color(0, 0, 0, 0); // Transparent
        }

        if (density < 0.25) {
            float t = (float) (density / 0.25);
            return new Color(0, (int) (120 + 100 * t), 255, alpha);
        } else if (density < 0.50) {
            float t = (float) ((density - 0.25) / 0.25);
            return new Color((int) (255 * t), 220, (int) (255 * (1 - t)), alpha);
        } else if (density < 0.75) {
            float t = (float) ((density - 0.50) / 0.25);
            return new Color(255, (int) (220 - 100 * t), 0, alpha);
        } else {
            float t = (float) Math.min(1.0, (density - 0.75) / 0.25);
            return new Color(255, (int) (120 * (1 - t)), 0, alpha);
        }
    }
}
