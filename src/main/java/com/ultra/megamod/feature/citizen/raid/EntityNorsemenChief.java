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
 * Norsemen chief — melee, netherite sword, diamond armor, 2.5x health.
 */
public class EntityNorsemenChief extends AbstractRaiderEntity {

    public EntityNorsemenChief(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        setCulture(RaiderCulture.NORSEMEN);
        equipItem(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_SWORD));
        equipItem(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
        equipItem(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE));
        equipItem(EquipmentSlot.LEGS, new ItemStack(Items.DIAMOND_LEGGINGS));
        equipItem(EquipmentSlot.FEET, new ItemStack(Items.DIAMOND_BOOTS));
    }

    @Override
    protected void registerCombatGoals() {
        this.goalSelector.addGoal(1, new RaiderMeleeAI(this, 1.15, true, 0.75));
    }

    public static AttributeSupplier.Builder createNorsemenChiefAttributes() {
        return createRaiderAttributes()
                .add(Attributes.MAX_HEALTH, 60.0)  // 2.5x base
                .add(Attributes.ATTACK_DAMAGE, 9.0)
                .add(Attributes.ARMOR, 12.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6)
                .add(Attributes.MOVEMENT_SPEED, 0.32);
    }
}
