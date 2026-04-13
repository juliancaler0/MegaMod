package com.ultra.megamod.feature.combat.rogues.item.armor;

import com.ultra.megamod.feature.relics.weapons.RpgArmorItem;

/**
 * Warrior armor item type marker.
 * Ported from net.rogues.item.armor.WarriorArmor.
 *
 * In the Fabric source, this extended Armor.CustomItem (SpellEngine RPG Series).
 * In MegaMod, warrior armor uses {@link RpgArmorItem} registered through ClassArmorRegistry.
 * This class is retained as a type marker for identification.
 */
public class WarriorArmor {
    // Marker class - actual armor items are registered in ClassArmorRegistry
    // as RpgArmorItem with WARRIOR_ARMOR_ASSET, BERSERKER_ARMOR_ASSET, etc.
}
