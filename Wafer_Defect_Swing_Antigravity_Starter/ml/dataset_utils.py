"""
WaferScope ML - Dataset Utilities & Preprocessing Pipeline
Handles variable-size ternary wafer maps for the WM-811K benchmark dataset
and synthetic demonstration datasets.

Ternary encoding:
 -1 : Outside wafer substrate
  0 : Good die (Pass)
  1 : Defective die (Fail)
"""

import os
import json
import numpy as np
from typing import Tuple, List, Dict, Any, Optional

DEFECT_CLASSES = [
    "Center", "Donut", "Edge-Loc", "Edge-Ring",
    "Loc", "Near-Full", "Random", "Scratch", "None"
]

LABEL_TO_IDX = {name: idx for idx, name in enumerate(DEFECT_CLASSES)}
IDX_TO_LABEL = {idx: name for idx, name in enumerate(DEFECT_CLASSES)}


def parse_pipe_grid(pipe_str: str) -> np.ndarray:
    """Parses a pipe-separated string into a 2D ternary numpy array."""
    rows = pipe_str.strip().split('|')
    grid = []
    for r in rows:
        row = []
        for ch in r.strip():
            if ch in ('E', 'e', '2'):
                row.append(-1)
            elif ch == '1':
                row.append(1)
            elif ch == '0':
                row.append(0)
            else:
                raise ValueError(f"Invalid character '{ch}' in wafer map")
        grid.append(row)
    return np.array(grid, dtype=np.int8)


def grid_to_pipe_str(grid: np.ndarray) -> str:
    """Converts a 2D ternary numpy array into a pipe-separated string."""
    rows = []
    for r in range(grid.shape[0]):
        row_chars = []
        for c in range(grid.shape[1]):
            val = grid[r, c]
            row_chars.append('E' if val < 0 else ('1' if val == 1 else '0'))
        rows.append("".join(row_chars))
    return "|".join(rows)


def extract_spatial_features(grid: np.ndarray) -> np.ndarray:
    """
    Extracts physically motivated spatial features for wafer defect pattern classification:
    1. Overall Defect Density and Yield
    2. Radial Defect Distribution (distance from wafer center in 5 concentric rings)
    3. Angular Defect Distribution (octant defect distribution in 8 directions: 0 to 360 deg)
    4. Centroid displacement from geometric wafer center (for Center vs Loc vs Edge)
    5. Second-order spatial moments / inertia (for Donut vs Center vs Scratch)
    6. Linear defect projection / Radon approximation (for Scratch detection)
    7. Connected defect cluster size distribution
    """
    rows, cols = grid.shape
    valid_mask = (grid >= 0)
    defect_mask = (grid == 1)

    total_valid = int(np.sum(valid_mask))
    total_defects = int(np.sum(defect_mask))

    if total_valid == 0:
        return np.zeros(28, dtype=np.float32)

    defect_ratio = total_defects / float(total_valid)
    features = [defect_ratio, float(total_defects), float(total_valid)]

    # Normalized coordinates [-1, 1] relative to center
    center_y = (rows - 1) / 2.0
    center_x = (cols - 1) / 2.0
    max_radius = np.sqrt(center_x ** 2 + center_y ** 2)

    y_coords, x_coords = np.indices((rows, cols))
    norm_y = (y_coords - center_y) / (center_y if center_y > 0 else 1.0)
    norm_x = (x_coords - center_x) / (center_x if center_x > 0 else 1.0)
    r_coords = np.sqrt(norm_x ** 2 + norm_y ** 2)
    theta_coords = np.arctan2(norm_y, norm_x)  # [-pi, pi]

    # 1. Radial Defect Distribution across 5 concentric zones
    ring_bins = [0.0, 0.25, 0.50, 0.75, 1.0, 1.5]
    for i in range(5):
        r_low, r_high = ring_bins[i], ring_bins[i + 1]
        ring_mask = valid_mask & (r_coords >= r_low) & (r_coords < r_high)
        ring_valid = np.sum(ring_mask)
        if ring_valid > 0:
            ring_defects = np.sum(defect_mask & ring_mask)
            features.append(ring_defects / float(ring_valid))
        else:
            features.append(0.0)

    # 2. Angular Defect Distribution across 8 octants
    octant_bins = np.linspace(-np.pi, np.pi, 9)
    for i in range(8):
        t_low, t_high = octant_bins[i], octant_bins[i + 1]
        oct_mask = valid_mask & (theta_coords >= t_low) & (theta_coords < t_high)
        oct_valid = np.sum(oct_mask)
        if oct_valid > 0:
            oct_defects = np.sum(defect_mask & oct_mask)
            features.append(oct_defects / float(oct_valid))
        else:
            features.append(0.0)

    # 3. Defect Centroid & Dispersion
    if total_defects > 0:
        defect_xs = norm_x[defect_mask]
        defect_ys = norm_y[defect_mask]
        mean_x = float(np.mean(defect_xs))
        mean_y = float(np.mean(defect_ys))
        var_x = float(np.var(defect_xs))
        var_y = float(np.var(defect_ys))
        centroid_dist = np.sqrt(mean_x ** 2 + mean_y ** 2)
    else:
        mean_x, mean_y, var_x, var_y, centroid_dist = 0.0, 0.0, 0.0, 0.0, 0.0

    features.extend([mean_x, mean_y, centroid_dist, var_x, var_y])

    # 4. Scratch & Linearity Indicator (Radon-like projection along 4 primary axes)
    # Project defects along rows, columns, diagonal, anti-diagonal
    if total_defects > 0:
        row_proj = np.sum(defect_mask, axis=1)
        col_proj = np.sum(defect_mask, axis=0)
        max_row_density = float(np.max(row_proj)) / float(cols)
        max_col_density = float(np.max(col_proj)) / float(rows)
        # Principal axis ratio (eigenvalues of defect coordinates)
        cov = np.cov(np.vstack([defect_xs, defect_ys])) if len(defect_xs) > 1 else np.zeros((2, 2))
        eigvals = np.linalg.eigvals(cov) if cov.shape == (2, 2) else [0.0, 0.0]
        eig_ratio = float(np.max(eigvals) / (np.min(eigvals) + 1e-5))
    else:
        max_row_density, max_col_density, eig_ratio = 0.0, 0.0, 0.0

    features.extend([max_row_density, max_col_density, np.clip(eig_ratio, 0.0, 50.0)])

    # 5. Local clustering density (defects with defective 3x3 neighbors)
    neighbor_defects = 0
    for r in range(rows):
        for c in range(cols):
            if defect_mask[r, c]:
                r_min, r_max = max(0, r - 1), min(rows, r + 2)
                c_min, c_max = max(0, c - 1), min(cols, c + 2)
                sub = defect_mask[r_min:r_max, c_min:c_max]
                if np.sum(sub) > 1:
                    neighbor_defects += 1

    cluster_ratio = (neighbor_defects / float(total_defects)) if total_defects > 0 else 0.0
    features.append(cluster_ratio)

    return np.array(features, dtype=np.float32)


def resize_wafer(grid: np.ndarray, target_size: Tuple[int, int] = (32, 32)) -> np.ndarray:
    """
    Resizes variable-size wafer maps to target dimensions while preserving ternary values.
    Uses nearest-neighbor interpolation to ensure only {-1, 0, 1} are produced.
    """
    src_h, src_w = grid.shape
    dst_h, dst_w = target_size

    r_indices = (np.arange(dst_h) * (src_h / dst_h)).astype(int)
    c_indices = (np.arange(dst_w) * (src_w / dst_w)).astype(int)

    return grid[np.ix_(r_indices, c_indices)]

