package corgitaco.betterweather.weather.event.client;

import corgitaco.betterweather.api.client.WeatherEventClient;
import corgitaco.betterweather.api.client.graphics.Graphics;
import corgitaco.betterweather.weather.event.client.settings.NoneClientSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.biome.Biome;

import java.util.function.Predicate;

public class NoneClient extends WeatherEventClient<NoneClientSettings> {
    public NoneClient(NoneClientSettings clientSettings) {
        super(clientSettings);
    }

    @Override
    public void renderWeatherShaders(Graphics graphics, ClientLevel world, double x, double y, double z) {
    }

    @Override
    public void clientTick(ClientLevel world, int tickSpeed, long worldTime, Minecraft mc, Predicate<Biome> biomePredicate) {

    }
}
