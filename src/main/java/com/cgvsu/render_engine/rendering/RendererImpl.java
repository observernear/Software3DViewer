package com.cgvsu.render_engine.rendering;

import com.cgvsu.model.Model;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.render_engine.transform.Transform;
import javafx.scene.canvas.GraphicsContext;

public interface RendererImpl {

    void render(
            GraphicsContext gc,
            Camera camera,
            Model model,
            int width,
            int height,
            Transform transform,
            RenderSettings settings
    );

    Model applyTransform(Model model, Transform transform);
}
