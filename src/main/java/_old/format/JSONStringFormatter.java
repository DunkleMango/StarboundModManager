package _old.format;

import org.json.JSONObject;

public final class JSONStringFormatter {
    private static final int JSON_RENDER_DEPTH = 8;

    private JSONStringFormatter() {

    }

    public static String formatJson(JSONObject jsonObject) {
        String str = jsonObject.toString();
        return formatJsonString(str);
    }

    public static String formatJsonString(String jsonString) {
        return jsonString.substring(0, JSON_RENDER_DEPTH - 1) + " " + jsonString.substring(jsonString.length() - JSON_RENDER_DEPTH);
    }
}
