package data.config.view;

import data.config.ServerConfig;
import data.config.ServerConfigManager;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.Optional;

public class ServerConfigCell extends ListCell<ServerConfig> {
    private final Label label = new Label("(empty)");
    private ServerConfig currentItem;

    public ServerConfigCell() {
        super();
        label.setWrapText(true);
        label.setFont(new Font(label.getFont().getFamily(), 16));
        label.setPadding(new Insets(5));
        ContextMenu contextMenu = new ContextMenu();
        MenuItem renameMenuItem = new MenuItem("rename");
        renameMenuItem.setOnAction(actionEvent -> {
            TextInputDialog dialog = new TextInputDialog(currentItem == null ? "" : currentItem.getName());
            Optional<String> newName = dialog.showAndWait();
            newName.ifPresent(name -> currentItem.rename(name));
            updateLabel(currentItem.getName());
        });
        MenuItem exportMenuItem = new MenuItem("export");
        exportMenuItem.setOnAction(actionEvent -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select storage location");
            fileChooser.setInitialFileName(currentItem.getName());
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Configuration files (*.config)", "*.config"));
            File file = fileChooser.showSaveDialog(null);
            currentItem.exportData(file);
        });
        MenuItem deleteMenuItem = new MenuItem("delete");
        deleteMenuItem.setOnAction(actionEvent -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm deletion");
            alert.setHeaderText("Deletion of server config \"" + currentItem.getName() + "\"");
            alert.setContentText("Are you ok with this?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                ServerConfigManager.getInstance().removeServerConfig(currentItem);
            }
        });
        contextMenu.getItems().addAll(renameMenuItem, exportMenuItem, deleteMenuItem);
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
