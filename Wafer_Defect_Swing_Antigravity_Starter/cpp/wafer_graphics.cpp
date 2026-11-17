/**
 * WaferScope C++ High-Performance Computer Graphics Engine
 * 
 * Implements fundamental Computer Graphics algorithms in C++:
 * 1. Midpoint Circle Drawing Algorithm (Integer arithmetic & 8-way symmetry)
 * 2. Bresenham's Integer Line Drawing Algorithm (Wafer notch & scratch defects)
 * 3. 2D Geometric Transformations (Translation, Scaling/Zoom, Rotation around wafer center)
 * 4. Cohen-Sutherland Line Clipping Algorithm (4-bit region outcodes)
 * 5. Scan-Line Polygon Fill Algorithm for defect clusters
 * 6. Texture Mapping (Checkerboard, Silicon Micro-Grid, Passivation)
 * 7. Phong Illumination Lighting Model (Ambient, Diffuse, Specular reflection)
 */

#include "wafer_graphics_engine.h"
#include <iostream>
#include <vector>
#include <string>
#include <cmath>
#include <fstream>
#include <chrono>
#include <algorithm>

using namespace WaferCG;

int main(int argc, char* argv[]) {
    if (argc > 1) {
        std::string mode = argv[1];

        if (mode == "--circle" && argc >= 5) {
            int xc = std::stoi(argv[2]);
            int yc = std::stoi(argv[3]);
            int r = std::stoi(argv[4]);
            auto pts = RasterAlgorithms::midpointCircle(xc, yc, r);
            std::cout << "CIRCLE_POINTS_COUNT=" << pts.size() << "\n";
            std::cout << "ENGINE=C++ Computer Graphics Engine\n";
            return 0;
        }

        if (mode == "--line" && argc >= 6) {
            int x0 = std::stoi(argv[2]);
            int y0 = std::stoi(argv[3]);
            int x1 = std::stoi(argv[4]);
            int y1 = std::stoi(argv[5]);
            auto pts = RasterAlgorithms::bresenhamLine(x0, y0, x1, y1);
            std::cout << "LINE_POINTS_COUNT=" << pts.size() << "\n";
            std::cout << "ENGINE=C++ Computer Graphics Engine\n";
            return 0;
        }

        if (mode == "--lighting") {
            double angleDeg = (argc >= 3) ? std::stod(argv[2]) : 45.0;
            double rad = angleDeg * PI / 180.0;
            LightingModel::LightSource light;
            light.position = {std::cos(rad) * 200.0, std::sin(rad) * 200.0, 150.0};
            light.intensity = 1.0;

            LightingModel::MaterialProperties mat{0.25, 0.65, 0.40, 16.0};
            ColorRGB base{226, 232, 240};
            Vector3D normal{0, 0, 1};
            Vector3D pos{0, 0, 0};
            ColorRGB lit = LightingModel::calculatePhongShading(base, normal, pos, light, mat);

            std::cout << "PHONG_LIGHTING_RESULT\n";
            std::cout << "LIGHT_ANGLE=" << angleDeg << "\n";
            std::cout << "OUTPUT_RGB=" << (int)lit.r << "," << (int)lit.g << "," << (int)lit.b << "\n";
            return 0;
        }

        if (mode == "--clip" && argc >= 10) {
            double x0 = std::stod(argv[2]);
            double y0 = std::stod(argv[3]);
            double x1 = std::stod(argv[4]);
            double y1 = std::stod(argv[5]);
            double xmin = std::stod(argv[6]);
            double ymin = std::stod(argv[7]);
            double xmax = std::stod(argv[8]);
            double ymax = std::stod(argv[9]);
            auto res = LineClipping::clip(x0, y0, x1, y1, xmin, ymin, xmax, ymax);
            std::cout << "COHEN_SUTHERLAND_RESULT\n";
            std::cout << "VISIBLE=" << (res.visible ? "true" : "false") << "\n";
            std::cout << "CLIPPED_COORDS=" << res.x0 << "," << res.y0 << "," << res.x1 << "," << res.y1 << "\n";
            return 0;
        }
    }

    std::cout << "WaferScope C++ High-Performance Computer Graphics Engine Active.\n";
    std::cout << "Features: Bresenham, Midpoint Circle, Scanline Fill, 2D Transformations, Clipping, Textures, Phong Lighting.\n";
    return 0;
}
