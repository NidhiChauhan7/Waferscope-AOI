package waferscope.ui;

import waferscope.classifier.CppGraphicsBridge;
import waferscope.model.DieStatus;
import waferscope.model.WaferMap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.Locale;

/**
 * High-quality Computer Graphics Wafer Visualizer.
 *
 * Implements MITAOE Computer Graphics Curriculum (2310218L):
 * - Circular silicon wafer boundary with standard orientation notch/flat (Practical 01/02)
 * - Individual die rendering with antialiased rounded rectangles
 * - Continuous defect heatmap overlay (local density estimation)
 * - Viewport transformations: Zoom In (+), Zoom Out (-), Pan, Rotate 90° (Practical 04)
 * - Phong Illumination & Lighting simulation with specular highlights (CO.6)
 * - Texture Mapping: Checkerboard, Silicon Micro-Grid, Passivation (Practical 06)
 * - Cohen-Sutherland Line Clipping inspection window (Practical 05)
 * - Scan-line Polygon Fill for defect clusters (Practical 03)
 * - Real-time die inspection HUD on mouse hover with inverse transformation
 */
public class WaferCanvas extends JPanel {

    private WaferMap waferMap;
    private double[][] heatmapDensity;
    private boolean heatmapEnabled = false;
    private boolean gridLinesEnabled = true;

    // Viewport transformations for Zoom, Pan & Rotation (Practical 04 / CO.2)
    private double zoomFactor = 1.0;
    private double panX = 0;
    private double panY = 0;
    private double rotationAngleDeg = 0.0;
    private Point dragStart = null;

    // Computer Graphics Tools
    private boolean lightingEnabled = false;
    private double lightAngleDeg = 45.0;
    private String textureMode = "NONE"; // NONE, CHECKERBOARD, SILICON_GRID, PASSIVATION
    private boolean clippingEnabled = false;
    private boolean scanlineFillEnabled = false;

    // Hover inspection
    private int hoveredRow = -1;
    private int hoveredCol = -1;

    // Palette
    private static final Color BG_COLOR = new Color(248, 250, 252);
    private static final Color WAFER_SUBSTRATE = new Color(226, 232, 240);
    private static final Color WAFER_BORDER = new Color(71, 85, 105);
    private static final Color GOOD_DIE = new Color(34, 197, 94);
    private static final Color DEFECT_DIE = new Color(239, 68, 68);
    private static final Color OUTSIDE_DIE = new Color(241, 245, 249, 140);
    private static final Color DIE_BORDER = new Color(148, 163, 184, 80);
    private static final Color HOVER_HIGHLIGHT = new Color(250, 204, 21, 220);

    public WaferCanvas() {
        setBackground(BG_COLOR);
        setPreferredSize(new Dimension(480, 480));
        setToolTipText(""); // Enable tooltips

        initInteractions();
    }

    private void initInteractions() {
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) || SwingUtilities.isRightMouseButton(e)) {
                    dragStart = e.getPoint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragStart = null;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragStart != null) {
                    double dx = e.getX() - dragStart.x;
                    double dy = e.getY() - dragStart.y;
                    panX += dx;
                    panY += dy;
                    dragStart = e.getPoint();
                    repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                updateHoveredDie(e.getPoint());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hoveredRow = -1;
                hoveredCol = -1;
                repaint();
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double notches = e.getPreciseWheelRotation();
                double oldZoom = zoomFactor;
                if (notches < 0) {
                    zoomFactor = Math.min(5.0, zoomFactor * 1.15);
                } else {
                    zoomFactor = Math.max(0.5, zoomFactor / 1.15);
                }
                Point p = e.getPoint();
                panX = p.x - (p.x - panX) * (zoomFactor / oldZoom);
                panY = p.y - (p.y - panY) * (zoomFactor / oldZoom);
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    resetView();
                }
            }
        };

        addMouseListener(ma);
        addMouseMotionListener(ma);
        addMouseWheelListener(ma);
    }

    // ------------------------------------------------------------------------
    // COMPUTER GRAPHICS CONTROLS
    // ------------------------------------------------------------------------
    public void zoomIn() {
        zoomFactor = Math.min(5.0, zoomFactor * 1.25);
        repaint();
    }

    public void zoomOut() {
        zoomFactor = Math.max(0.5, zoomFactor / 1.25);
        repaint();
    }

    public void rotateClockwise() {
        rotationAngleDeg = (rotationAngleDeg + 90.0) % 360.0;
        repaint();
    }

    public void rotateCounterClockwise() {
        rotationAngleDeg = (rotationAngleDeg - 90.0 + 360.0) % 360.0;
        repaint();
    }

    public void toggleLighting() {
        lightingEnabled = !lightingEnabled;
        repaint();
    }

    public boolean isLightingEnabled() {
        return lightingEnabled;
    }

    public void cycleTextureMode() {
        if ("NONE".equals(textureMode)) textureMode = "CHECKERBOARD";
        else if ("CHECKERBOARD".equals(textureMode)) textureMode = "SILICON_GRID";
        else if ("SILICON_GRID".equals(textureMode)) textureMode = "PASSIVATION";
        else textureMode = "NONE";
        repaint();
    }

    public String getTextureMode() {
        return textureMode;
    }

    public void toggleClipping() {
        clippingEnabled = !clippingEnabled;
        repaint();
    }

    public boolean isClippingEnabled() {
        return clippingEnabled;
    }

    public void toggleScanlineFill() {
        scanlineFillEnabled = !scanlineFillEnabled;
        repaint();
    }

    public boolean isScanlineFillEnabled() {
        return scanlineFillEnabled;
    }

    public double getZoomFactor() {
        return zoomFactor;
    }

    public double getRotationAngle() {
        return rotationAngleDeg;
    }

    public void setRotationAngle(double angleDeg) {
        this.rotationAngleDeg = ((angleDeg % 360.0) + 360.0) % 360.0;
        repaint();
    }

    public void resetView() {
        zoomFactor = 1.0;
        panX = 0;
        panY = 0;
        rotationAngleDeg = 0.0;
        lightingEnabled = false;
        textureMode = "NONE";
        clippingEnabled = false;
        scanlineFillEnabled = false;
        hoveredRow = -1;
        hoveredCol = -1;
        repaint();
    }

    public void setWafer(WaferMap wafer) {
        this.waferMap = wafer;
        if (wafer != null) {
            this.heatmapDensity = wafer.computeDensityMap(2);
        } else {
            this.heatmapDensity = null;
        }
        resetView();
    }

    public void setHeatmapEnabled(boolean enabled) {
        this.heatmapEnabled = enabled;
        repaint();
    }

    public boolean isHeatmapEnabled() {
        return heatmapEnabled;
    }

    public void setGridLinesEnabled(boolean enabled) {
        this.gridLinesEnabled = enabled;
        repaint();
    }

    private double calculateWaferRadius(int rows, int cols, double cellSize, double originX, double originY, double cx, double cy) {
        if (waferMap == null) return (Math.min(cols, rows) * cellSize) * 0.45;
        double maxValidDist = 0.0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (waferMap.getDie(r, c) >= 0) {
                    double dcx = originX + (c + 0.5) * cellSize;
                    double dcy = originY + (r + 0.5) * cellSize;
                    double d = Math.hypot(dcx - cx, dcy - cy);
                    if (d > maxValidDist) {
                        maxValidDist = d;
                    }
                }
            }
        }
        if (maxValidDist > 0) {
            // Circle radius cuts right along the outer valid perimeter so boundary dies straddle the circular wafer edge
            return maxValidDist + cellSize * 0.15;
        } else {
            return (Math.min(cols, rows) * cellSize) * 0.45;
        }
    }

    private void drawCalloutBadge(Graphics2D g2, String text, double boxX, double boxY, double targetX, double targetY, Color borderColor, Color textColor) {
        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        FontMetrics fm = g2.getFontMetrics();
        int pad = 7;
        int bw = fm.stringWidth(text) + pad * 2;
        int bh = 22;

        // Pointer line connecting badge to target
        g2.setStroke(new BasicStroke(1.3f));
        g2.setColor(borderColor);
        double startX = (targetX < boxX) ? boxX : (targetX > boxX + bw ? boxX + bw : boxX + bw / 2.0);
        double startY = (targetY < boxY) ? boxY : (targetY > boxY + bh ? boxY + bh : boxY + bh / 2.0);
        g2.draw(new Line2D.Double(startX, startY, targetX, targetY));

        // Small target dot
        g2.fill(new Ellipse2D.Double(targetX - 2.5, targetY - 2.5, 5, 5));

        // Badge pill background
        g2.setColor(new Color(15, 23, 42, 235)); // High-contrast Slate-900
        g2.fillRoundRect((int) boxX, (int) boxY, bw, bh, 6, 6);
        g2.setColor(borderColor);
        g2.drawRoundRect((int) boxX, (int) boxY, bw, bh, 6, 6);

        // Text
        g2.setColor(textColor);
        g2.drawString(text, (float) (boxX + pad), (float) (boxY + (bh + fm.getAscent() - fm.getDescent()) / 2));
    }

    private void updateHoveredDie(Point mousePoint) {
        if (waferMap == null) return;

        int w = getWidth();
        int h = getHeight();
        int rows = waferMap.getRows();
        int cols = waferMap.getCols();

        int maxDim = Math.max(rows, cols);
        double baseCellSize = Math.min((w - 60) / (double) maxDim, (h - 70) / (double) maxDim);
        double cellSize = baseCellSize * zoomFactor;

        double gridW = cols * cellSize;
        double gridH = rows * cellSize;
        double originX = (w - gridW) / 2.0 + panX;
        double originY = (h - gridH) / 2.0 + panY;

        double waferCenterX = originX + gridW / 2.0;
        double waferCenterY = originY + gridH / 2.0;

        // Invert rotation transform for hit-testing
        double rad = Math.toRadians(-rotationAngleDeg);
        double mx = mousePoint.x - waferCenterX;
        double my = mousePoint.y - waferCenterY;
        double rx = mx * Math.cos(rad) - my * Math.sin(rad) + waferCenterX;
        double ry = mx * Math.sin(rad) + my * Math.cos(rad) + waferCenterY;

        double relX = rx - originX;
        double relY = ry - originY;

        int col = (int) Math.floor(relX / cellSize);
        int row = (int) Math.floor(relY / cellSize);

        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            if (row != hoveredRow || col != hoveredCol) {
                hoveredRow = row;
                hoveredCol = col;
                repaint();
            }
        } else {
            if (hoveredRow != -1 || hoveredCol != -1) {
                hoveredRow = -1;
                hoveredCol = -1;
                repaint();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        if (waferMap == null) {
            g2.setColor(new Color(148, 163, 184));
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            String msg = "No Wafer Map Loaded";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(msg, (w - fm.stringWidth(msg)) / 2, h / 2);
            g2.dispose();
            return;
        }

        int rows = waferMap.getRows();
        int cols = waferMap.getCols();
        int maxDim = Math.max(rows, cols);

        double baseCellSize = Math.min((w - 60) / (double) maxDim, (h - 70) / (double) maxDim);
        double cellSize = baseCellSize * zoomFactor;

        double gridW = cols * cellSize;
        double gridH = rows * cellSize;
        double originX = (w - gridW) / 2.0 + panX;
        double originY = (h - gridH) / 2.0 + panY;

        double waferCenterX = originX + gridW / 2.0;
        double waferCenterY = originY + gridH / 2.0;
        double waferRadius = calculateWaferRadius(rows, cols, cellSize, originX, originY, waferCenterX, waferCenterY);
        double waferDiam = waferRadius * 2.0;

        // Apply 2D Rotation Transformation around Wafer Center (Practical 04 / CO.2)
        AffineTransform oldTx = g2.getTransform();
        if (rotationAngleDeg != 0.0) {
            g2.rotate(Math.toRadians(rotationAngleDeg), waferCenterX, waferCenterY);
        }

        double gap = Math.max(1.0, cellSize * 0.08);
        double dieSize = Math.max(1.0, cellSize - gap);

        // --------------------------------------------------------------------
        // LAYER 1: Full Rectangular Cartesian Grid & Removed Outside Region
        // (Matching User Sketch: "This part is removed", with cross-hatchings)
        // --------------------------------------------------------------------
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int val = waferMap.getDie(r, c);
                double dx = originX + c * cellSize;
                double dy = originY + r * cellSize;
                double dieCenterX = dx + cellSize / 2.0;
                double dieCenterY = dy + cellSize / 2.0;
                double dist = Math.hypot(dieCenterX - waferCenterX, dieCenterY - waferCenterY);

                if (val < 0 || dist > (waferRadius - cellSize * 0.15)) {
                    // Cell is in removed outer area outside the circular wafer
                    g2.setColor(clippingEnabled ? new Color(254, 242, 242, 210) : new Color(248, 250, 252));
                    g2.fill(new Rectangle2D.Double(dx, dy, cellSize, cellSize));

                    // Grid line boundary
                    g2.setStroke(clippingEnabled ?
                        new BasicStroke(1.1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{3.0f, 3.0f}, 0.0f) :
                        new BasicStroke(0.8f));
                    g2.setColor(clippingEnabled ? new Color(239, 68, 68, 160) : new Color(226, 232, 240));
                    g2.draw(new Rectangle2D.Double(dx, dy, cellSize, cellSize));

                    // Diagonal pen cross-hatching (matching user's drawing)
                    g2.setStroke(new BasicStroke(clippingEnabled ? 1.1f : 0.7f));
                    g2.setColor(clippingEnabled ? new Color(239, 68, 68, 150) : new Color(203, 213, 225, 160));
                    g2.draw(new Line2D.Double(dx + 2, dy + 2, dx + cellSize - 2, dy + cellSize - 2));
                    g2.draw(new Line2D.Double(dx + cellSize - 2, dy + 2, dx + 2, dy + cellSize - 2));

                    if (clippingEnabled && cellSize >= 18) {
                        g2.setFont(new Font("SansSerif", Font.BOLD, (int) Math.max(7, cellSize * 0.26)));
                        g2.setColor(new Color(185, 28, 28, 200));
                        String lbl = "CLIP";
                        FontMetrics mfm = g2.getFontMetrics();
                        g2.drawString(lbl, (float) (dx + (cellSize - mfm.stringWidth(lbl)) / 2.0),
                                           (float) (dy + (cellSize + mfm.getAscent() - mfm.getDescent()) / 2.0));
                    }

                    // Hovered Die Outline on outer cells
                    if (r == hoveredRow && c == hoveredCol) {
                        g2.setColor(HOVER_HIGHLIGHT);
                        g2.setStroke(new BasicStroke(2.2f));
                        g2.draw(new Rectangle2D.Double(dx, dy, cellSize, cellSize));
                    }
                }
            }
        }

        // --------------------------------------------------------------------
        // LAYER 2: Circular Semiconductor Wafer Substrate
        // (Matching User Sketch: "This circle is semiconductor wafer which is needed")
        // --------------------------------------------------------------------
        Shape waferCircle = new Ellipse2D.Double(waferCenterX - waferRadius, waferCenterY - waferRadius, waferDiam, waferDiam);

        if (lightingEnabled) {
            float lightX = (float) (waferCenterX + Math.cos(Math.toRadians(lightAngleDeg)) * waferRadius * 0.7);
            float lightY = (float) (waferCenterY - Math.sin(Math.toRadians(lightAngleDeg)) * waferRadius * 0.7);
            RadialGradientPaint litPaint = new RadialGradientPaint(
                new Point2D.Float(lightX, lightY), (float) (waferRadius * 1.3),
                new float[]{0.0f, 0.45f, 0.85f, 1.0f},
                new Color[]{new Color(255, 255, 255), new Color(241, 245, 249), WAFER_SUBSTRATE, new Color(180, 195, 210)}
            );
            g2.setPaint(litPaint);
        } else {
            RadialGradientPaint p = new RadialGradientPaint(
                new Point2D.Double(waferCenterX, waferCenterY), (float) waferRadius,
                new float[]{0.0f, 0.85f, 1.0f},
                new Color[]{new Color(241, 245, 249), WAFER_SUBSTRATE, new Color(203, 213, 225)}
            );
            g2.setPaint(p);
        }
        g2.fill(waferCircle);

        // Procedural Texture Mapping if active
        if (!"NONE".equals(textureMode)) {
            Shape clipSave = g2.getClip();
            g2.clip(waferCircle);
            int step = 14;
            int startX = (int) (waferCenterX - waferRadius);
            int startY = (int) (waferCenterY - waferRadius);
            int endX = (int) (waferCenterX + waferRadius);
            int endY = (int) (waferCenterY + waferRadius);

            for (int tx = startX; tx < endX; tx += step) {
                for (int ty = startY; ty < endY; ty += step) {
                    Color tc = CppGraphicsBridge.getTextureColor(tx, ty, textureMode);
                    if (tc != null) {
                        g2.setColor(tc);
                        g2.fillRect(tx, ty, step, step);
                    }
                }
            }
            g2.setClip(clipSave);
        }

        // --------------------------------------------------------------------
        // LAYER 3: Active Dies & Grid Lines CLIPPED TO CIRCLE BORDER
        // ("The grids should be clipped from the circle border. Apply for all analysis.")
        // --------------------------------------------------------------------
        Shape origClip = g2.getClip();
        g2.clip(waferCircle); // <--- HARD GEOMETRIC CLIPPING APPLIED ACROSS ALL ANALYSIS

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int val = waferMap.getDie(r, c);
                if (val < 0) continue; // Outside grid cells handled in Layer 1

                double dx = originX + c * cellSize;
                double dy = originY + r * cellSize;

                if (val == 1) {
                    // Defective Die
                    Color dieColor = DEFECT_DIE;
                    if (lightingEnabled) {
                        dieColor = CppGraphicsBridge.applyLighting(DEFECT_DIE, 0.0, 0.0, 1.0, lightAngleDeg, 0.25, 0.60, 0.35);
                    }
                    g2.setColor(dieColor);
                    g2.fill(new RoundRectangle2D.Double(dx + gap/2, dy + gap/2, dieSize, dieSize, 3, 3));

                    // Scan-line polygon fill stripes (Practical 03)
                    if (scanlineFillEnabled && dieSize > 4) {
                        g2.setColor(new Color(254, 226, 226, 200));
                        for (double sy = dy + gap/2 + 2; sy < dy + gap/2 + dieSize - 2; sy += 3.0) {
                            g2.draw(new Line2D.Double(dx + gap/2 + 2, sy, dx + gap/2 + dieSize - 2, sy));
                        }
                    }

                    if (gridLinesEnabled && dieSize > 5) {
                        g2.setColor(new Color(185, 28, 28, 120));
                        g2.draw(new RoundRectangle2D.Double(dx + gap/2, dy + gap/2, dieSize, dieSize, 3, 3));
                    }
                } else {
                    // Good Die
                    Color dieColor = GOOD_DIE;
                    if (lightingEnabled) {
                        dieColor = CppGraphicsBridge.applyLighting(GOOD_DIE, 0.0, 0.0, 1.0, lightAngleDeg, 0.20, 0.65, 0.40);
                    }
                    g2.setColor(dieColor);
                    g2.fill(new RoundRectangle2D.Double(dx + gap/2, dy + gap/2, dieSize, dieSize, 3, 3));
                    if (gridLinesEnabled && dieSize > 5) {
                        g2.setColor(new Color(22, 101, 52, 100));
                        g2.draw(new RoundRectangle2D.Double(dx + gap/2, dy + gap/2, dieSize, dieSize, 3, 3));
                    }
                }

                // Heatmap Overlay if toggled
                if (heatmapEnabled && heatmapDensity != null) {
                    double dens = heatmapDensity[r][c];
                    if (dens > 0.0) {
                        Color heatColor = getThermalColor(dens);
                        g2.setColor(heatColor);
                        g2.fill(new RoundRectangle2D.Double(dx + gap/2, dy + gap/2, dieSize, dieSize, 3, 3));
                    }
                }

                // Hovered Die Outline on active dies
                if (r == hoveredRow && c == hoveredCol) {
                    g2.setColor(HOVER_HIGHLIGHT);
                    g2.setStroke(new BasicStroke(2.5f));
                    g2.draw(new RoundRectangle2D.Double(dx + gap/2 - 1, dy + gap/2 - 1, dieSize + 2, dieSize + 2, 4, 4));
                }
            }
        }

        // Internal die grid lines across the wafer substrate (clipped to wafer circle)
        if (gridLinesEnabled) {
            g2.setColor(new Color(203, 213, 225, 140));
            g2.setStroke(new BasicStroke(0.9f));
            for (int r = 0; r <= rows; r++) {
                double gy = originY + r * cellSize;
                g2.draw(new Line2D.Double(originX, gy, originX + gridW, gy));
            }
            for (int c = 0; c <= cols; c++) {
                double gx = originX + c * cellSize;
                g2.draw(new Line2D.Double(gx, originY, gx, originY + gridH));
            }
        }

        g2.setClip(origClip); // Restore unclipped graphics context

        // --------------------------------------------------------------------
        // LAYER 4: Circular Wafer Notch & Clipping Border
        // (Matching User Sketch: "clipping border.")
        // --------------------------------------------------------------------
        double notchSize = Math.max(6.0, cellSize * 0.8);
        Path2D notch = new Path2D.Double();
        notch.moveTo(waferCenterX - notchSize, waferCenterY + waferRadius - 1);
        notch.lineTo(waferCenterX, waferCenterY + waferRadius - notchSize - 2);
        notch.lineTo(waferCenterX + notchSize, waferCenterY + waferRadius - 1);
        notch.closePath();
        g2.setColor(new Color(51, 65, 85));
        g2.fill(notch);
        g2.draw(notch);

        if (clippingEnabled) {
            // High-contrast Amber & Cyan Clipping Border
            Stroke circleClipStroke = new BasicStroke(3.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{8.0f, 4.0f}, 0.0f);
            g2.setStroke(circleClipStroke);
            g2.setColor(new Color(245, 158, 11)); // Vivid Amber #f59e0b
            g2.draw(waferCircle);

            g2.setStroke(new BasicStroke(1.8f));
            g2.setColor(new Color(6, 182, 212, 200)); // Vivid Cyan #06b6d4
            g2.draw(new Ellipse2D.Double(waferCenterX - waferRadius - 2, waferCenterY - waferRadius - 2, waferDiam + 4, waferDiam + 4));

            // Radial diagonal cutting guides
            Stroke guideStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{4.0f, 4.0f}, 0.0f);
            g2.setStroke(guideStroke);
            g2.setColor(new Color(245, 158, 11, 140));
            double cornerOffset = Math.max(gridW, gridH) * 0.54;
            g2.draw(new Line2D.Double(waferCenterX - cornerOffset, waferCenterY - cornerOffset, waferCenterX + cornerOffset, waferCenterY + cornerOffset));
            g2.draw(new Line2D.Double(waferCenterX - cornerOffset, waferCenterY + cornerOffset, waferCenterX + cornerOffset, waferCenterY - cornerOffset));

            // Cohen-Sutherland aperture
            double clipX = originX + 3 * cellSize;
            double clipY = originY + 3 * cellSize;
            double clipW = (cols - 6) * cellSize;
            double clipH = (rows - 6) * cellSize;
            Stroke dashed = new BasicStroke(1.8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{5.0f, 4.0f}, 0.0f);
            g2.setStroke(dashed);
            g2.setColor(new Color(245, 158, 11, 180));
            g2.draw(new Rectangle2D.Double(clipX, clipY, clipW, clipH));
            g2.setFont(new Font("Monospaced", Font.BOLD, 10));
            g2.setColor(new Color(180, 83, 9));
            g2.drawString("COHEN-SUTHERLAND APERTURE [0000]", (float) clipX + 6, (float) clipY + 14);

            // --------------------------------------------------------------------
            // LAYER 5: Visual Annotations Matching User's Handwritten Sketch
            // --------------------------------------------------------------------
            // Callout 1: "clipping border (Circle Wafer Edge)"
            double angleRad1 = Math.toRadians(-35);
            double targetX1 = waferCenterX + waferRadius * Math.cos(angleRad1);
            double targetY1 = waferCenterY + waferRadius * Math.sin(angleRad1);
            drawCalloutBadge(g2, "clipping border: x² + y² = R²",
                targetX1 + 16, targetY1 - 10, targetX1, targetY1,
                new Color(245, 158, 11), new Color(254, 243, 199));

            // Callout 2: "This part is removed (x² + y² > R²)" in top-right outer corner
            double targetX2 = originX + gridW - cellSize * 1.5;
            double targetY2 = originY + cellSize * 1.5;
            drawCalloutBadge(g2, "This part is removed (x² + y² > R²)",
                originX + gridW - 220, originY + cellSize * 0.4, targetX2, targetY2,
                new Color(239, 68, 68), new Color(254, 242, 242));

            // Callout 3: "Boundary Grid: Some part inside circle, this part is clipped outside"
            double angleRad3 = Math.toRadians(140);
            double targetX3 = waferCenterX + waferRadius * Math.cos(angleRad3);
            double targetY3 = waferCenterY + waferRadius * Math.sin(angleRad3);
            drawCalloutBadge(g2, "Boundary Grid: Inside on wafer | Outside clipped",
                originX + 10, waferCenterY + waferRadius * 0.48, targetX3, targetY3,
                new Color(14, 165, 233), new Color(240, 249, 255));

            // Highlight the boundary cut die with cyan dashed outline
            int bCol = (int) Math.floor((targetX3 - originX) / cellSize);
            int bRow = (int) Math.floor((targetY3 - originY) / cellSize);
            if (bRow >= 0 && bRow < rows && bCol >= 0 && bCol < cols) {
                double bdx = originX + bCol * cellSize;
                double bdy = originY + bRow * cellSize;
                Stroke bStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{4.0f, 3.0f}, 0.0f);
                g2.setStroke(bStroke);
                g2.setColor(new Color(6, 182, 212)); // Cyan
                g2.draw(new Rectangle2D.Double(bdx, bdy, cellSize, cellSize));
            }
        } else {
            g2.setColor(WAFER_BORDER);
            g2.setStroke(new BasicStroke(2.4f));
            g2.draw(waferCircle);
        }

        // Restore transform
        g2.setTransform(oldTx);

        // Draw Top Floating Banner if Clipping is Active (Stacked neatly under top HUD)
        if (clippingEnabled) {
            int validCount = waferMap.getValidCount();
            int totalCells = rows * cols;
            int clippedCount = totalCells - validCount;
            String clipLegend = String.format(Locale.US,
                "OUTER CIRCLE CLIPPING (Practical 05): x² + y² ≤ R² (R=%.1fpx) | %d Active Dies Preserved | %d Grid Cells Clipped Out",
                waferRadius, validCount, clippedCount);

            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            FontMetrics cfm = g2.getFontMetrics();
            int legendW = cfm.stringWidth(clipLegend) + 22;
            int legendH = 24;
            int legendX = 12;
            int legendY = 42; // Directly below Top CG Engine HUD (which is at y=12)

            g2.setColor(new Color(15, 23, 42, 230));
            g2.fillRoundRect(legendX, legendY, legendW, legendH, 8, 8);
            g2.setColor(new Color(245, 158, 11));
            g2.setStroke(new BasicStroke(1.4f));
            g2.drawRoundRect(legendX, legendY, legendW, legendH, 8, 8);
            g2.setColor(new Color(254, 243, 199));
            g2.drawString(clipLegend, (float) (legendX + 11), (float) (legendY + (legendH + cfm.getAscent() - cfm.getDescent()) / 2));
        }

        // Draw HUDs (Top CG Telemetry + Bottom Die Inspection)
        drawCgEngineHud(g2, w);
        drawInspectionHud(g2, w, h);

        g2.dispose();
    }

    private void drawCgEngineHud(Graphics2D g2, int w) {
        int validCount = (waferMap != null) ? waferMap.getValidCount() : 0;
        int totalCells = (waferMap != null) ? waferMap.getRows() * waferMap.getCols() : 0;
        int clippedCount = totalCells - validCount;

        String clipHud = clippingEnabled ?
            String.format(Locale.US, " | Outer Circle Clip: ON (%d Inside / %d Clipped Out)", validCount, clippedCount) : "";

        String cgText = String.format(Locale.US,
            "C++ CG Engine | Zoom: %d%% | Rot: %d° | Light: %s | Texture: %s%s%s",
            (int) (zoomFactor * 100), (int) rotationAngleDeg,
            (lightingEnabled ? "ON (Phong)" : "OFF"),
            textureMode,
            clipHud,
            (scanlineFillEnabled ? " | Fill: ON" : "")
        );

        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        FontMetrics fm = g2.getFontMetrics();
        int textW = fm.stringWidth(cgText);
        int pad = 10;
        int barH = 24;

        g2.setColor(new Color(15, 23, 42, 220)); // High contrast Slate-900
        g2.fillRoundRect(12, 12, textW + pad * 2, barH, 8, 8);
        g2.setColor(new Color(37, 99, 235)); // Royal Blue Border
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(12, 12, textW + pad * 2, barH, 8, 8);

        g2.setColor(Color.WHITE);
        g2.drawString(cgText, 12 + pad, 12 + (barH + fm.getAscent() - fm.getDescent()) / 2);
    }

    private Color getThermalColor(double density) {
        int alpha = (int) (130 + Math.min(1.0, density) * 110);
        if (density < 0.25) {
            float t = (float) (density / 0.25);
            return new Color(0, (int) (120 + 100 * t), 255, alpha);
        } else if (density < 0.50) {
            float t = (float) ((density - 0.25) / 0.25);
            return new Color((int) (255 * t), 220, (int) (255 * (1 - t)), alpha);
        } else if (density < 0.75) {
            float t = (float) ((density - 0.50) / 0.25);
            return new Color(255, (int) (220 - 100 * t), 0, alpha);
        } else {
            float t = (float) Math.min(1.0, (density - 0.75) / 0.25);
            return new Color(255, (int) (120 * (1 - t)), 0, alpha);
        }
    }

    private void drawInspectionHud(Graphics2D g2, int w, int h) {
        String hudText;
        if (hoveredRow >= 0 && hoveredCol >= 0 && waferMap != null) {
            int val = waferMap.getDie(hoveredRow, hoveredCol);
            int rows = waferMap.getRows();
            int cols = waferMap.getCols();
            int maxDim = Math.max(rows, cols);
            double baseCellSize = Math.min((w - 60) / (double) maxDim, (h - 70) / (double) maxDim);
            double cellSize = baseCellSize * zoomFactor;
            double gridW = cols * cellSize;
            double gridH = rows * cellSize;
            double originX = (w - gridW) / 2.0 + panX;
            double originY = (h - gridH) / 2.0 + panY;
            double waferCenterX = originX + gridW / 2.0;
            double waferCenterY = originY + gridH / 2.0;
            double waferRadius = calculateWaferRadius(rows, cols, cellSize, originX, originY, waferCenterX, waferCenterY);

            double dieCenterX = originX + (hoveredCol + 0.5) * cellSize;
            double dieCenterY = originY + (hoveredRow + 0.5) * cellSize;
            double distFromWaferCenter = Math.hypot(dieCenterX - waferCenterX, dieCenterY - waferCenterY);

            if (val < 0 || distFromWaferCenter > waferRadius) {
                hudText = String.format(Locale.US,
                    "Grid Cell (%d, %d) | THIS PART IS REMOVED / CLIPPED (x² + y² > R²) | dist: %.1fpx > R(%.1fpx) [Clipped Out] | Zoom: %.1fx",
                    hoveredRow, hoveredCol, distFromWaferCenter, waferRadius, zoomFactor);
            } else {
                String status = (val == 1) ? "DEFECTIVE DIE" : "GOOD DIE (PASS)";
                double dens = (heatmapDensity != null && hoveredRow < heatmapDensity.length && hoveredCol < heatmapDensity[0].length) ?
                    heatmapDensity[hoveredRow][hoveredCol] * 100.0 : 0.0;
                hudText = String.format(Locale.US,
                    "Die (%d, %d) | %s | INSIDE CIRCLE (x² + y² ≤ R²) | dist: %.1fpx ≤ R(%.1fpx) | Defect Density: %.1f%% | Zoom: %.1fx",
                    hoveredRow, hoveredCol, status, distFromWaferCenter, waferRadius, dens, zoomFactor);
            }
        } else {
            int validCount = (waferMap != null) ? waferMap.getValidCount() : 0;
            int totalCells = (waferMap != null) ? waferMap.getRows() * waferMap.getCols() : 0;
            int clippedCount = totalCells - validCount;

            hudText = String.format(Locale.US,
                "Wafer: %s [%dx%d] | Inside Circle: %d | Clipped Out: %d | Defect: %d (%.1f%%) | Zoom: %.1fx | Drag to pan, scroll to zoom",
                waferMap.getId(), waferMap.getRows(), waferMap.getCols(),
                validCount, clippedCount, waferMap.getDefectiveCount(), waferMap.getDefectPercentage(), zoomFactor);
        }

        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        FontMetrics fm = g2.getFontMetrics();
        int textW = fm.stringWidth(hudText);
        int pad = 8;
        int barH = 26;

        g2.setColor(new Color(15, 23, 42, 210));
        g2.fillRoundRect(12, h - barH - 12, textW + pad * 2, barH, 8, 8);
        g2.setColor(new Color(51, 65, 85));
        g2.drawRoundRect(12, h - barH - 12, textW + pad * 2, barH, 8, 8);

        g2.setColor(Color.WHITE);
        g2.drawString(hudText, 12 + pad, h - 12 - (barH - fm.getAscent()) / 2);
    }

    public int getHoveredRow() {
        return hoveredRow;
    }

    public int getHoveredCol() {
        return hoveredCol;
    }

    public BufferedImage exportToImage() {
        return captureImage();
    }

    public BufferedImage captureImage() {
        int w = Math.max(600, getWidth());
        int h = Math.max(600, getHeight());
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        paintComponent(g2);
        g2.dispose();
        return img;
    }
}
