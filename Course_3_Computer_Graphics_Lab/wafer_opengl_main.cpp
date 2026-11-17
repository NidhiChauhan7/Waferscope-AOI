/**
 * Course 3: Computer Graphics Lab (2310218L)
 * OpenGL Wafer Visualizer in C++
 * Compatible with Code::Blocks / Win32 OpenGL
 *
 * Demonstrates:
 * 1. OpenGL Primitives (GL_POLYGON, GL_LINE_LOOP, GL_QUADS)
 * 2. 2D Transformations: glTranslatef, glScalef, glRotatef
 * 3. Color mapping: glColor3f for silicon, pass dies, and defect clusters
 */

#include <windows.h>
#include <gl/gl.h>
#include <cmath>

const double PI = 3.14159265358979323846;

// Draw circular wafer substrate
void drawCircularWafer(float cx, float cy, float r, int segments) {
    glColor3f(0.88f, 0.91f, 0.94f); // Polished silicon substrate
    glBegin(GL_POLYGON);
    for (int i = 0; i < segments; i++) {
        float theta = 2.0f * PI * float(i) / float(segments);
        float x = r * cosf(theta);
        float y = r * sinf(theta);
        glVertex2f(cx + x, cy + y);
    }
    glEnd();

    // Circular Bevel Border
    glColor3f(0.28f, 0.33f, 0.41f);
    glLineWidth(2.5f);
    glBegin(GL_LINE_LOOP);
    for (int i = 0; i < segments; i++) {
        float theta = 2.0f * PI * float(i) / float(segments);
        float x = r * cosf(theta);
        float y = r * sinf(theta);
        glVertex2f(cx + x, cy + y);
    }
    glEnd();

    // Orientation Notch at bottom
    glColor3f(0.12f, 0.16f, 0.23f);
    glBegin(GL_TRIANGLES);
    glVertex2f(cx - 0.08f, cy - r + 0.01f);
    glVertex2f(cx, cy - r + 0.08f);
    glVertex2f(cx + 0.08f, cy - r + 0.01f);
    glEnd();
}

// Render sample die grid
void drawWaferDies() {
    float cellSize = 0.08f;
    float startX = -0.6f;
    float startY = -0.6f;

    for (int r = 0; r < 15; r++) {
        for (int c = 0; c < 15; c++) {
            float x = startX + c * cellSize;
            float y = startY + r * cellSize;
            float distFromCenter = sqrtf((x)*(x) + (y)*(y));

            if (distFromCenter > 0.75f) continue; // Outside circular boundary

            if (distFromCenter < 0.25f) {
                glColor3f(0.94f, 0.27f, 0.27f); // Defective Center Cluster (Red)
            } else {
                glColor3f(0.13f, 0.77f, 0.37f); // Good Passing Dies (Green)
            }

            glBegin(GL_QUADS);
            glVertex2f(x, y);
            glVertex2f(x + cellSize * 0.85f, y);
            glVertex2f(x + cellSize * 0.85f, y + cellSize * 0.85f);
            glVertex2f(x, y + cellSize * 0.85f);
            glEnd();
        }
    }
}

void displayScene(float zoom, float rotationAngle) {
    glClear(GL_COLOR_BUFFER_BIT);
    glLoadIdentity();

    // Apply Geometric Transformations (CO3)
    glScalef(zoom, zoom, 1.0f);
    glRotatef(rotationAngle, 0.0f, 0.0f, 1.0f);

    drawCircularWafer(0.0f, 0.0f, 0.85f, 64);
    drawWaferDies();

    glFlush();
}
