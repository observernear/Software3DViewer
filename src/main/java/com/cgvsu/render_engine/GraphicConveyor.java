package com.cgvsu.render_engine;

import com.cgvsu.utils.math.Point2f;
import com.cgvsu.utils.math.Vector3f;
import com.cgvsu.utils.math.Matrix4f;
import com.cgvsu.utils.math.Vector4f;

public class GraphicConveyor {
    public static Matrix4f scale(float scaleX, float scaleY, float scaleZ) {
        float[][] data = new float[][]{
                {scaleX, 0, 0, 0},
                {0, scaleY, 0, 0},
                {0, 0, scaleZ, 0},
                {0, 0, 0, 1}
        };
        return new Matrix4f(data);
    }

    public static Matrix4f rotateX(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        float[][] data = new float[][]{
                {1, 0, 0, 0},
                {0, cos, -sin, 0},
                {0, sin, cos, 0},
                {0, 0, 0, 1}
        };
        return new Matrix4f(data);
    }

    public static Matrix4f rotateY(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        float[][] data = new float[][]{
                {cos, 0, sin, 0},
                {0, 1, 0, 0},
                {-sin, 0, cos, 0},
                {0, 0, 0, 1}
        };
        return new Matrix4f(data);
    }

    public static Matrix4f rotateZ(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        float[][] data = new float[][]{
                {cos, -sin, 0, 0},
                {sin, cos, 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}
        };
        return new Matrix4f(data);
    }

    public static Matrix4f translate(float tx, float ty, float tz) {
        float[][] data = new float[][]{
                {1, 0, 0, tx},
                {0, 1, 0, ty},
                {0, 0, 1, tz},
                {0, 0, 0, 1}
        };
        return new Matrix4f(data);
    }

    public static Matrix4f createModelMatrix(
            float scaleX, float scaleY, float scaleZ,
            float rotateX, float rotateY, float rotateZ,
            float translateX, float translateY, float translateZ) {

        Matrix4f scaleMatrix = scale(scaleX, scaleY, scaleZ);
        Matrix4f rotateXMatrix = rotateX(rotateX);
        Matrix4f rotateYMatrix = rotateY(rotateY);
        Matrix4f rotateZMatrix = rotateZ(rotateZ);
        Matrix4f translateMatrix = translate(translateX, translateY, translateZ);

        Matrix4f rotationMatrix = rotateZMatrix.multiply(rotateYMatrix).multiply(rotateXMatrix);

        return translateMatrix.multiply(rotationMatrix).multiply(scaleMatrix);
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target) {
        return lookAt(eye, target, new Vector3f(0f, 1f, 0f));
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target, Vector3f up) {
        Vector3f direction = target.subtract(eye);

        if (direction.length() < 0.0001f) {
            direction = new Vector3f(0, 0, -1);
        }

        Vector3f z = eye.subtract(target).normalize();
        Vector3f x = up.cross(z).normalize();
        Vector3f y = z.cross(x).normalize();

        float[][] data = new float[4][4];

        data[0][0] = x.getX(); data[0][1] = x.getY(); data[0][2] = x.getZ(); data[0][3] = -x.dot(eye);
        data[1][0] = y.getX(); data[1][1] = y.getY(); data[1][2] = y.getZ(); data[1][3] = -y.dot(eye);
        data[2][0] = z.getX(); data[2][1] = z.getY(); data[2][2] = z.getZ(); data[2][3] = -z.dot(eye);
        data[3][0] = 0;        data[3][1] = 0;        data[3][2] = 0;        data[3][3] = 1;

        return new Matrix4f(data);
    }

    public static Matrix4f perspective(float fov, float aspectRatio, float nearPlane, float farPlane) {
        float f = 1.0f / (float)Math.tan(fov / 2.0f);
        float[][] data = new float[4][4];

        data[0][0] = f / aspectRatio;
        data[0][1] = 0;
        data[0][2] = 0;
        data[0][3] = 0;
        data[1][0] = 0;
        data[1][1] = f;
        data[1][2] = 0;
        data[1][3] = 0;
        data[2][0] = 0;
        data[2][1] = 0;
        data[2][2] = (farPlane + nearPlane) / (nearPlane - farPlane);
        data[2][3] = (2 * farPlane * nearPlane) / (nearPlane - farPlane);
        data[3][0] = 0;
        data[3][1] = 0;
        data[3][2] = -1;
        data[3][3] = 0;

        return new Matrix4f(data);
    }

    public static Vector3f multiplyMatrix4ByVector3(Matrix4f m, Vector3f v) {
        Vector4f v4 = m.multiply(new Vector4f(v.getX(), v.getY(), v.getZ(), 1.0f));

        float w = v4.getW();
        if (Math.abs(w) < 1e-6f) {
            return new Vector3f(v4.getX(), v4.getY(), v4.getZ());
        }

        return new Vector3f(
                v4.getX() / w,
                v4.getY() / w,
                v4.getZ() / w
        );
    }

    public static Vector4f multiplyMatrix4ByVector4(Matrix4f m, Vector4f v) {

        float x =
                m.get(0, 0) * v.getX() +
                        m.get(0, 1) * v.getY() +
                        m.get(0, 2) * v.getZ() +
                        m.get(0, 3) * v.getW();

        float y =
                m.get(1, 0) * v.getX() +
                        m.get(1, 1) * v.getY() +
                        m.get(1, 2) * v.getZ() +
                        m.get(1, 3) * v.getW();

        float z =
                m.get(2, 0) * v.getX() +
                        m.get(2, 1) * v.getY() +
                        m.get(2, 2) * v.getZ() +
                        m.get(2, 3) * v.getW();

        float w =
                m.get(3, 0) * v.getX() +
                        m.get(3, 1) * v.getY() +
                        m.get(3, 2) * v.getZ() +
                        m.get(3, 3) * v.getW();

        return new Vector4f(x, y, z, w);
    }



    public static Point2f vertexToPoint(Vector3f v, int width, int height) {
        float x_ndc = v.getX();
        float y_ndc = v.getY();

        float x_screen = (x_ndc + 1.0f) * 0.5f * width;
        float y_screen = (1.0f - (y_ndc + 1.0f) * 0.5f) * height;

        return new Point2f(x_screen, y_screen);
    }

}