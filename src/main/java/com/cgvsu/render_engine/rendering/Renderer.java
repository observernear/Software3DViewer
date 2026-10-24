package com.cgvsu.render_engine.rendering;

import com.cgvsu.model.Model;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.render_engine.utils.Rasterizer;
import com.cgvsu.render_engine.transform.Transform;
import com.cgvsu.render_engine.utils.VertexProjector;
import com.cgvsu.utils.math.Matrix4f;
import com.cgvsu.utils.math.Vector3f;
import com.cgvsu.utils.math.Vertex;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.ArrayList;

import static com.cgvsu.render_engine.utils.VertexProjector.projectPolygon;

/**
 * Основной рендерер. Рисует модели с учётом настроек RenderSettings.
 * Может работать в "быстром" режиме, с текстурой и освещением, а также рисовать каркас.
 */
public class Renderer implements RendererImpl {

    @Override
    public void render(
            GraphicsContext gc,
            Camera camera,
            Model model,
            int width,
            int height,
            Transform transform,
            RenderSettings settings
    ) {
        if (model == null) return;

        // ===== Model-View-Projection =====
        var modelMatrix = com.cgvsu.render_engine.GraphicConveyor.createModelMatrix(
                transform.scaleX, transform.scaleY, transform.scaleZ,
                transform.rotateX, transform.rotateY, transform.rotateZ,
                transform.translateX, transform.translateY, transform.translateZ
        );

        var mvp = camera.getProjectionMatrix()
                .multiply(camera.getViewMatrix())
                .multiply(modelMatrix);

        Vector3f lightPos = (camera != null) ? camera.getPosition() : null;

        boolean useTexture = settings.isUseTexture() && model.getTexture() != null;
        boolean useLighting = settings.isUseLighting() && lightPos != null;

        boolean fastMode = !useTexture && !useLighting;

        if (fastMode) {
            drawFast(gc, model, mvp, width, height, settings.getBaseColor());
        } else {
            drawWithRasterizer(gc, model, mvp, width, height, useTexture, useLighting, lightPos);
        }

        if (settings.isWireframe()) {
            drawWireframeZBuffer(gc, model, mvp, width, height);
        }
    }

    /** Быстрая отрисовка без текстур и освещения */
    private void drawFast(GraphicsContext gc,
                          Model model,
                          Matrix4f mvp,
                          int width,
                          int height,
                          Color baseColor) {
        gc.clearRect(0, 0, width, height);
        gc.setFill(baseColor);

        for (var polygon : model.getPolygons()) {
            ArrayList<Vertex> verts = projectPolygon(model, polygon, mvp, width, height);
            if (verts.size() < 3) continue;

            double[] x = new double[verts.size()];
            double[] y = new double[verts.size()];

            for (int i = 0; i < verts.size(); i++) {
                x[i] = verts.get(i).x;
                y[i] = verts.get(i).y;
            }

            gc.fillPolygon(x, y, verts.size());
        }
    }

    /** Отрисовка с использованием растеризатора, текстур и освещения */
    private void drawWithRasterizer(GraphicsContext gc,
                                    Model model,
                                    Matrix4f mvp,
                                    int width,
                                    int height,
                                    boolean useTexture,
                                    boolean useLighting,
                                    Vector3f lightPos) {
        WritableImage frame = new WritableImage(width, height);
        PixelWriter pw = frame.getPixelWriter();
        ZBuffer zBuffer = new ZBuffer(width, height);

        if (!useLighting) {
            model.setLightingEnabled(false);
        }

        for (var polygon : model.getPolygons()) {
            ArrayList<Vertex> verts = projectPolygon(model, polygon, mvp, width, height);
            if (verts.size() < 3) continue;

            Rasterizer.triangulateAndRasterize(
                    verts,
                    zBuffer,
                    pw,
                    useTexture ? model.getTexture() : null,
                    lightPos,
                    model.getBaseColor(),
                    model.isLightingEnabled()
            );
        }

        gc.drawImage(frame, 0, 0);
    }

    /** Рисует каркас модели */
    private void drawWireframeZBuffer(GraphicsContext gc,
                                      Model model,
                                      Matrix4f mvp,
                                      int width,
                                      int height) {

        WritableImage frame = new WritableImage(width, height);
        PixelWriter pw = frame.getPixelWriter();
        ZBuffer zBuffer = new ZBuffer(width, height);

        for (var polygon : model.getPolygons()) {
            ArrayList<Vertex> verts =
                    VertexProjector.projectPolygon(model, polygon, mvp, width, height);

            if (verts.size() < 2) continue;

            for (int i = 0; i < verts.size(); i++) {
                Vertex v1 = verts.get(i);
                Vertex v2 = verts.get((i + 1) % verts.size());

                drawLineZBuffer(v1, v2, zBuffer, pw);
            }
        }

        gc.drawImage(frame, 0, 0);
    }

    /** Рисует линию между двумя вершинами с проверкой Z-буфера */
    private void drawLineZBuffer(Vertex v1, Vertex v2,
                                 ZBuffer zBuffer,
                                 PixelWriter pw) {

        int x0 = Math.round(v1.x);
        int y0 = Math.round(v1.y);
        int x1 = Math.round(v2.x);
        int y1 = Math.round(v2.y);

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            // Интерполяция Z по линии
            float t = (dx + dy == 0) ? 0 :
                    (float) Math.hypot(x0 - v1.x, y0 - v1.y) /
                            (float) Math.hypot(v2.x - v1.x, v2.y - v1.y);

            float z = v1.z * (1 - t) + v2.z * t;

            if (x0 >= 0 && x0 < zBuffer.getWidth()
                    && y0 >= 0 && y0 < zBuffer.getHeight()) {

                if (zBuffer.testAndSet(x0, y0, z)) {
                    pw.setColor(x0, y0, Color.BLACK);
                }
            }

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
    }

    /** Применяет Transform к модели и возвращает новую копию */
    @Override
    public Model applyTransform(Model model, Transform transform) {
        if (model == null) return null;

        Model transformedModel = new Model(model); // копия модели

        for (int i = 0; i < transformedModel.getVertices().size(); i++) {
            Vector3f v = transformedModel.getVertices().get(i);

            Vector3f scaled = new Vector3f(
                    v.getX() * transform.scaleX,
                    v.getY() * transform.scaleY,
                    v.getZ() * transform.scaleZ
            );

            Vector3f rotated = transform.rotate(scaled);

            Vector3f translated = new Vector3f(
                    rotated.getX() + transform.translateX,
                    rotated.getY() + transform.translateY,
                    rotated.getZ() + transform.translateZ
            );

            transformedModel.getVertices().set(i, translated);
        }

        return transformedModel;
    }
}
