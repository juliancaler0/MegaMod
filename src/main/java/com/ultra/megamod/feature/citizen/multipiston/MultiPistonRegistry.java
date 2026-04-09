package com.ultra.megamod.feature.citizen.multipiston;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Registry for the Multi-Piston block, block item, block entity, and network.
 */
public class MultiPistonRegistry {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks("megamod");
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("megamod");
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "megamod");

    // Block
    public static final DeferredBlock<MultiPistonBlock> MULTI_PISTON = BLOCKS.registerBlock(
        "multi_piston", MultiPistonBlock::new,
        () -> BlockBehaviour.Properties.of()
            .mapColor(MapColor.STONE)
            .strength(3.5f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops());

    // Block Item
    public static final DeferredItem<BlockItem> MULTI_PISTON_ITEM = ITEMS.registerSimpleBlockItem(
        "multi_piston", MULTI_PISTON);

    // Block Entity — must use lazy .get() inside the supplier to avoid accessing unbound DeferredBlock
    public static Supplier<BlockEntityType<MultiPistonBlockEntity>> MULTI_PISTON_BE;

    // Init
    public static void init(IEventBus modBus) {
        // Register BE type here so the DeferredBlock is not accessed during static init
        MULTI_PISTON_BE = BLOCK_ENTITIES.register("multi_piston_be",
            () -> new BlockEntityType<>(MultiPistonBlockEntity::new, MULTI_PISTON.get()));
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        modBus.addListener(MultiPistonNetwork::registerPayloads);
    }
}
