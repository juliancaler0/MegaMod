package com.ultra.megamod.feature.skills;

import com.ultra.megamod.feature.computer.network.handlers.SettingsHandler;
import com.ultra.megamod.feature.skills.prestige.PrestigeManager;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Vector3f;

@EventBusSubscriber(modid = "megamod")
public class SkillCosmetics {

    private static final DustParticleOptions MINING_DUST = new DustParticleOptions(0xB36619, 1.0f);

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer().getTickCount() % 10 != 0) return;

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (!SettingsHandler.isEnabled(player.getUUID(), "skill_particles")) continue;

            ServerLevel overworld = player.level().getServer().overworld();
            ServerLevel playerLevel = (ServerLevel) player.level();
            SkillManager manager = SkillManager.get(overworld);
            Set<String> unlocked = manager.getUnlockedNodes(player.getUUID());
            if (unlocked.isEmpty()) continue;

            // Find highest tier across all branches
            SkillBranch bestBranch = null;
            int bestTier = 0;
            for (SkillBranch branch : SkillBranch.values()) {
                for (int t = 5; t >= 4; t--) {
                    String nodeId = branch.name().toLowerCase() + "_" + t;
                    if (unlocked.contains(nodeId)) {
                        if (t > bestTier) {
                            bestTier = t;
                            bestBranch = branch;
                        }
                        break;
                    }
                }
            }

            PrestigeManager prestige = PrestigeManager.get(overworld);
            int totalPrestige = prestige.getTotalPrestige(player.getUUID());

            if (bestTier < 4 || bestBranch == null) {
                if (totalPrestige > 0) {
                    spawnPrestigeParticles(player, playerLevel, totalPrestige);
                }
                continue;
            }

            // Spawn tree-specific particles — visible to others, not self
            double px = player.getX();
            double py = player.getY() + 0.1;
            double pz = player.getZ();
            int count = bestTier >= 5 ? 5 : 2;
            double ySpeed = bestTier >= 5 ? 0.05 : 0.01;

            SkillTreeType tree = bestBranch.getTreeType();
            spawnTreeParticlesForViewers(playerLevel, player, tree, px, py, pz, count, ySpeed);

            if (totalPrestige > 0) {
                spawnPrestigeParticles(player, playerLevel, totalPrestige);
            }
        }
    }

    private static void spawnTreeParticlesForViewers(ServerLevel level, ServerPlayer self, SkillTreeType tree,
                                                     double px, double py, double pz, int count, double ySpeed) {
        // Get nearby players excluding self
        List<ServerPlayer> viewers = level.getPlayers(p -> p != self && p.distanceTo(self) < 32);
        if (viewers.isEmpty()) return;

        ParticleOptions particle = getTreeParticle(tree);
        double yOffset = tree == SkillTreeType.ARCANE ? 0.5 : (tree == SkillTreeType.COMBAT || tree == SkillTreeType.FARMING ? 0.3 : 0.2);

        for (int i = 0; i < count; i++) {
            double ox = (level.random.nextDouble() - 0.5) * 0.8;
            double oz = (level.random.nextDouble() - 0.5) * 0.8;
            // sendParticles broadcasts to all players in range — we send individual particles at slightly randomized positions
            level.sendParticles(particle, px + ox, py + yOffset, pz + oz, 1, 0, ySpeed, 0, 0);
        }
    }

    private static ParticleOptions getTreeParticle(SkillTreeType tree) {
        return switch (tree) {
            case COMBAT -> ParticleTypes.CRIT;
            case MINING -> MINING_DUST;
            case FARMING -> ParticleTypes.HAPPY_VILLAGER;
            case ARCANE -> ParticleTypes.ENCHANT;
            case SURVIVAL -> ParticleTypes.CLOUD;
        };
    }

    private static void spawnPrestigeParticles(ServerPlayer self, ServerLevel level, int totalPrestige) {
        double px = self.getX();
        double py = self.getY() + 0.5;
        double pz = self.getZ();

        List<ServerPlayer> viewers = level.getPlayers(p -> p != self && p.distanceTo(self) < 32);
        if (viewers.isEmpty()) return;

        if (totalPrestige >= 25) {
            // Max prestige: full golden aura with END_ROD particles circling
            double angle = (level.getGameTime() % 40) * (Math.PI * 2 / 40);
            for (int i = 0; i < 3; i++) {
                double a = angle + (i * Math.PI * 2 / 3);
                double ox = Math.cos(a) * 0.7;
                double oz = Math.sin(a) * 0.7;
                level.sendParticles(ParticleTypes.END_ROD, px + ox, py + 0.5, pz + oz, 1, 0, 0.02, 0, 0);
            }
        } else {
            // Gold totem particles — 1 per prestige level per tick cycle
            int particleCount = Math.min(totalPrestige, 10);
            for (int i = 0; i < particleCount; i++) {
                double ox = (level.random.nextDouble() - 0.5) * 0.6;
                double oz = (level.random.nextDouble() - 0.5) * 0.6;
                double oy = level.random.nextDouble() * 0.5;
                level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, px + ox, py + oy, pz + oz, 1, 0, 0.02, 0, 0);
            }
        }
    }
}
