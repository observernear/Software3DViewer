package com.cgvsu.render_engine.utils;

import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.util.ArrayList;
import java.util.List;

/**
 * Триангуляция модели:
 * Преобразует многоугольники с более чем 3 вершинами в набор треугольников.
 * Используется метод "fan triangulation" — вентиль, хорошо подходит для выпуклых многоугольников.
 */
public class Triangulator {

    /**
     * Применяет триангуляцию ко всем полигонам модели.
     *
     * @param model Модель, полигоны которой нужно триангулировать
     */
    public static void triangulate(Model model) {
        if (model == null || model.getPolygons() == null) return;

        ArrayList<Polygon> newPolygons = new ArrayList<>();

        for (Polygon polygon : model.getPolygons()) {
            List<Integer> vertices = polygon.getVertexIndices();

            if (vertices.size() <= 3) {
                // Уже треугольник, добавляем как есть
                newPolygons.add(polygon);
                continue;
            }

            // Fan triangulation: фиксируем вершину 0, создаем треугольники с каждой парой (i, i+1)
            for (int i = 1; i < vertices.size() - 1; i++) {
                Polygon.Builder builder = Polygon.builder()
                        .addVertexIndex(vertices.get(0))
                        .addVertexIndex(vertices.get(i))
                        .addVertexIndex(vertices.get(i + 1));

                // Копируем текстурные индексы, если они есть
                List<Integer> texIndices = polygon.getTextureVertexIndices();
                if (!texIndices.isEmpty()) {
                    builder.addTextureVertexIndex(texIndices.get(0))
                            .addTextureVertexIndex(texIndices.get(i))
                            .addTextureVertexIndex(texIndices.get(i + 1));
                }

                // Можно аналогично скопировать нормали, если потребуется
                newPolygons.add(builder.build());
            }
        }

        // Обновляем полигоны модели на триангулированные
        model.setPolygons(newPolygons);
    }
}
