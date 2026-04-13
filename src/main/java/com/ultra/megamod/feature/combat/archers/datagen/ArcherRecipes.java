package com.ultra.megamod.feature.combat.archers.datagen;

import com.ultra.megamod.feature.combat.archers.block.ArcherBlocks;
import com.ultra.megamod.feature.combat.archers.item.ArcherArmors;
import com.ultra.megamod.feature.combat.archers.item.ArcherWeapons;
import com.ultra.megamod.feature.combat.archers.item.misc.Misc;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import com.ultra.megamod.lib.spellengine.rpg_series.item.Armor;
import com.ultra.megamod.lib.spellengine.rpg_series.item.Weapon;

/**
 * Defines all crafting recipes for the Archers system.
 * In MegaMod NeoForge context, recipes are provided as JSON files rather than via datagen.
 * This class is retained for reference and documentation of the crafting patterns.
 */
public class ArcherRecipes {

    // ========================================
    // SPEAR RECIPES
    // ========================================
    // flint_spear: P=FLINT, #=STICK, pattern "  P" / " # " / "#  "
    // iron_spear: P=IRON_INGOT
    // golden_spear: P=GOLD_INGOT
    // diamond_spear: P=DIAMOND

    // ========================================
    // BOW RECIPES
    // ========================================
    // composite_longbow: " #X" / "B X" / " #X" (B=BONE, #=STICK, X=STRING)
    // mechanic_shortbow: " IX" / "R X" / " IX" (I=IRON, R=REDSTONE, X=STRING)
    // royal_longbow: " GX" / "D X" / " GX" (G=GOLD, D=DIAMOND, X=STRING)

    // ========================================
    // CROSSBOW RECIPES
    // ========================================
    // rapid_crossbow: "IRI" / "XTX" / " I " (I=IRON, R=REDSTONE, X=STRING, T=TRIPWIRE_HOOK)
    // heavy_crossbow: "IDI" / "XTX" / " I " (I=IRON, D=DIAMOND, X=STRING, T=TRIPWIRE_HOOK)

    // ========================================
    // ARMOR RECIPES
    // ========================================
    // Archer T1 (leather + chain): helmet "LLL"/"C C", chest "C C"/"LLL"/"LLL", etc.
    // Ranger T2 (leather + rabbit_hide + scute): helmet "SRS"/"L L", etc.

    // ========================================
    // OTHER RECIPES
    // ========================================
    // archers_workbench: "SAL" / "###" (S=STRING, A=ARROW, L=LEATHER, #=PLANKS)
    // auto_fire_hook: "C  " / "C I" / " R " (C=COPPER, I=IRON, R=REDSTONE)

    // ========================================
    // NETHERITE UPGRADES
    // ========================================
    // diamond_spear -> netherite_spear
    // royal_longbow -> netherite_longbow
    // mechanic_shortbow -> netherite_shortbow
    // rapid_crossbow -> netherite_rapid_crossbow
    // heavy_crossbow -> netherite_heavy_crossbow
    // ranger_armor T2 -> T3 (all pieces)
}
