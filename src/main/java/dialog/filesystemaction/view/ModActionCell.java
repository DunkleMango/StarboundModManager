package dialog.filesystemaction.view;

import dialog.filesystemaction.ActionResultType;
import dialog.filesystemaction.ActionType;
import dialog.filesystemaction.ModAction;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class ModActionCell extends ListCell<ModAction> implements Observer {
    private static Logger logger = LogManager.getLogger("ModActionCell");
    private ModAction lastItem;

    private static Map<ActionType, String> actionTypeStringMap;
    private static Map<ActionResultType, String> actionResultTypeStringMap;
    private BorderPane rootPane = new BorderPane();
    private Label modTitle = new Label();
    private static Image copyActionIconBlack = new Image(ModActionCell.class.getResourceAsStream("/application/copy_black.png"));
    private static Image deleteActionIconBlack = new Image(ModActionCell.class.getResourceAsStream("/application/delete_black.png"));
    private static Image checkmarkIconGreen = new Image(ModActionCell.class.getResourceAsStream("/application/checkmark_green.png"));
    private static Image errorIconRed = new Image(ModActionCell.class.getResourceAsStream("/application/error_red.png"));
    private HBox infoBox = new HBox();

    private ImageWithTooltip actionView;
    private ImageWithTooltip completionStatusView;

    static {
        {
            Map<ActionType, String> tmpMap = new HashMap<>();
            tmpMap.put(ActionType.COPY, "Mods with this icon will be copied to the server.");
            tmpMap.put(ActionType.DELETE, "Mods with this icon will be deleted from the server.");
            actionTypeStringMap = Collections.unmodifiableMap(tmpMap);
        }
        {
            Map<ActionResultType, String> tmpMap = new HashMap<>();
            tmpMap.put(ActionResultType.INDETERMINATE, "No status available.");
            tmpMap.put(ActionResultType.SUCCESSFUL, "The operation on this mod was successful.");
            tmpMap.put(ActionResultType.ERROR, "A problem occurred and the operation on this mod was stopped.");
            actionResultTypeStringMap = Collections.unmodifiableMap(tmpMap);
        }
    }

    public ModActionCell() {
        super();
        modTitle.setFont(Font.font(modTitle.getFont().getFamily(), 14));
        rootPane.setLeft(modTitle);
        infoBox.setSpacing(10);
        actionView = new ImageWithTooltip(Optional.of(copyActionIconBlack), Optional.empty(), 16);
        BorderPane.setAlignment(actionView, Pos.CENTER);
        completionStatusView = new ImageWithTooltip(Optional.empty(), Optional.empty(), 16);
        BorderPane.setAlignment(completionStatusView, Pos.CENTER);
        infoBox.getChildren().addAll(actionView, completionStatusView);
        rootPane.setRight(infoBox);
    }

    @Override
    public void updateSelected(boolean isSelected) {
        super.updateSelected(isSelected);
    }

    private Image getActionImage(ActionType actionType) {
        Image actionImage = copyActionIconBlack;
        switch (actionType) {
            case COPY:
                actionImage = copyActionIconBlack;
                break;
            case DELETE:
                actionImage = deleteActionIconBlack;
                break;
        }
        return actionImage;
    }

    @Override
    public void updateItem(ModAction item, boolean empty) {
        super.updateItem(item, empty);
        setText(null);  // No text in label of super class
        if (empty) {
            // check for observers
            if (lastItem != null) {
                lastItem.deleteObserver(this);
                logger.debug("{}-cell unsubscribed from {}", hashCode(), lastItem);
            }
            // change UI
            lastItem = null;
            setGraphic(null);
        } else {
            // check for observers
            if (lastItem != null) {
                if (lastItem != item) {
                    lastItem.deleteObserver(this);
                    logger.debug("{}-cell unsubscribed from {}", hashCode(), lastItem);
                    item.addObserver(this);
                    logger.debug("{}-cell subscribed to {}", hashCode(), item);
                }
            } else {
                item.addObserver(this);
                logger.debug("{}-cell subscribed to {}", hashCode(), item);
            }
            // change UI
            lastItem = item;
            String title = item.getModTitle();
            modTitle.setText((title != null) ? title : "<null>");
            actionView.setTooltipText(actionTypeStringMap.get(item.getActionType()));
            actionView.setImage(getActionImage(item.getActionType()));
            completionStatusView.setTooltipText(actionResultTypeStringMap.get(item.getActionResultType()));
            setGraphic(rootPane);
        }
    }

    /**
     * This method is called whenever the observed object is changed. An
     * application calls an <tt>Observable</tt> object's
     * <code>notifyObservers</code> method to have all the object's
     * observers notified of the change.
     *
     * @param o   the observable object.
     * @param arg an argument passed to the <code>notifyObservers</code>
     */
    @Override
    public void update(Observable o, Object arg) {
        logger.debug("Update triggered by Observable");
        if (o instanceof ModAction) {
            ModAction item = (ModAction) o;
            switch (item.getActionResultType()) {
                case INDETERMINATE:
                    completionStatusView.removeImage();
                    break;
                case SUCCESSFUL:
                    completionStatusView.setImage(checkmarkIconGreen);
                    break;
                case ERROR:
                    completionStatusView.setImage(errorIconRed);
                    break;
            }
            completionStatusView.setTooltipText(actionResultTypeStringMap.get(item.getActionResultType()));
            logger.debug("Updated completion-status image");
        }
    }
}
