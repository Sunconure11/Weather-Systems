package corgitaco.betterweather.mixin.access;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Registry.class)
public interface RegistryAccess {

    @Invoker("registerSimple")
    static <T> Registry<T> invokeRegisterSimple(ResourceKey<? extends Registry<T>> registryKey, Registry.RegistryBootstrap<T> registryBootstrap) {
        throw new Error("Mixin did not apply!");
    }
}
