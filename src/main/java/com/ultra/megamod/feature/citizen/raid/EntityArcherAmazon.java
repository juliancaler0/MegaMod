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
 * Amazon archer — bow, leather armor, fast fire rate.
 */
public class EntityArcherAmazon extends AbstractRaiderEntity {

    public EntityArcherAmazon(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        setCulture(RaiderCulture.AMAZON);
        equipItem(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        equipItem(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
        equipItem(EquipmentSlot.CHEST, new ItemStack(Items.LEATHER_CHESTPLATE));
        equipItem(EquipmentSlot.LEGS, new ItemStack(Items.LEATHER_LEGGINGS));
        equipItem(EquipmentSlot.FEET, new ItemStack(Items.LEATHER_BOOTS));
    }

    @Override
    protected void registerCombatGoals() {
        // Fast fire rate (22 ticks) — Amazons are swift archers
        this.goalSelector.addGoal(1, new RaiderRangedAI(this, 1.1, 22, 24.0f));
        this.goalSelector.addGoal(3, new RaiderMeleeAI(this, 1.1, true));
    }

    public static AttributeSupplier.Builder createArcherAmazonAttributes() {
        return createRaiderAttributes()
                .add(Attributes.MAX_HEALTH, 18.0)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.ARMOR, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.35);
    }
}
