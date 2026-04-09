package com.ultra.megamod.feature.baritone.movement;

import com.ultra.megamod.feature.baritone.pathfinding.CalculationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

/**
 * Static helpers for block validation during pathfinding.
 * Methods accepting CalculationContext are thread-safe (use snapshot).
 * Methods accepting ServerLevel are for server-thread execution only.
 */
public final class MovementHelper {
    private MovementHelper() {}

    // === Thread-safe methods using CalculationContext (for A* cost calculation) ===

    public static boolean canWalkThrough(CalculationContext ctx, BlockPos pos) {
        return canWalkThroughState(ctx.getBlockState(pos));
    }

    public static boolean canWalkOn(CalculationContext ctx, BlockPos pos) {
        return canWalkOnState(ctx.getBlockState(pos));
    }

    public static boolean canBreak(CalculationContext ctx, BlockPos pos) {
        return canBreakState(ctx.getBlockState(pos));
    }

    public static boolean isDanger(CalculationContext ctx, BlockPos pos) {
        return isDangerState(ctx.getBlockState(pos));
    }

    public static double getBreakCost(CalculationContext ctx, BlockPos pos) {
        BlockState state = ctx.getBlockState(pos);
        try {
            float hardness = state.getDestroySpeed(null, pos);
            if (hardness < 0) return ActionCosts.COST_INF;
            if (hardness == 0) return ActionCosts.BREAK_BLOCK_BASE_COST;
            return ActionCosts.BREAK_BLOCK_BASE_COST + hardness * 1.5;
        } catch (Exception e) {
            // Some blocks NPE without a valid BlockGetter — use a safe default
            return ActionCosts.BREAK_BLOCK_BASE_COST + 5.0;
        }
    }

    public static boolean isWater(CalculationContext ctx, BlockPos pos) {
        return ctx.getBlockState(pos).getFluidState().is(Fluids.WATER);
    }

    public static boolean isLava(CalculationContext ctx, BlockPos pos) {
        return ctx.getBlockState(pos).getFluidState().is(Fluids.LAVA);
    }

    public static boolean isClimbable(CalculationContext ctx, BlockPos pos) {
        return isClimbableState(ctx.getBlockState(pos));
    }

    public static boolean isLiquid(CalculationContext ctx, BlockPos pos) {
        return !ctx.getBlockState(pos).getFluidState().isEmpty();
    }

    // === Server-thread methods using ServerLevel (for movement execution) ===

    public static boolean canWalkThrough(ServerLevel level, BlockPos pos) {
        return canWalkThroughState(level.getBlockState(pos));
    }

    public static boolean canWalkOn(ServerLevel level, BlockPos pos) {
        return canWalkOnState(level.getBlockState(pos));
    }

    public static boolean canBreak(ServerLevel level, BlockPos pos) {
        return canBreakState(level.getBlockState(pos));
    }

    public static boolean isDanger(ServerLevel level, BlockPos pos) {
        return isDangerState(level.getBlockState(pos));
    }

    public static boolean isWater(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).getFluidState().is(Fluids.WATER);
    }

    public static boolean isClimbable(ServerLevel level, BlockPos pos) {
        return isClimbableState(level.getBlockState(pos));
    }

    public static boolean isStandable(ServerLevel level, BlockPos feetPos) {
        return canWalkOnState(level.getBlockState(feetPos.below()))
            && canWalkThroughState(level.getBlockState(feetPos))
            && canWalkThroughState(level.getBlockState(feetPos.above()));
    }

    public static double getBreakCost(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        float hardness = state.getDestroySpeed(level, pos);
        if (hardness <= 0) return ActionCosts.COST_INF;
        return ActionCosts.BREAK_BLOCK_BASE_COST + hardness * 1.5;
    }

    // === Shared state-based checks ===

    private static boolean canWalkThroughState(BlockState state) {
        if (state.isAir()) return true;
        Block block = state.getBlock();
        if (block instanceof DoorBlock) return true;
        if (block instanceof TrapDoorBlock) return true;
        if (block instanceof FenceGateBlock) return true;
        if (state.is(BlockTags.FLOWERS) || state.is(BlockTags.SAPLINGS)) return true;
        if (block instanceof TallGrassBlock) return true;
        if (block instanceof TorchBlock || block instanceof SignBlock || block instanceof WallSignBlock) return true;
        if (block instanceof ButtonBlock || block instanceof LeverBlock) return true;
        if (block instanceof SnowLayerBlock) {
            return state.getValue(SnowLayerBlock.LAYERS) <= 1;
        }
        if (block instanceof CarpetBlock) return true;
        // Water/lava are walkable-through for swimming
        if (block instanceof LiquidBlock) return true;
        // Climbable blocks
        if (isClimbableState(state)) return true;
        // Powder snow
        if (block instanceof PowderSnowBlock) return true;
        return false;
    }

    private static boolean canWalkOnState(BlockState state) {
        if (state.isAir()) return false;
        Block block = state.getBlock();
        if (state.isSolidRender()) return true;
        if (block instanceof SlabBlock || block instanceof StairBlock) return true;
        if (block instanceof TransparentBlock) return true;
        if (state.is(BlockTags.LEAVES)) return true;
        if (block instanceof ChestBlock || block instanceof CraftingTableBlock) return true;
        if (block instanceof BedBlock) return true;
        if (block instanceof ShulkerBoxBlock) return true;
        // Scaffolding can be stood on
        if (block instanceof ScaffoldingBlock) return true;
        return false;
    }

    private static boolean canBreakState(BlockState state) {
        if (state.isAir()) return false;
        // Bedrock and unbreakable blocks have hardness < 0
        Block block = state.getBlock();
        if (block == Blocks.BEDROCK || block == Blocks.END_PORTAL_FRAME || block == Blocks.BARRIER
            || block == Blocks.COMMAND_BLOCK || block == Blocks.CHAIN_COMMAND_BLOCK || block == Blocks.REPEATING_COMMAND_BLOCK) {
            return false;
        }
        if (block instanceof ChestBlock || block instanceof ShulkerBoxBlock) return false;
        if (block instanceof BedBlock || block instanceof SpawnerBlock) return false;
        return true;
    }

    private static boolean isDangerState(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof LiquidBlock && state.getFluidState().is(Fluids.LAVA)) return true;
        if (block instanceof FireBlock || block instanceof CampfireBlock) return true;
        if (block instanceof CactusBlock || block instanceof SweetBerryBushBlock) return true;
        if (block instanceof MagmaBlock) return true;
        if (block instanceof PowderSnowBlock) return true;
        return false;
    }

    /**
     * Check if a block state is climbable (ladders, vines, etc.)
     */
    private static boolean isClimbableState(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof LadderBlock) return true;
        if (block instanceof VineBlock) return true;
        if (state.is(BlockTags.CLIMBABLE)) return true;
        return false;
    }
}
