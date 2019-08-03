package com.github.dunklemango.starboundmodmanager.workshop;

import com.github.dunklemango.starboundmodmanager.format.JSONStringFormatter;
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
public final class WorkshopItemManager {
    private static final String WORKSHOP_URI = "https://api.steampowered.com/ISteamRemoteStorage/GetPublishedFileDetails/v1/";
    private static final Logger logger = LogManager.getLogger("WorkshopItemManager");

    private WorkshopItemManager() {

    }

    public static void loadWorkshopDataFromSteam(List<WorkshopItem> workshopItems) {
        try (CloseableHttpClient httpClient = initClient()) {
            workshopItems.forEach(workshopItem -> {
                logger.debug("retrieving data from server for workshopItem with id: {}", workshopItem.getId());
                workshopItem.setData(loadWorkshopDataFromSteam(httpClient, workshopItem.getId()));
                logger.debug("retrieved data for workshopItem: {}",
                        JSONStringFormatter.formatJson(workshopItem.toJsonObject()));
            });
        } finally {
            return;
        }
    }

    public static void loadWorkshopDataFromSteam(WorkshopItem workshopItem) {
        try (CloseableHttpClient httpClient = initClient()) {
            workshopItem.setData(loadWorkshopDataFromSteam(httpClient, workshopItem.getId()));
        } finally {
            return;
        }
    }

    private static JSONObject loadWorkshopDataFromSteam(CloseableHttpClient httpClient, int workshopId) {
        JSONObject data = null;
        try {
            CloseableHttpResponse response = httpClient.execute(createRequestForWorkshopId(workshopId));
            HttpEntity entity = response.getEntity();

            data = getDataJsonFromString(EntityUtils.toString(entity));

            EntityUtils.consume(entity);
            response.close();
        } catch (IOException e) {
            logger.error("unable to complete HTTP-POST request", e);
        } catch (JSONException e) {
            logger.error("HTTP-POST response received but unable to convert to JSON", e);
        } finally {
            return data;
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
