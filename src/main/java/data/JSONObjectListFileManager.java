package data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Allows to load contents from a {@link File} and to store data in a {@link File}.
 * The type of data being moved between memory and drive is a {@link List} of {@link JSONObject}s.
 */
public class JSONObjectListFileManager implements FileManager<List<JSONObject>> {
    private static final Logger logger = LogManager.getLogger("CacheFileManager");

    /**
     * Loads the contents from the specified {@link File} into the returned value.
     *
     * @param file The {@link File} from which data has to be read
     * @return list The {@link List} of {@link JSONObject}s being stored in the file
     * @throws IOException if the file could not be read from
     */
    @NotNull
    @Override
    public List<JSONObject> load(@NotNull File file) throws IOException {
        if (!file.exists()) return new ArrayList<>();
        FileInputStream fileInputStream = new FileInputStream(file);
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
        try {
            Object parsedObject = objectInputStream.readObject();
            if (!(parsedObject instanceof List)) throw new ClassCastException("Not instance of List");
            List<?> objectList = (List<?>) parsedObject;
            List<JSONObject> jsonObjects = new ArrayList<>();
            objectList.forEach(obj -> {
                if (obj instanceof JSONObject) {
                    jsonObjects.add((JSONObject) obj);
                }
            });
            objectInputStream.close();
            fileInputStream.close();
            return jsonObjects;
        } catch (ClassNotFoundException | ClassCastException e) {
            logger.error("Unable to read JSONObject file.", e);
            return new ArrayList<>();
        }
    }

    /**
     * Stores the specified data in the specified {@link File}.
     *
     * @param file The {@link File} to which data has to be written
     * @param data The {@link List} of {@link JSONObject}s being stored in the file
     * @throws IOException if the file could not be written to
     */
    @Override
    public void store(@NotNull File file, @NotNull List<JSONObject> data) throws IOException{
        if (!file.exists()) Files.createFile(file.toPath());
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(data);
    }
}
