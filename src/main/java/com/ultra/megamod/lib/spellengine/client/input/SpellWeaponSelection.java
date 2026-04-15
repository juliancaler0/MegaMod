package com.ultra.megamod.lib.spellengine.client.input;

/**
 * Client-side spell index selection for held weapons with multi-spell containers.
 * Cycled by holding R + mouse wheel. The selected index is used by
 * {@link SpellHotbar#update} to rotate the weapon's spell list so the chosen
 * spell becomes the one bound to the use-key (right-click).
 *
 * <p>This is pure client state — no persistence. Reset on relog or world change
 * is acceptable; the HUD shows the current selection so players reorient quickly.</p>
 */
public final class SpellWeaponSelection {
    private static int selectedIndex = 0;

    private SpellWeaponSelection() {}

    public static int getSelectedIndex() {
        return selectedIndex;
    }

    /** Clamp / wrap the selection against {@code total} available spells. */
    public static int clampedIndex(int total) {
        if (total <= 0) return 0;
        int i = selectedIndex % total;
        if (i < 0) i += total;
        return i;
    }

    public static void cycleNext(int total) {
        if (total <= 0) { selectedIndex = 0; return; }
        selectedIndex = (selectedIndex + 1) % total;
    }

    public static void cyclePrev(int total) {
        if (total <= 0) { selectedIndex = 0; return; }
        selectedIndex = (selectedIndex - 1 + total) % total;
    }

    public static void reset() {
        selectedIndex = 0;
    }
}
