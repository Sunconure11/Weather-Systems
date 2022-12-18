package corgitaco.betterweather.api;

import java.util.logging.Level;

/**
 * A class used to acquire a world's Climate info.
 * Safely castable to or extenders of: {@link net.minecraft.client.multiplayer.ClientLevel} or {@link net.minecraft.server.level.ServerLevel}.
 * <p></p>
 * Basically the functioning "entry point" for Better Weather's Data.
 */
public interface Climate {
    static Climate getClimate(Level world) {
        return ((Climate)(Object) world);
    }
}
