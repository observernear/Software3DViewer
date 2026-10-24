package com.cgvsu.render_engine;

import com.cgvsu.utils.math.Matrix4f;
import com.cgvsu.utils.math.Vector3f;

/**
 * Камера для 3D сцены с поддержкой:
 * - свободного движения (free-look)
 * - орбитального вращения вокруг цели
 * - зума
 * - генерации view и projection матриц
 */
public class Camera {

    private final String name;       // имя камеры
    private Vector3f position;       // позиция камеры
    private Vector3f target;         // куда смотрит камера
    private Vector3f up;             // направление вверх

    private float fov;               // поле зрения (радианы)
    private float aspectRatio;       // соотношение сторон
    private float nearPlane;         // ближняя плоскость отсечения
    private float farPlane;          // дальняя плоскость отсечения

    public Camera(String name, Vector3f position, Vector3f target, float fov, float aspectRatio, float nearPlane, float farPlane) {
        this.name = name;
        this.position = position;
        this.target = target;
        this.up = new Vector3f(0, 1, 0);
        this.fov = fov;
        this.aspectRatio = aspectRatio;
        this.nearPlane = nearPlane;
        this.farPlane = farPlane;
    }

    public String getName() { return name; }

    // ------------------------
    // Основные направления камеры
    // ------------------------
    public Vector3f getForward() { return target.subtract(position).normalize(); }
    public Vector3f getRight() { return getForward().cross(up).normalize(); }
    public Vector3f getUp() { return up; }

    // ------------------------
    // Перемещения камеры
    // ------------------------
    public void moveForward(float distance) {
        Vector3f dir = getForward().multiply(distance);
        position = position.add(dir);
        target = target.add(dir);
    }

    public void moveBackward(float distance) { moveForward(-distance); }
    public void moveRight(float distance) {
        Vector3f dir = getRight().multiply(distance);
        position = position.add(dir);
        target = target.add(dir);
    }
    public void moveLeft(float distance) { moveRight(-distance); }

    public void moveUp(float distance) {
        Vector3f dir = up.multiply(distance);
        position = position.add(dir);
        target = target.add(dir);
    }

    public void moveDown(float distance) { moveUp(-distance); }

    // ------------------------
    // Вращения (Free-look)
    // ------------------------
    public void rotateHorizontal(float angleRad) { rotateAroundAxis(up, angleRad); }
    public void rotateVertical(float angleRad) { rotateAroundAxis(getRight(), angleRad); }

    private void rotateAroundAxis(Vector3f axis, float angleRad) {
        Vector3f dir = target.subtract(position);
        Matrix4f rot = createRotationAroundAxis(axis, angleRad);
        Vector3f rotated = rot.multiply(dir);
        target = position.add(rotated);
    }

    // ------------------------
    // Орбитальное вращение вокруг target
    // ------------------------
    public void orbitHorizontal(float angleRad) {
        Vector3f toCam = position.subtract(target);
        Matrix4f rot = createRotationAroundAxis(up, angleRad);
        position = target.add(rot.multiply(toCam));
    }

    public void orbitVertical(float angleRad) {
        Vector3f toCam = position.subtract(target);
        Matrix4f rot = createRotationAroundAxis(getRight(), angleRad);
        Vector3f rotated = rot.multiply(toCam);

        // Ограничение вертикального угла
        float dot = rotated.normalize().dot(up);
        if (dot < 0.95f && dot > -0.95f) {
            position = target.add(rotated);
        }
    }

    // ------------------------
    // Зум к/от цели
    // ------------------------
    public void zoom(float distance) {
        Vector3f dir = getForward();
        float minDist = 0.1f;
        float currDist = target.subtract(position).length();
        float newDist = Math.max(currDist - distance, minDist);
        position = target.subtract(dir.multiply(newDist));
    }

    // ------------------------
    // Матрицы для рендера
    // ------------------------
    public Matrix4f getViewMatrix() { return GraphicConveyor.lookAt(position, target, up); }
    public Matrix4f getProjectionMatrix() { return GraphicConveyor.perspective(fov, aspectRatio, nearPlane, farPlane); }

    // ------------------------
    // Сеттеры и геттеры
    // ------------------------
    public Vector3f getPosition() { return position; }
    public Vector3f getTarget() { return target; }
    public void setPosition(Vector3f position) { this.position = position; }
    public void setTarget(Vector3f target) { this.target = target; }
    public void setAspectRatio(float aspect) { this.aspectRatio = aspect; }

    public float getFov() { return fov; }
    public void setFov(float fov) { this.fov = fov; }
    public float getAspectRatio() { return aspectRatio; }
    public float getNearPlane() { return nearPlane; }
    public void setNearPlane(float nearPlane) { this.nearPlane = nearPlane; }
    public float getFarPlane() { return farPlane; }
    public void setFarPlane(float farPlane) { this.farPlane = farPlane; }

    // ------------------------
    // Создание матрицы вращения вокруг оси
    // ------------------------
    private Matrix4f createRotationAroundAxis(Vector3f axis, float angle) {
        float x = axis.getX(), y = axis.getY(), z = axis.getZ();
        float cos = (float)Math.cos(angle);
        float sin = (float)Math.sin(angle);
        float oneMinusCos = 1 - cos;

        float[][] data = {
                {cos + x*x*oneMinusCos, x*y*oneMinusCos - z*sin, x*z*oneMinusCos + y*sin, 0},
                {y*x*oneMinusCos + z*sin, cos + y*y*oneMinusCos, y*z*oneMinusCos - x*sin, 0},
                {z*x*oneMinusCos - y*sin, z*y*oneMinusCos + x*sin, cos + z*z*oneMinusCos, 0},
                {0,0,0,1}
        };
        return new Matrix4f(data);
    }

    @Override
    public String toString() { return name; }
}
