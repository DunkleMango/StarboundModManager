package gui.cells;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import managers.checkboxes.CheckBoxManager;
import managers.checkboxes.InputCheckBoxManager;
import managers.checkboxes.OutputCheckBoxManager;

public class CheckBoxCell extends ListCell<String> {

    private final RepresentingType type;
    private HBox hBox = new HBox();
    private Label label = new Label("(empty)");
    private Pane pane = new Pane();
    private CheckBox checkBox = new CheckBox();
    private CheckBoxManager checkBoxManager;
    String lastItem;

    public CheckBoxCell(RepresentingType type) {
        super();
        this.type = type;

        init();
    }

    private void init() {
        switch (type) {
            case INPUT:
                checkBoxManager = InputCheckBoxManager.getInstance();
                break;
            case OUTPUT:
                checkBoxManager = OutputCheckBoxManager.getInstance();
                break;
        }

        checkBox.setOnAction(event -> {
            checkBoxManager.put(getItem(), getChecked());
        });
        hBox.getChildren().addAll(label, pane, checkBox);
        HBox.setHgrow(pane, Priority.ALWAYS);
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setText(null);  // No text in label of super class
        if (empty) {
            lastItem = null;
            setGraphic(null);
        } else {
            lastItem = item;
            label.setText(item!=null ? item : "<null>");
            setGraphic(hBox);
        }
        Boolean isSelected = checkBoxManager.get(item);
        checkBox.setSelected(isSelected != null ? isSelected : false);
    }

    public boolean getChecked() {
        return checkBox.isSelected();
    }
}
