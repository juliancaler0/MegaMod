/*
 * Decompiled with CFR 0.152.
 */
package com.ultra.megamod.feature.skills;

public enum SkillTreeType {
    COMBAT("Combat", "Mastery of weapons and warfare"),
    MINING("Mining", "Expertise in excavation and ore extraction"),
    FARMING("Farming", "Knowledge of agriculture and animal husbandry"),
    ARCANE("Arcane", "Understanding of magical forces and relics"),
    SURVIVAL("Survival", "Endurance, exploration, and environmental mastery");

    private final String displayName;
    private final String description;

    private SkillTreeType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getDescription() {
        return this.description;
    }

    public float getRadialAngle() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> 270.0f;
            case 1 -> 342.0f;
            case 2 -> 54.0f;
            case 3 -> 126.0f;
            case 4 -> 198.0f;
        };
    }

    public String getAbbreviation() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> "CB";
            case 1 -> "MI";
            case 2 -> "FA";
            case 3 -> "AR";
            case 4 -> "SV";
        };
    }

    public int getColor() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> -48060;
            case 1 -> -12276993;
            case 2 -> -12264124;
            case 3 -> -3390209;
            case 4 -> -2250172;
        };
    }

    public int getDimColor() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> -7855582;
            case 1 -> -14527096;
            case 2 -> -14514142;
            case 3 -> -10083704;
            case 4 -> -7838174;
        };
    }

    public static int xpForLevel(int level) {
        return 100 + level * 50;
    }
}

