package com.cgvsu.controller;

import com.cgvsu.manager.*;
import com.cgvsu.manager.interfaces.InputManagerImpl;
import com.cgvsu.manager.interfaces.FileManagerImpl;
import javafx.util.Callback;

// Фабрика контроллеров для FXMLLoader
public class ControllerFactory implements Callback<Class<?>, Object> {

    private final SceneManager sceneManager;
    private final AnimationManager animationManager;
    private final UIManager uiManager;
    private final FileManagerImpl modelManager;
    private final InputManagerImpl inputManager;
    private final MainController mainController;

    public ControllerFactory(SceneManager sceneManager, AnimationManager animationManager,
                             UIManager uiManager, FileManagerImpl modelManager,
                             InputManagerImpl inputManager, MainController mainController) {
        this.sceneManager = sceneManager;
        this.animationManager = animationManager;
        this.uiManager = uiManager;
        this.modelManager = modelManager;
        this.inputManager = inputManager;
        this.mainController = mainController;
    }

    @Override
    public Object call(Class<?> type) {
        if (type == ViewportController.class) {
            return new ViewportController(sceneManager, animationManager, inputManager, mainController);
        }
        if (type == ToolController.class) {
            return new ToolController(sceneManager, uiManager, mainController);
        }
        if (type == MenuController.class) {
            return new MenuController(modelManager, sceneManager, mainController);
        }

        // Попытка создать контроллер по умолчанию
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create controller: " + type.getName(), e);
        }
    }
}
