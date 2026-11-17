package com.spotihud.hud;

import com.spotihud.spotify.SpotifyApi;
import com.spotihud.spotify.SpotifyAuth;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.MinecraftClient;

public class SpotifyHudRenderer {
    private static final int x = 10;
    private static final int y = 10;
    private static long lastUpdate = 0;

    public static void render(DrawContext context) {
        if (System.currentTimeMillis() - lastUpdate > 3000) {
            SpotifyApi.updatePlaybackState();
            lastUpdate = System.currentTimeMillis();
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options.hudHidden) return;

        SpotifyApi.TrackInfo track = SpotifyApi.currentTrack;

        // Arka plan ve Spotify Yeşili Çerçeve
        context.fill(x, y, x + 160, y + 45, 0xAA000000);
        context.drawBorder(x, y, 160, 45, 0xFF1DB954);

        if (SpotifyAuth.getAccessToken().isEmpty()) {
            context.drawText(client.textRenderer, "[ Giriş İçin Tıkla! ]", x + 15, y + 18, 0xFF55FF55, true);
            return;
        }

        context.drawText(client.textRenderer, truncate(track.title, 22), x + 8, y + 7, 0xFFFFFFFF, true);
        context.drawText(client.textRenderer, truncate(track.artist, 25), x + 8, y + 19, 0xFFAAAAAA, true);

        String playPauseIcon = track.isPlaying ? "||" : ">";
        context.drawText(client.textRenderer, "<<", x + 12, y + 32, 0xFFFFFFFF, true);
        context.drawText(client.textRenderer, playPauseIcon, x + 48, y + 32, 0xFF1DB954, true);
        context.drawText(client.textRenderer, ">>", x + 82, y + 32, 0xFFFFFFFF, true);
    }

    public static boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= x && mouseX <= x + 160 && mouseY >= y && mouseY <= y + 45) {
            if (SpotifyAuth.getAccessToken().isEmpty()) {
                SpotifyAuth.startAuthFlow();
                return true;
            }

            if (mouseY >= y + 28 && mouseY <= y + 43) {
                if (mouseX >= x + 8 && mouseX <= x + 36) SpotifyApi.prevTrack();
                else if (mouseX >= x + 42 && mouseX <= x + 68) SpotifyApi.playPause();
                else if (mouseX >= x + 74 && mouseX <= x + 102) SpotifyApi.nextTrack();
            }
            return true;
        }
        return false;
    }

    private static String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}
