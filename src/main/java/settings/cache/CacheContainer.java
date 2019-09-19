package settings.cache;

import data.CacheFileManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Manages the caching of mods from the Steam-API. Updates values if requested in the Controller or its subroutines.
 * <p/>
 * The {@link CacheContainer} first loads the cache file into memory. If the cache has not been created yet, or some mods
 * are missing, it will load the information from the Steam-API and add them to memory. When closing, the
 * {@link CacheContainer} saves changes into the cache file, if it does not exist, it will be created.<br>
 * Generally uses the information given to show statistics of the cache and to provide encapsulated data to other
 * classes for further processing.
 */
public final class CacheContainer {
    private static final Logger logger = LogManager.getLogger("CacheManager");
    private static final String TAG_DATA_USED = "used";
    private static final String TAG_DATA_FREE = "free";
    private static volatile CacheContainer instance;
    private ObservableList<PieChart.Data> observableCacheData = FXCollections.observableArrayList(
            new PieChart.Data(TAG_DATA_USED, 80),
            new PieChart.Data(TAG_DATA_FREE, 120)
    );
    // Synchronization locks
    private static final Object instanceLock = new Object();

    private CacheContainer() {

    }

    /**
     * Returns the only instance of the CacheManager. If no instance exists yet, a new one will be created and returned.
     * Implements synchronized functionality for multithreaded access.
     * @return instance CacheManager instance
     */
    public static synchronized CacheContainer getInstance() {
        if (instance == null) {
            synchronized (instanceLock) {
                // Check again to see if another thread has instantiated the singleton class.
                if (instance == null) {
                    instance = new CacheContainer();
                }
            }
        }
        return instance;
    }

    public void load(File cacheFile) {
        CacheFileManager cfm = new CacheFileManager();
        try {
            List<JSONObject> jsonObjects = cfm.load(cacheFile);
        } catch (IOException e) {
            logger.error("Unable to load mod-cache file.",e);
        }
        //TODO add conversion of JSONs
    }

    /**
     * Loads the current cache-statistics into the observable
     * @param cachePieChart The {@link PieChart} to update
     */
    public void linkStatistics(PieChart cachePieChart) {
        cachePieChart.setData(this.observableCacheData);
        cachePieChart.setStartAngle(180);
        cachePieChart.setClockwise(false);
    }
}
