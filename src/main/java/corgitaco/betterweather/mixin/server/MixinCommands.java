package corgitaco.betterweather.mixin.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.server.command.SetWeatherCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Commands.class)
public abstract class MixinCommands {

    @Shadow
    @Final
    private CommandDispatcher<CommandSourceStack> dispatcher;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void addBetterWeatherCommands(Commands.CommandSelection p_230943_, CommandBuildContext p_230944_, CallbackInfo ci) {
        LiteralArgumentBuilder<CommandSourceStack> requires = Commands.literal(BetterWeather.MOD_ID).requires(commandSource -> commandSource.hasPermission(3));

        requires.then(SetWeatherCommand.register(dispatcher));
        LiteralCommandNode<CommandSourceStack> source = dispatcher.register(requires);
        dispatcher.register(Commands.literal(BetterWeather.MOD_ID).redirect(source));
        dispatcher.register(Commands.literal("bw").redirect(source)); // Create 'bw' alias.
        BetterWeather.LOGGER.debug("Registered Better Weather Commands!");
    }
}
