/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.level.Level
 *  net.neoforged.neoforge.network.PacketDistributor
 */
package com.ultra.megamod.feature.dimensions;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.dimensions.MegaModDimensions;
import com.ultra.megamod.feature.dimensions.network.DimensionSyncPayload;
import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.network.PacketDistributor;

public class DimensionHelper {
    private static final Map<UUID, ReturnData> returnPositions = new HashMap<UUID, ReturnData>();
    private static final String FILE_NAME = "megamod_return_positions.dat";
    private static boolean dirty = false;
    private static boolean loaded = false;

    private DimensionHelper() {
    }

    public static void teleportToDimension(ServerPlayer player, ResourceKey<Level> dimension, BlockPos target) {
        DimensionHelper.teleportToDimension(player, dimension, target, player.getYRot(), player.getXRot());
    }

    /**
     * Teleport with a custom transition label override (e.g., "arena" for arena instances
     * that use the dungeon pocket dimension but should show "Entering the Arena").
     */
    public static void teleportToDimension(ServerPlayer player, ResourceKey<Level> dimension, BlockPos target, String transitionLabel) {
        ServerLevel targetLevel = player.level().getServer().getLevel(dimension);
        if (targetLevel == null) {
            MegaMod.LOGGER.warn("Cannot teleport to dimension {} — level not found", dimension.identifier());
            return;
        }
        returnPositions.put(player.getUUID(), new ReturnData(player.level().dimension(), player.blockPosition(), player.getYRot(), player.getXRot()));
        dirty = true;
        targetLevel.getChunk(target);
        double x = (double)target.getX() + 0.5;
        double y = target.getY();
        double z = (double)target.getZ() + 0.5;
        player.teleportTo(targetLevel, x, y, z, Set.of(), player.getYRot(), player.getXRot(), false);
        boolean inPocket = dimension.equals(MegaModDimensions.MUSEUM) || dimension.equals(MegaModDimensions.DUNGEON) || dimension.equals(MegaModDimensions.CASINO) || dimension.equals(MegaModDimensions.RESOURCE) || dimension.equals(MegaModDimensions.TRADING);
        // Send the custom label as the dimension ID so the client picks the right transition text
        PacketDistributor.sendToPlayer(player, new DimensionSyncPayload(transitionLabel, inPocket));
        MegaMod.LOGGER.debug("Teleported {} to {} (label: {}) at ({}, {}, {})", player.getGameProfile().name(), dimension.identifier(), transitionLabel, (int)x, (int)y, (int)z);
    }

    public static void teleportToDimension(ServerPlayer player, ResourceKey<Level> dimension, BlockPos target, float yRot, float xRot) {
        ServerLevel targetLevel = player.level().getServer().getLevel(dimension);
        if (targetLevel == null) {
            MegaMod.LOGGER.warn("Cannot teleport to dimension {} \u2014 level not found", (Object)dimension.identifier());
            return;
        }
        returnPositions.put(player.getUUID(), new ReturnData((ResourceKey<Level>)player.level().dimension(), player.blockPosition(), player.getYRot(), player.getXRot()));
        dirty = true;
        targetLevel.getChunk(target);
        double x = (double)target.getX() + 0.5;
        double y = target.getY();
        double z = (double)target.getZ() + 0.5;
        player.teleportTo(targetLevel, x, y, z, Set.of(), yRot, xRot, false);
        String dimId = dimension.identifier().toString();
        boolean inPocket = dimension.equals(MegaModDimensions.MUSEUM) || dimension.equals(MegaModDimensions.DUNGEON) || dimension.equals(MegaModDimensions.CASINO) || dimension.equals(MegaModDimensions.RESOURCE) || dimension.equals(MegaModDimensions.TRADING);
        PacketDistributor.sendToPlayer((ServerPlayer)player, (CustomPacketPayload)new DimensionSyncPayload(dimId, inPocket), (CustomPacketPayload[])new CustomPacketPayload[0]);
        MegaMod.LOGGER.debug("Teleported {} to {} at ({}, {}, {})", new Object[]{player.getGameProfile().name(), dimId, (int)x, (int)y, (int)z});
    }

    public static void teleportBack(ServerPlayer player) {
        ServerLevel returnLevel;
        ReturnData returnData = returnPositions.remove(player.getUUID());
        if (returnData != null) dirty = true;
        if (returnData != null && (returnLevel = player.level().getServer().getLevel(returnData.dimension())) != null) {
            double x = (double)returnData.pos().getX() + 0.5;
            double y = returnData.pos().getY();
            double z = (double)returnData.pos().getZ() + 0.5;
            player.teleportTo(returnLevel, x, y, z, Set.of(), returnData.yRot(), returnData.xRot(), false);
            String dimId = returnData.dimension().identifier().toString();
            boolean inPocket = returnData.dimension().equals(MegaModDimensions.MUSEUM) || returnData.dimension().equals(MegaModDimensions.DUNGEON) || returnData.dimension().equals(MegaModDimensions.CASINO) || returnData.dimension().equals(MegaModDimensions.TRADING);
            PacketDistributor.sendToPlayer((ServerPlayer)player, (CustomPacketPayload)new DimensionSyncPayload(dimId, inPocket), (CustomPacketPayload[])new CustomPacketPayload[0]);
            MegaMod.LOGGER.debug("Returned {} to {} at ({}, {}, {})", new Object[]{player.getGameProfile().name(), dimId, (int)x, (int)y, (int)z});
            return;
        }
        ServerLevel overworld = player.level().getServer().getLevel(Level.OVERWORLD);
        if (overworld != null) {
            BlockPos spawn = new BlockPos(0, 64, 0);
            player.teleportTo(overworld, (double)spawn.getX() + 0.5, (double)spawn.getY(), (double)spawn.getZ() + 0.5, Set.of(), 0.0f, 0.0f, false);
            PacketDistributor.sendToPlayer((ServerPlayer)player, (CustomPacketPayload)new DimensionSyncPayload("minecraft:overworld", false), (CustomPacketPayload[])new CustomPacketPayload[0]);
        }
    }

    public static boolean hasReturnPosition(UUID playerId) {
        return returnPositions.containsKey(playerId);
    }

    public static void clearReturnPosition(UUID playerId) {
        if (returnPositions.remove(playerId) != null) {
            dirty = true;
        }
    }

    public static void reset() {
        returnPositions.clear();
        dirty = false;
        loaded = false;
    }

    public static void loadFromDisk(ServerLevel level) {
        if (loaded) return;
        loaded = true;
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (dataFile.exists()) {
                CompoundTag root = NbtIo.readCompressed((Path) dataFile.toPath(), (NbtAccounter) NbtAccounter.unlimitedHeap());
                CompoundTag positions = root.getCompoundOrEmpty("positions");
                for (String key : positions.keySet()) {
                    UUID uuid = UUID.fromString(key);
                    CompoundTag tag = positions.getCompoundOrEmpty(key);
                    String dimId = tag.getStringOr("dimension", "minecraft:overworld");
                    ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION, Identifier.parse(dimId));
                    BlockPos pos = new BlockPos(tag.getIntOr("x", 0), tag.getIntOr("y", 64), tag.getIntOr("z", 0));
                    float yRot = tag.getFloatOr("yRot", 0f);
                    float xRot = tag.getFloatOr("xRot", 0f);
                    returnPositions.put(uuid, new ReturnData(dim, pos, yRot, xRot));
                }
                MegaMod.LOGGER.info("Loaded {} return positions from disk", returnPositions.size());
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load return positions", (Throwable) e);
        }
    }

    public static void saveToDisk(ServerLevel level) {
        if (!dirty) return;
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataDir = new File(saveDir, "data");
            if (!dataDir.exists()) dataDir.mkdirs();
            File dataFile = new File(dataDir, FILE_NAME);
            CompoundTag root = new CompoundTag();
            CompoundTag positions = new CompoundTag();
            for (Map.Entry<UUID, ReturnData> entry : returnPositions.entrySet()) {
                CompoundTag tag = new CompoundTag();
                ReturnData data = entry.getValue();
                tag.putString("dimension", data.dimension().identifier().toString());
                tag.putInt("x", data.pos().getX());
                tag.putInt("y", data.pos().getY());
                tag.putInt("z", data.pos().getZ());
                tag.putFloat("yRot", data.yRot());
                tag.putFloat("xRot", data.xRot());
                positions.put(entry.getKey().toString(), (Tag) tag);
            }
            root.put("positions", (Tag) positions);
            NbtIo.writeCompressed((CompoundTag) root, (Path) dataFile.toPath());
            dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save return positions", (Throwable) e);
        }
    }

    public record ReturnData(ResourceKey<Level> dimension, BlockPos pos, float yRot, float xRot) {
    }
}

