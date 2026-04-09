package com.ultra.megamod.feature.citizen.blueprint.network;

import com.ultra.megamod.feature.citizen.blueprint.BlockInfo;
import com.ultra.megamod.feature.citizen.blueprint.RotationMirror;
import com.ultra.megamod.feature.citizen.blueprint.packs.StructurePacks;
import com.ultra.megamod.feature.computer.admin.AdminSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

/**
 * Client -> Server: request placement of a blueprint from a structure pack.
 * The server loads the blueprint file, applies rotation/mirror, and places the blocks.
 */
public record BuildToolPlacePayload(
        String packId,
        String blueprintPath,
        BlockPos position,
        int rotationMirrorOrdinal
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<BuildToolPlacePayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "build_tool_place"));

    public static final StreamCodec<FriendlyByteBuf, BuildToolPlacePayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public BuildToolPlacePayload decode(FriendlyByteBuf buf) {
                    return new BuildToolPlacePayload(
                            buf.readUtf(128),
                            buf.readUtf(256),
                            buf.readBlockPos(),
                            buf.readByte()
                    );
                }

                @Override
                public void encode(FriendlyByteBuf buf, BuildToolPlacePayload payload) {
                    buf.writeUtf(payload.packId(), 128);
                    buf.writeUtf(payload.blueprintPath(), 256);
                    buf.writeBlockPos(payload.position());
                    buf.writeByte(payload.rotationMirrorOrdinal());
                }
            };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Handles blueprint placement on the server.
     * Loads the blueprint from disk, applies rotation/mirror, and places blocks in the world.
     */
    public static void handleOnServer(BuildToolPlacePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;

            // Permission check — only ops/admins can place blueprints
            if (!AdminSystem.isAdmin(serverPlayer)) {
                serverPlayer.sendSystemMessage(
                        net.minecraft.network.chat.Component.literal("You don't have permission to place blueprints."));
                return;
            }

            // Ensure packs are discovered on the server side
            if (StructurePacks.loadedPacks.isEmpty()) {
                java.nio.file.Path packsDir = java.nio.file.Path.of("blueprints", "megamod");
                StructurePacks.discoverPacks(packsDir);
            }

            // Load the blueprint from the pack
            List<BlockInfo> blocks = StructurePacks.loadBlueprint(payload.packId(), payload.blueprintPath());
            if (blocks == null || blocks.isEmpty()) {
                serverPlayer.sendSystemMessage(
                        net.minecraft.network.chat.Component.literal("Failed to load blueprint: " + payload.blueprintPath()));
                return;
            }

            // Determine rotation/mirror
            RotationMirror rm = RotationMirror.NONE;
            RotationMirror[] values = RotationMirror.values();
            if (payload.rotationMirrorOrdinal() >= 0 && payload.rotationMirrorOrdinal() < values.length) {
                rm = values[payload.rotationMirrorOrdinal()];
            }

            ServerLevel level = (ServerLevel) serverPlayer.level();

            // Place each block
            int placed = 0;
            for (BlockInfo info : blocks) {
                if (info.state() == null || info.state().isAir()) continue;

                // Apply rotation/mirror to position
                BlockPos localTransformed = rm.applyToPos(info.pos());
                BlockPos worldPos = payload.position().offset(localTransformed);

                // Rotate/mirror the block state
                BlockState state = info.state();
                if (rm.getMirror() != net.minecraft.world.level.block.Mirror.NONE) {
                    state = state.mirror(rm.getMirror());
                }
                if (rm.getRotation() != net.minecraft.world.level.block.Rotation.NONE) {
                    state = state.rotate(rm.getRotation());
                }

                level.setBlock(worldPos, state, 3);

                // Restore block entity data if present
                if (info.hasTileEntityData()) {
                    CompoundTag beTag = info.tileEntityData().copy();
                    // Update position coordinates in the tag to match world position
                    beTag.putInt("x", worldPos.getX());
                    beTag.putInt("y", worldPos.getY());
                    beTag.putInt("z", worldPos.getZ());
                    BlockEntity loaded = BlockEntity.loadStatic(worldPos, state, beTag, level.registryAccess());
                    if (loaded != null) {
                        level.setBlockEntity(loaded);
                    }
                }

                placed++;
            }

            serverPlayer.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal("Placed " + placed + " blocks from blueprint."));
        });
    }
}
