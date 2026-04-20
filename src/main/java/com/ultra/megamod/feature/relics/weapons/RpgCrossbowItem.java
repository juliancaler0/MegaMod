package com.ultra.megamod.feature.relics.weapons;

import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;

public class RpgCrossbowItem extends CrossbowItem {
    private final String weaponName;
    private final float baseDamage;

    public RpgCrossbowItem(String weaponName, float baseDamage, Item.Properties properties) {
        super(properties);
        this.weaponName = weaponName;
        this.baseDamage = baseDamage;
    }

    public String getWeaponName() { return weaponName; }
    public float getBaseDamage() { return baseDamage; }
}
