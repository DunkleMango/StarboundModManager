package data.mod;

import cache.CacheInformationProvider;
import com.sun.javafx.collections.ObservableListWrapper;
import data.file.FileLocationCoordinator;
import data.file.storage.ModDataFileManager;
import data.mod.exception.ModLoadingException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import settings.AppSettingsCoordinator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the loading and storing of mods from the Steam-API. Updates values if requested in the Controller or its subroutines.
 * <p/>
 * The {@link ModDataManager} first loads the mods into memory. If the cache has not been created yet, or some mods
 * are missing, it will load the information from the Steam-API and add them to memory. When closing, the
 * {@link ModDataManager} saves changes into the cache file, if it does not exist, it will be created.<br>
 * Provides encapsulated data to other classes for further processing.
 */
public final class ModDataManager {
    private static final Logger logger = LogManager.getLogger("ModDataManager");
    private static volatile ModDataManager instance;
    private ObservableList<ModData> mods;
    // Synchronization locks
    private static final Object instanceLock = new Object();

    private ModDataManager() {
        this.mods = FXCollections.observableArrayList();
    }

    /**
     * Returns the only instance of the {@link ModDataManager}. If no instance exists yet, a new one will be created and returned.
     * Implements synchronized functionality for multithreaded access.
     * @return instance {@link ModDataManager} instance
     */
    public static ModDataManager getInstance() {
        if (instance == null) {
            synchronized (instanceLock) {
                // Check again to see if another thread has instantiated the singleton class.
                if (instance == null) {
                    instance = new ModDataManager();
                }
            }
        }
        return instance;
    }

    private void sortMods() {
        this.mods.sort(new ModComparator());
    }

    public void getMods() {
        loadModsFromCache();
        loadModsFromWorkshopDirectory();
        this.mods.forEach(modData -> {
            logger.debug("Loaded mod: id={}, title=\"{}\"", modData.getId(), modData.getTitle());
        });
    }

    private void loadModsFromWorkshopDirectory() {
        AppSettingsCoordinator appSettingsCoordinator = AppSettingsCoordinator.getInstance();
        final File workshopDirectory = appSettingsCoordinator.getWorkshopDirectory();
        final File[] workshopFiles = workshopDirectory.listFiles(File::isDirectory);
        if (workshopFiles != null && workshopFiles.length > 0) {
            for (File dir: workshopFiles) {
                logger.debug("Workshop-mod found: {}", dir.getName());
                addModFromWorkshop(dir);
                sortMods();
            }
            logger.debug("Mods: {}", this.mods);
        } else {
            logger.debug("No workshop-mods found.");
        }
    }

    private void addModFromWorkshop(File dir) {
        String idStr = dir.getName();
        long id = Long.parseLong(idStr);
        if (this.mods.stream().anyMatch(modData -> modData.getId() == id)) {
            logger.debug("Mod already in memory.");
            return;
        }
        try {
            this.mods.add(new ModData(id));
        } catch (ModLoadingException e) {
            logger.error("Failed to load mod: {}", id, e);
        }
    }

    private void loadModsFromCache() {
        CacheInformationProvider cacheInformationProvider = CacheInformationProvider.getInstance();
        try {
            File cacheFile = FileLocationCoordinator.getInstance().createAndGetCacheFile();
            ModDataFileManager fileManager = new ModDataFileManager();
            this.mods.addAll(fileManager.load(cacheFile));
            cacheInformationProvider.loadCacheStatistics();
            sortMods();
            logger.debug("Successfully loaded mods.");
        } catch (IOException e) {
            logger.error("Failed to load mods. Continuing without mods.",e);
        }
    }

    public void clearModsAndSavedData() {
        this.mods.clear();
        saveModsToCache();
        loadModsFromCache();
        getMods();
    }

    public void saveModsToCache() {
        try {
            FileLocationCoordinator dlc = FileLocationCoordinator.getInstance();
            final File cacheFile = dlc.createAndGetCacheFile();
            ModDataFileManager fileManager = new ModDataFileManager();
            fileManager.store(cacheFile, this.mods);
            logger.debug("Successfully stored mods.");
        } catch (IOException e) {
            logger.debug("Failed to store mods. Discarding.", e);
        }
    }

    public void setWorkshopModsListView(ListView<ModData> workshopModsListView) {
        workshopModsListView.setItems(this.mods);
    }
}
