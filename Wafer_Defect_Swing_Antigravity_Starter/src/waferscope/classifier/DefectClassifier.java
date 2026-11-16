package waferscope.classifier;

import waferscope.model.PredictionResult;
import waferscope.model.WaferMap;

/**
 * Common contract for all wafer defect classifiers in WaferScope.
 * Defines standard prediction behavior across Demo, Python ML, and C++ accelerated pipelines.
 */
public interface DefectClassifier {

    /**
     * Executes classification and anomaly detection on the provided wafer map.
     */
    PredictionResult classify(WaferMap wafer) throws Exception;

    /**
     * Descriptive name of this classifier implementation.
     */
    String getName();

    /**
     * Operation mode: "DEMO" (baseline nearest-neighbor) or "TRAINED" (CNN / Machine Learning).
     */
    String getModelMode();

    /**
     * Whether this classifier is initialized, installed, and ready for inference.
     */
    boolean isAvailable();

    /**
     * Diagnostic explanation of classifier status or setup instructions if unavailable.
     */
    String getStatusDescription();
}
