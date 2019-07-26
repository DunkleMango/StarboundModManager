package com.github.dunklemango.starboundmodmanager.managers.checkboxes;

public final class InputCheckBoxManager extends CheckBoxManager {

    private static InputCheckBoxManager instance;

    private InputCheckBoxManager() {

    }

    public static InputCheckBoxManager getInstance() {
        if (instance == null) {
            instance = new InputCheckBoxManager();
        }
        return instance;
    }
}
