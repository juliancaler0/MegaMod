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
 * Barbarian melee raider — iron sword, leather armor.
 */
public class EntityBarbarian extends AbstractRaiderEntity {

    public EntityBarbarian(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        setCulture(RaiderCulture.BARBARIAN);
        equipItem(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        equipItem(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
        equipItem(EquipmentSlot.CHEST, new ItemStack(Items.LEATHER_CHESTPLATE));
        equipItem(EquipmentSlot.LEGS, new ItemStack(Items.LEATHER_LEGGINGS));
        equipItem(EquipmentSlot.FEET, new ItemStack(Items.LEATHER_BOOTS));
    }

    @Override
    protected void registerCombatGoals() {
        this.goalSelector.addGoal(1, new RaiderMeleeAI(this, 1.0, true));
    }

    public static AttributeSupplier.Builder createBarbarianAttributes() {
        return createRaiderAttributes()
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.ARMOR, 3.0);
    }
}
