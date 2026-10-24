package com.cgvsu.manager.interfaces;

import com.cgvsu.model.Model;
import javafx.stage.Window;

public interface FileManagerImpl {

    /** Открыть модель через диалог */
    void openModelFile(Window window, ModelLoadCallback onSuccess, ModelErrorCallback onError);

    /** Сохранить модель через диалог */
    void saveModelFile(Window window, Model model, ModelSaveCallback onSuccess, ModelErrorCallback onError);

    /** Колбек успешной загрузки модели */
    @FunctionalInterface
    interface ModelLoadCallback {
        void onModelLoaded(Model model);
    }

    /** Колбек успешного сохранения модели */
    @FunctionalInterface
    interface ModelSaveCallback {
        void onModelSaved(String message);
    }

    /** Колбек ошибки при работе с моделью */
    @FunctionalInterface
    interface ModelErrorCallback {
        void onError(String errorMessage);
    }
}
