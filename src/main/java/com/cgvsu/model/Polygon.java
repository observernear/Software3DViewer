package com.cgvsu.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Класс Polygon представляет один полигон модели.
 * Полигон хранит индексы вершин, текстурных координат и нормалей.
 * Используется паттерн Builder для удобного и безопасного создания экземпляров.
 */
public class Polygon {

    // Индексы вершин (immutable список)
    private final List<Integer> vertexIndices;

    // Индексы вершин текстур (immutable список)
    private final List<Integer> textureVertexIndices;

    // Индексы нормалей (immutable список)
    private final List<Integer> normalIndices;

    /**
     * Приватный конструктор — экземпляры создаются только через Builder
     */
    private Polygon(List<Integer> vertexIndices, List<Integer> textureVertexIndices, List<Integer> normalIndices) {
        this.vertexIndices = vertexIndices != null ?
                Collections.unmodifiableList(new ArrayList<>(vertexIndices)) : Collections.emptyList();
        this.textureVertexIndices = textureVertexIndices != null ?
                Collections.unmodifiableList(new ArrayList<>(textureVertexIndices)) : Collections.emptyList();
        this.normalIndices = normalIndices != null ?
                Collections.unmodifiableList(new ArrayList<>(normalIndices)) : Collections.emptyList();
    }

    /**
     * Builder для удобного создания полигонов
     */
    public static class Builder {
        private List<Integer> vertexIndices = new ArrayList<>();
        private List<Integer> textureVertexIndices = new ArrayList<>();
        private List<Integer> normalIndices = new ArrayList<>();

        public Builder addVertexIndex(int index) {
            this.vertexIndices.add(index);
            return this;
        }

        public Builder addTextureVertexIndex(int index) {
            this.textureVertexIndices.add(index);
            return this;
        }

        public Builder addNormalIndex(int index) {
            this.normalIndices.add(index);
            return this;
        }

        public Builder setVertexIndices(List<Integer> indices) {
            this.vertexIndices = new ArrayList<>(indices);
            return this;
        }

        public Builder setTextureVertexIndices(List<Integer> indices) {
            this.textureVertexIndices = new ArrayList<>(indices);
            return this;
        }

        public Builder setNormalIndices(List<Integer> indices) {
            this.normalIndices = new ArrayList<>(indices);
            return this;
        }

        public Polygon build() {
            return new Polygon(vertexIndices, textureVertexIndices, normalIndices);
        }
    }

    // ------------------- Геттеры -------------------

    public List<Integer> getVertexIndices() {
        return vertexIndices;
    }

    public List<Integer> getTextureVertexIndices() {
        return textureVertexIndices;
    }

    public List<Integer> getNormalIndices() {
        return normalIndices;
    }

    // ------------------- Удобные методы -------------------

    /** Создаёт новый Builder с копией текущего полигона */
    public Builder toBuilder() {
        return new Builder()
                .setVertexIndices(vertexIndices)
                .setTextureVertexIndices(textureVertexIndices)
                .setNormalIndices(normalIndices);
    }

    /** Статический метод для начала построения нового полигона */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "Polygon{" +
                "vertices=" + vertexIndices.size() +
                ", textures=" + textureVertexIndices.size() +
                ", normals=" + normalIndices.size() +
                '}';
    }
}
