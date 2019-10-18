package data.file.storage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Allows to load contents from a {@link File} and to store data in a {@link File}.
 * The type of data being moved between memory and drive is a {@link Properties} object.
 */
public class PropertiesFileManager implements FileManager<Properties> {
    private static final Logger logger = LogManager.getLogger("PropertiesFileManager");

    /**
     * Loads the contents from the specified {@link File} into the returned value.
     *
     * @param file The {@link File} from which data has to be read
     * @return properties The {@link Properties} being stored in the file
     * @throws IOException if the file could not be read from
     */
    @NotNull
    @Override
    public Properties load(@NotNull File file) throws IOException {
        Properties properties = new Properties();
        FileInputStream fileInputStream = new FileInputStream(file);
        try {
            properties.load(fileInputStream);
        } catch (IllegalArgumentException e) {
            logger.error("Input file contains malformed characters.", e);
            return new Properties();
        }
        return properties;
    }

    /**
     * Stores the specified data in the specified {@link File}.
     *
     * @param file The {@link File} to which data has to be written
     * @param data The {@link Properties} being stored in the file
     * @throws IOException if the file could not be written to
     */
    @Override
    public void store(@NotNull File file, @NotNull Properties data) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        data.store(fileOutputStream, null);
    }
}
