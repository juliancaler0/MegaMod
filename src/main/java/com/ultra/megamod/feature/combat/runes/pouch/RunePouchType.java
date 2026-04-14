package com.ultra.megamod.feature.combat.runes.pouch;

import com.ultra.megamod.feature.combat.runes.RuneRegistry;
import net.minecraft.world.item.Item;

public enum RunePouchType {
    SMALL(9, "Small Rune Pouch"),
    MEDIUM(18, "Medium Rune Pouch"),
    LARGE(27, "Large Rune Pouch");

    private final int slots;
    private final String displayName;

    RunePouchType(int slots, String displayName) {
        this.slots = slots;
        this.displayName = displayName;
    }

    public int slots() { return slots; }
    public String displayName() { return displayName; }

    public static RunePouchType of(Item item) {
        if (item == RuneRegistry.MEDIUM_RUNE_POUCH.get()) return MEDIUM;
        if (item == RuneRegistry.LARGE_RUNE_POUCH.get()) return LARGE;
        return SMALL;
    }
}
