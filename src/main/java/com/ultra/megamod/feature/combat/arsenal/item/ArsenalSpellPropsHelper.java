package com.ultra.megamod.feature.combat.arsenal.item;

import com.ultra.megamod.lib.spellengine.api.spell.SpellDataComponents;
import com.ultra.megamod.lib.spellengine.api.spell.container.SpellChoice;
import com.ultra.megamod.lib.spellengine.api.spell.container.SpellContainer;
import com.ultra.megamod.lib.spellengine.rpg_series.item.RangedWeapon;
import com.ultra.megamod.lib.spellengine.rpg_series.item.Shield;
import com.ultra.megamod.lib.spellengine.rpg_series.item.Weapon;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

/**
 * Phase 25 — re-attaches SpellContainer (and SpellChoice) data components to the
 * Arsenal {@code unique_*} weapons / bows / shields whose actual {@link Item}
 * registrations live on {@code RelicRegistry} as plain {@code RpgWeaponItem}.
 *
 * <p>The {@link ArsenalWeapons}, {@link ArsenalBows} and {@link ArsenalShields}
 * classes hold the canonical {@code Weapon.Entry} / {@code RangedWeapon.Entry}
 * / {@code Shield.Entry} factory configs (with their {@code .spellContainer(...)}
 * calls). Those entry classes are never fed through the source-mod
 * {@code Weapons.register(...) → Weapon.java:220 → props.component(SPELL_CONTAINER, ...)}
 * pipeline, so the registered items had no spell container component and could
 * not cast.
 *
 * <p>This helper indexes the entry collections by item name and exposes a
 * {@link #applyTo(Item.Properties, String)} method that, called from each
 * {@code unique_*} props supplier in {@code RelicRegistry}, mirrors what the
 * source-mod registration path does — attaching {@code SPELL_CONTAINER} (and
 * {@code SPELL_CHOICE}, when present) data components on the props.
 *
 * <p>Item IDs are unchanged. Items without a matching entry (e.g. nightmare
 * {@code _3} variants and weapon types that were never given Arsenal entries)
 * pass through untouched.
 */
public final class ArsenalSpellPropsHelper {
    private ArsenalSpellPropsHelper() {}

    private static volatile boolean indexed = false;
    private static final Map<String, SpellContainer> CONTAINERS = new HashMap<>();
    private static final Map<String, SpellChoice> CHOICES = new HashMap<>();

    private static void ensureIndexed() {
        if (indexed) return;
        synchronized (ArsenalSpellPropsHelper.class) {
            if (indexed) return;
            // Force class-load the Arsenal entry holders so their static
            // initializers populate the `entries` lists.
            try { Class.forName(ArsenalWeapons.class.getName()); } catch (ClassNotFoundException ignored) {}
            try { Class.forName(ArsenalBows.class.getName()); } catch (ClassNotFoundException ignored) {}
            try { Class.forName(ArsenalShields.class.getName()); } catch (ClassNotFoundException ignored) {}

            for (Weapon.Entry e : ArsenalWeapons.entries) {
                if (e.spellContainer != null) CONTAINERS.put(e.name(), e.spellContainer);
                if (e.spellChoice != null) CHOICES.put(e.name(), e.spellChoice);
            }
            for (RangedWeapon.Entry e : ArsenalBows.entries) {
                if (e.spellContainer != null) CONTAINERS.put(e.id().getPath(), e.spellContainer);
            }
            for (Shield.Entry e : ArsenalShields.entries) {
                if (e.spellContainer != null) CONTAINERS.put(e.id().getPath(), e.spellContainer);
            }
            indexed = true;
        }
    }

    /**
     * Attach the Arsenal-defined SpellContainer (and SpellChoice when present)
     * to the given {@link Item.Properties}, keyed by Arsenal item name (the
     * registry path, e.g. {@code "unique_claymore_1"}). If no matching entry is
     * known, returns the props unchanged.
     */
    public static Item.Properties applyTo(Item.Properties props, String itemName) {
        ensureIndexed();
        SpellContainer container = CONTAINERS.get(itemName);
        if (container != null) {
            props = props.component(SpellDataComponents.SPELL_CONTAINER, container);
        }
        SpellChoice choice = CHOICES.get(itemName);
        if (choice != null) {
            props = props.component(SpellDataComponents.SPELL_CHOICE, choice);
        }
        return props;
    }

    /**
     * Convenience that creates a fresh stacks-to-1 {@link Item.Properties} and
     * applies any Arsenal SpellContainer/Choice for the given name. Mirrors the
     * shape of the existing {@code () -> new Item.Properties().stacksTo(1)}
     * suppliers used in {@code RelicRegistry}.
     */
    public static Item.Properties uniqueProps(String itemName) {
        return applyTo(new Item.Properties().stacksTo(1), itemName);
    }
}
