package com.ultra.megamod.feature.dynamiclights;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;

public class DynamicLightsConfigSender {

    public static void sendConfig(MinecraftServer server) {
        sendCommand("scoreboard objectives add ts.dl.settings dummy", server);

        applySetting("$enable", DynamicLightsConfig.enable, server);
        applySetting("$enable_on_fire", DynamicLightsConfig.enableOnFire, server);
        applySetting("$enable_glowing", DynamicLightsConfig.enableGlowing, server);
        applySetting("$enable_ghast", DynamicLightsConfig.enableGhast, server);
        applySetting("$enchanted_items", DynamicLightsConfig.enableEnchantedItems, server);
        applySetting("$amethyst_trimmed", DynamicLightsConfig.enableAmethystTrimmed, server);
        applySetting("$fire_aspect", DynamicLightsConfig.enableFireAspect, server);
        applySetting("$riptide", DynamicLightsConfig.enableRiptide, server);
        applySetting("$channeling", DynamicLightsConfig.enableChanneling, server);
        applySetting("$water_sensitive", DynamicLightsConfig.enableWaterSensitive, server);
        applySetting("$enable_sound", DynamicLightsConfig.enableSound, server);
        applySetting("$rain_sensitive", DynamicLightsConfig.enableRainSensitive, server);
    }

    private static void applySetting(String holder, DynamicLightsConfig.Setting value, MinecraftServer server) {
        if (value == DynamicLightsConfig.Setting.YES) {
            sendCommand("scoreboard players set " + holder + " ts.dl.settings 2", server);
        } else if (value == DynamicLightsConfig.Setting.NO) {
            sendCommand("scoreboard players set " + holder + " ts.dl.settings -1", server);
        } else {
            sendCommand("execute if score " + holder + " ts.dl.settings matches -1 run scoreboard players set " + holder + " ts.dl.settings 0", server);
            sendCommand("execute if score " + holder + " ts.dl.settings matches 2 run scoreboard players set " + holder + " ts.dl.settings 1", server);
        }
    }

    public static void sendCommand(String command, MinecraftServer server) {
        CommandSourceStack commandSource = server.createCommandSourceStack();
        try {
            server.getCommands().getDispatcher().execute(command, commandSource);
        } catch (CommandSyntaxException ignored) {
        }
    }
}
