package com.ultra.megamod.feature.combat.runes;

/**
 * Interface applied to Player via mixin to track rune crafting sound timing.
 * Prevents overlapping crafting sounds when rapidly crafting runes.
 */
public interface RuneCrafter {
    void setLastRuneCrafted(int time);
    int getLastRuneCrafted();

    default boolean shouldPlayRuneCraftingSound(int age) {
        return age > (getLastRuneCrafted() + RuneCrafting.SOUND_DELAY);
    }

    default void onPlayedRuneCraftingSound(int age) {
        setLastRuneCrafted(age);
    }
}
