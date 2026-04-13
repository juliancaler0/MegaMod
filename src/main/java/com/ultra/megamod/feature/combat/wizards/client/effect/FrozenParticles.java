package com.ultra.megamod.feature.combat.wizards.client.effect;

import com.ultra.megamod.lib.spellengine.api.effect.CustomParticleStatusEffect;
import com.ultra.megamod.lib.spellengine.api.spell.fx.ParticleBatch;
import com.ultra.megamod.lib.spellengine.client.util.Color;
import com.ultra.megamod.lib.spellengine.fx.ParticleHelper;
import com.ultra.megamod.lib.spellengine.fx.SpellEngineParticles;
import net.minecraft.world.entity.LivingEntity;

public class FrozenParticles implements CustomParticleStatusEffect.Spawner {

    private final ParticleBatch particles;

    public FrozenParticles(int particleCount) {
        this.particles = new ParticleBatch(
                SpellEngineParticles.MagicParticles.get(
                        SpellEngineParticles.MagicParticles.Shape.FROST,
                        SpellEngineParticles.MagicParticles.Motion.BURST
                ).id().toString(),
                ParticleBatch.Shape.SPHERE,
                ParticleBatch.Origin.CENTER,
                null,
                particleCount,
                0.1F,
                0.3F,
                0)
                .color(Color.FROST.toRGBA());
    }

    @Override
    public void spawnParticles(LivingEntity livingEntity, int amplifier) {
        var scaledParticles = new ParticleBatch(particles);
        scaledParticles.count *= (amplifier + 1);
        ParticleHelper.play(livingEntity.level(), livingEntity, scaledParticles);
    }
}
