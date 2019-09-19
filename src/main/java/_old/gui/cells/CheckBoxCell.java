package _old.gui.cells;

import _old.gui.checkboxes.CheckBoxManager;
import _old.gui.checkboxes.InputCheckBoxManager;
import _old.gui.checkboxes.OutputCheckBoxManager;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CheckBoxCell extends ListCell<String> {
    private static final Logger logger = LogManager.getLogger("CheckBoxCell");
    private final RepresentingType type;
    private HBox hBox = new HBox();
    private Label label = new Label("(empty)");
    private Pane pane = new Pane();
    private CheckBox checkBox = new CheckBox();
    private CheckBoxManager checkBoxManager;
    private final ListView<String> parent;
    String lastItem;

    public CheckBoxCell(RepresentingType type, ListView<String> parent) {
        super();
        this.type = type;
        this.parent = parent;
        this.getStyleClass().add("checkBoxCell");

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

        label.setWrapText(true);
        label.setPadding(new Insets(0, 2, 0, 2));
        label.getStyleClass().add("checkBoxCellLabel");
        hBox.getChildren().addAll(label, pane, checkBox);
        label.setMaxWidth(parent.getWidth() - 60);
        hBox.setHgrow(pane, Priority.ALWAYS);
        hBox.setPadding(new Insets(5, 0, 5 ,0));
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
