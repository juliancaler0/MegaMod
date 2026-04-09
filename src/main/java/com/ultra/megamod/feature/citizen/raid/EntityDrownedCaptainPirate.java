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
 * Drowned pirate captain — melee, water breathing, diamond sword, 2x health.
 */
public class EntityDrownedCaptainPirate extends AbstractRaiderEntity {

    public EntityDrownedCaptainPirate(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        setCulture(RaiderCulture.DROWNED_PIRATE);
        equipItem(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
        equipItem(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        equipItem(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
        equipItem(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
        equipItem(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
    }

    @Override
    protected void registerCombatGoals() {
        this.goalSelector.addGoal(1, new RaiderMeleeAI(this, 1.1, true, 0.85));
    }

    public static AttributeSupplier.Builder createDrownedCaptainPirateAttributes() {
        return createRaiderAttributes()
                .add(Attributes.MAX_HEALTH, 60.0)  // 2x base
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.ARMOR, 8.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5)
                .add(Attributes.MOVEMENT_SPEED, 0.30);
    }
}
