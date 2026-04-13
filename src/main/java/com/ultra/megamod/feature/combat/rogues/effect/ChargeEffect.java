package com.ultra.megamod.feature.combat.rogues.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import com.ultra.megamod.feature.combat.spell.SpellEffects;

/**
 * Charge effect that cleanses movement-impairing effects every tick.
 * Ported from net.rogues.effect.ChargeEffect.
 */
public class ChargeEffect extends MobEffect {
    public ChargeEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    private void removeMovementImpairingEffects(LivingEntity entity) {
        if (entity.level().isClientSide()) {
            return;
        }
        // Remove vanilla slowness
        entity.removeEffect(MobEffects.SLOWNESS);
        // Remove custom spell movement impairments
        entity.removeEffect(SpellEffects.FROZEN);
        entity.removeEffect(SpellEffects.FROST_SLOWNESS);
        entity.removeEffect(SpellEffects.ENTANGLING_ROOTS);
        entity.removeEffect(SpellEffects.SHOCK);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        removeMovementImpairingEffects(entity);
        return true;
    }

    @Override
    public void onEffectAdded(LivingEntity entity, int amplifier) {
        super.onEffectAdded(entity, amplifier);
        removeMovementImpairingEffects(entity);
    }
}
