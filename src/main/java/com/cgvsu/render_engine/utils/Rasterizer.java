package com.cgvsu.render_engine.utils;

import com.cgvsu.render_engine.rendering.ZBuffer;
import com.cgvsu.utils.math.Vector3f;
import com.cgvsu.utils.math.Vertex;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

import java.util.List;

/**
 * Простейший растеризатор для треугольников.
 * Поддерживает:
 * - Z-буфер
 * - текстуры (с интерполяцией)
 * - простое освещение
 */
public class Rasterizer {

    private static final float K = 0.7f;               // коэффициент диффузного освещения
    private static final float AMBIENT = 1.0f - K;     // коэффициент фонового освещения
    private static final Color FILL_COLOR = Color.LIGHTGRAY;


    /**
     * Разбивает многоугольник на треугольники и растеризует каждый
     *
     * @param vertices вершины полигона
     * @param zBuffer  буфер глубины
     * @param pw       объект для записи пикселей
     * @param texture  текстура (может быть null)
     * @param lightPos позиция источника света
     */
    public static void triangulateAndRasterize(
            List<Vertex> vertices,
            ZBuffer zBuffer,
            PixelWriter pw,
            Image texture,
            Vector3f lightPos,
            javafx.scene.paint.Color baseColor, // базовый цвет
            boolean lightingEnabled            // флаг освещения
    ) {
        if (vertices.size() < 3) return;

        Vertex v0 = vertices.get(0);
        for (int i = 1; i < vertices.size() - 1; i++) {
            rasterizeTriangle(
                    v0,
                    vertices.get(i),
                    vertices.get(i + 1),
                    zBuffer,
                    pw,
                    texture,
                    lightPos,
                    baseColor,
                    lightingEnabled
            );
        }
    }



    /**
     * Растеризация треугольника с использованием barycentric координат
     */
    private static void rasterizeTriangle(
            Vertex v0,
            Vertex v1,
            Vertex v2,
            ZBuffer zBuffer,
            PixelWriter pw,
            Image tex,
            Vector3f lightPos,
            javafx.scene.paint.Color modelBaseColor,  // базовый цвет модели
            boolean lightingEnabled                   // включено ли освещение
    ) {
        // ограничивающий прямоугольник
        int minX = (int) Math.max(0, Math.floor(Math.min(v0.x, Math.min(v1.x, v2.x))));
        int maxX = (int) Math.min(zBuffer.getWidth() - 1, Math.ceil(Math.max(v0.x, Math.max(v1.x, v2.x))));
        int minY = (int) Math.max(0, Math.floor(Math.min(v0.y, Math.min(v1.y, v2.y))));
        int maxY = (int) Math.min(zBuffer.getHeight() - 1, Math.ceil(Math.max(v0.y, Math.max(v1.y, v2.y))));

        float area = edge(v0.x, v0.y, v1.x, v1.y, v2.x, v2.y);
        if (Math.abs(area) < 1e-6f) return; // вырожденный треугольник

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {

                float px = x + 0.5f;
                float py = y + 0.5f;

                float w0 = edge(v1.x, v1.y, v2.x, v2.y, px, py) / area;
                float w1 = edge(v2.x, v2.y, v0.x, v0.y, px, py) / area;
                float w2 = edge(v0.x, v0.y, v1.x, v1.y, px, py) / area;

                if (w0 < 0 || w1 < 0 || w2 < 0) continue;

                float z = w0 * v0.z + w1 * v1.z + w2 * v2.z;
                if (!zBuffer.testAndSet(x, y, z)) continue;

                Vector3f worldPos = v0.worldPos.multiply(w0)
                        .add(v1.worldPos.multiply(w1))
                        .add(v2.worldPos.multiply(w2));

                Vector3f normal = v0.normal.multiply(w0)
                        .add(v1.normal.multiply(w1))
                        .add(v2.normal.multiply(w2))
                        .normalize();

                Color finalColor;

                if (tex != null) {
                    float invW = w0 * v0.invW + w1 * v1.invW + w2 * v2.invW;
                    float u = (w0 * v0.u * v0.invW + w1 * v1.u * v1.invW + w2 * v2.u * v2.invW) / invW;
                    float v = (w0 * v0.v * v0.invW + w1 * v1.v * v1.invW + w2 * v2.v * v2.invW) / invW;

                    int tx = clamp((float) (u * (tex.getWidth() - 1)), 0, (int) tex.getWidth() - 1);
                    int ty = clamp((float) ((1 - v) * (tex.getHeight() - 1)), 0, (int) tex.getHeight() - 1);

                    Color tc = tex.getPixelReader().getColor(tx, ty);

                    float intensity = lightingEnabled ? AMBIENT + K * Math.max(0, normal.dot(lightPos.subtract(worldPos).normalize())) : 1.0f;

                    finalColor = new Color(
                            clamp01(tc.getRed() * intensity),
                            clamp01(tc.getGreen() * intensity),
                            clamp01(tc.getBlue() * intensity),
                            tc.getOpacity()
                    );
                } else {
                    float intensity = lightingEnabled ? AMBIENT + K * Math.max(0, normal.dot(lightPos.subtract(worldPos).normalize())) : 1.0f;

                    finalColor = new Color(
                            clamp01(modelBaseColor.getRed() * intensity),
                            clamp01(modelBaseColor.getGreen() * intensity),
                            clamp01(modelBaseColor.getBlue() * intensity),
                            modelBaseColor.getOpacity()
                    );
                }

                pw.setColor(x, y, finalColor);
            }
        }
    }


    // ================= HELPERS =================

    /** Вспомогательная функция для barycentric координат */
    private static float edge(float x0, float y0, float x1, float y1, float x2, float y2) {
        return (x2 - x0) * (y1 - y0) - (y2 - y0) * (x1 - x0);
    }

    /** Ограничение int в диапазоне */
    private static int clamp(float v, int min, int max) {
        return (int) Math.max(min, Math.min(max, v));
    }

    /** Ограничение double в [0,1] */
    private static double clamp01(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }
}
