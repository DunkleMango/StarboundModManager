package com.github.dunklemango.starboundmodmanager.workshop;

import com.github.dunklemango.starboundmodmanager.exceptions.WorkshopItemDataNotLoadedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class WorkshopItem {
    private static final Logger logger = LogManager.getLogger("WorkshopItem");
    private static final String TITLE_NOT_FOUND = "[error: title not found]";
    private static final String PARSE_ID = "id";
    private static final String PARSE_DATA = "data";
    private final Integer id;
    private JSONObject data;
    private boolean isDataLoaded;

    public WorkshopItem(Integer id) {
        this.id = id;
        this.isDataLoaded = false;
        this.data = new JSONObject();
    }

    public WorkshopItem(JSONObject data) {
        this.isDataLoaded = false;
        Integer tmpId = -1;
        try {
            tmpId = data.getInt(PARSE_ID);
            this.data = data.getJSONObject(PARSE_DATA);
        } catch (JSONException e) {
            logger.error("unable to create JSON for parsing WorkshopItem in constructor", e);
            this.data = new JSONObject();
        } finally {
            this.id = tmpId;
        }
    }

    public WorkshopItem(String serializedWorkshopItem) {
        this.isDataLoaded = false;
        Integer tmpId = -1;
        try {
            JSONObject obj = new JSONObject(serializedWorkshopItem);
            tmpId = obj.getInt(PARSE_ID);
            this.data = obj.getJSONObject(PARSE_DATA);

            this.isDataLoaded = true;
        } catch (JSONException e) {
            logger.error("unable to create JSON for parsing WorkshopItem in constructor", e);
            this.data = new JSONObject();
        } finally {
            this.id = tmpId;
        }
    }

    public static void main(String[] args) {
        WorkshopItem item = new WorkshopItem(29571295);
        String serializedItem = item.toString();
        logger.debug("item serialization: {}", serializedItem);
        WorkshopItem deserializedItem = new WorkshopItem(serializedItem);
        logger.debug("deserializedItem.id = {}", deserializedItem.id);
        logger.debug("deserializedItem.data = {}", deserializedItem.data);
    }

    public JSONObject toJsonObject() {
        JSONObject obj = new JSONObject();
        try {
            obj.put(PARSE_ID, id);
            obj.put(PARSE_DATA, data);
            return obj;
        } catch (JSONException e) {
            logger.error("unable to create JSON for parsing WorkshopItem in toString() method", e);
            return null;
        }
    }

    @Override
    public String toString() {
        JSONObject obj = toJsonObject();
        if (obj == null) {
            return null;
        } else {
            return obj.toString();
        }
    }

    public String getTitle() {
        String title = TITLE_NOT_FOUND;
        try {
            title = data.getString("title");
        } catch (NullPointerException | JSONException e) {
            logger.error("unable to retrieve item \"title\" from JSON");
        } finally {
            return title;
        }
    }

    public Integer getId() {
        return this.id;
    }

    protected JSONObject getData() throws WorkshopItemDataNotLoadedException {
        if (!isDataLoaded) throw new WorkshopItemDataNotLoadedException();
        return this.data;
    }

    protected void setData(JSONObject data) {
        this.data = data;
        isDataLoaded = (data != null);
    }
}
