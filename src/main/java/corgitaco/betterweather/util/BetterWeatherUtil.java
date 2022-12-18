package corgitaco.betterweather.util;

import com.mojang.math.Vector3d;
import com.mojang.serialization.Codec;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.api.BetterWeatherRegistry;
import corgitaco.betterweather.api.weather.WeatherEvent;
import corgitaco.betterweather.util.client.ColorUtil;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;

import java.util.*;

@SuppressWarnings("deprecation")
public class BetterWeatherUtil {

    public static boolean filterRegistryID(ResourceLocation id, Registry<?> registry, String registryTypeName) {
        if (registry.keySet().contains(id))
            return true;
        else {
            if (!id.toString().contains("modid:dummymob")) {
                BetterWeather.LOGGER.error("\"" + id.toString() + "\" was not a registryID in the " + registryTypeName + "! Skipping entry...");
            }
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public static <I, O> O unsafeCast(I obj) {
        return (O) obj;
    }


    public static int modifiedColorValue(int original, int target, double blendStrength) {
        return (int) Mth.lerp(blendStrength, original, target);
    }

    public static int transformFloatColor(Vector3d floatColor) {
        return ColorUtil.pack((int) (floatColor.x * 255), (int) (floatColor.y * 255), (int) (floatColor.z * 255));
    }

    public static IdentityHashMap<Block, Block> transformBlockBlockResourceLocations(Map<ResourceLocation, ResourceLocation> blockBlockMap) {
        IdentityHashMap<Block, Block> newMap = new IdentityHashMap<>();
        blockBlockMap.forEach((resourceLocation, resourceLocation2) -> {
            if (Registry.BLOCK.keySet().contains(resourceLocation) && Registry.BLOCK.keySet().contains(resourceLocation2)) {
                newMap.put(Registry.BLOCK.get(resourceLocation), Registry.BLOCK.get(resourceLocation2));
            } else {
                BetterWeather.LOGGER.error("The value: \"" + resourceLocation.toString() + "\" is not a valid block ID...");
            }
        });
        return newMap;
    }

    public static TreeMap<ResourceLocation, ResourceLocation> transformBlockBlocksToResourceLocations(IdentityHashMap<Block, Block> blockBlockMap) {
        TreeMap<ResourceLocation, ResourceLocation> newMap = new TreeMap<>(Comparator.comparing(ResourceLocation::toString));
        blockBlockMap.forEach((resourceLocation, resourceLocation2) -> {
            newMap.put(Registry.BLOCK.getKey(resourceLocation), Registry.BLOCK.getKey(resourceLocation2));
        });
        return newMap;
    }

    public static ReferenceArraySet<ResourceKey<Codec<? extends WeatherEvent>>> transformWeatherLocationsToKeys(Collection<ResourceLocation> blockResourceLocationToCropGrowthMultiplierMap) {
        ReferenceArraySet<ResourceKey<Codec<? extends WeatherEvent>>> newMap = new ReferenceArraySet<>();
        blockResourceLocationToCropGrowthMultiplierMap.forEach((resourceLocation) -> {
            newMap.add(ResourceKey.create(BetterWeatherRegistry.WEATHER_EVENT_KEY, resourceLocation));
        });
        return newMap;
    }
}