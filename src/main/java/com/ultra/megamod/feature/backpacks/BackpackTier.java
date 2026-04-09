package com.ultra.megamod.feature.backpacks;

public enum BackpackTier {
    LEATHER("Leather", 27, 2, 2),
    IRON("Iron", 45, 3, 3),
    GOLD("Gold", 63, 4, 4),
    DIAMOND("Diamond", 81, 5, 5),
    NETHERITE("Netherite", 99, 6, 6);

    private final String displayName;
    private final int storageSlots;
    private final int upgradeSlots;
    private final int toolSlots;

    BackpackTier(String displayName, int storageSlots, int upgradeSlots, int toolSlots) {
        this.displayName = displayName;
        this.storageSlots = storageSlots;
        this.upgradeSlots = upgradeSlots;
        this.toolSlots = toolSlots;
    }

    public String getDisplayName() { return displayName; }
    public int getStorageSlots() { return storageSlots; }
    public int getUpgradeSlots() { return upgradeSlots; }
    public int getToolSlots() { return toolSlots; }
    public int getStorageRows() { return storageSlots / 9; }

    public static BackpackTier fromString(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return LEATHER;
        }
    }
}
