package com.ultra.megamod.feature.citizen.block;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Registry for the Town Chest block, block entity, menu, and item.
 */
public class TownChestRegistry {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks("megamod");
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("megamod");
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
            Registries.BLOCK_ENTITY_TYPE, "megamod");
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(
            (ResourceKey) Registries.MENU, "megamod");

    // Block
    public static final DeferredBlock<TownChestBlock> TOWN_CHEST = BLOCKS.registerBlock(
            "town_chest",
            TownChestBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(3.0f)
                    .noOcclusion());

    // Item
    public static final DeferredItem<BlockItem> TOWN_CHEST_ITEM = ITEMS.registerSimpleBlockItem(
            "town_chest", TOWN_CHEST);

    // Block Entity
    public static final Supplier<BlockEntityType<TownChestBlockEntity>> TOWN_CHEST_BE =
            BLOCK_ENTITIES.register("town_chest_be",
                    () -> new BlockEntityType<>(TownChestBlockEntity::new, TOWN_CHEST.get()));

    // Menu
    public static final Supplier<MenuType<TownChestMenu>> TOWN_CHEST_MENU = MENUS.register("town_chest",
            () -> IMenuTypeExtension.create(TownChestMenu::new));

    public static void init(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        MENUS.register(modBus);
    }

    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(TOWN_CHEST_MENU.get(), TownChestScreen::new);
    }
}
