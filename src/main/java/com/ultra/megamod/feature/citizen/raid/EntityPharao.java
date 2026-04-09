package com.ultra.megamod.feature.citizen.raid;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Egyptian pharaoh — melee + magic, diamond sword, gold armor, 3x health.
 * Applies Weakness to targets on hit.
 */
public class EntityPharao extends AbstractRaiderEntity {

    private int magicCooldown = 0;

    public EntityPharao(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        setCulture(RaiderCulture.EGYPTIAN);
        equipItem(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
        equipItem(EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET));
        equipItem(EquipmentSlot.CHEST, new ItemStack(Items.GOLDEN_CHESTPLATE));
        equipItem(EquipmentSlot.LEGS, new ItemStack(Items.GOLDEN_LEGGINGS));
        equipItem(EquipmentSlot.FEET, new ItemStack(Items.GOLDEN_BOOTS));
    }

    @Override
    protected void registerCombatGoals() {
        this.goalSelector.addGoal(1, new RaiderMeleeAI(this, 1.0, true, 0.9));
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, net.minecraft.world.entity.Entity target) {
        boolean hit = super.doHurtTarget(level, target);
        if (hit && target instanceof LivingEntity living) {
            // Apply Weakness and Slowness on hit (magic curse)
            living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 0));
        }
        return hit;
    }

    @Override
    public void tick() {
        super.tick();
        // Periodically apply Regeneration to self
        if (!level().isClientSide() && magicCooldown-- <= 0) {
            magicCooldown = 200; // Every 10 seconds
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0));
        }
    }

    public static AttributeSupplier.Builder createPharaoAttributes() {
        return createRaiderAttributes()
                .add(Attributes.MAX_HEALTH, 72.0)  // 3x base
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.ARMOR, 8.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6)
                .add(Attributes.MOVEMENT_SPEED, 0.28);
    }
}
