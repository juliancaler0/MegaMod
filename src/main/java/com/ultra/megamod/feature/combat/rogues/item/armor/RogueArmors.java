package com.ultra.megamod.feature.combat.rogues.item.armor;

import com.ultra.megamod.feature.combat.items.ClassArmorRegistry;
import com.ultra.megamod.feature.relics.weapons.RpgArmorItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Convenience references to Rogues & Warriors armor items.
 * Ported from net.rogues.item.armor.RogueArmors.
 *
 * All armor is registered through {@link ClassArmorRegistry}. This class
 * provides typed references for rogues-specific code.
 *
 * Armor stat values from the source:
 * - Rogue T1: evasion +3%, haste +4% per piece
 * - Assassin T2: evasion +4%, haste +5%, damage +2%, crit chance +2% per piece
 * - Netherite Assassin T3: evasion +5%, haste +5%, damage +5%, crit chance +2.5% per piece
 * - Warrior T1: damage +4% per piece
 * - Berserker T2: damage +5%, knockback +0.1, crit damage +4% per piece
 * - Netherite Berserker T3: damage +5%, toughness +1, knockback +0.1, crit damage +5% per piece
 */
public class RogueArmors {

    // Rogue T1
    public static final DeferredItem<RpgArmorItem> ROGUE_HEAD = ClassArmorRegistry.ROGUE_ARMOR_HEAD;
    public static final DeferredItem<RpgArmorItem> ROGUE_CHEST = ClassArmorRegistry.ROGUE_ARMOR_CHEST;
    public static final DeferredItem<RpgArmorItem> ROGUE_LEGS = ClassArmorRegistry.ROGUE_ARMOR_LEGS;
    public static final DeferredItem<RpgArmorItem> ROGUE_FEET = ClassArmorRegistry.ROGUE_ARMOR_BOOTS;

    // Assassin T2
    public static final DeferredItem<RpgArmorItem> ASSASSIN_HEAD = ClassArmorRegistry.ASSASSIN_ARMOR_HEAD;
    public static final DeferredItem<RpgArmorItem> ASSASSIN_CHEST = ClassArmorRegistry.ASSASSIN_ARMOR_CHEST;
    public static final DeferredItem<RpgArmorItem> ASSASSIN_LEGS = ClassArmorRegistry.ASSASSIN_ARMOR_LEGS;
    public static final DeferredItem<RpgArmorItem> ASSASSIN_FEET = ClassArmorRegistry.ASSASSIN_ARMOR_BOOTS;

    // Netherite Assassin T3
    public static final DeferredItem<RpgArmorItem> NETHERITE_ASSASSIN_HEAD = ClassArmorRegistry.NETHERITE_ASSASSIN_ARMOR_HEAD;
    public static final DeferredItem<RpgArmorItem> NETHERITE_ASSASSIN_CHEST = ClassArmorRegistry.NETHERITE_ASSASSIN_ARMOR_CHEST;
    public static final DeferredItem<RpgArmorItem> NETHERITE_ASSASSIN_LEGS = ClassArmorRegistry.NETHERITE_ASSASSIN_ARMOR_LEGS;
    public static final DeferredItem<RpgArmorItem> NETHERITE_ASSASSIN_FEET = ClassArmorRegistry.NETHERITE_ASSASSIN_ARMOR_BOOTS;

    // Warrior T1
    public static final DeferredItem<RpgArmorItem> WARRIOR_HEAD = ClassArmorRegistry.WARRIOR_ARMOR_HEAD;
    public static final DeferredItem<RpgArmorItem> WARRIOR_CHEST = ClassArmorRegistry.WARRIOR_ARMOR_CHEST;
    public static final DeferredItem<RpgArmorItem> WARRIOR_LEGS = ClassArmorRegistry.WARRIOR_ARMOR_LEGS;
    public static final DeferredItem<RpgArmorItem> WARRIOR_FEET = ClassArmorRegistry.WARRIOR_ARMOR_BOOTS;

    // Berserker T2
    public static final DeferredItem<RpgArmorItem> BERSERKER_HEAD = ClassArmorRegistry.BERSERKER_ARMOR_HEAD;
    public static final DeferredItem<RpgArmorItem> BERSERKER_CHEST = ClassArmorRegistry.BERSERKER_ARMOR_CHEST;
    public static final DeferredItem<RpgArmorItem> BERSERKER_LEGS = ClassArmorRegistry.BERSERKER_ARMOR_LEGS;
    public static final DeferredItem<RpgArmorItem> BERSERKER_FEET = ClassArmorRegistry.BERSERKER_ARMOR_BOOTS;

    // Netherite Berserker T3
    public static final DeferredItem<RpgArmorItem> NETHERITE_BERSERKER_HEAD = ClassArmorRegistry.NETHERITE_BERSERKER_ARMOR_HEAD;
    public static final DeferredItem<RpgArmorItem> NETHERITE_BERSERKER_CHEST = ClassArmorRegistry.NETHERITE_BERSERKER_ARMOR_CHEST;
    public static final DeferredItem<RpgArmorItem> NETHERITE_BERSERKER_LEGS = ClassArmorRegistry.NETHERITE_BERSERKER_ARMOR_LEGS;
    public static final DeferredItem<RpgArmorItem> NETHERITE_BERSERKER_FEET = ClassArmorRegistry.NETHERITE_BERSERKER_ARMOR_BOOTS;
}
