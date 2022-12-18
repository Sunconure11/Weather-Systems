package corgitaco.betterweather.mixin.access;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkMap.class)
public interface ChunkManagerAccess {

    @Accessor
    void setGenerator(ChunkGenerator chunkGenerator);
}
