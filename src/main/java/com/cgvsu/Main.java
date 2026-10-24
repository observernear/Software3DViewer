package com.cgvsu;

import com.cgvsu.controller.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cgvsu/fxml/Main.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1200, 900);

        String cssPath = "/com/cgvsu/css/style.css";
        try {
            String cssUrl = Objects.requireNonNull(getClass().getResource(cssPath)).toExternalForm();
            scene.getStylesheets().add(cssUrl);
            System.out.println("CSS loaded from: " + cssUrl);
        } catch (NullPointerException e) {
            System.err.println("CSS file not found at: " + cssPath);
        }

        stage.setTitle("3D Model Viewer");
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.show();

        MainController controller = loader.getController();
        stage.setOnCloseRequest(event -> {
            if (controller != null) {
                controller.cleanup();
            }
        });
    }

    public static void main(String[] args) {
        launch();
    }
}