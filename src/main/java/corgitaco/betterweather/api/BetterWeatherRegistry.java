package corgitaco.betterweather.api;

import com.mojang.serialization.Codec;
import corgitaco.betterweather.api.weather.WeatherEvent;
import corgitaco.betterweather.api.weather.WeatherEventClientSettings;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

import static corgitaco.betterweather.BetterWeather.MOD_ID;

public class BetterWeatherRegistry {
    // TODO: Make this a registry similar to those of world gen registries.
    public static final Map<ResourceLocation, WeatherEvent> DEFAULT_EVENTS = new HashMap<>();

    public static final ResourceKey<Registry<Codec<? extends WeatherEvent>>> WEATHER_EVENT_KEY = ResourceKey.createRegistryKey(new ResourceLocation(MOD_ID, "weather_event"));

    public static final ResourceKey<Registry<Codec<? extends WeatherEventClientSettings>>> CLIENT_WEATHER_EVENT_KEY = ResourceKey.createRegistryKey(new ResourceLocation(MOD_ID, "weather_event_client"));

    public static final Registry<Codec<? extends WeatherEvent>> WEATHER_EVENT = Registry.registerSimple(WEATHER_EVENT_KEY, p_236015_ -> WeatherEvent.CODEC);

    public static final Registry<Codec<? extends WeatherEventClientSettings>> CLIENT_WEATHER_EVENT_SETTINGS = Registry.registerSimple(CLIENT_WEATHER_EVENT_KEY, p_236015_ -> WeatherEventClientSettings.CODEC);

    public static Map<String, WeatherEvent> getWeather() {
        HashMap<String, WeatherEvent> weatherEventHashMap = new HashMap<>();
        for (ResourceLocation rl : DEFAULT_EVENTS.keySet().stream().toList()) {
            weatherEventHashMap.put(rl.getPath(), DEFAULT_EVENTS.get(rl));
        }
        return weatherEventHashMap;
    }
}
