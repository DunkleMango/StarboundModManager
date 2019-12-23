package data.mod.view;

import data.mod.ModData;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class ModDataCell extends ListCell<ModData> {
    private HBox hBox = new HBox();
    private Label label = new Label("(empty)");
    private ImageView imageView = new ImageView();
    private Image imageNew = new Image(getClass().getResourceAsStream("/application/new.png"));
    private ImageView imageView2 = new ImageView();
    private ModData lastItem;

    public ModDataCell() {
        super();
        label.setWrapText(true);
        label.setFont(new Font("Cambria", 16));
        label.setPadding(new Insets(5));
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(100);
        imageView.setFitWidth(100);
        imageView2.setPreserveRatio(true);
        imageView2.setFitHeight(32);
        imageView2.setFitWidth(32);
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
            //imageView.setImage(item.getPreviewImage());
            //imageView2.setImage(imageNew);
            setGraphic(hBox);
        }
    }
}
