package com.ultra.megamod.feature.arena;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * PvE wave spawning logic for the Arena.
 */
public class ArenaWaveSpawner {

    // Mob spawn pads relative to arena origin (matching ArenaBuilder colosseum mob pads)
    // Center is at 26,26; mob pads at center +/- 12
    private static final int CENTER = 26;
    private static final int[][] SPAWN_OFFSETS = {
            {CENTER - 12, CENTER - 12}, {CENTER - 12, CENTER + 12},
            {CENTER + 12, CENTER - 12}, {CENTER + 12, CENTER + 12}
    };

    /**
     * Spawn mobs for the given wave in a PvE arena instance.
     */
    public static void spawnWave(ServerLevel level, ArenaManager.ArenaInstance instance) {
        int wave = instance.wave;
        int baseCount = 4 + wave * 2;
        // Cap at reasonable mob count
        int mobCount = Math.min(baseCount, 40);

        // Every 5th wave is a mini-boss wave
        boolean isBossWave = (wave % 5 == 0);

        int spawned = 0;
        if (isBossWave) {
            if (spawnMiniBoss(level, instance, wave)) spawned++;
            int supportCount = Math.min(wave / 5 * 2, 10);
            for (int i = 0; i < supportCount; i++) {
                if (spawnRegularMob(level, instance, wave, i)) spawned++;
            }
        } else {
            for (int i = 0; i < mobCount; i++) {
                if (spawnRegularMob(level, instance, wave, i)) spawned++;
            }
        }
        instance.mobsAlive = spawned;

        // Notify players + sync HUD
        for (java.util.UUID playerId : instance.players) {
            net.minecraft.server.level.ServerPlayer player = level.getServer().getPlayerList().getPlayer(playerId);
            if (player != null) {
                String waveMsg = isBossWave
                        ? "Wave " + wave + " - MINI-BOSS!"
                        : "Wave " + wave + " - " + instance.mobsAlive + " enemies!";
                player.sendSystemMessage(Component.literal(waveMsg)
                        .withStyle(isBossWave ? net.minecraft.ChatFormatting.RED : net.minecraft.ChatFormatting.GOLD));
                // Sync arena HUD
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,
                        new com.ultra.megamod.feature.arena.network.ArenaHudSyncPayload(true, wave, instance.mobsAlive));
            }
        }
    }

    private static boolean spawnRegularMob(ServerLevel level, ArenaManager.ArenaInstance instance, int wave, int index) {
        EntityType<? extends Mob> type = selectMobType(wave, index);
        Mob mob = (Mob) type.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (mob == null) return false;

        // Position at one of the 4 spawn pads
        int[] offset = SPAWN_OFFSETS[index % SPAWN_OFFSETS.length];
        BlockPos spawnPos = instance.origin.offset(offset[0], 1, offset[1]);
        // Add small random offset to avoid stacking
        double rx = (level.getRandom().nextDouble() - 0.5) * 4.0;
        double rz = (level.getRandom().nextDouble() - 0.5) * 4.0;
        mob.setPos(spawnPos.getX() + 0.5 + rx, spawnPos.getY(), spawnPos.getZ() + 0.5 + rz);

        // Scale health and damage based on wave
        double healthScale = 1.0 + wave * 0.15;
        double damageScale = 1.0 + wave * 0.15;
        applyScaling(mob, healthScale, damageScale);

        mob.setPersistenceRequired();
        level.addFreshEntity(mob);
        return true;
    }

    @SuppressWarnings("unchecked")
    private static EntityType<? extends Mob> selectMobType(int wave, int index) {
        if (wave <= 4) {
            // Waves 1-4: zombies and skeletons
            return index % 2 == 0 ? EntityType.ZOMBIE : EntityType.SKELETON;
        } else if (wave <= 9) {
            // Waves 6-9 (non-boss): creepers, spiders, witches
            int mod = index % 3;
            if (mod == 0) return EntityType.CREEPER;
            if (mod == 1) return EntityType.SPIDER;
            return EntityType.WITCH;
        } else {
            // Waves 11+: mix of all
            int mod = index % 5;
            switch (mod) {
                case 0: return EntityType.ZOMBIE;
                case 1: return EntityType.SKELETON;
                case 2: return EntityType.CREEPER;
                case 3: return EntityType.SPIDER;
                default: return EntityType.WITCH;
            }
        }
    }

    private static boolean spawnMiniBoss(ServerLevel level, ArenaManager.ArenaInstance instance, int wave) {
        EntityType<? extends Mob> type;
        String bossName;

        if (wave == 5) {
            type = EntityType.WITHER_SKELETON;
            bossName = "Wave 5 Guardian";
        } else if (wave == 10) {
            type = EntityType.ENDERMAN;
            bossName = "Wave 10 Guardian";
        } else if (wave == 15) {
            type = EntityType.RAVAGER;
            bossName = "Wave 15 Guardian";
        } else if (wave == 20) {
            type = EntityType.WARDEN;
            bossName = "Wave 20 Guardian";
        } else {
            // Generic boss for waves 25, 30, 35...
            type = EntityType.WITHER_SKELETON;
            bossName = "Wave " + wave + " Guardian";
        }

        Mob mob = (Mob) type.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (mob == null) return false;

        BlockPos center = instance.origin.offset(CENTER, 1, CENTER);
        mob.setPos(center.getX() + 0.5, center.getY(), center.getZ() + 0.5);

        // Mini-bosses get 3x HP scaling on top of wave scaling
        double healthScale = 3.0 * (1.0 + wave * 0.15);
        double damageScale = 1.5 * (1.0 + wave * 0.15);
        applyScaling(mob, healthScale, damageScale);

        mob.setCustomName(Component.literal(bossName).withStyle(net.minecraft.ChatFormatting.DARK_RED));
        mob.setCustomNameVisible(true);
        mob.setPersistenceRequired();

        level.addFreshEntity(mob);
        return true;
    }

    private static void applyScaling(Mob mob, double healthScale, double damageScale) {
        var healthAttr = mob.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            double baseHealth = healthAttr.getBaseValue();
            healthAttr.setBaseValue(baseHealth * healthScale);
            mob.setHealth((float) (baseHealth * healthScale));
        }

        var damageAttr = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damageAttr != null) {
            damageAttr.setBaseValue(damageAttr.getBaseValue() * damageScale);
        }
    }
}
