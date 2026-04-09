package com.ultra.megamod.feature.dimensions.resource;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.dimensions.DimensionHelper;
import com.ultra.megamod.feature.dimensions.MegaModDimensions;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.util.List;

public class ResourceDimensionManager {
    private static ResourceDimensionManager INSTANCE;
    private static final String FILE_NAME = "megamod_resource_dim.dat";
    private static final long RESET_INTERVAL_MS = 24L * 60L * 60L * 1000L; // 24 hours real time
    private static final int ERA_OFFSET = 10000; // blocks between eras

    private long lastResetTime;
    private int currentEra;
    private boolean dirty = false;

    public static ResourceDimensionManager get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new ResourceDimensionManager();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    public static void reset() {
        INSTANCE = null;
    }

    public BlockPos getSpawnPos() {
        return new BlockPos(currentEra * ERA_OFFSET, 0, 0);
    }

    public int getCurrentEra() {
        return currentEra;
    }

    public long getTimeUntilReset() {
        long elapsed = System.currentTimeMillis() - lastResetTime;
        return Math.max(0, RESET_INTERVAL_MS - elapsed);
    }

    public void checkAndReset(ServerLevel overworld) {
        long now = System.currentTimeMillis();
        if (now - lastResetTime < RESET_INTERVAL_MS) {
            return;
        }

        ServerLevel resourceLevel = overworld.getServer().getLevel(MegaModDimensions.RESOURCE);
        if (resourceLevel == null) {
            return;
        }

        // Teleport all players in resource dimension back to overworld
        List<ServerPlayer> playersInResource = resourceLevel.players().stream()
                .filter(p -> p instanceof ServerPlayer)
                .map(p -> (ServerPlayer) p)
                .toList();

        for (ServerPlayer player : playersInResource) {
            player.sendSystemMessage(Component.literal("The Resource Dimension is resetting! Teleporting you back...").withStyle(ChatFormatting.GOLD));
            DimensionHelper.teleportBack(player);
        }

        // Increment era so next entry uses fresh terrain
        currentEra++;
        lastResetTime = now;
        dirty = true;
        saveToDisk(overworld);

        // Broadcast to all online players
        for (ServerPlayer player : overworld.getServer().getPlayerList().getPlayers()) {
            player.sendSystemMessage(Component.literal("The Resource Dimension has reset! Fresh resources await.").withStyle(ChatFormatting.GREEN));
        }

        MegaMod.LOGGER.info("Resource Dimension reset — now era {}", currentEra);
    }

    private void markDirty() {
        this.dirty = true;
    }

    public void loadFromDisk(ServerLevel level) {
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (dataFile.exists()) {
                CompoundTag root = NbtIo.readCompressed(dataFile.toPath(), NbtAccounter.unlimitedHeap());
                this.currentEra = root.getIntOr("currentEra", 0);
                this.lastResetTime = root.getLongOr("lastResetTime", System.currentTimeMillis());
            } else {
                // First time — initialize
                this.currentEra = 0;
                this.lastResetTime = System.currentTimeMillis();
                this.dirty = true;
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load resource dimension data", e);
            this.currentEra = 0;
            this.lastResetTime = System.currentTimeMillis();
        }
    }

    public void saveToDisk(ServerLevel level) {
        if (!this.dirty) {
            return;
        }
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataDir = new File(saveDir, "data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            File dataFile = new File(dataDir, FILE_NAME);
            CompoundTag root = new CompoundTag();
            root.putInt("currentEra", this.currentEra);
            root.putLong("lastResetTime", this.lastResetTime);
            NbtIo.writeCompressed(root, dataFile.toPath());
            this.dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save resource dimension data", e);
        }
    }
}
