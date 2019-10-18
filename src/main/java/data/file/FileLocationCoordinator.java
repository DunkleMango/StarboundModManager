package data.file;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Coordinates access to the filesystem. Controls the paths for specific files.
 */
public final class FileLocationCoordinator {
    private static volatile FileLocationCoordinator instance;
    private static final String FILE_ENDING_PROPERTIES = ".properties";
    private static final String FILE_ENDING_JSON = ".json";
    private static final String HOME_DIR_STR = System.getProperty("user.home");
    private static final String PROJECT_ID_STR = "StarboundModManager";
    private static final String SETTINGS_FILE_STR = "settings";
    private static final String CACHE_FILE_STR = "cache";
    // Synchronization locks
    private static final Object instanceLock = new Object();

    private FileLocationCoordinator() {

    }

    /**
     * Returns the only instance of the {@link FileLocationCoordinator}. If no instance exists yet, a new one will be created and returned.
     * Implements synchronized functionality for multithreaded access.
     * @return instance {@link FileLocationCoordinator} instance
     */
    public static FileLocationCoordinator getInstance() {
        if (instance == null) {
            synchronized (instanceLock) {
                // Check again to see if another thread has instantiated the singleton class.
                if (instance == null) {
                    instance = new FileLocationCoordinator();
                }
            }
        }
        return instance;
    }

    /**
     * Returns the file, where the settings for the application are stored.
     * Guarantees that the {@link File} exists. If it previously did not, it will be created.
     * It will never override existing files.
     * @return file The {@link File}, where the settings for the application are stored
     */
    @NotNull
    public File createAndGetSettingsFile() throws IOException {
        return createAndGetFile(HOME_DIR_STR, PROJECT_ID_STR, SETTINGS_FILE_STR + FILE_ENDING_PROPERTIES);
    }

    /**
     * Returns the file, where the cache for the application are stored.
     * Guarantees that the {@link File} exists. If it previously did not, it will be created.
     * It will never override existing files.
     * @return file The {@link File}, where the cache for the application are stored
     */
    @NotNull
    public File createAndGetCacheFile() throws IOException {
        return createAndGetFile(HOME_DIR_STR, PROJECT_ID_STR, CACHE_FILE_STR + FILE_ENDING_JSON);
    }

    private File createAndGetFile(String basePath, String...pathSteps) throws IOException {
        File file = getFile(basePath, pathSteps);
        createFile(file);
        return file;
    }

    public File getFile(String basePath, String...pathSteps) {
        return Paths.get(basePath, pathSteps).toFile();
    }

    private boolean createFile(File file) throws IOException {
        // Check and possibly create directories
        File parent = file.getParentFile();
        boolean mkdirs = false;
        if (!parent.exists()) {
            mkdirs = parent.mkdirs();
            if (!mkdirs) throw new IOException("Unable to create all necessary directories.");
        }
        // Check and possibly create file
        boolean newFile = false;
        if (mkdirs) {
            newFile = file.createNewFile();
        }
        return newFile;
    }

    private boolean deleteFile(File file) {
        return file.delete();
    }

}
