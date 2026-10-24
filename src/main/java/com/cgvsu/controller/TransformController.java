package com.cgvsu.controller;

import com.cgvsu.manager.SceneManager;
import com.cgvsu.manager.UIManager;
import com.cgvsu.model.Model;
import com.cgvsu.render_engine.transform.Transform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.layout.VBox;

public class TransformController {

    @FXML private VBox transformPanel;
    @FXML private Spinner<Double> translateXField, translateYField, translateZField;
    @FXML private Spinner<Double> rotateXField, rotateYField, rotateZField;
    @FXML private Spinner<Double> scaleXField, scaleYField, scaleZField;
    @FXML private Button resetButton;

    private final SceneManager sceneManager;
    private final UIManager uiManager;
    private final ChangeListener<Transform> transformListener;

    public TransformController(SceneManager sceneManager, UIManager uiManager) {
        this.sceneManager = sceneManager;
        this.uiManager = uiManager;

        // слушатель обновления спиннеров при изменении модели
        transformListener = (obs, oldTransform, newTransform) -> {
            if (!uiManager.isUpdatingFromModel()) updateSpinnersFromTransform(newTransform);
        };
    }

    @FXML
    private void initialize() {
        setupUI();        // инициализация спиннеров
        setupListeners(); // слушатели спиннеров и активной модели
        hidePanel();      // по умолчанию скрыта
    }

    private void setupUI() {
        uiManager.setupTransformSpinners(
                translateXField, translateYField, translateZField,
                rotateXField, rotateYField, rotateZField,
                scaleXField, scaleYField, scaleZField
        );
    }

    private void setupListeners() {
        // слушатели изменения значений спиннеров
        Spinner<Double>[] spinners = new Spinner[]{
                translateXField, translateYField, translateZField,
                rotateXField, rotateYField, rotateZField,
                scaleXField, scaleYField, scaleZField
        };
        for (Spinner<Double> spinner : spinners) setupSpinnerListener(spinner);

        // слушаем смену активной модели
        sceneManager.activeModelProperty().addListener((obs, oldModel, newModel) -> {
            if (oldModel != null) oldModel.transformProperty().removeListener(transformListener);
            if (newModel != null) {
                newModel.transformProperty().addListener(transformListener);
                updateSpinnersFromTransform(newModel.getTransform());
                setSpinnersEditable(true);
            } else setSpinnersEditable(false);
        });

        // начальная настройка спиннеров для текущей модели
        Model activeModel = sceneManager.getActiveModel();
        if (activeModel != null) {
            activeModel.transformProperty().addListener(transformListener);
            updateSpinnersFromTransform(activeModel.getTransform());
            setSpinnersEditable(true);
        } else setSpinnersEditable(false);
    }

    private void setupSpinnerListener(Spinner<Double> spinner) {
        spinner.valueProperty().addListener((obs, oldVal, newVal) -> handleTransformChange());
    }

    @FXML
    private void handleResetTransform() {
        Model activeModel = sceneManager.getActiveModel();
        if (activeModel == null) return;

        activeModel.setTransform(new Transform());
        updateSpinnersFromTransform(activeModel.getTransform());
    }

    private void handleTransformChange() {
        if (uiManager.isUpdatingFromModel()) return;

        Model activeModel = sceneManager.getActiveModel();
        if (activeModel == null) return;

        Transform transform = uiManager.createTransformFromSpinners(
                translateXField, translateYField, translateZField,
                rotateXField, rotateYField, rotateZField,
                scaleXField, scaleYField, scaleZField
        );
        activeModel.setTransform(transform);
    }

    private void updateSpinnersFromTransform(Transform transform) {
        uiManager.updateSpinnersFromTransform(
                transform,
                translateXField, translateYField, translateZField,
                rotateXField, rotateYField, rotateZField,
                scaleXField, scaleYField, scaleZField
        );
    }

    private void setSpinnersEditable(boolean editable) {
        Spinner<Double>[] spinners = new Spinner[]{
                translateXField, translateYField, translateZField,
                rotateXField, rotateYField, rotateZField,
                scaleXField, scaleYField, scaleZField
        };
        for (Spinner<Double> spinner : spinners) spinner.setDisable(!editable);
        resetButton.setDisable(!editable);
    }

    // ------------------ Панель ------------------

    public void showPanel() {
        transformPanel.setVisible(true);
        transformPanel.setManaged(true);
    }

    public void hidePanel() {
        transformPanel.setVisible(false);
        transformPanel.setManaged(false);
    }

    /** Корневой узел для ToolController */
    public Node getRoot() {
        return transformPanel;
    }
}
