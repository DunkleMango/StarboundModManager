package _old.storage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

public final class FileManager {
    public static final String SEPARATOR = System.getProperty("file.separator");
    public static final String DIR_PATH = new File(System.getProperty("user.home")
            + SEPARATOR + "StarboundModManager" + SEPARATOR).getAbsolutePath();
    private static final Logger logger = LogManager.getLogger("FileManager");

    private FileManager() {

    }

    public static boolean createDirectories(String dirPath) {
        File file = new File(dirPath);
        boolean directoriesCreated = false;
        if (!file.exists()) {
            directoriesCreated = file.mkdirs();
        } else {
            directoriesCreated = true;
        }
        if (!directoriesCreated) {
            logger.error("Directory {} could not be created.", dirPath);
        }
        return directoriesCreated;
    }

    public static void createFileIfNotExisting(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                logger.error("File {} could not be created.", filePath);
            }
        }
    }
}
