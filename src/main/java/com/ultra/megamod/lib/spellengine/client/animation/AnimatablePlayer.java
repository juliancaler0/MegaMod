package com.ultra.megamod.lib.spellengine.client.animation;

import com.ultra.megamod.lib.spellengine.internals.casting.SpellCast;

public interface AnimatablePlayer {
    void playSpellAnimation(SpellCast.Animation type, String name, float speed);
    void updateSpellCastAnimationsOnTick();
}
