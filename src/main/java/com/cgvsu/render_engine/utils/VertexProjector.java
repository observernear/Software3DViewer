package com.cgvsu.render_engine.utils;

import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import com.cgvsu.utils.math.Matrix4f;
import com.cgvsu.utils.math.Vector3f;
import com.cgvsu.utils.math.Vector4f;
import com.cgvsu.utils.math.Vertex;

import java.util.ArrayList;

import static com.cgvsu.render_engine.GraphicConveyor.multiplyMatrix4ByVector4;

/**
 * Класс для проекции вершин полигона из мировой системы координат
 * в экранное пространство с учетом матрицы MVP (Model-View-Projection).
 */
public class VertexProjector {

    /**
     * Проецирует вершины полигона в экранное пространство.
     *
     * @param model   Модель, содержащая вершины, текстуры и нормали
     * @param polygon Полигон, который нужно спроецировать
     * @param mvp     Матрица Model-View-Projection
     * @param width   Ширина канвы/экрана
     * @param height  Высота канвы/экрана
     * @return Список экранных вершин с текстурными координатами и нормалями
     */
    public static ArrayList<Vertex> projectPolygon(
            Model model,
            Polygon polygon,
            Matrix4f mvp,
            int width,
            int height
    ) {
        ArrayList<Vertex> result = new ArrayList<>();

        // Проходим по всем вершинам полигона
        for (int i = 0; i < polygon.getVertexIndices().size(); i++) {
            int vi = polygon.getVertexIndices().get(i);

            // Мировая позиция вершины
            Vector3f worldPos = model.getVertices().get(vi);

            // Нормаль вершины (усреднённая по полигонам)
            Vector3f normal = model.getNormals().get(vi).normalize();

            // Преобразуем в clip space через MVP
            Vector4f clip = multiplyMatrix4ByVector4(
                    mvp,
                    new Vector4f(worldPos.getX(), worldPos.getY(), worldPos.getZ(), 1.0f)
            );

            // Проверка на деление на ноль (W близко к нулю)
            if (Math.abs(clip.getW()) < 1e-6f) continue;

            float invW = 1.0f / clip.getW();

            // Нормализованные координаты устройства (NDC)
            float ndcX = clip.getX() * invW;
            float ndcY = clip.getY() * invW;
            float ndcZ = clip.getZ() * invW;

            // Преобразуем в экранные координаты
            float screenX = (ndcX + 1.0f) * 0.5f * width;
            float screenY = (1.0f - ndcY) * 0.5f * height;

            // Текстурные координаты (если есть)
            float u = 0.0f;
            float vTex = 0.0f;

            if (!polygon.getTextureVertexIndices().isEmpty()) {
                int ti = polygon.getTextureVertexIndices().get(i);
                if (ti >= 0 && ti < model.getTextureVertices().size()) {
                    u = model.getTextureVertices().get(ti).getX();
                    vTex = model.getTextureVertices().get(ti).getY();
                }
            }

            // Добавляем вершину в результат
            result.add(new Vertex(
                    screenX,
                    screenY,
                    ndcZ,
                    invW,
                    u,
                    vTex,
                    worldPos,
                    normal
            ));
        }

        return result;
    }
}
