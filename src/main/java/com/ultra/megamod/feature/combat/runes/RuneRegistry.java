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
     * and removes them if found. Searches both raw inventory AND rune pouches
     * (BundleItems) so runes stored in pouches are properly consumed.
     *
     * @return true if the runes were consumed, false if the player didn't have enough
     */
    public static boolean consumeRune(net.minecraft.world.entity.player.Player player, String schoolName, int count) {
        Item runeItem = getRuneForSchool(schoolName);
        if (runeItem == null) return true; // No rune requirement for this school

        java.util.function.Predicate<net.minecraft.world.item.ItemStack> runeTest = stack -> stack.is(runeItem);

        // Phase 1: Count available runes across inventory + pouches
        int found = 0;

        // Check rune pouches first (BundleItems in inventory)
        var pouchSources = new java.util.ArrayList<net.minecraft.world.item.ItemStack>();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            var stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof BundleItem) {
                int inPouch = countInBundle(stack, runeTest);
                if (inPouch > 0) {
                    pouchSources.add(stack);
                    found += inPouch;
                }
            }
        }

        // Then check loose runes in inventory
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            var stack = player.getInventory().getItem(i);
            if (stack.is(runeItem)) {
                found += stack.getCount();
            }
        }

        if (found < count) return false;

        // Phase 2: Consume from pouches first, then loose inventory
        int remaining = count;

        // Consume from pouches
        for (var pouchStack : pouchSources) {
            if (remaining <= 0) break;
            remaining -= takeFromBundle(pouchStack, runeTest, remaining);
        }

        // Consume from loose inventory
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

    /**
     * Count how many items matching the predicate are inside a BundleItem stack.
     */
    private static int countInBundle(net.minecraft.world.item.ItemStack containerStack,
                                      java.util.function.Predicate<net.minecraft.world.item.ItemStack> predicate) {
        var bundle = containerStack.get(net.minecraft.core.component.DataComponents.BUNDLE_CONTENTS);
        if (bundle == null) return 0;
        int total = 0;
        for (var item : bundle.items()) {
            if (predicate.test(item)) {
                total += item.getCount();
            }
        }
        return total;
    }

    /**
     * Remove up to {@code amount} matching items from inside a BundleItem stack.
     * @return number of items actually removed
     */
    private static int takeFromBundle(net.minecraft.world.item.ItemStack containerStack,
                                       java.util.function.Predicate<net.minecraft.world.item.ItemStack> predicate,
                                       int amount) {
        var bundle = containerStack.get(net.minecraft.core.component.DataComponents.BUNDLE_CONTENTS);
        if (bundle == null) return 0;
        int taken = 0;
        int toTake = amount;
        var putBack = new java.util.ArrayList<net.minecraft.world.item.ItemStack>();
        for (var storedStack : bundle.items()) {
            if (predicate.test(storedStack) && toTake > 0) {
                int decrementable = Math.min(storedStack.getCount(), toTake);
                storedStack.shrink(decrementable);
                toTake -= decrementable;
                taken += decrementable;
            }
            if (!storedStack.isEmpty()) {
                putBack.add(storedStack);
            }
        }
        // Rebuild the bundle contents
        var freshBundle = new net.minecraft.world.item.component.BundleContents.Mutable(
                net.minecraft.world.item.component.BundleContents.EMPTY);
        for (var stackToAdd : putBack.reversed()) {
            freshBundle.tryInsert(stackToAdd);
        }
        containerStack.set(net.minecraft.core.component.DataComponents.BUNDLE_CONTENTS, freshBundle.toImmutable());
        return taken;
    }
}
