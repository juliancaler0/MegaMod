package com.ultra.megamod.feature.combat.spell.client.particle;

import com.ultra.megamod.MegaMod;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Central registry for all custom spell particle types.
 * Names MUST match existing particle definition JSONs in assets/megamod/particles/.
 * Ported from SpellEngine's SpellEngineParticles registration.
 */
public class SpellParticleRegistry {

    public static final DeferredRegister<net.minecraft.core.particles.ParticleType<?>> PARTICLES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, MegaMod.MODID);

    // ═══════════════════════════════════════════
    // Flame / Spark particles (match existing JSONs)
    // ═══════════════════════════════════════════
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, SimpleParticleType> FLAME =
            PARTICLES.register("flame", () -> new SimpleParticleType(false));
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, SimpleParticleType> FLAME_SPARK =
            PARTICLES.register("flame_spark", () -> new SimpleParticleType(false));
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, SimpleParticleType> FLAME_MEDIUM =
            PARTICLES.register("flame_medium_a", () -> new SimpleParticleType(false));
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, SimpleParticleType> FROST_SHARD =
            PARTICLES.register("frost_shard", () -> new SimpleParticleType(false));
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, SimpleParticleType> ELECTRIC_SPARK =
            PARTICLES.register("electric_arc_a", () -> new SimpleParticleType(false));

    // ═══════════════════════════════════════════
    // Snowflake variants
    // ═══════════════════════════════════════════
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, SimpleParticleType> SPELL_SNOWFLAKE =
            PARTICLES.register("snowflake", () -> new SimpleParticleType(false));
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, SimpleParticleType> DRIPPING_BLOOD =
            PARTICLES.register("dripping_blood", () -> new SimpleParticleType(false));
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, SimpleParticleType> SPELL_ROOTS =
            PARTICLES.register("roots", () -> new SimpleParticleType(false));

    // ═══════════════════════════════════════════
    // Explosion / Smoke
    // ═══════════════════════════════════════════
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, SimpleParticleType> SPELL_EXPLOSION =
            PARTICLES.register("fire_explosion", () -> new SimpleParticleType(false));
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, SimpleParticleType> SPELL_SMOKE_MEDIUM =
            PARTICLES.register("smoke_medium", () -> new SimpleParticleType(false));
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, SimpleParticleType> SPELL_SMOKE_LARGE =
            PARTICLES.register("smoke_large", () -> new SimpleParticleType(false));
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, SimpleParticleType> WEAKNESS_SMOKE =
            PARTICLES.register("weakness_smoke", () -> new SimpleParticleType(false));

    // ═══════════════════════════════════════════
    // Electric arcs
    // ═══════════════════════════════════════════
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, SimpleParticleType> ELECTRIC_ARC_B =
            PARTICLES.register("electric_arc_b", () -> new SimpleParticleType(false));
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, SimpleParticleType> SHIELD_SMALL =
            PARTICLES.register("shield_small", () -> new SimpleParticleType(false));
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, SimpleParticleType> GROUND_GLOW =
            PARTICLES.register("ground_glow", () -> new SimpleParticleType(false));

    // ═══════════════════════════════════════════
    // Universal magic particles (match magic_*_*.json files)
    // ═══════════════════════════════════════════
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, SimpleParticleType> SPELL_ASCEND =
            PARTICLES.register("magic_spell_ascend", () -> new SimpleParticleType(false));
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, SimpleParticleType> SPELL_BURST =
            PARTICLES.register("magic_spell_burst", () -> new SimpleParticleType(false));
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, SimpleParticleType> SPELL_DECELERATE =
            PARTICLES.register("magic_spell_decelerate", () -> new SimpleParticleType(false));
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, SimpleParticleType> SPELL_FLOAT =
            PARTICLES.register("magic_spell_float", () -> new SimpleParticleType(false));

    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, SimpleParticleType> ARCANE_ASCEND =
            PARTICLES.register("magic_arcane_ascend", () -> new SimpleParticleType(false));
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, SimpleParticleType> ARCANE_BURST =
            PARTICLES.register("magic_arcane_burst", () -> new SimpleParticleType(false));
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, SimpleParticleType> FROST_BURST =
            PARTICLES.register("magic_frost_burst", () -> new SimpleParticleType(false));
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, SimpleParticleType> HEAL_ASCEND =
            PARTICLES.register("magic_heal_ascend", () -> new SimpleParticleType(false));
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, SimpleParticleType> HOLY_ASCEND =
            PARTICLES.register("magic_holy_ascend", () -> new SimpleParticleType(false));
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, SimpleParticleType> HOLY_BURST =
            PARTICLES.register("magic_holy_burst", () -> new SimpleParticleType(false));
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, SimpleParticleType> SPARK_FLOAT =
            PARTICLES.register("magic_spark_float", () -> new SimpleParticleType(false));
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, SimpleParticleType> SPARK_DECELERATE =
            PARTICLES.register("magic_spark_decelerate", () -> new SimpleParticleType(false));
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, SimpleParticleType> SPARK_BURST =
            PARTICLES.register("magic_spark_burst", () -> new SimpleParticleType(false));
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, SimpleParticleType> STRIPE_FLOAT =
            PARTICLES.register("magic_stripe_float", () -> new SimpleParticleType(false));
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, SimpleParticleType> HOLY_SPELL_FLOAT =
            PARTICLES.register("magic_holy_float", () -> new SimpleParticleType(false));
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, SimpleParticleType> HOLY_SPELL_DECELERATE =
            PARTICLES.register("magic_holy_decelerate", () -> new SimpleParticleType(false));

    // ═══════════════════════════════════════════
    // Area effect particles
    // ═══════════════════════════════════════════
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, SimpleParticleType> AREA_CIRCLE =
            PARTICLES.register("area_circle_1", () -> new SimpleParticleType(false));
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, SimpleParticleType> AREA_SWIRL =
            PARTICLES.register("area_swirl", () -> new SimpleParticleType(false));

    // ═══════════════════════════════════════════
    // Sign particles
    // ═══════════════════════════════════════════
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, SimpleParticleType> SIGN_SPEED =
            PARTICLES.register("sign_speed", () -> new SimpleParticleType(false));
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, SimpleParticleType> SIGN_SHIELD =
            PARTICLES.register("sign_shield", () -> new SimpleParticleType(false));
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, SimpleParticleType> SIGN_WAND =
            PARTICLES.register("sign_wand", () -> new SimpleParticleType(false));

    // BetterCombat slash particles (12 types) are now owned by BetterCombatParticles.
    // Previously double-registered here as plain SimpleParticleTypes — removed to avoid
    // "Adding duplicate key ... megamod:botslash45" registry conflict.

    public static void init(IEventBus modBus) {
        PARTICLES.register(modBus);
    }
}
