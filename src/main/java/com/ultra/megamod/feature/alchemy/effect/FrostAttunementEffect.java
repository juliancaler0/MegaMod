package com.ultra.megamod.feature.alchemy.effect;

import com.ultra.megamod.feature.attributes.MegaModAttributes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * Frost Attunement: +20 ICE_DAMAGE_BONUS for 3 minutes.
 * Uses an attribute modifier so the bonus is automatically applied/removed.
 */
public class FrostAttunementEffect extends MobEffect {
    public FrostAttunementEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x66CCFF);
        this.addAttributeModifier(
            MegaModAttributes.ICE_DAMAGE_BONUS,
            Identifier.fromNamespaceAndPath("megamod", "effect.frost_attunement"),
            20.0,
            AttributeModifier.Operation.ADD_VALUE
        );
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false; // Passive attribute modifier
    }
}
