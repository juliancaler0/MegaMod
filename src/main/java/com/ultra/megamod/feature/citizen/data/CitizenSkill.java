package com.ultra.megamod.feature.citizen.data;

import java.util.Locale;

/**
 * The 11 citizen skills in MegaColonies. Each skill has a complimentary skill
 * (gains 10% of primary XP when primary gains XP) and an adverse skill
 * (loses 10% of primary XP when primary gains XP).
 *
 * Circular enum references are handled via lazy string-based resolution.
 */
public enum CitizenSkill {
    ATHLETICS("Athletics", "STRENGTH", "DEXTERITY"),
    STRENGTH("Strength", "ATHLETICS", "AGILITY"),
    DEXTERITY("Dexterity", "AGILITY", "ATHLETICS"),
    AGILITY("Agility", "DEXTERITY", "STRENGTH"),
    STAMINA("Stamina", "KNOWLEDGE", "MANA"),
    KNOWLEDGE("Knowledge", "STAMINA", "CREATIVITY"),
    MANA("Mana", "FOCUS", "STAMINA"),
    FOCUS("Focus", "MANA", "ADAPTABILITY"),
    CREATIVITY("Creativity", "ADAPTABILITY", "KNOWLEDGE"),
    ADAPTABILITY("Adaptability", "CREATIVITY", "FOCUS"),
    INTELLIGENCE("Intelligence", null, null);

    private final String displayName;
    private final String complimentaryName;
    private final String adverseName;

    // Lazily resolved references (populated in static init)
    private CitizenSkill complimentary;
    private CitizenSkill adverse;

    CitizenSkill(String displayName, String complimentaryName, String adverseName) {
        this.displayName = displayName;
        this.complimentaryName = complimentaryName;
        this.adverseName = adverseName;
    }

    static {
        // Resolve circular references after all enum constants are constructed
        for (CitizenSkill skill : values()) {
            if (skill.complimentaryName != null) {
                skill.complimentary = valueOf(skill.complimentaryName);
            }
            if (skill.adverseName != null) {
                skill.adverse = valueOf(skill.adverseName);
            }
        }
    }

    /**
     * Returns the display name for this skill (e.g. "Athletics").
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * The complimentary skill gains 10% of the primary XP earned.
     * Returns null for INTELLIGENCE.
     */
    public CitizenSkill getComplimentary() {
        return complimentary;
    }

    /**
     * The adverse skill loses 10% of the primary XP earned.
     * Returns null for INTELLIGENCE.
     */
    public CitizenSkill getAdverse() {
        return adverse;
    }

    /**
     * Parse a CitizenSkill from a case-insensitive string.
     * Returns null if not found.
     */
    public static CitizenSkill fromString(String name) {
        if (name == null || name.isEmpty()) return null;
        try {
            return valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            // Try matching display name
            for (CitizenSkill skill : values()) {
                if (skill.displayName.equalsIgnoreCase(name)) {
                    return skill;
                }
            }
            return null;
        }
    }
}
