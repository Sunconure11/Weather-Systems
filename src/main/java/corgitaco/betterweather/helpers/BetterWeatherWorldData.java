package corgitaco.betterweather.helpers;

import corgitaco.betterweather.weather.BWWeatherEventContext;

import javax.annotation.Nullable;

public interface BetterWeatherWorldData {

    @Nullable
    BWWeatherEventContext getWeatherEventContext();

    @Nullable
    BWWeatherEventContext setWeatherEventContext(BWWeatherEventContext weatherEventContext);
}
