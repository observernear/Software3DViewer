package com.cgvsu.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class AlertDialogController {

    @FXML private Button btnOriginal;
    @FXML private Button btnTransformed;
    @FXML private Button btnCancel;
    @FXML private Label headerLabel;
    @FXML private Label contentLabel;

    private String result = null;

    // Показ/скрытие кнопок
    public void editButtons(boolean visible) {
        btnOriginal.setVisible(visible);
        btnTransformed.setVisible(visible);
        btnCancel.setVisible(visible);
    }

    public void setHeaderText(String text) {
        headerLabel.setText(text);
    }

    public void setContentText(String text) {
        contentLabel.setText(text);
    }

    public String getResult() {
        return result;
    }

    @FXML private void onOriginalClick() {
        result = "Исходная";
        closeWindow();
    }

    @FXML private void onTransformedClick() {
        result = "Преобразованная";
        closeWindow();
    }

    @FXML private void onCancelClick() {
        result = null;
        closeWindow();
    }

    // Закрытие окна
    private void closeWindow() {
        Stage stage = (Stage) headerLabel.getScene().getWindow();
        stage.close();
    }
}
