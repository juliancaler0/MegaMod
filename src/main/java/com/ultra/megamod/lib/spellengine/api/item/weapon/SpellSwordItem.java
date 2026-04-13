package com.ultra.megamod.lib.spellengine.api.item.weapon;

import net.minecraft.world.item.Item;

public class SpellSwordItem extends SpellWeaponItem {
    public SpellSwordItem(Item.Properties settings) {
        super(settings);
    }

    public SpellSwordItem(com.ultra.megamod.lib.spellengine.rpg_series.item.Weapon.CustomMaterial material, Item.Properties settings) {
        super(material, settings);
    }
}
