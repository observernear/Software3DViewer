package com.cgvsu.render_engine.rendering;

import javafx.scene.paint.Color;

/**
 * Настройки рендеринга модели.
 * Позволяет включать/отключать каркас, текстуру, освещение и задавать базовый цвет.
 */
public class RenderSettings {

    /** Рисовать модель в виде каркаса (wireframe) */
    private boolean wireframe = false;

    /** Использовать текстуру модели */
    private boolean useTexture = false;

    /** Использовать освещение при рендеринге */
    private boolean useLighting = false;

    /** Базовый цвет для модели (если нет текстуры или wireframe) */
    private Color baseColor = Color.GRAY;

    /** Флаг, указывающий, что текстура уже загружена */
    private boolean textureLoaded = false;

    // -----------------------
    // Texture loaded
    // -----------------------
    public boolean isTextureLoaded() {
        return textureLoaded;
    }

    public void setTextureLoaded(boolean loaded) {
        this.textureLoaded = loaded;
    }

    // -----------------------
    // Wireframe
    // -----------------------
    public boolean isWireframe() {
        return wireframe;
    }

    public void setWireframe(boolean wireframe) {
        this.wireframe = wireframe;
    }

    // -----------------------
    // Use texture
    // -----------------------
    public boolean isUseTexture() {
        return useTexture;
    }

    public void setUseTexture(boolean useTexture) {
        this.useTexture = useTexture;
    }

    // -----------------------
    // Use lighting
    // -----------------------
    public boolean isUseLighting() {
        return useLighting;
    }

    public void setUseLighting(boolean useLighting) {
        this.useLighting = useLighting;
    }

    // -----------------------
    // Base color
    // -----------------------
    public Color getBaseColor() {
        return baseColor;
    }

    public void setBaseColor(Color baseColor) {
        this.baseColor = baseColor;
    }
}
