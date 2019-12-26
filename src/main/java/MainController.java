import cache.CacheInformationProvider;
import data.mod.ModData;
import data.mod.ModDataManager;
import data.mod.view.ModDataCell;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.Event;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import settings.AppSettingsCoordinator;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the {@link MainApplication}. Applies logic to UI-components. Abstracts from style in view
 * wherever possible!
 */
public class MainController implements Initializable {
    private static Logger logger = LogManager.getLogger("MainController");
    public PieChart cachePieChart;
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
        modSearchBar.textProperty().addListener((observableValue, s, t1) -> {
            String filter = modSearchBar.getText().toLowerCase();
            if (filter == null || filter.length() == 0) {
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

        modDataManager.getMods();
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
    }

    public void onCopySelectedAction() {
        ObservableList<ModData> selectedMods = getSelectedModsWorkshop();
        if (selectedMods.size() == 0) return;
        for (ModData mod : selectedMods) {
            try {
                boolean successful = mod.copyToServer();
                logger.info("Copying file {}({}) was {}successful", mod.getId(), mod.getTitle(), successful ? "" : "not ");
            } catch (IOException e) {
                logger.error("Unable to copy \"{}\" mod to server, skipping to next..", mod.getId(), e);
            }
        }
        logger.info("Copied mods from workshop to server: {}", selectedMods);
        ModDataManager.getInstance().updateView();
    }

    public void onUpdateServerModsAction() {
        ModDataManager.getInstance().updateServerModsData();
    }

    public void onDeleteServerModsAction() {
        ObservableList<ModData> selectedMods = getSelectedModsServer();
        if (selectedMods.size() == 0) return;
        // check if the user actually wanted to delete the mods server-side
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Deleting mods!");
        alert.setHeaderText("Confirm the deletion of the following selected mods from the server!");
        StringBuilder builder = new StringBuilder();
        for (ModData mod : selectedMods) {
            builder.append(mod.getTitle());
            builder.append(" (");
            builder.append(mod.getId());
            builder.append(")\n");
        }
        alert.setContentText(builder.toString());
        Optional<ButtonType> alertResult = alert.showAndWait();
        if (alertResult.get() == ButtonType.OK) {
            // delete the mods after confirmation by the user
            for (ModData mod : selectedMods) {
                try {
                    boolean successful = mod.deleteServerSide();
                    logger.info("Copying file {}({}) was {}successful", mod.getId(), mod.getTitle(), successful ? "" : "not ");
                } catch (IOException e) {
                    logger.error("Unable to delete \"{}\" mod from server, skipping to next..", mod.getId(), e);
                }
            }
            logger.info("Deleted mods from server: {}", selectedMods);
            ModDataManager.getInstance().updateView();
        }
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
}
