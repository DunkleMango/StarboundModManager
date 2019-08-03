package com.github.dunklemango.starboundmodmanager.storage;

import com.github.dunklemango.starboundmodmanager.format.JSONStringFormatter;
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

public class WorkshopCacheManager {
    private static final Logger logger = LogManager.getLogger("WorkshopCacheManager");
    private static final String FILE_PATH = FileManager.DIR_PATH + "\\workshopCache.json";
    private static final String PARSE_KEY = "key";
    private static final String PARSE_VALUE = "value";
    private static WorkshopCacheManager instance;
    private boolean directoriesCreated;
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

    public Integer getIdFromTitle(String title) {
        Set<Map.Entry<Integer, WorkshopItem>> entrySet = this.data.entrySet();
        for (Map.Entry<Integer, WorkshopItem> entry: entrySet) {
            if (entry.getValue().getTitle().contentEquals(title)) {
                return entry.getKey();
            }
        }
        return -1;
    }

    public List<String> getTitlesFromIds (List<Integer> ids) {
        List<String> titles = new ArrayList<>();
        ids.forEach(id -> titles.add(get(id).getTitle()));
        return titles;
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
                logger.debug("String to data: mapEntryJson #{}: {}", i, JSONStringFormatter.formatJson(mapEntryJson));
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
                logger.debug("data to String: workshopItem: {}",
                        JSONStringFormatter.formatJson(workshopItem.toJsonObject()));
                logger.debug("data to String: mapEntryJson: {}",
                        JSONStringFormatter.formatJson(mapEntryJson));
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
        return new ArrayList<>(this.data.keySet());
    }

    public boolean containsKey(Integer id) {
        return this.data.containsKey(id);
    }

}
