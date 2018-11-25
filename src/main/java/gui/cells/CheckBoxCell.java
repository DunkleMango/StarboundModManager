package gui.cells;

import managers.checkboxes.InputCheckBoxManager;
import managers.checkboxes.OutputCheckBoxManager;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

public class CheckBoxCell extends ListCell<String> {

    private HBox hBox = new HBox();
    private Label label = new Label("(empty)");
    private Pane pane = new Pane();
    private CheckBox checkBox = new CheckBox();
    String lastItem;

    public CheckBoxCell(RepresentingType type) {
        super();
        checkBox.setOnAction(event -> {
            switch (type) {
                case INPUT:
                    InputCheckBoxManager.getInstance().put(getItem(), getChecked());
                    break;
                case OUTPUT:
                    OutputCheckBoxManager.getInstance().put(getItem(), getChecked());
                    break;
            }
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
        checkBox.setSelected(false);
    }

    public boolean getChecked() {
        return checkBox.isSelected();
    }
}
