package com.cgvsu.manager;

import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import com.cgvsu.render_engine.transform.Transform;

public class UIManager {
    private boolean updatingFromModel = false; // флаг, чтобы спиннеры не вызывали рекурсивное обновление

    // Инициализация всех спиннеров трансформации
    public void setupTransformSpinners(Spinner<Double> translateXField, Spinner<Double> translateYField,
                                       Spinner<Double> translateZField, Spinner<Double> rotateXField,
                                       Spinner<Double> rotateYField, Spinner<Double> rotateZField,
                                       Spinner<Double> scaleXField, Spinner<Double> scaleYField,
                                       Spinner<Double> scaleZField) {

        setupSpinner(translateXField, -100.0, 100.0, 0.0, 0.1);
        setupSpinner(translateYField, -100.0, 100.0, 0.0, 0.1);
        setupSpinner(translateZField, -100.0, 100.0, 0.0, 0.1);

        setupSpinner(rotateXField, -360.0, 360.0, 0.0, 1.0);
        setupSpinner(rotateYField, -360.0, 360.0, 0.0, 1.0);
        setupSpinner(rotateZField, -360.0, 360.0, 0.0, 1.0);

        setupSpinner(scaleXField, 0.01, 10.0, 1.0, 0.1);
        setupSpinner(scaleYField, 0.01, 10.0, 1.0, 0.1);
        setupSpinner(scaleZField, 0.01, 10.0, 1.0, 0.1);
    }

    // Настройка одного спиннера
    private void setupSpinner(Spinner<Double> spinner, double min, double max, double initial, double step) {
        if (spinner != null) {
            SpinnerValueFactory.DoubleSpinnerValueFactory factory =
                    new SpinnerValueFactory.DoubleSpinnerValueFactory(min, max, initial, step);
            spinner.setValueFactory(factory);
        }
    }

    // Создание Transform из значений спиннеров
    public Transform createTransformFromSpinners(Spinner<Double> translateXField, Spinner<Double> translateYField,
                                                 Spinner<Double> translateZField, Spinner<Double> rotateXField,
                                                 Spinner<Double> rotateYField, Spinner<Double> rotateZField,
                                                 Spinner<Double> scaleXField, Spinner<Double> scaleYField,
                                                 Spinner<Double> scaleZField) {

        Transform transform = new Transform();
        transform.translateX = getSpinnerValue(translateXField);
        transform.translateY = getSpinnerValue(translateYField);
        transform.translateZ = getSpinnerValue(translateZField);

        transform.rotateX = (float) Math.toRadians(getSpinnerValue(rotateXField));
        transform.rotateY = (float) Math.toRadians(getSpinnerValue(rotateYField));
        transform.rotateZ = (float) Math.toRadians(getSpinnerValue(rotateZField));

        transform.scaleX = getSpinnerValue(scaleXField);
        transform.scaleY = getSpinnerValue(scaleYField);
        transform.scaleZ = getSpinnerValue(scaleZField);

        return transform;
    }

    // Обновление спиннеров по Transform
    public void updateSpinnersFromTransform(Transform transform,
                                            Spinner<Double> translateXField, Spinner<Double> translateYField,
                                            Spinner<Double> translateZField, Spinner<Double> rotateXField,
                                            Spinner<Double> rotateYField, Spinner<Double> rotateZField,
                                            Spinner<Double> scaleXField, Spinner<Double> scaleYField,
                                            Spinner<Double> scaleZField) {
        if (transform == null) return;

        updatingFromModel = true; // блокируем реакцию на изменение спиннеров
        try {
            setSpinnerValue(translateXField, transform.translateX);
            setSpinnerValue(translateYField, transform.translateY);
            setSpinnerValue(translateZField, transform.translateZ);

            setSpinnerValue(rotateXField, Math.toDegrees(transform.rotateX));
            setSpinnerValue(rotateYField, Math.toDegrees(transform.rotateY));
            setSpinnerValue(rotateZField, Math.toDegrees(transform.rotateZ));

            setSpinnerValue(scaleXField, transform.scaleX);
            setSpinnerValue(scaleYField, transform.scaleY);
            setSpinnerValue(scaleZField, transform.scaleZ);
        } finally {
            updatingFromModel = false;
        }
    }

    private float getSpinnerValue(Spinner<Double> spinner) {
        return spinner != null && spinner.getValue() != null ? spinner.getValue().floatValue() : 0.0f;
    }

    private void setSpinnerValue(Spinner<Double> spinner, double value) {
        if (spinner != null && spinner.getValueFactory() != null) {
            spinner.getValueFactory().setValue(value);
        }
    }

    public boolean isUpdatingFromModel() {
        return updatingFromModel;
    }

    public void setUpdatingFromModel(boolean updating) {
        this.updatingFromModel = updating;
    }
}
