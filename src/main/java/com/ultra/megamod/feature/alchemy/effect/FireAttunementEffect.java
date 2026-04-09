package com.ultra.megamod.feature.alchemy.effect;

import com.ultra.megamod.feature.attributes.MegaModAttributes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * Fire Attunement: +20 FIRE_DAMAGE_BONUS for 3 minutes.
 * Uses an attribute modifier so the bonus is automatically applied/removed.
 */
public class FireAttunementEffect extends MobEffect {
    public FireAttunementEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFF6600);
        this.addAttributeModifier(
            MegaModAttributes.FIRE_DAMAGE_BONUS,
            Identifier.fromNamespaceAndPath("megamod", "effect.fire_attunement"),
            20.0,
            AttributeModifier.Operation.ADD_VALUE
        );
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false; // Passive attribute modifier
    }
}
