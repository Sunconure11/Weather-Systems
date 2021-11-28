package com.mystic.weathersystems.datastorage;

import com.mystic.weathersystems.WeatherSystems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

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
    public void load(CompoundTag nbt) {
        setAcidRain(nbt.getBoolean("AcidRain"));
        setBlizzard(nbt.getBoolean("Blizzard"));
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        compound.putBoolean("AcidRain", acidRain);
        compound.putBoolean("Blizzard", blizzard);
        return compound;
    }

    public boolean isAcidRain() {
        return this.acidRain;
    }

    public boolean isBlizzard() {
        return this.blizzard;
    }

    public void setAcidRain(boolean acidRain) {
        this.acidRain = acidRain;
        setDirty();
    }

    public void setBlizzard(boolean isBlizzard) {
        this.blizzard = isBlizzard;
        setDirty();
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