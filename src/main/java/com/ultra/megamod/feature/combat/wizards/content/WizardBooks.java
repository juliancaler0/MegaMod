package com.ultra.megamod.feature.combat.wizards.content;

import java.util.List;

/**
 * Wizard spell book school tags.
 * Ported from Wizards mod - these correspond to the three wizard schools:
 * Arcane, Fire, and Frost.
 */
public class WizardBooks {
    public static final List<String> SCHOOLS = List.of("arcane", "fire", "frost");

    public static void register() {
        // Books are registered via data-driven spell tags and spell book items.
        // The spell book items are in SpellItemRegistry; this class provides
        // the school list for reference.
    }
}
