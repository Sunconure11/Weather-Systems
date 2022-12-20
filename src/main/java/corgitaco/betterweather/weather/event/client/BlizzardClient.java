package corgitaco.betterweather.weather.event.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.api.client.WeatherEventClient;
import corgitaco.betterweather.api.client.graphics.Graphics;
import corgitaco.betterweather.api.client.graphics.opengl.program.ShaderProgram;
import corgitaco.betterweather.api.client.graphics.opengl.program.ShaderProgramBuilder;
import corgitaco.betterweather.api.weather.WeatherEventAudio;
import corgitaco.betterweather.weather.event.client.settings.BlizzardClientSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;

import java.io.IOException;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;

public class BlizzardClient extends WeatherEventClient<BlizzardClientSettings> implements WeatherEventAudio {
    private final float[] rainSizeX = new float[1024];
    private final float[] rainSizeZ = new float[1024];
    private final ResourceLocation textureLocation;
    private final SoundEvent audio;
    private final float audioVolume;
    private final float audioPitch;

    private final Consumer<ShaderProgramBuilder> builder;

    private final Matrix4f modelMatrix = new Matrix4f();

    public BlizzardClient(BlizzardClientSettings clientSettings) {
        super(clientSettings);
        this.textureLocation = clientSettings.textureLocation;
        this.audio = clientSettings.getSound();
        this.audioVolume = clientSettings.getVolume();
        this.audioPitch = clientSettings.getPitch();

        for (int i = 0; i < 32; ++i) {
            for (int j = 0; j < 32; ++j) {
                float f = (float) (j - 16);
                float f1 = (float) (i - 16);
                float f2 = Mth.sqrt(f * f + f1 * f1);
                this.rainSizeX[i << 5 | j] = -f1 / f2;
                this.rainSizeZ[i << 5 | j] = f / f2;
            }
        }

        builder = builder1 -> {
            ResourceManager manager = Minecraft.getInstance().getResourceManager();

            try {
                builder1
                        .compile(GL_FRAGMENT_SHADER, manager.getResource(new ResourceLocation(BetterWeather.MOD_ID, "shaders/fragment.glsl")).orElseThrow())
                        .compile(GL_VERTEX_SHADER, manager.getResource(new ResourceLocation(BetterWeather.MOD_ID, "shaders/vertex.glsl")).orElseThrow());
            } catch (IOException e) {
                BetterWeather.LOGGER.error(e);

                builder1.clean();
            }
        };
    }

    @Override
    public void renderWeatherShaders(Graphics graphics, ClientLevel world, double x, double y, double z) {
        ShaderProgram program = buildOrGetProgram(builder);

        int floorX = Mth.floor(x);
        int floorY = Mth.floor(y);
        int floorZ = Mth.floor(z);

        int radius = 5;

        program.bind();

        modelMatrix.setIdentity();
        modelMatrix.translate(new Vector3f((float) x, (float) y, (float) z));

        program.uploadMatrix4f("pos", modelMatrix);

        /**
         * Imagine a horizontal plane.
         *
         * iterating from:
         * (floor - radius) 🡢 (floor + radius) X
         * 🡣
         * (floor + radius)
         * Z
         *
         * -x-z ─────(x)───── +x
         * │                   ┊
         * │                   ┊
         * (z)                 ┊
         * │                   ┊
         * │                   ┊
         * +z ╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌
         */
        for (int planeX = floorX - radius; planeX < floorX + radius; planeX++) for (int planeZ = floorZ - radius; planeZ < floorZ + radius; planeZ++) {

            int height = world.getHeight(Heightmap.Types.MOTION_BLOCKING, planeX, planeZ);

        }

        program.unbind();

    }

    public boolean renderWeatherLegacyBlizzard(Minecraft mc, ClientLevel world, LightTexture lightTexture, int ticks, float partialTicks, double x, double y,  double z, Predicate<Biome> biomePredicate) {
        float rainStrength = world.getRainLevel(partialTicks);
        lightTexture.turnOnLightLayer();
        int floorX = Mth.floor(x);
        int floorY = Mth.floor(y);
        int floorZ = Mth.floor(z);
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        int graphicsQuality = 5;
        if (Minecraft.useFancyGraphics()) {
            graphicsQuality = 10;
        }

        RenderSystem.depthMask(Minecraft.useShaderTransparency());
        int i1 = -1;
        float ticksAndPartialTicks = (float) ticks + partialTicks;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();

        for (int graphicQualityZ = floorZ - graphicsQuality; graphicQualityZ <= floorZ + graphicsQuality; ++graphicQualityZ) {
            for (int graphicQualityX = floorX - graphicsQuality; graphicQualityX <= floorX + graphicsQuality; ++graphicQualityX) {
                int rainSizeIdx = (graphicQualityZ - floorZ + 16) * 32 + graphicQualityX - floorX + 16;
                //These 2 doubles control the size of rain particles.
                double rainSizeX = (double) this.rainSizeX[rainSizeIdx] * 0.5D;
                double rainSizeZ = (double) this.rainSizeZ[rainSizeIdx] * 0.5D;
                blockPos.set(graphicQualityX, 0, graphicQualityZ);
                Holder<Biome> biome = world.getBiome(blockPos);
                int topPosY = mc.level.getHeight(Heightmap.Types.MOTION_BLOCKING, blockPos.getX(), blockPos.getZ());
                int floorYMinusGraphicsQuality = floorY - graphicsQuality;
                int floorYPlusGraphicsQuality = floorY + graphicsQuality;
                if (floorYMinusGraphicsQuality < topPosY) {
                    floorYMinusGraphicsQuality = topPosY;
                }

                if (floorYPlusGraphicsQuality < topPosY) {
                    floorYPlusGraphicsQuality = topPosY;
                }

                int posY2 = Math.max(topPosY, floorY);

                if (floorYMinusGraphicsQuality != floorYPlusGraphicsQuality) {
                    Random random = new Random(graphicQualityX * graphicQualityX * 3121 + graphicQualityX * 45238971 ^ graphicQualityZ * graphicQualityZ * 418711 + graphicQualityZ * 13761);
                    blockPos.set(graphicQualityX, floorYMinusGraphicsQuality, graphicQualityZ);

                    //This is rain rendering.
                    if (i1 != 1) {
                        if (i1 >= 0) {
                            tessellator.end();
                        }

                        i1 = 1;
                        mc.getTextureManager().getTexture(this.textureLocation);
                        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
                    }

                    float f7 = (float) (random.nextDouble() + (double) (ticksAndPartialTicks * (float) random.nextGaussian()) * 0.03D);
                    float fallSpeed = (float) (random.nextDouble() + (double) (ticksAndPartialTicks * (float) random.nextGaussian()) * 0.03D);
                    double d3 = (double) ((float) graphicQualityX + 0.5F) - x;
                    double d5 = (double) ((float) graphicQualityZ + 0.5F) - z;
                    float f9 = Mth.sqrt((float) (d3 * d3 + d5 * d5)) / (float) graphicsQuality;
                    float ticksAndPartialTicks0 = ((1.0F - f9 * f9) * 0.3F + 0.5F) * rainStrength;
                    blockPos.set(graphicQualityX, posY2, graphicQualityZ);
                    int k3 = LevelRenderer.getLightColor(world, blockPos);
                    int l3 = k3 >> 16 & '\uffff';
                    int i4 = (k3 & '\uffff') * 3;
                    int j4 = (l3 * 3 + 240) / 4;
                    int k4 = (i4 * 3 + 240) / 4;
                    if (biomePredicate.test(biome.value())) {
                        bufferbuilder.vertex((double) graphicQualityX - x - rainSizeX + 0.5D + random.nextGaussian() * 2, (double) floorYPlusGraphicsQuality - y, (double) graphicQualityZ - z - rainSizeZ + 0.5D + random.nextGaussian()).uv(0.0F + f7, (float) floorYMinusGraphicsQuality * 0.25F - Math.abs(fallSpeed)).color(1.0F, 1.0F, 1.0F, ticksAndPartialTicks0).uv2(k4, j4).endVertex();
                        bufferbuilder.vertex((double) graphicQualityX - x + rainSizeX + 0.5D + random.nextGaussian() * 2, (double) floorYPlusGraphicsQuality - y, (double) graphicQualityZ - z + rainSizeZ + 0.5D + random.nextGaussian()).uv(1.0F + f7, (float) floorYMinusGraphicsQuality * 0.25F - Math.abs(fallSpeed)).color(1.0F, 1.0F, 1.0F, ticksAndPartialTicks0).uv2(k4, j4).endVertex();
                        bufferbuilder.vertex((double) graphicQualityX - x + rainSizeX + 0.5D + random.nextGaussian() * 2, (double) floorYMinusGraphicsQuality - y, (double) graphicQualityZ - z + rainSizeZ + 0.5D + random.nextGaussian()).uv(1.0F + f7, (float) floorYPlusGraphicsQuality * 0.25F - Math.abs(fallSpeed)).color(1.0F, 1.0F, 1.0F, ticksAndPartialTicks0).uv2(k4, j4).endVertex();
                        bufferbuilder.vertex((double) graphicQualityX - x - rainSizeX + 0.5D + random.nextGaussian() * 2, (double) floorYMinusGraphicsQuality - y, (double) graphicQualityZ - z - rainSizeZ + 0.5D + random.nextGaussian()).uv(0.0F + f7, (float) floorYPlusGraphicsQuality * 0.25F - Math.abs(fallSpeed)).color(1.0F, 1.0F, 1.0F, ticksAndPartialTicks0).uv2(k4, j4).endVertex();
                    }
                }
            }
        }

        if (i1 >= 0) {
            tessellator.end();
        }

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        lightTexture.turnOffLightLayer();
        return true;
    }

    @Override
    public void clientTick(ClientLevel world, int tickSpeed, long worldTime, Minecraft mc, Predicate<Biome> biomePredicate) {
    }

    @Override
    public float getVolume() {
        return this.audioVolume;
    }

    @Override
    public float getPitch() {
        return this.audioPitch;
    }

    @Override
    public SoundEvent getSound() {
        return this.audio;
    }
}
