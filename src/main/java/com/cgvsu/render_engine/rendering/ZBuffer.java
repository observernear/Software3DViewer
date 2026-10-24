package com.cgvsu.render_engine.rendering;

public class ZBuffer {
    private final float[][] buffer;

    public ZBuffer(int width, int height) {
        buffer = new float[width][height];
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                buffer[x][y] = Float.POSITIVE_INFINITY;
    }

    public boolean testAndSet(int x, int y, float z) {
        if (z < buffer[x][y]) {
            buffer[x][y] = z;
            return true;
        }
        return false;
    }

    public int getWidth() {
        return buffer.length;
    }

    public int getHeight() {
        return buffer[0].length;
    }
}
