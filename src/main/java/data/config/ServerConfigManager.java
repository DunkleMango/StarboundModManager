package data.config;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.ListView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerConfigManager {
    private static final Logger logger = LogManager.getLogger("ServerConfigManager");
    private static volatile ServerConfigManager instance;
    private ObservableList<ServerConfig> configs;
    private FilteredList<ServerConfig> configsFiltered;
    // Synchronization locks
    private static final Object instanceLock = new Object();

    private ServerConfigManager() {
        this.configs = FXCollections.observableArrayList();
        this.configsFiltered = new FilteredList<>(this.configs);
    }

    /**
     * Returns the only instance of the {@link ServerConfigManager}. If no instance exists yet, a new one will be created and returned.
     * Implements synchronized functionality for multithreaded access.
     * @return instance {@link ServerConfigManager} instance
     */
    public static ServerConfigManager getInstance() {
        if (instance == null) {
            synchronized (instanceLock) {
                // Check again to see if another thread has instantiated the singleton class.
                if (instance == null) {
                    instance = new ServerConfigManager();
                }
            }
        }
        return instance;
    }

    public void addServerConfig(ServerConfig config) {
        this.configs.add(config);
    }

    public void removeServerConfig(ServerConfig config) {
        this.configs.remove(config);
    }

    public void setServerConfigListView(ListView<ServerConfig> serverConfigListView) {
        serverConfigListView.setItems(this.configsFiltered);
    }
}
