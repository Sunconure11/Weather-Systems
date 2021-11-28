package com.mystic.weathersystems.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mystic.weathersystems.WeatherSystems;
import com.mystic.weathersystems.weatherevents.Blizzard;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public class MixinFogRenderer {

    private static int idx2 = 0;

    private static final Minecraft minecraft = Minecraft.getInstance();


    @Inject(at = @At("HEAD"), method = "setupFog(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/FogRenderer$FogMode;FZ)V", cancellable = true)
    private static void blizzardFogDensity(Camera camera, FogRenderer.FogMode fogMode, float f, boolean bl, CallbackInfo ci) {
        if (WeatherSystems.WS_CONFIG.blizzard.client.blizzardFog) {
            if (minecraft.level != null && minecraft.player != null) {
                WeatherSystems.WeatherSystemsEvents.setWeatherData(minecraft.level);
                BlockPos playerPos = new BlockPos(minecraft.player.position());
                if (WeatherSystems.WeatherSystemsEvents.weatherData.isBlizzard() && minecraft.level.getLevelData().isRaining() && Blizzard.doBlizzardsAffectDeserts(minecraft.level.getBiome(playerPos))) {
                    RenderSystem.fogDensity((float) WeatherSystems.WS_CONFIG.blizzard.client.blizzardFogDensity);
                    ci.cancel();
                    if (idx2 != 0)
                        idx2 = 0;
                } else {
                    if (idx2 == 0) {
                        idx2++;
                    }
                }
            }
        }
    }
}
