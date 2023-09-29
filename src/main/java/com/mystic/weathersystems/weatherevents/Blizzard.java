package com.mystic.weathersystems.weatherevents;

import com.mystic.weathersystems.SoundRegistry;
import com.mystic.weathersystems.WeatherSystems;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;


public class Blizzard {

    public static void blizzardEvent(LevelChunk chunk, Level world, int tickSpeed, long worldTime) {
        ChunkPos chunkpos = chunk.getPos();
        int chunkXStart = chunkpos.getMinBlockX();
        int chunkZStart = chunkpos.getMinBlockZ();
        ProfilerFiller iprofiler = world.getProfiler();
        iprofiler.incrementCounter("blizzard");
        BlockPos blockpos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, world.getBlockRandomPos(chunkXStart, 0, chunkZStart, 15));
        Biome biome = world.getBiome(blockpos);
        if (isAreaLoaded(blockpos, 1, world)) {
            if (WeatherSystems.WeatherSystemsEvents.weatherData.isBlizzard() && world.getLevelData().isRaining() && worldTime % WeatherSystems.WS_CONFIG.blizzard.world.snow_generation.tickSnowAndIcePlaceSpeed == 0 && biome.getBiomeCategory() != Biome.BiomeCategory.NETHER && biome.getBiomeCategory() != Biome.BiomeCategory.THEEND && biome.getBiomeCategory() != Biome.BiomeCategory.NONE && doBlizzardsAffectDeserts(biome) && WeatherSystems.WS_CONFIG.blizzard.world.snow_generation.spawnSnowAndIce) {
                if (world.getBlockState(blockpos.below()).getBlock() == Blocks.WATER && world.getBlockState(blockpos.below()).getFluidState().getAmount() == 8) {
                    world.setBlockAndUpdate(blockpos.below(), Blocks.ICE.defaultBlockState());
                }
                if (world.getBlockState(blockpos.below()).getMaterial() != Material.WATER && world.getBlockState(blockpos.below()).getMaterial() != Material.LAVA && world.getBlockState(blockpos.below()).getMaterial() != Material.ICE && world.getBlockState(blockpos.below()).getMaterial() != Material.CACTUS && doBlizzardsDestroyPlants(world.getBlockState(blockpos).getMaterial())) {
                    if (world.getBlockState(blockpos).getBlock() != Blocks.SNOW)
                        world.setBlockAndUpdate(blockpos, Blocks.SNOW.defaultBlockState());

                    Block block = world.getBlockState(blockpos).getBlock();

                    if (block == Blocks.SNOW && world.getBlockState(blockpos).getValue(BlockStateProperties.LAYERS) == 1 && world.random.nextInt(5) == 2)
                        world.setBlockAndUpdate(blockpos, block.defaultBlockState().setValue(BlockStateProperties.LAYERS, 2));
                    else if (block == Blocks.SNOW && world.getBlockState(blockpos).getValue(BlockStateProperties.LAYERS) == 2 && world.random.nextInt(5) == 3)
                        world.setBlockAndUpdate(blockpos, block.defaultBlockState().setValue(BlockStateProperties.LAYERS, 3));
                    else if (block == Blocks.SNOW && world.getBlockState(blockpos).getValue(BlockStateProperties.LAYERS) == 3 && world.random.nextInt(5) == 0)
                        world.setBlockAndUpdate(blockpos, block.defaultBlockState().setValue(BlockStateProperties.LAYERS, 4));
                    else if (block == Blocks.SNOW && world.getBlockState(blockpos).getValue(BlockStateProperties.LAYERS) == 4 && world.random.nextInt(5) == 4)
                        world.setBlockAndUpdate(blockpos, block.defaultBlockState().setValue(BlockStateProperties.LAYERS, 5));
                    else if (block == Blocks.SNOW && world.getBlockState(blockpos).getValue(BlockStateProperties.LAYERS) == 5 && world.random.nextInt(5) == 0)
                        world.setBlockAndUpdate.place(blockpos, block.defaultBlockState().setValue(BlockStateProperties.LAYERS, 6));
                    else if (block == Blocks.SNOW && world.getBlockState(blockpos).getValue(BlockStateProperties.LAYERS) == 6 && world.random.nextInt(5) == 1)
                        world.setBlockAndUpdate(blockpos, block.defaultBlockState().setValue(BlockStateProperties.LAYERS, 7));
                    else if (block == Blocks.SNOW && world.getBlockState(blockpos).getValue(BlockStateProperties.LAYERS) == 7 && world.random.nextInt(5) == 0)
                        world.setBlockAndUpdate(blockpos, Blocks.SNOW_BLOCK.defaultBlockState());
                }
            }
        }
        iprofiler.pop();
    }

    public static void doesIceAndSnowDecay(LevelChunk chunk, Level world, long worldTime) {
        ChunkPos chunkpos = chunk.getPos();
        int chunkXStart = chunkpos.getMinBlockX();
        int chunkZStart = chunkpos.getMinBlockZ();
        ProfilerFiller iprofiler = world.getProfiler();
        iprofiler.push("iceandsnowdecay");
        BlockPos blockpos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, world.getBlockRandomPos(chunkXStart, 0, chunkZStart, 15));
        Biome biome = world.getBiome(blockpos);
        Block blockDown = world.getBlockState(blockpos.below()).getBlock();
        Block block = world.getBlockState(blockpos).getBlock();

        if (isAreaLoaded(blockpos, 1, world)) {
            if (biome.getTemperature(blockpos) >= WeatherSystems.WS_CONFIG.blizzard.world.snow_decay.snowDecayTemperatureThreshold) {
                if (!world.getLevelData().isRaining() && worldTime % WeatherSystems.WS_CONFIG.blizzard.world.snow_decay.tickSnowAndIceDecaySpeed == 0 && biome.getBiomeCategory() != Biome.BiomeCategory.NETHER && biome.getBiomeCategory() != Biome.BiomeCategory.THEEND && biome.getBiomeCategory() != Biome.BiomeCategory.NONE && doBlizzardsAffectDeserts(biome)) {
                    if (blockDown == Blocks.SNOW)
                        world.setBlockAndUpdate(blockpos.below(), Blocks.AIR.defaultBlockState());
                    if (block == Blocks.SNOW)
                        world.setBlockAndUpdate(blockpos, Blocks.AIR.defaultBlockState());
                    if (blockDown == Blocks.ICE)
                        world.setBlockAndUpdate(blockpos.below(), Blocks.WATER.defaultBlockState());

                }
            }
        }
        iprofiler.pop();
    }


    static int cycleBlizzardSounds = 0;

    @Environment(EnvType.CLIENT)
    public static void blizzardSoundHandler(Minecraft minecraft, Camera activeRenderInfo) {
        double volume = WeatherSystems.WS_CONFIG.blizzard.client.blizzardVolume;
        double pitch = WeatherSystems.WS_CONFIG.blizzard.client.blizzardPitch;
        BlockPos pos = new BlockPos(activeRenderInfo.getPosition());
        if (minecraft.level != null) {
            if (WeatherSystems.WeatherSystemsEvents.weatherData.isBlizzard() && minecraft.level.isRaining()) {
                minecraft.getSoundManager().soundEngine.updateCategoryVolume(SoundSource.WEATHER, 0.1F);
            }
        }


        BlizzardLoopSoundTrack soundTrack = WeatherSystems.WS_CONFIG.blizzard.client.blizzardSoundTrack;

        SimpleSoundInstance simplesound = new SimpleSoundInstance(soundTrack.getSoundEvent(), SoundSource.WEATHER, (float) volume, (float) pitch, pos.getX(), pos.getY(), pos.getZ());
        if (minecraft.level != null && minecraft.level.getLevelData().isRaining() && WeatherSystems.WeatherSystemsEvents.weatherData.isBlizzard() && doBlizzardsAffectDeserts(minecraft.level.getBiome(pos))) {
            if (cycleBlizzardSounds == 0 || minecraft.level.getLevelData().getGameTime() % soundTrack.getReplayRate() == 0) {
                minecraft.getSoundManager().play(simplesound);
                cycleBlizzardSounds++;
            }
        }
        if (minecraft.level != null) {
            if (!WeatherSystems.WeatherSystemsEvents.weatherData.isBlizzard() || !doBlizzardsAffectDeserts(minecraft.level.getBiome(pos))) {
                minecraft.getSoundManager().stop(simplesound.getLocation(), SoundSource.WEATHER);
                if (cycleBlizzardSounds != 0)
                    cycleBlizzardSounds = 0;
            }
        }
    }

    public enum BlizzardLoopSoundTrack {
        LOOP1(SoundRegistry.BLIZZARD_LOOP1, 2400),
        LOOP2(SoundRegistry.BLIZZARD_LOOP2, 2400),
        LOOP3(SoundRegistry.BLIZZARD_LOOP3, 2400),
        LOOP4(SoundRegistry.BLIZZARD_LOOP4, 2400),
        LOOP5(SoundRegistry.BLIZZARD_LOOP5, 2400),
        LOOP6(SoundRegistry.BLIZZARD_LOOP6, 2400),
        LOOP7(SoundRegistry.BLIZZARD_LOOP7, 1200);

        private final SoundEvent soundEvent;
        private final int replayRate;

        BlizzardLoopSoundTrack(SoundEvent soundEvent, int tickReplayRate) {
            this.soundEvent = soundEvent;
            this.replayRate = tickReplayRate;
        }

        public SoundEvent getSoundEvent() {
            return this.soundEvent;
        }

        public int getReplayRate() {
            return this.replayRate;
        }

        public SoundEvent[] soundRegistries = {
                SoundRegistry.BLIZZARD_LOOP1,
                SoundRegistry.BLIZZARD_LOOP2,
                SoundRegistry.BLIZZARD_LOOP3,
                SoundRegistry.BLIZZARD_LOOP4,
                SoundRegistry.BLIZZARD_LOOP5,
                SoundRegistry.BLIZZARD_LOOP6,
                SoundRegistry.BLIZZARD_LOOP7
        };
    }

    public static void blizzardEntityHandler(Entity entity) {
        WeatherSystems.WeatherSystemsEvents.setWeatherData(entity.level);
        if (entity instanceof LivingEntity) {
            if (entity.level.getLevelData().isRaining() && WeatherSystems.WeatherSystemsEvents.weatherData.isBlizzard() && WeatherSystems.WS_CONFIG.blizzard.entity.doBlizzardsSlowEntities)
                ((LivingEntity) entity).addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, WeatherSystems.WS_CONFIG.blizzard.entity.blizzardSlownessAmplifier, true, false));
        }
    }


    public static boolean doBlizzardsAffectDeserts(Biome biome) {
        if (!WeatherSystems.WS_CONFIG.blizzard.world.snow_generation.blizzardsInDeserts)
            return biome.getBiomeCategory() != Biome.BiomeCategory.DESERT;
        else
            return true;
    }

    public static boolean doBlizzardsDestroyPlants(Material material) {
        if (!WeatherSystems.WS_CONFIG.blizzard.world.snow_generation.destroyPlants)
            return material != Material.PLANT && material != Material.REPLACEABLE_PLANT && material != Material.WATER_PLANT;
        else
            return true;
    }

    public static boolean isAreaLoaded(BlockPos center, int range, Level level) {
        return level.hasChunksAt(center.offset(-range, -range, -range), center.offset(range, range, range));
    }
}
