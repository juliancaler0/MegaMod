package com.ultra.megamod.feature.citizen.colonyblocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Decoration controller block — serves as a blueprint anchor for decoration placement.
 * Stores the schematic name and corner positions in its block entity.
 * Right-click to view/configure the decoration schematic.
 * Level property (0-4) determines decoration tier.
 */
public class BlockDecorationController extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<BlockDecorationController> CODEC =
        BlockDecorationController.simpleCodec(BlockDecorationController::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 4);

    public BlockDecorationController(BlockBehaviour.Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(LEVEL, 0));
    }

    @Override
    protected MapCodec<? extends BlockDecorationController> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{FACING, LEVEL});
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileEntityDecorationController(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TileEntityDecorationController controller) {
            String schematic = controller.getSchematicName();
            int lvl = state.getValue(LEVEL);
            ServerPlayer sp = (ServerPlayer) player;

            if (schematic.isEmpty()) {
                sp.displayClientMessage(
                    Component.literal("\u00a7eDecoration Controller (Level " + lvl + ") \u2014 No schematic assigned."), true);
            } else {
                BlockPos c1 = controller.getCorner1();
                BlockPos c2 = controller.getCorner2();
                boolean hasCorners = !c1.equals(BlockPos.ZERO) || !c2.equals(BlockPos.ZERO);

                if (hasCorners) {
                    sp.displayClientMessage(
                        Component.literal("\u00a7aDecoration: \u00a7f" + schematic + " \u00a7a(Level " + lvl + ")"
                            + " \u00a77[" + c1.getX() + "," + c1.getY() + "," + c1.getZ()
                            + " \u2192 " + c2.getX() + "," + c2.getY() + "," + c2.getZ() + "]"), true);
                } else {
                    sp.displayClientMessage(
                        Component.literal("\u00a7aDecoration: \u00a7f" + schematic + " \u00a7a(Level " + lvl + ")"), true);
                }
            }
        }
        return InteractionResult.CONSUME;
    }
}
