package com.ultra.megamod.feature.dungeons.generation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/**
 * Configuration for the DungeonMobFeature.
 * Specifies which entity type to spawn at the feature's placement position.
 */
public record DungeonMobFeatureConfig(String entityType) implements FeatureConfiguration {

    public static final Codec<DungeonMobFeatureConfig> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("entity_type").forGetter(DungeonMobFeatureConfig::entityType)
    ).apply(inst, DungeonMobFeatureConfig::new));
}
