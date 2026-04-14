package com.ultra.megamod.feature.combat.rogues.item;

import com.ultra.megamod.feature.combat.items.ClassWeaponRegistry;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Convenience references to Rogues & Warriors weapon items.
 * Ported from net.rogues.item.RogueWeapons.
 *
 * Rogue/Warrior weapons are still registered through {@link ClassWeaponRegistry}
 * via NeoForge DeferredRegister (with plain {@code RpgWeaponItem}, not yet
 * SpellEngine-backed). Wand/staff/claymore/etc. entries migrated to SpellEngine
 * are exposed via {@code ClassWeaponRegistry.Lookup} shims, but rogue/warrior
 * weapons remain on the legacy path for now.
 */
public class RogueWeapons {

    // DAGGERS
    public static final DeferredItem<? extends Item> flint_dagger = ClassWeaponRegistry.FLINT_DAGGER;
    public static final DeferredItem<? extends Item> iron_dagger = ClassWeaponRegistry.IRON_DAGGER;
    public static final DeferredItem<? extends Item> golden_dagger = ClassWeaponRegistry.GOLDEN_DAGGER;
    public static final DeferredItem<? extends Item> diamond_dagger = ClassWeaponRegistry.DIAMOND_DAGGER;
    public static final DeferredItem<? extends Item> netherite_dagger = ClassWeaponRegistry.NETHERITE_DAGGER;

    // SICKLES
    public static final DeferredItem<? extends Item> iron_sickle = ClassWeaponRegistry.IRON_SICKLE;
    public static final DeferredItem<? extends Item> golden_sickle = ClassWeaponRegistry.GOLDEN_SICKLE;
    public static final DeferredItem<? extends Item> diamond_sickle = ClassWeaponRegistry.DIAMOND_SICKLE;
    public static final DeferredItem<? extends Item> netherite_sickle = ClassWeaponRegistry.NETHERITE_SICKLE;

    // DOUBLE AXES
    public static final DeferredItem<? extends Item> stone_double_axe = ClassWeaponRegistry.STONE_DOUBLE_AXE;
    public static final DeferredItem<? extends Item> iron_double_axe = ClassWeaponRegistry.IRON_DOUBLE_AXE;
    public static final DeferredItem<? extends Item> golden_double_axe = ClassWeaponRegistry.GOLDEN_DOUBLE_AXE;
    public static final DeferredItem<? extends Item> diamond_double_axe = ClassWeaponRegistry.DIAMOND_DOUBLE_AXE;
    public static final DeferredItem<? extends Item> netherite_double_axe = ClassWeaponRegistry.NETHERITE_DOUBLE_AXE;

    // GLAIVES
    public static final DeferredItem<? extends Item> iron_glaive = ClassWeaponRegistry.IRON_GLAIVE;
    public static final DeferredItem<? extends Item> golden_glaive = ClassWeaponRegistry.GOLDEN_GLAIVE;
    public static final DeferredItem<? extends Item> diamond_glaive = ClassWeaponRegistry.DIAMOND_GLAIVE;
    public static final DeferredItem<? extends Item> netherite_glaive = ClassWeaponRegistry.NETHERITE_GLAIVE;
}
