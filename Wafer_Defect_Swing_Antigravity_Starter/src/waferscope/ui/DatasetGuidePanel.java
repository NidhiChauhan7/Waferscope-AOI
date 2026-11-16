package waferscope.ui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Explains WM-811K dataset integration, schema, and setup instructions.
 */
public class DatasetGuidePanel extends JPanel {

    public DatasetGuidePanel() {
        setLayout(new BorderLayout(12, 12));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(14, 16, 14, 16));

        JTextArea text = new JTextArea();
        text.setEditable(false);
        text.setFont(new Font("Monospaced", Font.PLAIN, 12));
        text.setBackground(new Color(248, 250, 252));
        text.setBorder(new CompoundBorder(new LineBorder(new Color(226, 232, 240)), new EmptyBorder(12, 12, 12, 12)));
        text.setText(
            "========================================================================================\n" +
            "               WM-811K DATASET & MACHINE LEARNING INTEGRATION GUIDE\n" +
            "========================================================================================\n\n" +
            "1. WHAT IS WM-811K?\n" +
            "   - Contributed by Ming-Ju Wu, Jyh-Shing Roger Jang & Jui-Long Chen (MIR Corpora / TSMC).\n" +
            "   - Total Wafer Maps : 811,457 real production wafer maps collected from 46,393 lots.\n" +
            "   - Labelled Maps    : 172,950 wafers labeled by domain engineers into 9 categories.\n" +
            "   - Unlabelled Maps  : 638,507 wafers (ideal for unsupervised Autoencoder anomaly detection).\n\n" +
            "2. THE NINE PRESERVED DEFECT CATEGORIES:\n" +
            "   [1] Center    : Defect cluster concentrated around the center of the wafer.\n" +
            "   [2] Donut     : Toroidal defect ring surrounding the center with a clear core.\n" +
            "   [3] Edge-Loc  : Localized defect cluster situated along the wafer perimeter.\n" +
            "   [4] Edge-Ring : Continuous or semi-continuous ring of defects along the outer edge.\n" +
            "   [5] Loc       : Localized defect cluster away from the edge and center.\n" +
            "   [6] Near-Full : Widespread die failure covering almost the entire wafer area.\n" +
            "   [7] Random    : Scattered, unclustered defect dies (salt-and-pepper noise).\n" +
            "   [8] Scratch   : Linear or curvilinear scratch pattern caused by mechanical handling.\n" +
            "   [9] None      : Normal wafer with no systematic pattern (only random pass/fail noise).\n\n" +
            "3. DEMO DATASET VS REAL WM-811K AUDIT:\n" +
            "   - CURRENT BUNDLE  : 'dataset/wafer_samples.csv' contains 108 synthetic 17x17 demonstration maps\n" +
            "     (12 for each of the 9 categories). It allows immediate testing of the Java Swing GUI,\n" +
            "     visual rendering, file imports, and data pipeline without downloading gigabytes of data.\n" +
            "   - REAL WM-811K    : Original dataset file is typically 'LSWMD.pkl' (~2.5 GB to 3.5 GB).\n" +
            "     Never describe the synthetic demonstration data as WM-811K.\n\n" +
            "4. HOW TO CONNECT REAL WM-811K & TRAIN THE AI MODEL:\n" +
            "   Step 1: Download 'LSWMD.pkl' from Kaggle (Search: 'WM-811K wafer map') or MIR Corpora.\n" +
            "   Step 2: Place the file in the 'ml/data/' folder (e.g., 'ml/data/LSWMD.pkl').\n" +
            "   Step 3: In terminal, run the Python training pipeline:\n" +
            "           python ml/train.py --dataset-path ml/data/LSWMD.pkl --epochs 15\n" +
            "           (Or test the pipeline anytime using: python ml/train.py --use-demo-data)\n" +
            "   Step 4: The script performs stratified train/validation/test splits, computes evaluation\n" +
            "           metrics (Accuracy, Macro-F1, Confusion Matrix), and saves model weights to\n" +
            "           'ml/models/wafer_model.joblib'.\n" +
            "   Step 5: WaferScope automatically detects the trained weights and switches from DEMO\n" +
            "           to TRAINED AI model mode!\n" +
            "========================================================================================"
        );

        JScrollPane scroll = new JScrollPane(text);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);
    }
}
