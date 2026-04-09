package com.ultra.megamod.feature.citizen.data;

import net.minecraft.nbt.CompoundTag;

import java.util.EnumMap;
import java.util.Map;

/**
 * Per-citizen skill levels and XP, using the MegaColonies 11-skill system.
 *
 * <h2>XP Distribution</h2>
 * When a citizen earns XP for a primary skill, related skills are also affected:
 * <ul>
 *   <li>Primary skill: 100% of XP</li>
 *   <li>Primary's complimentary: +10% of primary XP</li>
 *   <li>Primary's adverse: -10% of primary XP</li>
 *   <li>Secondary skill: 50% of XP</li>
 *   <li>Secondary's complimentary: +5% of primary XP</li>
 *   <li>Secondary's adverse: -5% of primary XP</li>
 * </ul>
 *
 * <h2>XP Formula</h2>
 * <pre>
 *   localXp = baseXp * (1 + (buildingLevel + hutLevel) / 10.0)
 *   localXp += localXp * (intelligenceLevel / 100.0)
 * </pre>
 *
 * <h2>Level-Up Curve</h2>
 * Exponential: XP required for level N = N^2 * 100
 */
public class CitizenSkillData {

    /** Maximum skill level a citizen can reach. */
    public static final int MAX_LEVEL = 100;

    private final Map<CitizenSkill, Integer> levels = new EnumMap<>(CitizenSkill.class);
    private final Map<CitizenSkill, Double> xp = new EnumMap<>(CitizenSkill.class);

    public CitizenSkillData() {
        // Initialize all skills to level 1, 0 XP
        for (CitizenSkill skill : CitizenSkill.values()) {
            levels.put(skill, 1);
            xp.put(skill, 0.0);
        }
    }

    // ---- Getters ----

    /**
     * Returns the current level of the given skill (default 1).
     */
    public int getLevel(CitizenSkill skill) {
        return levels.getOrDefault(skill, 1);
    }

    /**
     * Returns the current accumulated XP for the given skill (default 0.0).
     */
    public double getXp(CitizenSkill skill) {
        return xp.getOrDefault(skill, 0.0);
    }

    /**
     * Returns the XP required to reach the given level.
     * Formula: level^2 * 100 (exponential curve).
     */
    public static double getXpForLevel(int level) {
        return (double) level * level * 100.0;
    }

    /**
     * Returns the fraction of progress toward the next level (0.0 to 1.0).
     */
    public double getLevelProgress(CitizenSkill skill) {
        int currentLevel = getLevel(skill);
        if (currentLevel >= MAX_LEVEL) return 1.0;
        double currentXp = getXp(skill);
        double xpForCurrent = getXpForLevel(currentLevel);
        double xpForNext = getXpForLevel(currentLevel + 1);
        double range = xpForNext - xpForCurrent;
        if (range <= 0) return 1.0;
        return Math.max(0.0, Math.min(1.0, (currentXp - xpForCurrent) / range));
    }

    // ---- XP Addition ----

    /**
     * Adds experience to a citizen using the MegaColonies XP formula.
     *
     * @param primary           the primary skill for the citizen's current job
     * @param secondary         the secondary skill for the citizen's current job (may be null)
     * @param baseAmount        the raw XP amount earned
     * @param buildingLevel     the level of the building this citizen works in (0 if none)
     * @param hutLevel          the level of the citizen's home/hut (0 if none)
     * @param intelligenceLevel the citizen's current Intelligence skill level
     */
    public void addExperience(CitizenSkill primary, CitizenSkill secondary, double baseAmount,
                              int buildingLevel, int hutLevel, int intelligenceLevel) {
        if (baseAmount <= 0) return;

        // Apply MegaColonies XP formula
        double localXp = baseAmount * (1.0 + (buildingLevel + hutLevel) / 10.0);
        localXp += localXp * (intelligenceLevel / 100.0);

        // Primary skill: 100%
        applyXp(primary, localXp);

        // Primary's complimentary: +10%
        CitizenSkill comp = primary.getComplimentary();
        if (comp != null) {
            applyXp(comp, localXp * 0.10);
        }

        // Primary's adverse: -10%
        CitizenSkill adv = primary.getAdverse();
        if (adv != null) {
            removeXp(adv, localXp * 0.10);
        }

        // Secondary skill: 50%
        if (secondary != null) {
            applyXp(secondary, localXp * 0.50);

            // Secondary's complimentary: +5%
            CitizenSkill secComp = secondary.getComplimentary();
            if (secComp != null) {
                applyXp(secComp, localXp * 0.05);
            }

            // Secondary's adverse: -5%
            CitizenSkill secAdv = secondary.getAdverse();
            if (secAdv != null) {
                removeXp(secAdv, localXp * 0.05);
            }
        }
    }

    /**
     * Adds XP to a skill and checks for level-ups.
     */
    private void applyXp(CitizenSkill skill, double amount) {
        if (amount <= 0) return;
        double current = xp.getOrDefault(skill, 0.0);
        xp.put(skill, current + amount);
        checkLevelUp(skill);
    }

    /**
     * Removes XP from a skill (cannot go below 0 or below current level's base XP).
     */
    private void removeXp(CitizenSkill skill, double amount) {
        if (amount <= 0) return;
        double current = xp.getOrDefault(skill, 0.0);
        int currentLevel = levels.getOrDefault(skill, 1);
        double minXp = getXpForLevel(currentLevel);
        xp.put(skill, Math.max(minXp, current - amount));
    }

    /**
     * Checks if the citizen has enough XP to level up the given skill,
     * and applies the level-up if so. Handles multiple level-ups.
     */
    public void checkLevelUp(CitizenSkill skill) {
        int currentLevel = levels.getOrDefault(skill, 1);
        double currentXp = xp.getOrDefault(skill, 0.0);

        while (currentLevel < MAX_LEVEL) {
            double required = getXpForLevel(currentLevel + 1);
            if (currentXp >= required) {
                currentLevel++;
                levels.put(skill, currentLevel);
            } else {
                break;
            }
        }
    }

    // ---- Direct Setters (for admin/testing) ----

    /**
     * Directly sets a skill level. XP is set to the base XP for that level.
     */
    public void setLevel(CitizenSkill skill, int level) {
        int clamped = Math.max(1, Math.min(MAX_LEVEL, level));
        levels.put(skill, clamped);
        xp.put(skill, getXpForLevel(clamped));
    }

    /**
     * Directly sets XP for a skill and recalculates level.
     */
    public void setXp(CitizenSkill skill, double amount) {
        xp.put(skill, Math.max(0.0, amount));
        // Recalculate level from XP
        int newLevel = 1;
        while (newLevel < MAX_LEVEL && amount >= getXpForLevel(newLevel + 1)) {
            newLevel++;
        }
        levels.put(skill, newLevel);
    }

    // ---- NBT Persistence ----

    /**
     * Saves all skill levels and XP to a CompoundTag.
     */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        for (CitizenSkill skill : CitizenSkill.values()) {
            CompoundTag skillTag = new CompoundTag();
            skillTag.putInt("level", levels.getOrDefault(skill, 1));
            skillTag.putDouble("xp", xp.getOrDefault(skill, 0.0));
            tag.put(skill.name(), skillTag);
        }
        return tag;
    }

    /**
     * Loads skill levels and XP from a CompoundTag.
     */
    public void load(CompoundTag tag) {
        if (tag == null) return;
        for (CitizenSkill skill : CitizenSkill.values()) {
            CompoundTag skillTag = tag.getCompoundOrEmpty(skill.name());
            if (!skillTag.isEmpty()) {
                levels.put(skill, skillTag.getIntOr("level", 1));
                xp.put(skill, skillTag.getDoubleOr("xp", 0.0));
            } else {
                levels.put(skill, 1);
                xp.put(skill, 0.0);
            }
        }
    }

    /**
     * Returns a summary string for debugging.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("CitizenSkillData{");
        for (CitizenSkill skill : CitizenSkill.values()) {
            sb.append(skill.getDisplayName()).append("=")
              .append(getLevel(skill)).append("(").append(String.format("%.1f", getXp(skill))).append("xp), ");
        }
        if (sb.length() > 2) sb.setLength(sb.length() - 2);
        sb.append("}");
        return sb.toString();
    }
}
