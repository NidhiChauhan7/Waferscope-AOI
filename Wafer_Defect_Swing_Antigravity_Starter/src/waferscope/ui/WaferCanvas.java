package waferscope.ui;

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
 * Implements:
 * - Circular silicon wafer boundary with standard orientation notch/flat
 * - Individual die rendering with antialiased rounded rectangles
 * - Continuous defect heatmap overlay (local density estimation)
 * - Interactive zoom (mouse wheel) and pan (click & drag)
 * - Real-time die inspection HUD on mouse hover
 */
public class WaferCanvas extends JPanel {

    private WaferMap waferMap;
    private double[][] heatmapDensity;
    private boolean heatmapEnabled = false;
    private boolean gridLinesEnabled = true;

    // Viewport transformations for Zoom & Pan
    private double zoomFactor = 1.0;
    private double panX = 0;
    private double panY = 0;
    private Point dragStart = null;

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
                    zoomFactor = Math.max(0.6, zoomFactor / 1.15);
                }
                // Zoom centered on cursor
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

    public void resetView() {
        zoomFactor = 1.0;
        panX = 0;
        panY = 0;
        hoveredRow = -1;
        hoveredCol = -1;
        repaint();
    }

    private void updateHoveredDie(Point mousePoint) {
        if (waferMap == null) return;

        int w = getWidth();
        int h = getHeight();
        int rows = waferMap.getRows();
        int cols = waferMap.getCols();

        int maxDim = Math.max(rows, cols);
        double baseCellSize = Math.min((w - 50) / (double) maxDim, (h - 50) / (double) maxDim);
        double cellSize = baseCellSize * zoomFactor;

        double gridW = cols * cellSize;
        double gridH = rows * cellSize;
        double originX = (w - gridW) / 2.0 + panX;
        double originY = (h - gridH) / 2.0 + panY;

        double relX = mousePoint.x - originX;
        double relY = mousePoint.y - originY;

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

        double baseCellSize = Math.min((w - 50) / (double) maxDim, (h - 50) / (double) maxDim);
        double cellSize = baseCellSize * zoomFactor;

        double gridW = cols * cellSize;
        double gridH = rows * cellSize;
        double originX = (w - gridW) / 2.0 + panX;
        double originY = (h - gridH) / 2.0 + panY;

        // Draw Circular Wafer Substrate & Notch
        double waferDiam = Math.max(gridW, gridH) * 1.05;
        double waferCenterX = originX + gridW / 2.0;
        double waferCenterY = originY + gridH / 2.0;
        double waferRadius = waferDiam / 2.0;

        // Substrate circle with gradient
        Point2D center = new Point2D.Double(waferCenterX, waferCenterY);
        float radius = (float) waferRadius;
        RadialGradientPaint p = new RadialGradientPaint(
            center, radius,
            new float[]{0.0f, 0.85f, 1.0f},
            new Color[]{new Color(241, 245, 249), WAFER_SUBSTRATE, new Color(203, 213, 225)}
        );
        g2.setPaint(p);
        Shape waferCircle = new Ellipse2D.Double(waferCenterX - waferRadius, waferCenterY - waferRadius, waferDiam, waferDiam);
        g2.fill(waferCircle);

        // Circular Bevel
        g2.setColor(WAFER_BORDER);
        g2.setStroke(new BasicStroke(2.2f));
        g2.draw(waferCircle);

        // Wafer Notch (standard alignment flat/notch at the bottom)
        double notchSize = Math.max(6.0, cellSize * 0.8);
        Path2D notch = new Path2D.Double();
        notch.moveTo(waferCenterX - notchSize, waferCenterY + waferRadius - 1);
        notch.lineTo(waferCenterX, waferCenterY + waferRadius - notchSize - 2);
        notch.lineTo(waferCenterX + notchSize, waferCenterY + waferRadius - 1);
        notch.closePath();
        g2.setColor(new Color(51, 65, 85));
        g2.fill(notch);
        g2.draw(notch);

        // Render Dies
        double gap = Math.max(1.0, cellSize * 0.08);
        double dieSize = Math.max(1.0, cellSize - gap);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int val = waferMap.getDie(r, c);
                double dx = originX + c * cellSize;
                double dy = originY + r * cellSize;

                if (val < 0) {
                    // Outside wafer substrate cell
                    g2.setColor(OUTSIDE_DIE);
                    g2.fill(new RoundRectangle2D.Double(dx, dy, dieSize, dieSize, 2, 2));
                } else if (val == 1) {
                    // Defective die
                    g2.setColor(DEFECT_DIE);
                    g2.fill(new RoundRectangle2D.Double(dx, dy, dieSize, dieSize, 3, 3));
                    if (gridLinesEnabled && dieSize > 5) {
                        g2.setColor(new Color(185, 28, 28, 120));
                        g2.draw(new RoundRectangle2D.Double(dx, dy, dieSize, dieSize, 3, 3));
                    }
                } else {
                    // Good die
                    g2.setColor(GOOD_DIE);
                    g2.fill(new RoundRectangle2D.Double(dx, dy, dieSize, dieSize, 3, 3));
                    if (gridLinesEnabled && dieSize > 5) {
                        g2.setColor(new Color(22, 101, 52, 100));
                        g2.draw(new RoundRectangle2D.Double(dx, dy, dieSize, dieSize, 3, 3));
                    }
                }

                // Heatmap Overlay if toggled
                if (heatmapEnabled && heatmapDensity != null && val >= 0) {
                    double dens = heatmapDensity[r][c];
                    if (dens > 0.0) {
                        Color heatColor = getThermalColor(dens);
                        g2.setColor(heatColor);
                        g2.fill(new RoundRectangle2D.Double(dx, dy, dieSize, dieSize, 3, 3));
                    }
                }

                // Hovered Die Outline
                if (r == hoveredRow && c == hoveredCol) {
                    g2.setColor(HOVER_HIGHLIGHT);
                    g2.setStroke(new BasicStroke(2.5f));
                    g2.draw(new RoundRectangle2D.Double(dx - 1, dy - 1, dieSize + 2, dieSize + 2, 4, 4));
                }
            }
        }

        // Draw HUD / Die Inspector Banner
        drawInspectionHud(g2, w, h);

        g2.dispose();
    }

    /**
     * Maps defect density (0.0 to 1.0) to a thermal color gradient:
     * Translucent Blue -> Cyan -> Yellow -> Orange -> Vivid Red
     */
    private Color getThermalColor(double density) {
        int alpha = (int) (130 + Math.min(1.0, density) * 110); // Alpha 130..240
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
            String status = (val < 0) ? "Substrate (Outside Wafer)" : (val == 1 ? "DEFECTIVE DIE" : "GOOD DIE (PASS)");
            double dens = (heatmapDensity != null && hoveredRow < heatmapDensity.length && hoveredCol < heatmapDensity[0].length && val >= 0) ? heatmapDensity[hoveredRow][hoveredCol] * 100.0 : 0.0;
            hudText = String.format(Locale.US, "Die (%d, %d) | %s | Local Defect Density: %.1f%% | Zoom: %.1fx",
                hoveredRow, hoveredCol, status, dens, zoomFactor);
        } else {
            hudText = String.format(Locale.US, "Wafer: %s [%dx%d] | Valid: %d | Defect: %d (%.1f%%) | Zoom: %.1fx (Scroll to zoom, drag to pan)",
                waferMap.getId(), waferMap.getRows(), waferMap.getCols(),
                waferMap.getValidCount(), waferMap.getDefectiveCount(), waferMap.getDefectPercentage(), zoomFactor);
        }

        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        FontMetrics fm = g2.getFontMetrics();
        int textW = fm.stringWidth(hudText);
        int pad = 8;
        int barH = 26;

        g2.setColor(new Color(15, 23, 42, 210)); // Dark semi-transparent
        g2.fillRoundRect(12, h - barH - 12, textW + pad * 2, barH, 8, 8);
        g2.setColor(new Color(51, 65, 85));
        g2.drawRoundRect(12, h - barH - 12, textW + pad * 2, barH, 8, 8);

        g2.setColor(Color.WHITE);
        g2.drawString(hudText, 12 + pad, h - 12 - (barH - fm.getAscent()) / 2);
    }

    /**
     * Renders wafer canvas to a BufferedImage for PNG export.
     */
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
