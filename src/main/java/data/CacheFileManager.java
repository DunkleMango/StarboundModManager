package data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Allows to load and store a list of {@link JSONObject}s to a file.
 */
public class CacheFileManager extends FileManager<List<JSONObject>> {
    private static final Logger logger = LogManager.getLogger("CacheFileManager");

    @Override
    public List<JSONObject> load(File file) throws IOException {
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

    @Override
    public void store(File file, List<JSONObject> contents) throws IOException{
        if (!file.exists()) Files.createFile(file.toPath());
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(contents);
    }
}
