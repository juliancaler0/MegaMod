package com.ultra.megamod.feature.alchemy.effect;

import com.ultra.megamod.feature.attributes.MegaModAttributes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * Healing Grace: +20 HEALING_POWER for 3 minutes.
 * Uses an attribute modifier so the bonus is automatically applied/removed.
 */
public class HealingGraceEffect extends MobEffect {
    public HealingGraceEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x55FF55);
        this.addAttributeModifier(
            MegaModAttributes.HEALING_POWER,
            Identifier.fromNamespaceAndPath("megamod", "effect.healing_grace"),
            20.0,
            AttributeModifier.Operation.ADD_VALUE
        );
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false; // Passive attribute modifier
    }
}
