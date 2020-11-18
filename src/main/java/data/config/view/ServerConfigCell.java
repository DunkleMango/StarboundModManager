package data.config.view;

import data.config.ServerConfig;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.text.Font;

public class ServerConfigCell extends ListCell<ServerConfig> {
    private Label label = new Label("(empty)");
    private ServerConfig lastItem;

    public ServerConfigCell() {
        super();
        label.setWrapText(true);
        label.setFont(new Font(label.getFont().getFamily(), 16));
        label.setPadding(new Insets(5));
    }

    @Override
    protected void updateItem(ServerConfig item, boolean empty) {
        super.updateItem(item, empty);
        setText(null);  // No text in label of super class
        if (empty) {
            lastItem = null;
            setGraphic(null);
        } else {
            lastItem = item;
            label.setText(item.getName()!=null ? item.getName() : "<null>");
            setGraphic(label);
        }
    }
}
