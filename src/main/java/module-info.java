module com.cgvsu {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    // Экспортируем все пакеты, которые используются в FXML
    exports com.cgvsu;
    exports com.cgvsu.controller;
    exports com.cgvsu.manager;
    exports com.cgvsu.render_engine;
    exports com.cgvsu.utils.math;
    exports com.cgvsu.model;
    exports com.cgvsu.utils.objtools;
    exports com.cgvsu.manager.implementations;
    exports com.cgvsu.manager.interfaces;
    exports com.cgvsu.render_engine.rendering;

    // Открываем пакеты для рефлексии (FXML загрузка)
    opens com.cgvsu to javafx.fxml;
    opens com.cgvsu.controller to javafx.fxml;
    opens com.cgvsu.model to javafx.fxml;
    opens com.cgvsu.utils.math to javafx.fxml;
    opens com.cgvsu.render_engine to javafx.fxml;
    opens com.cgvsu.utils.objtools to javafx.fxml;
    opens com.cgvsu.manager.implementations to javafx.fxml;
    opens com.cgvsu.manager.interfaces to javafx.fxml;
    opens com.cgvsu.render_engine.rendering to javafx.fxml;
    exports com.cgvsu.render_engine.transform;
    opens com.cgvsu.render_engine.transform to javafx.fxml;
    exports com.cgvsu.render_engine.utils;
    opens com.cgvsu.render_engine.utils to javafx.fxml;
}