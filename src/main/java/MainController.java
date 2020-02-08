import cache.CacheInformationProvider;
import data.mod.ModData;
import data.mod.ModDataManager;
import data.mod.view.ModDataCell;
import dialog.filesystemaction.ActionType;
import dialog.filesystemaction.FSADialogController;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import settings.AppSettingsCoordinator;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the {@link MainApplication}. Applies logic to UI-components. Abstracts from style in view
 * wherever possible!
 */
public class MainController implements Initializable {
    private static Logger logger = LogManager.getLogger("MainController");
    public PieChart cachePieChart;
    public TabPane rootTabPane;
    public Tab settingsTab;
    public Tab modControlTab;
    public Label steamDirectoryLabel;
    public Button steamDirectorySelectButton;
    public Button clearCacheButton;
    public Button copySelectedButton;
    public Button updateAllButton;
    public Button deleteSelectedButton;
    public ListView<ModData> workshopModsListView;
    public ListView<ModData> serverModsListView;
    public TextField modSearchBar;

    //TODO find out what URL and ResourceBundle do in this context to meaningfully extend javadoc

    /**
     * Sets up the application when starting. Loads values from files etc. and stores them inside the control elements.
     * @param url {@link URL}
     * @param resourceBundle {@link ResourceBundle}
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        AppSettingsCoordinator asc = AppSettingsCoordinator.getInstance();
        steamDirectoryLabel.setText(asc.getSteamDirectory().getAbsolutePath());

        CacheInformationProvider cacheInformationProvider = CacheInformationProvider.getInstance();
        cacheInformationProvider.linkStatistics(cachePieChart);

        ModDataManager modDataManager = ModDataManager.getInstance();
        modDataManager.initiallyLoadMods();

        boolean isHealthy = checkFSIntegrity();
        updateUIHealthMode(isHealthy);

        modSearchBar.textProperty().addListener((observableValue, s, t1) -> {
            String filter = modSearchBar.getText().toLowerCase();
            if (filter.length() == 0) {
                modDataManager.setModFilter(modData -> true);
            } else {
                modDataManager.setModFilter(modData -> modData.getTitle().toLowerCase().contains(filter));
            }
        });

        workshopModsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        workshopModsListView.setCellFactory(modDataListView -> new ModDataCell());
        modDataManager.setWorkshopModsListView(workshopModsListView);

        serverModsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        serverModsListView.setCellFactory(modDataListView -> new ModDataCell());
        modDataManager.setServerModsListView(serverModsListView);
    }

    private void updateUIHealthMode(boolean isHealthy) {
        modControlTab.setDisable(!isHealthy);
        if (isHealthy) {
            rootTabPane.getSelectionModel().select(modControlTab);
        }
    }

    private boolean checkFSIntegrity() {
        AppSettingsCoordinator asc = AppSettingsCoordinator.getInstance();
        String alertTitle = "ERROR: Unable to find Starbound!";
        String alertContentText = "Please select the correct steam directory and try again.";
        File steamDir = asc.getSteamDirectory();
        if (!steamDir.exists()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(alertTitle);
            alert.setHeaderText("The steam directory under \"" + steamDir.getAbsolutePath() + "\" could not be found.");
            alert.setContentText(alertContentText);
            alert.showAndWait();
            return false;
        }
        File workshopDir = asc.getWorkshopDirectory();
        if (!workshopDir.exists()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(alertTitle);
            alert.setHeaderText("The Starbound workshop directory under \"" + workshopDir.getAbsolutePath() + "\" could not be found.");
            alert.setContentText(alertContentText);
            alert.showAndWait();
            return false;
        }
        File serverDir = asc.getServerDirectory();
        if (!serverDir.exists()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(alertTitle);
            alert.setHeaderText("The Starbound server directory under \"" + serverDir.getAbsolutePath() + "\" could not be found.");
            alert.setContentText(alertContentText);
            alert.showAndWait();
            return false;
        }
        return true;
    }

    /**
     * Specifies the behaviour of the application, when the user switches to a different tab on the root pane.
     * <p/>
     * Delegates tasks to components of the tabs, allowing the application to load, unload, store and update data.
     * New values should be set in multithreaded singleton classes and never in this section.
     * Use observable collections instead of reloading values again and again.
     * @param event The event associated with the selection of a tab
     * @see Tab
     * @see javafx.scene.control.TabPane
     */
    public void onTabSelection(@NotNull Event event) {
        if (settingsTab.isSelected()) {
            logger.debug("selection: settingsTab");
        } else if (modControlTab.isSelected()) {
            logger.debug("selection: modControlTab");
        }
    }

    /**
     * Specifies the behaviour of the application, when the user presses the button, corresponding to selecting the
     * steam directory.
     * <p/>
     * When pressing the button, a {@link DirectoryChooser} will appear. The selected directory - if it is valid -
     * is then saved to the {@link AppSettingsCoordinator} and stored in the text of the {@link Label}
     * above the button, indicating the success of the action to the user.
     * @param event The event associated with the press of the button, corresponding to selecting the steam directory
     */
    public void onSteamDirectorySelectAction(@NotNull Event event) {
        // Select directory
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("select path to steam");
        File dir = directoryChooser.showDialog(null);
        if (dir == null || !dir.exists()) return;
        // Save in settings and show as text in label
        AppSettingsCoordinator appSettingsCoordinator = AppSettingsCoordinator.getInstance();
        appSettingsCoordinator.setSteamDirectory(dir);
        steamDirectoryLabel.setText(dir.getAbsolutePath());

        boolean isHealthy = checkFSIntegrity();
        updateUIHealthMode(isHealthy);
        if (isHealthy) ModDataManager.getInstance().reloadMods();
    }

    public void showFSADialogAndReloadMods(ObservableList<ModData> mods, ActionType actionType) {
        final Stage stage = new Stage();
        stage.getIcons().add(new Image(this.getClass().getResource("application/icon.png").toString()));
        Parent root = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("application/dialog_filesystemaction.fxml"));
            root = loader.load();
            FSADialogController dialogController = loader.<FSADialogController>getController();
            dialogController.supplyData(mods, actionType);
        } catch (IOException e) {
            logger.error("Unable to load dialog_filesystemaction.fxml");
            return;
        }
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();

        ModDataManager.getInstance().reloadMods();
    }

    public void onCopySelectedAction() {
        ObservableList<ModData> mods = getSelectedModsWorkshop();
        showFSADialogAndReloadMods(mods, ActionType.COPY);
        logger.info("Copied mods from workshop to server: {}", mods);
    }

    public void onUpdateServerModsAction() {
        ObservableList<ModData> mods = getModsServer();
        showFSADialogAndReloadMods(mods, ActionType.COPY);
        logger.info("Updated server mods via workshop: {}", mods);
    }

    public void onDeleteServerModsAction() {
        ObservableList<ModData> mods = getSelectedModsServer();
        showFSADialogAndReloadMods(mods, ActionType.DELETE);
        logger.info("Deleted mods from server: {}", mods);
    }

    public void onClearCacheAction() {
        ModDataManager.getInstance().clearModsAndSavedData();
    }

    public void onWorkshopItemSelection() {
        logger.debug("Current selection in workshop: {}", getSelectedModsWorkshop());
    }

    public void onServerItemSelection() {
        logger.debug("Current selection in server: {}", getSelectedModsServer());
    }

    private ObservableList<ModData> getSelectedModsWorkshop() {
        return workshopModsListView.getSelectionModel().getSelectedItems();
    }

    private ObservableList<ModData> getSelectedModsServer() {
        return serverModsListView.getSelectionModel().getSelectedItems();
    }

    private ObservableList<ModData> getModsServer() {
        return serverModsListView.getItems();
    }
}
