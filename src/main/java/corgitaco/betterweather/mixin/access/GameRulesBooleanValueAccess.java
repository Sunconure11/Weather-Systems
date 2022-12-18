package corgitaco.betterweather.mixin.access;

import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRules.BooleanValue.class)
public interface GameRulesBooleanValueAccess {

    @Invoker
    static GameRules.Type<GameRules.BooleanValue> invokeCreate(boolean defaultValue) {
        throw new Error("Mixin did not apply");
    }
}
