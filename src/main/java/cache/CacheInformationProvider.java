package cache;

import data.convert.DataSizeConverter;
import data.file.FileLocationCoordinator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * Controls information related to the mod-cache file like file-size.
 * <p/>
 * The {@link CacheInformationProvider} gathers information from the file and used it to show statistics of the cache.
 */
public final class CacheInformationProvider {
    private static final Logger logger = LogManager.getLogger("CacheManager");
    private static final String TAG_DATA_USED = "used";
    private static final String TAG_DATA_FREE = "free";
    private static volatile CacheInformationProvider instance;
    private ObservableList<PieChart.Data> observableCacheData;
    private final long fileSizeLimit = DataSizeConverter.megaBytesToBytes(100); // Bytes
    private volatile long fileSize; // Bytes
    // Synchronization locks
    private static final Object instanceLock = new Object();

    private CacheInformationProvider() {
        observableCacheData = FXCollections.observableArrayList(
                new PieChart.Data(TAG_DATA_USED, 50),
                new PieChart.Data(TAG_DATA_FREE, 100)
        );
        loadCacheStatistics();
    }

    /**
     * Returns the only instance of the {@link CacheInformationProvider}. If no instance exists yet, a new one will be created and returned.
     * Implements synchronized functionality for multithreaded access.
     * @return instance {@link CacheInformationProvider} instance
     */
    public static CacheInformationProvider getInstance() {
        if (instance == null) {
            synchronized (instanceLock) {
                // Check again to see if another thread has instantiated the singleton class.
                if (instance == null) {
                    instance = new CacheInformationProvider();
                }
            }
        }
        return instance;
    }

    /**
     * Loads cached mod files from the storage on this computer.
     */
    public synchronized void loadCacheStatistics() {
        FileLocationCoordinator fileLocationCoordinator = FileLocationCoordinator.getInstance();
        try {
            final File cacheFile = fileLocationCoordinator.createAndGetCacheFile();
            updateCacheSize(cacheFile);
            updateStatistics();
            logger.debug("Updated cache-statistics.");
        } catch (IOException e) {
            logger.error("Failed to load mod-cache. Continuing without statistics.",e);
        }
    }

    /**
     * Updates the values of all cache-related statistics.
     */
    public synchronized void updateStatistics() {
        final long used = DataSizeConverter.bytesToKiloBytes(fileSize);
        final long free = DataSizeConverter.bytesToKiloBytes(fileSizeLimit) - used;
        this.observableCacheData.get(0).setPieValue(used);
        this.observableCacheData.get(1).setPieValue(free);
        logger.debug("cache-memory: used={}KB, free={}KB", used, free);
    }

    /**
     * Loads the current cache-statistics into the observable
     * @param cachePieChart The {@link PieChart} to update
     */
    public void linkStatistics(@NotNull PieChart cachePieChart) {
        cachePieChart.setData(this.observableCacheData);
        cachePieChart.setStartAngle(180);
        cachePieChart.setClockwise(false);
    }

    /**
     * Returns how much data is currently occupied by the cache file.
     * @return fileSize size of the cache file in Bytes
     */
    public long getFileSize() {
        return this.fileSize;
    }

    /**
     * Returns how much data can be occupied by the cache file at most.
     * @return fileSizeLimit maximum size of the cache file in Bytes
     */
    public long getFileSizeLimit() {
        return this.fileSizeLimit;
    }

    private synchronized void updateCacheSize(@NotNull File file) {
        this.fileSize = file.length();
        logger.debug("cache - occupied space: {} Bytes", this.fileSize);
    }
}
