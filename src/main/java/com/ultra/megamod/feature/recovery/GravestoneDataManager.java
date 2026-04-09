package com.ultra.megamod.feature.recovery;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.util.*;

/**
 * NbtIo persistence for gravestone data.
 * Stores item contents keyed by block position so they survive server restarts.
 */
public class GravestoneDataManager {
    private static final String FILE_NAME = "megamod_gravestones.dat";
    private static final Map<String, GravestoneData> gravestones = new HashMap<>();
    private static boolean loaded = false;
    private static boolean dirty = false;

    public record GravestoneData(UUID ownerUuid, String ownerName, long spawnGameTime, List<ItemStack> items) {}

    private static String posKey(BlockPos pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    public static void saveGravestone(ServerLevel level, BlockPos pos, UUID ownerUuid,
                                       String ownerName, long spawnGameTime, List<ItemStack> items) {
        ensureLoaded(level);
        List<ItemStack> copies = new ArrayList<>();
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) copies.add(stack.copy());
        }
        gravestones.put(posKey(pos), new GravestoneData(ownerUuid, ownerName, spawnGameTime, copies));
        dirty = true;
        saveToDisk(level);
    }

    public static GravestoneData getGravestone(ServerLevel level, BlockPos pos) {
        ensureLoaded(level);
        return gravestones.get(posKey(pos));
    }

    public static void removeGravestone(BlockPos pos) {
        gravestones.remove(posKey(pos));
        dirty = true;
    }

    private static void ensureLoaded(ServerLevel level) {
        if (loaded) return;
        loaded = true;
        try {
            File dataDir = new File(level.getServer().getWorldPath(LevelResource.ROOT).toFile(), "data");
            File file = new File(dataDir, FILE_NAME);
            if (!file.exists()) return;

            CompoundTag root = NbtIo.readCompressed(file.toPath(), NbtAccounter.unlimitedHeap());
            for (String key : root.keySet()) {
                CompoundTag entry = root.getCompoundOrEmpty(key);
                String uuidStr = entry.getStringOr("owner", "");
                UUID ownerUuid = null;
                try { ownerUuid = UUID.fromString(uuidStr); } catch (Exception ignored) {}
                String ownerName = entry.getStringOr("name", "");
                long spawnTime = entry.getLongOr("spawnTime", 0L);

                List<ItemStack> items = new ArrayList<>();
                ListTag itemList = entry.getListOrEmpty("items");
                for (int i = 0; i < itemList.size(); i++) {
                    CompoundTag itemTag = itemList.getCompound(i).orElse(new CompoundTag());
                    String itemId = itemTag.getStringOr("id", "");
                    int count = itemTag.getIntOr("count", 1);
                    if (!itemId.isEmpty()) {
                        var id = Identifier.parse(itemId);
                        var item = BuiltInRegistries.ITEM.getValue(id);
                        if (item != null && item != Items.AIR) {
                            items.add(new ItemStack(item, count));
                        }
                    }
                }

                if (ownerUuid != null) {
                    gravestones.put(key, new GravestoneData(ownerUuid, ownerName, spawnTime, items));
                }
            }
        } catch (Exception e) {
            com.ultra.megamod.MegaMod.LOGGER.warn("Failed to load gravestone data: {}", e.getMessage());
        }
    }

    private static void saveToDisk(ServerLevel level) {
        if (!dirty) return;
        try {
            File dataDir = new File(level.getServer().getWorldPath(LevelResource.ROOT).toFile(), "data");
            if (!dataDir.exists()) dataDir.mkdirs();
            File file = new File(dataDir, FILE_NAME);

            CompoundTag root = new CompoundTag();
            for (Map.Entry<String, GravestoneData> e : gravestones.entrySet()) {
                GravestoneData data = e.getValue();
                CompoundTag entry = new CompoundTag();
                entry.putString("owner", data.ownerUuid().toString());
                entry.putString("name", data.ownerName());
                entry.putLong("spawnTime", data.spawnGameTime());

                ListTag itemList = new ListTag();
                for (ItemStack stack : data.items()) {
                    if (!stack.isEmpty()) {
                        CompoundTag itemTag = new CompoundTag();
                        itemTag.putString("id", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
                        itemTag.putInt("count", stack.getCount());
                        itemList.add(itemTag);
                    }
                }
                entry.put("items", itemList);
                root.put(e.getKey(), entry);
            }

            NbtIo.writeCompressed(root, file.toPath());
            dirty = false;
        } catch (Exception e) {
            com.ultra.megamod.MegaMod.LOGGER.warn("Failed to save gravestone data: {}", e.getMessage());
        }
    }

    public static void reset() {
        loaded = false;
        gravestones.clear();
        dirty = false;
    }
}
