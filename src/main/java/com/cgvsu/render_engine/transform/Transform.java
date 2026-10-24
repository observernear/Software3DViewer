package com.cgvsu.render_engine.transform;

import com.cgvsu.utils.math.Vector3f;

/**
 * Представляет трансформацию модели:
 * масштабирование, вращение и перемещение.
 */
public class Transform {

    // --- Масштабирование ---
    public float scaleX = 1, scaleY = 1, scaleZ = 1;

    // --- Вращение (градусы) ---
    public float rotateX = 0, rotateY = 0, rotateZ = 0;

    // --- Перемещение ---
    public float translateX = 0, translateY = 0, translateZ = 0;

    /**
     * Применяет вращение к вектору.
     * @param v исходный вектор
     * @return новый вектор после применения вращения
     */
    public Vector3f rotate(Vector3f v) {
        // Перевод градусов в радианы
        double rx = Math.toRadians(rotateX);
        double ry = Math.toRadians(rotateY);
        double rz = Math.toRadians(rotateZ);

        double x = v.getX();
        double y = v.getY();
        double z = v.getZ();

        // --- Вращение вокруг X ---
        double cosX = Math.cos(rx);
        double sinX = Math.sin(rx);
        double y1 = y * cosX - z * sinX;
        double z1 = y * sinX + z * cosX;
        y = y1;
        z = z1;

        // --- Вращение вокруг Y ---
        double cosY = Math.cos(ry);
        double sinY = Math.sin(ry);
        double x1 = x * cosY + z * sinY;
        double z2 = -x * sinY + z * cosY;
        x = x1;
        z = z2;

        // --- Вращение вокруг Z ---
        double cosZ = Math.cos(rz);
        double sinZ = Math.sin(rz);
        double x2 = x * cosZ - y * sinZ;
        double y2 = x * sinZ + y * cosZ;
        x = x2;
        y = y2;

        return new Vector3f((float) x, (float) y, (float) z);
    }
}
