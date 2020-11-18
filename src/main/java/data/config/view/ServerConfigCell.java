package data.config.view;

import data.config.ServerConfig;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.text.Font;

import java.util.Optional;

public class ServerConfigCell extends ListCell<ServerConfig> {
    private Label label = new Label("(empty)");
    private ContextMenu contextMenu;
    private ServerConfig currentItem;

    public ServerConfigCell() {
        super();
        label.setWrapText(true);
        label.setFont(new Font(label.getFont().getFamily(), 16));
        label.setPadding(new Insets(5));
        contextMenu = new ContextMenu();
        MenuItem renameMenuItem = new MenuItem("rename");
        renameMenuItem.setOnAction(actionEvent -> {
            TextInputDialog dialog = new TextInputDialog(currentItem == null ? "" : currentItem.getName());
            Optional<String> newName = dialog.showAndWait();
            newName.ifPresent(name -> currentItem.rename(name));
            updateLabel(currentItem.getName());
        });
        contextMenu.getItems().add(renameMenuItem);
        setContextMenu(contextMenu);
    }

    @Override
    protected void updateItem(ServerConfig item, boolean empty) {
        super.updateItem(item, empty);
        setText(null);  // No text in label of super class
        if (empty) {
            currentItem = null;
            setGraphic(null);
        } else {
            currentItem = item;
            updateLabel(item.getName());
            setGraphic(label);
        }
    }

    private void updateLabel(String name) {
        label.setText(name);
    }
}
