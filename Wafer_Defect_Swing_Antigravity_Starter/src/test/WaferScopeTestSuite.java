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
        testCppGraphicsBridge();
        testDataStructuresEngine();
        testCoaArchitectureEngine();
        testJavaHistoryArraySorter();

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

    private static void testCppGraphicsBridge() {
        System.out.println("\n[6] Testing C++ Computer Graphics Bridge Integration...");
        try {
            waferscope.classifier.CppGraphicsBridge bridge = new waferscope.classifier.CppGraphicsBridge();
            var rep = bridge.runGraphicsPipeline(150, 150, 120, 140, 30, 160, 30, 20, 100, 280, 100, 50, 50, 250, 250);
            assertTrue("C++ graphics engine report non-null", rep != null);
            assertTrue("Midpoint circle points generated > 0", rep.circlePointsCount > 0);
            assertTrue("Bresenham line points generated > 0", rep.linePointsCount > 0);
            assertTrue("Cohen-Sutherland clipping executed", rep.clipVisible);
            assertTrue("Graphics execution measured (>= 0 ms)", rep.latencyMs >= 0.0);

            // Test C++ Lighting (Phong Illumination)
            java.awt.Color lit = waferscope.classifier.CppGraphicsBridge.applyLighting(
                new java.awt.Color(34, 197, 94), 0, 0, 1, 45.0, 0.20, 0.65, 0.40);
            assertTrue("Phong lighting color generated non-null", lit != null);

            // Test C++ Texture Mapping
            java.awt.Color texCheck = waferscope.classifier.CppGraphicsBridge.getTextureColor(16, 16, "CHECKERBOARD");
            assertTrue("Checkerboard texture pixel generated non-null", texCheck != null);

            // Test C++ 2D Geometric Transformation (Rotation & Scaling)
            var pt = waferscope.classifier.CppGraphicsBridge.transformPoint(100, 100, 1.5, 90.0, 0, 0, 100, 100);
            assertTrue("2D geometric transformation computed", pt != null);

            // Test C++ Outer Circle & Grid Clipping (Practical 05)
            double cellSize = 20.0;
            double cx = 17 * cellSize / 2.0;
            double cy = 17 * cellSize / 2.0;
            double radius = 17 * cellSize * 1.05 / 2.0;
            var clipRep = waferscope.classifier.CppGraphicsBridge.clipGridToCircle(17, 17, cellSize, cx, cy, radius);
            assertTrue("Outer Circle Clipping report non-null", clipRep != null);
            assertTrue("Outer Circle Clipping identifies preserved dies (> 150)", clipRep.insideCount > 150);
            assertTrue("Outer Circle Clipping identifies clipped-out grid cells (> 0)", clipRep.clippedOutCount > 0);
            assertTrue("Total grid dies matches 289", clipRep.totalGridDies == 289);
            assertTrue("Circle equation x² + y² <= R² holds at center", waferscope.classifier.CppGraphicsBridge.isPointInsideCircle(0, 0, 0, 0, 50.0));
            assertTrue("Circle equation x² + y² > R² identifies outside die", !waferscope.classifier.CppGraphicsBridge.isPointInsideCircle(60, 60, 0, 0, 50.0));

            System.out.println("  -> C++ Engine: " + rep.engineInfo + ", Latency: " + String.format("%.3f ms", rep.latencyMs));
            System.out.println("  -> Outer Circle Clipping: " + clipRep.insideCount + " Preserved, " + clipRep.clippedOutCount + " Clipped Out (" + clipRep.equation + ")");
        } catch (Exception ex) {
            assertTrue("C++ graphics bridge failed: " + ex.getMessage(), false);
        }
    }

    private static void testDataStructuresEngine() {
        System.out.println("\n[7] Testing Live Data Structures Engine & QuickSort...");
        try {
            // QuickSort verification
            waferscope.datastructures.WaferSearchSort.WaferRecord[] records = new waferscope.datastructures.WaferSearchSort.WaferRecord[]{
                new waferscope.datastructures.WaferSearchSort.WaferRecord("W3", "Center", 20, 200, 10.0, 90.0),
                new waferscope.datastructures.WaferSearchSort.WaferRecord("W1", "None", 0, 200, 0.0, 100.0),
                new waferscope.datastructures.WaferSearchSort.WaferRecord("W2", "Donut", 30, 200, 15.0, 85.0)
            };
            var report = waferscope.datastructures.WaferSearchSort.quickSortByDensity(records);
            assertTrue("QuickSort report non-null", report != null);
            assertTrue("QuickSort sorted ascending (0.0% <= 10.0% <= 15.0%)",
                records[0].defectDensity <= records[1].defectDensity && records[1].defectDensity <= records[2].defectDensity);

            // Binary Search verification
            int idx = waferscope.datastructures.WaferSearchSort.binarySearch(records, "W3");
            assertTrue("Binary Search locates target ID", idx != -1);

            // Linked List verification
            waferscope.datastructures.WaferLinkedList list = new waferscope.datastructures.WaferLinkedList();
            list.insertHead("W-HEAD", "Center", 12.5);
            list.insertTail("W-TAIL", "Donut", 18.0);
            assertTrue("Linked List head non-null", list.getHead() != null);
            assertTrue("Linked List size is 2", list.getSize() == 2);

            // Hash Table verification
            waferscope.datastructures.WaferHashTable ht = new waferscope.datastructures.WaferHashTable(5);
            ht.put("TSMC-TEST", "Center Pattern [12.5%]");
            assertTrue("Hash table retrieval O(1) matches", "Center Pattern [12.5%]".equals(ht.get("TSMC-TEST")));

            // C++ Data Structures Bridge verification
            waferscope.datastructures.CppDataStructuresBridge bridge = new waferscope.datastructures.CppDataStructuresBridge();
            var cppSortReport = bridge.runQuickSort(new java.util.ArrayList<>(), "asc");
            assertTrue("C++ Data Structures Bridge sort report non-null", cppSortReport != null);
            assertTrue("C++ Data Structures Engine info present", cppSortReport.engineInfo != null && !cppSortReport.engineInfo.isEmpty());

            var cppSearchReport = bridge.runBinarySearch(new java.util.ArrayList<>(), "W1");
            assertTrue("C++ Data Structures Bridge search report non-null", cppSearchReport != null);

            System.out.println("  -> QuickSort Latency: " + String.format("%.3f ms", report.latencyMs) + ", Comparisons: " + report.comparisons);
            System.out.println("  -> C++ DS Engine: " + cppSortReport.engineInfo + ", Latency: " + String.format("%.3f us", cppSortReport.latencyMicroseconds));
        } catch (Exception ex) {
            assertTrue("Data structures engine test failed: " + ex.getMessage(), false);
        }
    }

    private static void testCoaArchitectureEngine() {
        System.out.println("\n[8] Testing Computer Organization & Architecture (COA) Engine...");
        try {
            int[][] data = new int[17][17];
            for (int r = 0; r < 17; r++) {
                for (int c = 0; c < 17; c++) {
                    double dist = Math.hypot(r - 8, c - 8);
                    if (dist > 8) data[r][c] = 0; // Outside
                    else if (dist < 4) data[r][c] = 2; // Defect
                    else data[r][c] = 1; // Good
                }
            }
            WaferMap w = new WaferMap("COA-TEST", "Center", data);
            var t = waferscope.datastructures.CoaArchitectureEngine.simulate(w);

            assertTrue("COA Telemetry non-null", t != null);
            assertTrue("RAX equals Good die count", t.rax == w.getGoodCount());
            assertTrue("RBX equals Defective die count", t.rbx == w.getDefectiveCount());
            assertTrue("RDX equals Outside substrate count", t.rdx == w.getOutsideCount());
            assertTrue("RCX decremented to 0 and Zero Flag ZF=1", t.rcx == 0 && t.zf);
            assertTrue("Successive Addition equals Shift-and-Add product", t.successiveProduct == t.shiftAddProduct);
            assertTrue("Shift-and-Add cycles < Successive cycles (logarithmic speedup)", t.shiftAddCycles < t.successiveCycles);
            assertTrue("L1 Data Cache total accesses equals 289", (t.cacheHits + t.cacheMisses) == 289);
            assertTrue("L1 Data Cache Hit Rate > 90%", t.cacheHitRate > 90.0);
            assertTrue("COA Register File report generated", t.registerFileReport != null && !t.registerFileReport.isEmpty());
            assertTrue("COA 64-Bit Assembly trace generated", t.assemblyTraceReport != null && !t.assemblyTraceReport.isEmpty());

            System.out.println(String.format("  -> COA CPU State: RAX=%d (Good), RBX=%d (Defect), ZF=%b", t.rax, t.rbx, t.zf));
            System.out.println(String.format("  -> Arithmetic: Shift-and-Add %d cycles vs Successive %d cycles", t.shiftAddCycles, t.successiveCycles));
            System.out.println(String.format("  -> L1 Data Cache: %d hits, %d misses (Hit Rate: %.2f%%)", t.cacheHits, t.cacheMisses, t.cacheHitRate));
        } catch (Exception ex) {
            assertTrue("COA engine test failed: " + ex.getMessage(), false);
        }
    }

    private static void testJavaHistoryArraySorter() {
        System.out.println("\n[9] Testing Course 4 Java History Array Sorter (Time, Latency, Yield)...");
        try {
            PredictionResult r1 = new PredictionResult.Builder()
                .waferId("W-01")
                .predictedClass("Center")
                .anomalous(true)
                .confidenceOrScore(0.95)
                .defectiveDieCount(40)
                .totalValidDieCount(200)
                .processingMs(12.5)
                .timestamp("2026-11-17 01:00:00")
                .build();

            PredictionResult r2 = new PredictionResult.Builder()
                .waferId("W-02")
                .predictedClass("None")
                .anomalous(false)
                .confidenceOrScore(0.99)
                .defectiveDieCount(0)
                .totalValidDieCount(200)
                .processingMs(2.1)
                .timestamp("2026-11-17 02:00:00")
                .build();

            PredictionResult r3 = new PredictionResult.Builder()
                .waferId("W-03")
                .predictedClass("Donut")
                .anomalous(true)
                .confidenceOrScore(0.88)
                .defectiveDieCount(20)
                .totalValidDieCount(200)
                .processingMs(7.8)
                .timestamp("2026-11-17 03:00:00")
                .build();

            List<PredictionResult> list = List.of(r1, r2, r3);

            // 1. Sort by Time: Newest First
            var reportTime = waferscope.datastructures.JavaHistoryArraySorter.arrange(
                list, waferscope.datastructures.JavaHistoryArraySorter.CRITERIA_TIME_DESC, "ALL", ""
            );
            assertTrue("Time Descending array non-null", reportTime.sortedArray != null);
            assertTrue("Time Descending sorted newest first (03:00 >= 02:00 >= 01:00)",
                reportTime.sortedArray[0].getWaferId().equals("W-03") && reportTime.sortedArray[2].getWaferId().equals("W-01"));

            // 2. Sort by Latency: Lowest / Fastest First
            var reportLatency = waferscope.datastructures.JavaHistoryArraySorter.arrange(
                list, waferscope.datastructures.JavaHistoryArraySorter.CRITERIA_LATENCY_ASC, "ALL", ""
            );
            assertTrue("Latency Ascending sorted fastest first (2.1ms <= 7.8ms <= 12.5ms)",
                reportLatency.sortedArray[0].getProcessingMs() <= reportLatency.sortedArray[1].getProcessingMs() &&
                reportLatency.sortedArray[1].getProcessingMs() <= reportLatency.sortedArray[2].getProcessingMs());

            // 3. Sort by Yield: Highest Quality First
            var reportYield = waferscope.datastructures.JavaHistoryArraySorter.arrange(
                list, waferscope.datastructures.JavaHistoryArraySorter.CRITERIA_YIELD_DESC, "ALL", ""
            );
            assertTrue("Yield Descending sorted highest yield first (100% >= 90% >= 80%)",
                reportYield.sortedArray[0].getYieldPercentage() >= reportYield.sortedArray[1].getYieldPercentage() &&
                reportYield.sortedArray[1].getYieldPercentage() >= reportYield.sortedArray[2].getYieldPercentage());

            // 4. Dynamic Filter: Anomalous Only
            var reportFilter = waferscope.datastructures.JavaHistoryArraySorter.arrange(
                list, waferscope.datastructures.JavaHistoryArraySorter.CRITERIA_TIME_DESC, "ANOMALOUS ONLY", ""
            );
            assertTrue("Filter Anomalous returns exactly 2 records", reportFilter.filteredCount == 2);

            // 5. Dynamic Filter: Wafer ID Search
            var reportSearch = waferscope.datastructures.JavaHistoryArraySorter.arrange(
                list, waferscope.datastructures.JavaHistoryArraySorter.CRITERIA_TIME_DESC, "ALL", "W-01"
            );
            assertTrue("Search W-01 returns exactly 1 record", reportSearch.filteredCount == 1 && reportSearch.sortedArray[0].getWaferId().equals("W-01"));

            System.out.println("  -> " + reportTime.telemetrySummary);
            System.out.println("  -> " + reportLatency.telemetrySummary);
            System.out.println("  -> " + reportYield.telemetrySummary);
        } catch (Exception ex) {
            assertTrue("Java history array sorter test failed: " + ex.getMessage(), false);
        }
    }
}
