package com.ultra.megamod.feature.hud.combos;

import java.util.*;

/**
 * Defines ability elemental tags and combo recipes.
 * When two abilities with matching tags are cast within 8 seconds,
 * a combo triggers with bonus effects.
 */
public class CombatComboRegistry {

    public enum AbilityTag {
        FIRE, ICE, SHIELD, ATTACK, HEAL, SHADOW, LIGHTNING, ARCANE
    }

    public record ComboDefinition(String id, String displayName, AbilityTag tag1, AbilityTag tag2,
                                   int color, String effectDescription) {}

    // Ability name -> tag mapping (populated from known relic ability names)
    private static final Map<String, AbilityTag> ABILITY_TAGS = new HashMap<>();

    // All defined combos
    private static final List<ComboDefinition> ALL_COMBOS = List.of(
        new ComboDefinition("shatter", "SHATTER!", AbilityTag.FIRE, AbilityTag.ICE, 0xFF00DDFF, "AoE frost-fire burst"),
        new ComboDefinition("counter_strike", "COUNTER STRIKE!", AbilityTag.SHIELD, AbilityTag.ATTACK, 0xFFFFAA00, "2x damage on next hit"),
        new ComboDefinition("life_drain", "LIFE DRAIN!", AbilityTag.HEAL, AbilityTag.ATTACK, 0xFF44FF44, "Heal 50% of damage dealt for 8s"),
        new ComboDefinition("deep_freeze", "DEEP FREEZE!", AbilityTag.ICE, AbilityTag.ICE, 0xFF88CCFF, "Freeze nearby mobs 3s"),
        new ComboDefinition("inferno", "INFERNO!", AbilityTag.FIRE, AbilityTag.FIRE, 0xFFFF4400, "AoE fire damage over time"),
        new ComboDefinition("assassinate", "ASSASSINATE!", AbilityTag.SHADOW, AbilityTag.ATTACK, 0xFF8800AA, "Next hit ignores armor"),
        new ComboDefinition("overcharge", "OVERCHARGE!", AbilityTag.LIGHTNING, AbilityTag.ARCANE, 0xFFFFFF00, "+50% ability damage 5s"),
        new ComboDefinition("sanctify", "SANCTIFY!", AbilityTag.HEAL, AbilityTag.HEAL, 0xFFFFFFAA, "AoE heal to nearby players"),
        new ComboDefinition("fortress", "FORTRESS!", AbilityTag.SHIELD, AbilityTag.SHIELD, 0xFFAAAAAA, "Damage immunity 2s"),
        new ComboDefinition("arcane_surge", "ARCANE SURGE!", AbilityTag.ARCANE, AbilityTag.ATTACK, 0xFFDD88FF, "+25% XP gain 10s")
    );

    static {
        // Map known ability names to tags
        // Fire abilities
        for (String s : List.of("Meteor Strike", "Flame Burst", "Fire Wave", "Dragon Breath", "Lava Surge",
                "Elven Harvest", "Searing Impact")) {
            ABILITY_TAGS.put(s, AbilityTag.FIRE);
        }
        // Ice abilities
        for (String s : List.of("Frost Nova", "Ice Spike", "Blizzard", "Glacial Wall", "Frozen Tomb")) {
            ABILITY_TAGS.put(s, AbilityTag.ICE);
        }
        // Shield abilities
        for (String s : List.of("Iron Guard", "Barrier", "Shield Bash", "Fortress Stance", "Guardian Aura",
                "Aegis Shield")) {
            ABILITY_TAGS.put(s, AbilityTag.SHIELD);
        }
        // Attack abilities
        for (String s : List.of("Power Strike", "Cleave", "Whirlwind", "Execute", "Mounting Strike",
                "Swirling Reap", "Fury", "Blade Storm", "Thunder Clap", "Crushing Blow")) {
            ABILITY_TAGS.put(s, AbilityTag.ATTACK);
        }
        // Heal abilities
        for (String s : List.of("Healing Touch", "Rejuvenation", "Life Bloom", "Nature's Grace",
                "Mending Wave", "Holy Light")) {
            ABILITY_TAGS.put(s, AbilityTag.HEAL);
        }
        // Shadow abilities
        for (String s : List.of("Shadow Strike", "Void Bolt", "Dark Pulse", "Phantom Step",
                "Soul Drain", "Eclipse")) {
            ABILITY_TAGS.put(s, AbilityTag.SHADOW);
        }
        // Lightning abilities
        for (String s : List.of("Lightning Bolt", "Chain Lightning", "Thunder Strike", "Storm Call",
                "Shock Wave", "Static Charge")) {
            ABILITY_TAGS.put(s, AbilityTag.LIGHTNING);
        }
        // Arcane abilities
        for (String s : List.of("Arcane Blast", "Mana Surge", "Mystic Orb", "Arcane Barrage",
                "Spell Weave", "Runic Shield")) {
            ABILITY_TAGS.put(s, AbilityTag.ARCANE);
        }
    }

    public static AbilityTag getTag(String abilityName) {
        return ABILITY_TAGS.get(abilityName);
    }

    /**
     * Check if two tags form a combo. Returns the combo definition or null.
     */
    public static ComboDefinition findCombo(AbilityTag tag1, AbilityTag tag2) {
        for (ComboDefinition combo : ALL_COMBOS) {
            if ((combo.tag1() == tag1 && combo.tag2() == tag2) ||
                (combo.tag1() == tag2 && combo.tag2() == tag1)) {
                return combo;
            }
        }
        return null;
    }

    public static List<ComboDefinition> getAllCombos() {
        return ALL_COMBOS;
    }
}
