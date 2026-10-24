package com.cgvsu.controller;

import com.cgvsu.manager.SceneManager;
import com.cgvsu.manager.UIManager;
import com.cgvsu.model.Model;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.*;
import java.util.stream.Collectors;

public class DeletionController {

    @FXML private VBox deletionPanel;
    @FXML private Label verticesCountLabel, polygonsCountLabel, selectedIndicesLabel, indicesCountLabel, statusLabel;
    @FXML private Button deleteSelectedBtn, deleteUnusedBtn, parseIndicesBtn, deleteIndicesBtn, selectAllBtn, selectNoneBtn;
    @FXML private RadioButton vertexRadioButton, polygonRadioButton;
    @FXML private TextArea indicesInput;
    @FXML private VBox indicesInfoBox;

    private final SceneManager sceneManager;
    private final UIManager uiManager;
    private Model currentModel;
    private ToggleGroup deleteTypeGroup;
    private Set<Integer> selectedIndices = new HashSet<>();
    private Runnable onModelChanged;

    public DeletionController(SceneManager sceneManager, UIManager uiManager) {
        this.sceneManager = sceneManager;
        this.uiManager = uiManager;
    }

    @FXML
    private void initialize() {
        setupUI();
        setupListeners();
        hidePanel();

        // Обновление при смене активной модели
        sceneManager.activeModelProperty().addListener((obs, oldM, newM) -> setModel(newM));
    }

    // Инициализация радиокнопок и статистики
    private void setupUI() {
        deleteTypeGroup = new ToggleGroup();
        if (vertexRadioButton != null) vertexRadioButton.setToggleGroup(deleteTypeGroup);
        if (polygonRadioButton != null) polygonRadioButton.setToggleGroup(deleteTypeGroup);
        if (vertexRadioButton != null) vertexRadioButton.setSelected(true);
        updateStatistics();
    }

    // Привязка событий кнопок
    private void setupListeners() {
        if (deleteSelectedBtn != null) deleteSelectedBtn.setOnAction(e -> handleDeleteSelected());
        if (deleteUnusedBtn != null) deleteUnusedBtn.setOnAction(e -> handleDeleteUnused());
        if (parseIndicesBtn != null) parseIndicesBtn.setOnAction(e -> handleParseIndices());
        if (deleteIndicesBtn != null) deleteIndicesBtn.setOnAction(e -> handleDeleteIndices());
        if (selectAllBtn != null) selectAllBtn.setOnAction(e -> handleSelectAll());
        if (selectNoneBtn != null) selectNoneBtn.setOnAction(e -> handleSelectNone());
    }

    // Установка текущей модели
    public void setModel(Model model) {
        currentModel = model;
        updateStatistics();
        clearSelection();
    }

    public void setOnModelChanged(Runnable callback) { this.onModelChanged = callback; }

    // Обновление информации о модели
    private void updateStatistics() {
        if (currentModel == null) {
            if (verticesCountLabel != null) verticesCountLabel.setText("0");
            if (polygonsCountLabel != null) polygonsCountLabel.setText("0");
            if (statusLabel != null) statusLabel.setText("Модель не загружена");
            return;
        }

        if (verticesCountLabel != null) verticesCountLabel.setText(String.valueOf(currentModel.getVertices().size()));
        if (polygonsCountLabel != null) polygonsCountLabel.setText(String.valueOf(currentModel.getPolygons().size()));

        if (indicesInput != null && vertexRadioButton != null) {
            boolean isVertexMode = vertexRadioButton.isSelected();
            int maxIndex = isVertexMode ? currentModel.getVertices().size() - 1 : currentModel.getPolygons().size() - 1;
            if (maxIndex >= 0) {
                indicesInput.setPromptText(String.format("Индексы %s (0-%d). Пример: 1,2,5 или 0-5 или 1,3-5,10",
                        isVertexMode ? "вершин" : "полигонов", maxIndex));
            }
        }
    }

    // Удаление выбранных индексов
    @FXML private void handleDeleteSelected() {
        if (currentModel == null) { showStatus("Модель не загружена", "error"); return; }
        if (selectedIndices.isEmpty()) { showStatus("Нет выбранных индексов", "warning"); return; }

        int deleted = deleteSelectedItems();
        if (deleted > 0) {
            showStatus(String.format("Удалено %d %s", deleted, (vertexRadioButton != null && vertexRadioButton.isSelected()) ? "вершин" : "полигонов"), "success");
            updateStatistics();
            clearSelection();
            notifyModelChanged();
        }
    }

    // Удаление неиспользуемых вершин
    @FXML private void handleDeleteUnused() {
        if (currentModel == null) { showStatus("Модель не загружена", "error"); return; }
        int deleted = currentModel.deleteUnusedVertices();
        showStatus(String.format("Удалено %d неиспользуемых вершин", deleted), "success");
        updateStatistics();
        notifyModelChanged();
    }

    // Парсинг введённых индексов
    @FXML private void handleParseIndices() {
        if (indicesInput == null) return;
        String input = indicesInput.getText().trim();
        if (input.isEmpty()) { showStatus("Введите индексы", "warning"); return; }

        try {
            selectedIndices = parseIndices(input);
            updateIndicesInfo();
            showStatus("Индексы успешно выбраны", "success");
        } catch (Exception e) {
            showStatus("Ошибка в формате индексов: " + e.getMessage(), "error");
            selectedIndices.clear();
            updateIndicesInfo();
        }
    }

    // Удаление конкретных индексов
    @FXML private void handleDeleteIndices() {
        if (selectedIndices.isEmpty()) { showStatus("Нет индексов", "warning"); return; }
        if (currentModel == null) { showStatus("Модель не загружена", "error"); return; }

        boolean isVertexMode = vertexRadioButton != null && vertexRadioButton.isSelected();
        int maxIndex = isVertexMode ? currentModel.getVertices().size() - 1 : currentModel.getPolygons().size() - 1;

        for (int index : selectedIndices) {
            if (index < 0 || index > maxIndex) {
                showStatus(String.format("Индекс %d выходит за пределы (0-%d)", index, maxIndex), "error");
                return;
            }
        }

        int deleted = deleteSelectedItems();
        if (deleted > 0) {
            showStatus(String.format("Удалено %d %s", deleted, isVertexMode ? "вершин" : "полигонов"), "success");
            updateStatistics();
            clearSelection();
            notifyModelChanged();
        }
    }

    // Выбрать все индексы
    @FXML private void handleSelectAll() {
        if (currentModel == null) { showStatus("Модель не загружена", "error"); return; }
        boolean isVertexMode = vertexRadioButton != null && vertexRadioButton.isSelected();
        int maxIndex = isVertexMode ? currentModel.getVertices().size() - 1 : currentModel.getPolygons().size() - 1;

        selectedIndices.clear();
        for (int i = 0; i <= maxIndex; i++) selectedIndices.add(i);
        if (indicesInput != null) indicesInput.setText("0-" + maxIndex);

        updateIndicesInfo();
        showStatus(String.format("Выбраны все %d %s", selectedIndices.size(), isVertexMode ? "вершин" : "полигонов"), "info");
    }

    // Снять выбор
    @FXML private void handleSelectNone() {
        selectedIndices.clear();
        if (indicesInput != null) indicesInput.clear();
        updateIndicesInfo();
        showStatus("Выбор сброшен", "info");
    }

    // Парсинг строковых индексов
    private Set<Integer> parseIndices(String input) {
        Set<Integer> indices = new HashSet<>();
        for (String part : input.split(",")) {
            part = part.trim();
            if (part.isEmpty()) continue;

            if (part.contains("-")) {
                String[] range = part.split("-");
                if (range.length != 2) throw new IllegalArgumentException("Неверный формат диапазона: " + part);
                int start = Integer.parseInt(range[0].trim());
                int end = Integer.parseInt(range[1].trim());
                if (start > end) throw new IllegalArgumentException("Начало диапазона больше конца: " + part);
                for (int i = start; i <= end; i++) indices.add(i);
            } else {
                indices.add(Integer.parseInt(part));
            }
        }
        return indices;
    }

    // Удаление выбранных индексов
    private int deleteSelectedItems() {
        if (selectedIndices.isEmpty() || currentModel == null) return 0;
        List<Integer> list = new ArrayList<>(selectedIndices);
        list.sort(Collections.reverseOrder());
        return (vertexRadioButton != null && vertexRadioButton.isSelected())
                ? currentModel.deleteVertices(list)
                : currentModel.deletePolygons(list);
    }

    // Обновление панели выбранных индексов
    private void updateIndicesInfo() {
        if (indicesInfoBox == null) return;
        indicesInfoBox.setVisible(!selectedIndices.isEmpty());
        if (selectedIndices.isEmpty()) return;

        List<Integer> sorted = new ArrayList<>(selectedIndices);
        Collections.sort(sorted);

        String text = sorted.size() <= 10
                ? sorted.stream().map(String::valueOf).collect(Collectors.joining(", "))
                : sorted.get(0) + ", " + sorted.get(1) + ", ..., " + sorted.get(sorted.size() - 1);

        if (selectedIndicesLabel != null) selectedIndicesLabel.setText(text);
        if (indicesCountLabel != null) indicesCountLabel.setText(String.format("Всего: %d элементов", selectedIndices.size()));
    }

    // Очистка выбора
    private void clearSelection() {
        selectedIndices.clear();
        if (indicesInput != null) indicesInput.clear();
        updateIndicesInfo();
    }

    // Показ сообщений пользователю
    private void showStatus(String message, String type) {
        if (statusLabel == null) return;
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("status-success", "status-error", "status-warning", "status-info");
        switch (type) {
            case "success" -> statusLabel.getStyleClass().add("status-success");
            case "error" -> statusLabel.getStyleClass().add("status-error");
            case "warning" -> statusLabel.getStyleClass().add("status-warning");
            default -> statusLabel.getStyleClass().add("status-info");
        }
    }

    private void notifyModelChanged() {
        if (onModelChanged != null) onModelChanged.run();
    }

    // Показ/скрытие панели
    public void showPanel() {
        if (deletionPanel != null) {
            deletionPanel.setVisible(true);
            deletionPanel.setManaged(true);
            if (currentModel != null) updateStatistics();
        }
    }

    public void hidePanel() {
        if (deletionPanel != null) {
            deletionPanel.setVisible(false);
            deletionPanel.setManaged(false);
        }
    }

    // Получение корневого узла панели
    public Node getRoot() { return deletionPanel; }
}
