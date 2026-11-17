package com.spotihud.spotify;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class SpotifyApi {
    private static final HttpClient client = HttpClient.newHttpClient();
    public static TrackInfo currentTrack = new TrackInfo("Spoti Hud [ Mobile Ready ]", "Giriş Bekleniyor...", false);

    public static void updatePlaybackState() {
        String token = SpotifyAuth.getAccessToken();
        if (token.isEmpty()) return;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.spotify.com/v1/me/player/currently-playing"))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 && !response.body().isEmpty()) {
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                boolean isPlaying = json.get("is_playing").getAsBoolean();
                JsonObject item = json.getAsJsonObject("item");
                
                String trackName = item.get("name").getAsString();
                String artistName = item.getAsJsonArray("artists").get(0).getAsJsonObject().get("name").getAsString();
                
                currentTrack = new TrackInfo(trackName, artistName, isPlaying);
            } else {
                currentTrack = new TrackInfo("Müzik Çalmıyor", "Spotify açık değil", false);
            }
        } catch (Exception e) {
            currentTrack = new TrackInfo("Bağlantı Hatası", "Yeniden deneniyor...", false);
        }
    }

    public static void sendCommand(String endpoint) {
        String token = SpotifyAuth.getAccessToken();
        if (token.isEmpty()) return;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.spotify.com/v1/me/player/" + endpoint))
                    .header("Authorization", "Bearer " + token)
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();
            client.sendAsync(request, HttpResponse.BodyHandlers.discarding());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void playPause() {
        if (currentTrack.isPlaying) sendCommand("pause");
        else sendCommand("play");
    }

    public static void nextTrack() { sendCommand("next"); }
    public static void prevTrack() { sendCommand("previous"); }

    public static class TrackInfo {
        public String title;
        public String artist;
        public boolean isPlaying;

        public TrackInfo(String title, String artist, boolean isPlaying) {
            this.title = title;
            this.artist = artist;
            this.isPlaying = isPlaying;
        }
    }
}
