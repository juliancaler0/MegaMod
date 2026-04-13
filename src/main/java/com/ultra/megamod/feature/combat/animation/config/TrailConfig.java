package com.ultra.megamod.feature.combat.animation.config;

import com.ultra.megamod.feature.combat.animation.api.fx.ConditionalTrailAppearance;
import com.ultra.megamod.feature.combat.animation.api.fx.ParticlePlacement;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Configuration for weapon trail particle effects.
 * Ported 1:1 from BetterCombat (net.bettercombat.config.TrailConfig).
 */
public class TrailConfig {
    public ConditionalTrailAppearance trail_appearance = new ConditionalTrailAppearance();
    public LinkedHashMap<String, List<ParticlePlacement>> animation_based = new LinkedHashMap<>();

    public TrailConfig() {}

    public TrailConfig(ConditionalTrailAppearance trail_appearance, LinkedHashMap<String, List<ParticlePlacement>> animation_based) {
        this.trail_appearance = trail_appearance;
        this.animation_based = animation_based;
    }
}
