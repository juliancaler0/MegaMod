package com.ultra.megamod.feature.combat.archers.item.armor;

import com.ultra.megamod.lib.spellengine.rpg_series.item.Armor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.equipment.ArmorMaterial;

public class ArcherArmor extends Armor.CustomItem {
    public ArcherArmor(ArmorMaterial material, EquipmentSlot slot, Properties settings) {
        super(material, slot, settings);
    }

    public static ArcherArmor archer(ArmorMaterial material, EquipmentSlot slot, Properties settings) {
        return new ArcherArmor(material, slot, settings);
    }

    public static ArcherArmor ranger(ArmorMaterial material, EquipmentSlot slot, Properties settings) {
        return new ArcherArmor(material, slot, settings);
    }
}
