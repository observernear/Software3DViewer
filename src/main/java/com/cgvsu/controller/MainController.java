package com.cgvsu.controller;

import com.cgvsu.manager.*;
import com.cgvsu.manager.implementations.DefaultInputManager;
import com.cgvsu.manager.implementations.ObjFileManager;
import com.cgvsu.manager.interfaces.InputManagerImpl;
import com.cgvsu.manager.interfaces.FileManagerImpl;
import com.cgvsu.render_engine.rendering.RendererImpl;
import com.cgvsu.render_engine.rendering.Renderer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class MainController {

    @FXML private BorderPane borderPane;

    // Основные менеджеры приложения
    private final SceneManager sceneManager;
    private final AnimationManager animationManager;
    private final UIManager uiManager;
    private final FileManagerImpl modelManager;
    private final InputManagerImpl inputManager;

    // Подконтроллеры
    private ViewportController viewportController;
    private ToolController toolController;
    private MenuController menuController;
    private RendererImpl renderer;

    // Конструктор и инициализация менеджеров
    public MainController() {
        this.renderer = new Renderer();  // Отвечает за рендеринг сцены
        this.sceneManager = new SceneManager(renderer);
        this.animationManager = new AnimationManager(this::renderFrame);
        this.uiManager = new UIManager();
        this.modelManager = new ObjFileManager();
        this.inputManager = new DefaultInputManager(sceneManager);
    }

    // Универсальное окно для сообщений
    public void showAlert(String title, String message) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cgvsu/fxml/AlertDialog.fxml"));
            Parent root = loader.load();
            AlertDialogController controller = loader.getController();
            controller.setHeaderText(title);
            controller.setContentText(message);
            controller.editButtons(false);

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.initOwner(borderPane.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setScene(new Scene(root));
            stage.getScene().getStylesheets().add(
                    Objects.requireNonNull(getClass().getResource("/com/cgvsu/css/style.css")).toExternalForm()
            );
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Инициализация контроллера после загрузки FXML
    @FXML
    private void initialize() {
        try {
            setupChildControllers();  // Подключение подконтроллеров
            animationManager.start();  // Запуск анимаций
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize main controller", e);
        }
    }

    // Загрузка всех подконтроллеров через фабрику
    private void setupChildControllers() throws IOException {
        ControllerFactory factory = new ControllerFactory(
                sceneManager, animationManager, uiManager, modelManager, inputManager, this
        );

        // Viewport (рендер сцены)
        FXMLLoader viewportLoader = new FXMLLoader(getClass().getResource("/com/cgvsu/fxml/ViewportPane.fxml"));
        viewportLoader.setControllerFactory(factory);
        Parent viewportNode = viewportLoader.load();
        borderPane.setCenter(viewportNode);
        this.viewportController = viewportLoader.getController();

        // Tool Panel (панель инструментов)
        FXMLLoader toolLoader = new FXMLLoader(getClass().getResource("/com/cgvsu/fxml/ToolPanel.fxml"));
        toolLoader.setControllerFactory(factory);
        Parent toolNode = toolLoader.load();
        addStylesToNode(toolNode);
        borderPane.setRight(toolNode);
        this.toolController = toolLoader.getController();

        // Menu (верхнее меню)
        FXMLLoader menuLoader = new FXMLLoader(getClass().getResource("/com/cgvsu/fxml/MenuBar.fxml"));
        menuLoader.setControllerFactory(factory);
        Parent menuNode = menuLoader.load();
        addStylesToNode(menuNode);
        borderPane.setTop(menuNode);
        this.menuController = menuLoader.getController();
    }

    // Копирование стилей сцены на узел
    private void addStylesToNode(Parent node) {
        Scene scene = borderPane.getScene();
        if (scene != null) {
            for (String stylesheet : scene.getStylesheets()) {
                if (!node.getStylesheets().contains(stylesheet)) {
                    node.getStylesheets().add(stylesheet);
                }
            }
        }
    }

    // Запрос рендера из других контроллеров
    public void requestRender() {
        renderFrame();
    }

    // Рендер текущего кадра
    private void renderFrame() {
        if (viewportController != null) {
            viewportController.renderFrame();
        }
    }

    // Останов анимации при выходе
    public void cleanup() {
        if (animationManager != null) animationManager.stop();
    }
}
