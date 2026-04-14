package com.ultra.megamod.feature.combat.rogues.item.armor;

import com.ultra.megamod.lib.spellengine.rpg_series.item.Armor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.equipment.ArmorMaterial;

/**
 * Rogue armor item type — extends SpellEngine's {@link Armor.CustomItem} so armor-slot
 * bonus MODIFIER spells (evasion, haste, crit chance, etc.) are picked up by the
 * equipment attribute resolver.
 */
public class RogueArmor extends Armor.CustomItem {
    public RogueArmor(ArmorMaterial material, EquipmentSlot slot, Properties settings) {
        super(material, slot, settings);
    }
}
