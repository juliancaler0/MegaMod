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
 * Egyptian archer mummy — bow, chain armor.
 */
public class EntityArcherMummy extends AbstractRaiderEntity {

    public EntityArcherMummy(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        setCulture(RaiderCulture.EGYPTIAN);
        equipItem(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        equipItem(EquipmentSlot.HEAD, new ItemStack(Items.CHAINMAIL_HELMET));
        equipItem(EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
        equipItem(EquipmentSlot.LEGS, new ItemStack(Items.CHAINMAIL_LEGGINGS));
        equipItem(EquipmentSlot.FEET, new ItemStack(Items.CHAINMAIL_BOOTS));
    }

    @Override
    protected void registerCombatGoals() {
        this.goalSelector.addGoal(1, new RaiderRangedAI(this, 0.95, 35, 20.0f));
        this.goalSelector.addGoal(3, new RaiderMeleeAI(this, 0.95, true));
    }

    public static AttributeSupplier.Builder createArcherMummyAttributes() {
        return createRaiderAttributes()
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.26);
    }
}
