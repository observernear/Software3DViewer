package com.cgvsu.manager;

import com.cgvsu.model.Model;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.render_engine.rendering.RenderSettings;
import com.cgvsu.render_engine.rendering.RendererImpl;
import com.cgvsu.utils.math.Vector3f;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.canvas.GraphicsContext;

public class SceneManager {

    // --- Камеры ---
    private final ObservableList<Camera> cameras = FXCollections.observableArrayList();
    private final ObjectProperty<Camera> activeCamera = new SimpleObjectProperty<>();
    private int cameraCounter = 1;

    // --- Рендер и модели ---
    private final RendererImpl renderer;
    private final RenderSettings renderSettings = new RenderSettings();
    private final ObservableList<Model> models = FXCollections.observableArrayList();
    private final ObjectProperty<Model> activeModel = new SimpleObjectProperty<>();

    // ------------------------
    // Конструктор
    // ------------------------
    public SceneManager(RendererImpl renderer) {
        this.renderer = renderer;

        // Камера по умолчанию
        Camera defaultCam = new Camera(
                "Камера " + cameraCounter++,
                new Vector3f(0, 0, 100),
                new Vector3f(0, 0, 0),
                1.0f, 1f, 0.01f, 100f
        );
        addCamera(defaultCam);
        setActiveCamera(defaultCam);
    }

    // ------------------------
    // Рендер
    // ------------------------
    public void render(GraphicsContext gc, double width, double height) {
        Camera camera = getActiveCamera();
        Model model = getActiveModel();
        if (camera != null && model != null) {
            renderer.render(gc, camera, model, (int) width, (int) height, model.getTransform(), renderSettings);
        }
    }

    public RenderSettings getRenderSettings() {
        return renderSettings;
    }

    // ------------------------
    // Модели
    // ------------------------
    public ObservableList<Model> getModels() {
        return models;
    }

    public void addModel(Model model) {
        models.add(model);
        if (getActiveModel() == null) setActiveModel(model);
    }

    public void removeModel(Model model) {
        models.remove(model);
        if (model == getActiveModel()) {
            setActiveModel(models.isEmpty() ? null : models.get(0));
        }
    }

    public Model getActiveModel() {
        return activeModel.get();
    }

    public void setActiveModel(Model model) {
        activeModel.set(model);
    }

    public ObjectProperty<Model> activeModelProperty() {
        return activeModel;
    }

    public Model getTransformedModel() {
        Model model = getActiveModel();
        return model != null ? renderer.applyTransform(model, model.getTransform()) : null;
    }

    // ------------------------
    // Камеры
    // ------------------------
    public ObservableList<Camera> getCameras() {
        return cameras;
    }

    public Camera getActiveCamera() {
        return activeCamera.get();
    }

    public void setActiveCamera(Camera camera) {
        if (cameras.contains(camera)) activeCamera.set(camera);
    }

    public ObjectProperty<Camera> activeCameraProperty() {
        return activeCamera;
    }

    public void addCamera(Camera camera) {
        if (!cameras.contains(camera)) {
            if (camera.getName() == null || camera.getName().isEmpty()) {
                camera = new Camera(
                        "Камера " + cameraCounter++,
                        camera.getPosition(),
                        camera.getTarget(),
                        camera.getFov(),
                        camera.getAspectRatio(),
                        camera.getNearPlane(),
                        camera.getFarPlane()
                );
            }
            cameras.add(camera);
            if (getActiveCamera() == null) setActiveCamera(camera);
        }
    }

    public void removeCamera(Camera camera) {
        cameras.remove(camera);
        if (camera == getActiveCamera()) {
            setActiveCamera(cameras.isEmpty() ? null : cameras.get(0));
        }
    }
}
