package com.ultra.megamod.lib.skilltree.effect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class WizardAbsorbEffect extends MobEffect {
    private final int healthPerStack;

    public WizardAbsorbEffect(MobEffectCategory category, int color) {
        super(category, color);
        this.healthPerStack = 2;
    }

    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        return entity.getAbsorptionAmount() > 0.0F || entity.level().isClientSide();
    }

    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    // TODO: onApplied removed in 1.21.11 - need to use onEffectAdded or similar event
    // public void onApplied(LivingEntity entity, int amplifier) {
    //     entity.setAbsorptionAmount(Math.max(entity.getAbsorptionAmount(), (float)(healthPerStack * (1 + amplifier))));
    // }
}