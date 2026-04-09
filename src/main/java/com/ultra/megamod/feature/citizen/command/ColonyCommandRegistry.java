package com.ultra.megamod.feature.citizen.command;

import com.ultra.megamod.MegaMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Event handler for RegisterCommandsEvent.
 * Registers the unified /mc command tree (colony, citizens, kill subcommands).
 */
@EventBusSubscriber(modid = MegaMod.MODID)
public class ColonyCommandRegistry {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        ColonyCommand.register(event.getDispatcher());
    }
}
