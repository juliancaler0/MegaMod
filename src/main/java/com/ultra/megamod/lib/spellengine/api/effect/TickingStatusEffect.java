package com.ultra.megamod.lib.spellengine.api.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.registries.BuiltInRegistries;
import com.ultra.megamod.lib.spellengine.internals.SpellTriggers;

public class TickingStatusEffect extends MobEffect {
    private int interval = 10;

    public TickingStatusEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    public TickingStatusEffect interval(int interval) {
        this.interval = interval;
        return this;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        if (entity instanceof Player player) {
            var entry = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(this);
            SpellTriggers.onEffectTick(player, entry);
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % interval == 0;
    }
}
