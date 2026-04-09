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
 * Amazon spearman — melee, trident, leather armor.
 */
public class EntityAmazonSpearman extends AbstractRaiderEntity {

    public EntityAmazonSpearman(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        setCulture(RaiderCulture.AMAZON);
        equipItem(EquipmentSlot.MAINHAND, new ItemStack(Items.TRIDENT));
        equipItem(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
        equipItem(EquipmentSlot.CHEST, new ItemStack(Items.LEATHER_CHESTPLATE));
        equipItem(EquipmentSlot.LEGS, new ItemStack(Items.LEATHER_LEGGINGS));
        equipItem(EquipmentSlot.FEET, new ItemStack(Items.LEATHER_BOOTS));
    }

    @Override
    protected void registerCombatGoals() {
        this.goalSelector.addGoal(1, new RaiderMeleeAI(this, 1.1, true, 0.9));
    }

    public static AttributeSupplier.Builder createAmazonSpearmanAttributes() {
        return createRaiderAttributes()
                .add(Attributes.MAX_HEALTH, 22.0)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.ARMOR, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.34);
    }
}
