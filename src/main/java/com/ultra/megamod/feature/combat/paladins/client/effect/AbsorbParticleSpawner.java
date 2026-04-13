package com.ultra.megamod.feature.combat.paladins.client.effect;

import com.ultra.megamod.lib.spellengine.api.effect.CustomParticleStatusEffect;
import com.ultra.megamod.lib.spellengine.api.spell.fx.ParticleBatch;
import com.ultra.megamod.lib.spellengine.client.util.Color;
import com.ultra.megamod.lib.spellengine.fx.ParticleHelper;
import com.ultra.megamod.lib.spellengine.fx.SpellEngineParticles;
import net.minecraft.world.entity.LivingEntity;

public class AbsorbParticleSpawner implements CustomParticleStatusEffect.Spawner {
    public static final ParticleBatch particles = new ParticleBatch(
            SpellEngineParticles.MagicParticles.get(
                    SpellEngineParticles.MagicParticles.Shape.SPARK,
                    SpellEngineParticles.MagicParticles.Motion.FLOAT).id().toString(),
            ParticleBatch.Shape.WIDE_PIPE,
            ParticleBatch.Origin.FEET,
            null,
            5,
            0.01F,
            0.05F,
            0).color(Color.HOLY.toRGBA());

    @Override
    public void spawnParticles(LivingEntity livingEntity, int amplifier) {
        var level = livingEntity.level();
        if (level.isClientSide()) {
            var scaledParticles = new ParticleBatch(particles);
            scaledParticles.count *= (amplifier + 1);
            scaledParticles.max_speed *= livingEntity.getScale();
            ParticleHelper.play(level, livingEntity, scaledParticles);
        }
    }
}
