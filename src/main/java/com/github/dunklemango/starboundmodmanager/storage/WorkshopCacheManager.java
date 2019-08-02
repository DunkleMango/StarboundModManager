package com.github.dunklemango.starboundmodmanager.storage;

import com.github.dunklemango.starboundmodmanager.workshop.WorkshopItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class WorkshopCacheManager {
    private static final Logger logger = LogManager.getLogger("SettingsManager");
    private static final String FILE_PATH = FileManager.DIR_PATH + "\\workshopCache.json";
    private static final String PARSE_KEY = "key";
    private static final String PARSE_VALUE = "value";
    private static WorkshopCacheManager instance;
    private boolean directoriesCreated = false;
    private Map<Integer, WorkshopItem> data = new HashMap<>();

    private WorkshopCacheManager() {
        directoriesCreated = FileManager.createDirectories(FileManager.DIR_PATH);
        FileManager.createFileIfNotExisting(FILE_PATH);
        loadData();
    }

    public static WorkshopCacheManager getInstance() {
        if (instance == null) {
            instance = new WorkshopCacheManager();
        }
        return instance;
    }

    public void loadData() {
        Path path = new File(FILE_PATH).toPath();
        if (directoriesCreated && Files.exists(path)) {
            try {
                String content = Files.readString(path);
                if (content != null && !content.isBlank()) {
                    parseStringToData(content);
                }
            } catch (IOException e) {
                logger.error("{} could not be located.", FILE_PATH);
            }
        }
    }

    private void parseStringToData(String str) {
        try {
            JSONArray jsonArray = new JSONArray(str);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject mapEntryJson = jsonArray.getJSONObject(i);
                logger.debug("String to data: mapEntryJson #{}: {}", i, mapEntryJson);
                this.data.put(mapEntryJson.getInt(PARSE_KEY),
                        new WorkshopItem(mapEntryJson.getJSONObject(PARSE_VALUE)));
            }
        } catch (JSONException e) {
            logger.error("unable to convert JSON to data-map item", e);
        }
    }

    private JSONArray mapToJson() {
        JSONArray jsonArray = new JSONArray();
        this.data.forEach((id, workshopItem) -> {
            try {
                JSONObject mapEntryJson = new JSONObject();
                mapEntryJson.put(PARSE_KEY, id);
                mapEntryJson.put(PARSE_VALUE, workshopItem.toJsonObject());
                logger.debug("data to String: workshopItem: {}", workshopItem.toJsonObject());
                logger.debug("data to String: mapEntryJson: {}", mapEntryJson);
                jsonArray.put(mapEntryJson);
            } catch (JSONException e) {
                logger.error("unable to convert item from data-map to JSON", e);
            }
        });
        return jsonArray;
    }

    public void saveData() {
        if (directoriesCreated) {
            try {
                Files.writeString(new File(FILE_PATH).toPath(), mapToJson().toString());
            } catch (IOException e) {
                logger.error("{} could not be located.", FILE_PATH);
            }
        }
    }

    public WorkshopItem get(Integer id) {
        return this.data.get(id);
    }

    public WorkshopItem put(Integer id, WorkshopItem item) {
        return this.data.put(id, item);
    }

    public void putAll(List<WorkshopItem> workshopItems) {
        workshopItems.forEach(item -> this.data.put(item.getId(), item));
    }

    public List<Integer> getCachedIds() {
        return new ArrayList<Integer>(this.data.keySet());
    }

    public boolean containsKey(Integer id) {
        return this.data.containsKey(id);
    }

}
