import javafx.event.Event;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.stage.DirectoryChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import settings.cache.CacheContainer;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the {@link MainApplication}. Applies logic to UI-components. Abstract from style in view
 * wherever possible!
 */
public class MainController implements Initializable {
    private static Logger logger = LogManager.getLogger("MainController");
    public PieChart cachePieChart;
    public Tab settingsTab;
    public Tab modControlTab;
    public Label pathToSteamLabel;
    public Button selectPathToSteamButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        CacheContainer cacheContainer = CacheContainer.getInstance();
        cacheContainer.linkStatistics(cachePieChart);
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
    public void onTabSelection(Event event) {
        if (settingsTab.isSelected()) {
            logger.debug("selection: settingsTab");
        } else if (modControlTab.isSelected()) {
            logger.debug("selection: modControlTab");
        }
    }

    public void onPathToSteamSelectAction(Event event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("select path to steam");
        File dir = directoryChooser.showDialog(null);
        if (dir == null || !dir.exists()) return;
        pathToSteamLabel.setText(dir.getAbsolutePath());
    }
}
