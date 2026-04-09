package com.ultra.megamod.feature.baritone.process;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Named waypoints with NbtIo persistence.
 * Waypoints are per-server, keyed by player UUID.
 */
public class WaypointManager {
    private static final Map<UUID, Map<String, BlockPos>> waypoints = new HashMap<>();
    private static Path savePath;

    public static void init(Path worldDir) {
        savePath = worldDir.resolve("megamod_waypoints.dat");
        load();
    }

    public static void save(String name, UUID playerUUID, BlockPos pos) {
        waypoints.computeIfAbsent(playerUUID, k -> new LinkedHashMap<>()).put(name.toLowerCase(), pos);
        saveToDisk();
    }

    public static boolean delete(String name, UUID playerUUID) {
        Map<String, BlockPos> playerWps = waypoints.get(playerUUID);
        if (playerWps != null && playerWps.remove(name.toLowerCase()) != null) {
            saveToDisk();
            return true;
        }
        return false;
    }

    public static BlockPos get(String name, UUID playerUUID) {
        Map<String, BlockPos> playerWps = waypoints.get(playerUUID);
        return playerWps != null ? playerWps.get(name.toLowerCase()) : null;
    }

    public static Map<String, BlockPos> list(UUID playerUUID) {
        return waypoints.getOrDefault(playerUUID, Collections.emptyMap());
    }

    public static List<String> listNames(UUID playerUUID) {
        Map<String, BlockPos> playerWps = waypoints.get(playerUUID);
        return playerWps != null ? new ArrayList<>(playerWps.keySet()) : Collections.emptyList();
    }

    private static void load() {
        if (savePath == null || !Files.exists(savePath)) return;
        try {
            CompoundTag root = NbtIo.readCompressed(savePath, net.minecraft.nbt.NbtAccounter.unlimitedHeap());
            for (String uuidStr : root.keySet()) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    CompoundTag playerTag = root.getCompoundOrEmpty(uuidStr);
                    Map<String, BlockPos> playerWps = new LinkedHashMap<>();
                    ListTag list = playerTag.getListOrEmpty("waypoints");
                    for (int i = 0; i < list.size(); i++) {
                        CompoundTag wp = list.getCompound(i).orElse(new CompoundTag());
                        String name = wp.getStringOr("name", "");
                        int x = wp.getIntOr("x", 0);
                        int y = wp.getIntOr("y", 0);
                        int z = wp.getIntOr("z", 0);
                        if (!name.isEmpty()) {
                            playerWps.put(name, new BlockPos(x, y, z));
                        }
                    }
                    if (!playerWps.isEmpty()) {
                        waypoints.put(uuid, playerWps);
                    }
                } catch (IllegalArgumentException ignored) {}
            }
        } catch (IOException e) {
            // Failed to load — start fresh
        }
    }

    private static void saveToDisk() {
        if (savePath == null) return;
        try {
            CompoundTag root = new CompoundTag();
            for (Map.Entry<UUID, Map<String, BlockPos>> entry : waypoints.entrySet()) {
                CompoundTag playerTag = new CompoundTag();
                ListTag list = new ListTag();
                for (Map.Entry<String, BlockPos> wp : entry.getValue().entrySet()) {
                    CompoundTag wpTag = new CompoundTag();
                    wpTag.putString("name", wp.getKey());
                    wpTag.putInt("x", wp.getValue().getX());
                    wpTag.putInt("y", wp.getValue().getY());
                    wpTag.putInt("z", wp.getValue().getZ());
                    list.add(wpTag);
                }
                playerTag.put("waypoints", list);
                root.put(entry.getKey().toString(), playerTag);
            }
            Files.createDirectories(savePath.getParent());
            NbtIo.writeCompressed(root, savePath);
        } catch (IOException e) {
            // Save failed
        }
    }

    public static void shutdown() {
        saveToDisk();
    }
}
