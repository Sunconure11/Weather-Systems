package corgitaco.betterweather.mixin.client.block;


import corgitaco.betterweather.helpers.BetterWeatherWorldData;
import corgitaco.betterweather.weather.BWWeatherEventContext;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;


@Mixin(LeavesBlock.class)
public abstract class MixinLeavesBlock {

    @Inject(at = @At("HEAD"), method = "animateTick", cancellable = true)
    private void noRainDripping(BlockState p_221374_, Level world, BlockPos p_221376_, RandomSource p_221377_, CallbackInfo ci) {
        BWWeatherEventContext weatherEventContext = ((BetterWeatherWorldData) world).getWeatherEventContext();
        if (weatherEventContext != null) {
            if (!weatherEventContext.getCurrentEvent().getClientSettings().drippingLeaves()) {
                ci.cancel();
            }
        }
    }
}