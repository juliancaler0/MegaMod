package com.ultra.megamod.feature.citizen.blueprint.network;

import com.mojang.logging.LogUtils;
import com.ultra.megamod.feature.citizen.blueprint.Blueprint;
import com.ultra.megamod.feature.citizen.blueprint.BlueprintUtil;
import com.ultra.megamod.feature.citizen.blueprint.BlueprintUtils;
import com.ultra.megamod.feature.citizen.blueprint.packs.StructurePacks;
import com.ultra.megamod.feature.citizen.blueprint.packs.StructurePackMeta;
import com.ultra.megamod.feature.computer.admin.AdminSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.slf4j.Logger;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Client -> Server: save a scanned world region as a .blueprint file.
 * The server scans the blocks between corner1 and corner2, converts them to
 * the blueprint NBT format, and saves the file into the specified pack directory.
 */
public record ScanSavePayload(
        BlockPos corner1,
        BlockPos corner2,
        String name,
        String packId
) implements CustomPacketPayload {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final CustomPacketPayload.Type<ScanSavePayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "scan_save"));

    public static final StreamCodec<FriendlyByteBuf, ScanSavePayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public ScanSavePayload decode(FriendlyByteBuf buf) {
                    return new ScanSavePayload(
                            buf.readBlockPos(),
                            buf.readBlockPos(),
                            buf.readUtf(128),
                            buf.readUtf(128)
                    );
                }

                @Override
                public void encode(FriendlyByteBuf buf, ScanSavePayload payload) {
                    buf.writeBlockPos(payload.corner1());
                    buf.writeBlockPos(payload.corner2());
                    buf.writeUtf(payload.name(), 128);
                    buf.writeUtf(payload.packId(), 128);
                }
            };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Handles the scan-and-save operation on the server.
     * Uses BlueprintUtils.createBlueprint() to scan the world region, then
     * BlueprintUtil.writeBlueprintToNBT() to serialize in the correct format.
     */
    public static void handleOnServer(ScanSavePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;

            // Permission check
            if (!AdminSystem.isAdmin(serverPlayer)) {
                serverPlayer.sendSystemMessage(Component.literal("You don't have permission to scan blueprints."));
                return;
            }

            ServerLevel level = (ServerLevel) serverPlayer.level();

            // Safety check — don't scan enormous regions
            int sizeX = Math.abs(payload.corner2().getX() - payload.corner1().getX()) + 1;
            int sizeY = Math.abs(payload.corner2().getY() - payload.corner1().getY()) + 1;
            int sizeZ = Math.abs(payload.corner2().getZ() - payload.corner1().getZ()) + 1;
            long volume = (long) sizeX * sizeY * sizeZ;
            if (volume > 1_000_000) {
                serverPlayer.sendSystemMessage(Component.literal("Scan region too large (" + volume + " blocks). Max 1,000,000."));
                return;
            }

            // Create blueprint from world region using the proper utility
            String scanName = payload.name().isEmpty() ? "scan" : payload.name();
            Blueprint blueprint = BlueprintUtils.createBlueprint(level, payload.corner1(), payload.corner2(), scanName);
            if (blueprint == null) {
                serverPlayer.sendSystemMessage(Component.literal("Failed to scan region — no blocks found."));
                return;
            }

            // Serialize using the canonical blueprint NBT format
            CompoundTag root = BlueprintUtil.writeBlueprintToNBT(blueprint);

            // Determine save path
            String fileName = scanName.replaceAll("[^a-zA-Z0-9_-]", "_");
            if (fileName.isEmpty()) fileName = "scan";
            if (!fileName.endsWith(".blueprint")) fileName += ".blueprint";

            StructurePackMeta pack = StructurePacks.getPack(payload.packId());
            Path saveDir;
            if (pack != null) {
                saveDir = pack.getPath();
            } else {
                saveDir = Path.of("blueprints", "megamod", payload.packId().isEmpty() ? "default" : payload.packId());
            }

            try {
                Files.createDirectories(saveDir);
                Path outFile = saveDir.resolve(fileName);
                try (OutputStream os = Files.newOutputStream(outFile)) {
                    NbtIo.writeCompressed(root, os);
                }
                serverPlayer.sendSystemMessage(Component.literal("Blueprint saved: " + outFile.getFileName()));
                LOGGER.info("Player {} saved blueprint {} ({} non-air blocks, {}x{}x{})",
                        serverPlayer.getGameProfile().name(), outFile,
                        BlueprintUtils.countNonAirBlocks(blueprint), sizeX, sizeY, sizeZ);
            } catch (Exception e) {
                serverPlayer.sendSystemMessage(Component.literal("Failed to save blueprint: " + e.getMessage()));
                LOGGER.error("Failed to save blueprint: {}", e.getMessage());
            }
        });
    }
}
