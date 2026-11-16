# Antigravity Prompt — Build the complete Wafer Defect Detection desktop project

You are working inside the extracted `Wafer_Defect_Swing_Antigravity_Starter` project. Read `README.md`, the Java source, the bundled CSV, and the supplied project requirements below before editing anything.

## Project identity and requirements
Build **AI-Based Semiconductor Wafer Defect Detection System**, an academic integrated project for Java/PSOOP, C++/PL, Computer Graphics and COA. The system must accept a wafer map, preprocess it, detect normal versus anomalous wafers, classify recognized defect patterns, visualize the circular wafer and highlight defective regions, then display a useful result.

The intended real dataset is **WM-811K**. The project document describes 811,457 wafer maps, 172,950 labelled maps and 638,507 unlabelled maps. The nine named labels are:
`Center`, `Donut`, `Edge-Loc`, `Edge-Ring`, `Loc`, `Near-Full`, `Random`, `Scratch`, `None`.
Each map represents die test results as a 2D ternary grid (pass, fail, edge die).

## Non-negotiable honesty rules
1. The current `dataset/wafer_samples.csv` is synthetic demo data only. Never call it WM-811K, real TSMC data, or a real training dataset.
2. The current Java nearest-example classifier is a baseline/demo only. Do not label its similarity score as AI confidence or claim it is a trained CNN.
3. Do not invent accuracy, precision, recall, F1, confusion matrices, dataset counts loaded, training results or hardware performance. Show metrics only after they are computed from a valid held-out test set.
4. If the real WM-811K file is not present, keep the demo fully runnable and clearly label the demo mode. Provide a clear UI message explaining how to import/configure the real dataset.
5. Preserve all nine class names exactly as listed above. In WM-811K the `None` class means no apparent labelled failure pattern; treat it as the normal/no-pattern class for this prototype and document that interpretation.

## Required implementation roadmap
### A. Preserve and improve the Java Swing desktop application
- Keep a polished, responsive Swing interface with a dashboard header, dataset/sample selector, import controls, Analyze action, result summary and wafer visualization.
- Support browsing a CSV dataset and importing one wafer map from a supported file. Validate malformed rows and show friendly errors.
- Display good dies, defective dies and edge/outside-wafer cells distinctly; include a legend.
- Show wafer ID, normal/anomalous status, predicted defect class, number/percentage of defective dies, processing time and model mode (DEMO or TRAINED).
- Keep file I/O and model inference off the Swing event-dispatch thread using `SwingWorker`; keep UI updates on the EDT.
- Use separate OOP classes such as `WaferMap`, `DatasetLoader`, `Preprocessor`, `Prediction`, `DefectClassifier`, `WaferPanel` and `MainFrame`. Add comments explaining responsibilities.
- Add a history table for analyzed wafers and an export-results-to-CSV option.
- Make the UI usable at common laptop resolutions and handle missing files gracefully.

### B. Real WM-811K data pipeline (Python)
- Add a `ml/` directory with a documented Python environment and scripts.
- Inspect the actual downloaded dataset schema first; support its real format (commonly pickle/CSV variants) rather than assuming the synthetic CSV schema.
- Add preprocessing for variable-sized ternary wafer maps: preserve the meaning of pass/fail/edge values, normalize/resize consistently, and document each transformation.
- Create reproducible train/validation/test splits with stratification for labelled classes; prevent leakage between splits.
- Train a CNN classifier for the nine labelled classes. Optionally add an Autoencoder or Isolation Forest for anomaly detection, but do not force a separate anomaly model if a well-documented CNN baseline is more feasible.
- Save model weights, label mapping, preprocessing configuration and evaluation report.
- Evaluate on a held-out test set and calculate accuracy, per-class precision/recall/F1, macro-F1, confusion matrix and class counts. Report class imbalance.
- Add a prediction script that accepts a single wafer map and returns structured JSON: `wafer_id`, `is_anomalous`, `predicted_class`, `scores`, `defective_die_count`, `total_valid_die_count`, `processing_ms`, `model_mode`.
- If model dependencies or dataset are unavailable, fail clearly and retain Java demo mode.

### C. Java ↔ Python integration
- Prefer a simple local process/CLI boundary initially: Java launches the Python prediction script with a validated input file, reads JSON from stdout, captures stderr, applies a timeout and handles non-zero exit codes.
- Do not hardcode machine-specific Python paths. Add a settings/configuration field or documented environment variable.
- Keep the Java demo classifier as a fallback when the trained model is not configured.
- Validate all inputs and never execute arbitrary shell text from user-provided filenames.

### D. Computer Graphics
- Render a circular wafer with individual die cells.
- Distinguish good dies, defective dies and edge cells.
- Add a heatmap/highlight overlay for the predicted defect region. If the model does not produce localization, label the overlay as a visualization of failed dies, not a Grad-CAM explanation.
- Add zoom/reset or a larger view if practical.

### E. C++ / PL integration
- Inspect the supplied `main.c` and Code::Blocks project. It is a Windows Win32 OpenGL rotating-triangle sample, not a wafer processing module. Do not pretend it already detects defects.
- If adding C++, create a separate portable command-line preprocessing utility with a clear input/output contract and tests. Keep it optional so the Java application still runs without a C++ compiler.
- Explain exactly which numerical/preprocessing task C++ accelerates and benchmark it only with measured timings.

### F. COA demonstration
- Record actual inference/preprocessing time using a monotonic clock.
- Provide a short system-information/performance panel describing CPU/GPU mode and memory use only when obtained from real system measurements.
- Explain memory hierarchy, data transfer and parallelism in documentation; do not invent cache sizes or claim GPU acceleration when it is not used.

## Required deliverables
1. A runnable Java Swing application.
2. A clear `README.md` with exact build/run instructions for Windows.
3. Dataset import documentation and schema examples.
4. Python training and inference scripts plus `requirements.txt` (only if implemented).
5. A sample configuration file with no personal machine paths.
6. Unit tests for parsing, preprocessing, class mapping and inference response parsing.
7. A report explaining architecture, limitations and measured evaluation.
8. A final checklist mapping Java/PSOOP, C++/PL, Computer Graphics and COA contributions.

## Work process
1. First inspect the repository and list what is already implemented.
2. Compile and run the current Java app before refactoring; fix any compilation/runtime issues.
3. Implement in small stages and test each stage.
4. Never delete the runnable demo fallback.
5. At the end, provide exact commands to run the Java app, configure real WM-811K, train the model and launch inference. Clearly separate completed features from future/unavailable features.
