/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.InsideBlockEffectApplier
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.HorizontalDirectionalBlock
 *  net.minecraft.world.level.block.RenderShape
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.EnumProperty
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.Vec3
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package com.ultra.megamod.feature.dungeons.boss;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FogWallBlock
extends Block {
    public static final MapCodec<FogWallBlock> CODEC = FogWallBlock.simpleCodec(FogWallBlock::new);
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    private static final double REPEL_FORCE = 0.2;

    public FogWallBlock(BlockBehaviour.Properties props) {
        super(props);
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH));
    }

    protected MapCodec<? extends FogWallBlock> codec() {
        return CODEC;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{FACING});
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return (BlockState)this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier applier, boolean intersects) {
        Player player;
        if (entity instanceof Player && (player = (Player)entity).isCreative()) {
            return;
        }
        Direction facing = (Direction)state.getValue(FACING);
        Vec3 repelVec = switch (facing) {
            case Direction.NORTH -> new Vec3(0.0, 0.0, 0.2);
            case Direction.SOUTH -> new Vec3(0.0, 0.0, -0.2);
            case Direction.EAST -> new Vec3(-0.2, 0.0, 0.0);
            case Direction.WEST -> new Vec3(0.2, 0.0, 0.0);
            default -> Vec3.ZERO;
        };
        entity.push(repelVec.x, repelVec.y, repelVec.z);
    }

    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Direction facing = (Direction)state.getValue(FACING);
        double x = (double)pos.getX() + 0.5;
        double y = (double)pos.getY() + random.nextDouble();
        double z = (double)pos.getZ() + 0.5;
        double vx = 0.0;
        double vz = 0.0;
        switch (facing) {
            case NORTH: {
                z = (double)pos.getZ() + 0.1;
                vz = 0.02;
                break;
            }
            case SOUTH: {
                z = (double)pos.getZ() + 0.9;
                vz = -0.02;
                break;
            }
            case EAST: {
                x = (double)pos.getX() + 0.9;
                vx = -0.02;
                break;
            }
            case WEST: {
                x = (double)pos.getX() + 0.1;
                vx = 0.02;
            }
        }
        level.addParticle((ParticleOptions)ParticleTypes.SOUL_FIRE_FLAME, x, y, z, vx, 0.01, vz);
        if (random.nextInt(3) == 0) {
            level.addParticle((ParticleOptions)ParticleTypes.SMOKE, x + random.nextGaussian() * 0.2, y, z + random.nextGaussian() * 0.2, 0.0, 0.02, 0.0);
        }
    }

    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return InteractionResult.CONSUME;
    }

    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0f;
    }
}

