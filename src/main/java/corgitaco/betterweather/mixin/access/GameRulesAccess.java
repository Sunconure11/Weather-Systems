package corgitaco.betterweather.mixin.access;

import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(GameRules.class)
public interface GameRulesAccess {

    @Accessor("GAME_RULE_TYPES")
    static Map<GameRules.Key<?>, GameRules.Type<?>> getGameRules() {
        throw new Error("Mixin did not apply");
    }

}
