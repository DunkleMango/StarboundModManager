package settings;

import data.file.FileLocationCoordinator;
import data.file.storage.PropertiesFileManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * The {@link AppSettingsCoordinator} provides synchronized access to all application wide settings.
 * <p/>
 * The settings are stored in a volatile {@link Properties} object, so that every thread gets the
 * latest values. Meanwhile write-access to the properties is handled with synchronization on lock
 * objects, with each key-value-pair having their own lock.
 */
public final class AppSettingsCoordinator {
    private static final Logger logger = LogManager.getLogger("AppSettingsCoordinator");
    private static final String KEY_PATH_TO_STEAM = "pathToSteam";
    private static volatile AppSettingsCoordinator instance;
    private volatile Properties settings;
    // Synchronization locks
    private static final Object instanceLock = new Object();
    private final Object steamPathLock = new Object();

    private AppSettingsCoordinator() {
        loadSettings();
    }

    /**
     * Returns the only instance of the {@link AppSettingsCoordinator}. If no instance exists yet,
     * a new one will be created and returned. Implements synchronized functionality for
     * multithreaded access.
     * @return instance {@link AppSettingsCoordinator} instance
     */
    public static AppSettingsCoordinator getInstance() {
        if (instance == null) {
            synchronized (instanceLock) {
                // Check again to see if another thread has instantiated the singleton class.
                if (instance == null) {
                    instance = new AppSettingsCoordinator();
                }
            }
        }
        return instance;
    }

    /**
     * Loads the settings {@link Properties}. If an error occurred, the settings are set to default.
     */
    public synchronized void loadSettings() {
        FileLocationCoordinator dlc = FileLocationCoordinator.getInstance();
        try {
            final File settingsFile = dlc.createAndGetSettingsFile();
            PropertiesFileManager pfm = new PropertiesFileManager();
            this.settings = pfm.load(settingsFile);
        } catch (IOException e) {
            logger.error("Failed to load settings file. Continuing with default settings.", e);
            settings = new Properties();
        }
    }

    /**
     * Saves the settings {@link Properties}. If an error occurred, the settings are discarded.
     */
    public synchronized void saveSettings() {
        FileLocationCoordinator dlc = FileLocationCoordinator.getInstance();
        try {
            final File settingsFile = dlc.createAndGetSettingsFile();
            PropertiesFileManager pfm = new PropertiesFileManager();
            pfm.store(settingsFile, this.settings);
        } catch (IOException e) {
            logger.error("Failed to save settings file.", e);
        }
    }

    /**
     * Sets the Steam-directory to the absolute path of the specified file.
     * @param dir The {@link File} directory
     */
    public void setSteamDirectory(@NotNull File dir) {
        synchronized (steamPathLock) {
            this.settings.setProperty(KEY_PATH_TO_STEAM, dir.getAbsolutePath());
        }
    }

    /**
     * Returns the Steam-directory as a {@link File}.
     * @return dir The {@link File} directory
     */
    @NotNull
    @Contract(pure = true)
    public File getSteamDirectory() {
        return new File(this.settings.getProperty(KEY_PATH_TO_STEAM, "C:/"));
    }

    public File getWorkshopDirectory() {
        return FileLocationCoordinator.getInstance().getFile(getSteamDirectory().getAbsolutePath(),
                "steamapps", "workshop", "content", "211820");
    }

    public File getServerDirectory() {
        return FileLocationCoordinator.getInstance().getFile(getSteamDirectory().getAbsolutePath(),
                "steamapps", "common", "Starbound", "mods");
    }
}
