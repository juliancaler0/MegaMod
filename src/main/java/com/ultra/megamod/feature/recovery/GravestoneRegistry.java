package com.ultra.megamod.feature.recovery;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class GravestoneRegistry {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks("megamod");
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("megamod");
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "megamod");

    public static final DeferredBlock<GravestoneBlock> GRAVESTONE = BLOCKS.registerBlock("gravestone",
        GravestoneBlock::new,
        () -> BlockBehaviour.Properties.of()
            .mapColor(MapColor.STONE)
            .strength(2.5f)
            .noOcclusion());

    public static final DeferredItem<BlockItem> GRAVESTONE_ITEM =
        ITEMS.registerSimpleBlockItem("gravestone", GRAVESTONE);

    public static final Supplier<BlockEntityType<GravestoneBlockEntity>> GRAVESTONE_BE =
        BLOCK_ENTITIES.register("gravestone",
            () -> new BlockEntityType<>(GravestoneBlockEntity::new, new Block[]{GRAVESTONE.get()}));

    public static void init(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
    }
}
