package data;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

/**
 * Allows to load contents from a {@link File} and to store data in a {@link File}.
 * @param <T> The type of data being moved between memory and drive.
 */
public interface FileManager<T> {
    /**
     * Loads the contents from the specified {@link File} into the returned value.
     * @param file The {@link File} from which data has to be read
     * @return storedValue
     * @throws IOException if the file could not be read from
     */
    @NotNull
    public abstract T load(@NotNull File file) throws IOException;

    /**
     * Stores the specified data in the specified {@link File}.
     * @param file The {@link File} to which data has to be written
     * @param data The data being stored in the file
     * @throws IOException if the file could not be written to
     */
    public abstract void store(@NotNull File file, @NotNull T data) throws IOException;
}
