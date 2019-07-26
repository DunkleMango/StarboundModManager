package com.github.dunklemango.starboundmodmanager.managers.checkboxes;

public final class OutputCheckBoxManager extends CheckBoxManager {

    private static OutputCheckBoxManager instance;

    private OutputCheckBoxManager() {

    }

    public static OutputCheckBoxManager getInstance() {
        if (instance == null) {
            instance = new OutputCheckBoxManager();
        }
        return instance;
    }
}
