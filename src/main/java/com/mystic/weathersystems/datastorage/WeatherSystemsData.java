package com.mystic.weathersystems.datastorage;

import com.mystic.weathersystems.WeatherSystems;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.*;

public class WeatherSystemsData extends SavedData {
    public static String DATA_NAME = WeatherSystems.MOD_ID + ":weather_data";

    private boolean acidRain;
    private boolean blizzard;

    public WeatherSystemsData() {
        super(DATA_NAME);
    }

    public WeatherSystemsData(String s) {
        super(s);
    }

    @Override
    public void get(NbtCompound get) {
        setAcidRain(get.getBoolean("AcidRain"));
        setBlizzard(get.getBoolean("Blizzard"));
    }

    @Override
    public NbtCompound set(NbtCompound set) {
        set.putBoolean("AcidRain", acidRain);
        set.putBoolean("Blizzard", blizzard);
        return set;
    }

    public boolean isAcidRain() {
        return this.acidRain;
    }

    public boolean isBlizzard() {
        return this.blizzard;
    }

    public void setAcidRain(boolean acidRain) {
        this.acidRain = acidRain;
        this.set(new NbtCompound());
    }

    public void setBlizzard(boolean isBlizzard) {
        this.blizzard = isBlizzard;
        this.set(new NbtCompound());
    }

    public static WeatherSystemsData get(LevelAccessor world) {
        if (!(world instanceof ServerLevel))
            return new WeatherSystemsData();
        ServerLevel overWorld = ((ServerLevel) world).getLevel().getServer().getLevel(Level.OVERWORLD);
        DimensionDataStorage data = overWorld.getDataStorage();
       WeatherSystemsData weatherData = data.get(WeatherSystemsData::new, DATA_NAME);

        if (weatherData == null) {
            weatherData = new WeatherSystemsData();
            data.set(weatherData);
        }
        return weatherData;
    }
}