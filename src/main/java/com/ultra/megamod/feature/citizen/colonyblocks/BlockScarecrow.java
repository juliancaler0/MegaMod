package com.ultra.megamod.feature.citizen.colonyblocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Scarecrow block — marks the center of a farm field for the Farmer citizen.
 * Stores the assigned farmer UUID and field bounds.
 */
public class BlockScarecrow extends Block implements EntityBlock {
    public static final MapCodec<BlockScarecrow> CODEC = BlockScarecrow.simpleCodec(BlockScarecrow::new);

    private static final VoxelShape SHAPE = Shapes.or(
        Block.box(6, 0, 6, 10, 16, 10),  // Post
        Block.box(2, 10, 7, 14, 12, 9)   // Arms
    );

    public BlockScarecrow(BlockBehaviour.Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<? extends BlockScarecrow> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileEntityScarecrow(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TileEntityScarecrow scarecrow) {
            ServerPlayer sp = (ServerPlayer) player;
            String farmer = scarecrow.getAssignedFarmerName();
            BlockPos min = scarecrow.getFieldMin();
            BlockPos max = scarecrow.getFieldMax();
            if (farmer.isEmpty()) {
                sp.displayClientMessage(Component.literal(
                    "\u00a7eScarecrow: No farmer assigned. Place near a Farmer citizen's hut."), true);
            } else {
                sp.displayClientMessage(Component.literal(
                    "\u00a7aScarecrow: Farmer=" + farmer + " Field=(" +
                    min.getX() + "," + min.getZ() + ")-(" + max.getX() + "," + max.getZ() + ")"), true);
            }
        }
        return InteractionResult.CONSUME;
    }
}
