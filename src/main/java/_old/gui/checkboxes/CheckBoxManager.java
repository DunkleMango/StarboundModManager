package _old.gui.checkboxes;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

public abstract class CheckBoxManager {
    protected ObservableMap<String, Boolean> filesChecked = FXCollections.observableHashMap();

    public void clear() {
        filesChecked.clear();
    }

    public void put(String checkBoxItemString, Boolean isChecked) {
        filesChecked.put(checkBoxItemString, isChecked);
    }

    public boolean get(String checkBoxItemString) {
        Boolean tmp = filesChecked.get(checkBoxItemString);
        return (tmp == null) ? false : tmp;
    }

    public abstract void debugLogCheckBoxes(String prefix);
}
