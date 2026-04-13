package com.ultra.megamod.feature.combat.rogues.config;

/**
 * Configuration values for Rogues & Warriors tweaks.
 * Ported from net.rogues.config.TweaksConfig.
 */
public class TweaksConfig {
    public TweaksConfig() {}

    /** Whether to ignore mod requirements for items (aether, betternether, betterend). */
    public boolean ignore_items_required_mods = true;

    /**
     * Multiplier applied to Strength effect's attack damage bonus.
     * Set to 0 to disable the rebalance. Default 0.1 = 10% per level.
     */
    public double rebalance_strength_attack_damage_multiplier = 0.1;

    /** Follow range for mobs when the target is in stealth (in blocks). */
    public double stealth_follow_range = 1.0;

    /**
     * Multiplier applied to visibility scaling when entity is stealthed.
     * Lower = harder to detect. 0.1 = 10% of normal visibility.
     */
    public double stealth_visibility_multiplier = 0.1;
}
