package com.mystic.weathersystems.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mystic.weathersystems.WeatherSystems;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class WeatherSystemsCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        WeatherSystems.LOGGER.debug("Registering WS commands...");
        List<String> weatherTypes = new ArrayList<>();
        weatherTypes.add("acidrain");
        weatherTypes.add("blizzard");
        weatherTypes.add("clear");
        LiteralCommandNode<CommandSourceStack> source = dispatcher.register(
                Commands.literal(WeatherSystems.MOD_ID)
                        .then(Commands.argument("weathertype", StringArgumentType.string()).suggests((ctx, sb) -> SharedSuggestionProvider.suggest(weatherTypes.stream(), sb))
                                .executes((cs) -> weatherSystemsSetWeatherType(cs.getSource().getLevel(), cs.getSource(), cs.getArgument("weathertype", String.class)))));

        dispatcher.register(Commands.literal(WeatherSystems.MOD_ID).redirect(source));
        WeatherSystems.LOGGER.debug("Registered WS Commands!");
    }

    public static int weatherSystemsSetWeatherType(Level world, CommandSourceStack source, String weatherType) {
        if (weatherType.equals("acidrain")) {
            WeatherSystems.WeatherSystemsEvents.weatherData.setBlizzard(false);
            WeatherSystems.WeatherSystemsEvents.weatherData.setAcidRain(true);
            world.getLevelData().setRaining(true);
            source.sendSuccess(new TranslatableComponent("commands.bw.setweather.success.acidrain"), true);
        } else if (weatherType.equals("blizzard")) {
            WeatherSystems.WeatherSystemsEvents.weatherData.setAcidRain(false);
            WeatherSystems.WeatherSystemsEvents.weatherData.setBlizzard(true);
            world.getLevelData().setRaining(true);
            source.sendSuccess(new TranslatableComponent("commands.bw.setweather.success.blizzard"), true);
        } else if (weatherType.equals("clear")) {
            WeatherSystems.WeatherSystemsEvents.weatherData.setAcidRain(false);
            WeatherSystems.WeatherSystemsEvents.weatherData.setBlizzard(false);
            world.getLevelData().setRaining(false);
            source.sendSuccess(new TranslatableComponent("commands.bw.setweather.success.clear"), true);
        } else {
            source.sendSuccess(new TranslatableComponent("commands.bw.setweather.failed", weatherType), true);
        }
        return 1;
    }
}
