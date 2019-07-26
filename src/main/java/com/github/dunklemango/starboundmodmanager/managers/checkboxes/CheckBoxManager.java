package com.github.dunklemango.starboundmodmanager.managers.checkboxes;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

public abstract class CheckBoxManager {

    protected ObservableMap<String, Boolean> filesChecked = FXCollections.observableHashMap();

    public void clear() {
        filesChecked.clear();
    }

    public void put(String fileName, Boolean isChecked) {
        filesChecked.put(fileName, isChecked);
    }

    public Boolean get(String fileName) {
        return filesChecked.get(fileName);
    }
}
