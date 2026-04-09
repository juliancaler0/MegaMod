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
 * Pirate archer raider — crossbow, gold armor.
 */
public class EntityArcherPirate extends AbstractRaiderEntity {

    public EntityArcherPirate(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        setCulture(RaiderCulture.PIRATE);
        equipItem(EquipmentSlot.MAINHAND, new ItemStack(Items.CROSSBOW));
        equipItem(EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET));
        equipItem(EquipmentSlot.CHEST, new ItemStack(Items.GOLDEN_CHESTPLATE));
        equipItem(EquipmentSlot.LEGS, new ItemStack(Items.GOLDEN_LEGGINGS));
        equipItem(EquipmentSlot.FEET, new ItemStack(Items.GOLDEN_BOOTS));
    }

    @Override
    protected void registerCombatGoals() {
        this.goalSelector.addGoal(1, new RaiderRangedAI(this, 1.0, 25, 22.0f));
        this.goalSelector.addGoal(3, new RaiderMeleeAI(this, 1.0, true));
    }

    public static AttributeSupplier.Builder createArcherPirateAttributes() {
        return createRaiderAttributes()
                .add(Attributes.MAX_HEALTH, 22.0)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.ARMOR, 3.0);
    }
}
