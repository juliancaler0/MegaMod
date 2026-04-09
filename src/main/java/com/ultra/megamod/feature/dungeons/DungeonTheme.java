package com.ultra.megamod.feature.dungeons;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public enum DungeonTheme {
    NETHER_FORTRESS(Blocks.NETHER_BRICKS, Blocks.RED_NETHER_BRICKS, Blocks.SOUL_LANTERN, "Nether Fortress",
            new MobWeight[]{
                    new MobWeight(MobType.DUNGEON_MOB, 25),
                    new MobWeight(MobType.UNDEAD_KNIGHT, 20),
                    new MobWeight(MobType.LANTERN, 15),
                    new MobWeight(MobType.UMVUTHANA, 10),
                    new MobWeight(MobType.DUNGEON_SLIME, 8),
                    new MobWeight(MobType.VANILLA_HUSK, 10),
                    new MobWeight(MobType.VANILLA_SKELETON, 10),
                    new MobWeight(MobType.SPAWNER_CARRIER, 2)
            }, BossPreference.WROUGHTNAUT),
    ICE_CAVERN(Blocks.PACKED_ICE, Blocks.BLUE_ICE, Blocks.SEA_LANTERN, "Ice Cavern",
            new MobWeight[]{
                    new MobWeight(MobType.DUNGEON_MOB, 20),
                    new MobWeight(MobType.HOLLOW, 18),
                    new MobWeight(MobType.GROTTOL, 15),
                    new MobWeight(MobType.RAT, 12),
                    new MobWeight(MobType.VANILLA_STRAY, 12),
                    new MobWeight(MobType.VANILLA_SPIDER, 10),
                    new MobWeight(MobType.NAGA, 3),
                    new MobWeight(MobType.SPAWNER_CARRIER, 2),
                    new MobWeight(MobType.LANTERN, 3)
            }, BossPreference.FROSTMAW),
    ANCIENT_TEMPLE(Blocks.SANDSTONE, Blocks.CHISELED_SANDSTONE, Blocks.GLOWSTONE, "Ancient Temple",
            new MobWeight[]{
                    new MobWeight(MobType.UMVUTHANA, 22),
                    new MobWeight(MobType.UNDEAD_KNIGHT, 18),
                    new MobWeight(MobType.DUNGEON_MOB, 15),
                    new MobWeight(MobType.NAGA, 5),
                    new MobWeight(MobType.RAT, 10),
                    new MobWeight(MobType.VANILLA_ZOMBIE, 10),
                    new MobWeight(MobType.VANILLA_SKELETON, 8),
                    new MobWeight(MobType.SPAWNER_CARRIER, 2),
                    new MobWeight(MobType.FOLIAATH, 3),
                    new MobWeight(MobType.BLUFF, 5)
            }, BossPreference.UMVUTHI),
    VOID_CASTLE(Blocks.PURPUR_BLOCK, Blocks.END_STONE_BRICKS, Blocks.END_ROD, "Void Castle",
            new MobWeight[]{
                    new MobWeight(MobType.HOLLOW, 22),
                    new MobWeight(MobType.DUNGEON_MOB, 18),
                    new MobWeight(MobType.LANTERN, 12),
                    new MobWeight(MobType.DUNGEON_SLIME, 12),
                    new MobWeight(MobType.GROTTOL, 8),
                    new MobWeight(MobType.VANILLA_SKELETON, 10),
                    new MobWeight(MobType.VANILLA_CAVE_SPIDER, 8),
                    new MobWeight(MobType.SPAWNER_CARRIER, 5),
                    new MobWeight(MobType.NAGA, 2)
            }, BossPreference.CHAOS_SPAWNER),
    OVERGROWN_RUIN(Blocks.MOSSY_STONE_BRICKS, Blocks.MOSS_BLOCK, Blocks.SHROOMLIGHT, "Overgrown Ruin",
            new MobWeight[]{
                    new MobWeight(MobType.RAT, 20),
                    new MobWeight(MobType.FOLIAATH, 18),
                    new MobWeight(MobType.NAGA, 5),
                    new MobWeight(MobType.DUNGEON_SLIME, 12),
                    new MobWeight(MobType.DUNGEON_MOB, 10),
                    new MobWeight(MobType.VANILLA_ZOMBIE, 10),
                    new MobWeight(MobType.VANILLA_SPIDER, 8),
                    new MobWeight(MobType.GROTTOL, 5),
                    new MobWeight(MobType.SPAWNER_CARRIER, 2),
                    new MobWeight(MobType.BLUFF, 5),
                    new MobWeight(MobType.BABY_FOLIAATH, 5)
            }, BossPreference.OSSUKAGE);

    private final Block wallBlock;
    private final Block accentBlock;
    private final Block lightBlock;
    private final String displayName;
    private final MobWeight[] mobWeights;
    private final BossPreference bossPreference;

    private DungeonTheme(Block wallBlock, Block accentBlock, Block lightBlock, String displayName,
                         MobWeight[] mobWeights, BossPreference bossPreference) {
        this.wallBlock = wallBlock;
        this.accentBlock = accentBlock;
        this.lightBlock = lightBlock;
        this.displayName = displayName;
        this.mobWeights = mobWeights;
        this.bossPreference = bossPreference;
    }

    public Block getWallBlock() {
        return this.wallBlock;
    }

    public Block getAccentBlock() {
        return this.accentBlock;
    }

    public Block getLightBlock() {
        return this.lightBlock;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public MobWeight[] getMobWeights() {
        return this.mobWeights;
    }

    public BossPreference getBossPreference() {
        return this.bossPreference;
    }

    /**
     * Rolls a random mob type based on the theme's weighted mob table.
     */
    public MobType rollMobType(RandomSource random) {
        int totalWeight = 0;
        for (MobWeight mw : this.mobWeights) {
            totalWeight += mw.weight;
        }
        int roll = random.nextInt(totalWeight);
        int cumulative = 0;
        for (MobWeight mw : this.mobWeights) {
            cumulative += mw.weight;
            if (roll < cumulative) return mw.type;
        }
        return MobType.DUNGEON_MOB; // fallback
    }

    public static DungeonTheme random(RandomSource random) {
        DungeonTheme[] themes = DungeonTheme.values();
        return themes[random.nextInt(themes.length)];
    }

    public Block getCrackedWallBlock() {
        return switch (this) {
            case NETHER_FORTRESS -> Blocks.CRACKED_NETHER_BRICKS;
            case ICE_CAVERN -> Blocks.BLUE_ICE;
            case ANCIENT_TEMPLE -> Blocks.CUT_SANDSTONE;
            case VOID_CASTLE -> Blocks.PURPUR_PILLAR;
            case OVERGROWN_RUIN -> Blocks.MOSS_BLOCK;
        };
    }

    public Block getMossyWallBlock() {
        return switch (this) {
            case NETHER_FORTRESS -> Blocks.CHISELED_NETHER_BRICKS;
            case ICE_CAVERN -> Blocks.SNOW_BLOCK;
            case ANCIENT_TEMPLE -> Blocks.SMOOTH_SANDSTONE;
            case VOID_CASTLE -> Blocks.END_STONE;
            case OVERGROWN_RUIN -> Blocks.ROOTED_DIRT;
        };
    }

    public static DungeonTheme fromName(String name) {
        for (DungeonTheme theme : DungeonTheme.values()) {
            if (!theme.name().equalsIgnoreCase(name)) continue;
            return theme;
        }
        return NETHER_FORTRESS;
    }

    public enum MobType {
        DUNGEON_MOB,
        UNDEAD_KNIGHT,
        DUNGEON_SLIME,
        HOLLOW,
        RAT,
        VANILLA_ZOMBIE,
        VANILLA_HUSK,
        VANILLA_SKELETON,
        VANILLA_STRAY,
        VANILLA_SPIDER,
        VANILLA_CAVE_SPIDER,
        NAGA,
        GROTTOL,
        LANTERN,
        FOLIAATH,
        UMVUTHANA,
        SPAWNER_CARRIER,
        BLUFF,
        BABY_FOLIAATH
    }

    public enum BossPreference {
        WRAITH,            // 75% Wraith, 12.5% Ossukage, 12.5% Keeper
        OSSUKAGE,          // 75% Ossukage, 12.5% Wraith, 12.5% Keeper
        DUNGEON_KEEPER,    // 75% Keeper, 12.5% Wraith, 12.5% Ossukage
        WRAITH_OR_KEEPER,  // 50% Wraith, 50% Keeper
        FROSTMAW,          // 75% Frostmaw, 12.5% each other
        WROUGHTNAUT,       // 75% Wroughtnaut, 12.5% each other
        UMVUTHI,           // 75% Umvuthi, 12.5% each other
        CHAOS_SPAWNER,     // 75% ChaosSpawner, 12.5% each other
        SCULPTOR           // 75% Sculptor, 12.5% each other
    }

    public record MobWeight(MobType type, int weight) {}
}
