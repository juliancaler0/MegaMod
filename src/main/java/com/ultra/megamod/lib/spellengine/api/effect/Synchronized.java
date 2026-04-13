package com.ultra.megamod.lib.spellengine.api.effect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffect;

import java.util.List;

public interface Synchronized {
    boolean shouldSynchronize();
    MobEffect setSynchronized(boolean value);

    static void configure(MobEffect effect, boolean isSynchronized) {
        ((Synchronized)effect).setSynchronized(isSynchronized);
    }

    record Effect(MobEffect effect, int amplifier) { }
    static List<Effect> effectsOf(LivingEntity entity) {
        return ((Provider)entity).SpellEngine_syncedStatusEffects();
    }

    public interface Provider {
        List<Effect> SpellEngine_syncedStatusEffects();
    }
}
