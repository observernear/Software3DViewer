package com.cgvsu.render_engine.transform;

import com.cgvsu.model.Model;
import com.cgvsu.utils.math.Matrix4f;
import com.cgvsu.utils.math.Vector3f;

import java.util.ArrayList;

import static com.cgvsu.render_engine.GraphicConveyor.multiplyMatrix4ByVector3;

/**
 * Утилита для применения трансформаций к модели.
 * Позволяет масштабировать, вращать и перемещать модель.
 */
public class ModelTransformer {

    /**
     * Применяет трансформацию к модели и возвращает новую модель с обновлёнными вершинами.
     *
     * @param original Исходная модель
     * @param t        Трансформация
     * @return новая модель с применённой трансформацией (оригинальная модель не изменяется)
     */
    public static Model applyTransform(Model original, Transform t) {
        if (original == null || t == null) {
            return null;
        }

        // Создаём матрицу модели из параметров трансформации
        Matrix4f modelMatrix = com.cgvsu.render_engine.GraphicConveyor.createModelMatrix(
                t.scaleX, t.scaleY, t.scaleZ,
                t.rotateX, t.rotateY, t.rotateZ,
                t.translateX, t.translateY, t.translateZ
        );

        // Создаём новую модель, чтобы не изменять исходную
        Model transformed = new Model();

        // Применяем матрицу ко всем вершинам
        ArrayList<Vector3f> transformedVertices = new ArrayList<>();
        for (Vector3f v : original.getVertices()) {
            transformedVertices.add(multiplyMatrix4ByVector3(modelMatrix, v));
        }

        transformed.setVertices(transformedVertices);

        // Копируем данные текстур, нормалей и полигонов
        transformed.setTextureVertices(new ArrayList<>(original.getTextureVertices()));
        transformed.setNormals(new ArrayList<>(original.getNormals()));
        transformed.setPolygons(new ArrayList<>(original.getPolygons()));

        // Копируем текстуру
        transformed.setTexture(original.getTexture());

        return transformed;
    }
}
