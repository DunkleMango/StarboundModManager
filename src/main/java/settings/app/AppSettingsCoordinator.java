package settings.app;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Properties;

/**
 * The {@link AppSettingsCoordinator} provides synchronized access to all application wide settings.
 * <p/>
 * The settings are stored in a volatile {@link Properties} object, so that every thread gets the latest values.
 * Meanwhile write-access to the properties is handled with synchronization on lock objects, with each key-value-pair
 * having their own lock.
 */
public final class AppSettingsCoordinator {
    private static final String KEY_PATH_TO_STEAM = "pathToSteam";
    private static volatile AppSettingsCoordinator instance;
    private volatile Properties settings;
    private File settingsFile; //TODO create dataLocationCoordinator for all stored data
    // Synchronization locks
    private static final Object instanceLock = new Object();
    private final Object steamPathLock = new Object();

    private AppSettingsCoordinator() {
        //TODO Load properties first
        settings = new Properties();
    }

    /**
     * Returns the only instance of the {@link AppSettingsCoordinator}. If no instance exists yet, a new one will be created and returned.
     * Implements synchronized functionality for multithreaded access.
     * @return instance {@link AppSettingsCoordinator} instance
     */
    public static synchronized AppSettingsCoordinator getInstance() {
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
    @Contract(pure = true, value = " -> null")
    public File getSteamDirectory() {
        return new File(this.settings.getProperty(KEY_PATH_TO_STEAM, "C:/"));
    }

}
