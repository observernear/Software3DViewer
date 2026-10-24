package com.cgvsu.manager.implementations;

import com.cgvsu.manager.SceneManager;
import com.cgvsu.manager.interfaces.InputManagerImpl;
import com.cgvsu.utils.math.Vector3f;
import com.cgvsu.render_engine.Camera;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class DefaultInputManager implements InputManagerImpl {

    private final SceneManager sceneManager;
    private boolean middleMousePressed = false;
    private double lastMouseX, lastMouseY;

    private float rotateSensitivity = 0.005f;   // вращение камеры
    private float moveMouseSensitivity = 0.05f; // сдвиг мышью (Shift)
    private float moveSpeed = 0.5f;             // WASD, Space, Shift

    private Runnable onOpenModel, onSaveModel, onShowTransformPanel, onHideTransformPanel, onRenderRequest;

    public DefaultInputManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    private Camera getCamera() {
        return sceneManager.getActiveCamera();
    }

    @Override
    public void setupKeyboardHandlers(Node targetNode, Runnable onRenderRequest) {
        this.onRenderRequest = onRenderRequest;
        targetNode.setFocusTraversable(true);
        targetNode.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
    }

    @Override
    public void setupMouseHandlers(Node targetNode) {
        targetNode.setOnMousePressed(this::handleMousePressed);
        targetNode.setOnMouseReleased(this::handleMouseReleased);
        targetNode.setOnMouseDragged(this::handleMouseDragged);
        targetNode.setOnScroll(this::handleMouseScroll);
    }

    @Override
    public void setHotkeyHandlers(Runnable onOpenModel, Runnable onSaveModel,
                                  Runnable onShowTransformPanel, Runnable onHideTransformPanel) {
        this.onOpenModel = onOpenModel;
        this.onSaveModel = onSaveModel;
        this.onShowTransformPanel = onShowTransformPanel;
        this.onHideTransformPanel = onHideTransformPanel;
    }

    private void handleKeyPressed(KeyEvent event) {
        Camera camera = getCamera();
        if (camera == null) return;

        boolean cameraMoved = false;

        // горячие клавиши с Ctrl
        if (event.isControlDown()) {
            switch (event.getCode()) {
                case O -> { if (onOpenModel != null) { onOpenModel.run(); event.consume(); return; } }
                case S -> { if (onSaveModel != null) { onSaveModel.run(); event.consume(); return; } }
                case T -> { if (onShowTransformPanel != null) { onShowTransformPanel.run(); event.consume(); return; } }
                case G -> { if (onHideTransformPanel != null) { onHideTransformPanel.run(); event.consume(); return; } }
            }
        }

        // движение камеры
        switch (event.getCode()) {
            case W -> { camera.moveForward(moveSpeed); cameraMoved = true; }
            case S -> { camera.moveBackward(moveSpeed); cameraMoved = true; }
            case A -> { camera.moveLeft(moveSpeed); cameraMoved = true; }
            case D -> { camera.moveRight(moveSpeed); cameraMoved = true; }
            case SPACE -> { camera.moveUp(moveSpeed); cameraMoved = true; }
            case SHIFT -> { camera.moveDown(moveSpeed); cameraMoved = true; }
            case R -> { resetCamera(camera); cameraMoved = true; }
        }

        if (cameraMoved && onRenderRequest != null) onRenderRequest.run();
    }

    private void resetCamera(Camera camera) {
        camera.setPosition(new Vector3f(0, 0, 50));
        camera.setTarget(new Vector3f(0, 0, 0));
    }

    private void handleMousePressed(MouseEvent event) {
        if (event.getButton() == MouseButton.MIDDLE) {
            middleMousePressed = true;
            lastMouseX = event.getSceneX();
            lastMouseY = event.getSceneY();
            event.consume();
        }
    }

    private void handleMouseReleased(MouseEvent event) {
        if (event.getButton() == MouseButton.MIDDLE) {
            middleMousePressed = false;
            event.consume();
        }
    }

    private void handleMouseDragged(MouseEvent event) {
        Camera camera = getCamera();
        if (!middleMousePressed || camera == null) return;

        double deltaX = event.getSceneX() - lastMouseX;
        double deltaY = event.getSceneY() - lastMouseY;

        // сдвиг, вращение, орбита
        if (event.isShiftDown()) {
            camera.moveRight((float) (-deltaX * moveMouseSensitivity));
            camera.moveUp((float) (deltaY * moveMouseSensitivity));
        } else if (event.isControlDown()) {
            camera.rotateHorizontal((float) (-deltaX * rotateSensitivity));
            camera.rotateVertical((float) (-deltaY * rotateSensitivity));
        } else {
            camera.orbitHorizontal((float) (-deltaX * rotateSensitivity));
            camera.orbitVertical((float) (-deltaY * rotateSensitivity));
        }

        lastMouseX = event.getSceneX();
        lastMouseY = event.getSceneY();

        if (onRenderRequest != null) onRenderRequest.run();
        event.consume();
    }

    private void handleMouseScroll(javafx.scene.input.ScrollEvent event) {
        Camera camera = getCamera();
        if (camera == null) return;

        float zoomAmount = (float) (event.getDeltaY() * 0.05f);
        camera.zoom(zoomAmount);
        if (onRenderRequest != null) onRenderRequest.run();
        event.consume();
    }

    @Override
    public void setMouseSensitivity(float sensitivity) {
        this.rotateSensitivity = sensitivity;
    }

    @Override
    public void setMoveSpeed(float speed) {
        this.moveSpeed = speed;
    }
}
