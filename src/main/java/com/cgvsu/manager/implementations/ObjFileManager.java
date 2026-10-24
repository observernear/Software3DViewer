package com.cgvsu.manager.implementations;

import com.cgvsu.manager.interfaces.FileManagerImpl;
import com.cgvsu.model.Model;
import com.cgvsu.render_engine.utils.NormalCalculator;
import com.cgvsu.render_engine.utils.Triangulator;
import com.cgvsu.utils.objtools.ObjReader;
import com.cgvsu.utils.objtools.ObjWriter;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ObjFileManager implements FileManagerImpl {

    @Override
    public void openModelFile(Window window, ModelLoadCallback onSuccess, ModelErrorCallback onError) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("3D Models (*.obj)", "*.obj")
        );
        fileChooser.setTitle("Open 3D Model");
        File file = fileChooser.showOpenDialog(window);
        if (file != null) loadModelFromFile(file, onSuccess, onError);
    }

    @Override
    public void saveModelFile(Window window, Model model, ModelSaveCallback onSuccess, ModelErrorCallback onError) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("3D Models (*.obj)", "*.obj")
        );
        fileChooser.setTitle("Save 3D Model");
        fileChooser.setInitialFileName("model.obj");
        File file = fileChooser.showSaveDialog(window);
        if (file != null) saveModelToFile(file, model, onSuccess, onError);
    }

    /** Чтение модели из файла и подготовка (триангуляция, нормали) */
    private void loadModelFromFile(File file, ModelLoadCallback onSuccess, ModelErrorCallback onError) {
        try {
            String fileContent = Files.readString(file.toPath());
            Model model = ObjReader.read(fileContent);
            Triangulator.triangulate(model);      // делаем только треугольники
            NormalCalculator.calculateNormals(model); // считаем нормали
            model.setName(file.getName());
            onSuccess.onModelLoaded(model);
        } catch (IOException exception) {
            onError.onError("Failed to load model: " + exception.getMessage());
        } catch (Exception exception) {
            onError.onError("Error parsing model: " + exception.getMessage());
        }
    }

    /** Сохранение модели в файл */
    private void saveModelToFile(File file, Model model, ModelSaveCallback onSuccess, ModelErrorCallback onError) {
        try {
            if (!file.getName().toLowerCase().endsWith(".obj")) {
                file = new File(file.getAbsolutePath() + ".obj");
            }
            String content = ObjWriter.modelToString(model, "From Software3DViewer");
            Files.writeString(file.toPath(), content);
            onSuccess.onModelSaved("Model saved successfully");
        } catch (IOException exception) {
            onError.onError("Failed to save model: " + exception.getMessage());
        } catch (Exception exception) {
            onError.onError("Error saving model: " + exception.getMessage());
        }
    }
}
