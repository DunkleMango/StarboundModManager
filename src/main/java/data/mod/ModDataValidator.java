package data.mod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public final class ModDataValidator {
    private static final Logger logger = LogManager.getLogger("ModDataValidator");

    private ModDataValidator() {

    }

    public static boolean isModDataInvalid(JSONObject objectModData) {
        if (!objectModData.has(ModData.KEY_ID)) return true;
        if (!objectModData.has(ModData.KEY_PREVIEW_IMAGE_URL)) return true;
        if (!objectModData.has(ModData.KEY_TITLE)) return true;
        if (!objectModData.has(ModData.KEY_TAGS)) return true;

        return false;
    }

    public static boolean isModDataValid(JSONObject objectModData, long modId) {
        if (isModDataInvalid(objectModData)) return false;
        try {
            final long id = objectModData.getLong(ModData.KEY_ID);
            return id == modId;
        } catch (JSONException e) {
            logger.error("Mod associated JSONObject was malformed. -> not valid");
            return false;
        }
    }
}
