package com.ultra.megamod.feature.prestige;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.computer.network.handlers.SettingsHandler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Spawns ambient particle effects for players with active prestige particle rewards.
 * Ticks every 20 server ticks (1 second).
 */
@EventBusSubscriber(modid = MegaMod.MODID)
public class PrestigeParticleHandler {

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer().getTickCount() % 20 != 0) return;

        ServerLevel overworld = event.getServer().overworld();
        PrestigeRewardManager prm = PrestigeRewardManager.get(overworld);

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            // Respect per-player particle settings toggle
            if (!SettingsHandler.isEnabled(player.getUUID(), "skill_particles")) continue;

            String particleId = prm.getActiveParticleId(player.getUUID());
            if (particleId.isEmpty()) continue;

            ServerLevel level = (ServerLevel) player.level();
            spawnPrestigeParticle(level, player, particleId);
        }
    }

    private static void spawnPrestigeParticle(ServerLevel level, ServerPlayer player, String id) {
        double x = player.getX();
        double y = player.getY() + 1.5;
        double z = player.getZ();

        switch (id) {
            case "flame_aura" -> level.sendParticles(ParticleTypes.FLAME, x, y, z, 3, 0.3, 0.5, 0.3, 0.01);
            case "enchant_aura" -> level.sendParticles(ParticleTypes.ENCHANT, x, y, z, 5, 0.5, 0.5, 0.5, 0.5);
            case "end_rod_trail" -> level.sendParticles(ParticleTypes.END_ROD, x, y, z, 2, 0.2, 0.3, 0.2, 0.01);
            case "soul_fire" -> level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 3, 0.3, 0.4, 0.3, 0.01);
            case "totem_burst" -> level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, x, y, z, 2, 0.3, 0.5, 0.3, 0.1);
            case "cherry_blossom" -> level.sendParticles(ParticleTypes.CHERRY_LEAVES, x, y + 1, z, 3, 0.5, 0.3, 0.5, 0.01);
            case "void_tendrils" -> level.sendParticles(ParticleTypes.PORTAL, x, y, z, 5, 0.4, 0.6, 0.4, 0.5);
            case "heart_trail" -> level.sendParticles(ParticleTypes.HEART, x, y + 0.5, z, 1, 0.3, 0.3, 0.3, 0.0);
        }
    }
}
