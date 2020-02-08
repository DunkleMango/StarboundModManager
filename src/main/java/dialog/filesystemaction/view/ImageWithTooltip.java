package dialog.filesystemaction.view;

import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.util.Optional;

public class ImageWithTooltip extends AnchorPane {
    private final ImageView imageView;
    private final Tooltip tooltip;

    public ImageWithTooltip(Optional<Image> image, Optional<String> tooltipText, double size) {
        this(image, tooltipText, size, size);
    }

    public ImageWithTooltip(Optional<Image> image, Optional<String> tooltipText, double width, double height) {
        super();
        this.imageView = new ImageView(image.orElse(null));
        this.tooltip = new Tooltip(tooltipText.orElse(""));

        imageView.setPreserveRatio(true);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        this.setPrefSize(width, height);
        this.getChildren().add(imageView);
        Tooltip.install(this, tooltip);
    }

    public void setTooltipText(String newTooltipText) {
        this.tooltip.setText(newTooltipText);
    }

    public void setImage(Image newImage) {
        this.imageView.setImage(newImage);
    }

    public void removeImage() {
        this.imageView.setImage(null);
    }

}
