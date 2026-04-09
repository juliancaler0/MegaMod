/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.chunk.ChunkGenerator
 *  net.minecraft.world.level.material.MapColor
 *  net.minecraft.world.level.material.PushReaction
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.neoforge.registries.DeferredBlock
 *  net.neoforged.neoforge.registries.DeferredItem
 *  net.neoforged.neoforge.registries.DeferredRegister
 *  net.neoforged.neoforge.registries.DeferredRegister$Blocks
 *  net.neoforged.neoforge.registries.DeferredRegister$Items
 */
package com.ultra.megamod.feature.dimensions;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.dimensions.BlankChunkGenerator;
import com.ultra.megamod.feature.dimensions.PortalBlock;
import com.ultra.megamod.feature.dimensions.PortalBlockEntity;
import com.ultra.megamod.feature.dimensions.network.DimensionNetwork;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DimensionRegistry {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks((String)"megamod");
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems((String)"megamod");
    public static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS = DeferredRegister.create((ResourceKey)Registries.CHUNK_GENERATOR, (String)"megamod");
    public static final Supplier<MapCodec<BlankChunkGenerator>> BLANK_GENERATOR = CHUNK_GENERATORS.register("blank", () -> BlankChunkGenerator.CODEC);
    public static final DeferredBlock<PortalBlock> PORTAL_BLOCK = BLOCKS.registerBlock("pocket_portal", PortalBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(-1.0f, 3600000.0f).noOcclusion().noLootTable().pushReaction(PushReaction.BLOCK).lightLevel(state -> 11));
    public static final DeferredItem<BlockItem> PORTAL_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("pocket_portal", PORTAL_BLOCK);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "megamod");
    public static final Supplier<BlockEntityType<PortalBlockEntity>> PORTAL_BE = BLOCK_ENTITIES.register("portal_be",
        () -> new BlockEntityType<>(PortalBlockEntity::new, PORTAL_BLOCK.get()));

    public static void init(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        CHUNK_GENERATORS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        modBus.addListener(DimensionNetwork::registerPayloads);
    }
}

