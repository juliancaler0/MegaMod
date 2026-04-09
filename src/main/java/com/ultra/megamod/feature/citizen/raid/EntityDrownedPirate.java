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
 * Drowned pirate — melee, water breathing, trident.
 */
public class EntityDrownedPirate extends AbstractRaiderEntity {

    public EntityDrownedPirate(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        setCulture(RaiderCulture.DROWNED_PIRATE);
        equipItem(EquipmentSlot.MAINHAND, new ItemStack(Items.TRIDENT));
    }

    @Override
    protected void registerCombatGoals() {
        this.goalSelector.addGoal(1, new RaiderMeleeAI(this, 1.0, true));
    }

    public static AttributeSupplier.Builder createDrownedPirateAttributes() {
        return createRaiderAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28);
    }
}
