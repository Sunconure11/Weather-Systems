package corgitaco.betterweather.helpers;

import corgitaco.betterweather.api.BiomeClimate;
import net.minecraft.core.BlockPos;

import java.util.function.Supplier;

public class BiomeHelper implements BiomeModifier, BiomeClimate {
    private Supplier<Float> weatherTempModifier = () -> 0.0F;
    private Supplier<Float> weatherHumidityModifier = () -> 0.0F;

    @Override
    public double getTemperatureModifier() {
        return weatherTempModifier.get();
    }

    @Override
    public double getWeatherTemperatureModifier(BlockPos pos) {
        return weatherTempModifier.get();
    }

    @Override
    public double getHumidityModifier() {
        return weatherHumidityModifier.get();
    }

    @Override
    public double getWeatherHumidityModifier(BlockPos pos) {
        return weatherHumidityModifier.get();
    }


    @Override
    public void setWeatherTempModifier(float tempModifier) {
        this.weatherTempModifier = () -> tempModifier;
    }

    @Override
    public void setWeatherHumidityModifier(float humidityModifier) {
        this.weatherHumidityModifier = () -> humidityModifier;
    }
}
