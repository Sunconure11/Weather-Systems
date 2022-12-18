package corgitaco.betterweather.api.weather;

import net.minecraft.sounds.SoundEvent;

public interface WeatherEventAudio {

    float getVolume();

    float getPitch();

    SoundEvent getSound();
}
