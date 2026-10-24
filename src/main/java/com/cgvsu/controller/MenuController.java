package com.cgvsu.controller;

import com.cgvsu.manager.SceneManager;
import com.cgvsu.manager.interfaces.FileManagerImpl;
import com.cgvsu.model.Model;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.util.Objects;
import java.util.Optional;

public class MenuController {

    @FXML private MenuBar menuBar;

    private final FileManagerImpl modelManager;
    private final SceneManager sceneManager;
    private final MainController mainController;
    private TransformController transformController;

    public MenuController(FileManagerImpl modelManager, SceneManager sceneManager, MainController mainController) {
        this.modelManager = modelManager;
        this.sceneManager = sceneManager;
        this.mainController = mainController;
    }

    public void setTransformController(TransformController transformController) {
        this.transformController = transformController;
    }

    // ------------------ МЕНЮ: МОДЕЛЬ ------------------

    @FXML
    private void onOpenModelMenuItemClick() {
        modelManager.openModelFile(
                menuBar.getScene().getWindow(),
                this::onModelLoaded,
                this::onModelLoadError
        );
    }

    @FXML
    private void onSaveModelMenuItemClick() {
        Model currentModel = sceneManager.getActiveModel();
        if (currentModel == null) {
            showCustomAlert("Нет модели", "Сначала откройте модель.");
            return;
        }

        Optional<String> choice = showSaveModelDialog();
        if (choice.isEmpty()) return;

        Model toSave = choice.get().equals("Преобразованная")
                ? sceneManager.getTransformedModel()
                : currentModel;

        modelManager.saveModelFile(
                menuBar.getScene().getWindow(),
                toSave,
                msg -> showCustomAlert("Успех", msg),
                err -> showCustomAlert("Ошибка", err)
        );
    }

    // Добавление модели в сцену после загрузки
    private void onModelLoaded(Model model) {
        sceneManager.addModel(model);
        sceneManager.setActiveModel(model);
        mainController.requestRender();
    }

    private void onModelLoadError(String errorMessage) {
        showCustomAlert("Ошибка", errorMessage);
    }

    // ------------------ МЕНЮ: ТЕКСТУРЫ ------------------

    @FXML
    private void onOpenTextureMenuItemClick() {
        Model activeModel = sceneManager.getActiveModel();
        if (activeModel == null) {
            showCustomAlert("Нет модели", "Сначала откройте модель.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        var file = fileChooser.showOpenDialog(menuBar.getScene().getWindow());
        if (file != null) {
            Image img = new Image(file.toURI().toString());
            activeModel.setTexture(img);
            mainController.requestRender();
        }
    }

    // ------------------ УТИЛИТЫ ------------------

    /** Показ модального окна с сообщением */
    private void showCustomAlert(String title, String message) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cgvsu/fxml/AlertDialog.fxml"));
            Parent root = loader.load();
            AlertDialogController controller = loader.getController();

            controller.setHeaderText(title);
            controller.setContentText(message);
            controller.editButtons(false);

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.initOwner(menuBar.getScene().getWindow());
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

    /** Диалог выбора, какую модель сохранить */
    private Optional<String> showSaveModelDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cgvsu/fxml/AlertDialog.fxml"));
            Parent root = loader.load();
            AlertDialogController controller = loader.getController();

            controller.setHeaderText("Сохранение модели");
            controller.setContentText("Хотите сохранить исходную модель или преобразованную?");
            controller.editButtons(true);

            Stage stage = new Stage();
            stage.setTitle("Сохранение модели");
            stage.initOwner(menuBar.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setScene(new Scene(root));
            stage.getScene().getStylesheets().add(
                    Objects.requireNonNull(getClass().getResource("/com/cgvsu/css/style.css")).toExternalForm()
            );
            stage.showAndWait();

            return Optional.ofNullable(controller.getResult());

        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
