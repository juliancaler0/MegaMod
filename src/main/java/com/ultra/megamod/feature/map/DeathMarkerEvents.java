package com.ultra.megamod.feature.map;

import com.ultra.megamod.feature.computer.network.handlers.MapHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber(modid = "megamod")
public class DeathMarkerEvents {

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerLevel level = (ServerLevel) player.level();
        String dimension = level.dimension().identifier().toString();
        MapHandler.addDeathMarker(
            player.getUUID(),
            player.getBlockX(),
            player.getBlockY(),
            player.getBlockZ(),
            dimension,
            level
        );
    }
}
