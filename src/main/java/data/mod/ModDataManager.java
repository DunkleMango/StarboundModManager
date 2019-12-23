package data.mod;

import cache.CacheInformationProvider;
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
import java.util.Collection;
import java.util.HashMap;

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
    private HashMap<Long, ModData> modsCached;
    private ObservableList<ModData> modsWorkshop;
    private ObservableList<ModData> modsServer;
    // Synchronization locks
    private static final Object instanceLock = new Object();

    private ModDataManager() {
        this.modsCached = new HashMap<>();
        this.modsWorkshop = FXCollections.observableArrayList();
        this.modsServer = FXCollections.observableArrayList();
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
        this.modsWorkshop.sort(new ModComparator());
        this.modsServer.sort(new ModComparator());
    }

    public void getMods() {
        loadModsFromCache();
        updateView();
        this.modsCached.values().forEach(modData -> {
            logger.debug("Loaded mod: id={}, title=\"{}\"", modData.getId(), modData.getTitle());
        });
    }

    public void updateView() {
        loadModsFromWorkshopDirectory();
        loadModsFromServerDirectory();
    }

    private void loadModsFromServerDirectory() {
        AppSettingsCoordinator appSettingsCoordinator = AppSettingsCoordinator.getInstance();
        final File serverDirectory = appSettingsCoordinator.getServerDirectory();
        final File[] serverFiles = serverDirectory.listFiles((dir, name) -> name.endsWith(ModData.FILE_EXTENSION));
        if (serverFiles != null && serverFiles.length > 0) {
            for (File dir: serverFiles) {
                logger.debug("Server-mod found: {}", dir.getName());
                addModFromServer(dir);
                sortMods(); //TODO only sort one
            }
            logger.debug("Server mods: {}", this.modsServer);
        } else {
            logger.debug("No server-mods found.");
        }
    }

    private void loadModsFromWorkshopDirectory() {
        AppSettingsCoordinator appSettingsCoordinator = AppSettingsCoordinator.getInstance();
        final File workshopDirectory = appSettingsCoordinator.getWorkshopDirectory();
        final File[] workshopFiles = workshopDirectory.listFiles(File::isDirectory);
        if (workshopFiles != null && workshopFiles.length > 0) {
            for (File dir: workshopFiles) {
                logger.debug("Workshop-mod found: {}", dir.getName());
                addModFromWorkshop(dir);
                sortMods(); //TODO only sort one
            }
            logger.debug("Workshop mods: {}", this.modsWorkshop);
        } else {
            logger.debug("No workshop-mods found.");
        }
    }

    private void addModFromServer(File dir) {
        String dirName = dir.getName();
        String idStr = dirName.substring(0, dirName.length() - ModData.FILE_EXTENSION.length());
        long id = Long.parseLong(idStr);
        try {
            if (this.modsCached.containsKey(id)) {
                logger.debug("Mod already in memory.");
            } else {
                this.modsCached.put(id, new ModData(id));
            }
        } catch (ModLoadingException e) {
            logger.error("Failed to load mod: {}", id, e);
        } finally {
            this.modsServer.add(modsCached.get(id));
        }
    }

    private void addModFromWorkshop(File dir) {
        String idStr = dir.getName();
        long id = Long.parseLong(idStr);
        try {
            if (this.modsCached.containsKey(id)) {
                logger.debug("Mod already in memory.");
            } else {
                this.modsCached.put(id, new ModData(id));
            }
        } catch (ModLoadingException e) {
            logger.error("Failed to load mod: {}", id, e);
        } finally {
            this.modsWorkshop.add(modsCached.get(id));
        }
    }

    private void loadModsFromCache() {
        CacheInformationProvider cacheInformationProvider = CacheInformationProvider.getInstance();
        try {
            File cacheFile = FileLocationCoordinator.getInstance().createAndGetCacheFile();
            ModDataFileManager fileManager = new ModDataFileManager();
            Collection<ModData> modsLoaded = fileManager.load(cacheFile);
            for (ModData mod: modsLoaded) {
                this.modsCached.put(mod.getId(), mod);
            }
            cacheInformationProvider.loadCacheStatistics();
            sortMods();
            logger.debug("Successfully loaded mods.");
        } catch (IOException e) {
            logger.error("Failed to load mods. Continuing without mods.",e);
        }
    }

    public void clearModsAndSavedData() {
        this.modsCached.clear();
        this.modsWorkshop.clear();
        this.modsServer.clear();
        saveModsToCache();
        getMods();
    }

    public void saveModsToCache() {
        try {
            FileLocationCoordinator dlc = FileLocationCoordinator.getInstance();
            final File cacheFile = dlc.createAndGetCacheFile();
            ModDataFileManager fileManager = new ModDataFileManager();
            fileManager.store(cacheFile, this.modsCached.values());
            logger.debug("Successfully stored mods.");
        } catch (IOException e) {
            logger.debug("Failed to store mods. Discarding.", e);
        }
    }

    public void setWorkshopModsListView(ListView<ModData> workshopModsListView) {
        workshopModsListView.setItems(this.modsWorkshop);
    }

    public void setServerModsListView(ListView<ModData> serverModsListView) {
        serverModsListView.setItems(this.modsServer);
    }
}
