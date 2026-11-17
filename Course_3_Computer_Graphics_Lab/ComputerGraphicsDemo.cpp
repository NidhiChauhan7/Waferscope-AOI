/**
 * ============================================================================
 * COMPUTER GRAPHICS LABORATORY IN C++ (COURSE 3 - 2310218L)
 * Master Demonstration Application
 * 
 * Demonstrates:
 * Practical 01: OpenGL Primitives & Color Shading
 * Practical 02: Bresenham's Line & Circle Drawing Algorithms
 * Practical 03: Scan-line Polygon Fill & Boundary Fill Algorithms
 * Practical 04: 2D & 3D Geometric Transformations (Rotation, Zoom/Scaling, Translation)
 * Practical 05: Cohen-Sutherland Line Clipping Algorithm
 * Practical 06: Texture Mapping (Checkerboard, Silicon Micro-Grid, Passivation)
 * CO.6: Phong Illumination Model (Ambient, Diffuse, Specular Lighting)
 * ============================================================================
 */

#include "wafer_graphics_engine.h"
#include <iostream>
#include <string>
#include <vector>
#include <iomanip>

using namespace WaferCG;

int main(int argc, char* argv[]) {
    // ------------------------------------------------------------------------
    // IPC BRIDGE EXECUTION MODES
    // ------------------------------------------------------------------------
    if (argc > 1) {
        std::string mode = argv[1];

        if (mode == "--circle" && argc >= 5) {
            int xc = std::stoi(argv[2]);
            int yc = std::stoi(argv[3]);
            int r = std::stoi(argv[4]);
            auto pts = RasterAlgorithms::midpointCircle(xc, yc, r);
            std::cout << "CIRCLE_POINTS_COUNT=" << pts.size() << "\n";
            std::cout << "ALGORITHM=Midpoint/Bresenham Circle Drawing\n";
            return 0;
        }

        if (mode == "--line" && argc >= 6) {
            int x0 = std::stoi(argv[2]);
            int y0 = std::stoi(argv[3]);
            int x1 = std::stoi(argv[4]);
            int y1 = std::stoi(argv[5]);
            auto pts = RasterAlgorithms::bresenhamLine(x0, y0, x1, y1);
            std::cout << "LINE_POINTS_COUNT=" << pts.size() << "\n";
            std::cout << "ALGORITHM=Bresenham Integer Line Drawing\n";
            return 0;
        }

        if (mode == "--lighting") {
            double angleDeg = (argc >= 3) ? std::stod(argv[2]) : 45.0;
            double rad = angleDeg * PI / 180.0;
            LightingModel::LightSource light;
            light.position = {std::cos(rad) * 200.0, std::sin(rad) * 200.0, 150.0};
            light.intensity = 1.0;

            LightingModel::MaterialProperties mat{0.25, 0.65, 0.40, 16.0}; // Polished Silicon
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

    // ------------------------------------------------------------------------
    // MASTER DEMONSTRATION OF ALL 6 PRACTICALS
    // ------------------------------------------------------------------------
    std::cout << "==========================================================================\n";
    std::cout << "         COMPUTER GRAPHICS LABORATORY IN C++ (COURSE 3 - 2310218L)\n";
    std::cout << "           AI-Based Semiconductor Wafer Inspection Graphics Engine\n";
    std::cout << "==========================================================================\n";

    // 1. PRACTICAL 01: OPENGL PRIMITIVES & SHADING
    std::cout << "\n>>> [PRACTICAL 01] OPENGL PRIMITIVES & COLOR SHADING\n";
    auto notch = OpenGLPrimitives::createWaferNotchTriangle(240, 240, 180, 12);
    std::cout << "Rendered Wafer Alignment Notch Triangle (GL_TRIANGLES):\n";
    std::cout << "  Vertex 0: (" << notch.p0.x << ", " << notch.p0.y << ") | RGB(" << (int)notch.c0.r << "," << (int)notch.c0.g << "," << (int)notch.c0.b << ")\n";
    std::cout << "  Vertex 1: (" << notch.p1.x << ", " << notch.p1.y << ") | RGB(" << (int)notch.c1.r << "," << (int)notch.c1.g << "," << (int)notch.c1.b << ")\n";
    std::cout << "  Vertex 2: (" << notch.p2.x << ", " << notch.p2.y << ") | RGB(" << (int)notch.c2.r << "," << (int)notch.c2.g << "," << (int)notch.c2.b << ")\n";

    // 2. PRACTICAL 02: BRESENHAM LINE & CIRCLE DRAWING
    std::cout << "\n>>> [PRACTICAL 02] BRESENHAM'S LINE & CIRCLE DRAWING ALGORITHM\n";
    int xc = 200, yc = 200, radius = 50;
    auto circlePts = RasterAlgorithms::midpointCircle(xc, yc, radius);
    std::cout << "Midpoint/Bresenham Circle Drawing: Center (" << xc << "," << yc << "), Radius " << radius << "\n";
    std::cout << "  -> Generated " << circlePts.size() << " raster pixels using 8-way integer symmetry.\n";

    auto linePts = RasterAlgorithms::bresenhamLine(50, 50, 120, 90);
    std::cout << "Bresenham Line Drawing (Scratch Defect Vector): (50, 50) to (120, 90)\n";
    std::cout << "  -> Generated " << linePts.size() << " raster pixels using integer decision parameter err = dx - dy.\n";

    // 3. PRACTICAL 03: SCAN-LINE POLYGON FILL ALGORITHM
    std::cout << "\n>>> [PRACTICAL 03] SCAN-LINE POLYGON FILL ALGORITHM\n";
    std::vector<Point2D> defectPoly = {{10, 10}, {50, 15}, {45, 40}, {15, 35}};
    auto filledPixels = PolygonFillAlgorithms::scanlineFill(defectPoly);
    std::cout << "Scan-line Defect Cluster Polygon Fill:\n";
    std::cout << "  -> Successfully rasterized and filled " << filledPixels.size() << " internal defect die pixels.\n";

    // 4. PRACTICAL 04: 2D GEOMETRIC TRANSFORMATIONS (ROTATION & ZOOM)
    std::cout << "\n>>> [PRACTICAL 04] 2D GEOMETRIC TRANSFORMATIONS (ROTATION & ZOOM)\n";
    double testX = 200.0, testY = 150.0;
    double cx = 200.0, cy = 200.0;
    double zoom = 1.5;     // 150% Zoom
    double angle = 90.0;   // 90 Degree Rotation
    auto transformed = GeometricTransformations::transform2D(testX, testY, zoom, angle, 0, 0, cx, cy);
    std::cout << "Transformed Die (" << testX << ", " << testY << ") at Zoom 1.5x and 90 deg Rotation:\n";
    std::cout << "  -> Result Coordinate: (" << std::fixed << std::setprecision(1) << transformed.x << ", " << transformed.y << ")\n";

    // 5. PRACTICAL 05: COHEN-SUTHERLAND LINE CLIPPING
    std::cout << "\n>>> [PRACTICAL 05] COHEN-SUTHERLAND LINE CLIPPING ALGORITHM\n";
    double xmin = 50, ymin = 50, xmax = 250, ymax = 250;
    auto clipRes = LineClipping::clip(20, 100, 280, 100, xmin, ymin, xmax, ymax);
    std::cout << "Clipping Inspection Aperture [" << xmin << "," << ymin << "] to [" << xmax << "," << ymax << "]:\n";
    std::cout << "  Original Vector : (20, 100) -> (280, 100)\n";
    std::cout << "  Clipped Result  : (" << clipRes.x0 << ", " << clipRes.y0 << ") -> (" << clipRes.x1 << ", " << clipRes.y1 << ") [Visible: " << (clipRes.visible ? "YES" : "NO") << "]\n";

    // 6. PRACTICAL 06: TEXTURE MAPPING
    std::cout << "\n>>> [PRACTICAL 06] TEXTURE MAPPING (CHECKERBOARD & SILICON MICRO-GRID)\n";
    auto checkColor = TextureMapping::getCheckerboardTexture(16, 16);
    auto gridColor = TextureMapping::getSiliconGridTexture(8, 8);
    auto passColor = TextureMapping::getPassivationTexture(100, 100);
    std::cout << "Checkerboard Texture (16, 16): RGB(" << (int)checkColor.r << ", " << (int)checkColor.g << ", " << (int)checkColor.b << ")\n";
    std::cout << "Silicon Grid Texture (8, 8)  : RGB(" << (int)gridColor.r << ", " << (int)gridColor.g << ", " << (int)gridColor.b << ")\n";
    std::cout << "Passivation Texture (100,100): RGB(" << (int)passColor.r << ", " << (int)passColor.g << ", " << (int)passColor.b << ")\n";

    // 7. COURSE OUTCOME 6: PHONG ILLUMINATION & LIGHTING MODEL
    std::cout << "\n>>> [CO.6] PHONG ILLUMINATION & LIGHTING MODEL (AMBIENT, DIFFUSE, SPECULAR)\n";
    LightingModel::LightSource light{{150.0, -150.0, 180.0}, 1.0};
    LightingModel::MaterialProperties siliconMat{0.20, 0.60, 0.45, 32.0};
    ColorRGB litDie = LightingModel::calculatePhongShading({34, 197, 94}, {0, 0, 1}, {0, 0, 0}, light, siliconMat);
    std::cout << "Calculated Phong Shading for Pass Silicon Die:\n";
    std::cout << "  -> Diffuse + Specular Highlighted RGB: (" << (int)litDie.r << ", " << (int)litDie.g << ", " << (int)litDie.b << ")\n";

    std::cout << "\n==========================================================================\n";
    std::cout << "   [SUCCESS] ALL COMPUTER GRAPHICS MODULES PROCESSED IN C++!\n";
    std::cout << "==========================================================================\n";

    return 0;
}
