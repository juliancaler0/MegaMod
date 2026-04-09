package com.ultra.megamod.feature.citizen.raid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

import java.util.Random;

/**
 * Dynamic difficulty scaling for colony raids.
 * Determines raider count, adjusts difficulty based on citizen deaths,
 * and selects raider culture based on biome.
 */
public class RaidDifficultyScaler {

    public static final int MIN_DIFFICULTY = 1;
    public static final int MAX_DIFFICULTY = 14;
    public static final int INITIAL_DIFFICULTY = 7;

    private static final Random random = new Random();

    /**
     * Calculate how many raiders to spawn for a given difficulty and citizen count.
     * Base formula: (difficulty * 1.5) + (citizenCount / 4), capped at 30.
     */
    public static int calculateRaiderCount(int difficulty, int citizenCount) {
        int base = (int) (difficulty * 1.5) + citizenCount / 4;
        return Math.max(4, Math.min(30, base));
    }

    /**
     * Adjust difficulty after a raid based on citizen casualties.
     * More citizen deaths = lower difficulty next time (mercy mechanic).
     * No deaths = difficulty ticks up.
     *
     * @param current       current difficulty level
     * @param citizenDeaths number of citizens killed during the raid
     * @return new difficulty level
     */
    public static int adjustDifficulty(int current, int citizenDeaths) {
        if (citizenDeaths >= 5) {
            // Heavy losses: reduce difficulty significantly
            return Math.max(MIN_DIFFICULTY, current - 3);
        } else if (citizenDeaths >= 2) {
            // Moderate losses: reduce difficulty slightly
            return Math.max(MIN_DIFFICULTY, current - 1);
        } else if (citizenDeaths == 0) {
            // No losses: increase difficulty
            return Math.min(MAX_DIFFICULTY, current + 1);
        }
        // 1 death: no change
        return current;
    }

    /**
     * Select a raider culture based on the biome at the given position.
     * - Desert/badlands/savanna biomes -> EGYPTIAN
     * - Ocean/beach/river biomes -> PIRATE or DROWNED_PIRATE
     * - Cold/snowy biomes -> NORSEMEN
     * - Jungle/swamp biomes -> AMAZON
     * - All other biomes -> BARBARIAN
     * Some randomness is added so any culture can appear anywhere occasionally.
     */
    public static RaiderCulture selectCulture(Level level, BlockPos pos) {
        // 15% chance of a completely random culture regardless of biome
        if (random.nextFloat() < 0.15f) {
            RaiderCulture[] values = RaiderCulture.values();
            return values[random.nextInt(values.length)];
        }

        Holder<Biome> biomeHolder = level.getBiome(pos);
        // Check biome by key
        if (biomeHolder.is(Biomes.DESERT) || biomeHolder.is(Biomes.BADLANDS)
                || biomeHolder.is(Biomes.ERODED_BADLANDS) || biomeHolder.is(Biomes.WOODED_BADLANDS)
                || biomeHolder.is(Biomes.SAVANNA) || biomeHolder.is(Biomes.SAVANNA_PLATEAU)) {
            return RaiderCulture.EGYPTIAN;
        }

        if (biomeHolder.is(Biomes.OCEAN) || biomeHolder.is(Biomes.DEEP_OCEAN)
                || biomeHolder.is(Biomes.WARM_OCEAN) || biomeHolder.is(Biomes.LUKEWARM_OCEAN)
                || biomeHolder.is(Biomes.COLD_OCEAN) || biomeHolder.is(Biomes.DEEP_COLD_OCEAN)
                || biomeHolder.is(Biomes.DEEP_LUKEWARM_OCEAN) || biomeHolder.is(Biomes.FROZEN_OCEAN)
                || biomeHolder.is(Biomes.DEEP_FROZEN_OCEAN) || biomeHolder.is(Biomes.BEACH)
                || biomeHolder.is(Biomes.STONY_SHORE) || biomeHolder.is(Biomes.RIVER)
                || biomeHolder.is(Biomes.FROZEN_RIVER) || biomeHolder.is(Biomes.MUSHROOM_FIELDS)) {
            // 50/50 between pirates and drowned pirates in water biomes
            return random.nextBoolean() ? RaiderCulture.PIRATE : RaiderCulture.DROWNED_PIRATE;
        }

        if (biomeHolder.is(Biomes.SNOWY_PLAINS) || biomeHolder.is(Biomes.SNOWY_TAIGA)
                || biomeHolder.is(Biomes.SNOWY_BEACH) || biomeHolder.is(Biomes.SNOWY_SLOPES)
                || biomeHolder.is(Biomes.ICE_SPIKES) || biomeHolder.is(Biomes.FROZEN_PEAKS)
                || biomeHolder.is(Biomes.JAGGED_PEAKS) || biomeHolder.is(Biomes.GROVE)
                || biomeHolder.is(Biomes.OLD_GROWTH_SPRUCE_TAIGA) || biomeHolder.is(Biomes.TAIGA)) {
            return RaiderCulture.NORSEMEN;
        }

        if (biomeHolder.is(Biomes.JUNGLE) || biomeHolder.is(Biomes.SPARSE_JUNGLE)
                || biomeHolder.is(Biomes.BAMBOO_JUNGLE) || biomeHolder.is(Biomes.SWAMP)
                || biomeHolder.is(Biomes.MANGROVE_SWAMP)) {
            return RaiderCulture.AMAZON;
        }

        // Default: Barbarians for plains, forests, etc.
        return RaiderCulture.BARBARIAN;
    }

    /**
     * Calculate wave configuration for a given difficulty and raider count.
     * Wave count scales with difficulty:
     * - Difficulty 1-5:  1 wave
     * - Difficulty 6-10: 2 waves
     * - Difficulty 11-14: 3 waves
     *
     * @param difficulty   current difficulty level (1-14)
     * @param raiderCount  total raiders to spread across waves
     * @return array of wave sizes
     */
    public static int[] calculateWaves(int difficulty, int raiderCount) {
        int maxWaves;
        if (difficulty >= 11) {
            maxWaves = 3;
        } else if (difficulty >= 6) {
            maxWaves = 2;
        } else {
            maxWaves = 1;
        }

        int[] waveSizes = new int[maxWaves];
        int remaining = raiderCount;

        for (int i = 0; i < maxWaves; i++) {
            if (i == maxWaves - 1) {
                // Last wave gets the remainder
                waveSizes[i] = remaining;
            } else {
                // Each wave gets a roughly equal share, slightly escalating
                int share = remaining / (maxWaves - i);
                waveSizes[i] = Math.max(2, share - 1);
                remaining -= waveSizes[i];
            }
        }

        return waveSizes;
    }

    /**
     * Returns whether raiders should break doors at this difficulty level.
     * Door breaking starts at difficulty 5+.
     *
     * @param difficulty current difficulty level
     * @return true if raiders should break doors
     */
    public static boolean shouldBreakDoors(int difficulty) {
        return difficulty >= 5;
    }
}
