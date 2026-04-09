/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.BlockHitResult
 *  net.neoforged.neoforge.network.PacketDistributor
 */
package com.ultra.megamod.feature.relics.research;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.relics.RelicItem;
import com.ultra.megamod.feature.relics.data.RelicData;
import com.ultra.megamod.feature.relics.network.OpenRelicScreenPayload;
import com.ultra.megamod.feature.relics.research.RerollPayload;
import com.ultra.megamod.feature.relics.research.ResearchManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;

public class ResearchingTableBlock
extends Block {
    public static final MapCodec<ResearchingTableBlock> CODEC = ResearchingTableBlock.simpleCodec(ResearchingTableBlock::new);
    public static final net.minecraft.world.level.block.state.properties.EnumProperty<net.minecraft.core.Direction> FACING =
            net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

    public ResearchingTableBlock(BlockBehaviour.Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, net.minecraft.core.Direction.NORTH));
    }

    protected MapCodec<? extends ResearchingTableBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    protected InteractionResult useItemOn(ItemStack heldItem, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        Item item = heldItem.getItem();
        if (!(item instanceof RelicItem)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        RelicItem relicItem = (RelicItem)item;
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        ServerPlayer serverPlayer = (ServerPlayer)player;
        if (!RelicData.isInitialized(heldItem)) {
            RelicData.initialize(heldItem, relicItem.getAbilities(), level.random);
        }
        String relicId = BuiltInRegistries.ITEM.getKey(item).toString();
        ResearchManager manager = ResearchManager.get((ServerLevel)level);
        if (!manager.isResearched(serverPlayer.getUUID(), relicId)) {
            manager.research(serverPlayer.getUUID(), relicId);
            serverPlayer.sendSystemMessage(Component.literal(("Researched: " + relicItem.getRelicName() + "!")).withStyle(ChatFormatting.GREEN));
            RelicData.addXp(heldItem, 25);
            level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
            if (level instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)level;
                serverLevel.sendParticles((ParticleOptions)ParticleTypes.ENCHANT, (double)pos.getX() + 0.5, (double)pos.getY() + 1.0, (double)pos.getZ() + 0.5, 30, 0.3, 0.5, 0.3, 0.05);
            }
        }
        PacketDistributor.sendToPlayer((ServerPlayer)serverPlayer, (CustomPacketPayload)new OpenRelicScreenPayload(relicItem.getRelicName()), (CustomPacketPayload[])new CustomPacketPayload[0]);
        return InteractionResult.SUCCESS;
    }

    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        ServerPlayer serverPlayer = (ServerPlayer)player;
        PacketDistributor.sendToPlayer(serverPlayer, new RerollPayload.OpenRerollPayload(true));
        return InteractionResult.SUCCESS;
    }
}

