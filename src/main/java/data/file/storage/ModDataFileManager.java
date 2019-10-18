package data.file.storage;

import data.mod.ModData;
import data.mod.exception.ModLoadingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Allows to load contents from a {@link File} and to store data in a {@link File}.
 * The type of data being moved between memory and drive is a {@link java.util.Collection} of
 * {@link data.mod.ModData} items.
 */
public class ModDataFileManager implements FileManager<Collection<ModData>> {
    private static final Logger logger = LogManager.getLogger("ModDataFileManager");
    private List<JSONObject> writableJsonObjects = new ArrayList<>();

    /**
     * Loads the contents from the specified {@link File} into the returned value.
     *
     * @param file The {@link File} from which data has to be read
     * @return list The {@link java.util.Collection} of {@link data.mod.ModData} being loaded from the file
     * @throws IOException if the file could not be read from
     */
    @NotNull
    @Override
    public Collection<ModData> load(@NotNull File file) throws IOException {
        if (!file.exists()) return new ArrayList<>();
        List<ModData> list = new ArrayList<>();
        JSONArray jsonArray;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            JSONTokener jsonTokener = new JSONTokener(bufferedReader);
            jsonArray = new JSONArray(jsonTokener);
        } catch (JSONException e) {
            logger.error("Failed to read JSON file. Using empty JSONArray instead.", e);
            return new ArrayList<>();
        }
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject data = jsonArray.optJSONObject(i);
            if (data != null) {
                try {
                    list.add(new ModData(data));
                } catch (ModLoadingException e) {
                    logger.error("Failed to load mod from file. Skipping.", e);
                }
            }
        }
        return list;
    }

    /**
     * Stores the specified data in the specified {@link File}.
     *
     * @param file The {@link File} to which data has to be written
     * @param data The {@link java.util.Collection} of {@link data.mod.ModData} being stored in the file
     * @throws IOException if the file could not be written to
     */
    @Override
    public void store(@NotNull File file, @NotNull Collection<ModData> data) throws IOException{
        if (!file.exists()) Files.createFile(file.toPath());
        data.forEach(modData -> modData.addToFileManager(this));
        logger.debug("list: {}", writableJsonObjects);
        JSONArray jsonArray = new JSONArray(writableJsonObjects);
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
            jsonArray.write(bufferedWriter);
        } catch (JSONException e) {
            logger.error("Failed to write JSON file. Discarding JSONArray.", e);
        }
    }

    public void addToData(JSONObject jsonObject) {
        writableJsonObjects.add(jsonObject);
    }
}
