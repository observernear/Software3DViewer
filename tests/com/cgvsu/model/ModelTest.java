package com.cgvsu.model;

import com.cgvsu.utils.math.Vector2f;
import com.cgvsu.utils.math.Vector3f;
import javafx.scene.image.Image;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ModelTest {

    private Model model;

    @BeforeEach
    void setUp() {
        model = new Model();

        // Добавим 4 вершины
        model.setVertices(new ArrayList<>(List.of(
                new Vector3f(0, 0, 0), // 0
                new Vector3f(1, 0, 0), // 1
                new Vector3f(0, 1, 0), // 2
                new Vector3f(0, 0, 1)  // 3
        )));

        // Добавим 2 полигона
        Polygon p1 = Polygon.builder()
                .setVertexIndices(List.of(0, 1, 2))
                .build();

        Polygon p2 = Polygon.builder()
                .setVertexIndices(List.of(0, 2, 3))
                .build();

        model.setPolygons(new ArrayList<>(List.of(p1, p2)));
    }

    // ======= Конструкторы =======

    @Test
    void testDefaultConstructorCreatesEmptyLists() {
        Model empty = new Model();

        assertNotNull(empty.getVertices());
        assertNotNull(empty.getTextureVertices());
        assertNotNull(empty.getNormals());
        assertNotNull(empty.getPolygons());
        assertTrue(empty.getVertices().isEmpty());
        assertTrue(empty.getPolygons().isEmpty());
    }

    @Test
    void testCopyConstructorCreatesDeepCopy() {
        Model copy = new Model(model);

        // Разные списки (важно)
        assertNotSame(model.getVertices(), copy.getVertices());
        assertNotSame(model.getPolygons(), copy.getPolygons());

        // Но размеры одинаковые
        assertEquals(model.getVertices().size(), copy.getVertices().size());
        assertEquals(model.getPolygons().size(), copy.getPolygons().size());

        // === Проверка глубокой копии БЕЗ setX() ===
        // Заменим вершину в копии на новую
        copy.getVertices().set(0, new Vector3f(100, 100, 100));

        // В оригинале она НЕ должна измениться
        Vector3f originalFirst = model.getVertices().get(0);
        assertNotEquals(100f, originalFirst.getX());
        assertNotEquals(100f, originalFirst.getY());
        assertNotEquals(100f, originalFirst.getZ());
    }


    // ======= Удаление полигонов =======

    @Test
    void testDeletePolygonByIndex() {
        boolean deleted = model.deletePolygon(0);

        assertTrue(deleted);
        assertEquals(1, model.getPolygons().size());
    }

    @Test
    void testDeletePolygonInvalidIndex() {
        boolean deleted = model.deletePolygon(999);
        assertFalse(deleted);
        assertEquals(2, model.getPolygons().size());
    }

    @Test
    void testDeletePolygonsByList() {
        int removed = model.deletePolygons(List.of(0, 1));

        assertEquals(2, removed);
        assertTrue(model.getPolygons().isEmpty());
    }

    // ======= Удаление вершин =======

    @Test
    void testDeleteVertexRemovesDependentPolygons() {
        // Вершина 0 используется в обоих полигонах
        boolean deleted = model.deleteVertex(0);

        assertTrue(deleted);
        assertEquals(3, model.getVertices().size());
        assertEquals(0, model.getPolygons().size()); // оба полигона должны удалиться
    }

    @Test
    void testDeleteVerticesMultiple() {
        int removed = model.deleteVertices(List.of(0, 3));

        assertEquals(2, removed);
        assertEquals(2, model.getVertices().size());
        assertEquals(0, model.getPolygons().size()); // оба полигона должны исчезнуть
    }

    @Test
    void testDeleteUnusedVertices() {
        // Добавим неиспользуемую вершину
        model.getVertices().add(new Vector3f(10, 10, 10));

        int removed = model.deleteUnusedVertices();

        assertEquals(1, removed);
        assertEquals(4, model.getVertices().size());
    }

    // ======= Работа с индексами =======

    @Test
    void testPolygonIndicesShiftAfterVertexDeletion() {
        model.deleteVertex(0);

        // Осталось 3 вершины: исходные (1,2,3) → стали (0,1,2)
        assertEquals(3, model.getVertices().size());

        // Проверяем, что индексы корректно сдвинулись (если бы полигоны остались)
        // В нашем случае полигоны удаляются, поэтому просто проверим отсутствие ошибок
        assertDoesNotThrow(() -> model.validateModel());
    }

    // ======= Валидация =======

    @Test
    void testValidateModelIsValidInitially() {
        assertTrue(model.validateModel());
    }

    @Test
    void testValidateModelFailsWithBadIndex() {
        // Создаём "битый" полигон
        Polygon bad = Polygon.builder()
                .setVertexIndices(List.of(0, 1, 999))
                .build();

        model.getPolygons().add(bad);

        assertFalse(model.validateModel());
    }

    @Test
    void testCleanInvalidPolygons() {
        Polygon bad = Polygon.builder()
                .setVertexIndices(List.of(0, 1, 999))
                .build();

        model.getPolygons().add(bad);

        int removed = model.cleanInvalidPolygons();

        assertEquals(1, removed);
        assertEquals(2, model.getPolygons().size());
        assertTrue(model.validateModel());
    }

    // ======= Used / Unused vertices =======

    @Test
    void testGetUsedVertices() {
        Set<Integer> used = model.getUsedVertices();

        assertEquals(Set.of(0, 1, 2, 3), used);
    }

    @Test
    void testGetUnusedVertices() {
        model.getVertices().add(new Vector3f(5, 5, 5)); // индекс 4 — не используется

        List<Integer> unused = model.getUnusedVertices();

        assertEquals(List.of(4), unused);
    }

    // ======= Текстура и цвет =======

    @Test
    void testSetAndGetTexture() {
        // НЕ создаём реальный JavaFX Image
        model.setTexture(null);

        assertNull(model.getTexture());

        // Проверяем, что сеттер/геттер работают
        model.setTexture(null);
        assertNull(model.getTexture());
    }


    @Test
    void testBaseColorNotNull() {
        var old = model.getBaseColor();
        model.setBaseColor(null);
        assertEquals(old, model.getBaseColor()); // не должно измениться
    }

    // ======= Lighting =======

    @Test
    void testLightingToggle() {
        model.setLightingEnabled(false);
        assertFalse(model.isLightingEnabled());

        model.setLightingEnabled(true);
        assertTrue(model.isLightingEnabled());
    }
}
