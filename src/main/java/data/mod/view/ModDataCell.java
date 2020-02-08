package data.mod.view;

import data.mod.ModData;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;

public class ModDataCell extends ListCell<ModData> {
    private HBox hBox = new HBox();
    private Label label = new Label("(empty)");
    private ImageView imageView = new ImageView();
    //private Image imageNew = new Image(getClass().getResourceAsStream("/application/new.png"));
    private ModData lastItem;

    public ModDataCell() {
        super();
        label.setWrapText(true);
        label.setFont(new Font(label.getFont().getFamily(), 16));
        label.setPadding(new Insets(5));
        hBox.setSpacing(10);
        hBox.getChildren().addAll(label);
    }

    @Override
    protected void updateItem(ModData item, boolean empty) {
        super.updateItem(item, empty);
        setText(null);  // No text in label of super class
        if (empty) {
            lastItem = null;
            setGraphic(null);
        } else {
            lastItem = item;
            label.setText(item.getTitle()!=null ? item.getTitle() : "<null>");
            //imageView2.setImage(imageNew);
            setGraphic(hBox);
        }
    }
}
