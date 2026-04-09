package com.ultra.megamod.feature.backpacks;

import com.ultra.megamod.feature.backpacks.client.BackpackScreen;
import com.ultra.megamod.feature.backpacks.menu.BackpackMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class BackpackRegistry {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("megamod");
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks("megamod");
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "megamod");
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "megamod");
    public static final DeferredRegister<MenuType<?>> MENUS =
        DeferredRegister.create((ResourceKey) Registries.MENU, "megamod");

    public static final Supplier<MenuType<BackpackMenu>> BACKPACK_MENU = MENUS.register("backpack",
        () -> IMenuTypeExtension.create(BackpackMenu::new));

    // Store all variant registrations
    private static final Map<BackpackVariant, DeferredItem<BackpackItem>> BACKPACK_ITEMS = new EnumMap<>(BackpackVariant.class);

    // Tier upgrade items (plain items, not UpgradeItem — they modify the tier, not add features)
    public static final DeferredItem<Item> BLANK_UPGRADE = ITEMS.registerSimpleItem("blank_upgrade", () -> new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> IRON_TIER_UPGRADE = ITEMS.registerSimpleItem("iron_tier_upgrade", () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> GOLD_TIER_UPGRADE = ITEMS.registerSimpleItem("gold_tier_upgrade", () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> DIAMOND_TIER_UPGRADE = ITEMS.registerSimpleItem("diamond_tier_upgrade", () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> NETHERITE_TIER_UPGRADE = ITEMS.registerSimpleItem("netherite_tier_upgrade", () -> new Item.Properties().stacksTo(1));

    // Feature upgrade items (UpgradeItem — place in upgrade slots to add functionality)
    public static final DeferredItem<Item> CRAFTING_UPGRADE = ITEMS.registerItem("crafting_upgrade",
            p -> new com.ultra.megamod.feature.backpacks.upgrade.UpgradeItem(p, "crafting", "Crafting",
                    com.ultra.megamod.feature.backpacks.upgrade.crafting.CraftingUpgrade::new));
    public static final DeferredItem<Item> FURNACE_UPGRADE = ITEMS.registerItem("furnace_upgrade",
            p -> new com.ultra.megamod.feature.backpacks.upgrade.UpgradeItem(p, "furnace", "Furnace",
                    com.ultra.megamod.feature.backpacks.upgrade.smelting.FurnaceUpgrade::new));
    public static final DeferredItem<Item> SMOKER_UPGRADE = ITEMS.registerItem("smoker_upgrade",
            p -> new com.ultra.megamod.feature.backpacks.upgrade.UpgradeItem(p, "smoker", "Smoker",
                    com.ultra.megamod.feature.backpacks.upgrade.smelting.SmokerUpgrade::new));
    public static final DeferredItem<Item> BLAST_FURNACE_UPGRADE = ITEMS.registerItem("blast_furnace_upgrade",
            p -> new com.ultra.megamod.feature.backpacks.upgrade.UpgradeItem(p, "blast_furnace", "Blast Furnace",
                    com.ultra.megamod.feature.backpacks.upgrade.smelting.BlastFurnaceUpgrade::new));
    public static final DeferredItem<Item> TANKS_UPGRADE = ITEMS.registerItem("tanks_upgrade",
            p -> new com.ultra.megamod.feature.backpacks.upgrade.UpgradeItem(p, "tanks", "Tanks",
                    com.ultra.megamod.feature.backpacks.upgrade.tanks.TanksUpgrade::new));
    public static final DeferredItem<Item> PICKUP_UPGRADE = ITEMS.registerItem("pickup_upgrade",
            p -> new com.ultra.megamod.feature.backpacks.upgrade.UpgradeItem(p, "auto_pickup", "Auto-Pickup",
                    com.ultra.megamod.feature.backpacks.upgrade.pickup.AutoPickupUpgrade::new));
    public static final DeferredItem<Item> MAGNET_UPGRADE = ITEMS.registerItem("magnet_upgrade",
            p -> new com.ultra.megamod.feature.backpacks.upgrade.UpgradeItem(p, "magnet", "Magnet",
                    com.ultra.megamod.feature.backpacks.upgrade.magnet.MagnetUpgrade::new));
    public static final DeferredItem<Item> JUKEBOX_UPGRADE = ITEMS.registerItem("jukebox_upgrade",
            p -> new com.ultra.megamod.feature.backpacks.upgrade.UpgradeItem(p, "jukebox", "Jukebox",
                    com.ultra.megamod.feature.backpacks.upgrade.jukebox.JukeboxUpgrade::new));
    public static final DeferredItem<Item> VOID_UPGRADE = ITEMS.registerItem("void_upgrade",
            p -> new com.ultra.megamod.feature.backpacks.upgrade.UpgradeItem(p, "void", "Void",
                    com.ultra.megamod.feature.backpacks.upgrade.voiding.VoidUpgrade::new));
    public static final DeferredItem<Item> FEEDING_UPGRADE = ITEMS.registerItem("feeding_upgrade",
            p -> new com.ultra.megamod.feature.backpacks.upgrade.UpgradeItem(p, "feeding", "Feeding",
                    com.ultra.megamod.feature.backpacks.upgrade.feeding.FeedingUpgrade::new));
    public static final DeferredItem<Item> REFILL_UPGRADE = ITEMS.registerItem("refill_upgrade",
            p -> new com.ultra.megamod.feature.backpacks.upgrade.UpgradeItem(p, "refill", "Refill",
                    com.ultra.megamod.feature.backpacks.upgrade.refill.RefillUpgrade::new));

    // Fluid Hose utility item
    public static final DeferredItem<HoseItem> HOSE = ITEMS.registerItem("hose", p -> new HoseItem(p));

    // ==========================================
    // --- Backpack Block (single block, variant stored in block entity) ---
    // ==========================================
    public static final DeferredBlock<BackpackBlock> BACKPACK_BLOCK = BLOCKS.registerBlock(
        "backpack_block",
        BackpackBlock::new,
        () -> BlockBehaviour.Properties.of()
            .mapColor(MapColor.WOOL)
            .strength(0.8f)
            .noOcclusion());

    // Block Entity Type
    public static final Supplier<BlockEntityType<BackpackBlockEntity>> BACKPACK_BE =
        BLOCK_ENTITIES.register("backpack_be",
            () -> new BlockEntityType<>(BackpackBlockEntity::new, BACKPACK_BLOCK.get()));

    // ==========================================
    // --- Sleeping Bag Items (16 colors, functional skip-night) ---
    // ==========================================
    private static final String[] DYE_COLORS = {
        "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
        "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"
    };

    private static final List<DeferredItem<Item>> SLEEPING_BAG_ITEMS = new ArrayList<>();

    static {
        // Register all 42 backpack variants
        for (BackpackVariant variant : BackpackVariant.values()) {
            String regName = variant.getRegistryName();
            final BackpackVariant v = variant;
            DeferredItem<BackpackItem> item = ITEMS.registerItem(regName,
                props -> new BackpackItem(v, props.stacksTo(1)));
            BACKPACK_ITEMS.put(variant, item);
        }

        // Register 16 sleeping bag items (functional — skip night on use)
        for (String color : DYE_COLORS) {
            final String c = color;
            DeferredItem<Item> bag = ITEMS.registerItem(color + "_sleeping_bag",
                props -> new SleepingBagItem(c, props.stacksTo(1)));
            SLEEPING_BAG_ITEMS.add(bag);
        }
    }

    public static DeferredItem<BackpackItem> getItem(BackpackVariant variant) {
        return BACKPACK_ITEMS.get(variant);
    }

    public static List<DeferredItem<Item>> getSleepingBagItems() {
        return SLEEPING_BAG_ITEMS;
    }

    // Creative tab
    public static final Supplier<CreativeModeTab> BACKPACK_TAB = CREATIVE_MODE_TABS.register("backpacks",
        () -> CreativeModeTab.builder()
            .title(Component.literal("MegaMod Backpacks"))
            .icon(() -> new ItemStack(BACKPACK_ITEMS.get(BackpackVariant.STANDARD).get()))
            .displayItems((params, output) -> {
                for (BackpackVariant variant : BackpackVariant.values()) {
                    DeferredItem<BackpackItem> item = BACKPACK_ITEMS.get(variant);
                    if (item != null) {
                        output.accept((ItemLike) item.get());
                    }
                }
                // Upgrade items
                output.accept((ItemLike) BLANK_UPGRADE.get());
                output.accept((ItemLike) IRON_TIER_UPGRADE.get());
                output.accept((ItemLike) GOLD_TIER_UPGRADE.get());
                output.accept((ItemLike) DIAMOND_TIER_UPGRADE.get());
                output.accept((ItemLike) NETHERITE_TIER_UPGRADE.get());
                output.accept((ItemLike) CRAFTING_UPGRADE.get());
                output.accept((ItemLike) FURNACE_UPGRADE.get());
                output.accept((ItemLike) SMOKER_UPGRADE.get());
                output.accept((ItemLike) BLAST_FURNACE_UPGRADE.get());
                output.accept((ItemLike) TANKS_UPGRADE.get());
                output.accept((ItemLike) PICKUP_UPGRADE.get());
                output.accept((ItemLike) MAGNET_UPGRADE.get());
                output.accept((ItemLike) JUKEBOX_UPGRADE.get());
                output.accept((ItemLike) VOID_UPGRADE.get());
                output.accept((ItemLike) FEEDING_UPGRADE.get());
                output.accept((ItemLike) REFILL_UPGRADE.get());
                // Hose
                output.accept((ItemLike) HOSE.get());
                // Sleeping bags
                for (DeferredItem<Item> bag : SLEEPING_BAG_ITEMS) {
                    output.accept((ItemLike) bag.get());
                }
            }).build());

    public static void init(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        CREATIVE_MODE_TABS.register(modBus);
        MENUS.register(modBus);
    }

    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(BACKPACK_MENU.get(), BackpackScreen::new);
    }
}
