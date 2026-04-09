package com.ultra.megamod.feature.combat.runes;

import com.ultra.megamod.MegaMod;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registry for rune stone items. Runes are consumable spell reagents used
 * as optional costs for high-tier spells. Each rune type corresponds to
 * a spell school.
 *
 * Runes drop from dungeon loot and can be crafted from dungeon materials.
 */
public class RuneRegistry {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MegaMod.MODID);

    // ══════════════════════════════════════════════
    // Rune Stone Items — one per spell school
    // ══════════════════════════════════════════════

    public static final DeferredItem<Item> ARCANE_RUNE = ITEMS.registerSimpleItem("arcane_rune",
            () -> new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<Item> FIRE_RUNE = ITEMS.registerSimpleItem("fire_rune",
            () -> new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<Item> FROST_RUNE = ITEMS.registerSimpleItem("frost_rune",
            () -> new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<Item> HEALING_RUNE = ITEMS.registerSimpleItem("healing_rune",
            () -> new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<Item> LIGHTNING_RUNE = ITEMS.registerSimpleItem("lightning_rune",
            () -> new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<Item> SOUL_RUNE = ITEMS.registerSimpleItem("soul_rune",
            () -> new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON));

    // ══════════════════════════════════════════════
    // RUNE POUCHES — portable rune storage (bundle-based)
    // ══════════════════════════════════════════════

    public static final DeferredItem<Item> SMALL_RUNE_POUCH = ITEMS.registerItem("small_rune_pouch",
            props -> new BundleItem((Item.Properties) props),
            () -> new Item.Properties().stacksTo(1));

    public static final DeferredItem<Item> MEDIUM_RUNE_POUCH = ITEMS.registerItem("medium_rune_pouch",
            props -> new BundleItem((Item.Properties) props),
            () -> new Item.Properties().stacksTo(1));

    public static final DeferredItem<Item> LARGE_RUNE_POUCH = ITEMS.registerItem("large_rune_pouch",
            props -> new BundleItem((Item.Properties) props),
            () -> new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static void init(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    /**
     * Returns the rune item for a given spell school name, or null if no rune matches.
     */
    public static Item getRuneForSchool(String schoolName) {
        if (schoolName == null) return null;
        return switch (schoolName.toUpperCase()) {
            case "ARCANE" -> ARCANE_RUNE.get();
            case "FIRE" -> FIRE_RUNE.get();
            case "FROST" -> FROST_RUNE.get();
            case "HEALING" -> HEALING_RUNE.get();
            case "LIGHTNING" -> LIGHTNING_RUNE.get();
            case "SOUL" -> SOUL_RUNE.get();
            default -> null;
        };
    }

    /**
     * Checks if the player has at least {@code count} runes of the given school
     * and removes them if found.
     *
     * @return true if the runes were consumed, false if the player didn't have enough
     */
    public static boolean consumeRune(net.minecraft.world.entity.player.Player player, String schoolName, int count) {
        Item runeItem = getRuneForSchool(schoolName);
        if (runeItem == null) return true; // No rune requirement for this school

        int found = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            var stack = player.getInventory().getItem(i);
            if (stack.is(runeItem)) {
                found += stack.getCount();
                if (found >= count) break;
            }
        }

        if (found < count) return false;

        // Consume the runes
        int remaining = count;
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            var stack = player.getInventory().getItem(i);
            if (stack.is(runeItem)) {
                int toRemove = Math.min(remaining, stack.getCount());
                stack.shrink(toRemove);
                remaining -= toRemove;
            }
        }
        return true;
    }
}
