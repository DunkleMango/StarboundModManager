package com.github.dunklemango.starboundmodmanager.connect;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads data from Steam and extracts usable information.
 */
public final class SteamWorkshopManager {
    private static final String WORKSHOP_URI = "https://api.steampowered.com/ISteamRemoteStorage/GetPublishedFileDetails/v1/";
    private static final String TITLE_NOT_FOUND = "[error: title not found]";
    private static final Logger logger = LogManager.getLogger("SteamWorkshopManager");

    private SteamWorkshopManager() {

    }

    public static List<String> getTitlesFromWorkshopIds(List<Integer> workshopIds) {
        List<String> titles = new ArrayList<>();
        workshopIds.forEach(id -> titles.add(TITLE_NOT_FOUND));
        try (CloseableHttpClient httpClient = initClient()) {
            for (int i = 0; i < titles.size(); i++) {
                titles.set(i, getTitleFromWorkshopId(httpClient, workshopIds.get(i)));
            }
        } finally {
            return titles;
        }
    }

    public static String getTitleFromWorkshopId(int workshopId) {
        String title = TITLE_NOT_FOUND;
        try (CloseableHttpClient httpClient = initClient()) {
            title = getTitleFromWorkshopId(httpClient, workshopId);
        } finally {
            return title;
        }
    }

    private static String getTitleFromWorkshopId(CloseableHttpClient httpClient, int workshopId) {
        String title = TITLE_NOT_FOUND;
        try {
            CloseableHttpResponse response = httpClient.execute(createRequestForWorkshopId(workshopId));

            HttpEntity entity = response.getEntity();
            JSONObject jsonObject = getDataJsonFromString(EntityUtils.toString(entity));
            title = jsonObject.getString("title");
            EntityUtils.consume(entity);

            response.close();
        } catch (IOException e) {
            logger.error("unable to complete HTTP-POST request", e);
        } catch (JSONException e) {
            logger.error("HTTP-POST response received but unable to convert to JSON", e);
        } finally {
            return title;
        }
    }

    private static JSONObject getDataJsonFromString(String jsonString) throws JSONException {
        final JSONObject jsonObject = new JSONObject(jsonString);
        final JSONObject jsonResponse = jsonObject.getJSONObject("response");
        final JSONArray publishedFileDetails = jsonResponse.getJSONArray("publishedfiledetails");
        return publishedFileDetails.getJSONObject(0);
    }

    private static CloseableHttpClient initClient() {
        return HttpClients.createDefault();
    }

    private static HttpPost createRequestForWorkshopId(int workshopId) throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost(WORKSHOP_URI);
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("itemcount", "1"));
        nvps.add(new BasicNameValuePair("publishedfileids[0]", String.valueOf(workshopId)));
        httpPost.setEntity(new UrlEncodedFormEntity(nvps));
        return httpPost;
    }
}
