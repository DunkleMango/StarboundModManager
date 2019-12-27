package data.mod;

import cache.CacheInformationProvider;
import data.file.FileLocationCoordinator;
import data.file.storage.ModDataFileManager;
import data.mod.exception.ModLoadingException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.ListView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import settings.AppSettingsCoordinator;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.function.Predicate;

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
    private FilteredList<ModData> modsWorkshopFiltered;
    private FilteredList<ModData> modsServerFiltered;
    // Synchronization locks
    private static final Object instanceLock = new Object();

    private ModDataManager() {
        this.modsCached = new HashMap<>();
        this.modsWorkshop = FXCollections.observableArrayList();
        this.modsServer = FXCollections.observableArrayList();
        this.modsWorkshopFiltered = new FilteredList<>(this.modsWorkshop);
        this.modsServerFiltered = new FilteredList<>(this.modsServer);
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

    public void setModFilter(Predicate<? super ModData> predicate) {
        this.modsWorkshopFiltered.setPredicate(predicate);
        this.modsServerFiltered.setPredicate(predicate);
    }

    private void sortMods(ObservableList<ModData> target) {
        target.sort(new ModComparator());
    }

    public void getMods() {
        loadModsFromCache();
        reloadMods();
        this.modsCached.values().forEach(modData -> {
            logger.debug("Loaded mod: id={}, title=\"{}\"", modData.getId(), modData.getTitle());
        });
    }

    public void reloadMods() {
        this.modsWorkshop.clear();
        this.modsServer.clear();
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
                sortMods(modsServer);
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
                sortMods(modsWorkshop);
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
            logger.debug("Successfully loaded mods.");
        } catch (IOException e) {
            logger.error("Failed to load mods. Continuing without mods.",e);
        }
    }

    public void clearModsAndSavedData() {
        this.modsCached.clear();
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

    public void updateServerModsData() {
        if (this.modsServer.size() == 0) return;
        for (ModData mod: this.modsServer) {
            try {
                boolean successful = mod.copyToServer();
                logger.info("Copying file {}({}) was {}successful", mod.getId(), mod.getTitle(), successful ? "" : "not ");
            } catch (IOException e) {
                logger.error("Unable to copy \"{}\" mod to server, skipping to next..", mod.getId(), e);
            }
        }
        logger.info("Copied mods from workshop to server: {}", this.modsServer);
        reloadMods();
    }

    public void setWorkshopModsListView(ListView<ModData> workshopModsListView) {
        workshopModsListView.setItems(this.modsWorkshopFiltered);
    }

    public void setServerModsListView(ListView<ModData> serverModsListView) {
        serverModsListView.setItems(this.modsServerFiltered);
    }
}
