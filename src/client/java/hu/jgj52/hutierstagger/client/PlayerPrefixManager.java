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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerPrefixManager {

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();
    private static final ConcurrentHashMap<UUID, String> prefixMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Boolean> retiredMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, String> nowGamemode = new ConcurrentHashMap<>();

    public static void fetchPlayer(UUID player) {
        new Thread(() -> {
            try {
                prefixMap.putIfAbsent(player, "");
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI("https://api.hutiers.hu/v3/player/" + player))
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
                        nowGamemode.put(player, "");
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
                            nowGamemode.put(player, "");
                        } else {
                            for (Map.Entry<String, Integer> entry : list.entrySet()) {
                                if (entry.getValue() == max) {
                                    gamemode = prefixObj.get(entry.getKey()).getAsString();
                                    nowGamemode.put(player, entry.getKey());
                                }
                            }
                        }
                    }

                    prefixMap.put(player, gamemode);

                    Boolean retired = retiredObj.has(gamemodeKey) && retiredObj.get(gamemodeKey).getAsBoolean();
                    retiredMap.put(player, retired);

                } else {
                    prefixMap.put(player, "");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static String getPrefix(UUID player) {
        return prefixMap.getOrDefault(player, null);
    }

    public static Boolean getRetired(UUID player) {
        return retiredMap.getOrDefault(player, false);
    }

    public static void resetMap() {
        prefixMap.clear();
    }

    public static String getNowGamemode(UUID player) {
        return nowGamemode.getOrDefault(player, null);
    }

    public static void removePlayer(UUID player) {
        prefixMap.remove(player);
        retiredMap.remove(player);
        nowGamemode.remove(player);
    }
}
