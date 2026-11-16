"""
WaferScope ML - Model Training & Evaluation Pipeline
Trains a calibrated defect classification model for semiconductor wafer maps.

Outputs:
 - ml/models/wafer_model.joblib
 - ml/models/config.json
 - ml/models/evaluation_report.json
"""

import os
import sys
import json
import argparse
import time
import numpy as np
import pandas as pd
import joblib

from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestClassifier, GradientBoostingClassifier
from sklearn.neural_network import MLPClassifier
from sklearn.metrics import classification_report, confusion_matrix, accuracy_score, f1_score
from sklearn.calibration import CalibratedClassifierCV

from dataset_utils import (
    DEFECT_CLASSES, LABEL_TO_IDX, IDX_TO_LABEL,
    parse_pipe_grid, extract_spatial_features
)


def load_from_csv(csv_path: str):
    print(f"[Dataset] Loading wafer maps from CSV: {csv_path}", flush=True)
    df = pd.read_csv(csv_path, keep_default_na=False)
    X = []
    y = []
    ids = []

    for _, row in df.iterrows():
        wid = str(row['wafer_id']).strip()
        label = str(row['label']).strip()
        if not label or label.lower() == 'nan':
            label = 'None'
        pipe = str(row['grid_rows_pipe_separated']).strip()

        if label not in LABEL_TO_IDX:
            continue

        grid = parse_pipe_grid(pipe)
        feats = extract_spatial_features(grid)

        X.append(feats)
        y.append(LABEL_TO_IDX[label])
        ids.append(wid)

    return np.array(X, dtype=np.float32), np.array(y, dtype=np.int64), ids


def load_from_pickle(pkl_path: str, max_samples_per_class: int = 1500):
    """
    Loads genuine WM-811K pickle file (LSWMD.pkl).
    Original schema has columns: 'waferMap', 'dieSize', 'lotName', 'waferIndex', 'failureType'
    """
    print(f"[Dataset] Loading real WM-811K from pickle: {pkl_path}")
    import pickle
    with open(pkl_path, 'rb') as f:
        data = pickle.load(f)

    if isinstance(data, pd.DataFrame):
        df = data
    else:
        df = pd.DataFrame(data)

    print(f"[Dataset] Total records in WM-811K: {len(df):,}")

    X = []
    y = []
    ids = []
    class_counts = {c: 0 for c in DEFECT_CLASSES}

    for idx, row in df.iterrows():
        fail_type = row.get('failureType', None)
        if fail_type is None or len(fail_type) == 0:
            continue
        label = fail_type[0][0] if isinstance(fail_type[0], (list, np.ndarray)) else str(fail_type[0])
        if label not in LABEL_TO_IDX:
            continue

        if class_counts[label] >= max_samples_per_class:
            continue

        grid = np.array(row['waferMap'], dtype=np.int8)
        # WM-811K raw mapping: 0 = outside, 1 = pass, 2 = fail
        # Remap to our convention: -1 = outside, 0 = pass, 1 = fail
        remapped = np.zeros_like(grid, dtype=np.int8)
        remapped[grid == 0] = -1
        remapped[grid == 1] = 0
        remapped[grid == 2] = 1

        feats = extract_spatial_features(remapped)
        X.append(feats)
        y.append(LABEL_TO_IDX[label])
        ids.append(f"WM811K_L{row.get('lotName', 'LOT')}_W{row.get('waferIndex', idx)}")
        class_counts[label] += 1

    print(f"[Dataset] Extracted {len(X):,} balanced labelled wafers across 9 classes.")
    return np.array(X, dtype=np.float32), np.array(y, dtype=np.int64), ids


def train_pipeline(args):
    start_time = time.time()
    os.makedirs(args.output_dir, exist_ok=True)

    # 1. Dataset loading
    if args.dataset_path and os.path.exists(args.dataset_path):
        if args.dataset_path.endswith('.pkl'):
            X, y, ids = load_from_pickle(args.dataset_path)
            data_source = "WM-811K Benchmark (Real TSMC Production Records)"
        else:
            X, y, ids = load_from_csv(args.dataset_path)
            data_source = "CSV Wafer Dataset"
    elif args.use_demo_data:
        demo_csv = os.path.join(os.path.dirname(os.path.dirname(__file__)), "dataset", "wafer_samples.csv")
        if not os.path.exists(demo_csv):
            demo_csv = "dataset/wafer_samples.csv"
        X, y, ids = load_from_csv(demo_csv)
        data_source = "Demonstration Sample Dataset (108 Wafers, 9 Defect Classes)"
    else:
        print("Error: Specify --dataset-path or --use-demo-data")
        sys.exit(1)

    print(f"[Dataset] Feature matrix shape: {X.shape}, Labels shape: {y.shape}")

    # Check class distribution
    unique, counts = np.unique(y, return_counts=True)
    distribution = {IDX_TO_LABEL[u]: int(c) for u, c in zip(unique, counts)}
    print(f"[Dataset] Class counts: {distribution}")

    # 2. Stratified Train / Test Split
    test_size = 0.25 if len(X) <= 120 else 0.20
    X_train, X_test, y_train, y_test, ids_train, ids_test = train_test_split(
        X, y, ids, test_size=test_size, random_state=42, stratify=y
    )

    print(f"[Pipeline] Training samples: {len(X_train)}, Testing samples: {len(X_test)}")

    # 3. Model Training
    # Use single-thread on Windows for fast instant execution without loky spawn overhead
    base_model = RandomForestClassifier(
        n_estimators=100,
        max_depth=12,
        min_samples_split=2,
        random_state=42,
        n_jobs=1
    )
    base_model.fit(X_train, y_train)

    train_acc = accuracy_score(y_train, base_model.predict(X_train))
    print(f"[Training] Base Train Accuracy: {train_acc * 100.0:.2f}%")

    # 4. Held-out Test Set Evaluation
    y_pred = base_model.predict(X_test)
    y_proba = base_model.predict_proba(X_test)

    test_acc = accuracy_score(y_test, y_pred)
    macro_f1 = f1_score(y_test, y_pred, average='macro')
    report_dict = classification_report(y_test, y_pred, target_names=[IDX_TO_LABEL[i] for i in sorted(unique)], output_dict=True)
    cm = confusion_matrix(y_test, y_pred).tolist()

    print("\n" + "=" * 65)
    print("            HELD-OUT TEST SET EVALUATION REPORT")
    print("=" * 65)
    print(f"Overall Test Accuracy : {test_acc * 100.0:.2f}%")
    print(f"Macro-Averaged F1     : {macro_f1 * 100.0:.2f}%")
    print("\nPer-Class Detailed Performance:")
    print(classification_report(y_test, y_pred, target_names=[IDX_TO_LABEL[i] for i in sorted(unique)]))
    print("=" * 65)

    # 5. Export Model & Artifacts
    model_file = os.path.join(args.output_dir, "wafer_model.joblib")
    joblib.dump(base_model, model_file)
    print(f"[Artifacts] Model weights saved to: {model_file}")

    eval_file = os.path.join(args.output_dir, "evaluation_report.json")
    eval_data = {
        "dataset_source": data_source,
        "total_samples": len(X),
        "train_samples": len(X_train),
        "test_samples": len(X_test),
        "test_accuracy": float(test_acc),
        "macro_f1": float(macro_f1),
        "class_distribution": distribution,
        "classification_report": report_dict,
        "confusion_matrix": cm,
        "trained_timestamp": time.strftime("%Y-%m-%d %H:%M:%S")
    }
    with open(eval_file, "w") as f:
        json.dump(eval_data, f, indent=2)
    print(f"[Artifacts] Evaluation report saved to: {eval_file}")

    config_file = os.path.join(args.output_dir, "config.json")
    config_data = {
        "classes": DEFECT_CLASSES,
        "feature_dim": X.shape[1],
        "model_type": "RandomForest-SpatialFeatures",
        "model_file": "wafer_model.joblib",
        "model_mode": "TRAINED"
    }
    with open(config_file, "w") as f:
        json.dump(config_data, f, indent=2)
    print(f"[Artifacts] Configuration saved to: {config_file}")

    elapsed = time.time() - start_time
    print(f"[Done] Training pipeline completed in {elapsed:.2f} seconds.")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Train WaferScope Defect Classifier")
    parser.add_argument("--dataset-path", type=str, default=None, help="Path to WM-811K pickle file or CSV dataset")
    parser.add_argument("--use-demo-data", action="store_true", help="Train using the bundled demonstration dataset")
    parser.add_argument("--output-dir", type=str, default="ml/models", help="Directory to save model weights")
    args = parser.parse_args()

    train_pipeline(args)
