package com.ultra.megamod.feature.alchemy.effect;

import com.ultra.megamod.feature.attributes.MegaModAttributes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * Spell Arcane Surge: +20 ARCANE_POWER for 3 minutes.
 * Uses an attribute modifier so the bonus is automatically applied/removed.
 */
public class SpellArcaneSurgeEffect extends MobEffect {
    public SpellArcaneSurgeEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x9B59B6);
        this.addAttributeModifier(
            MegaModAttributes.ARCANE_POWER,
            Identifier.fromNamespaceAndPath("megamod", "effect.spell_arcane_surge"),
            20.0,
            AttributeModifier.Operation.ADD_VALUE
        );
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false; // Passive attribute modifier
    }
}
