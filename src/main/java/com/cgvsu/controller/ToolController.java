package com.cgvsu.controller;

import com.cgvsu.manager.SceneManager;
import com.cgvsu.manager.UIManager;
import com.cgvsu.model.Model;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.render_engine.rendering.RenderSettings;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.io.File;

public class ToolController {

    @FXML private StackPane contentPane;
    @FXML private Button transformButton, deleteButton, modelsButton;
    @FXML private CheckBox wireframeCheckBox, textureCheckBox, lightingCheckBox;
    @FXML private ColorPicker baseColorPicker;
    @FXML private ComboBox<Camera> cameraComboBox;
    @FXML private Button addCameraButton, removeCameraButton;

    private TransformController transformController;
    private DeletionController deletionController;
    private ModelsController modelsController;

    private final SceneManager sceneManager;
    private final UIManager uiManager;
    private final MainController mainController;

    public ToolController(SceneManager sceneManager, UIManager uiManager, MainController mainController) {
        this.sceneManager = sceneManager;
        this.uiManager = uiManager;
        this.mainController = mainController;
    }

    @FXML
    private void initialize() {
        loadPanels();          // загружаем панели
        showModelsPanel();      // по умолчанию Models
        setupCameraControls();  // камеры
        setupRenderSettings();  // галочки и ColorPicker
        baseColorPicker.setValue(Color.WHITE); // дефолтный цвет
        baseColorPicker.setOnAction(e -> updateModelColor());
        lightingCheckBox.setOnAction(e -> updateModelColor());
    }

    /** Универсальная загрузка панели */
    private <T> T loadPanel(String fxmlPath, Class<T> controllerClass) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(type -> {
                if (type == controllerClass) {
                    try {
                        if (controllerClass == TransformController.class)
                            return controllerClass.cast(new TransformController(sceneManager, uiManager));
                        if (controllerClass == DeletionController.class)
                            return controllerClass.cast(new DeletionController(sceneManager, uiManager));
                        if (controllerClass == ModelsController.class)
                            return controllerClass.cast(new ModelsController(sceneManager, uiManager));
                    } catch (Exception e) { throw new RuntimeException(e); }
                }
                try { return type.getDeclaredConstructor().newInstance(); }
                catch (Exception e) { throw new RuntimeException(e); }
            });

            Node root = loader.load();
            contentPane.getChildren().add(root); // добавляем Node
            return loader.getController();

        } catch (Exception e) {
            throw new RuntimeException("Не удалось загрузить панель: " + fxmlPath, e);
        }
    }

    /** Загрузка всех панелей */
    private void loadPanels() {
        transformController = loadPanel("/com/cgvsu/fxml/TransformPanel.fxml", TransformController.class);
        deletionController = loadPanel("/com/cgvsu/fxml/DeletionPanel.fxml", DeletionController.class);
        modelsController = loadPanel("/com/cgvsu/fxml/ModelsPanel.fxml", ModelsController.class);

        transformController.hidePanel();
        deletionController.hidePanel();
        modelsController.hidePanel();
    }

    /** Настройка галочек и цвета */
    private void setupRenderSettings() {
        RenderSettings settings = sceneManager.getRenderSettings();

        wireframeCheckBox.selectedProperty().addListener((obs, oldV, newV) -> {
            settings.setWireframe(newV);
            mainController.requestRender();
        });

        textureCheckBox.selectedProperty().addListener((obs, oldV, newV) -> {
            Model activeModel = sceneManager.getActiveModel();
            if (newV) {
                if (activeModel == null || activeModel.getTexture() == null) {
                    textureCheckBox.setSelected(false);
                    mainController.showAlert(
                            activeModel == null ? "Нет модели" : "Нет текстуры",
                            activeModel == null ? "Сначала откройте модель." :
                                    "Сначала загрузите текстуру для модели."
                    );
                    return;
                }
                settings.setUseTexture(true);
            } else settings.setUseTexture(false);

            settings.setBaseColor(baseColorPicker.getValue());
            mainController.requestRender();
        });

        lightingCheckBox.selectedProperty().addListener((obs, oldV, newV) -> {
            settings.setUseLighting(newV);
            settings.setBaseColor(baseColorPicker.getValue());
            mainController.requestRender();
        });

        baseColorPicker.setValue(Color.GRAY);
        baseColorPicker.valueProperty().addListener((obs, oldV, newV) -> {
            settings.setBaseColor(newV);
            mainController.requestRender();
        });
    }

    /** Настройка панели камер */
    private void setupCameraControls() {
        ObservableList<Camera> cameras = sceneManager.getCameras();
        cameraComboBox.setItems(cameras);
        cameraComboBox.setValue(sceneManager.getActiveCamera());

        cameraComboBox.valueProperty().addListener((obs, oldCam, newCam) -> {
            if (newCam != null) {
                sceneManager.setActiveCamera(newCam);
                mainController.requestRender();
            }
        });

        addCameraButton.setOnAction(e -> {
            int camIndex = sceneManager.getCameras().size() + 1; // порядковый номер
            Camera newCam = new Camera(
                    "Камера " + camIndex,                     // Имя камеры
                    new com.cgvsu.utils.math.Vector3f(0, 0, 100),
                    new com.cgvsu.utils.math.Vector3f(0, 0, 0),
                    1.0f, 1f, 0.01f, 100f
            );

            // Добавляем камеру в сцену
            sceneManager.addCamera(newCam);

            // Добавляем камеру в ComboBox
            if (!cameraComboBox.getItems().contains(newCam)) {
                cameraComboBox.getItems().add(newCam);
            }

            // Сразу делаем её активной
            sceneManager.setActiveCamera(newCam);

            // Выбираем её в ComboBox
            cameraComboBox.setValue(newCam);

            // Перерисовка сцены
            mainController.requestRender();

            // Блокировка кнопки удаления, если осталась только одна камера
            removeCameraButton.setDisable(sceneManager.getCameras().size() <= 1);
        });

        removeCameraButton.setOnAction(e -> {
            Camera cam = cameraComboBox.getValue();
            if (cam != null && sceneManager.getCameras().size() > 1) { // проверка на >1 камеру
                sceneManager.removeCamera(cam);

                // Переключаемся на первую оставшуюся камеру
                Camera firstCam = sceneManager.getCameras().get(0);
                sceneManager.setActiveCamera(firstCam);
                cameraComboBox.setValue(firstCam);

                mainController.requestRender();
            }

            // Обновляем состояние кнопки удаления
            removeCameraButton.setDisable(sceneManager.getCameras().size() <= 1);
        });

    }

    /** Загрузка текстуры */
    @FXML
    private void loadTexture() {
        Model activeModel = sceneManager.getActiveModel();
        if (activeModel == null) {
            mainController.showAlert("Нет модели", "Сначала откройте модель.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(baseColorPicker.getScene().getWindow());
        if (file != null) {
            try {
                Image img = new Image(file.toURI().toString());
                activeModel.setTexture(img);
                mainController.showAlert("Текстура загружена",
                        "Включите галочку 'Использовать текстуру', чтобы отобразить её.");
            } catch (Exception ex) {
                mainController.showAlert("Ошибка загрузки", "Не удалось загрузить текстуру: " + ex.getMessage());
            }
        }
    }

    /** Панели переключения */
    @FXML private void showTransformPanel() {
        transformController.showPanel();
        deletionController.hidePanel();
        modelsController.hidePanel();
        updateMenuButtonStyles(true, false, false);
    }

    @FXML private void showDeletionPanel() {
        transformController.hidePanel();
        deletionController.showPanel();
        modelsController.hidePanel();
        updateMenuButtonStyles(false, true, false);
    }

    @FXML private void showModelsPanel() {
        transformController.hidePanel();
        deletionController.hidePanel();
        modelsController.showPanel();
        updateMenuButtonStyles(false, false, true);
    }

    /** Стили для кнопок меню */
    private void updateMenuButtonStyles(boolean transformActive, boolean deleteActive, boolean modelsActive) {
        transformButton.getStyleClass().removeAll("button-primary", "button-secondary");
        deleteButton.getStyleClass().removeAll("button-primary", "button-secondary");
        modelsButton.getStyleClass().removeAll("button-primary", "button-secondary");

        transformButton.getStyleClass().add(transformActive ? "button-primary" : "button-secondary");
        deleteButton.getStyleClass().add(deleteActive ? "button-primary" : "button-secondary");
        modelsButton.getStyleClass().add(modelsActive ? "button-primary" : "button-secondary");
    }

    private void updateModelColor() {
        Model model = sceneManager.getActiveModel();
        if (model == null) return;

        // Получаем цвет из ColorPicker (JavaFX)
        javafx.scene.paint.Color fxColor = baseColorPicker.getValue();

        // Применяем к модели прямо как javafx Color
        model.setBaseColor(fxColor);

        // Включение/выключение освещения
        model.setLightingEnabled(lightingCheckBox.isSelected());

        mainController.requestRender();
    }
}
