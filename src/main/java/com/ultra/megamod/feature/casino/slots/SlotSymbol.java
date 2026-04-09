package com.ultra.megamod.feature.casino.slots;

public enum SlotSymbol {
    DIAMOND(500, 0xFFB9F2FF, "Diamond", "diamond"),
    EMERALD(100, 0xFF50C878, "Emerald", "emerald"),
    GOLD(50, 0xFFFFD700, "Gold", "gold"),
    IRON(20, 0xFFC0C0C0, "Iron", "iron"),
    REDSTONE(10, 0xFFFF0000, "Redstone", "redstone"),
    LAPIS(8, 0xFF1E90FF, "Lapis", "lapis"),
    COAL(6, 0xFF333333, "Coal", "coal"),
    CHERRY(0, 0xFFFF69B4, "Cherry", "cherry"),
    SKULL(0, 0xFF888888, "Skull", "skull");

    private final int tripleMultiplier;
    private final int color;
    private final String displayName;
    private final String textureId;

    SlotSymbol(int tripleMultiplier, int color, String displayName, String textureId) {
        this.tripleMultiplier = tripleMultiplier;
        this.color = color;
        this.displayName = displayName;
        this.textureId = textureId;
    }

    public int getTripleMultiplier() {
        return tripleMultiplier;
    }

    public int getColor() {
        return color;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Cherry special multiplier based on match count from left.
     * 1 cherry = 2x, 2 cherries = 3x, 3 cherries = 5x.
     */
    public static int getCherryMultiplier(int matchCount) {
        return switch (matchCount) {
            case 1 -> 2;
            case 2 -> 3;
            case 3 -> 5;
            default -> 0;
        };
    }

    /**
     * Returns true for all symbols except SKULL.
     */
    public boolean isWinnable() {
        return this != SKULL;
    }

    public String getTextureId() {
        return textureId;
    }

    /**
     * Returns the texture resource path for use in GUI rendering.
     * Maps to: assets/megamod/textures/gui/slot_machine/symbols/{textureId}.png
     */
    public String getTexturePath() {
        return "megamod:textures/gui/slot_machine/symbols/" + textureId + ".png";
    }
}
