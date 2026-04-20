package com.ultra.megamod.feature.relics.weapons;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShieldItem;

public class RpgShieldItem extends ShieldItem {
    private final String shieldName;
    private final float baseDamage;

    public RpgShieldItem(String shieldName, float baseDamage, Item.Properties properties) {
        super(properties);
        this.shieldName = shieldName;
        this.baseDamage = baseDamage;
    }

    public String getShieldName() { return shieldName; }
    public float getBaseDamage() { return baseDamage; }
}
