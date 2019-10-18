package web;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.fail;

class SteamCommunicatorTest {

    @Test
    void testCorrectModRequest() throws IOException {
        try (SteamCommunicator steamCommunicator = new SteamCommunicator()) {
            JSONObject jsonObject = steamCommunicator.postRequestMod(1264107917);
            System.out.println(jsonObject);
        } catch (HTTPModRequestException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void testMalformedJSONModRequest() {
        try (SteamCommunicator steamCommunicator = new SteamCommunicator()) {
            JSONObject jsonObject = steamCommunicator.postRequestMod(1);
            System.out.println(jsonObject);
            fail();
        } catch (HTTPModRequestException | IOException ignored) { }
    }
}