package com.ultra.megamod.feature.combat.spell.client;

/**
 * Client-side tracker for the currently selected spell in the spell book HUD.
 * Resets when the player switches spell books or puts one away.
 */
public class SpellBookSelection {

    private static int selectedIndex = 0;
    private static String lastBookSchool = "";

    /**
     * Cycle forward through the spell list.
     */
    public static void cycleNext(int spellCount) {
        if (spellCount <= 0) return;
        selectedIndex = (selectedIndex + 1) % spellCount;
    }

    /**
     * Cycle backward through the spell list.
     */
    public static void cyclePrev(int spellCount) {
        if (spellCount <= 0) return;
        selectedIndex = (selectedIndex - 1 + spellCount) % spellCount;
    }

    public static int getSelected() {
        return selectedIndex;
    }

    public static void setSelected(int index) {
        selectedIndex = Math.max(0, index);
    }

    /**
     * Reset selection to 0. Called when the book changes or is removed.
     */
    public static void reset() {
        selectedIndex = 0;
        lastBookSchool = "";
    }

    /**
     * Track whether the book school changed so we can reset the index.
     */
    public static String getLastBookSchool() {
        return lastBookSchool;
    }

    public static void setLastBookSchool(String school) {
        lastBookSchool = school;
    }

    /**
     * Clamp the selected index to the valid range (e.g., if spells changed).
     */
    public static void clamp(int spellCount) {
        if (spellCount <= 0) {
            selectedIndex = 0;
        } else if (selectedIndex >= spellCount) {
            selectedIndex = spellCount - 1;
        }
    }
}
