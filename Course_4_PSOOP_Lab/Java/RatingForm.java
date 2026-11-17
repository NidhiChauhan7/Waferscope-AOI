import java.awt.*;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * Short rating form shown after a wafer has been analysed.
 * Collects engineer feedback on defect classification accuracy,
 * star rating (1-5), and correction feedback.
 *
 * Compile: javac RatingForm.java      Test: java RatingForm
 */
public class RatingForm extends JDialog {

    private static final String[] DEFECTS = {
        "Center", "Donut", "Edge-Loc", "Edge-Ring", "Loc",
        "Random", "Scratch", "Near-Full", "None"
    };

    private final ButtonGroup starGroup = new ButtonGroup();
    private final JRadioButton yes = new JRadioButton("Yes", true);
    private final JRadioButton no = new JRadioButton("No");
    private final JComboBox<String> actualBox = new JComboBox<>(DEFECTS);
    private final JTextArea comment = new JTextArea(3, 22);

    public RatingForm(JFrame owner, String detectedDefect) {
        super(owner, "Rate This Wafer Analysis", true);
        setLayout(new GridLayout(0, 1, 8, 8));
        getContentPane().setBackground(Color.WHITE);
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        Color darkText = new Color(15, 23, 42);

        // 1. Detected result
        JLabel result = new JLabel("Detected Defect: " + detectedDefect, SwingConstants.CENTER);
        result.setFont(new Font("SansSerif", Font.BOLD, 15));
        result.setForeground(new Color(37, 99, 235));
        add(result);

        // 2. Star rating (1-5)
        JPanel stars = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 2));
        stars.setBackground(Color.WHITE);
        JLabel rateLbl = new JLabel("Accuracy Rating (1-5):");
        rateLbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        rateLbl.setForeground(darkText);
        stars.add(rateLbl);

        for (int i = 1; i <= 5; i++) {
            JRadioButton star = new JRadioButton(String.valueOf(i));
            star.setActionCommand(String.valueOf(i));
            star.setFont(new Font("SansSerif", Font.BOLD, 11));
            star.setForeground(darkText);
            star.setBackground(Color.WHITE);
            if (i == 5) star.setSelected(true);
            starGroup.add(star);
            stars.add(star);
        }
        add(stars);

        // 3. Was the detection correct?
        ButtonGroup correct = new ButtonGroup();
        correct.add(yes);
        correct.add(no);
        yes.setFont(new Font("SansSerif", Font.BOLD, 11));
        yes.setForeground(darkText);
        yes.setBackground(Color.WHITE);
        no.setFont(new Font("SansSerif", Font.BOLD, 11));
        no.setForeground(darkText);
        no.setBackground(Color.WHITE);

        actualBox.setEnabled(false);
        actualBox.setFont(new Font("SansSerif", Font.PLAIN, 12));
        actualBox.setForeground(darkText);
        actualBox.setBackground(Color.WHITE);

        no.addActionListener(e -> actualBox.setEnabled(true));
        yes.addActionListener(e -> actualBox.setEnabled(false));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 2));
        row.setBackground(Color.WHITE);
        JLabel correctLbl = new JLabel("Correct?");
        correctLbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        correctLbl.setForeground(darkText);
        row.add(correctLbl);
        row.add(yes);
        row.add(no);

        JLabel actualLbl = new JLabel("Actual Defect:");
        actualLbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        actualLbl.setForeground(darkText);
        row.add(actualLbl);
        row.add(actualBox);
        add(row);

        // 4. Comment
        comment.setFont(new Font("Monospaced", Font.PLAIN, 11));
        comment.setForeground(darkText);
        comment.setBackground(new Color(248, 250, 252));
        comment.setBorder(new CompoundBorder(new LineBorder(new Color(203, 213, 225)), new EmptyBorder(6, 6, 6, 6)));
        add(new JScrollPane(comment));

        // 5. Submit
        JButton submit = new JButton("Submit Rating & Feedback");
        submit.setFont(new Font("SansSerif", Font.BOLD, 12));
        submit.setBackground(new Color(37, 99, 235));
        submit.setForeground(Color.WHITE);
        submit.setFocusPainted(false);
        submit.addActionListener(e -> {
            String rating = starGroup.getSelection().getActionCommand();
            String actual = yes.isSelected() ? detectedDefect : (String) actualBox.getSelectedItem();
            JOptionPane.showMessageDialog(this,
                "Thank you for engineering feedback!\nRating: " + rating + " / 5\nGround Truth: " + actual
                + "\nNotes: " + (comment.getText().isBlank() ? "N/A" : comment.getText()),
                "Feedback Recorded", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        });
        add(submit);

        pack();
        setLocationRelativeTo(owner);
    }

    // Standalone test
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RatingForm(null, "Donut").setVisible(true));
    }
}
