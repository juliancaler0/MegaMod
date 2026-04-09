package com.ultra.megamod.feature.furniture;

import com.mojang.serialization.MapCodec;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FurnitureBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<FurnitureBlock> CODEC = FurnitureBlock.simpleCodec(FurnitureBlock::new);
    private static final Map<Block, Map<Direction, VoxelShape>> SHAPES = new HashMap<>();
    private static final VoxelShape DEFAULT_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);

    public FurnitureBlock(BlockBehaviour.Properties props) {
        super(props);
        this.registerDefaultState((BlockState) this.stateDefinition.any().setValue((Property) FACING, (Comparable) Direction.NORTH));
    }

    public static void registerShape(Block block, VoxelShape northShape) {
        Map<Direction, VoxelShape> rotated = new EnumMap<>(Direction.class);
        rotated.put(Direction.NORTH, northShape);
        rotated.put(Direction.SOUTH, rotateShape(northShape, Direction.SOUTH));
        rotated.put(Direction.WEST, rotateShape(northShape, Direction.WEST));
        rotated.put(Direction.EAST, rotateShape(northShape, Direction.EAST));
        SHAPES.put(block, rotated);
    }

    private static VoxelShape rotateShape(VoxelShape shape, Direction to) {
        double x1 = shape.min(Direction.Axis.X) * 16.0;
        double y1 = shape.min(Direction.Axis.Y) * 16.0;
        double z1 = shape.min(Direction.Axis.Z) * 16.0;
        double x2 = shape.max(Direction.Axis.X) * 16.0;
        double y2 = shape.max(Direction.Axis.Y) * 16.0;
        double z2 = shape.max(Direction.Axis.Z) * 16.0;

        return switch (to) {
            case SOUTH -> Block.box(16.0 - x2, y1, 16.0 - z2, 16.0 - x1, y2, 16.0 - z1);
            case WEST -> Block.box(z1, y1, 16.0 - x2, z2, y2, 16.0 - x1);
            case EAST -> Block.box(16.0 - z2, y1, x1, 16.0 - z1, y2, x2);
            default -> shape;
        };
    }

    protected MapCodec<? extends FurnitureBlock> codec() {
        return CODEC;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{FACING});
    }

    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return (BlockState) this.defaultBlockState().setValue((Property) FACING, (Comparable) ctx.getHorizontalDirection().getOpposite());
    }

    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        Direction facing = state.getValue(FACING);
        Map<Direction, VoxelShape> blockShapes = SHAPES.get(this);
        if (blockShapes != null) {
            return blockShapes.getOrDefault(facing, DEFAULT_SHAPE);
        }
        return DEFAULT_SHAPE;
    }
}
