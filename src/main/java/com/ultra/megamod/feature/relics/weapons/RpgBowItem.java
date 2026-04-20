package com.ultra.megamod.feature.relics.weapons;

import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;

public class RpgBowItem extends BowItem {
    private final String weaponName;
    private final float baseDamage;

    public RpgBowItem(String weaponName, float baseDamage, Item.Properties properties) {
        super(properties);
        this.weaponName = weaponName;
        this.baseDamage = baseDamage;
    }

    public String getWeaponName() { return weaponName; }
    public float getBaseDamage() { return baseDamage; }
}
