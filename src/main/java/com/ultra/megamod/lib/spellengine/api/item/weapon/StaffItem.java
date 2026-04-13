package com.ultra.megamod.lib.spellengine.api.item.weapon;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class StaffItem extends Item {

    public StaffItem(Item.Properties settings) {
        super(settings);
    }

    public StaffItem(com.ultra.megamod.lib.spellengine.rpg_series.item.Weapon.CustomMaterial material, Item.Properties settings) {
        super(settings);
    }

    // hurtEnemy is called when the item is used to hit an entity
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
    }
}
