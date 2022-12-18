package corgitaco.betterweather.weather.event;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.betterweather.api.weather.WeatherEvent;
import corgitaco.betterweather.api.weather.WeatherEventClientSettings;
import corgitaco.betterweather.util.TomlCommentedConfigOps;
import corgitaco.betterweather.weather.event.client.settings.CloudyClientSettings;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;

public class Cloudy extends WeatherEvent {

    public static final Codec<Cloudy> CODEC = RecordCodecBuilder.create((builder) -> {
        return builder.group(WeatherEventClientSettings.CODEC.fieldOf("clientSettings").forGetter((rain) -> {
            return rain.getClientSettings();
        }), Codec.STRING.fieldOf("biomeCondition").forGetter(rain -> {
            return rain.getBiomeCondition();
        }), Codec.DOUBLE.fieldOf("defaultChance").forGetter(rain -> {
            return rain.getDefaultChance();
        }), Codec.DOUBLE.fieldOf("temperatureOffset").forGetter(rain -> {
            return rain.getTemperatureOffsetRaw();
        }), Codec.DOUBLE.fieldOf("humidityOffset").forGetter(rain -> {
            return rain.getHumidityOffsetRaw();
        }), Codec.BOOL.fieldOf("isThundering").forGetter(rain -> {
            return rain.isThundering();
        }), Codec.INT.fieldOf("lightningChance").forGetter(rain -> {
            return rain.getLightningChance();
        })).apply(builder, Cloudy::new);
    });

    public static final Cloudy DEFAULT = new Cloudy(new CloudyClientSettings(Rain.RAIN_COLORS, 0.0F, -1.0F, true), "ALL", 0.7D, !MODIFY_TEMPERATURE ? 0.0 : -0.05, 0.07, false, 0);

    public static final Cloudy DEFAULT_THUNDERING = new Cloudy(new CloudyClientSettings(Rain.THUNDER_COLORS, 0.0F, -0.09F, true), "ALL", 0.1D, !MODIFY_TEMPERATURE ? 0.0 :-0.05, 0.07, true, 100000);

    public static final TomlCommentedConfigOps CONFIG_OPS = new TomlCommentedConfigOps(Util.make(new HashMap<>(WeatherEvent.VALUE_COMMENTS), (map) -> {
    }), true);


    public Cloudy(WeatherEventClientSettings clientSettings, String biomeCondition, double defaultChance, double temperatureOffsetRaw, double humidityOffsetRaw, boolean isThundering, int lightningFrequency) {
        super(clientSettings, biomeCondition, defaultChance, temperatureOffsetRaw, humidityOffsetRaw, isThundering, lightningFrequency);
    }

    @Override
    public void worldTick(ServerLevel world, int tickSpeed, long worldTime) {
    }

    @Override
    public Codec<? extends WeatherEvent> codec() {
        return CODEC;
    }

    @Override
    public DynamicOps<?> configOps() {
        return CONFIG_OPS;
    }

    @Override
    public double getTemperatureModifierAtPosition(BlockPos pos) {
        return getTemperatureOffsetRaw();
    }

    @Override
    public double getHumidityModifierAtPosition(BlockPos pos) {
        return getHumidityOffsetRaw();
    }
}
