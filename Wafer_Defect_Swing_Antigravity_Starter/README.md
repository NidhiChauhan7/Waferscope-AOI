# WaferScope AOI — AI-Based Semiconductor Wafer Defect Detection System

An integrated academic engineering prototype developed for **Java / PSOOP**, **C++ / PL**, **Computer Graphics**, and **Computer Organization & Architecture (COA)**.

Department of Computer Engineering (Software Engineering)  
MIT Academy of Engineering, Alandi, Pune

---

## 1. What's New in this Release

### A. Standardized SEMI Fab Wafer Identifiers
Every wafer now carries an authentic, standardized semiconductor fabrication lot-wafer identifier based on TSMC WM-811K benchmark lot conventions:
- **Center**: `TSMC-L46393-W001-CTR` to `TSMC-L46393-W012-CTR`
- **Donut**: `TSMC-L46393-W013-DNT` to `TSMC-L46393-W024-DNT`
- **Edge-Loc**: `TSMC-L46393-W025-ELC` to `TSMC-L46393-W036-ELC`
- **Edge-Ring**: `TSMC-L46393-W037-ERG` to `TSMC-L46393-W048-ERG`
- **Loc**: `TSMC-L46393-W049-LOC` to `TSMC-L46393-W060-LOC`
- **Near-Full**: `TSMC-L46393-W061-NFL` to `TSMC-L46393-W072-NFL`
- **Random**: `TSMC-L46393-W073-RND` to `TSMC-L46393-W084-RND`
- **Scratch**: `TSMC-L46393-W085-SCR` to `TSMC-L46393-W096-SCR`
- **None (Normal)**: `TSMC-L46393-W097-NON` to `TSMC-L46393-W108-NON`

### B. Segregated Architecture & Modern Navigation
The application interface is segregated into 6 purpose-built views accessible via the top navigation bar:
1. 📊 **Dashboard**: Semiconductor fab quality KPIs (Total Wafers, Normal/Pass count, Anomalous count, Mean Die Yield), defect pattern distribution cards with visual badges, and "Run Full Lot Batch Anomaly Scan".
2. 🔬 **Wafer Visualizer**: Circular silicon substrate rendering with antialiased dies, physical orientation notch/flat, heatmap density overlay with intensity control, zoom/pan navigation, and an interactive die inspector on click.
3. ⚡ **AI Inference Studio**: Segregated 5-stage interactive pipeline (Ingestion -> Denoising -> Feature Extraction -> Anomaly Gate -> Classification), real-time interactive anomaly sensitivity slider (1% to 25%), step-by-step pipeline simulation, 9-class calibrated probability bars, and C++ native acceleration toggle.
4. 📜 **Inspection History**: Tabular audit log of all analyzed wafers in the session, sorting, and CSV export.
5. 💻 **COA & Hardware Profiling**: Real system telemetry (JVM Heap utilization, available logical CPU cores, high-resolution monotonic timer latency breakdown) and memory hierarchy/cache line analysis.
6. 📚 **WM-811K Dataset Guide**: Complete documentation of the 811,457-wafer dataset schema, download instructions (`LSWMD.pkl`), and model training steps.

### C. Industrial Header & Status Footer
- **Header**: System branding (`WAFERSCOPE AOI`), active lot pill (`LOT: TSMC-46393`), model mode pill (`TRAINED AI` vs `DEMO BASELINE`), total sample counter, and real-time navigation tabs.
- **Footer**: Active wafer breadcrumb (ID, dimensions, defect count, percentage, status), live JVM memory gauge (`Used MB / Max MB`), CPU core count, and academic credits.

---

## 2. Four-Course Academic Integration Summary

| Subject | Contribution | Key Implementation Files |
| :--- | :--- | :--- |
| **Java / PSOOP** | Desktop Swing GUI, Object-Oriented design, `SwingWorker` non-blocking async execution, file parsing, and result models. | `Main.java`, `MainFrame.java`, `WaferMap.java`, `DatasetLoader.java`, `PredictionResult.java` |
| **C++ / PL** | High-performance numerical preprocessor, 3x3 morphological filtering, spatial centroid, radial entropy, and angular moment extraction. | `cpp/wafer_preprocessor.cpp`, `CppPreprocessorBridge.java` |
| **Computer Graphics** | Circular silicon substrate geometry, antialiased die rasterization, wafer orientation flat/notch, continuous thermal heatmaps, and die inspection HUD. | `WaferCanvas.java`, `WaferViewPanel.java` |
| **COA** | High-resolution monotonic timing (`System.nanoTime()`), memory hierarchy (64-byte L1 cache line locality in 2D grids), SIMD vectorization potential, and IPC profiling. | `CoaMetricsPanel.java`, `FooterPanel.java` |

---

## 3. How to Run the Application

### Prerequisites
- **Java**: JDK 11 or later (JDK 17 recommended). Verify with `javac -version`.
- **Python** (Optional, for trained AI model): Python 3.9+ with `scikit-learn`, `numpy`, `pandas`, `scipy`.

### On Windows
Run the bundled launcher:
```cmd
run_windows.bat
```
Or manually compile and run:
```cmd
javac -encoding UTF-8 -d out -sourcepath src src\Main.java src\waferscope\model\*.java src\waferscope\io\*.java src\waferscope\classifier\*.java src\waferscope\ui\*.java
java -cp out Main
```

### On Linux / macOS
```bash
chmod +x run_unix.sh
./run_unix.sh
```

---

## 4. Running Automated Tests

A comprehensive 28-test suite verifies all models, parsers, classifiers, and proper IDs:
```cmd
javac -d out -sourcepath src src\test\WaferScopeTestSuite.java
java -cp out test.WaferScopeTestSuite
```
Expected output:
```
Tests Completed: 28 / 28 Passed
[SUCCESS] ALL TESTS PASSED SUCCESSFULLY!
```

---

## 5. Training the AI Model (Python ML Pipeline)

To train the defect classification model using the bundled 108 demonstration maps:
```cmd
python ml/train.py --use-demo-data
```

To train on the genuine WM-811K dataset (`LSWMD.pkl`, ~2.5 GB):
1. Download `LSWMD.pkl` from Kaggle / MIR Corpora.
2. Place it in `ml/data/LSWMD.pkl`.
3. Run:
```cmd
python ml/train.py --dataset-path ml/data/LSWMD.pkl
```
The script trains the model, computes held-out test evaluation metrics (Accuracy, Macro-F1, Confusion Matrix), and saves model weights to `ml/models/wafer_model.joblib`. WaferScope automatically detects the trained weights and activates **TRAINED AI Mode**!

---

## 6. Keyboard Shortcuts
- `Space`: Run Full AI Inference on current wafer
- `D`: Switch to **Dashboard**
- `V`: Switch to **Wafer Visualizer**
- `I`: Switch to **AI Inference Studio**
- `Double-click canvas`: Reset zoom and pan
