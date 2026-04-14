package com.ultra.megamod.feature.worldedit.command;

import com.ultra.megamod.MegaMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Entry point: subscribes to RegisterCommandsEvent and registers every
 * WorldEdit command. All commands are prefixed {@code we_} so they never
 * collide with vanilla or other mods. Admin-only gating is enforced at
 * command dispatch time via {@link com.ultra.megamod.feature.worldedit.WorldEditPermissions}.
 */
@EventBusSubscriber(modid = MegaMod.MODID)
public final class WorldEditCommands {

    private WorldEditCommands() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        var disp = event.getDispatcher();
        SelectionCommands.register(disp);
        RegionCommands.register(disp);
        ClipboardCommands.register(disp);
        GenerationCommands.register(disp);
        HistoryCommands.register(disp);
        BrushCommands.register(disp);
        ToolCommands.register(disp);
        UtilityCommands.register(disp);
        NavigationCommands.register(disp);
        ChunkCommands.register(disp);
        BiomeCommands.register(disp);
        SchematicCommands.register(disp);
        PresetCommands.register(disp);
    }
}
