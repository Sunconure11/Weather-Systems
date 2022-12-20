package corgitaco.betterweather.mixin.client;

import corgitaco.betterweather.api.Climate;
import corgitaco.betterweather.helpers.BetterWeatherWorldData;
import corgitaco.betterweather.weather.BWWeatherEventContext;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.RegistryAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.function.BooleanSupplier;

@Mixin(ClientLevel.class)
public abstract class MixinClientWorld implements BetterWeatherWorldData, Climate {

    @Shadow public abstract RegistryAccess registryAccess();

    @Nullable
    private BWWeatherEventContext weatherContext;

    @Nullable
    @Override
    public BWWeatherEventContext getWeatherEventContext() {
        return this.weatherContext;
    }

    @Nullable
    @Override
    public BWWeatherEventContext setWeatherEventContext(BWWeatherEventContext weatherEventContext) {
        this.weatherContext = weatherEventContext;
        return this.weatherContext;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(BooleanSupplier hasTicksLeft, CallbackInfo ci) {
        if (this.weatherContext != null) {
            this.weatherContext.tick((ClientLevel) (Object) this);
        }
    }

    @Redirect(method = "getSkyColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getRainLevel(F)F"))
    private float doNotDarkenSkyWithRainStrength(ClientLevel world, float delta) {
        return this.weatherContext != null ? 0.0F : world.getRainLevel(delta);
    }

    @Redirect(method = "getCloudColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getRainLevel(F)F"))
    private float doNotDarkenCloudsWithRainStrength(ClientLevel instance, float v) {
        return this.weatherContext != null ? 0.0F : instance.getRainLevel(v);
    }

    @Redirect(method = "getSkyDarken", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getRainLevel(F)F"))
    private float sunBrightness(ClientLevel instance, float v) {
        float rainStrength = ((ClientLevel) (Object) this).getRainLevel(v);
        BWWeatherEventContext weatherContext = this.weatherContext;
        return weatherContext != null ? rainStrength * weatherContext.getCurrentEvent().getClientSettings().dayLightDarkness() : rainStrength;
    }
}
