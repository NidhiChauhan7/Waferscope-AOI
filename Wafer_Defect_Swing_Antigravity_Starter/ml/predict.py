"""
WaferScope ML - Single Wafer Inference CLI
Accepts a wafer map, extracts spatial features, executes model inference,
and outputs structured JSON to standard output.
"""

import os
import sys
import json
import time
import argparse
import numpy as np
import joblib

from dataset_utils import (
    DEFECT_CLASSES, IDX_TO_LABEL,
    parse_pipe_grid, extract_spatial_features
)


def predict_wafer(args):
    start_ns = time.perf_counter_ns()

    # 1. Acquire Wafer Grid
    if args.pipe:
        pipe_str = args.pipe
    elif args.file and os.path.exists(args.file):
        with open(args.file, "r") as f:
            lines = [l.strip().replace(",", "").replace(" ", "") for l in f if l.strip() and not l.strip().startswith("#")]
            pipe_str = "|".join(lines)
    else:
        # Read from stdin
        pipe_str = sys.stdin.read().strip()

    if not pipe_str:
        print(json.dumps({"error": "Empty input wafer map", "wafer_id": args.wafer_id}))
        sys.exit(1)

    grid = parse_pipe_grid(pipe_str)
    total_valid = int(np.sum(grid >= 0))
    defective_count = int(np.sum(grid == 1))

    # 2. Extract Features
    features = extract_spatial_features(grid).reshape(1, -1)

    # 3. Model Inference
    model_path = args.model_path
    if not os.path.exists(model_path):
        # Fallback candidate checks
        alt_paths = [
            os.path.join(os.path.dirname(__file__), "models", "wafer_model.joblib"),
            os.path.join("ml", "models", "wafer_model.joblib")
        ]
        for p in alt_paths:
            if os.path.exists(p):
                model_path = p
                break

    if os.path.exists(model_path):
        model = joblib.load(model_path)
        probs = model.predict_proba(features)[0]
        pred_idx = int(np.argmax(probs))
        predicted_class = IDX_TO_LABEL.get(pred_idx, "Unknown")
        confidence = float(probs[pred_idx])

        # If zero defects found, enforce None
        if defective_count == 0:
            predicted_class = "None"
            confidence = 1.0

        scores = {IDX_TO_LABEL[i]: float(probs[i]) for i in range(len(probs))}
        model_mode = "TRAINED"
    else:
        # Transparent rule-based feature baseline if model not trained yet
        defect_ratio = defective_count / float(total_valid) if total_valid > 0 else 0.0
        scores = {c: 0.0 for c in DEFECT_CLASSES}

        if defect_ratio < 0.02:
            predicted_class = "None"
            scores["None"] = 0.95
        elif features[0, 11] < 0.35 and defect_ratio > 0.08:  # Centroid distance close to center
            predicted_class = "Center"
            scores["Center"] = 0.85
        elif features[0, 11] > 0.65:
            predicted_class = "Edge-Loc"
            scores["Edge-Loc"] = 0.80
        elif defect_ratio > 0.40:
            predicted_class = "Near-Full"
            scores["Near-Full"] = 0.90
        else:
            predicted_class = "Random"
            scores["Random"] = 0.70

        confidence = scores[predicted_class]
        model_mode = "DEMO"

    elapsed_ms = (time.perf_counter_ns() - start_ns) / 1_000_000.0
    is_anomalous = (predicted_class != "None") and (defective_count > 0)

    result = {
        "wafer_id": args.wafer_id,
        "is_anomalous": is_anomalous,
        "predicted_class": predicted_class,
        "confidence": round(confidence, 4),
        "scores": {k: round(v, 4) for k, v in scores.items()},
        "defective_die_count": defective_count,
        "total_valid_die_count": total_valid,
        "defect_percentage": round((defective_count / float(total_valid) * 100.0) if total_valid > 0 else 0.0, 2),
        "processing_ms": round(elapsed_ms, 3),
        "model_mode": model_mode
    }

    print(json.dumps(result, indent=2))


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="WaferScope Single Wafer Inference")
    parser.add_argument("--pipe", type=str, default=None, help="Pipe-separated wafer grid")
    parser.add_argument("--file", type=str, default=None, help="File path to wafer map")
    parser.add_argument("--wafer-id", type=str, default="WAFER_INSPECTED", help="Identifier for the wafer")
    parser.add_argument("--model-path", type=str, default="ml/models/wafer_model.joblib", help="Path to trained model")
    args = parser.parse_args()

    predict_wafer(args)

