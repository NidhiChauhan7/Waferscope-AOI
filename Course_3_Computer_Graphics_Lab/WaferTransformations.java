import java.awt.Point;
import java.awt.geom.Point2D;

/**
 * Course 3: Computer Graphics Lab (2310218L)
 * CO3 & CO5: 2D Geometric Transformations & Cohen-Sutherland Clipping
 *
 * Demonstrates:
 * 1. Translation: P' = P + T (Panning canvas)
 * 2. Scaling: P' = P * S (Zooming in/out)
 * 3. Rotation: P' = R * P (Wafer orientation rotation)
 * 4. Cohen-Sutherland Clipping: Viewport bounding box clipping
 */
public class WaferTransformations {

    // 1. TRANSLATION
    public static Point2D.Double translate(Point2D.Double p, double dx, double dy) {
        return new Point2D.Double(p.x + dx, p.y + dy);
    }

    // 2. SCALING (relative to origin or center)
    public static Point2D.Double scale(Point2D.Double p, double sx, double sy, Point2D.Double center) {
        double x = center.x + (p.x - center.x) * sx;
        double y = center.y + (p.y - center.y) * sy;
        return new Point2D.Double(x, y);
    }

    // 3. ROTATION (around a center point by theta radians)
    public static Point2D.Double rotate(Point2D.Double p, double thetaRad, Point2D.Double center) {
        double cosT = Math.cos(thetaRad);
        double sinT = Math.sin(thetaRad);
        double dx = p.x - center.x;
        double dy = p.y - center.y;
        double x = center.x + (dx * cosT - dy * sinT);
        double y = center.y + (dx * sinT + dy * cosT);
        return new Point2D.Double(x, y);
    }

    // 4. COHEN-SUTHERLAND CLIPPING ALGORITHM
    // 4-bit Outcodes
    public static final int INSIDE = 0; // 0000
    public static final int LEFT   = 1; // 0001
    public static final int RIGHT  = 2; // 0010
    public static final int BOTTOM = 4; // 0100
    public static final int TOP    = 8; // 1000

    public static int computeOutCode(double x, double y, double xmin, double ymin, double xmax, double ymax) {
        int code = INSIDE;
        if (x < xmin) code |= LEFT;
        else if (x > xmax) code |= RIGHT;
        if (y < ymin) code |= BOTTOM;
        else if (y > ymax) code |= TOP;
        return code;
    }

    public static class ClippedLine {
        public boolean visible;
        public double x0, y0, x1, y1;

        public ClippedLine(boolean visible, double x0, double y0, double x1, double y1) {
            this.visible = visible;
            this.x0 = x0;
            this.y0 = y0;
            this.x1 = x1;
            this.y1 = y1;
        }
    }

    public static ClippedLine cohenSutherlandClip(double x0, double y0, double x1, double y1,
                                                 double xmin, double ymin, double xmax, double ymax) {
        int code0 = computeOutCode(x0, y0, xmin, ymin, xmax, ymax);
        int code1 = computeOutCode(x1, y1, xmin, ymin, xmax, ymax);
        boolean accept = false;

        while (true) {
            if ((code0 | code1) == 0) {
                // Trivial accept: both points inside
                accept = true;
                break;
            } else if ((code0 & code1) != 0) {
                // Trivial reject: both points share an outside zone
                break;
            } else {
                // Calculate line intersection with viewport boundary
                double x = 0, y = 0;
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
                    x0 = x;
                    y0 = y;
                    code0 = computeOutCode(x0, y0, xmin, ymin, xmax, ymax);
                } else {
                    x1 = x;
                    y1 = y;
                    code1 = computeOutCode(x1, y1, xmin, ymin, xmax, ymax);
                }
            }
        }
        return new ClippedLine(accept, x0, y0, x1, y1);
    }
}
