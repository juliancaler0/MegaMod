package com.ultra.megamod.feature.combat.spell.client.particle;

import com.ultra.megamod.feature.combat.spell.client.util.Color;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

/**
 * Registers particle providers (factories) for all custom spell particle types.
 * Called from MegaModClient via modEventBus.addListener.
 */
public class SpellParticleProviders {

    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        // ── Flame variants ──
        event.registerSpriteSet(SpellParticleRegistry.FLAME.get(), SpellFlameParticle.FlameFactory::new);
        event.registerSpriteSet(SpellParticleRegistry.FLAME_SPARK.get(), SpellFlameParticle.AnimatedFlameFactory::new);
        event.registerSpriteSet(SpellParticleRegistry.FLAME_MEDIUM.get(), SpellFlameParticle.MediumFlameFactory::new);
        event.registerSpriteSet(SpellParticleRegistry.FROST_SHARD.get(), SpellFlameParticle.FrostShardFactory::new);
        event.registerSpriteSet(SpellParticleRegistry.ELECTRIC_SPARK.get(), SpellFlameParticle.ElectricSparkFactory::new);
        event.registerSpriteSet(SpellParticleRegistry.WEAKNESS_SMOKE.get(), SpellFlameParticle.WeaknessSmokeFactory::new);

        // ── Snowflake variants ──
        event.registerSpriteSet(SpellParticleRegistry.SPELL_SNOWFLAKE.get(), SpellSnowflakeParticle.FrostFactory::new);
        event.registerSpriteSet(SpellParticleRegistry.DRIPPING_BLOOD.get(), SpellSnowflakeParticle.DrippingBloodFactory::new);
        event.registerSpriteSet(SpellParticleRegistry.SPELL_ROOTS.get(), SpellSnowflakeParticle.RootsFactory::new);

        // ── Explosion ──
        event.registerSpriteSet(SpellParticleRegistry.SPELL_EXPLOSION.get(), SpellExplosionParticle.Factory::new);

        // ── Smoke ──
        event.registerSpriteSet(SpellParticleRegistry.SPELL_SMOKE_MEDIUM.get(), SpellSmokeParticle.CosySmokeFactory::new);
        event.registerSpriteSet(SpellParticleRegistry.SPELL_SMOKE_LARGE.get(), SpellSmokeParticle.CosySmokeFactory::new);

        // ── Electric arcs ──
        event.registerSpriteSet(SpellParticleRegistry.ELECTRIC_SPARK.get(), SpellFlameParticle.AnimatedFlameFactory::new);
        event.registerSpriteSet(SpellParticleRegistry.ELECTRIC_ARC_B.get(), SpellFlameParticle.AnimatedFlameFactory::new);

        // ── Shield / ground ──
        event.registerSpriteSet(SpellParticleRegistry.SHIELD_SMALL.get(), SpellFlameParticle.FlameFactory::new);
        event.registerSpriteSet(SpellParticleRegistry.GROUND_GLOW.get(), SpellFlameParticle.FlameFactory::new);

        // ── Universal magic particles (school-colored) ──
        event.registerSpriteSet(SpellParticleRegistry.SPELL_ASCEND.get(),
                s -> new SpellUniversalParticle.MagicVariant(s, new SpellParticleEnums.MagicVariant(SpellParticleEnums.Shape.SPELL, SpellParticleEnums.Motion.ASCEND)));
        event.registerSpriteSet(SpellParticleRegistry.SPELL_BURST.get(),
                s -> new SpellUniversalParticle.MagicVariant(s, new SpellParticleEnums.MagicVariant(SpellParticleEnums.Shape.SPELL, SpellParticleEnums.Motion.BURST)));
        event.registerSpriteSet(SpellParticleRegistry.SPELL_DECELERATE.get(),
                s -> new SpellUniversalParticle.MagicVariant(s, new SpellParticleEnums.MagicVariant(SpellParticleEnums.Shape.SPELL, SpellParticleEnums.Motion.DECELERATE)));
        event.registerSpriteSet(SpellParticleRegistry.SPELL_FLOAT.get(),
                s -> new SpellUniversalParticle.MagicVariant(s, new SpellParticleEnums.MagicVariant(SpellParticleEnums.Shape.SPELL, SpellParticleEnums.Motion.FLOAT)));

        event.registerSpriteSet(SpellParticleRegistry.ARCANE_ASCEND.get(),
                s -> new SpellUniversalParticle.MagicVariant(s, new SpellParticleEnums.MagicVariant(SpellParticleEnums.Shape.ARCANE, SpellParticleEnums.Motion.ASCEND)));
        event.registerSpriteSet(SpellParticleRegistry.ARCANE_BURST.get(),
                s -> new SpellUniversalParticle.MagicVariant(s, new SpellParticleEnums.MagicVariant(SpellParticleEnums.Shape.ARCANE, SpellParticleEnums.Motion.BURST)));
        event.registerSpriteSet(SpellParticleRegistry.FROST_BURST.get(),
                s -> new SpellUniversalParticle.MagicVariant(s, new SpellParticleEnums.MagicVariant(SpellParticleEnums.Shape.SPELL, SpellParticleEnums.Motion.BURST)));
        event.registerSpriteSet(SpellParticleRegistry.HEAL_ASCEND.get(),
                s -> new SpellUniversalParticle.MagicVariant(s, new SpellParticleEnums.MagicVariant(SpellParticleEnums.Shape.HEAL, SpellParticleEnums.Motion.ASCEND)));
        event.registerSpriteSet(SpellParticleRegistry.HOLY_ASCEND.get(),
                s -> new SpellUniversalParticle.MagicVariant(s, new SpellParticleEnums.MagicVariant(SpellParticleEnums.Shape.HOLY, SpellParticleEnums.Motion.ASCEND)));
        event.registerSpriteSet(SpellParticleRegistry.HOLY_BURST.get(),
                s -> new SpellUniversalParticle.MagicVariant(s, new SpellParticleEnums.MagicVariant(SpellParticleEnums.Shape.HOLY, SpellParticleEnums.Motion.BURST)));
        event.registerSpriteSet(SpellParticleRegistry.SPARK_FLOAT.get(),
                s -> new SpellUniversalParticle.MagicVariant(s, new SpellParticleEnums.MagicVariant(SpellParticleEnums.Shape.SPARK, SpellParticleEnums.Motion.FLOAT)));
        event.registerSpriteSet(SpellParticleRegistry.SPARK_DECELERATE.get(),
                s -> new SpellUniversalParticle.MagicVariant(s, new SpellParticleEnums.MagicVariant(SpellParticleEnums.Shape.SPARK, SpellParticleEnums.Motion.DECELERATE)));
        event.registerSpriteSet(SpellParticleRegistry.SPARK_BURST.get(),
                s -> new SpellUniversalParticle.MagicVariant(s, new SpellParticleEnums.MagicVariant(SpellParticleEnums.Shape.SPARK, SpellParticleEnums.Motion.BURST)));
        event.registerSpriteSet(SpellParticleRegistry.STRIPE_FLOAT.get(),
                s -> new SpellUniversalParticle.MagicVariant(s, new SpellParticleEnums.MagicVariant(SpellParticleEnums.Shape.STRIPE, SpellParticleEnums.Motion.FLOAT)));
        event.registerSpriteSet(SpellParticleRegistry.HOLY_SPELL_FLOAT.get(),
                s -> new SpellUniversalParticle.MagicVariant(s, new SpellParticleEnums.MagicVariant(SpellParticleEnums.Shape.HOLY_SPELL, SpellParticleEnums.Motion.FLOAT)));
        event.registerSpriteSet(SpellParticleRegistry.HOLY_SPELL_DECELERATE.get(),
                s -> new SpellUniversalParticle.MagicVariant(s, new SpellParticleEnums.MagicVariant(SpellParticleEnums.Shape.HOLY_SPELL, SpellParticleEnums.Motion.DECELERATE)));

        // ── Area effect particles ──
        event.registerSpriteSet(SpellParticleRegistry.AREA_CIRCLE.get(),
                s -> new SpellAreaParticle.Factory(s, SpellParticleEnums.Fading.IN_OUT, SpellParticleEnums.Orientation.HORIZONTAL, false));
        event.registerSpriteSet(SpellParticleRegistry.AREA_SWIRL.get(),
                s -> new SpellAreaParticle.Factory(s, SpellParticleEnums.Fading.IN_OUT, SpellParticleEnums.Orientation.HORIZONTAL, false));

        // ── Sign particles ──
        event.registerSpriteSet(SpellParticleRegistry.SIGN_SPEED.get(), SpellFlameParticle.SignFactory::new);
        event.registerSpriteSet(SpellParticleRegistry.SIGN_SHIELD.get(), SpellFlameParticle.SignFactory::new);
        event.registerSpriteSet(SpellParticleRegistry.SIGN_WAND.get(), SpellFlameParticle.SignFactory::new);

        // BetterCombat slash particles (12 types) are now registered by
        // BetterCombatParticleProviders.register() on the mod event bus (see MegaModClient).
        // They use the proper source-ported SlashParticleEffect + rotation pipeline under
        // feature/combat/animation/particle/ instead of the SimpleParticleType shims.
    }
}
