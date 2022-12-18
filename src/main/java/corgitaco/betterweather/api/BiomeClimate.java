package corgitaco.betterweather.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;

/**
 * Safely castable to or extenders of {@link net.minecraft.world.level.biome.Biome}
 * Used to acquire any new climate data provided by Better Weather for the given biome.
 */
public interface BiomeClimate {

    /**
     * @return temperature modifier for the current weather event and/or season.
     */
    double getTemperatureModifier();

    double getWeatherTemperatureModifier(BlockPos pos);

    /**
     * @return temperature modifier for the current weather event and/or season.
     */
    double getHumidityModifier();

    double getWeatherHumidityModifier(BlockPos pos);

    static BiomeClimate getClimate(Biome biome) {
        return ((BiomeClimate)(Object) biome);
    }
}