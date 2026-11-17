import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Course 3: Computer Graphics Lab (2310218L)
 * CO1 & CO2: Drawing Algorithms (Midpoint Circle & Bresenham Line)
 *
 * Used in project to:
 * 1. Draw the circular silicon wafer substrate perimeter using Midpoint Circle Algorithm
 * 2. Draw the wafer orientation notch/flat using Bresenham Line Algorithm
 */
public class WaferCircleAlgorithms {

    /**
     * Midpoint Circle Drawing Algorithm (Integer arithmetic only)
     * Calculates pixel coordinates for a circle with center (xc, yc) and radius r.
     * Generates points using 8-way symmetry.
     */
    public static List<Point> midpointCircle(int xc, int yc, int r) {
        List<Point> points = new ArrayList<>();
        int x = 0;
        int y = r;
        int p = 1 - r; // Initial decision parameter

        addSymmetricPoints(xc, yc, x, y, points);

        while (x < y) {
            x++;
            if (p < 0) {
                p += 2 * x + 1;
            } else {
                y--;
                p += 2 * (x - y) + 1;
            }
            addSymmetricPoints(xc, yc, x, y, points);
        }
        return points;
    }

    private static void addSymmetricPoints(int xc, int yc, int x, int y, List<Point> points) {
        points.add(new Point(xc + x, yc + y));
        points.add(new Point(xc - x, yc + y));
        points.add(new Point(xc + x, yc - y));
        points.add(new Point(xc - x, yc - y));
        points.add(new Point(xc + y, yc + x));
        points.add(new Point(xc - y, yc + x));
        points.add(new Point(xc + y, yc - x));
        points.add(new Point(xc - y, yc - x));
    }

    /**
     * Bresenham Line Drawing Algorithm
     * Used for drawing wafer notch edges with pure integer addition and subtraction.
     */
    public static List<Point> bresenhamLine(int x0, int y0, int x1, int y1) {
        List<Point> line = new ArrayList<>();
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            line.add(new Point(x0, y0));
            if (x0 == x1 && y0 == y1) break;
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x0 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y0 += sy;
            }
        }
        return line;
    }
}
