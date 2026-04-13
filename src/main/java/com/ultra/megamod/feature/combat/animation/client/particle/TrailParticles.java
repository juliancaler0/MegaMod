package com.ultra.megamod.feature.combat.animation.client.particle;

import com.ultra.megamod.feature.combat.animation.api.fx.Color;
import com.ultra.megamod.feature.combat.animation.api.fx.ConditionalTrailAppearance;
import com.ultra.megamod.feature.combat.animation.api.fx.ParticlePlacement;
import com.ultra.megamod.feature.combat.animation.api.fx.TrailAppearance;
import com.ultra.megamod.feature.combat.animation.config.TrailConfig;
import com.ultra.megamod.feature.combat.animation.particle.BetterCombatParticles;
import com.ultra.megamod.feature.combat.animation.particle.SlashParticleEffect;
import net.minecraft.core.particles.ParticleType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Defines the layered particle entries and default trail config for all weapon animations.
 * Ported 1:1 from BetterCombat (net.bettercombat.client.particle.TrailParticles).
 */
public class TrailParticles {

    public record LayeredParticle(ParticleType<SlashParticleEffect> top, ParticleType<SlashParticleEffect> bottom) {
        public LayeredParticle of(ParticleType<SlashParticleEffect> top, ParticleType<SlashParticleEffect> bottom) {
            return new LayeredParticle(top, bottom);
        }
    }

    public record Entry(List<LayeredParticle> particles, float rollOffset, boolean stabPosition) {
        public Entry(List<LayeredParticle> particles, float rollOffset) {
            this(particles, rollOffset, false);
        }
        public Entry(List<LayeredParticle> particles) {
            this(particles, 0F, false);
        }
    }

    private static final String NAMESPACE = "megamod";

    public static Map<String, List<Entry>> ENTRIES = Map.of(
            "stab", List.of(
                    new Entry(List.of(
                            new LayeredParticle(
                                    BetterCombatParticles.topstab.particleType(),
                                    BetterCombatParticles.botstab.particleType()
                            )
                    ), -45F, true),
                    new Entry(List.of(
                            new LayeredParticle(
                                    BetterCombatParticles.topstab.particleType(),
                                    BetterCombatParticles.botstab.particleType()
                            )
                    ), 45F, true)
            ),
            "slash45", List.of(
                    new Entry(List.of(
                            new LayeredParticle(
                                    BetterCombatParticles.topslash45.particleType(),
                                    BetterCombatParticles.botslash45.particleType()
                            )
                    ))
            ),
            "slash90", List.of(
                    new Entry(List.of(
                            new LayeredParticle(
                                    BetterCombatParticles.topslash90.particleType(),
                                    BetterCombatParticles.botslash90.particleType()
                            )
                    ))
            ),
            "slash180", List.of(
                    new Entry(List.of(
                            new LayeredParticle(
                                    BetterCombatParticles.topslash180.particleType(),
                                    BetterCombatParticles.botslash180.particleType()
                            )
                    ))
            ),
            "slash270", List.of(
                    new Entry(List.of(
                            new LayeredParticle(
                                    BetterCombatParticles.topslash270.particleType(),
                                    BetterCombatParticles.botslash270.particleType()
                            )
                    ))
            ),
            "slash360", List.of(
                    new Entry(List.of(
                            new LayeredParticle(
                                    BetterCombatParticles.topslash360.particleType(),
                                    BetterCombatParticles.botslash360.particleType()
                            )
                    ))
            )
    );

    private static TrailConfig cachedDefaults;

    public static TrailConfig getTrailConfig() {
        if (cachedDefaults == null) {
            cachedDefaults = defaults();
        }
        return cachedDefaults;
    }

    public static TrailConfig defaults() {
        LinkedHashMap<String, List<ParticlePlacement>> map = new LinkedHashMap<>();

        map.put(NAMESPACE + ":one_handed_slash_horizontal_right", List.of(
                new ParticlePlacement("slash90", 0.0F, -0.1F, 0.0F, 0.0F, 0.0F, 0.0F)
        ));

        map.put(NAMESPACE + ":one_handed_slash_horizontal_left", List.of(
                new ParticlePlacement("slash90", 0.0F, -0.1F, 0.0F, 0.0F, 0.0F, 180.0F)
        ));

        map.put(NAMESPACE + ":one_handed_uppercut_right", List.of(
                new ParticlePlacement("slash90", 0.0F, -0.1F, 0.0F, 0.0F, 0.0F, 75.0F)
        ));

        map.put(NAMESPACE + ":one_handed_swipe_horizontal_right", List.of(
                new ParticlePlacement("slash90", 0.0F, -0.1F, 0.0F, 0.0F, 0.0F, -0.10F)
        ));

        map.put(NAMESPACE + ":one_handed_slam", List.of(
                new ParticlePlacement("slash90", 0.05F, -0.1F, 0.0F, 45.0F, 0.0F, -85.0F)
        ));

        map.put(NAMESPACE + ":one_handed_stab", List.of(
                new ParticlePlacement("stab", 0.0F, -0.13F, 0.20F, 0.0F, 0.0F, 0.0F)
        ));

        map.put(NAMESPACE + ":one_handed_stab_mounted", List.of(
                new ParticlePlacement("stab", 0.0F, 0.15F, 0.0F, 0.0F, 0.0F, 0.0F)
        ));

        map.put(NAMESPACE + ":one_handed_punch", List.of(
                new ParticlePlacement("stab", 0.0F, -0.1F, 0.15F, 0.0F, 0.0F, 0.0F)
        ));

        map.put(NAMESPACE + ":dual_handed_slash_cross", List.of(
                new ParticlePlacement("slash180", 0.2F, -0.15F, 0.0F, 0.0F, 0.0F, -120.0F),
                new ParticlePlacement("slash180", -0.2F, -0.15F, 0.0F, 0.0F, 0.0F, -60.0F)
        ));

        map.put(NAMESPACE + ":dual_handed_slash_uncross", List.of(
                new ParticlePlacement("slash180", 0.2F, -0.15F, 0.0F, 0.0F, 0.0F, 240.0F),
                new ParticlePlacement("slash180", -0.2F, -0.15F, 0.0F, 0.0F, 0.0F, 300.0F)
        ));

        map.put(NAMESPACE + ":dual_handed_stab", List.of(
                new ParticlePlacement("stab", 0.4F, -0.3F, 0.0F, 0.0F, 0.0F, 0.0F),
                new ParticlePlacement("stab", -0.4F, -0.3F, 0.0F, 0.0F, 0.0F, 0.0F)
        ));

        map.put(NAMESPACE + ":two_handed_stab_left", List.of(
                new ParticlePlacement("stab", 0.0F, -0.15F, 0.0F, 0.0F, 0.0F, 0.0F)
        ));

        map.put(NAMESPACE + ":two_handed_stab_right", List.of(
                new ParticlePlacement("stab", 0.0F, -0.15F, 0.0F, 0.0F, 0.0F, 0.0F)
        ));

        map.put(NAMESPACE + ":two_handed_slash_horizontal_right", List.of(
                new ParticlePlacement("slash180", 0.0F, -0.1F, 0.0F, 0.0F, 0.0F, -5.0F)
        ));

        map.put(NAMESPACE + ":two_handed_slash_horizontal_left", List.of(
                new ParticlePlacement("slash180", 0.0F, -0.1F, 0.0F, 0.0F, 0.0F, 180.0F)
        ));

        map.put(NAMESPACE + ":one_handed_slash_switch_blade_right", List.of(
                new ParticlePlacement("slash180", 0.0F, -0.1F, 0.0F, 0.0F, 0.0F, 0.0F)
        ));

        map.put(NAMESPACE + ":one_handed_slash_switch_blade_left", List.of(
                new ParticlePlacement("slash180", 0.0F, -0.1F, 0.0F, 0.0F, 0.0F, 180.0F)
        ));

        map.put(NAMESPACE + ":two_handed_spin", List.of(
                new ParticlePlacement("slash360", 0.0F, -0.1F, 0.0F, 0.0F, 0.0F, 180.0F)
        ));

        map.put(NAMESPACE + ":two_handed_slash_vertical_right", List.of(
                new ParticlePlacement("slash90", 0.0F, -0.1F, 0.0F, 45.0F, 0.0F, -80.0F)
        ));

        map.put(NAMESPACE + ":two_handed_slash_vertical_left", List.of(
                new ParticlePlacement("slash90", 0.0F, -0.1F, 0.0F, 45.0F, 0.0F, -100.0F)
        ));

        map.put(NAMESPACE + ":two_handed_slam", List.of(
                new ParticlePlacement("slash180", 0.1F, -0.1F, 0.0F, 45.0F, 0.0F, -86.0F)
        ));

        map.put(NAMESPACE + ":two_handed_slam_heavy", List.of(
                new ParticlePlacement("slash180", 0.1F, -0.1F, 0.0F, 45.0F, 0.0F, -86.0F)
        ));

        var defaultTrail = new TrailAppearance(
                new TrailAppearance.Part(Color.WHITE.alpha(0.6F).toRGBA(), false),
                new TrailAppearance.Part(Color.from(0x999999).alpha(0.4F).toRGBA(), false)
        );
        var enchantedTrail = new TrailAppearance(
                new TrailAppearance.Part(Color.from(0x66d9ff).alpha(0.6F).toRGBA(), true),
                new TrailAppearance.Part(Color.from(0x99e6ff).alpha(0.3F).toRGBA(), true)
        );

        var conditionalAppearances = new LinkedHashMap<String, TrailAppearance>();
        conditionalAppearances.put("is_enchanted", enchantedTrail);
        var trailAppearance = new ConditionalTrailAppearance(defaultTrail, conditionalAppearances);

        return new TrailConfig(trailAppearance, map);
    }
}
