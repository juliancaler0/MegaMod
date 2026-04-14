package com.ultra.megamod.feature.combat.wizards.item.armor;

import com.ultra.megamod.lib.spellengine.rpg_series.item.Armor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.equipment.ArmorMaterial;

/**
 * Wizard robe armor item — extends the SpellEngine custom armor base.
 * Ported from {@code net.wizards.item.WizardArmor}.
 */
public class WizardArmor extends Armor.CustomItem {
    public WizardArmor(ArmorMaterial material, EquipmentSlot slot, Properties settings) {
        super(material, slot, settings);
    }
}
