package com.cgvsu.manager;

import com.cgvsu.render_engine.Camera;
import java.util.ArrayList;
import java.util.List;

public class CameraManager {

    private final List<Camera> cameras = new ArrayList<>();
    private int activeIndex = -1;

    public CameraManager() {}

    public void addCamera(Camera camera) {
        cameras.add(camera);
        if (activeIndex == -1) activeIndex = 0; // первая камера становится активной
    }

    public void removeCamera(Camera camera) {
        int index = cameras.indexOf(camera);
        if (index == activeIndex) {
            activeIndex = cameras.size() > 1 ? 0 : -1;
        } else if (index < activeIndex) {
            activeIndex--;
        }
        cameras.remove(camera);
    }

    public Camera getActiveCamera() {
        if (activeIndex >= 0 && activeIndex < cameras.size()) {
            return cameras.get(activeIndex);
        }
        return null;
    }

    public void setActiveCamera(int index) {
        if (index >= 0 && index < cameras.size()) {
            activeIndex = index;
        }
    }

    public List<Camera> getCameras() {
        return cameras;
    }

    public int getActiveIndex() {
        return activeIndex;
    }
}
