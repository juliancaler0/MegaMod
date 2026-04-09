package com.ultra.megamod.feature.combat.animation.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

/**
 * NeoForge config for the BetterCombat system.
 * Ported 1:1 from BetterCombat's ServerConfig + ClientConfig.
 * Uses ModConfigSpec for proper config file generation and in-game editing.
 *
 * Config file: megamod-combat.toml
 * Also accessible via the Computer admin panel.
 */
public class BetterCombatConfig {

    public static final ModConfigSpec SPEC;
    private static final BetterCombatConfig INSTANCE;

    static {
        final Pair<BetterCombatConfig, ModConfigSpec> pair = new ModConfigSpec.Builder()
                .configure(BetterCombatConfig::new);
        INSTANCE = pair.getLeft();
        SPEC = pair.getRight();
    }

    // ═══════════════════════════════════════════
    // Server Config
    // ═══════════════════════════════════════════
    private final ModConfigSpec.DoubleValue upswingMultiplierCfg;
    private final ModConfigSpec.BooleanValue allowFastAttacksCfg;
    private final ModConfigSpec.BooleanValue allowAttackingMountCfg;
    private final ModConfigSpec.IntValue attackIntervalCapCfg;
    private final ModConfigSpec.BooleanValue allowVanillaSwpCfg;
    private final ModConfigSpec.BooleanValue allowReworkedSwpCfg;
    private final ModConfigSpec.IntValue swpExtraTargetsCfg;
    private final ModConfigSpec.DoubleValue swpMaxPenaltyCfg;
    private final ModConfigSpec.BooleanValue swpPlaysSoundCfg;
    private final ModConfigSpec.BooleanValue swpEmitsParticlesCfg;
    private final ModConfigSpec.BooleanValue allowThruWallsCfg;
    private final ModConfigSpec.DoubleValue moveSpeedWhileAttackingCfg;
    private final ModConfigSpec.BooleanValue moveSpeedSmoothCfg;
    private final ModConfigSpec.BooleanValue moveSpeedMountingCfg;
    private final ModConfigSpec.BooleanValue knockbackReducedCfg;
    private final ModConfigSpec.DoubleValue knockbackThresholdCfg;
    private final ModConfigSpec.DoubleValue comboResetRateCfg;
    private final ModConfigSpec.DoubleValue targetSearchMultCfg;
    private final ModConfigSpec.DoubleValue dualWieldSpeedMultCfg;
    private final ModConfigSpec.DoubleValue dualWieldMainDmgCfg;
    private final ModConfigSpec.DoubleValue dualWieldOffDmgCfg;

    // ═══════════════════════════════════════════
    // Client Config
    // ═══════════════════════════════════════════
    private final ModConfigSpec.BooleanValue holdToAttackCfg;
    private final ModConfigSpec.BooleanValue miningWithWeaponsCfg;
    private final ModConfigSpec.BooleanValue swingThruGrassCfg;
    private final ModConfigSpec.BooleanValue swingThruGrassSmartCfg;
    private final ModConfigSpec.BooleanValue attackInsteadMineCfg;
    private final ModConfigSpec.BooleanValue highlightCrosshairCfg;
    private final ModConfigSpec.BooleanValue showWeaponTrailsCfg;
    private final ModConfigSpec.BooleanValue showArmsFirstPersonCfg;
    private final ModConfigSpec.BooleanValue showOtherHandFirstPersonCfg;
    private final ModConfigSpec.IntValue swingSoundVolumeCfg;

    BetterCombatConfig(ModConfigSpec.Builder builder) {
        builder.push("server");
        upswingMultiplierCfg = builder.comment("Upswing duration multiplier (0.2-1.0)").defineInRange("upswing_multiplier", 0.5, 0.2, 1.0);
        allowFastAttacksCfg = builder.comment("Bypass damage throttling for fast attacks").define("allow_fast_attacks", true);
        allowAttackingMountCfg = builder.comment("Allow attacking currently mounted entity").define("allow_attacking_mount", false);
        attackIntervalCapCfg = builder.comment("Minimum ticks between attacks").defineInRange("attack_interval_cap", 2, 0, 20);
        allowVanillaSwpCfg = builder.comment("Allow vanilla sweeping mechanic").define("allow_vanilla_sweeping", false);
        allowReworkedSwpCfg = builder.comment("Allow reworked sweeping (damage falloff)").define("allow_reworked_sweeping", true);
        swpExtraTargetsCfg = builder.comment("Extra targets before max sweeping penalty").defineInRange("sweeping_extra_target_count", 4, 1, 20);
        swpMaxPenaltyCfg = builder.comment("Max sweeping damage penalty (0.0-1.0)").defineInRange("sweeping_max_penalty", 0.5, 0.0, 1.0);
        swpPlaysSoundCfg = builder.define("sweeping_plays_sound", true);
        swpEmitsParticlesCfg = builder.define("sweeping_emits_particles", true);
        allowThruWallsCfg = builder.comment("Allow attacking through walls").define("allow_attacking_thru_walls", false);
        moveSpeedWhileAttackingCfg = builder.comment("Movement speed multiplier while attacking (0-1)").defineInRange("movement_speed_while_attacking", 0.5, 0.0, 1.0);
        moveSpeedSmoothCfg = builder.define("movement_speed_applied_smoothly", true);
        moveSpeedMountingCfg = builder.define("movement_speed_effected_while_mounting", false);
        knockbackReducedCfg = builder.define("knockback_reduced_for_fast_attacks", true);
        knockbackThresholdCfg = builder.defineInRange("knockback_reduction_threshold", 12.5, 0.0, 40.0);
        comboResetRateCfg = builder.comment("Combo resets after idle * weapon_cooldown ticks").defineInRange("combo_reset_rate", 3.0, 1.0, 10.0);
        targetSearchMultCfg = builder.defineInRange("target_search_range_multiplier", 2.0, 1.0, 5.0);
        dualWieldSpeedMultCfg = builder.defineInRange("dual_wielding_attack_speed_multiplier", 1.2, 0.5, 2.0);
        dualWieldMainDmgCfg = builder.defineInRange("dual_wielding_main_hand_damage_multiplier", 1.0, 0.1, 2.0);
        dualWieldOffDmgCfg = builder.defineInRange("dual_wielding_off_hand_damage_multiplier", 1.0, 0.1, 2.0);
        builder.pop();

        builder.push("client");
        holdToAttackCfg = builder.define("hold_to_attack", true);
        miningWithWeaponsCfg = builder.define("mining_with_weapons", true);
        swingThruGrassCfg = builder.define("swing_thru_grass", true);
        swingThruGrassSmartCfg = builder.define("swing_thru_grass_smart", true);
        attackInsteadMineCfg = builder.define("attack_instead_mine_when_enemies_close", true);
        highlightCrosshairCfg = builder.define("highlight_crosshair", true);
        showWeaponTrailsCfg = builder.define("show_weapon_trails", true);
        showArmsFirstPersonCfg = builder.define("show_arms_first_person", false);
        showOtherHandFirstPersonCfg = builder.define("show_other_hand_first_person", true);
        swingSoundVolumeCfg = builder.defineInRange("weapon_swing_sound_volume", 100, 0, 100);
        builder.pop();
    }

    // ═══════════════════════════════════════════
    // Public static accessors (for compatibility with existing code)
    // ═══════════════════════════════════════════

    // Server
    public static float upswing_multiplier;
    public static boolean allow_fast_attacks;
    public static boolean allow_attacking_mount;
    public static int attack_interval_cap;
    public static boolean allow_vanilla_sweeping;
    public static boolean allow_reworked_sweeping;
    public static int reworked_sweeping_extra_target_count;
    public static float reworked_sweeping_maximum_damage_penalty;
    public static boolean reworked_sweeping_plays_sound;
    public static boolean reworked_sweeping_emits_particles;
    public static boolean allow_attacking_thru_walls;
    public static float movement_speed_while_attacking;
    public static boolean movement_speed_applied_smoothly;
    public static boolean movement_speed_effected_while_mounting;
    public static boolean knockback_reduced_for_fast_attacks;
    public static float knockback_reduction_threshold;
    public static float combo_reset_rate;
    public static float target_search_range_multiplier;
    public static float dual_wielding_attack_speed_multiplier;
    public static float dual_wielding_main_hand_damage_multiplier;
    public static float dual_wielding_off_hand_damage_multiplier;

    // Client
    public static boolean isHoldToAttackEnabled;
    public static boolean isMiningWithWeaponsEnabled;
    public static boolean isSwingThruGrassEnabled;
    public static boolean isSwingThruGrassSmart;
    public static boolean isAttackInsteadOfMineWhenEnemiesCloseEnabled;
    public static boolean isHighlightCrosshairEnabled;
    public static boolean isShowingWeaponTrails;
    public static boolean isShowingArmsInFirstPerson;
    public static boolean isShowingOtherHandFirstPerson;
    public static int weaponSwingSoundVolume;
    public static float legAnimationThreshold = 0;

    /**
     * Sync config values from ModConfigSpec to static fields.
     * Called when config loads or changes.
     */
    public static void syncFromSpec() {
        upswing_multiplier = INSTANCE.upswingMultiplierCfg.get().floatValue();
        allow_fast_attacks = INSTANCE.allowFastAttacksCfg.get();
        allow_attacking_mount = INSTANCE.allowAttackingMountCfg.get();
        attack_interval_cap = INSTANCE.attackIntervalCapCfg.get();
        allow_vanilla_sweeping = INSTANCE.allowVanillaSwpCfg.get();
        allow_reworked_sweeping = INSTANCE.allowReworkedSwpCfg.get();
        reworked_sweeping_extra_target_count = INSTANCE.swpExtraTargetsCfg.get();
        reworked_sweeping_maximum_damage_penalty = INSTANCE.swpMaxPenaltyCfg.get().floatValue();
        reworked_sweeping_plays_sound = INSTANCE.swpPlaysSoundCfg.get();
        reworked_sweeping_emits_particles = INSTANCE.swpEmitsParticlesCfg.get();
        allow_attacking_thru_walls = INSTANCE.allowThruWallsCfg.get();
        movement_speed_while_attacking = INSTANCE.moveSpeedWhileAttackingCfg.get().floatValue();
        movement_speed_applied_smoothly = INSTANCE.moveSpeedSmoothCfg.get();
        movement_speed_effected_while_mounting = INSTANCE.moveSpeedMountingCfg.get();
        knockback_reduced_for_fast_attacks = INSTANCE.knockbackReducedCfg.get();
        knockback_reduction_threshold = INSTANCE.knockbackThresholdCfg.get().floatValue();
        combo_reset_rate = INSTANCE.comboResetRateCfg.get().floatValue();
        target_search_range_multiplier = INSTANCE.targetSearchMultCfg.get().floatValue();
        dual_wielding_attack_speed_multiplier = INSTANCE.dualWieldSpeedMultCfg.get().floatValue();
        dual_wielding_main_hand_damage_multiplier = INSTANCE.dualWieldMainDmgCfg.get().floatValue();
        dual_wielding_off_hand_damage_multiplier = INSTANCE.dualWieldOffDmgCfg.get().floatValue();

        isHoldToAttackEnabled = INSTANCE.holdToAttackCfg.get();
        isMiningWithWeaponsEnabled = INSTANCE.miningWithWeaponsCfg.get();
        isSwingThruGrassEnabled = INSTANCE.swingThruGrassCfg.get();
        isSwingThruGrassSmart = INSTANCE.swingThruGrassSmartCfg.get();
        isAttackInsteadOfMineWhenEnemiesCloseEnabled = INSTANCE.attackInsteadMineCfg.get();
        isHighlightCrosshairEnabled = INSTANCE.highlightCrosshairCfg.get();
        isShowingWeaponTrails = INSTANCE.showWeaponTrailsCfg.get();
        isShowingArmsInFirstPerson = INSTANCE.showArmsFirstPersonCfg.get();
        isShowingOtherHandFirstPerson = INSTANCE.showOtherHandFirstPersonCfg.get();
        weaponSwingSoundVolume = INSTANCE.swingSoundVolumeCfg.get();
    }

    public static float getUpswingMultiplier() {
        return Math.max(0.2F, Math.min(1, upswing_multiplier));
    }

    /**
     * Update a config value programmatically (from admin panel).
     */
    public static void setAndSave(String key, Object value) {
        // This allows the admin panel to change values at runtime
        // The config file will be updated on next save
        switch (key) {
            case "upswing_multiplier" -> INSTANCE.upswingMultiplierCfg.set(((Number) value).doubleValue());
            case "allow_fast_attacks" -> INSTANCE.allowFastAttacksCfg.set((Boolean) value);
            case "hold_to_attack" -> INSTANCE.holdToAttackCfg.set((Boolean) value);
            case "mining_with_weapons" -> INSTANCE.miningWithWeaponsCfg.set((Boolean) value);
            case "swing_thru_grass" -> INSTANCE.swingThruGrassCfg.set((Boolean) value);
            case "movement_speed_while_attacking" -> INSTANCE.moveSpeedWhileAttackingCfg.set(((Number) value).doubleValue());
            case "dual_wielding_attack_speed_multiplier" -> INSTANCE.dualWieldSpeedMultCfg.set(((Number) value).doubleValue());
            case "dual_wielding_main_hand_damage_multiplier" -> INSTANCE.dualWieldMainDmgCfg.set(((Number) value).doubleValue());
            case "dual_wielding_off_hand_damage_multiplier" -> INSTANCE.dualWieldOffDmgCfg.set(((Number) value).doubleValue());
        }
        syncFromSpec();
    }
}
