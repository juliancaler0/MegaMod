package com.ultra.megamod.feature.combat.rogues.util;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.rogues.effect.RogueEffects;
import com.ultra.megamod.feature.combat.spell.SpellDefinition;
import com.ultra.megamod.feature.combat.spell.SpellRegistry;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Convenience references to Rogues & Warriors spell definitions.
 * Ported from net.rogues.util.RogueSpells.
 *
 * All spells are registered through {@link SpellRegistry}. This class provides
 * typed references and metadata for rogues-specific code.
 */
public class RogueSpells {

    public enum Book { ROGUE, WARRIOR }

    public record Entry(Identifier id, String title, String description, Book book) {
        public Entry(String name, String title, String description, Book book) {
            this(Identifier.fromNamespaceAndPath(MegaMod.MODID, name), title, description, book);
        }
    }

    public static final List<Entry> entries = new ArrayList<>();
    private static Entry add(Entry entry) {
        entries.add(entry);
        return entry;
    }

    // Rogue spells
    public static final Entry SLICE_AND_DICE = add(new Entry("slice_and_dice", "Slice and Dice",
            "Increases attack damage per melee hit", Book.ROGUE));
    public static final Entry SHOCK_POWDER = add(new Entry("shock_powder", "Shock Powder",
            "Stuns nearby enemies", Book.ROGUE));
    public static final Entry SHADOW_STEP = add(new Entry("shadow_step", "Shadow Step",
            "Teleport behind an enemy", Book.ROGUE));
    public static final Entry VANISH = add(new Entry("vanish", "Vanish",
            "Enter stealth, becoming invisible to enemies", Book.ROGUE));

    // Warrior spells
    public static final Entry SHATTERING_THROW = add(new Entry("shattering_throw", "Shattering Throw",
            "Throw weapon to shatter enemy armor", Book.WARRIOR));
    public static final Entry SHOUT = add(new Entry("shout", "Shout",
            "Demoralize nearby enemies, reducing their damage", Book.WARRIOR));
    public static final Entry CHARGE = add(new Entry("charge", "Charge",
            "Gain speed and knockback resistance, cleanse movement impairments", Book.WARRIOR));

    // Shared melee skill (from RPG Series / SpellEngine)
    public static final Entry WHIRLWIND = add(new Entry("whirlwind", "Whirlwind",
            "Hold to spin around, dealing damage to nearby enemies", Book.WARRIOR));
}
