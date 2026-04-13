package com.ultra.megamod.feature.combat.rogues.config;

/**
 * Default configuration values for Rogues & Warriors equipment and villages.
 * Ported from net.rogues.config.Default.
 *
 * In the Fabric source, this configured SpellEngine's ConfigFile.Equipment and
 * StructurePoolConfig. In MegaMod, items and villages are registered through
 * the existing ClassWeaponRegistry, ClassArmorRegistry, and CombatVillagerRegistry.
 * This class is retained for reference / future config integration.
 */
public class Default {

    /** Village barracks structure weight (how commonly it spawns). */
    public static final int VILLAGE_WEIGHT = 6;

    /** Village barracks structure limit per village. */
    public static final int VILLAGE_LIMIT = 1;

    /** Village biome structure paths (megamod namespace). */
    public static final String[] VILLAGE_BIOMES = {
            "minecraft:village/desert/houses",
            "minecraft:village/savanna/houses",
            "minecraft:village/plains/houses",
            "minecraft:village/taiga/houses",
            "minecraft:village/snowy/houses"
    };
}
