package com.ultra.megamod.feature.adminmodules;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.computer.admin.AdminSystem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = MegaMod.MODID)
public class AdminModuleEvents {

    private static boolean initialized = false;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!initialized) {
            ServerLevel level = event.getServer().overworld();
            AdminModuleManager.get().loadFromDisk(level);
            initialized = true;
        }
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (!AdminSystem.isAdmin(player)) continue;
            ServerLevel level = (ServerLevel) player.level();
            AdminModuleManager.get().onServerTick(player, level);
        }
    }

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!AdminSystem.isAdmin(player)) return;
        AdminModuleManager.get().onDamage(player, event);
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!AdminSystem.isAdmin(player)) return;
        AdminModuleManager.get().onBreakSpeed(player, event);
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ServerLevel level = event.getServer().overworld();
        AdminModuleManager.get().saveToDisk(level);
        AdminModuleManager.reset();
        initialized = false;
    }
}
