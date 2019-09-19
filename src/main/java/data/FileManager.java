package data;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

/**
 * Loads 
 * @param <T>
 */
public abstract class FileManager<T> {
    public abstract T load(File file) throws IOException;
    public abstract void store(File file, T contents) throws IOException;
}
