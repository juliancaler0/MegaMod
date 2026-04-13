package com.ultra.megamod.feature.worldedit.mask;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

/** Matches positions whose biome matches the given biome resource key. */
public record BiomeMask(Identifier biomeId) implements Mask {
    @Override
    public boolean test(Level level, BlockPos pos) {
        var biomeHolder = level.getBiome(pos);
        var key = biomeHolder.unwrapKey();
        if (key.isEmpty()) return false;
        return key.get().identifier().equals(biomeId);
    }
}
