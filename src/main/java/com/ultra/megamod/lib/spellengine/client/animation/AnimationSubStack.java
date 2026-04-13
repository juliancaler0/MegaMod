package com.ultra.megamod.lib.spellengine.client.animation;

import com.ultra.megamod.lib.playeranim.core.animation.layered.ModifierLayer;
import com.ultra.megamod.lib.playeranim.core.animation.layered.modifier.AdjustmentModifier;
import com.ultra.megamod.lib.playeranim.core.animation.layered.modifier.MirrorModifier;
import com.ultra.megamod.lib.playeranim.core.animation.layered.modifier.SpeedModifier;

public class AnimationSubStack {
    public final SpeedModifier speed = new SpeedModifier(1.0f);
    public final MirrorModifier mirror = new MirrorModifier();
    public final ModifierLayer base = new ModifierLayer(null);
    public AdjustmentModifier adjustment = null;

    public AnimationSubStack(AdjustmentModifier adjustmentModifier) {
        mirror.enabled = false;
        if (adjustmentModifier != null) {
            this.adjustment = adjustmentModifier;
            base.addModifier(adjustmentModifier, 0);
        }
        base.addModifier(speed, 0);
        base.addModifier(mirror, 0);
    }
}
