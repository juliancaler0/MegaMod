package net.relics_rpgs.client;

import net.relics_rpgs.spell.RelicEffects;
import net.relics_rpgs.spell.RelicSpells;
import net.spell_engine.api.effect.CustomParticleStatusEffect;
import net.spell_engine.api.render.BuffParticleSpawner;
import net.spell_engine.api.render.StunParticleSpawner;
import net.spell_engine.api.spell.fx.ParticleBatch;
import net.spell_engine.client.gui.SpellTooltip;
import net.spell_engine.client.util.Color;
import net.spell_engine.fx.SpellEngineParticles;

public class RelicsClientMod {
    public static void init() {
        for (var entry: RelicSpells.entries) {
            if (entry.mutator() != null) {
                SpellTooltip.addDescriptionMutator(entry.id(), entry.mutator());
            }
        }

        var spark_float = SpellEngineParticles.MagicParticles.get(
                SpellEngineParticles.MagicParticles.Shape.SPARK,
                SpellEngineParticles.MagicParticles.Motion.FLOAT).id().toString();

        CustomParticleStatusEffect.register(
                RelicEffects.LESSER_ATTACK_DAMAGE.effect,
                new BuffParticleSpawner(
                        BuffParticleSpawner.defaultBatch(
                                spark_float,
                                1,
                                Color.RAGE.toRGBA())
                )
        );
        CustomParticleStatusEffect.register(
                RelicEffects.LESSER_ATTACKS_SPEED.effect,
                new BuffParticleSpawner(
                        BuffParticleSpawner.defaultBatch(
                                spark_float,
                                1,
                                Color.FROST.toRGBA())
                )
        );
        CustomParticleStatusEffect.register(
                RelicEffects.LESSER_RANGED_DAMAGE.effect,
                new BuffParticleSpawner(
                        BuffParticleSpawner.defaultBatch(
                                spark_float,
                                1,
                                Color.NATURE.toRGBA())
                )
        );
        CustomParticleStatusEffect.register(
                RelicEffects.LESSER_SPELL_POWER.effect,
                new BuffParticleSpawner(
                        BuffParticleSpawner.defaultBatch(
                                spark_float,
                                1,
                                Color.WHITE.toRGBA())
                )
        );
        CustomParticleStatusEffect.register(
                RelicEffects.LESSER_SPELL_HASTE.effect,
                new BuffParticleSpawner(
                        BuffParticleSpawner.defaultBatch(
                                spark_float,
                                1,
                                Color.HOLY.toRGBA())
                )
        );
        CustomParticleStatusEffect.register(
                RelicEffects.LESSER_SPELL_CRIT.effect,
                new BuffParticleSpawner(
                        BuffParticleSpawner.defaultBatch(
                                spark_float,
                                1,
                                Color.HOLY.toRGBA())
                )
        );
        CustomParticleStatusEffect.register(
                RelicEffects.LESSER_POWER_ARCANE_FIRE.effect,
                new BuffParticleSpawner(new ParticleBatch[]{
                        BuffParticleSpawner.defaultBatch(
                                spark_float,
                                0.5F,
                                Color.ARCANE.toRGBA()),
                        BuffParticleSpawner.defaultBatch(
                                SpellEngineParticles.flame_spark.id().toString(),
                                0.5F)
                })
        );
        CustomParticleStatusEffect.register(
                RelicEffects.LESSER_POWER_FROST_HEALING.effect,
                new BuffParticleSpawner(new ParticleBatch[]{
                        BuffParticleSpawner.defaultBatch(
                                spark_float,
                                0.5F,
                                Color.FROST.toRGBA()),
                        BuffParticleSpawner.defaultBatch(
                                spark_float,
                                0.5F,
                                Color.HOLY.toRGBA()),
                })
        );
        CustomParticleStatusEffect.register(
                RelicEffects.LESSER_PROC_CRIT_DAMAGE.effect,
                new BuffParticleSpawner(
                        BuffParticleSpawner.defaultBatch(
                                spark_float,
                                1,
                                Color.HOLY.toRGBA())
                )
        );

        var stripe_float = SpellEngineParticles.MagicParticles.get(
                SpellEngineParticles.MagicParticles.Shape.STRIPE,
                SpellEngineParticles.MagicParticles.Motion.FLOAT).id().toString();
        var spark_decelerate = SpellEngineParticles.MagicParticles.get(
                SpellEngineParticles.MagicParticles.Shape.SPARK,
                SpellEngineParticles.MagicParticles.Motion.DECELERATE).id().toString();

        CustomParticleStatusEffect.register(
                RelicEffects.MEDIUM_EVASION.effect,
                new BuffParticleSpawner(
                        new ParticleBatch[]{
                                new ParticleBatch(
                                        spark_decelerate,
                                ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                                1, 0.3F, 0.3F)
                                        .color(RelicSpells.EVASION_COLOR.toRGBA())
                                        .preSpawnTravel(4)
                                        .followEntity(true)
                                        .invert()
                        }
                )
        );
        CustomParticleStatusEffect.register(
                RelicEffects.MEDIUM_ATTACK_DAMAGE.effect,
                new BuffParticleSpawner(
                        BuffParticleSpawner.defaultBatch(
                                stripe_float,
                                1,
                                Color.RAGE.toRGBA())
                )
        );
        CustomParticleStatusEffect.register(
                RelicEffects.MEDIUM_ATTACKS_SPEED.effect,
                new BuffParticleSpawner(
                        BuffParticleSpawner.defaultBatch(
                                stripe_float,
                                1,
                                Color.FROST.toRGBA())
                )
        );
        CustomParticleStatusEffect.register(
                RelicEffects.MEDIUM_RANGED_DAMAGE.effect,
                new BuffParticleSpawner(
                        BuffParticleSpawner.defaultBatch(
                                stripe_float,
                                1,
                                Color.NATURE.toRGBA())
                )
        );
        CustomParticleStatusEffect.register(
                RelicEffects.MEDIUM_DEFENSE.effect,
                new BuffParticleSpawner(SpellEngineParticles.shield_small.id().toString(), 0.2F)
        );
        CustomParticleStatusEffect.register(
                RelicEffects.MEDIUM_SPELL_POWER.effect,
                new BuffParticleSpawner(
                        BuffParticleSpawner.defaultBatch(
                                stripe_float,
                                1,
                                Color.WHITE.toRGBA())
                )
        );
        CustomParticleStatusEffect.register(
                RelicEffects.MEDIUM_SPELL_HASTE.effect,
                new BuffParticleSpawner(
                        BuffParticleSpawner.defaultBatch(
                                stripe_float,
                                1,
                                Color.HOLY.toRGBA())
                )
        );
        CustomParticleStatusEffect.register(
                RelicEffects.MEDIUM_ARCANE_POWER.effect,
                new BuffParticleSpawner(
                        BuffParticleSpawner.defaultBatch(
                                stripe_float,
                                1,
                                Color.ARCANE.toRGBA())
                )
        );
        CustomParticleStatusEffect.register(
                RelicEffects.MEDIUM_FROST_POWER.effect,
                new BuffParticleSpawner(
                        BuffParticleSpawner.defaultBatch(
                                stripe_float,
                                1,
                                Color.FROST.toRGBA())
                )
        );
        CustomParticleStatusEffect.register(
                RelicEffects.MEDIUM_FIRE_POWER.effect,
                new BuffParticleSpawner(SpellEngineParticles.flame_medium_a.id().toString(), 0.5F)
        );
        CustomParticleStatusEffect.register(
                RelicEffects.MEDIUM_HEALING_POWER.effect,
                new BuffParticleSpawner(
                        BuffParticleSpawner.defaultBatch(
                                stripe_float,
                                1,
                                Color.HOLY.toRGBA())
                )
        );

        CustomParticleStatusEffect.register(
                RelicEffects.STUN.effect,
                new StunParticleSpawner()
        );
        CustomParticleStatusEffect.register(
                RelicEffects.GREATER_PHYSICAL_TRANCE.effect,
                new BuffParticleSpawner(
                        BuffParticleSpawner.defaultBatch(
                                stripe_float,
                                0.5F,
                                Color.RAGE.toRGBA())
                )
        );
        CustomParticleStatusEffect.register(
                RelicEffects.GREATER_SPELL_TRANCE.effect,
                new BuffParticleSpawner(
                        BuffParticleSpawner.defaultBatch(
                                stripe_float,
                                0.5F,
                                Color.WHITE.toRGBA())
                )
        );
        CustomParticleStatusEffect.register(
                RelicEffects.GREATER_DEFENSE_ARMOR.effect,
                new BuffParticleSpawner(SpellEngineParticles.shield_small.id().toString(), 0.5F)
        );

        var spell_float = SpellEngineParticles.MagicParticles.get(
                SpellEngineParticles.MagicParticles.Shape.SPELL,
                SpellEngineParticles.MagicParticles.Motion.FLOAT).id().toString();

        CustomParticleStatusEffect.register(
                RelicEffects.SUPERIOR_ATTACK_DAMAGE.effect,
                new BuffParticleSpawner(
                        BuffParticleSpawner.defaultBatch(
                                spell_float,
                                0.5F,
                                Color.HOLY.toRGBA())
                )
        );
        CustomParticleStatusEffect.register(
                RelicEffects.SUPERIOR_SPELL_POWER.effect,
                new BuffParticleSpawner(
                        BuffParticleSpawner.defaultBatch(
                                spell_float,
                                0.5F,
                                Color.WHITE.toRGBA())
                )
        );
        CustomParticleStatusEffect.register(
                RelicEffects.SUPERIOR_HEALING_TAKEN.effect,
                new BuffParticleSpawner(
                        BuffParticleSpawner.defaultBatch(
                                spell_float,
                                0.5F,
                                Color.FROST.toRGBA())
                )
        );
    }
}
