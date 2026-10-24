package com.cgvsu.model;

import com.cgvsu.render_engine.transform.Transform;
import com.cgvsu.utils.math.Vector2f;
import com.cgvsu.utils.math.Vector3f;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Model {
    // --- Основные данные модели ---
    private ArrayList<Vector3f> vertices;          // Вершины модели
    private ArrayList<Vector2f> textureVertices;   // Вершины текстуры
    private ArrayList<Vector3f> normals;           // Нормали
    private ArrayList<Polygon> polygons;           // Полигоны
    private String name;                            // Имя модели
    private final ObjectProperty<Transform> transform = new SimpleObjectProperty<>(new Transform()); // Трансформация модели
    private Image texture;                          // Текстура модели
    private javafx.scene.paint.Color baseColor = javafx.scene.paint.Color.LIGHTGRAY;
    private boolean lightingEnabled = true; // включение/выключение освещения

    // --------------------- Конструкторы ---------------------

    /** Пустой конструктор — создаёт пустую модель */
    public Model() {
        this.vertices = new ArrayList<>();
        this.textureVertices = new ArrayList<>();
        this.normals = new ArrayList<>();
        this.polygons = new ArrayList<>();
        this.transform.set(new Transform());
    }

    /** Конструктор копирования — глубокая копия другой модели */
    /** Конструктор копирования — создаёт глубокую копию другой модели */
    public Model(Model other) {
        // Копируем вершины
        this.vertices = new ArrayList<>();
        for (Vector3f v : other.vertices) {
            this.vertices.add(new Vector3f(v.getX(), v.getY(), v.getZ()));
        }

        // Копируем вершины текстуры
        this.textureVertices = new ArrayList<>();
        for (Vector2f tv : other.textureVertices) {
            this.textureVertices.add(new Vector2f(tv.getX(), tv.getY()));
        }

        // Копируем нормали
        this.normals = new ArrayList<>();
        for (Vector3f n : other.normals) {
            this.normals.add(new Vector3f(n.getX(), n.getY(), n.getZ()));
        }

        // Глубокое копирование полигонов
        this.polygons = new ArrayList<>();
        for (Polygon p : other.polygons) {
            this.polygons.add(p.toBuilder().build());
        }

        this.name = other.name;

        // Просто создаём новый Transform
        this.transform.set(new Transform());

        // Ссылка на текстуру (можно менять на глубокое копирование при необходимости)
        this.texture = other.texture;
    }


    // --------------------- Transform ---------------------

    public ObjectProperty<Transform> transformProperty() {
        return transform;
    }

    public Transform getTransform() {
        return transform.get();
    }

    public void setTransform(Transform transform) {
        this.transform.set(transform != null ? transform : new Transform());
    }

    // --------------------- Name ---------------------

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // --------------------- Vertices ---------------------

    public ArrayList<Vector3f> getVertices() { return vertices; }
    public void setVertices(ArrayList<Vector3f> vertices) {
        this.vertices = vertices != null ? vertices : new ArrayList<>();
    }

    // --------------------- Texture Vertices ---------------------

    public ArrayList<Vector2f> getTextureVertices() { return textureVertices; }
    public void setTextureVertices(ArrayList<Vector2f> textureVertices) {
        this.textureVertices = textureVertices != null ? textureVertices : new ArrayList<>();
    }

    // --------------------- Normals ---------------------

    public List<Vector3f> getNormals() { return normals; }
    public void setNormals(List<Vector3f> normals) {
        this.normals = normals != null ? new ArrayList<>(normals) : new ArrayList<>();
    }

    // --------------------- Polygons ---------------------

    public ArrayList<Polygon> getPolygons() { return polygons; }
    public void setPolygons(ArrayList<Polygon> polygons) {
        this.polygons = polygons != null ? polygons : new ArrayList<>();
    }

    /** Удаляет один полигон по индексу */
    public boolean deletePolygon(int polygonIndex) {
        if (polygonIndex < 0 || polygonIndex >= polygons.size()) return false;
        polygons.remove(polygonIndex);
        return true;
    }

    /** Удаляет несколько полигонов по списку индексов */
    public int deletePolygons(List<Integer> polygonIndices) {
        if (polygonIndices == null || polygonIndices.isEmpty()) return 0;

        // Убираем дубликаты и сортируем по убыванию
        List<Integer> sortedIndices = new ArrayList<>(new HashSet<>(polygonIndices));
        sortedIndices.sort(Collections.reverseOrder());

        int deletedCount = 0;
        for (int index : sortedIndices) {
            if (index >= 0 && index < polygons.size()) {
                polygons.remove(index);
                deletedCount++;
            }
        }
        return deletedCount;
    }

    // --------------------- Vertex Deletion ---------------------

    /** Удаляет одну вершину и обновляет все полигоны */
    public boolean deleteVertex(int vertexIndex) {
        if (vertexIndex < 0 || vertexIndex >= vertices.size()) return false;

        deletePolygonsContainingVertex(vertexIndex); // удаляем полигоны с этой вершиной
        vertices.remove(vertexIndex);               // удаляем саму вершину
        updatePolygonIndicesAfterSingleDeletion(vertexIndex);
        return true;
    }

    /** Удаляет несколько вершин безопасно */
    public int deleteVertices(List<Integer> vertexIndices) {
        if (vertexIndices == null || vertexIndices.isEmpty()) return 0;

        List<Integer> sortedIndices = new ArrayList<>(new HashSet<>(vertexIndices));
        sortedIndices.sort(Collections.reverseOrder()); // удаляем с конца

        int deletedCount = 0;
        for (int vertexIndex : sortedIndices) {
            if (vertexIndex < 0 || vertexIndex >= vertices.size()) continue;
            deletePolygonsContainingVertex(vertexIndex);
            vertices.remove(vertexIndex);
            updatePolygonIndicesAfterSingleDeletion(vertexIndex);
            deletedCount++;
        }

        return deletedCount;
    }

    /** Обновляет индексы полигонов после удаления одной вершины */
    private void updatePolygonIndicesAfterSingleDeletion(int deletedIndex) {
        for (int i = 0; i < polygons.size(); i++) {
            Polygon polygon = polygons.get(i);
            List<Integer> updatedIndices = new ArrayList<>();
            for (int idx : polygon.getVertexIndices()) {
                updatedIndices.add(idx > deletedIndex ? idx - 1 : idx);
            }
            polygons.set(i, createUpdatedPolygon(polygon, updatedIndices));
        }
    }

    /** Удаляет все неиспользуемые вершины */
    public int deleteUnusedVertices() {
        Set<Integer> used = new HashSet<>();
        for (Polygon p : polygons) used.addAll(p.getVertexIndices());

        List<Integer> unused = new ArrayList<>();
        for (int i = 0; i < vertices.size(); i++) {
            if (!used.contains(i)) unused.add(i);
        }
        return deleteVertices(unused);
    }

    // --------------------- Polygon Utilities ---------------------

    /** Удаляет все полигоны, содержащие указанную вершину */
    public int deletePolygonsContainingVertex(int vertexIndex) {
        List<Integer> polygonsToDelete = new ArrayList<>();
        for (int i = 0; i < polygons.size(); i++) {
            if (polygons.get(i).getVertexIndices().contains(vertexIndex)) {
                polygonsToDelete.add(i);
            }
        }
        return deletePolygons(polygonsToDelete);
    }

    /** Удаляет полигоны, содержащие любую из вершин в списке */
    public int deletePolygonsContainingVertices(List<Integer> vertexIndices) {
        if (vertexIndices == null || vertexIndices.isEmpty()) return 0;

        Set<Integer> vertexSet = new HashSet<>(vertexIndices);
        List<Integer> polygonsToDelete = new ArrayList<>();
        for (int i = 0; i < polygons.size(); i++) {
            Polygon polygon = polygons.get(i);
            if (polygon.getVertexIndices().stream().anyMatch(vertexSet::contains)) {
                polygonsToDelete.add(i);
            }
        }
        return deletePolygons(polygonsToDelete);
    }

    /** Создает новый Polygon с обновлёнными индексами */
    private Polygon createUpdatedPolygon(Polygon original, List<Integer> newVertexIndices) {
        Polygon.Builder builder = original.toBuilder().setVertexIndices(newVertexIndices);

        if (!original.getTextureVertexIndices().isEmpty())
            builder.setTextureVertexIndices(new ArrayList<>(original.getTextureVertexIndices()));

        if (!original.getNormalIndices().isEmpty())
            builder.setNormalIndices(new ArrayList<>(original.getNormalIndices()));

        return builder.build();
    }

    // --------------------- Validation ---------------------

    /** Проверка модели на валидность: все полигоны имеют ≥3 вершин и корректные индексы */
    public boolean validateModel() {
        for (Polygon p : polygons) {
            if (p.getVertexIndices().size() < 3) return false;
            for (int idx : p.getVertexIndices()) {
                if (idx < 0 || idx >= vertices.size()) return false;
            }
        }
        return true;
    }

    /** Удаляет все невалидные полигоны и возвращает количество удалённых */
    public int cleanInvalidPolygons() {
        List<Polygon> validPolygons = new ArrayList<>();
        for (Polygon p : polygons) {
            boolean valid = p.getVertexIndices().size() >= 3 &&
                    p.getVertexIndices().stream().allMatch(idx -> idx >= 0 && idx < vertices.size());
            if (valid) validPolygons.add(p);
        }
        int removed = polygons.size() - validPolygons.size();
        polygons = new ArrayList<>(validPolygons);
        return removed;
    }

    // --------------------- Utilities ---------------------

    public Set<Integer> getUsedVertices() {
        Set<Integer> used = new HashSet<>();
        for (Polygon p : polygons) used.addAll(p.getVertexIndices());
        return used;
    }

    public List<Integer> getUnusedVertices() {
        Set<Integer> used = getUsedVertices();
        List<Integer> unused = new ArrayList<>();
        for (int i = 0; i < vertices.size(); i++) if (!used.contains(i)) unused.add(i);
        return unused;
    }

    /** Очищает нормали всех полигонов */
    public void dropPolygonNormals() {
        for (Polygon p : polygons) p.getNormalIndices().clear();
    }

    // --------------------- Texture ---------------------

    public void setTexture(Image texture) { this.texture = texture; }
    public Image getTexture() { return texture; }

    public void setBaseColor(javafx.scene.paint.Color color) {
        if (color != null) this.baseColor = color;
    }

    public javafx.scene.paint.Color getBaseColor() {
        return baseColor;
    }

    // --- Lighting ---
    public void setLightingEnabled(boolean enabled) {
        this.lightingEnabled = enabled;
    }

    public boolean isLightingEnabled() {
        return lightingEnabled;
    }
}
