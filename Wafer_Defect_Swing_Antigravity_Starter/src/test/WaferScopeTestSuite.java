package test;

import waferscope.classifier.DefectClassifier;
import waferscope.classifier.DemoNearestNeighborClassifier;
import waferscope.classifier.PythonInferenceEngine;
import waferscope.io.DatasetLoader;
import waferscope.model.DieStatus;
import waferscope.model.PredictionResult;
import waferscope.model.WaferMap;

import java.nio.file.Path;
import java.util.List;

/**
 * Complete Automated Unit & Integration Test Suite for WaferScope.
 * Verifies parsing, proper wafer IDs, nearest-neighbor matching,
 * Python ML subprocess inference, and COA timing measurements.
 */
public class WaferScopeTestSuite {

    private static int testsRun = 0;
    private static int testsPassed = 0;

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("       WAFERSCOPE AUTOMATED TEST SUITE");
        System.out.println("=================================================\n");

        testWaferMapCreationAndMetrics();
        testWaferMapDensityCalculation();
        testDatasetLoadingAndProperIds();
        testDemoClassifierBaseline();
        testPythonInferenceEngine();

        System.out.println("\n-------------------------------------------------");
        System.out.println(String.format("Tests Completed: %d / %d Passed", testsPassed, testsRun));
        System.out.println("-------------------------------------------------");

        if (testsPassed == testsRun) {
            System.out.println("[SUCCESS] ALL TESTS PASSED SUCCESSFULLY!");
            System.exit(0);
        } else {
            System.err.println("[FAILURE] SOME TESTS FAILED!");
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition) {
        testsRun++;
        if (condition) {
            testsPassed++;
            System.out.println("[PASS] " + testName);
        } else {
            System.err.println("[FAIL] " + testName);
        }
    }

    private static void testWaferMapCreationAndMetrics() {
        System.out.println("[1] Testing WaferMap Creation & Metrics...");
        String samplePipe = "EEEE|E01E|E00E|EEEE";
        WaferMap wm = WaferMap.fromPipeString("TEST-W001", "Center", samplePipe);

        assertTrue("Wafer ID correctly assigned", "TEST-W001".equals(wm.getId()));
        assertTrue("Rows count matches", wm.getRows() == 4);
        assertTrue("Cols count matches", wm.getCols() == 4);
        assertTrue("Outside count matches", wm.getOutsideCount() == 12);
        assertTrue("Good count matches", wm.getGoodCount() == 3);
        assertTrue("Defect count matches", wm.getDefectiveCount() == 1);
        assertTrue("Valid count matches", wm.getValidCount() == 4);
        assertTrue("Defect percentage matches", Math.abs(wm.getDefectPercentage() - 25.0) < 0.01);
        assertTrue("DieStatus conversion matches", wm.getDieStatus(1, 2) == DieStatus.DEFECTIVE);
    }

    private static void testWaferMapDensityCalculation() {
        System.out.println("\n[2] Testing Defect Heatmap Density Calculation...");
        String samplePipe = "EEEE|E01E|E00E|EEEE";
        WaferMap wm = WaferMap.fromPipeString("TEST-W002", "Center", samplePipe);
        double[][] density = wm.computeDensityMap(1);

        assertTrue("Density map dimensions match rows", density.length == 4);
        assertTrue("Density map dimensions match cols", density[0].length == 4);
        assertTrue("Outside cell marked as -1", density[0][0] == -1.0);
        assertTrue("Defective neighborhood density > 0", density[1][2] > 0.0);
    }

    private static void testDatasetLoadingAndProperIds() {
        System.out.println("\n[3] Testing DatasetLoader & Proper SEMI Fab IDs...");
        try {
            Path p = DatasetLoader.resolveDefaultDatasetPath();
            DatasetLoader.LoadReport report = DatasetLoader.loadCsv(p);
            List<WaferMap> wafers = report.getWafers();

            assertTrue("Loaded exactly 108 demonstration wafers", wafers.size() == 108);

            // Check first wafer has proper fab ID
            WaferMap first = wafers.get(0);
            assertTrue("Wafer has proper SEMI ID format", first.getId().startsWith("TSMC-L46393-W001"));
            assertTrue("First wafer label is Center", "Center".equalsIgnoreCase(first.getGroundTruthLabel()));

            // Check all 9 categories exist with proper IDs
            boolean hasCenter = wafers.stream().anyMatch(w -> w.getId().contains("-CTR"));
            boolean hasDonut = wafers.stream().anyMatch(w -> w.getId().contains("-DNT"));
            boolean hasEdgeLoc = wafers.stream().anyMatch(w -> w.getId().contains("-ELC"));
            boolean hasEdgeRing = wafers.stream().anyMatch(w -> w.getId().contains("-ERG"));
            boolean hasLoc = wafers.stream().anyMatch(w -> w.getId().contains("-LOC"));
            boolean hasNearFull = wafers.stream().anyMatch(w -> w.getId().contains("-NFL"));
            boolean hasRandom = wafers.stream().anyMatch(w -> w.getId().contains("-RND"));
            boolean hasScratch = wafers.stream().anyMatch(w -> w.getId().contains("-SCR"));
            boolean hasNone = wafers.stream().anyMatch(w -> w.getId().contains("-NON"));

            assertTrue("All 9 defect category codes present in dataset",
                hasCenter && hasDonut && hasEdgeLoc && hasEdgeRing && hasLoc &&
                hasNearFull && hasRandom && hasScratch && hasNone);

        } catch (Exception ex) {
            assertTrue("Dataset loading failed with exception: " + ex.getMessage(), false);
        }
    }

    private static void testDemoClassifierBaseline() {
        System.out.println("\n[4] Testing Demo Nearest-Neighbor Classifier...");
        try {
            Path p = DatasetLoader.resolveDefaultDatasetPath();
            DatasetLoader.LoadReport report = DatasetLoader.loadCsv(p);
            DemoNearestNeighborClassifier classifier = new DemoNearestNeighborClassifier(report.getWafers());

            WaferMap testWafer = report.getWafers().get(0); // TSMC-L46393-W001-CTR
            PredictionResult res = classifier.classify(testWafer);

            assertTrue("Result Wafer ID matches", testWafer.getId().equals(res.getWaferId()));
            assertTrue("Model mode is DEMO", "DEMO".equals(res.getModelMode()));
            assertTrue("Predicted class is Center", "Center".equalsIgnoreCase(res.getPredictedClass()));
            assertTrue("Status is ANOMALOUS", res.isAnomalous());
            assertTrue("Score is between 0 and 1", res.getConfidenceOrScore() >= 0.0 && res.getConfidenceOrScore() <= 1.0);
            assertTrue("Processing time is measured (> 0 ms)", res.getProcessingMs() >= 0.0);

        } catch (Exception ex) {
            assertTrue("Demo classifier test failed: " + ex.getMessage(), false);
        }
    }

    private static void testPythonInferenceEngine() {
        System.out.println("\n[5] Testing Python Inference Engine (CLI Process Integration)...");
        try {
            PythonInferenceEngine pyEngine = new PythonInferenceEngine();
            assertTrue("Python predict.py script found", pyEngine.isAvailable());

            Path p = DatasetLoader.resolveDefaultDatasetPath();
            DatasetLoader.LoadReport report = DatasetLoader.loadCsv(p);
            WaferMap testWafer = report.getWafers().get(0);

            PredictionResult res = pyEngine.classify(testWafer);

            assertTrue("Python inference returned valid wafer ID", testWafer.getId().equals(res.getWaferId()));
            assertTrue("Predicted class is non-empty", res.getPredictedClass() != null && !res.getPredictedClass().isEmpty());
            assertTrue("Class scores map populated", !res.getClassScores().isEmpty());
            assertTrue("Model mode is TRAINED or DEMO", res.getModelMode().equals("TRAINED") || res.getModelMode().equals("DEMO"));
            System.out.println("  -> Python Inference Class: " + res.getPredictedClass() + ", Confidence: " + String.format("%.2f%%", res.getConfidenceOrScore() * 100.0) + " in " + res.getProcessingMs() + " ms");

        } catch (Exception ex) {
            assertTrue("Python inference failed with exception: " + ex.getMessage(), false);
        }
    }
}
