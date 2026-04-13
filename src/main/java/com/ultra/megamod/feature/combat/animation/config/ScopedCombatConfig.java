package com.ultra.megamod.feature.combat.animation.config;

import com.ultra.megamod.feature.computer.admin.AdminSystem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Returns per-player combat config values that respect the admin-only scoping toggle.
 *
 * <p>When {@link BetterCombatConfig#admin_only_combat_effects} is ON, non-admin players
 * get the vanilla/reference default values while admin players get whatever has been
 * configured in the combat tab. When scoping is OFF, everyone gets the configured value.</p>
 *
 * <p>Defaults match BetterCombat's reference source so non-admins experience the baseline
 * balance that the mod was designed for.</p>
 */
public final class ScopedCombatConfig {
    private ScopedCombatConfig() {}

    // Reference defaults from BetterCombat (also match the builder.defineInRange defaults in BetterCombatConfig)
    public static final float DEFAULT_UPSWING_MULTIPLIER = 0.5f;
    public static final int   DEFAULT_ATTACK_INTERVAL_CAP = 2;
    public static final float DEFAULT_COMBO_RESET_RATE = 3.0f;
    public static final float DEFAULT_TARGET_SEARCH_RANGE = 2.0f;
    public static final float DEFAULT_MOVEMENT_SPEED_ATTACKING = 0.5f;
    public static final float DEFAULT_DW_ATTACK_SPEED_MULT = 1.2f;
    public static final float DEFAULT_DW_MAIN_DMG_MULT = 1.0f;
    public static final float DEFAULT_DW_OFF_DMG_MULT = 1.0f;

    /** True when the admin-only scoping toggle is on AND player is not admin. */
    private static boolean nonAdminRestricted(Player player) {
        if (!BetterCombatConfig.admin_only_combat_effects) return false;
        if (player instanceof ServerPlayer sp) return !AdminSystem.isAdmin(sp);
        // Client-side: check the client player's name against admin list
        if (player != null) return !AdminSystem.isAdmin(player.getGameProfile().name());
        return false;
    }

    public static float upswingMultiplier(Player p) {
        return nonAdminRestricted(p) ? DEFAULT_UPSWING_MULTIPLIER : BetterCombatConfig.upswing_multiplier;
    }

    public static int attackIntervalCap(Player p) {
        return nonAdminRestricted(p) ? DEFAULT_ATTACK_INTERVAL_CAP : BetterCombatConfig.attack_interval_cap;
    }

    public static float comboResetRate(Player p) {
        return nonAdminRestricted(p) ? DEFAULT_COMBO_RESET_RATE : BetterCombatConfig.combo_reset_rate;
    }

    public static float targetSearchRangeMultiplier(Player p) {
        return nonAdminRestricted(p) ? DEFAULT_TARGET_SEARCH_RANGE : BetterCombatConfig.target_search_range_multiplier;
    }

    public static float movementSpeedWhileAttacking(Player p) {
        return nonAdminRestricted(p) ? DEFAULT_MOVEMENT_SPEED_ATTACKING : BetterCombatConfig.movement_speed_while_attacking;
    }

    public static float dualWieldingAttackSpeedMultiplier(Player p) {
        return nonAdminRestricted(p) ? DEFAULT_DW_ATTACK_SPEED_MULT : BetterCombatConfig.dual_wielding_attack_speed_multiplier;
    }

    public static float dualWieldingMainHandDamageMultiplier(Player p) {
        return nonAdminRestricted(p) ? DEFAULT_DW_MAIN_DMG_MULT : BetterCombatConfig.dual_wielding_main_hand_damage_multiplier;
    }

    public static float dualWieldingOffHandDamageMultiplier(Player p) {
        return nonAdminRestricted(p) ? DEFAULT_DW_OFF_DMG_MULT : BetterCombatConfig.dual_wielding_off_hand_damage_multiplier;
    }
}
