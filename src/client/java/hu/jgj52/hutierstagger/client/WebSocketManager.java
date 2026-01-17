package hu.jgj52.hutierstagger.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public class WebSocketManager implements WebSocket.Listener {

    private WebSocket webSocket;

    public void conncect(String url) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            webSocket = client.newWebSocketBuilder()
                    .buildAsync(URI.create(url), this)
                    .join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        String message = data.toString();
        if (message.startsWith("update")) {
            String player = message.split(" ")[1];
            PlayerPrefixManager.removePlayer(UUID.fromString(player));
        }
        return WebSocket.Listener.super.onText(webSocket, data, last);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
    }
}
