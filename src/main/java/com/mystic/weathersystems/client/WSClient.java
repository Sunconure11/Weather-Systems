package com.mystic.weathersystems.client;

import com.mystic.weathersystems.WeatherSystems;
import com.mystic.weathersystems.weatherevents.AcidRain;
import com.mystic.weathersystems.weatherevents.Blizzard;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.resources.ResourceLocation;

public class WSClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientTickEvents.START_CLIENT_TICK.register(WSClient::clientTickEvent);

        ClientSidePacketRegistry.INSTANCE.register(WeatherSystems.WS_WEATHER_PACKET,
                (packetContext, attachedData) -> {
                    boolean readBlizzard = attachedData.readBoolean();
                    boolean readAcidRain = attachedData.readBoolean();

                    packetContext.getTaskQueue().execute(() -> {
                        WeatherSystems.WeatherSystemsEvents.weatherData.setBlizzard(readBlizzard);
                        WeatherSystems.WeatherSystemsEvents.weatherData.setAcidRain(readAcidRain);


                    });
                });
    }

    public static final ResourceLocation RAIN_TEXTURE = new ResourceLocation("textures/environment/rain.png");
    public static final ResourceLocation ACID_RAIN_TEXTURE = new ResourceLocation(WeatherSystems.MOD_ID, "textures/environment/acid_rain.png");

    static int idx = 0;

    public static void clientTickEvent(Minecraft minecraft) {
        if (minecraft.level != null) {
            WeatherSystems.WeatherSystemsEvents.setWeatherData(minecraft.level);
            if (minecraft.level.getLevelData().isRaining() && WeatherSystems.WeatherSystemsEvents.weatherData.isAcidRain()) {

                if (WeatherSystems.WS_CONFIG.acid_rain.client.smokeParticles)
                    AcidRain.addAcidRainParticles(minecraft.gameRenderer.getMainCamera(), minecraft, minecraft.levelRenderer);

                if (LevelRenderer.RAIN_LOCATION != ACID_RAIN_TEXTURE && WeatherSystems.WeatherSystemsEvents.weatherData.isAcidRain())
                    LevelRenderer.RAIN_LOCATION = ACID_RAIN_TEXTURE;
                else if (LevelRenderer.RAIN_LOCATION != RAIN_TEXTURE && !WeatherSystems.WeatherSystemsEvents.weatherData.isAcidRain())
                    LevelRenderer.RAIN_LOCATION = RAIN_TEXTURE;
            }

            if (minecraft.level.getLevelData().isRaining() && WeatherSystems.WeatherSystemsEvents.weatherData.isBlizzard()) {
                minecraft.levelRenderer.lastViewDistance = WeatherSystems.WS_CONFIG.blizzard.client.forcedBlizzardRenderDistance;
                idx = 0;
            }
            if (minecraft.levelRenderer.lastViewDistance != minecraft.options.renderDistance && !WeatherSystems.WeatherSystemsEvents.weatherData.isBlizzard() && idx == 0) {
                minecraft.levelRenderer.lastViewDistance = minecraft.options.renderDistance;
                idx++;
            }
            Blizzard.blizzardSoundHandler(minecraft, minecraft.gameRenderer.getMainCamera());
        }
    }

}