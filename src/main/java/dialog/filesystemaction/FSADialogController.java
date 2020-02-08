package dialog.filesystemaction;

import data.mod.ModData;
import dialog.filesystemaction.view.ModActionCell;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class FSADialogController implements Initializable {
    private static Logger logger = LogManager.getLogger("FSADialogController");

    public ListView<ModAction> changesListView;
    public Label changesHeaderLabel;
    public Label changesContentLabel;
    public Button cancelButton;
    public Button startButton;
    public ProgressBar changesProgressBar;

    private volatile ObservableList<ModAction> modActions;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logger.debug("changesProgressBar: width = {}, height = {}", changesProgressBar.getWidth(), changesProgressBar.getHeight());
        changesHeaderLabel.setText("Processing mods..");
        changesContentLabel.setText("The following mods are going to be modified.");
        modActions = FXCollections.observableArrayList();
        changesListView.setCellFactory(modActionListView -> new ModActionCell());
        changesListView.setItems(modActions);
        changesListView.setFocusModel(null);
    }

    public void supplyData(List<ModData> modDataList, ActionType actionType) {
        logger.debug(modActions);
        modActions = modDataList.stream()
                .map(modData -> new ModAction(modData, actionType))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        logger.debug(modActions);
        changesListView.setItems(modActions);
    }

    public void onStart() {
        startButton.setDisable(true);
        cancelButton.setDisable(true);
        startButton.setText("Finish");
        if (!modActions.isEmpty()) {
            changesProgressBar.setProgress(0);
            logger.debug("progress main1: {}", changesProgressBar.getProgress());
            final double stepSize = 1.0 / modActions.size();
            Task<Void> actionTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    final ReentrantLock lock = new ReentrantLock();
                    modActions.parallelStream().forEach(modAction -> {
                        modAction.execute();
                        Platform.runLater(() -> {
                            lock.lock();
                            try {
                                changesProgressBar.setProgress(changesProgressBar.getProgress() + stepSize);
                                logger.debug("progress mainR: {}", changesProgressBar.getProgress());
                            } finally {
                                lock.unlock();
                            }
                        });
                    });
                    return null;
                }
            };
            actionTask.setOnSucceeded(workerStateEvent -> onFinish());
            actionTask.setOnFailed(workerStateEvent -> onFinish());
            actionTask.setOnCancelled(workerStateEvent -> onFinish());
            logger.debug("progress main2: {}", changesProgressBar.getProgress());
            Thread thread = new Thread((Runnable) actionTask);
            thread.start();
        }
    }

    private void onFinish() {
        startButton.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                Stage stage = (Stage) startButton.getScene().getWindow();
                stage.close();
            }
        });
        startButton.setDisable(false);
    }

    public void onCancellation() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        // TODO - rollback changes if possible
        stage.close();
    }
}
