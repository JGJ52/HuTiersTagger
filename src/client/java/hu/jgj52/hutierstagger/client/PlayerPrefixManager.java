package hu.jgj52.hutierstagger.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerPrefixManager {

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();
    private static final ConcurrentHashMap<String, String> prefixMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Boolean> retiredMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, String> nowGamemode = new ConcurrentHashMap<>();

    public static void fetchPlayer(String playerName) {
        new Thread(() -> {
            try {
                prefixMap.putIfAbsent(playerName, "");
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI("https://api.hutiers.hu/v2/player/" + playerName))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                JsonArray arr = gson.fromJson(response.body(), JsonArray.class);
                if (arr.size() > 0) {
                    JsonObject prefixObj = arr.get(0).getAsJsonObject();
                    JsonObject retiredObj = arr.get(1).getAsJsonObject();

                    String gamemodeKey = HutierstaggerClient.getInstance().getGamemode();
                    String gamemode = "";

                    if (prefixObj.has(gamemodeKey) && !prefixObj.get(gamemodeKey).getAsString().isEmpty()) {
                        gamemode = prefixObj.get(gamemodeKey).getAsString();
                        nowGamemode.put(playerName, "");
                    } else {
                        Map<String, Integer> list = new HashMap<>();
                        for (var entry : prefixObj.entrySet()) {
                            String tier = entry.getValue().getAsString();
                            int value = switch (tier) {
                                case "LT5" -> 1;
                                case "HT5" -> 2;
                                case "LT4" -> 3;
                                case "HT4" -> 4;
                                case "LT3" -> 5;
                                case "HT3" -> 6;
                                case "LT2" -> 7;
                                case "HT2" -> 8;
                                case "LT1" -> 9;
                                case "HT1" -> 10;
                                default -> 0;
                            };
                            list.put(entry.getKey(), value);
                        }
                        int max = list.values().stream().max(Integer::compare).orElse(0);
                        if (max == 0) {
                            gamemode = "";
                            nowGamemode.put(playerName, "");
                        } else {
                            for (Map.Entry<String, Integer> entry : list.entrySet()) {
                                if (entry.getValue() == max) {
                                    gamemode = prefixObj.get(entry.getKey()).getAsString();
                                    nowGamemode.put(playerName, entry.getKey());
                                }
                            }
                        }
                    }

                    prefixMap.put(playerName, gamemode);

                    Boolean retired = retiredObj.has(gamemodeKey) && retiredObj.get(gamemodeKey).getAsBoolean();
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

    public static String getNowGamemode(String playerName) {
        return nowGamemode.getOrDefault(playerName, null);
    }
}
