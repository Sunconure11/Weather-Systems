package corgitaco.betterweather.api.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.api.BetterWeatherRegistry;
import corgitaco.betterweather.api.client.graphics.Graphics;
import corgitaco.betterweather.api.client.graphics.opengl.program.ShaderProgram;
import corgitaco.betterweather.api.client.graphics.opengl.program.ShaderProgramBuilder;
import corgitaco.betterweather.api.weather.WeatherEvent;
import corgitaco.betterweather.api.weather.WeatherEventClientSettings;
import corgitaco.betterweather.data.storage.WeatherEventSavedData;
import corgitaco.betterweather.helpers.BetterWeatherWorldData;
import corgitaco.betterweather.util.BetterWeatherUtil;
import corgitaco.betterweather.weather.BWWeatherEventContext;
import corgitaco.betterweather.weather.event.AcidRain;
import corgitaco.betterweather.weather.event.Blizzard;
import corgitaco.betterweather.weather.event.Rain;
import corgitaco.betterweather.weather.event.client.AcidRainClient;
import corgitaco.betterweather.weather.event.client.BlizzardClient;
import corgitaco.betterweather.weather.event.client.RainClient;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class WeatherEventClient<T extends WeatherEventClientSettings> {

    private final ColorSettings colorSettings;
    private final float skyOpacity;
    private final float fogDensity;
    private final boolean sunsetSunriseColor;

    private ShaderProgram program;

    protected final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

    public WeatherEventClient(T clientSettings) {
        this.colorSettings = clientSettings.getColorSettings();
        this.skyOpacity = clientSettings.skyOpacity();
        this.fogDensity = clientSettings.fogDensity();
        this.sunsetSunriseColor = clientSettings.sunsetSunriseColor();
    }

    public void renderWeather(Graphics graphics, Minecraft mc, ClientLevel world, LightTexture lightTexture, int ticks, float partialTicks, double x, double y, double z, Predicate<Biome> biomePredicate) {
        if (graphics.isSupported()) {
            renderWeatherShaders(graphics, world, x, y, z);
        } else {
            renderWeatherLegacy(mc, world, lightTexture, ticks, partialTicks, x, y, z, biomePredicate);
        }
    }

    public abstract void renderWeatherShaders(Graphics graphics, ClientLevel world, double x, double y, double z);

    public void renderWeatherLegacy(Minecraft mc, ClientLevel world, LightTexture lightTexture, int ticks, float partialTicks, double x, double y, double z, Predicate<Biome> biomePredicate) {
        switch (BWWeatherEventContext.currentEvent.getName()) {
            case "none", "cloudy", "cloudy_thundering", "betterweather-none", "betterweather-cloudy", "betterweather-cloudy_thundering" -> {
            }
            case "rain", "betterweather-rain", "thundering", "betterweather-thundering" -> {
                renderVanillaWeather(mc, partialTicks, x, y, z, lightTexture, Rain.RAIN_LOCATION, Rain.SNOW_LOCATION, ticks, biomePredicate);
            }
            case "acid_rain", "betterweather-acid_rain", "acid_rain_thundering", "betterweather-acid_rain_thundering" -> {
                renderVanillaWeather(mc, partialTicks, x, y, z, lightTexture, AcidRain.ACID_RAIN_LOCATION, Rain.SNOW_LOCATION, ticks, biomePredicate);
            }
            case "blizzard", "betterweather-blizzard" -> {
                ((BlizzardClient) Blizzard.DEFAULT.getClient()).renderWeatherLegacyBlizzard(mc, world, lightTexture, ticks, partialTicks, x, y, z, biomePredicate);
            }
            case "blizzard_thundering", "betterweather-blizzard_thundering" -> {
                ((BlizzardClient) Blizzard.DEFAULT_THUNDERING.getClient()).renderWeatherLegacyBlizzard(mc, world, lightTexture, ticks, partialTicks, x, y, z, biomePredicate);
            }
            default -> throw new IllegalStateException("Unexpected value: " + BWWeatherEventContext.currentEvent.getName());
        }
    }

    public abstract void clientTick(ClientLevel world, int tickSpeed, long worldTime, Minecraft mc, Predicate<Biome> biomePredicate);

    public boolean sunsetSunriseColor() {
        return sunsetSunriseColor;
    }

    public float skyOpacity() {
        return Mth.clamp(skyOpacity, 0.0F, 1.0F);
    }

    public float dayLightDarkness() {
        return fogDensity;
    }

    public boolean drippingLeaves() {
        return false;
    }

    public float fogDensity() {
        return fogDensity;
    }

    public ColorSettings getColorSettings() {
        return colorSettings;
    }

    @OnlyIn(Dist.CLIENT)
    public float skyOpacity(ClientLevel world, BlockPos playerPos, Predicate<Biome> isValidBiome) {
        return mixer(world, playerPos, 12, 2.0F, 1.0F - skyOpacity, isValidBiome);
    }

    @OnlyIn(Dist.CLIENT)
    public float fogDensity(ClientLevel world, BlockPos playerPos, Predicate<Biome> isValidBiome) {
        return mixer(world, playerPos, 12, 0.1F, fogDensity, isValidBiome);
    }


    private float mixer(ClientLevel world, BlockPos playerPos, int transitionRange, float weight, float targetMaxValue, Predicate<Biome> validBiomes) {
        int x = playerPos.getX();
        int z = playerPos.getZ();
        float accumulated = 0.0F;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int sampleX = x - transitionRange; sampleX <= x + transitionRange; ++sampleX) {
            pos.setX(sampleX);

            for (int sampleZ = z - transitionRange; sampleZ <= z + transitionRange; ++sampleZ) {
                pos.setZ(sampleZ);

                Holder<Biome> biome = world.getBiome(pos);
                if (validBiomes.test(biome.value())) {

                    accumulated += weight * weight;
                }
            }
        }
        float transitionSmoothness = 33 * 33;
        return Math.min(targetMaxValue, (float) Math.sqrt(accumulated / transitionSmoothness));
    }

    @OnlyIn(Dist.CLIENT)
    public float cloudBlendStrength(ClientLevel world, BlockPos playerPos, Predicate<Biome> isValidBiome) {
        return mixer(world, playerPos, 15, 1.2F, (float) this.getColorSettings().getCloudColorBlendStrength(), isValidBiome);
    }

    public void renderVanillaWeather(Minecraft mc, float partialTicks, double cameraX, double cameraY, double cameraZ, LightTexture lightmapIn, ResourceLocation rainTexture, ResourceLocation snowTexture, int ticks, Predicate<Biome> isValidBiome) {
        if (mc.level != null) {
            float rainStrength = mc.level.getRainLevel(partialTicks);
            if (!(rainStrength <= 0.0F)) {
                lightmapIn.turnOnLightLayer();
                Level world = mc.level;
                int x = Mth.floor(cameraX);
                int y = Mth.floor(cameraY);
                int z = Mth.floor(cameraZ);
                Tesselator tessellator = Tesselator.getInstance();
                BufferBuilder bufferbuilder = tessellator.getBuilder();
                RenderSystem.disableCull();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.enableDepthTest();
                int weatherRenderDistanceInBlocks = 5;
                if (Minecraft.useFancyGraphics()) {
                    weatherRenderDistanceInBlocks = 10;
                }

                RenderSystem.depthMask(Minecraft.useShaderTransparency());
                int i1 = -1;
                float ticksAndPartialTicks = (float) ticks + partialTicks;
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

                for (int dz = z - weatherRenderDistanceInBlocks; dz <= z + weatherRenderDistanceInBlocks; ++dz) {
                    for (int dx = x - weatherRenderDistanceInBlocks; dx <= x + weatherRenderDistanceInBlocks; ++dx) {
                        int index = (dz - z + 16) * 32 + dx - x + 16;
                        double rainX = (double) RainClient.getRainSizeX() * 5.0D;
                        double rainZ = (double) RainClient.getRainSizeZ() * 5.0D;
                        mutable.set(dx, y, dz);
                        Holder<Biome> biome = world.getBiome(mutable);
                        if (!isValidBiome.test(biome.value())) {
                            continue;
                        }

                        if (biome.value().getPrecipitation() != Biome.Precipitation.NONE) {
                            int motionBlockingHeight = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, mutable).getY();
                            int belowCameraYWeatherRenderDistance = y - weatherRenderDistanceInBlocks;
                            int aboveCameraYWeatherRenderDistance = y + weatherRenderDistanceInBlocks;
                            if (belowCameraYWeatherRenderDistance < motionBlockingHeight) {
                                belowCameraYWeatherRenderDistance = motionBlockingHeight;
                            }

                            if (aboveCameraYWeatherRenderDistance < motionBlockingHeight) {
                                aboveCameraYWeatherRenderDistance = motionBlockingHeight;
                            }

                            int atAboveHeightY = Math.max(motionBlockingHeight, y);

                            if (belowCameraYWeatherRenderDistance != aboveCameraYWeatherRenderDistance) {
                                Random random = new Random((long) (dx * dx * 3121 + dx * 45238971 ^ dz * dz * 418711 + dz * 13761));
                                mutable.set(dx, belowCameraYWeatherRenderDistance, dz);
                                float biomeTemperature = biome.get().getBaseTemperature();
                                if (biomeTemperature >= 0.15F) {
                                    i1 = renderRain(mc, partialTicks, cameraX, cameraY, cameraZ, rainTexture, ticks, rainStrength, world, tessellator, bufferbuilder, (float) weatherRenderDistanceInBlocks, i1, mutable, dz, dx, rainX, rainZ, belowCameraYWeatherRenderDistance, aboveCameraYWeatherRenderDistance, atAboveHeightY, random);
                                } else {
                                    i1 = renderSnow(mc, partialTicks, cameraX, cameraY, cameraZ, snowTexture, ticks, rainStrength, world, tessellator, bufferbuilder, (float) weatherRenderDistanceInBlocks, i1, ticksAndPartialTicks, mutable, dz, dx, rainX, rainZ, belowCameraYWeatherRenderDistance, aboveCameraYWeatherRenderDistance, atAboveHeightY, random);
                                }
                            }
                        }
                    }
                }

                if (i1 >= 0) {
                    tessellator.end();
                }

                RenderSystem.enableCull();
                RenderSystem.disableBlend();
                lightmapIn.turnOffLightLayer();
            }
        }
    }

    private int renderSnow(Minecraft mc, float partialTicks, double x, double y, double z, ResourceLocation snowTexture, int ticks, float rainStrength, Level world, Tesselator tessellator, BufferBuilder bufferbuilder, float graphicsQuality, int i1, float f1, BlockPos.MutableBlockPos mutable, int dz, int dx, double d0, double d1, int j2, int k2, int atOrAboveY, Random random) {
        if (i1 != 1) {
            if (i1 >= 0) {
                tessellator.end();
            }

            i1 = 1;
            mc.getTextureManager().getTexture(snowTexture);
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        float f6 = -((float) (ticks & 511) + partialTicks) / 512.0F;
        float f7 = (float) (random.nextDouble() + (double) f1 * 0.01D * (double) ((float) random.nextGaussian()));
        float f8 = (float) (random.nextDouble() + (double) (f1 * (float) random.nextGaussian()) * 0.001D);
        double d3 = (double) ((float) dx + 0.5F) - x;
        double d5 = (double) ((float) dz + 0.5F) - z;
        float f9 = Mth.sqrt((float) (d3 * d3 + d5 * d5)) / graphicsQuality;
        float alpha = ((1.0F - f9 * f9) * 0.3F + 0.5F) * rainStrength;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(dx, atOrAboveY, dz);
        int combinedLight = LevelRenderer.getLightColor(world, mutableBlockPos);
        int l3 = combinedLight >> 16 & '\uffff';
        int i4 = (combinedLight & '\uffff') * 3;
        int j4 = (l3 * 3 + 240) / 4;
        int k4 = (i4 * 3 + 240) / 4;
        bufferbuilder.vertex((double) dx - x - d0 + 0.5D, (double) k2 - y, (double) dz - z - d1 + 0.5D).uv(0.0F + f7, (float) j2 * 0.25F + f6 + f8).color(1.0F, 1.0F, 1.0F, alpha).uv2(k4, j4).endVertex();
        bufferbuilder.vertex((double) dx - x + d0 + 0.5D, (double) k2 - y, (double) dz - z + d1 + 0.5D).uv(1.0F + f7, (float) j2 * 0.25F + f6 + f8).color(1.0F, 1.0F, 1.0F, alpha).uv2(k4, j4).endVertex();
        bufferbuilder.vertex((double) dx - x + d0 + 0.5D, (double) j2 - y, (double) dz - z + d1 + 0.5D).uv(1.0F + f7, (float) k2 * 0.25F + f6 + f8).color(1.0F, 1.0F, 1.0F, alpha).uv2(k4, j4).endVertex();
        bufferbuilder.vertex((double) dx - x - d0 + 0.5D, (double) j2 - y, (double) dz - z - d1 + 0.5D).uv(0.0F + f7, (float) k2 * 0.25F + f6 + f8).color(1.0F, 1.0F, 1.0F, alpha).uv2(k4, j4).endVertex();
        return i1;
    }

    private int renderRain(Minecraft mc, float partialTicks, double x, double y, double z, ResourceLocation rainTexture, int ticks, float rainStrength, Level world, Tesselator tessellator, BufferBuilder bufferbuilder, float l, int i1, BlockPos.MutableBlockPos mutable, int dz, int dx, double d0, double d1, int j2, int k2, int atOrAboveY, Random random) {
        if (i1 != 0) {
            if (i1 >= 0) {
                tessellator.end();
            }

            i1 = 0;
            mc.getTextureManager().getTexture(rainTexture);
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        int i3 = ticks + dx * dx * 3121 + dx * 45238971 + dz * dz * 418711 + dz * 13761 & 31;
        float f3 = -((float) i3 + partialTicks) / 32.0F * (3.0F + random.nextFloat());
        double d2 = (double) ((float) dx + 0.5F) - x;
        double d4 = (double) ((float) dz + 0.5F) - z;
        float f4 = Mth.sqrt((float) (d2 * d2 + d4 * d4)) / l;
        float alpha = ((1.0F - f4 * f4) * 0.5F + 0.5F) * rainStrength;
        mutable.set(dx, atOrAboveY, dz);
        int combinedLight = LevelRenderer.getLightColor(world, mutable);
        bufferbuilder.vertex((double) dx - x - d0 + 0.5D, (double) k2 - y, (double) dz - z - d1 + 0.5D).uv(0.0F, (float) j2 * 0.25F + f3).color(1.0F, 1.0F, 1.0F, alpha).uv2(combinedLight).endVertex();
        bufferbuilder.vertex((double) dx - x + d0 + 0.5D, (double) k2 - y, (double) dz - z + d1 + 0.5D).uv(1.0F, (float) j2 * 0.25F + f3).color(1.0F, 1.0F, 1.0F, alpha).uv2(combinedLight).endVertex();
        bufferbuilder.vertex((double) dx - x + d0 + 0.5D, (double) j2 - y, (double) dz - z + d1 + 0.5D).uv(1.0F, (float) k2 * 0.25F + f3).color(1.0F, 1.0F, 1.0F, alpha).uv2(combinedLight).endVertex();
        bufferbuilder.vertex((double) dx - x - d0 + 0.5D, (double) j2 - y, (double) dz - z - d1 + 0.5D).uv(0.0F, (float) k2 * 0.25F + f3).color(1.0F, 1.0F, 1.0F, alpha).uv2(combinedLight).endVertex();
        return i1;
    }

    public ShaderProgram buildOrGetProgram(Consumer<ShaderProgramBuilder> consumer) {
        if (program == null) {
            ShaderProgramBuilder builder = ShaderProgramBuilder.create();

            try {
                consumer.accept(builder);
            } catch (Exception e) {
                BetterWeather.LOGGER.error(e);

                builder.clean();
            }

            return program = builder.build();
        }

        return program;
    }
}
