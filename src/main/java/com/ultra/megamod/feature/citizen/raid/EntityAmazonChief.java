package com.ultra.megamod.feature.citizen.raid;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Amazon chief — melee, diamond sword, chain armor, 2x health.
 */
public class EntityAmazonChief extends AbstractRaiderEntity {

    public EntityAmazonChief(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        setCulture(RaiderCulture.AMAZON);
        equipItem(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
        equipItem(EquipmentSlot.HEAD, new ItemStack(Items.CHAINMAIL_HELMET));
        equipItem(EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
        equipItem(EquipmentSlot.LEGS, new ItemStack(Items.CHAINMAIL_LEGGINGS));
        equipItem(EquipmentSlot.FEET, new ItemStack(Items.CHAINMAIL_BOOTS));
    }

    @Override
    protected void registerCombatGoals() {
        this.goalSelector.addGoal(1, new RaiderMeleeAI(this, 1.15, true, 0.8));
    }

    public static AttributeSupplier.Builder createAmazonChiefAttributes() {
        return createRaiderAttributes()
                .add(Attributes.MAX_HEALTH, 44.0)  // 2x base
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.ARMOR, 6.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.3)
                .add(Attributes.MOVEMENT_SPEED, 0.33);
    }
}
