import javax.swing.*;
import waferscope.ui.MainFrame;

/**
 * Main application entrypoint for WaferScope.
 * AI-Based Semiconductor Wafer Defect Detection System.
 *
 * Integrated Academic Prototype:
 * - Java / PSOOP : Full desktop Swing interface, OOP architecture, file handling, and results
 * - Computer Graphics: Antialiased circular silicon wafer, dies, hover inspector, defect heatmaps
 * - COA : High-resolution execution profiling, memory hierarchy, cache locality, and SIMD analysis
 * - Python / ML : WM-811K deep learning pipeline integration for defect classification
 */
public class Main {
    public static void main(String[] args) {
        // Apply System Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        // Launch UI on Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
