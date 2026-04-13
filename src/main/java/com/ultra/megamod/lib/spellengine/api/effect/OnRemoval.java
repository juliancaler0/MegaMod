package com.ultra.megamod.lib.spellengine.api.effect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffect;

import java.util.function.Consumer;

public interface OnRemoval {
    record Context(LivingEntity entity) { }
    Consumer<Context> removalHandler();
    void setRemovalHandler(Consumer<Context> handler);

    static void configure(MobEffect effect, Consumer<Context> handler) {
        ((OnRemoval)effect).setRemovalHandler(handler);
    }
}
