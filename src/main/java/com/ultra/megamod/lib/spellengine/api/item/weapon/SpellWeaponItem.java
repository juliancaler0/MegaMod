package com.ultra.megamod.lib.spellengine.api.item.weapon;

import net.minecraft.world.item.Item;

public class SpellWeaponItem extends Item {
    public SpellWeaponItem(Item.Properties settings) {
        super(settings);
    }

    public SpellWeaponItem(com.ultra.megamod.lib.spellengine.rpg_series.item.Weapon.CustomMaterial material, Item.Properties settings) {
        super(settings);
    }
}
