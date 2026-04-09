package com.ultra.megamod.feature.combat.passive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps weapon registry names to their passive trigger effects.
 * Passive triggers fire automatically with a chance when the weapon is used.
 */
public class PassiveTriggerRegistry {

    /**
     * A passive weapon effect that fires with a % chance.
     *
     * @param triggerType when this passive fires
     * @param chance probability (0.0-1.0) per event
     * @param effectId identifier for the effect (maps to PassiveTriggerManager.executeEffect)
     * @param cooldownTicks minimum ticks between activations
     */
    public record PassiveTrigger(PassiveTriggerType triggerType, float chance, String effectId, int cooldownTicks) {}

    // Registry: weapon registry name → list of passive triggers
    private static final Map<String, List<PassiveTrigger>> TRIGGERS = new HashMap<>();

    static {
        // ══════════════════════════════════════════════
        // Arsenal Longbows — ranged passives
        // ══════════════════════════════════════════════
        register("megamod:unique_longbow_1",
                new PassiveTrigger(PassiveTriggerType.ARROW_HIT, 0.25f, "radiance_heal", 40));
        register("megamod:unique_longbow_2",
                new PassiveTrigger(PassiveTriggerType.ARROW_HIT, 0.20f, "wither_apply", 30));
        register("megamod:unique_longbow_sw",
                new PassiveTrigger(PassiveTriggerType.ARROW_HIT, 0.25f, "rampaging_buff", 60));

        // ══════════════════════════════════════════════
        // Arsenal Heavy Crossbows
        // ══════════════════════════════════════════════
        register("megamod:unique_heavy_crossbow_1",
                new PassiveTrigger(PassiveTriggerType.ARROW_HIT, 0.20f, "flame_cloud", 60));
        register("megamod:unique_heavy_crossbow_2",
                new PassiveTrigger(PassiveTriggerType.ARROW_HIT, 0.20f, "poison_cloud", 60));
        register("megamod:unique_heavy_crossbow_sw",
                new PassiveTrigger(PassiveTriggerType.ARROW_HIT, 0.30f, "bonus_shot", 20));

        // ══════════════════════════════════════════════
        // Arsenal Shields
        // ══════════════════════════════════════════════
        register("megamod:unique_shield_1",
                new PassiveTrigger(PassiveTriggerType.ON_BLOCK, 1.0f, "spiked_shield", 10));
        register("megamod:unique_shield_2",
                new PassiveTrigger(PassiveTriggerType.ON_BLOCK, 1.0f, "guarding_shield", 40));
        register("megamod:unique_shield_sw",
                new PassiveTrigger(PassiveTriggerType.ON_BLOCK, 1.0f, "unyielding_shield", 40));

        // ══════════════════════════════════════════════
        // Arsenal Melee Weapons — Claymores
        // ══════════════════════════════════════════════
        register("megamod:unique_claymore_1",
                new PassiveTrigger(PassiveTriggerType.MELEE_IMPACT, 0.15f, "exploding_hit", 40));
        register("megamod:unique_claymore_2",
                new PassiveTrigger(PassiveTriggerType.MELEE_IMPACT, 0.20f, "wither_apply", 30));
        register("megamod:unique_claymore_sw",
                new PassiveTrigger(PassiveTriggerType.MELEE_IMPACT, 0.20f, "shockwave", 60));

        // ══════════════════════════════════════════════
        // Arsenal Melee Weapons — Daggers
        // ══════════════════════════════════════════════
        register("megamod:unique_dagger_1",
                new PassiveTrigger(PassiveTriggerType.MELEE_IMPACT, 0.25f, "slowing_hit", 20));
        register("megamod:unique_dagger_2",
                new PassiveTrigger(PassiveTriggerType.MELEE_IMPACT, 0.20f, "leeching_hit", 10));
        register("megamod:unique_dagger_sw",
                new PassiveTrigger(PassiveTriggerType.MELEE_IMPACT, 0.20f, "stunning_hit", 30));

        // ══════════════════════════════════════════════
        // Arsenal Melee Weapons — Double Axes
        // ══════════════════════════════════════════════
        register("megamod:unique_double_axe_1",
                new PassiveTrigger(PassiveTriggerType.MELEE_IMPACT, 0.15f, "shockwave", 60));
        register("megamod:unique_double_axe_2",
                new PassiveTrigger(PassiveTriggerType.MELEE_IMPACT, 0.15f, "exploding_hit", 40));
        register("megamod:unique_double_axe_sw",
                new PassiveTrigger(PassiveTriggerType.MELEE_IMPACT, 0.25f, "rampaging_buff", 60));

        // ══════════════════════════════════════════════
        // Arsenal Melee Weapons — Glaives
        // ══════════════════════════════════════════════
        register("megamod:unique_glaive_1",
                new PassiveTrigger(PassiveTriggerType.MELEE_IMPACT, 0.20f, "flame_cloud", 60));
        register("megamod:unique_glaive_2",
                new PassiveTrigger(PassiveTriggerType.MELEE_IMPACT, 0.15f, "shockwave", 60));
        register("megamod:unique_glaive_sw",
                new PassiveTrigger(PassiveTriggerType.MELEE_IMPACT, 0.20f, "leeching_hit", 10));

        // ══════════════════════════════════════════════
        // Arsenal Melee Weapons — Hammers
        // ══════════════════════════════════════════════
        register("megamod:unique_hammer_1",
                new PassiveTrigger(PassiveTriggerType.MELEE_IMPACT, 0.20f, "stunning_hit", 40));
        register("megamod:unique_hammer_2",
                new PassiveTrigger(PassiveTriggerType.MELEE_IMPACT, 0.15f, "exploding_hit", 40));
        register("megamod:unique_hammer_sw",
                new PassiveTrigger(PassiveTriggerType.MELEE_IMPACT, 0.25f, "radiance_heal", 40));

        // ══════════════════════════════════════════════
        // Arsenal Melee Weapons — Maces
        // ══════════════════════════════════════════════
        register("megamod:unique_mace_1",
                new PassiveTrigger(PassiveTriggerType.MELEE_IMPACT, 0.20f, "slowing_hit", 20));
        register("megamod:unique_mace_2",
                new PassiveTrigger(PassiveTriggerType.MELEE_IMPACT, 0.20f, "stunning_hit", 40));
        register("megamod:unique_mace_sw",
                new PassiveTrigger(PassiveTriggerType.MELEE_IMPACT, 0.20f, "leeching_hit", 10));

        // ══════════════════════════════════════════════
        // Arsenal Melee Weapons — Sickles
        // ══════════════════════════════════════════════
        register("megamod:unique_sickle_1",
                new PassiveTrigger(PassiveTriggerType.MELEE_IMPACT, 0.20f, "poison_cloud", 60));
        register("megamod:unique_sickle_2",
                new PassiveTrigger(PassiveTriggerType.MELEE_IMPACT, 0.15f, "exploding_hit", 40));
        register("megamod:unique_sickle_sw",
                new PassiveTrigger(PassiveTriggerType.MELEE_IMPACT, 0.20f, "wither_apply", 30));

        // ══════════════════════════════════════════════
        // Arsenal Melee Weapons — Spears
        // ══════════════════════════════════════════════
        register("megamod:unique_spear_1",
                new PassiveTrigger(PassiveTriggerType.MELEE_IMPACT, 0.25f, "slowing_hit", 20));
        register("megamod:unique_spear_2",
                new PassiveTrigger(PassiveTriggerType.MELEE_IMPACT, 0.20f, "stunning_hit", 40));
        register("megamod:unique_spear_sw",
                new PassiveTrigger(PassiveTriggerType.MELEE_IMPACT, 0.20f, "leeching_hit", 10));

        // ══════════════════════════════════════════════
        // Arsenal Melee Weapons — Longsword
        // ══════════════════════════════════════════════
        register("megamod:unique_longsword_sw",
                new PassiveTrigger(PassiveTriggerType.MELEE_IMPACT, 0.20f, "rampaging_buff", 60));

        // ══════════════════════════════════════════════
        // Arsenal Staves — no combat passives (spell weapons)
        // Staves use spell casting, not melee passives
        // ══════════════════════════════════════════════
    }

    private static void register(String weaponId, PassiveTrigger trigger) {
        TRIGGERS.computeIfAbsent(weaponId, k -> new ArrayList<>()).add(trigger);
    }

    public static List<PassiveTrigger> getTriggersForWeapon(String weaponId) {
        return TRIGGERS.getOrDefault(weaponId, List.of());
    }

    public static List<PassiveTrigger> getTriggersForWeapon(String weaponId, PassiveTriggerType type) {
        return TRIGGERS.getOrDefault(weaponId, List.of()).stream()
                .filter(t -> t.triggerType() == type)
                .toList();
    }
}
