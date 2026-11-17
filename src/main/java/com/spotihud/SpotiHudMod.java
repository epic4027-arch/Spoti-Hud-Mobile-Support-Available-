package com.spotihud;

import com.spotihud.hud.SpotifyHudRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class SpotiHudMod implements ClientModInitializer {
    public static final String MOD_ID = "spoti_hud";

    @Override
    public void onInitializeClient() {
        // 1.21+ RenderTickCounter uyumlu HUD kayıt yapısı
        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            SpotifyHudRenderer.render(drawContext);
        });
    }
}
