package com.ultra.megamod.feature.combat.rogues.item.armor;

import com.ultra.megamod.feature.relics.weapons.RpgArmorItem;

/**
 * Rogue armor item type marker.
 * Ported from net.rogues.item.armor.RogueArmor.
 *
 * In the Fabric source, this extended Armor.CustomItem (SpellEngine RPG Series).
 * In MegaMod, rogue armor uses {@link RpgArmorItem} registered through ClassArmorRegistry.
 * This class is retained as a type marker for identification.
 */
public class RogueArmor {
    // Marker class - actual armor items are registered in ClassArmorRegistry
    // as RpgArmorItem with ROGUE_ARMOR_ASSET, ASSASSIN_ARMOR_ASSET, etc.
}
