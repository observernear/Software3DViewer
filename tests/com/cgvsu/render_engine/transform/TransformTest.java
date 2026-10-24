package com.cgvsu.render_engine.transform;

import com.cgvsu.utils.math.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransformTest {

    private static final float EPS = 1e-5f; // допустимая погрешность

    @Test
    void testRotate90DegreesAroundZ() {
        Transform t = new Transform();
        t.rotateZ = 90; // вращаем на 90 градусов вокруг Z

        Vector3f v = new Vector3f(1, 0, 0);
        Vector3f result = t.rotate(v);

        // Ожидаем (0, 1, 0)
        assertEquals(0f, result.getX(), EPS);
        assertEquals(1f, result.getY(), EPS);
        assertEquals(0f, result.getZ(), EPS);
    }

    @Test
    void testRotate90DegreesAroundX() {
        Transform t = new Transform();
        t.rotateX = 90;

        Vector3f v = new Vector3f(0, 1, 0);
        Vector3f result = t.rotate(v);

        // (0,1,0) -> (0,0,1) при вращении вокруг X на +90
        assertEquals(0f, result.getX(), EPS);
        assertEquals(0f, result.getY(), EPS);
        assertEquals(1f, result.getZ(), EPS);
    }

    @Test
    void testRotate90DegreesAroundY() {
        Transform t = new Transform();
        t.rotateY = 90;

        Vector3f v = new Vector3f(1, 0, 0);
        Vector3f result = t.rotate(v);

        // (1,0,0) -> (0,0,-1) при вращении вокруг Y на +90
        assertEquals(0f, result.getX(), EPS);
        assertEquals(0f, result.getY(), EPS);
        assertEquals(-1f, result.getZ(), EPS);
    }

    @Test
    void testZeroRotationReturnsSameVector() {
        Transform t = new Transform(); // все углы = 0

        Vector3f v = new Vector3f(3.2f, -1.5f, 7.0f);
        Vector3f result = t.rotate(v);

        assertEquals(v.getX(), result.getX(), EPS);
        assertEquals(v.getY(), result.getY(), EPS);
        assertEquals(v.getZ(), result.getZ(), EPS);
    }
}
