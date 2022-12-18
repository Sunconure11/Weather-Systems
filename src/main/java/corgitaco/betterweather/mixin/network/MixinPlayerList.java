package corgitaco.betterweather.mixin.network;

import corgitaco.betterweather.data.network.NetworkHandler;
import corgitaco.betterweather.data.network.packet.weather.WeatherContextConstructingPacket;
import corgitaco.betterweather.helpers.BetterWeatherWorldData;
import corgitaco.betterweather.weather.BWWeatherEventContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public abstract class MixinPlayerList {
    @Inject(method = "sendLevelInfo", at = @At(value = "HEAD"))
    private void sendContext(ServerPlayer playerIn, ServerLevel worldIn, CallbackInfo ci) {
        BWWeatherEventContext weatherEventContext = ((BetterWeatherWorldData) worldIn).getWeatherEventContext();
        if (weatherEventContext != null) {
            NetworkHandler.sendToPlayer(playerIn, new WeatherContextConstructingPacket(weatherEventContext));
        }
    }
}
