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
 * Norsemen archer — bow, iron armor.
 */
public class EntityNorsemenArcher extends AbstractRaiderEntity {

    public EntityNorsemenArcher(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        setCulture(RaiderCulture.NORSEMEN);
        equipItem(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        equipItem(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        equipItem(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
        equipItem(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
        equipItem(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
    }

    @Override
    protected void registerCombatGoals() {
        this.goalSelector.addGoal(1, new RaiderRangedAI(this, 1.0, 28, 22.0f));
        this.goalSelector.addGoal(3, new RaiderMeleeAI(this, 1.0, true));
    }

    public static AttributeSupplier.Builder createNorsemenArcherAttributes() {
        return createRaiderAttributes()
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.ARMOR, 6.0);
    }
}
