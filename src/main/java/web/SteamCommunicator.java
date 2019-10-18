package web;

import data.mod.ModDataValidator;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 *  Handles communication with the SteamAPI.
 *  </p>
 *  s
 */
public final class SteamCommunicator implements Closeable {
    private static final Logger logger = LogManager.getLogger("SteamCommunicator");
    private static final String URL_MOD_REQUEST = "https://api.steampowered.com/ISteamRemoteStorage/GetPublishedFileDetails/v1/";
    private CloseableHttpClient httpClient;

    public SteamCommunicator() {
        this.httpClient = HttpClients.createDefault();
    }

    public JSONObject postRequestMod(long modId) throws HTTPModRequestException {
        HttpPost httpPost = new HttpPost(URL_MOD_REQUEST);
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("itemcount", "1"));
        nvps.add(new BasicNameValuePair("publishedfileids[0]", String.valueOf(modId)));
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nvps));
            try (CloseableHttpResponse httpResponse = this.httpClient.execute(httpPost)) {
                HttpEntity entity = httpResponse.getEntity();
                if (entity != null) {
                    InputStream in = entity.getContent();
                    JSONTokener jsonTokener = new JSONTokener(new InputStreamReader(in));
                    JSONObject jsonObject = new JSONObject(jsonTokener);
                    if (isModJSONValid(jsonObject, modId)) {
                        final JSONObject objectResponse = jsonObject.getJSONObject("response");
                        final JSONArray arrayPublishedfiledetails = objectResponse.getJSONArray("publishedfiledetails");
                        return arrayPublishedfiledetails.getJSONObject(0);
                    } else {
                        throw new HTTPModRequestException(HTTPModRequestException.ERROR_TYPE.UNKNOWN_MOD_ID,
                                "Mod-id: " + modId + " is unknown to the SteamAPI.");
                    }
                }
            } catch (JSONException e) {
                logger.error("Failed to convert server-data to JSON for mod-id: {}", modId, e);
                throw new HTTPModRequestException(HTTPModRequestException.ERROR_TYPE.BAD_REQUEST,
                        "JSON conversion of server-data from Steam failed for mod-id: " + modId);
            }
        } catch (IOException e) {
            logger.error("Failed to complete POST-Request for mod-id: {}", modId, e);
            throw new HTTPModRequestException(HTTPModRequestException.ERROR_TYPE.BAD_REQUEST,
                    "POST-Request failed for mod-id: " + modId);
        }
        throw new HTTPModRequestException(HTTPModRequestException.ERROR_TYPE.BAD_REQUEST,
                "An unknown error occurred. The request for mod-id: " + modId + " was interrupted.");
    }

    private boolean isModJSONValid(@NotNull JSONObject jsonObject, long modId) {
        try {
            if (!jsonObject.has("response")) return false;
            final JSONObject objectResponse = jsonObject.getJSONObject("response");
            if (!objectResponse.has("publishedfiledetails")) return false;
            final JSONArray arrayPublishedfiledetails = objectResponse.getJSONArray("publishedfiledetails");
            if (arrayPublishedfiledetails.length() < 1) return false;
            final JSONObject objectModData = arrayPublishedfiledetails.getJSONObject(0);
            return ModDataValidator.isModDataValid(objectModData, modId);
        } catch (JSONException e) {
            logger.error("Mod associated JSONObject was malformed. -> not valid");
            return false;
        }
    }

    /**
     * Closes this stream and releases any system resources associated
     * with it. If the stream is already closed then invoking this
     * method has no effect.
     *
     * <p> As noted in {@link AutoCloseable#close()}, cases where the
     * close may fail require careful attention. It is strongly advised
     * to relinquish the underlying resources and to internally
     * <em>mark</em> the {@code Closeable} as closed, prior to throwing
     * the {@code IOException}.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        this.httpClient.close();
    }
}
