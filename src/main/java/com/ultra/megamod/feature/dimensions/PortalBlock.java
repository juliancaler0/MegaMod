/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  net.minecraft.core.BlockPos
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.InsideBlockEffectApplier
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package com.ultra.megamod.feature.dimensions;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.dimensions.DimensionHelper;
import com.ultra.megamod.feature.dimensions.MegaModDimensions;
import com.ultra.megamod.feature.dungeons.DungeonManager;
import com.ultra.megamod.feature.dungeons.network.DungeonSyncPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PortalBlock
extends Block implements EntityBlock {
    public static final MapCodec<PortalBlock> CODEC = PortalBlock.simpleCodec(PortalBlock::new);
    private static final VoxelShape SHAPE = Block.box((double)0.0, (double)0.0, (double)0.0, (double)16.0, (double)16.0, (double)16.0);
    // Prevents multiple portal blocks from triggering teleportBack on the same tick
    private static final Set<UUID> TELEPORTING_PLAYERS = new HashSet<>();

    public PortalBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    protected MapCodec<? extends PortalBlock> codec() {
        return CODEC;
    }

    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PortalBlockEntity(pos, state);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // Spawn portal particles so the player can see where it is
        for (int i = 0; i < 3; i++) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(ParticleTypes.PORTAL, x, y, z,
                    (random.nextDouble() - 0.5) * 0.5,
                    random.nextDouble() * 0.5,
                    (random.nextDouble() - 0.5) * 0.5);
        }
        if (random.nextInt(4) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(ParticleTypes.REVERSE_PORTAL, x, y, z, 0.0, 0.1, 0.0);
        }
    }

    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean movedByPiston) {
        boolean inPocket;
        if (level.isClientSide()) {
            return;
        }
        if (!(entity instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player = (ServerPlayer)entity;
        boolean bl = inPocket = level.dimension().equals(MegaModDimensions.MUSEUM) || level.dimension().equals(MegaModDimensions.DUNGEON) || level.dimension().equals(MegaModDimensions.CASINO) || level.dimension().equals(MegaModDimensions.RESOURCE) || level.dimension().equals(MegaModDimensions.TRADING);
        if (!inPocket) {
            return;
        }
        if (player.isChangingDimension()) {
            return;
        }
        // Prevent multiple portal blocks from triggering on the same tick
        if (!TELEPORTING_PLAYERS.add(player.getUUID())) {
            return;
        }
        if (level.dimension().equals(MegaModDimensions.DUNGEON)) {
            ServerLevel overworld = player.level().getServer().overworld();
            DungeonManager manager = DungeonManager.get(overworld);
            DungeonManager.DungeonInstance instance = manager.getDungeonForPlayer(player.getUUID());
            if (instance != null) {
                // Teleport all OTHER party members out first before cleanup
                java.util.Set<UUID> allPlayers = new java.util.HashSet<>(instance.partyPlayers);
                allPlayers.add(instance.playerUUID);
                allPlayers.remove(player.getUUID()); // we'll teleport this player below
                for (UUID memberId : allPlayers) {
                    ServerPlayer member = overworld.getServer().getPlayerList().getPlayer(memberId);
                    if (member != null && member.level().dimension().equals(MegaModDimensions.DUNGEON)) {
                        DimensionHelper.teleportBack(member);
                        PacketDistributor.sendToPlayer(member, (CustomPacketPayload) new DungeonSyncPayload("", "", "", 0, 0, false), new CustomPacketPayload[0]);
                        member.sendSystemMessage(net.minecraft.network.chat.Component.literal("Dungeon completed! You have been teleported out.").withStyle(net.minecraft.ChatFormatting.GREEN));
                        TELEPORTING_PLAYERS.add(member.getUUID());
                        player.level().getServer().execute(() -> TELEPORTING_PLAYERS.remove(member.getUUID()));
                    }
                }
                manager.cleanupDungeon(instance.instanceId, overworld);
                PacketDistributor.sendToPlayer(player, (CustomPacketPayload) new DungeonSyncPayload("", "", "", 0, 0, false), new CustomPacketPayload[0]);
            }
        }
        DimensionHelper.teleportBack(player);
        // Clear the guard after a short delay to allow the teleport to complete
        player.level().getServer().execute(() -> TELEPORTING_PLAYERS.remove(player.getUUID()));
    }
}

