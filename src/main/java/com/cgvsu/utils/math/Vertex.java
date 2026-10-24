package com.cgvsu.utils.math;

public class Vertex {
    public final float x, y;
    public final float z;
    public final float invW;
    public final float u, v;
    public final Vector3f worldPos;
    public final Vector3f normal;

    public Vertex(float x, float y, float z, float invW,
                  float u, float v,
                  Vector3f worldPos,
                  Vector3f normal) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.invW = invW;
        this.u = u;
        this.v = v;
        this.worldPos = worldPos;
        this.normal = normal;
    }
}
