package com.ultra.megamod.feature.citizen.colonyblocks;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.CreativeModeTab;
import com.ultra.megamod.feature.relics.weapons.RpgWeaponItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * DeferredRegister hub for ALL colony items:
 * scrolls, supply items, rally banner, tools, weapons, meshes, hammers, etc.
 */
public class ColonyItemRegistry {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("megamod");
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
        DeferredRegister.create((ResourceKey) Registries.CREATIVE_MODE_TAB, "megamod");

    // ==================== Scrolls (stack to 16) ====================

    public static final DeferredItem<Item> SCROLL_TP = ITEMS.registerItem("scroll_tp",
        p -> new ItemScrollTP((Item.Properties) p),
        () -> new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> SCROLL_AREA_TP = ITEMS.registerItem("scroll_area_tp",
        p -> new ItemScrollAreaTP((Item.Properties) p),
        () -> new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> SCROLL_BUFF = ITEMS.registerItem("scroll_buff",
        p -> new ItemScrollBuff((Item.Properties) p),
        () -> new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> SCROLL_GUARD_HELP = ITEMS.registerItem("scroll_guard_help",
        p -> new ItemScrollGuardHelp((Item.Properties) p),
        () -> new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> SCROLL_HIGHLIGHT = ITEMS.registerItem("scroll_highlight",
        p -> new ItemScrollHighlight((Item.Properties) p),
        () -> new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> RESOURCE_SCROLL = ITEMS.registerItem("resource_scroll",
        p -> new ItemResourceScroll((Item.Properties) p),
        () -> new Item.Properties().stacksTo(1));

    // ==================== Supply Items (stack to 1) ====================

    public static final DeferredItem<Item> SUPPLY_CHEST = ITEMS.registerItem("supply_chest",
        p -> new ItemSupplyShipDeployer((Item.Properties) p),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> SUPPLY_CAMP = ITEMS.registerItem("supply_camp",
        p -> new ItemSupplyCampDeployer((Item.Properties) p),
        () -> new Item.Properties().stacksTo(1));

    // ==================== Rally Banner (stack to 1) ====================

    public static final DeferredItem<Item> BANNER_RALLY_GUARDS = ITEMS.registerItem("banner_rally_guards",
        p -> new ItemRallyBanner((Item.Properties) p),
        () -> new Item.Properties().stacksTo(1));

    // ==================== Misc Items ====================

    public static final DeferredItem<Item> ANCIENT_TOME = ITEMS.registerItem("ancient_tome",
        p -> new ItemAncientTome((Item.Properties) p),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> COMPOST = ITEMS.registerSimpleItem("colony_compost_item",
        () -> new Item.Properties().stacksTo(64));
    public static final DeferredItem<Item> MISTLETOE = ITEMS.registerSimpleItem("mistletoe",
        () -> new Item.Properties().stacksTo(64));
    public static final DeferredItem<Item> MAGIC_POTION = ITEMS.registerItem("magic_potion",
        p -> new ItemMagicPotion((Item.Properties) p),
        () -> new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> COLONY_MAP = ITEMS.registerItem("colony_map",
        p -> new ItemColonyMap((Item.Properties) p),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> QUEST_LOG = ITEMS.registerItem("quest_log",
        p -> new ItemQuestLog((Item.Properties) p),
        () -> new Item.Properties().stacksTo(1));

    // ==================== Sifter Meshes (durability) ====================

    public static final DeferredItem<Item> MESH_STRING = ITEMS.registerSimpleItem("mesh_string",
        () -> new Item.Properties().stacksTo(1).durability(500));
    public static final DeferredItem<Item> MESH_FLINT = ITEMS.registerSimpleItem("mesh_flint",
        () -> new Item.Properties().stacksTo(1).durability(1000));
    public static final DeferredItem<Item> MESH_IRON = ITEMS.registerSimpleItem("mesh_iron",
        () -> new Item.Properties().stacksTo(1).durability(1500));
    public static final DeferredItem<Item> MESH_DIAMOND = ITEMS.registerSimpleItem("mesh_diamond",
        () -> new Item.Properties().stacksTo(1).durability(2000));

    // ==================== Assistant Hammers (durability) ====================

    public static final DeferredItem<Item> HAMMER_GOLD = ITEMS.registerSimpleItem("hammer_gold",
        () -> new Item.Properties().stacksTo(1).durability(200));
    public static final DeferredItem<Item> HAMMER_IRON = ITEMS.registerSimpleItem("hammer_iron",
        () -> new Item.Properties().stacksTo(1).durability(400));
    public static final DeferredItem<Item> HAMMER_DIAMOND = ITEMS.registerSimpleItem("hammer_diamond",
        () -> new Item.Properties().stacksTo(1).durability(1000));

    // ==================== Weapons ====================

    public static final DeferredItem<Item> CHIEF_SWORD = ITEMS.registerItem("chief_sword",
        p -> new RpgWeaponItem("Chief Sword", 8.0f, (Item.Properties) p, java.util.List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> IRON_SCIMITAR = ITEMS.registerItem("iron_scimitar",
        p -> new RpgWeaponItem("Iron Scimitar", 6.0f, (Item.Properties) p, java.util.List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> PHARAO_SCEPTER = ITEMS.registerItem("pharao_scepter",
        p -> new RpgWeaponItem("Pharao Scepter", 9.0f, (Item.Properties) p, java.util.List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> SPEAR = ITEMS.registerItem("colony_spear",
        p -> new RpgWeaponItem("Colony Spear", 5.0f, (Item.Properties) p, java.util.List.of()),
        () -> new Item.Properties().stacksTo(1));

    // ==================== Plate Armor (between iron and diamond stats) ====================

    public static final DeferredItem<Item> PLATE_ARMOR_HELMET = ITEMS.registerItem("plate_armor_helmet",
        p -> new Item((Item.Properties) p),
        () -> new Item.Properties().stacksTo(1).durability(275)
            .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).setAsset(EquipmentAssets.IRON).build()));
    public static final DeferredItem<Item> PLATE_ARMOR_CHESTPLATE = ITEMS.registerItem("plate_armor_chestplate",
        p -> new Item((Item.Properties) p),
        () -> new Item.Properties().stacksTo(1).durability(400)
            .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.CHEST).setAsset(EquipmentAssets.IRON).build()));
    public static final DeferredItem<Item> PLATE_ARMOR_LEGGINGS = ITEMS.registerItem("plate_armor_leggings",
        p -> new Item((Item.Properties) p),
        () -> new Item.Properties().stacksTo(1).durability(375)
            .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.LEGS).setAsset(EquipmentAssets.IRON).build()));
    public static final DeferredItem<Item> PLATE_ARMOR_BOOTS = ITEMS.registerItem("plate_armor_boots",
        p -> new Item((Item.Properties) p),
        () -> new Item.Properties().stacksTo(1).durability(250)
            .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.FEET).setAsset(EquipmentAssets.IRON).build()));

    // ==================== Pirate Armor Set 1 (Captain) ====================

    public static final DeferredItem<Item> PIRATE_HAT = ITEMS.registerItem("pirate_hat",
        p -> new Item((Item.Properties) p),
        () -> new Item.Properties().stacksTo(1).durability(200)
            .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).setAsset(EquipmentAssets.LEATHER).build()));
    public static final DeferredItem<Item> PIRATE_TOP = ITEMS.registerItem("pirate_top",
        p -> new Item((Item.Properties) p),
        () -> new Item.Properties().stacksTo(1).durability(300)
            .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.CHEST).setAsset(EquipmentAssets.LEATHER).build()));
    public static final DeferredItem<Item> PIRATE_LEGGINGS = ITEMS.registerItem("pirate_leggings",
        p -> new Item((Item.Properties) p),
        () -> new Item.Properties().stacksTo(1).durability(280)
            .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.LEGS).setAsset(EquipmentAssets.LEATHER).build()));
    public static final DeferredItem<Item> PIRATE_BOOTS = ITEMS.registerItem("pirate_boots",
        p -> new Item((Item.Properties) p),
        () -> new Item.Properties().stacksTo(1).durability(195)
            .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.FEET).setAsset(EquipmentAssets.LEATHER).build()));

    // ==================== Pirate Armor Set 2 (Crew) ====================

    public static final DeferredItem<Item> PIRATE_CAP = ITEMS.registerItem("pirate_cap",
        p -> new Item((Item.Properties) p),
        () -> new Item.Properties().stacksTo(1).durability(165)
            .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).setAsset(EquipmentAssets.LEATHER).build()));
    public static final DeferredItem<Item> PIRATE_CHEST = ITEMS.registerItem("pirate_chest",
        p -> new Item((Item.Properties) p),
        () -> new Item.Properties().stacksTo(1).durability(240)
            .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.CHEST).setAsset(EquipmentAssets.LEATHER).build()));
    public static final DeferredItem<Item> PIRATE_LEGS = ITEMS.registerItem("pirate_legs",
        p -> new Item((Item.Properties) p),
        () -> new Item.Properties().stacksTo(1).durability(225)
            .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.LEGS).setAsset(EquipmentAssets.LEATHER).build()));
    public static final DeferredItem<Item> PIRATE_SHOES = ITEMS.registerItem("pirate_shoes",
        p -> new Item((Item.Properties) p),
        () -> new Item.Properties().stacksTo(1).durability(160)
            .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.FEET).setAsset(EquipmentAssets.LEATHER).build()));

    // ==================== Santa Hat ====================

    public static final DeferredItem<Item> SANTA_HAT = ITEMS.registerItem("santa_hat",
        p -> new Item((Item.Properties) p),
        () -> new Item.Properties().stacksTo(1).durability(150)
            .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).setAsset(EquipmentAssets.LEATHER).build()));

    // ==================== Clipboard ====================

    public static final DeferredItem<Item> CLIPBOARD = ITEMS.registerItem("clipboard",
        p -> new ItemClipboard((Item.Properties) p),
        () -> new Item.Properties().stacksTo(1));

    // ==================== Calipers ====================

    public static final DeferredItem<Item> CALIPERS = ITEMS.registerItem("calipers",
        p -> new ItemCalipers((Item.Properties) p),
        () -> new Item.Properties().stacksTo(1));

    // ==================== Fire Arrow ====================

    public static final DeferredItem<Item> FIRE_ARROW = ITEMS.registerItem("fire_arrow",
        p -> new ItemFireArrow((Item.Properties) p),
        () -> new Item.Properties().stacksTo(64));

    // ==================== Creative Tab ====================

    public static final Supplier<CreativeModeTab> COLONY_ITEMS_TAB = CREATIVE_MODE_TABS.register("megamod_colony_items_tab",
        () -> CreativeModeTab.builder()
            .title((Component) Component.literal((String) "MegaMod - Colony Items"))
            .icon(() -> new ItemStack((ItemLike) Items.PAPER))
            .displayItems((parameters, output) -> {
                // Scrolls
                output.accept((ItemLike) SCROLL_TP.get());
                output.accept((ItemLike) SCROLL_AREA_TP.get());
                output.accept((ItemLike) SCROLL_BUFF.get());
                output.accept((ItemLike) SCROLL_GUARD_HELP.get());
                output.accept((ItemLike) SCROLL_HIGHLIGHT.get());
                output.accept((ItemLike) RESOURCE_SCROLL.get());
                // Supply
                output.accept((ItemLike) SUPPLY_CHEST.get());
                output.accept((ItemLike) SUPPLY_CAMP.get());
                // Rally
                output.accept((ItemLike) BANNER_RALLY_GUARDS.get());
                // Misc
                output.accept((ItemLike) ANCIENT_TOME.get());
                output.accept((ItemLike) COMPOST.get());
                output.accept((ItemLike) MISTLETOE.get());
                output.accept((ItemLike) MAGIC_POTION.get());
                output.accept((ItemLike) COLONY_MAP.get());
                output.accept((ItemLike) QUEST_LOG.get());
                // Meshes
                output.accept((ItemLike) MESH_STRING.get());
                output.accept((ItemLike) MESH_FLINT.get());
                output.accept((ItemLike) MESH_IRON.get());
                output.accept((ItemLike) MESH_DIAMOND.get());
                // Hammers
                output.accept((ItemLike) HAMMER_GOLD.get());
                output.accept((ItemLike) HAMMER_IRON.get());
                output.accept((ItemLike) HAMMER_DIAMOND.get());
                // Weapons
                output.accept((ItemLike) CHIEF_SWORD.get());
                output.accept((ItemLike) IRON_SCIMITAR.get());
                output.accept((ItemLike) PHARAO_SCEPTER.get());
                output.accept((ItemLike) SPEAR.get());
                // Plate Armor
                output.accept((ItemLike) PLATE_ARMOR_HELMET.get());
                output.accept((ItemLike) PLATE_ARMOR_CHESTPLATE.get());
                output.accept((ItemLike) PLATE_ARMOR_LEGGINGS.get());
                output.accept((ItemLike) PLATE_ARMOR_BOOTS.get());
                // Pirate Armor Set 1 (Captain)
                output.accept((ItemLike) PIRATE_HAT.get());
                output.accept((ItemLike) PIRATE_TOP.get());
                output.accept((ItemLike) PIRATE_LEGGINGS.get());
                output.accept((ItemLike) PIRATE_BOOTS.get());
                // Pirate Armor Set 2 (Crew)
                output.accept((ItemLike) PIRATE_CAP.get());
                output.accept((ItemLike) PIRATE_CHEST.get());
                output.accept((ItemLike) PIRATE_LEGS.get());
                output.accept((ItemLike) PIRATE_SHOES.get());
                // Santa Hat
                output.accept((ItemLike) SANTA_HAT.get());
                // Clipboard
                output.accept((ItemLike) CLIPBOARD.get());
                // Fire Arrow
                output.accept((ItemLike) FIRE_ARROW.get());
                // Calipers
                output.accept((ItemLike) CALIPERS.get());
            }).build());

    // ==================== Init ====================

    public static void init(IEventBus modBus) {
        ITEMS.register(modBus);
        CREATIVE_MODE_TABS.register(modBus);
    }
}
