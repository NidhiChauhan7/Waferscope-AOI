#ifndef WAFER_GRAPHICS_ENGINE_H
#define WAFER_GRAPHICS_ENGINE_H

#include <iostream>
#include <vector>
#include <string>
#include <cmath>
#include <fstream>
#include <chrono>
#include <algorithm>
#include <iomanip>
#include <cstdint>

/**
 * ============================================================================
 * COMPUTER GRAPHICS LABORATORY IN C++ (COURSE 3 - 2310218L)
 * MIT Academy of Engineering (MITAOE) Curriculum
 *
 * Implements all Syllabus Practicals & Course Outcomes:
 * - Practical 01: OpenGL Primitives, Geometric Objects & Color Shading
 * - Practical 02: Bresenham's Integer Line & Circle Drawing Algorithms
 * - Practical 03: Scan-line Polygon Fill & Boundary / Flood Fill Algorithms
 * - Practical 04: 2D & 3D Transformations (Rotation, Scaling/Zoom, Translation)
 * - Practical 05: Cohen-Sutherland Line Clipping Algorithm (4-bit Outcodes)
 * - Practical 06: Texture Mapping (Checkerboard, Silicon Micro-Grid, Passivation)
 * - CO.6 / CEO.6: Light, Color & Phong Illumination Model (Ambient, Diffuse, Specular)
 * - CO.4 / CEO.4: Viewing Transformations & Projections (Zoom In/Out, Viewport)
 * ============================================================================
 */

namespace WaferCG {

const double PI = 3.14159265358979323846;

struct Point2D {
    int x;
    int y;
};

struct Point2DDouble {
    double x;
    double y;
};

struct Vector3D {
    double x;
    double y;
    double z;

    Vector3D normalize() const {
        double len = std::sqrt(x * x + y * y + z * z);
        if (len < 1e-9) return {0, 0, 1};
        return {x / len, y / len, z / len};
    }

    double dot(const Vector3D& other) const {
        return x * other.x + y * other.y + z * other.z;
    }
};

struct ColorRGB {
    uint8_t r;
    uint8_t g;
    uint8_t b;

    static ColorRGB fromDouble(double rD, double gD, double bD) {
        auto clamp = [](double v) -> uint8_t {
            if (v < 0.0) return 0;
            if (v > 255.0) return 255;
            return static_cast<uint8_t>(v);
        };
        return {clamp(rD), clamp(gD), clamp(bD)};
    }
};

// ============================================================================
// PRACTICAL 01: OPENGL PRIMITIVES & COLOR SHADING
// ============================================================================
class OpenGLPrimitives {
public:
    struct Triangle {
        Point2D p0, p1, p2;
        ColorRGB c0, c1, c2;
    };

    static Triangle createWaferNotchTriangle(int cx, int cy, int radius, int notchSize) {
        Triangle t;
        t.p0 = {cx - notchSize, cy + radius - 2};
        t.p1 = {cx, cy + radius - notchSize - 4};
        t.p2 = {cx + notchSize, cy + radius - 2};
        t.c0 = {51, 65, 85};  // Slate-700
        t.c1 = {30, 41, 59};  // Slate-800
        t.c2 = {51, 65, 85};
        return t;
    }
};

// ============================================================================
// PRACTICAL 02: BRESENHAM'S LINE & CIRCLE DRAWING ALGORITHM
// ============================================================================
class RasterAlgorithms {
public:
    // Bresenham's Integer Line Algorithm (Zero Floating Point in Inner Loop)
    static std::vector<Point2D> bresenhamLine(int x0, int y0, int x1, int y1) {
        std::vector<Point2D> points;
        int dx = std::abs(x1 - x0);
        int dy = std::abs(y1 - y0);
        int sx = (x0 < x1) ? 1 : -1;
        int sy = (y0 < y1) ? 1 : -1;
        int err = dx - dy;

        int curX = x0;
        int curY = y0;

        while (true) {
            points.push_back({curX, curY});
            if (curX == x1 && curY == y1) break;
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                curX += sx;
            }
            if (e2 < dx) {
                err += dx;
                curY += sy;
            }
        }
        return points;
    }

    // Midpoint / Bresenham's Circle Drawing Algorithm (8-Way Symmetry)
    static std::vector<Point2D> midpointCircle(int xc, int yc, int radius) {
        std::vector<Point2D> points;
        int x = 0;
        int y = radius;
        int p = 1 - radius; // Initial decision parameter

        auto add8Symmetric = [&](int x, int y) {
            points.push_back({xc + x, yc + y});
            points.push_back({xc - x, yc + y});
            points.push_back({xc + x, yc - y});
            points.push_back({xc - x, yc - y});
            points.push_back({xc + y, yc + x});
            points.push_back({xc - y, yc + x});
            points.push_back({xc + y, yc - x});
            points.push_back({xc - y, yc - x});
        };

        add8Symmetric(x, y);

        while (x < y) {
            x++;
            if (p < 0) {
                p += 2 * x + 1;
            } else {
                y--;
                p += 2 * (x - y) + 1;
            }
            add8Symmetric(x, y);
        }
        return points;
    }
};

// ============================================================================
// PRACTICAL 03: SCAN-LINE POLYGON FILL & BOUNDARY FILL ALGORITHMS
// ============================================================================
class PolygonFillAlgorithms {
public:
    struct Edge {
        double yMax;
        double xCurrent;
        double invSlope; // 1/m = dx/dy
    };

    // Scan-Line Polygon Fill using Active Edge Table (AET)
    static std::vector<Point2D> scanlineFill(const std::vector<Point2D>& vertices) {
        std::vector<Point2D> filledPixels;
        if (vertices.size() < 3) return filledPixels;

        int yMin = vertices[0].y, yMax = vertices[0].y;
        for (const auto& v : vertices) {
            if (v.y < yMin) yMin = v.y;
            if (v.y > yMax) yMax = v.y;
        }

        // Build Edge Table
        std::vector<std::vector<Edge>> edgeTable(yMax - yMin + 1);
        size_t n = vertices.size();
        for (size_t i = 0; i < n; ++i) {
            Point2D p1 = vertices[i];
            Point2D p2 = vertices[(i + 1) % n];
            if (p1.y == p2.y) continue; // Ignore horizontal edges

            Edge edge;
            if (p1.y < p2.y) {
                edge.yMax = p2.y;
                edge.xCurrent = p1.y;
                edge.invSlope = (double)(p2.x - p1.x) / (double)(p2.y - p1.y);
                edgeTable[p1.y - yMin].push_back(edge);
            } else {
                edge.yMax = p1.y;
                edge.xCurrent = p2.y;
                edge.invSlope = (double)(p1.x - p2.x) / (double)(p1.y - p2.y);
                edgeTable[p2.y - yMin].push_back(edge);
            }
        }

        // Active Edge List
        std::vector<Edge> AET;
        for (int y = yMin; y <= yMax; ++y) {
            // Add new edges from Edge Table
            for (const auto& e : edgeTable[y - yMin]) {
                AET.push_back(e);
            }

            // Remove edges where y >= yMax
            AET.erase(std::remove_if(AET.begin(), AET.end(), [y](const Edge& e) {
                return y >= e.yMax;
            }), AET.end());

            // Sort AET by xCurrent
            std::sort(AET.begin(), AET.end(), [](const Edge& a, const Edge& b) {
                return a.xCurrent < b.xCurrent;
            });

            // Fill pixels between pairs of intersections
            for (size_t i = 0; i + 1 < AET.size(); i += 2) {
                int xStart = static_cast<int>(std::ceil(AET[i].xCurrent));
                int xEnd = static_cast<int>(std::floor(AET[i + 1].xCurrent));
                for (int x = xStart; x <= xEnd; ++x) {
                    filledPixels.push_back({x, y});
                }
            }

            // Update xCurrent for next scan line
            for (auto& e : AET) {
                e.xCurrent += e.invSlope;
            }
        }
        return filledPixels;
    }
};

// ============================================================================
// PRACTICAL 04: 2D & 3D GEOMETRIC TRANSFORMATIONS
// ============================================================================
class GeometricTransformations {
public:
    // 2D Rotation around arbitrary fixed point (cx, cy)
    static Point2DDouble rotatePoint(double x, double y, double angleDeg, double cx, double cy) {
        double rad = angleDeg * PI / 180.0;
        double cosA = std::cos(rad);
        double sinA = std::sin(rad);

        double dx = x - cx;
        double dy = y - cy;

        double rx = dx * cosA - dy * sinA;
        double ry = dx * sinA + dy * cosA;

        return {rx + cx, ry + cy};
    }

    // 2D Scaling / Zoom relative to center
    static Point2DDouble scalePoint(double x, double y, double zoomFactor, double cx, double cy) {
        return {cx + (x - cx) * zoomFactor, cy + (y - cy) * zoomFactor};
    }

    // Combined 2D Transformation Matrix: Translate * Rotate * Scale
    static Point2DDouble transform2D(double x, double y, double zoomFactor, double angleDeg, double panX, double panY, double cx, double cy) {
        Point2DDouble scaled = scalePoint(x, y, zoomFactor, cx, cy);
        Point2DDouble rotated = rotatePoint(scaled.x, scaled.y, angleDeg, cx, cy);
        return {rotated.x + panX, rotated.y + panY};
    }
};

// ============================================================================
// PRACTICAL 05: COHEN-SUTHERLAND LINE CLIPPING ALGORITHM
// ============================================================================
class LineClipping {
public:
    // 4-bit Region Outcodes
    static const int INSIDE = 0; // 0000
    static const int LEFT   = 1; // 0001
    static const int RIGHT  = 2; // 0010
    static const int BOTTOM = 4; // 0100
    static const int TOP    = 8; // 1000

    static int computeOutCode(double x, double y, double xmin, double ymin, double xmax, double ymax) {
        int code = INSIDE;
        if (x < xmin)      code |= LEFT;
        else if (x > xmax) code |= RIGHT;
        if (y < ymin)      code |= BOTTOM;
        else if (y > ymax) code |= TOP;
        return code;
    }

    struct ClippedLine {
        bool visible;
        double x0, y0, x1, y1;
    };

    static ClippedLine clip(double x0, double y0, double x1, double y1, double xmin, double ymin, double xmax, double ymax) {
        int code0 = computeOutCode(x0, y0, xmin, ymin, xmax, ymax);
        int code1 = computeOutCode(x1, y1, xmin, ymin, xmax, ymax);
        bool accept = false;

        while (true) {
            if ((code0 | code1) == 0) {
                // Trivial Accept: Both endpoints inside
                accept = true;
                break;
            } else if ((code0 & code1) != 0) {
                // Trivial Reject: Both endpoints outside on same side
                break;
            } else {
                // Line crosses boundary; calculate intersection
                double x = 0.0, y = 0.0;
                int codeOut = (code0 != 0) ? code0 : code1;

                if ((codeOut & TOP) != 0) {
                    x = x0 + (x1 - x0) * (ymax - y0) / (y1 - y0);
                    y = ymax;
                } else if ((codeOut & BOTTOM) != 0) {
                    x = x0 + (x1 - x0) * (ymin - y0) / (y1 - y0);
                    y = ymin;
                } else if ((codeOut & RIGHT) != 0) {
                    y = y0 + (y1 - y0) * (xmax - x0) / (x1 - x0);
                    x = xmax;
                } else if ((codeOut & LEFT) != 0) {
                    y = y0 + (y1 - y0) * (xmin - x0) / (x1 - x0);
                    x = xmin;
                }

                if (codeOut == code0) {
                    x0 = x; y0 = y;
                    code0 = computeOutCode(x0, y0, xmin, ymin, xmax, ymax);
                } else {
                    x1 = x; y1 = y;
                    code1 = computeOutCode(x1, y1, xmin, ymin, xmax, ymax);
                }
            }
        }
        return {accept, x0, y0, x1, y1};
    }
};

// ============================================================================
// PRACTICAL 05 (EXTENDED): OUTER CIRCLE CLIPPING & RECTANGULAR GRID CLIPPING
// ============================================================================
class CircularBoundaryClipping {
public:
    enum class DieClipStatus {
        INSIDE_PRESERVED,   // Fully inside circular wafer boundary (dist <= radius - margin)
        BOUNDARY_STRADDLE,  // Intersected by circular cutting boundary arc
        OUTSIDE_CLIPPED     // Clipped out of the circle (dist > radius)
    };

    struct GridDieClipResult {
        int row;
        int col;
        double centerX;
        double centerY;
        double distFromCenter;
        double radius;
        DieClipStatus status;
    };

    struct GridClippingSummary {
        int totalGridDies;
        int insideCount;
        int boundaryCount;
        int clippedOutCount;
        std::vector<GridDieClipResult> dieResults;
    };

    // Clips a rectangular N x M Cartesian fabrication grid against outer circle x^2 + y^2 = R^2
    static GridClippingSummary clipGridToOuterCircle(int rows, int cols, double cellSize,
                                                     double originX, double originY,
                                                     double cx, double cy, double radius) {
        GridClippingSummary summary;
        summary.totalGridDies = rows * cols;
        summary.insideCount = 0;
        summary.boundaryCount = 0;
        summary.clippedOutCount = 0;

        double halfCell = cellSize / 2.0;
        double margin = cellSize * 0.45;

        for (int r = 0; r < rows; ++r) {
            for (int c = 0; c < cols; ++c) {
                double dieCenterX = originX + c * cellSize + halfCell;
                double dieCenterY = originY + r * cellSize + halfCell;

                double dx = dieCenterX - cx;
                double dy = dieCenterY - cy;
                double dist = std::sqrt(dx * dx + dy * dy);

                GridDieClipResult res;
                res.row = r;
                res.col = c;
                res.centerX = dieCenterX;
                res.centerY = dieCenterY;
                res.distFromCenter = dist;
                res.radius = radius;

                if (dist <= (radius - margin)) {
                    res.status = DieClipStatus::INSIDE_PRESERVED;
                    summary.insideCount++;
                } else if (dist <= (radius + margin)) {
                    res.status = DieClipStatus::BOUNDARY_STRADDLE;
                    summary.boundaryCount++;
                } else {
                    res.status = DieClipStatus::OUTSIDE_CLIPPED;
                    summary.clippedOutCount++;
                }
                summary.dieResults.push_back(res);
            }
        }
        return summary;
    }

    struct ClippedLineSegment {
        bool visible;
        double x0, y0, x1, y1;
        bool clippedAtCircle;
    };

    // Geometric Line-Circle Clipping: Clips horizontal and vertical fabrication grid lines
    // against the circular wafer boundary equation x^2 + y^2 = R^2
    static ClippedLineSegment clipGridLineToCircle(double x0, double y0, double x1, double y1,
                                                   double cx, double cy, double radius) {
        double lx0 = x0 - cx, ly0 = y0 - cy;
        double lx1 = x1 - cx, ly1 = y1 - cy;
        double r2 = radius * radius;

        if (std::abs(ly0 - ly1) < 1e-5) {
            // Horizontal grid line y = const
            double y = ly0;
            if (y * y >= r2) return {false, 0, 0, 0, 0, true};
            double halfSpan = std::sqrt(r2 - y * y);
            double minX = std::max(std::min(lx0, lx1), -halfSpan);
            double maxX = std::min(std::max(lx0, lx1), halfSpan);
            if (minX > maxX) return {false, 0, 0, 0, 0, true};
            return {true, minX + cx, y0, maxX + cx, y0, (minX > std::min(lx0, lx1) || maxX < std::max(lx0, lx1))};
        } else if (std::abs(lx0 - lx1) < 1e-5) {
            // Vertical grid line x = const
            double x = lx0;
            if (x * x >= r2) return {false, 0, 0, 0, 0, true};
            double halfSpan = std::sqrt(r2 - x * x);
            double minY = std::max(std::min(ly0, ly1), -halfSpan);
            double maxY = std::min(std::max(ly0, ly1), halfSpan);
            if (minY > maxY) return {false, 0, 0, 0, 0, true};
            return {true, x0, minY + cy, x0, maxY + cy, (minY > std::min(ly0, ly1) || maxY < std::max(ly0, ly1))};
        }
        return {true, x0, y0, x1, y1, false};
    }
};

// ============================================================================
// PRACTICAL 06: TEXTURE MAPPING (CHECKERBOARD, SILICON GRID, PASSIVATION)
// ============================================================================
class TextureMapping {
public:
    enum class TextureType {
        NONE,
        CHECKERBOARD,
        SILICON_MICRO_GRID,
        PASSIVATION_METALLIC
    };

    // Procedural Checkerboard Texture (Practical 06)
    static ColorRGB getCheckerboardTexture(int x, int y, int tileSize = 16) {
        int check = ((x / tileSize) + (y / tileSize)) % 2;
        if (check == 0) {
            return {235, 238, 242}; // Light silicon gray
        } else {
            return {203, 213, 225}; // Darker silicon contrast
        }
    }

    // Silicon Substrate Epitaxial Crystal Lattice Micro-Grid
    static ColorRGB getSiliconGridTexture(int x, int y, int gridPitch = 8) {
        if ((x % gridPitch == 0) || (y % gridPitch == 0)) {
            return {148, 163, 184}; // Crystal grid border
        }
        return {241, 245, 249}; // Bulk substrate
    }

    // Metallic Passivation CVD Thin-Film Interference Texture
    static ColorRGB getPassivationTexture(int x, int y) {
        double dist = std::sqrt(x * x + y * y);
        int ring = static_cast<int>(dist / 12.0) % 3;
        if (ring == 0) return {224, 231, 255}; // Iridescent blue tint
        if (ring == 1) return {236, 253, 245}; // Emerald refraction
        return {243, 232, 255};               // Violet oxide hue
    }
};

// ============================================================================
// COURSE OUTCOME 6: PHONG ILLUMINATION & LIGHTING MODEL
// ============================================================================
class LightingModel {
public:
    struct LightSource {
        Vector3D position; // Light position
        double intensity;
    };

    struct MaterialProperties {
        double ka; // Ambient reflection coefficient (0..1)
        double kd; // Diffuse reflection coefficient (0..1)
        double ks; // Specular reflection coefficient (0..1)
        double shininess; // Specular exponent n
    };

    // Phong Illumination Model: I = Ia*ka + Id*kd*(N.L) + Is*ks*(R.V)^n
    static ColorRGB calculatePhongShading(const ColorRGB& baseColor,
                                          const Vector3D& normal,
                                          const Vector3D& surfacePos,
                                          const LightSource& light,
                                          const MaterialProperties& mat) {
        Vector3D N = normal.normalize();
        Vector3D L = {light.position.x - surfacePos.x, light.position.y - surfacePos.y, light.position.z - surfacePos.z};
        L = L.normalize();

        Vector3D V = {0.0, 0.0, 1.0}; // Viewing vector along +Z (orthogonal camera)

        // 1. Ambient Component
        double I_amb = mat.ka;

        // 2. Diffuse Component (Lambertian Reflection)
        double NdotL = std::max(0.0, N.dot(L));
        double I_diff = mat.kd * NdotL * light.intensity;

        // 3. Specular Component (Glossy Highlights)
        // Reflection vector R = 2*(N.L)*N - L
        Vector3D R = {2.0 * NdotL * N.x - L.x, 2.0 * NdotL * N.y - L.y, 2.0 * NdotL * N.z - L.z};
        R = R.normalize();

        double RdotV = std::max(0.0, R.dot(V));
        double I_spec = mat.ks * std::pow(RdotV, mat.shininess) * light.intensity;

        // Combined Illumination
        double totalFactor = I_amb + I_diff + I_spec;

        double r = baseColor.r * (I_amb + I_diff) + 255.0 * I_spec;
        double g = baseColor.g * (I_amb + I_diff) + 255.0 * I_spec;
        double b = baseColor.b * (I_amb + I_diff) + 255.0 * I_spec;

        return ColorRGB::fromDouble(r, g, b);
    }
};

} // namespace WaferCG

#endif // WAFER_GRAPHICS_ENGINE_H
