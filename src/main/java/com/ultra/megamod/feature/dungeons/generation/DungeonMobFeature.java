package com.ultra.megamod.feature.dungeons.generation;

import com.mojang.serialization.Codec;
import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.dungeons.DungeonTier;
import com.ultra.megamod.feature.dungeons.entity.DungeonEntityRegistry;
import com.ultra.megamod.feature.dungeons.entity.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

/**
 * A Feature that spawns a mob entity at the jigsaw position with tier scaling.
 * Used by configured_feature JSON files to place dungeon mobs via the jigsaw system.
 */
public class DungeonMobFeature extends Feature<DungeonMobFeatureConfig> {

    public DungeonMobFeature(Codec<DungeonMobFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<DungeonMobFeatureConfig> context) {
        WorldGenLevel worldGenLevel = context.level();
        if (!(worldGenLevel instanceof ServerLevel level)) return false;

        BlockPos pos = context.origin();
        String entityTypeName = context.config().entityType();

        // Default tier — will be overridden by DungeonManager after generation
        // The mob is spawned here as a placeholder; DungeonManager scales it post-generation
        DungeonTier tier = DungeonTier.NORMAL;

        try {
            switch (entityTypeName) {
                case "megamod:dungeon_mob" -> DungeonMobEntity.create(level, tier, pos);
                case "megamod:undead_knight" -> UndeadKnightEntity.create(level, tier, pos);
                case "megamod:dungeon_rat" -> RatEntity.create(level, tier, pos);
                case "megamod:hollow" -> HollowEntity.create(level, tier, pos);
                case "megamod:dungeon_slime" -> DungeonSlimeEntity.create(level, tier, pos);
                case "megamod:naga" -> NagaEntity.create(level, tier, pos);
                case "megamod:grottol" -> GrottolEntity.create(level, tier, pos);
                case "megamod:lantern" -> LanternEntity.create(level, tier, pos);
                case "megamod:foliaath" -> FoliaathEntity.create(level, tier, pos);
                case "megamod:umvuthana" -> UmvuthanaEntity.create(level, tier, pos);
                case "megamod:spawner_carrier" -> SpawnerCarrierEntity.create(level, tier, pos);
                case "megamod:bluff" -> BluffEntity.create(level, tier, pos);
                case "megamod:baby_foliaath" -> BabyFoliaathEntity.create(level, tier, pos);
                case "minecraft:zombie" -> spawnVanillaMob(level, tier, EntityType.ZOMBIE, pos);
                case "minecraft:skeleton" -> spawnVanillaMob(level, tier, EntityType.SKELETON, pos);
                case "minecraft:spider" -> spawnVanillaMob(level, tier, EntityType.SPIDER, pos);
                default -> MegaMod.LOGGER.warn("DungeonMobFeature: Unknown entity type {}", entityTypeName);
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("DungeonMobFeature: Failed to spawn {}", entityTypeName, e);
            return false;
        }

        return true;
    }

    private static void spawnVanillaMob(ServerLevel level, DungeonTier tier,
                                         EntityType<? extends Mob> type, BlockPos pos) {
        Mob mob = (Mob) type.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (mob == null) return;
        mob.setPos((double) pos.getX() + 0.5, pos.getY(), (double) pos.getZ() + 0.5);
        mob.setYRot(level.getRandom().nextFloat() * 360.0f);
        mob.setPersistenceRequired();
        float mult = tier.getDifficultyMultiplier();
        // HP scales with full multiplier, damage uses diminishing scaling
        if (mob.getAttribute(Attributes.MAX_HEALTH) != null) {
            double baseHP = mob.getAttribute(Attributes.MAX_HEALTH).getBaseValue();
            mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(baseHP * mult);
            mob.setHealth((float) (baseHP * mult));
        }
        if (mob.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            double baseDmg = mob.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue();
            float damageMult = 1.0f + (mult - 1.0f) * 0.5f;
            mob.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(baseDmg * damageMult);
        }
        tier.applyMobEffects(mob);
        level.addFreshEntity((Entity) mob);
    }
}
