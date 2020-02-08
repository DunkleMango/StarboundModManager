package dialog.filesystemaction.view;

import javafx.scene.image.Image;

public class InteractiveImage {
    private Image defaultImage;
    private Image selectionImage;

    public InteractiveImage(Image defaultImage) {
        setDefaultImage(defaultImage);
    }

    public InteractiveImage(Image defaultImage, Image selectionImage) {
        this(defaultImage);
        setSelectionImage(selectionImage);
    }

    public void setDefaultImage(Image defaultImage) {
        this.defaultImage = defaultImage;
    }

    public void setSelectionImage(Image selectionImage) {
        this.selectionImage = selectionImage;
    }

    public Image getImage(InteractionStatus status) {
        switch (status) {
            case DEFAULT:   return this.defaultImage;
            case SELECTED:  return this.selectionImage;
            default:        return null;
        }
    }
}
