package waferscope.ui;

import java.awt.*;
import javax.swing.*;

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
    private final JTextArea comment = new JTextArea(3, 20);

    public RatingForm(JFrame owner, String detectedDefect) {
        super(owner, "Rate This Analysis", true);
        setLayout(new GridLayout(0, 1, 6, 6));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // 1. Detected result
        JLabel result = new JLabel("Detected defect: " + detectedDefect, SwingConstants.CENTER);
        result.setFont(result.getFont().deriveFont(Font.BOLD, 16f));
        add(result);

        // 2. Star rating (1-5)
        JPanel stars = new JPanel(new FlowLayout());
        stars.add(new JLabel("Rating:"));
        for (int i = 1; i <= 5; i++) {
            JRadioButton star = new JRadioButton(String.valueOf(i));
            star.setActionCommand(String.valueOf(i));
            if (i == 5) star.setSelected(true);
            starGroup.add(star);
            stars.add(star);
        }
        add(stars);

        // 3. Was the detection correct?
        ButtonGroup correct = new ButtonGroup();
        correct.add(yes);
        correct.add(no);
        actualBox.setEnabled(false);
        no.addActionListener(e -> actualBox.setEnabled(true));
        yes.addActionListener(e -> actualBox.setEnabled(false));
        JPanel row = new JPanel(new FlowLayout());
        row.add(new JLabel("Correct?"));
        row.add(yes);
        row.add(no);
        row.add(new JLabel("Actual:"));
        row.add(actualBox);
        add(row);

        // 4. Comment
        add(new JScrollPane(comment));

        // 5. Submit
        JButton submit = new JButton("Submit");
        submit.addActionListener(e -> {
            String rating = starGroup.getSelection().getActionCommand();
            String actual = yes.isSelected() ? detectedDefect : (String) actualBox.getSelectedItem();
            JOptionPane.showMessageDialog(this,
                "Thank you!\nRating: " + rating + "/5\nActual defect: " + actual
                + "\nComment: " + comment.getText());
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

