package com.cgvsu.utils.math;

import com.cgvsu.utils.math.base.AbstractMatrix;

public class Matrix4f extends AbstractMatrix<Matrix4f, Vector4f> {

    public Matrix4f(float[][] data) {
        super(data, 4);
    }

    @Override
    protected Matrix4f createNew(float[][] data) {
        return new Matrix4f(data);
    }

    public static Matrix4f identity() { // единичная матрица
        return new Matrix4f(new float[][]{
                {1, 0, 0, 0},
                {0, 1, 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}
        });
    }

    public static Matrix4f zero() { // нулевая матрица
        return new Matrix4f(new float[4][4]);
    }

    @Override
    public Vector4f multiply(Vector4f vector) { // умножение матрицы на вектор

        float x = data[0][0] * vector.getX() + data[0][1] * vector.getY() +
                data[0][2] * vector.getZ() + data[0][3] * vector.getW();
        float y = data[1][0] * vector.getX() + data[1][1] * vector.getY() +
                data[1][2] * vector.getZ() + data[1][3] * vector.getW();
        float z = data[2][0] * vector.getX() + data[2][1] * vector.getY() +
                data[2][2] * vector.getZ() + data[2][3] * vector.getW();
        float w = data[3][0] * vector.getX() + data[3][1] * vector.getY() +
                data[3][2] * vector.getZ() + data[3][3] * vector.getW();

        return new Vector4f(x, y, z, w);

    }

    @Override
    public float determinant() { // определитель
        try {
            float det = 0;
            // по первой строке = разложение
            for (int j = 0; j < 4; j++) {
                det += data[0][j] * cofactor(0, j);
            }
            return det;
        } catch (Exception e) {
            throw new ArithmeticException("Calculating determinant: " + e.getMessage());
        }
    }

    private float minor(int row, int col) { // минорчик

        float[][] minorMatrix = new float[3][3];
        int minorRow = 0;


        for (int i = 0; i < 4; i++) {
            if (i == row) continue;
            int minorCol = 0;
            for (int j = 0; j < 4; j++) {
                if (j == col) continue;
                minorMatrix[minorRow][minorCol] = data[i][j];
                minorCol++;
            }
            minorRow++;
        }

        Matrix3f minor = new Matrix3f(minorMatrix);
        return minor.determinant();

    }

    private float cofactor(int row, int col) { // вычисление алгебраического дополнения

        float minor = minor(row, col);
        return ((row + col) % 2 == 0) ? minor : -minor;

    }

    @Override
    public Matrix4f inverse() { // обратная

        float det = determinant();

        if (Math.abs(det) < 1e-12f) {
            throw new ArithmeticException("Matrix is singular, cannot invert");
        }

        float[][] result = new float[4][4];

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[j][i] = cofactor(i, j) / det;
            }
        }
        return new Matrix4f(result);

    }

    @Override
    public Vector4f solveLinearSystem(Vector4f b) { // решение сист
        return solveGauss(b);
    }

    public Vector4f solveGauss(Vector4f b) { // тут аналог 3x3

        float[][] augmented = new float[4][5]; // расширенная матрица

        for (int i = 0; i < 4; i++) {
            System.arraycopy(data[i], 0, augmented[i], 0, 4);
            switch(i) {
                case 0: augmented[i][4] = b.getX(); break;
                case 1: augmented[i][4] = b.getY(); break;
                case 2: augmented[i][4] = b.getZ(); break;
                case 3: augmented[i][4] = b.getW(); break;
            }
        }

        for (int i = 0; i < 4; i++) {
            int maxRow = i;
            for (int k = i + 1; k < 4; k++) {
                if (Math.abs(augmented[k][i]) > Math.abs(augmented[maxRow][i])) {
                    maxRow = k;
                }
            }

            float[] temp = augmented[i]; // сохраняем
            augmented[i] = augmented[maxRow]; // заменяем
            augmented[maxRow] = temp; // восстанавливаем

            if (Math.abs(augmented[i][i]) < 1e-12f) {
                throw new ArithmeticException("Matrix is singular, cannot solve system");
            }

            for (int k = i + 1; k < 4; k++) {
                float factor = augmented[k][i] / augmented[i][i];
                for (int j = i; j < 5; j++) {
                    augmented[k][j] -= factor * augmented[i][j];
                }
            }
        }

        float[] solution = new float[4];

        for (int i = 3; i >= 0; i--) {
            solution[i] = augmented[i][4];
            for (int j = i + 1; j < 4; j++) {
                solution[i] -= augmented[i][j] * solution[j];
            }
            solution[i] /= augmented[i][i];
        }

        return new Vector4f(solution[0], solution[1], solution[2], solution[3]);
    }

    public float get(int row, int col) { // для доступа к эл матрицы
        if (row < 0 || row >= 4 || col < 0 || col >= 4) {
            throw new IllegalArgumentException("Index out of bounds");
        }
        return data[row][col];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            sb.append("[");
            for (int j = 0; j < 4; j++) {
                sb.append(String.format("%.4f", data[i][j]));
                if (j < 3) sb.append(", ");
            }
            sb.append("]\n");
        }
        return sb.toString();
    }

    public Vector3f multiply(Vector3f v) {
        Vector4f v4 = new Vector4f(v.getX(), v.getY(), v.getZ(), 1.0f);
        Vector4f res = this.multiply(v4);
        return new Vector3f(res.getX(), res.getY(), res.getZ());
    }

}
