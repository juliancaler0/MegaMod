package com.ultra.megamod.feature.combat.animation.client;

import com.ultra.megamod.lib.playeranim.core.animation.layered.IAnimation;
import com.ultra.megamod.lib.playeranim.core.animation.layered.ModifierLayer;
import com.ultra.megamod.lib.playeranim.core.animation.layered.modifier.AdjustmentModifier;
import com.ultra.megamod.lib.playeranim.core.animation.layered.modifier.MirrorModifier;
import com.ultra.megamod.lib.playeranim.core.animation.layered.modifier.SpeedModifier;

import javax.annotation.Nullable;

/**
 * Wraps animation layers with modifiers for speed, mirroring, and pitch adjustment.
 * Ported from SpellEngine's AnimationSubStack.
 */
public class AnimationSubStack {
    public final SpeedModifier speed = new SpeedModifier(1.0f);
    public final MirrorModifier mirror = new MirrorModifier();
    public final ModifierLayer<IAnimation> base = new ModifierLayer<>(null);
    @Nullable
    public AdjustmentModifier adjustment = null;

    public AnimationSubStack(@Nullable AdjustmentModifier adjustmentModifier) {
        mirror.enabled = false;
        if (adjustmentModifier != null) {
            this.adjustment = adjustmentModifier;
            base.addModifier(adjustmentModifier, 0);
        }
        base.addModifier(speed, 0);
        base.addModifier(mirror, 0);
    }
}
