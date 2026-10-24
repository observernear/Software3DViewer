package com.cgvsu.controller;

import com.cgvsu.manager.SceneManager;
import com.cgvsu.render_engine.Camera;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;

public class CameraController {

    @FXML private ComboBox<Camera> cameraComboBox;
    @FXML private Button addCameraButton;
    @FXML private Button removeCameraButton;

    private final SceneManager sceneManager;

    public CameraController(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    @FXML
    private void initialize() {
        ObservableList<Camera> cameras = sceneManager.getCameras();
        cameraComboBox.setItems(cameras);

        // Слежение за активной камерой
        sceneManager.activeCameraProperty().addListener((obs, oldCam, newCam) ->
                cameraComboBox.getSelectionModel().select(newCam)
        );

        cameraComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldCam, newCam) -> {
            if (newCam != null) sceneManager.setActiveCamera(newCam);
        });

        addCameraButton.setOnAction(e -> addCamera());
        removeCameraButton.setOnAction(e -> removeCamera());
    }

    // Добавление новой камеры
    private void addCamera() {
        String name = "Camera " + (sceneManager.getCameras().size() + 1);
        Camera active = sceneManager.getActiveCamera();
        Camera newCam = new Camera(
                name,
                active.getPosition(),
                active.getTarget(),
                1.0f,
                1f,
                0.01f,
                100f
        );
        sceneManager.addCamera(newCam);
        sceneManager.setActiveCamera(newCam);
    }

    // Удаление выбранной камеры
    private void removeCamera() {
        Camera selected = cameraComboBox.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (sceneManager.getCameras().size() == 1) {
                showAlert("Невозможно удалить", "Должна быть хотя бы одна камера.");
                return;
            }
            sceneManager.removeCamera(selected);
        }
    }

    // Простое уведомление
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
