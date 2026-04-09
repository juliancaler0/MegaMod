package com.ultra.megamod.feature.citizen.data;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Tracks whether a supply camp or supply ship has been placed in the world.
 * Enforces the one-per-world limit for supply items.
 * <p>
 * Persists to {@code world/data/megamod_supply_placed.dat} using manual NbtIo.
 */
public class SupplyPlacedTracker {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String FILE_NAME = "megamod_supply_placed.dat";

    private static SupplyPlacedTracker INSTANCE;

    private boolean campPlaced = false;
    private boolean shipPlaced = false;
    @Nullable
    private String campPlacerUUID = null;
    @Nullable
    private String shipPlacerUUID = null;
    private boolean dirty = false;

    public static SupplyPlacedTracker get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new SupplyPlacedTracker();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    /**
     * Resets the singleton instance. Call when the server stops.
     */
    public static void reset() {
        INSTANCE = null;
    }

    public boolean isCampPlaced() {
        return campPlaced;
    }

    public boolean isShipPlaced() {
        return shipPlaced;
    }

    @Nullable
    public String getCampPlacerUUID() {
        return campPlacerUUID;
    }

    @Nullable
    public String getShipPlacerUUID() {
        return shipPlacerUUID;
    }

    public void markCampPlaced(UUID placerUUID) {
        this.campPlaced = true;
        this.campPlacerUUID = placerUUID.toString();
        this.dirty = true;
    }

    public void markShipPlaced(UUID placerUUID) {
        this.shipPlaced = true;
        this.shipPlacerUUID = placerUUID.toString();
        this.dirty = true;
    }

    public void saveToDisk(ServerLevel level) {
        if (!dirty) return;
        try {
            Path dir = level.getServer().getWorldPath(LevelResource.ROOT).resolve("data");
            Files.createDirectories(dir);
            Path file = dir.resolve(FILE_NAME);

            CompoundTag root = new CompoundTag();
            root.putBoolean("campPlaced", campPlaced);
            root.putBoolean("shipPlaced", shipPlaced);
            if (campPlacerUUID != null) {
                root.putString("campPlacer", campPlacerUUID);
            }
            if (shipPlacerUUID != null) {
                root.putString("shipPlacer", shipPlacerUUID);
            }

            NbtIo.writeCompressed(root, file);
            dirty = false;
            LOGGER.debug("Saved supply placement tracker");
        } catch (Exception e) {
            LOGGER.error("Failed to save supply placement tracker", e);
        }
    }

    private void loadFromDisk(ServerLevel level) {
        try {
            Path dir = level.getServer().getWorldPath(LevelResource.ROOT).resolve("data");
            Path file = dir.resolve(FILE_NAME);

            if (!Files.exists(file)) return;

            CompoundTag root = NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
            campPlaced = root.getBooleanOr("campPlaced", false);
            shipPlaced = root.getBooleanOr("shipPlaced", false);
            campPlacerUUID = root.getStringOr("campPlacer", "");
            if (campPlacerUUID.isEmpty()) campPlacerUUID = null;
            shipPlacerUUID = root.getStringOr("shipPlacer", "");
            if (shipPlacerUUID.isEmpty()) shipPlacerUUID = null;
            LOGGER.debug("Loaded supply placement tracker: camp={}, ship={}", campPlaced, shipPlaced);
        } catch (Exception e) {
            LOGGER.error("Failed to load supply placement tracker", e);
        }
    }
}
