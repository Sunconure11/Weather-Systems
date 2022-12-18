package corgitaco.betterweather.mixin.entity;

import corgitaco.betterweather.helpers.BetterWeatherWorldData;
import corgitaco.betterweather.weather.BWWeatherEventContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {


    @Inject(method = "tick", at = @At("HEAD"))
    private void weatherLivingTickUpdate(CallbackInfo ci) {
        Level world = ((Entity) (Object) this).level;
        BWWeatherEventContext weatherEventContext = ((BetterWeatherWorldData) world).getWeatherEventContext();
        if (weatherEventContext != null) {
            weatherEventContext.getCurrentEvent().livingEntityUpdate((LivingEntity) (Object) this);
        }
    }
}
