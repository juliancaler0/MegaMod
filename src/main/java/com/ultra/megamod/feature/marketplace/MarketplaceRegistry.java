package com.ultra.megamod.feature.marketplace;

import com.ultra.megamod.feature.marketplace.block.TradingTerminalBlock;
import com.ultra.megamod.feature.marketplace.block.TradingTerminalBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class MarketplaceRegistry {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks("megamod");
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("megamod");
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
            (ResourceKey) Registries.BLOCK_ENTITY_TYPE, (String) "megamod");

    public static final DeferredBlock<TradingTerminalBlock> TRADING_TERMINAL = BLOCKS.registerBlock(
            "trading_terminal",
            TradingTerminalBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.0f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
    );

    public static final DeferredItem<BlockItem> TRADING_TERMINAL_ITEM = ITEMS.registerSimpleBlockItem(
            "trading_terminal", TRADING_TERMINAL
    );

    public static final Supplier<BlockEntityType<TradingTerminalBlockEntity>> TRADING_TERMINAL_BE =
            BLOCK_ENTITIES.register("trading_terminal",
                    () -> new BlockEntityType<>(TradingTerminalBlockEntity::new,
                            new Block[]{(Block) TRADING_TERMINAL.get()}));

    public static void init(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
    }
}
