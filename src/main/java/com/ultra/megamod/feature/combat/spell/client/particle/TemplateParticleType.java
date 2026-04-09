package com.ultra.megamod.feature.combat.spell.client.particle;

import com.ultra.megamod.feature.combat.spell.client.util.Color;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;

/**
 * Custom particle type that carries appearance data for runtime customization.
 * Ported 1:1 from SpellEngine's TemplateParticleType.
 *
 * Since NeoForge 1.21.11's particle registration is strict about types,
 * we use SimpleParticleType for registration and pass appearance data
 * via a ThreadLocal context instead of encoding it in the particle type itself.
 */
public class TemplateParticleType {

    /**
     * Appearance data for template particles.
     */
    public static class Appearance {
        @Nullable public Color color;
        public float scale = 1.0f;
        @Nullable public Entity entityFollowed;
        public boolean grounded = false;
        public float max_age = 1.0f;

        public Appearance() {}

        public Appearance(@Nullable Color color, float scale, @Nullable Entity entityFollowed, float maxAge) {
            this.color = color;
            this.scale = scale;
            this.entityFollowed = entityFollowed;
            this.max_age = maxAge;
        }
    }

    /**
     * ThreadLocal context for passing appearance data to particle factories.
     * Set before spawning a particle, consumed by the factory.
     */
    private static final ThreadLocal<Appearance> CURRENT_APPEARANCE = new ThreadLocal<>();

    public static void setAppearance(@Nullable Appearance appearance) {
        CURRENT_APPEARANCE.set(appearance);
    }

    @Nullable
    public static Appearance consumeAppearance() {
        Appearance a = CURRENT_APPEARANCE.get();
        CURRENT_APPEARANCE.remove();
        return a;
    }

    /**
     * Apply appearance color to a particle (same as SpellEngine's TemplateParticleType.apply).
     */
    public static void apply(@Nullable Appearance appearance, Particle particle) {
        if (appearance == null) return;
        if (particle instanceof SingleQuadParticle textured) {
            if (appearance.color != null) {
                textured.setColor(appearance.color.red(), appearance.color.green(), appearance.color.blue());
            }
        }
    }

    /**
     * Apply appearance to a particle (convenience method matching SpellEngine's static apply).
     */
    public static void applyFromContext(Particle particle) {
        Appearance appearance = consumeAppearance();
        apply(appearance, particle);
    }
}
