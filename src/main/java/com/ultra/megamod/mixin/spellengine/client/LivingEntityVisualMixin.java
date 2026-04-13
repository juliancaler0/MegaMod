package com.ultra.megamod.mixin.spellengine.client;

import net.minecraft.world.entity.LivingEntity;
import com.ultra.megamod.lib.spellengine.api.spell.Spell;
import com.ultra.megamod.lib.spellengine.api.effect.CustomParticleStatusEffect;
import com.ultra.megamod.lib.spellengine.api.effect.Synchronized;
import com.ultra.megamod.lib.spellengine.client.beam.BeamEmitterEntity;
import com.ultra.megamod.lib.spellengine.internals.delivery.Beam;
import com.ultra.megamod.lib.spellengine.internals.casting.SpellCasterEntity;
import com.ultra.megamod.lib.spellengine.fx.ParticleHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityVisualMixin implements BeamEmitterEntity {
    private LivingEntity livingEntity() {
        return (LivingEntity) ((Object) this);
    }

    @Nullable
    public Beam.Rendered lastRenderedBeam;

    @Override
    public void setLastRenderedBeam(Beam.Rendered beam) {
        lastRenderedBeam = beam;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick_TAIL_spawnBeamParticles(CallbackInfo ci) {
        var livingEntity = livingEntity();
        Spell.Target.Beam beam = null;
        if (livingEntity instanceof SpellCasterEntity caster) {
            beam = caster.getBeam();
        }
        var renderedBeam = lastRenderedBeam;
        if (livingEntity.level().isClientSide() && beam != null && renderedBeam != null) {
            var position = renderedBeam.position();
            var appearance = renderedBeam.appearance();

            var yaw = livingEntity.getYRot();

            if (position.hitBlock()) {
                for (var batch : appearance.block_hit_particles) {
                    ParticleHelper.play(livingEntity.level(), livingEntity.tickCount, position.end(),
                            appearance.width * 2, yaw, livingEntity.getXRot(), batch, livingEntity);
                }
            }
        }
    }

    @Inject(method = "tickEffects", at = @At("TAIL"))
    private void tickEffects_TAIL_SpellEngine_CustomParticles(CallbackInfo ci) {
        var livingEntity = livingEntity();
        if (!livingEntity.isAlive() || !livingEntity.level().isClientSide()) {
            return;
        }

        for (var entry: Synchronized.effectsOf(livingEntity)) {
            var effect = entry.effect();
            var amplifier = entry.amplifier();
            var spawner = CustomParticleStatusEffect.spawnerOf(effect);
            if (spawner != null) {
                spawner.spawnParticles(livingEntity, amplifier);
            }
        }
    }
}