package com.ultra.megamod.feature.adminmodules;

public enum ModuleCategory {
    COMBAT("Combat", 0xFFF85149),
    MOVEMENT("Movement", 0xFF58A6FF),
    RENDER("Render", 0xFFA371F7),
    PLAYER("Player", 0xFF3FB950),
    WORLD("World", 0xFFD29922),
    MISC("Misc", 0xFF8B949E);

    private final String displayName;
    private final int color;

    ModuleCategory(String displayName, int color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() { return displayName; }
    public int getColor() { return color; }
}
