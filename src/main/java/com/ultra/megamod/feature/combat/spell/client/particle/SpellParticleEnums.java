package com.ultra.megamod.feature.combat.spell.client.particle;

/**
 * Enums used by the spell particle system.
 * Ported from SpellEngine's SpellEngineParticles inner types.
 */
public class SpellParticleEnums {

    /** Particle motion behavior. */
    public enum Motion {
        FLOAT,      // Gentle floating with slight velocity damping
        DECELERATE, // Like float but with stronger damping
        ASCEND,     // Anti-gravity, floats upward
        BURST       // Gravity-affected burst projectile
    }

    /** Particle visual shape (determines alpha and animation). */
    public enum Shape {
        SPELL(false),
        STRIPE(false),
        SPARK(false),
        SKULL(true),
        HOLY(true),
        HEAL(true),
        HOLY_SPELL(true),
        ARCANE(true);

        public final boolean animated;
        Shape(boolean animated) { this.animated = animated; }
    }

    /** Particle orientation mode. */
    public enum Orientation {
        HORIZONTAL,
        VERTICAL
    }

    /** Fading mode for area particles. */
    public enum Fading {
        NONE,
        IN,
        OUT,
        IN_OUT
    }

    /** Represents a particle texture with frame count. */
    public record Texture(String id, int frames) {
        public Texture(String id) { this(id, 1); }
    }

    /** Combined shape + motion variant for universal particles. */
    public record MagicVariant(Shape shape, Motion motion) {}
}
