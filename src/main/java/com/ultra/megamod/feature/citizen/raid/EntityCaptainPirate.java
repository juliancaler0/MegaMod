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
 * Pirate captain — melee, diamond sword, diamond armor, 2x health.
 */
public class EntityCaptainPirate extends AbstractRaiderEntity {

    public EntityCaptainPirate(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        setCulture(RaiderCulture.PIRATE);
        equipItem(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
        equipItem(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
        equipItem(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE));
        equipItem(EquipmentSlot.LEGS, new ItemStack(Items.DIAMOND_LEGGINGS));
        equipItem(EquipmentSlot.FEET, new ItemStack(Items.DIAMOND_BOOTS));
    }

    @Override
    protected void registerCombatGoals() {
        this.goalSelector.addGoal(1, new RaiderMeleeAI(this, 1.1, true, 0.8));
    }

    public static AttributeSupplier.Builder createCaptainPirateAttributes() {
        return createRaiderAttributes()
                .add(Attributes.MAX_HEALTH, 52.0)  // 2x base
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5);
    }
}
