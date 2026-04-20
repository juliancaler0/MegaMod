package com.ultra.megamod.feature.combat.animation.client.particle;

import com.ultra.megamod.feature.combat.animation.particle.BetterCombatParticles;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

/**
 * Registers the {@link SlashParticle.Provider} factory for every slash-trail particle type
 * declared in {@link BetterCombatParticles}. Must be invoked on the mod event bus so NeoForge
 * wires the ParticleEngine to the correct sprite-set resource path.
 */
public final class BetterCombatParticleProviders {
    private BetterCombatParticleProviders() {}

    public static void register(RegisterParticleProvidersEvent event) {
        for (var entry : BetterCombatParticles.ENTRIES) {
            event.registerSpriteSet(entry.particleType(),
                    spriteSet -> new SlashParticle.Provider(spriteSet, entry.params));
        }
    }
}
