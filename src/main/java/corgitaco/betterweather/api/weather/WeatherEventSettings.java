package corgitaco.betterweather.api.weather;

import net.minecraft.core.BlockPos;

public interface WeatherEventSettings {

    /**
     * @return The temperature modifier at the position.
     */
    double getTemperatureModifierAtPosition(BlockPos pos);

    /**
     * @return The humidity modifier at the position.
     */
    double getHumidityModifierAtPosition(BlockPos pos);
}
