# Alignment with the supplied project files

- The project plan and presentation define the workflow: wafer-map input → preprocessing → AI model → anomaly detection → defect classification → visualization/heatmap → result.
- The nine categories are Center, Donut, Edge-Loc, Edge-Ring, Loc, Near-Full, Random, Scratch and None.
- Java is assigned the application interface, input/file handling, OOP classes and result display.
- C++ is proposed for efficient processing and numerical operations.
- Computer Graphics is assigned circular wafer/die visualization and defect highlighting.
- COA covers CPU/GPU, memory hierarchy, data transfer, parallel processing and performance.
- The supplied `main.c` / Code::Blocks project is a Win32 OpenGL rotating triangle example. It is not a wafer detector; it can be adapted for graphics learning but does not currently implement the project pipeline.
- The actual WM-811K dataset was not among the uploaded files. This starter therefore includes synthetic test maps, not a copy of WM-811K.
