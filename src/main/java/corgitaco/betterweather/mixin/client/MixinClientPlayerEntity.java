package corgitaco.betterweather.mixin.client;

import com.mojang.authlib.GameProfile;
import corgitaco.betterweather.client.audio.WeatherSoundHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class MixinClientPlayerEntity {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void weatherAmbience(Level p_219727_, BlockPos p_219728_, float p_219729_, GameProfile p_219730_, ProfilePublicKey p_219731_, CallbackInfo ci) {
         new WeatherSoundHandler((Player) (Object) this, Minecraft.getInstance().getSoundManager(), p_219727_.getBiomeManager());
    }
}
