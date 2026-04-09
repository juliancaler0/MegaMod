package com.ultra.megamod.feature.dungeons.generation;

import com.ultra.megamod.feature.dungeons.block.*;
import com.ultra.megamod.feature.dungeons.block.ChaosEdgeBlock;
import com.ultra.megamod.feature.dungeons.block.ChaosVertexBlock;
import com.ultra.megamod.feature.dungeons.block.ChaosBarrierEdgeBlock;
import com.ultra.megamod.feature.dungeons.block.ChaosBarrierVertexBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registers blocks under the "dungeonnowloading" namespace so that block IDs
 * baked into DNL's NBT structure templates resolve correctly at placement time.
 * Without these registrations, any block stored as "dungeonnowloading:cobblestone_pebbles"
 * in the NBTs would become air when placed.
 *
 * These are simple stub blocks using the same block classes we already have
 * under the megamod namespace — they just need to exist in the registry.
 */
public class DNLBlockRegistry {

    private static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks("dungeonnowloading");

    // Decoration blocks
    public static final DeferredBlock<PileBlock> COBBLESTONE_PEBBLES = BLOCKS.registerBlock(
            "cobblestone_pebbles", PileBlock::new,
            () -> BlockBehaviour.Properties.of().instabreak().noOcclusion().sound(SoundType.STONE));

    public static final DeferredBlock<PileBlock> MOSSY_COBBLESTONE_PEBBLES = BLOCKS.registerBlock(
            "mossy_cobblestone_pebbles", PileBlock::new,
            () -> BlockBehaviour.Properties.of().instabreak().noOcclusion().sound(SoundType.STONE));

    public static final DeferredBlock<PileBlock> IRON_INGOT_PILE = BLOCKS.registerBlock(
            "iron_ingot_pile", PileBlock::new,
            () -> BlockBehaviour.Properties.of().instabreak().noOcclusion().sound(SoundType.METAL));

    public static final DeferredBlock<PileBlock> GOLD_INGOT_PILE = BLOCKS.registerBlock(
            "gold_ingot_pile", PileBlock::new,
            () -> BlockBehaviour.Properties.of().instabreak().noOcclusion().sound(SoundType.METAL));

    public static final DeferredBlock<BookPileBlock> BOOK_PILE = BLOCKS.registerBlock(
            "book_pile", BookPileBlock::new,
            () -> BlockBehaviour.Properties.of().instabreak().noOcclusion().sound(SoundType.WOOL));

    public static final DeferredBlock<ExplosiveBarrelBlock> EXPLOSIVE_BARREL = BLOCKS.registerBlock(
            "explosive_barrel", ExplosiveBarrelBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0f, 1.0f).noOcclusion());

    public static final DeferredBlock<DungeonWallTorch> DUNGEON_WALL_TORCH = BLOCKS.registerBlock(
            "dungeon_wall_torch", DungeonWallTorch::new,
            () -> BlockBehaviour.Properties.of().noCollision().instabreak().sound(SoundType.WOOD)
                    .pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<WallPlatformBlock> WOODEN_WALL_PLATFORM = BLOCKS.registerBlock(
            "wooden_wall_platform", WallPlatformBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(3.0f).sound(SoundType.WOOD)
                    .ignitedByLava().noOcclusion());

    public static final DeferredBlock<WallRackBlock> WOODEN_WALL_RACK = BLOCKS.registerBlock(
            "wooden_wall_rack", WallRackBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(3.0f).sound(SoundType.WOOD)
                    .ignitedByLava().noOcclusion());

    public static final DeferredBlock<SpikeBlock> SPIKES = BLOCKS.registerBlock(
            "spikes", SpikeBlock::new,
            () -> BlockBehaviour.Properties.of().strength(3.0f, 6.0f).noOcclusion().sound(SoundType.METAL)
                    .pushReaction(PushReaction.DESTROY));

    // Chaos Spawner structural blocks — breakable with no drops so players can reach the boss
    public static final DeferredBlock<Block> CHAOS_SPAWNER = BLOCKS.registerSimpleBlock(
            "chaos_spawner",
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0f, 6.0f)
                    .sound(SoundType.METAL).noOcclusion().noLootTable().pushReaction(PushReaction.BLOCK));

    public static final DeferredBlock<ChaosEdgeBlock> CHAOS_SPAWNER_EDGE = BLOCKS.registerBlock(
            "chaos_spawner_edge", ChaosEdgeBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0f, 6.0f)
                    .sound(SoundType.METAL).noOcclusion().noLootTable().pushReaction(PushReaction.BLOCK));

    public static final DeferredBlock<ChaosEdgeBlock> CHAOS_SPAWNER_DIAMOND_EDGE = BLOCKS.registerBlock(
            "chaos_spawner_diamond_edge", ChaosEdgeBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(4.0f, 8.0f)
                    .sound(SoundType.METAL).noOcclusion().noLootTable().pushReaction(PushReaction.BLOCK));

    public static final DeferredBlock<ChaosVertexBlock> CHAOS_SPAWNER_DIAMOND_VERTEX = BLOCKS.registerBlock(
            "chaos_spawner_diamond_vertex", ChaosVertexBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(4.0f, 8.0f)
                    .sound(SoundType.METAL).noOcclusion().noLootTable().pushReaction(PushReaction.BLOCK));

    public static final DeferredBlock<Block> CHAOS_SPAWNER_BARRIER_CENTER = BLOCKS.registerSimpleBlock(
            "chaos_spawner_barrier_center",
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0f, 6.0f)
                    .sound(SoundType.AMETHYST).lightLevel(s -> 15).noOcclusion().noLootTable()
                    .pushReaction(PushReaction.BLOCK));

    public static final DeferredBlock<ChaosBarrierEdgeBlock> CHAOS_SPAWNER_BARRIER_EDGE = BLOCKS.registerBlock(
            "chaos_spawner_barrier_edge", ChaosBarrierEdgeBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0f, 6.0f)
                    .sound(SoundType.AMETHYST).lightLevel(s -> 15).noOcclusion().noLootTable()
                    .pushReaction(PushReaction.BLOCK));

    public static final DeferredBlock<ChaosBarrierVertexBlock> CHAOS_SPAWNER_BARRIER_VERTEX = BLOCKS.registerBlock(
            "chaos_spawner_barrier_vertex", ChaosBarrierVertexBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0f, 6.0f)
                    .sound(SoundType.METAL).lightLevel(s -> 15).noOcclusion().noLootTable()
                    .pushReaction(PushReaction.BLOCK));

    public static void init(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
