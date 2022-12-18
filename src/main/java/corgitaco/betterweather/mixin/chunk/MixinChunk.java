package corgitaco.betterweather.mixin.chunk;

import corgitaco.betterweather.chunk.TickHelper;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LevelChunk.class)
public abstract class MixinChunk implements TickHelper {

    private boolean isTickDirty;
    
    @Override
    public boolean isTickDirty() {
        return isTickDirty;
    }

    @Override
    public void setTickDirty() {
        isTickDirty = true;
    }
}
