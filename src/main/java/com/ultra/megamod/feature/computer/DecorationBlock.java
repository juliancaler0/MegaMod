/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.HorizontalDirectionalBlock
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.VoxelShape
 *  net.neoforged.neoforge.network.PacketDistributor
 */
package com.ultra.megamod.feature.computer;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.computer.ComputerBlock;
import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.computer.network.OpenComputerPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.network.PacketDistributor;

public class DecorationBlock
extends HorizontalDirectionalBlock {
    public static final MapCodec<DecorationBlock> CODEC = DecorationBlock.simpleCodec(DecorationBlock::new);
    private static final Map<Block, VoxelShape> SHAPES = new HashMap<Block, VoxelShape>();
    private static final VoxelShape DEFAULT_SHAPE = Block.box((double)2.0, (double)0.0, (double)2.0, (double)14.0, (double)12.0, (double)14.0);
    private final VoxelShape shape;

    public DecorationBlock(BlockBehaviour.Properties props) {
        this(props, Block.box((double)0.0, (double)0.0, (double)0.0, (double)16.0, (double)16.0, (double)16.0));
    }

    public DecorationBlock(BlockBehaviour.Properties props, VoxelShape shape) {
        super(props);
        this.shape = shape;
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue((Property)FACING, (Comparable)Direction.NORTH));
    }

    public static void registerShape(Block block, VoxelShape shape) {
        SHAPES.put(block, shape);
    }

    protected MapCodec<? extends DecorationBlock> codec() {
        return CODEC;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{FACING});
    }

    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return (BlockState)this.defaultBlockState().setValue((Property)FACING, (Comparable)ctx.getHorizontalDirection().getOpposite());
    }

    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPES.getOrDefault((Object)this, DEFAULT_SHAPE);
    }

    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)player;
            for (BlockPos adjacent : BlockPos.betweenClosed((BlockPos)pos.offset(-2, -1, -2), (BlockPos)pos.offset(2, 1, 2))) {
                if (adjacent.equals((Object)pos) || !(level.getBlockState(adjacent).getBlock() instanceof ComputerBlock)) continue;
                boolean isAdmin = AdminSystem.isAdmin(serverPlayer);
                EconomyManager eco = EconomyManager.get((ServerLevel)level);
                int wallet = eco.getWallet(player.getUUID());
                int bank = eco.getBank(player.getUUID());
                PacketDistributor.sendToPlayer((ServerPlayer)serverPlayer, (CustomPacketPayload)new OpenComputerPayload(isAdmin, wallet, bank), (CustomPacketPayload[])new CustomPacketPayload[0]);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}

