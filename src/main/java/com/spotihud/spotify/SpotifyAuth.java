package com.spotihud.spotify;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Util;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SpotifyAuth {
    private static final String CLIENT_ID = "BURAYA_SPOTIFY_DEVELOPER_CLIENT_ID";
    private static final String CLIENT_SECRET = "BURAYA_SPOTIFY_DEVELOPER_CLIENT_SECRET";
    private static final int PORT = 8888;
    private static final String REDIRECT_URI = "http://localhost:" + PORT + "/callback";

    private static String accessToken = "";
    private static HttpServer server;

    public static void startAuthFlow() {
        try {
            if (server != null) server.stop(0);
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext("/callback", new CallbackHandler());
            server.setExecutor(null);
            server.start();

            String authUrl = "https://accounts.spotify.com/authorize?client_id=" + CLIENT_ID +
                    "&response_type=code&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8) +
                    "&scope=user-read-playback-state%20user-modify-playback-state%20user-read-currently-playing";

            Util.getOperatingSystem().open(URI.create(authUrl));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class CallbackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String code = null;
            if (query != null) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair[0].equals("code") && pair.length > 1) {
                        code = pair[1];
                        break;
                    }
                }
            }

            String responseMessage;
            if (code != null) {
                exchangeTokens(code);
                responseMessage = "<html><body><h2>Giris Basarili! Oyuna donebilirsin.</h2></body></html>";
            } else {
                responseMessage = "<html><body><h2>Giris Basarisiz.</h2></body></html>";
            }

            exchange.sendResponseHeaders(200, responseMessage.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseMessage.getBytes());
            os.close();
            server.stop(0);
        }
    }

    private static void exchangeTokens(String code) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String body = "grant_type=authorization_code" +
                    "&code=" + code +
                    "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8);

            String authHeader = "Basic " + Base64.getEncoder().encodeToString((CLIENT_ID + ":" + CLIENT_SECRET).getBytes(StandardCharsets.UTF_8));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://accounts.spotify.com/api/token"))
                    .header("Authorization", authHeader)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                accessToken = json.get("access_token").getAsString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getAccessToken() {
        return accessToken;
    }
}
