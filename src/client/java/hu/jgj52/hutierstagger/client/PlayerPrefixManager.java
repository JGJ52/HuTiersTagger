package hu.jgj52.hutierstagger.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerPrefixManager {

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();
    private static final ConcurrentHashMap<String, String> prefixMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Boolean> retiredMap = new ConcurrentHashMap<>();

    public static void fetchPlayer(String playerName) {
        new Thread(() -> {
            try {
                prefixMap.put(playerName, "");
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI("https://api.hutiers.hu/v1/player/" + playerName))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                JsonArray arr = gson.fromJson(response.body(), JsonArray.class);
                if (arr.size() > 0) {
                    JsonObject prefixObj = arr.get(0).getAsJsonObject();
                    JsonObject retiredObj = arr.get(1).getAsJsonObject();

                    String gamemodeKey = HutierstaggerClient.getInstance().getGamemode().toLowerCase();
                    String gamemode = prefixObj.has(gamemodeKey) ? prefixObj.get(gamemodeKey).getAsString() : "";
                    prefixMap.put(playerName, gamemode);

                    Boolean retired = retiredObj.has(gamemodeKey) ? retiredObj.get(gamemodeKey).getAsBoolean() : false;
                    retiredMap.put(playerName, retired);

                } else {
                    prefixMap.put(playerName, "");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static String getPrefix(String playerName) {
        return prefixMap.getOrDefault(playerName, null);
    }

    public static Boolean getRetired(String playerName) {
        return retiredMap.getOrDefault(playerName, false);
    }

    public static void resetMap() {
        prefixMap.clear();
    }
}
