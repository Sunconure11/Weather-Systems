package com.mystic.weathersystems;

import com.google.common.collect.Lists;
import com.mystic.weathersystems.config.WeatherSystemsConfig;
import com.mystic.weathersystems.datastorage.WeatherSystemsData;
import com.mystic.weathersystems.server.WeatherSystemsCommand;
import com.mystic.weathersystems.weatherevents.AcidRain;
import com.mystic.weathersystems.weatherevents.Blizzard;
import io.netty.buffer.Unpooled;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class WeatherSystems implements ModInitializer {
    public static Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "weathersystems";
    public static WeatherSystemsConfig WS_CONFIG;


    public static final ResourceLocation WS_WEATHER_PACKET = new ResourceLocation(MOD_ID, "blizzard_update");
    public static final ResourceLocation ACID_RAIN_PACKET = new ResourceLocation(MOD_ID, "acid_rain_update");

    static boolean damageAnimals = false;
    static boolean damageMonsters = false;
    static boolean damagePlayer = false;

    public static boolean destroyGrass = false;
    public static boolean destroyLeaves = false;
    public static boolean destroyCrops = false;
    public static boolean destroyPlants = false;

    public static List<Block> blocksToNotDestroyList = new ArrayList<>();

    public void configReader() {
        AutoConfig.register(WeatherSystemsConfig.class, JanksonConfigSerializer::new);
        WS_CONFIG = AutoConfig.getConfigHolder(WeatherSystemsConfig.class).getConfig();

        String entityTypes = WS_CONFIG.acid_rain.entity.entityTypesToDamage;
        String removeSpaces = entityTypes.trim().toLowerCase().replace(" ", "");
        String[] entityList = removeSpaces.split(",");

        for (String s : entityList) {
            if (s.equalsIgnoreCase("animal") && !damageAnimals)
                damageAnimals = true;
            if (s.equalsIgnoreCase("monster") && !damageMonsters)
                damageMonsters = true;
            if (s.equalsIgnoreCase("player") && !damagePlayer)
                damagePlayer = true;
        }

        String allowedBlockTypesToDestroy = WS_CONFIG.acid_rain.world.allowedBlocksToDestroy;
        String removeBlockTypeSpaces = allowedBlockTypesToDestroy.trim().toLowerCase().replace(" ", "");
        String[] blockTypeToDestroyList = removeBlockTypeSpaces.split(",");

        for (String s : blockTypeToDestroyList) {
            if (s.equalsIgnoreCase("grass") && !destroyGrass)
                destroyGrass = true;
            if (s.equalsIgnoreCase("leaves") && !destroyLeaves)
                destroyLeaves = true;
            if (s.equalsIgnoreCase("crops") && !destroyCrops)
                destroyCrops = true;
            if (s.equalsIgnoreCase("plants") && !destroyCrops)
                destroyPlants = true;
        }

        String blocksToNotDestroy = WS_CONFIG.acid_rain.world.blocksToNotDestroy;
        String removeBlocksToNotDestroySpaces = blocksToNotDestroy.trim().toLowerCase().replace(" ", "");
        String[] blocksToNotDestroyList = removeBlocksToNotDestroySpaces.split(",");
        for (String s : blocksToNotDestroyList) {
            net.minecraft.world.level.block.Block block = Registry.BLOCK.get(new ResourceLocation(s));
            if (block != null)
                WeatherSystems.blocksToNotDestroyList.add(block);
            else
                LOGGER.error("A block registry name you added to the \"BlocksToNotDestroy\" list was incorrect, you put: " + s + "\n Please fix it or this block will be destroyed.");
        }
    }

    public static int dataCache = 0;

    @Override
    public void onInitialize() {
        configReader();
        ServerTickEvents.END_WORLD_TICK.register(event -> WeatherSystemsEvents.worldTick(event.getLevel()));
        WeatherSystemsEvents.commandRegisterEvent();
    }

    public static class WeatherSystemsEvents {
        public static WeatherSystemsData weatherData = null;

        public static void worldTick(Level world) {
            setWeatherData(world);
            ServerLevel serverWorld = (ServerLevel) world;
            int tickSpeed = world.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
            long worldTime = world.getLevelData().getGameTime();

            //Rolls a random chance for acid rain once every 5000 ticks and will not run when raining to avoid disco colored rain.
            if (worldTime == 100 || worldTime % 24000 == 0 && !world.getLevelData().isRaining()) {
                Random random = world.random;
                weatherData.setAcidRain(random.nextFloat() < WS_CONFIG.acid_rain.world.acidRainChance);
                weatherData.setBlizzard(false);
            }
            if (worldTime == 100 || worldTime % 24000 == 0 && !world.getLevelData().isRaining()) {
                Random random = world.random;
                weatherData.setBlizzard(random.nextFloat() + 0.05 < WS_CONFIG.blizzard.world.snow_generation.blizzardChance);
                weatherData.setAcidRain(false);
            }

            if (world.getLevelData().isRaining()) {
                if (dataCache == 0)
                    dataCache++;
            } else {
                if (dataCache != 0) {
                    if (weatherData.isBlizzard())
                        weatherData.setBlizzard(false);
                    if (weatherData.isAcidRain())
                        weatherData.setAcidRain(false);
                }
            }


            List<ChunkHolder> list = Lists.newArrayList((serverWorld.getChunkSource()).chunkMap.getChunks());
            list.forEach(chunkHolder -> {
                Optional<LevelChunk> optional = chunkHolder.getTickingChunkFuture().getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK).left();
                //Gets chunks to tick
                if (optional.isPresent()) {
                    Optional<LevelChunk> optional1 = chunkHolder.getEntityTickingChunkFuture().getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK).left();
                    if (optional1.isPresent()) {
                        LevelChunk chunk = optional1.get();
                        Blizzard.blizzardEvent(chunk, serverWorld, tickSpeed, worldTime);
                        if (WS_CONFIG.blizzard.world.snow_decay.decaySnowAndIce)
                            Blizzard.doesIceAndSnowDecay(chunk, serverWorld, worldTime);
                        AcidRain.acidRainEvent(chunk, serverWorld, tickSpeed, worldTime);
                    }
                }
            });



            FriendlyByteBuf passedData = new FriendlyByteBuf(Unpooled.buffer());
            passedData.writeBoolean(weatherData.isBlizzard());
            passedData.writeBoolean(weatherData.isAcidRain());


            if (serverWorld.getLevelData().getGameTime() % 5 == 0) {
                world.players().forEach(player -> {
                    ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, WS_WEATHER_PACKET, passedData);
                });
            }



        }

//    public static void playerTickEvent(TickEvent.PlayerTickEvent event) {
//        setWeatherData(event.player.world);
//    }

        public static void entityTickEvent(net.minecraft.world.entity.Entity entity) {
            if (entity.level != null)
                setWeatherData(entity.level);


            if (damageMonsters) {
                if (entity.getType().getCategory() == MobCategory.MONSTER) {
                    Level world = entity.level;
                    BlockPos entityPos = new BlockPos(entity.getX(), entity.getY(), entity.getZ());

                    if (world.canSeeSky(entityPos) && WeatherSystemsEvents.weatherData.isAcidRain() && world.getLevelData().isRaining() && world.getGameTime() % WS_CONFIG.acid_rain.entity.entityDamageTickSpeed == 0) {
                        entity.hurt(DamageSource.GENERIC, (float) WS_CONFIG.acid_rain.entity.damageStrength);
                    }
                }
            }

            if (damageAnimals) {
                if (entity.getType().getCategory() == MobCategory.CREATURE || entity.getType().getCategory() == MobCategory.AMBIENT) {
                    Level world = entity.level;
                    BlockPos entityPos = new BlockPos(entity.getX(), entity.getY(), entity.getZ());

                    if (world.canSeeSky(entityPos) && WeatherSystemsEvents.weatherData.isAcidRain() && world.getLevelData().isRaining() && world.getGameTime() % WS_CONFIG.acid_rain.entity.entityDamageTickSpeed == 0) {
                        entity.hurt(DamageSource.GENERIC, (float) WS_CONFIG.acid_rain.entity.damageStrength);
                    }
                }
            }

            if (damagePlayer) {
                if (entity instanceof ServerPlayer) {
                    Level world = entity.level;
                    BlockPos entityPos = new BlockPos(entity.getX(), entity.getY(), entity.getZ());

                    if (world.canSeeSky(entityPos) && WeatherSystemsEvents.weatherData.isAcidRain() && world.getLevelData().isRaining() && world.getGameTime() % WS_CONFIG.acid_rain.entity.entityDamageTickSpeed == 0) {
                        entity.hurt(DamageSource.GENERIC, (float) WS_CONFIG.acid_rain.entity.damageStrength);
                    }
                }
            }
            Blizzard.blizzardEntityHandler(entity);
        }

        public static void commandRegisterEvent() {
            CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
                WeatherSystemsCommand.register(dispatcher);
            });
            WeatherSystems.LOGGER.info("WS: \"Server Starting\" Event Complete!");
        }

        public static void setWeatherData(LevelAccessor world) {
            if (WeatherSystemsEvents.weatherData == null)
                WeatherSystemsEvents.weatherData = WeatherSystemsData.get(world);
        }
    }


    public enum WeatherType {
        BLIZZARD,
        HAIL,
        HEATWAVE,
        WINDSTORM,
        SANDSTORM,
        ACIDRAIN
    }
}
