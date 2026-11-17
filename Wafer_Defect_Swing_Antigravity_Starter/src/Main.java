import java.awt.Color;
import java.awt.Font;
import javax.swing.*;
import waferscope.ui.MainFrame;
import waferscope.ui.UiKit;

/**
 * Main application entrypoint for WaferScope AOI.
 * Semiconductor Wafer Defect Detection & Quality Intelligence Platform.
 *
 * Enforces crisp high-contrast theme, WCAG AA/AAA readability,
 * and seamless customer-facing presentation.
 */
public class Main {
    public static void main(String[] args) {
        // Apply System Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        // Enforce strict high-contrast text and UI properties globally
        Color darkSlate = UiKit.TEXT_PRIMARY; // #0f172a (14:1 contrast on white)
        UIManager.put("Label.foreground", darkSlate);
        UIManager.put("Button.foreground", darkSlate);
        UIManager.put("ToggleButton.foreground", darkSlate);
        UIManager.put("TextArea.foreground", darkSlate);
        UIManager.put("TextField.foreground", darkSlate);
        UIManager.put("Table.foreground", darkSlate);
        UIManager.put("TableHeader.foreground", darkSlate);
        UIManager.put("TableHeader.font", new Font("SansSerif", Font.BOLD, 12));
        UIManager.put("ComboBox.foreground", darkSlate);
        UIManager.put("CheckBox.foreground", darkSlate);
        UIManager.put("RadioButton.foreground", darkSlate);
        UIManager.put("TitledBorder.titleColor", darkSlate);
        UIManager.put("Panel.foreground", darkSlate);
        UIManager.put("List.foreground", darkSlate);
        UIManager.put("ToolTip.foreground", darkSlate);
        UIManager.put("ToolTip.background", Color.WHITE);
        UIManager.put("ProgressBar.foreground", UiKit.PRIMARY);
        UIManager.put("ProgressBar.selectionForeground", Color.WHITE);
        UIManager.put("ProgressBar.selectionBackground", darkSlate);

        // Launch UI on Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
