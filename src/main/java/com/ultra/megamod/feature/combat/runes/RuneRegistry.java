package com.ultra.megamod.feature.combat.runes;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.runes.pouch.RunePouchItem;
import com.ultra.megamod.feature.combat.runes.pouch.RunePouchType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
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
            props -> new RunePouchItem((Item.Properties) props, RunePouchType.SMALL),
            () -> new Item.Properties().stacksTo(1));

    public static final DeferredItem<Item> MEDIUM_RUNE_POUCH = ITEMS.registerItem("medium_rune_pouch",
            props -> new RunePouchItem((Item.Properties) props, RunePouchType.MEDIUM),
            () -> new Item.Properties().stacksTo(1));

    public static final DeferredItem<Item> LARGE_RUNE_POUCH = ITEMS.registerItem("large_rune_pouch",
            props -> new RunePouchItem((Item.Properties) props, RunePouchType.LARGE),
            () -> new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    // ══════════════════════════════════════════════
    // Menu type — client opens the pouch GUI via this
    // ══════════════════════════════════════════════

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, MegaMod.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<com.ultra.megamod.feature.combat.runes.pouch.RunePouchMenu>> RUNE_POUCH_MENU =
            MENUS.register("rune_pouch", () ->
                    net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(
                            (id, inv, buf) -> {
                                int size = buf.readVarInt();
                                boolean mainHand = buf.readBoolean();
                                var pouchStack = mainHand ? inv.player.getMainHandItem() : inv.player.getOffhandItem();
                                return com.ultra.megamod.feature.combat.runes.pouch.RunePouchMenu.create(id, inv, size, pouchStack);
                            }));

    public static void init(IEventBus modBus) {
        ITEMS.register(modBus);
        MENUS.register(modBus);
        // Wire TYPE on the menu class so the constructor knows which type to supertype-register
        modBus.addListener((net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent e) -> {
            com.ultra.megamod.feature.combat.runes.pouch.RunePouchMenu.TYPE = RUNE_POUCH_MENU.get();
        });
    }

    /** Client-side: register the pouch screen factory. Invoked from MegaModClient. */
    public static void onRegisterMenuScreens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event) {
        event.register(RUNE_POUCH_MENU.get(), com.ultra.megamod.feature.combat.runes.pouch.RunePouchScreen::new);
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

        int remaining = count;

        // Check rune pouches first (RunePouchItems in inventory)
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            var stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof com.ultra.megamod.feature.combat.runes.pouch.RunePouchItem) {
                remaining -= com.ultra.megamod.feature.combat.runes.pouch.RunePouchStorage
                        .consumeFromPouch(stack, runeItem, remaining);
            }
        }

        // Then loose runes in inventory
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            var stack = player.getInventory().getItem(i);
            if (stack.is(runeItem)) {
                int toRemove = Math.min(remaining, stack.getCount());
                stack.shrink(toRemove);
                remaining -= toRemove;
            }
        }

        return remaining <= 0;
    }
}
