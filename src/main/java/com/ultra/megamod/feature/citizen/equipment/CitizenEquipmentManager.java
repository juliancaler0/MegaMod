package com.ultra.megamod.feature.citizen.equipment;

import com.ultra.megamod.MegaMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

/**
 * Manages equipment loadouts for citizens (recruits and workers).
 * Recruits can equip weapons + armor. Workers can equip appropriate tools.
 * Equipment is stored per-citizen by UUID and persisted with NbtIo.
 */
public class CitizenEquipmentManager {
    private static CitizenEquipmentManager INSTANCE;
    private static final String FILE_NAME = "megamod_citizen_equipment.dat";
    private final Map<UUID, CitizenLoadout> loadouts = new HashMap<>();
    private boolean dirty = false;

    public static CitizenEquipmentManager get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new CitizenEquipmentManager();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    public static void reset() {
        INSTANCE = null;
    }

    public CitizenLoadout getLoadout(UUID citizenId) {
        return loadouts.computeIfAbsent(citizenId, k -> new CitizenLoadout());
    }

    public void setSlot(UUID citizenId, EquipmentSlot slot, String itemId) {
        getLoadout(citizenId).setSlot(slot, itemId);
        dirty = true;
    }

    public void clearSlot(UUID citizenId, EquipmentSlot slot) {
        getLoadout(citizenId).clearSlot(slot);
        dirty = true;
    }

    /** Check if an item is a valid tool for a worker job. */
    public static boolean isValidWorkerTool(String jobName, Item item) {
        ItemStack test = new ItemStack(item);
        return switch (jobName.toUpperCase()) {
            case "FARMER" -> test.is(ItemTags.HOES);
            case "MINER" -> test.is(ItemTags.PICKAXES) || test.is(ItemTags.SHOVELS);
            case "LUMBERJACK" -> test.is(ItemTags.AXES);
            case "FISHERMAN" -> item == Items.FISHING_ROD;
            case "SHEPHERD" -> item == Items.SHEARS;
            default -> false;
        };
    }

    /** Check if an item is valid recruit equipment. */
    public static boolean isValidRecruitGear(Item item) {
        ItemStack test = new ItemStack(item);
        return test.is(ItemTags.SWORDS) || test.is(ItemTags.AXES)
            || item == Items.BOW || item == Items.CROSSBOW
            || test.is(ItemTags.HEAD_ARMOR) || test.is(ItemTags.CHEST_ARMOR)
            || test.is(ItemTags.LEG_ARMOR) || test.is(ItemTags.FOOT_ARMOR)
            || item == Items.SHIELD;
    }

    public static class CitizenLoadout {
        private final Map<EquipmentSlot, String> slots = new EnumMap<>(EquipmentSlot.class);

        public void setSlot(EquipmentSlot slot, String itemId) {
            slots.put(slot, itemId);
        }

        public void clearSlot(EquipmentSlot slot) {
            slots.remove(slot);
        }

        public String getSlot(EquipmentSlot slot) {
            return slots.getOrDefault(slot, "");
        }

        public boolean hasSlot(EquipmentSlot slot) {
            return slots.containsKey(slot) && !slots.get(slot).isEmpty();
        }

        public CompoundTag toNbt() {
            CompoundTag tag = new CompoundTag();
            for (var entry : slots.entrySet()) {
                tag.putString(entry.getKey().getName(), entry.getValue());
            }
            return tag;
        }

        public static CitizenLoadout fromNbt(CompoundTag tag) {
            CitizenLoadout loadout = new CitizenLoadout();
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                String val = tag.getStringOr(slot.getName(), "");
                if (!val.isEmpty()) loadout.slots.put(slot, val);
            }
            return loadout;
        }
    }

    // Persistence
    public void saveToDisk(ServerLevel level) {
        if (!dirty) return;
        dirty = false;
        try {
            Path dir = level.getServer().getWorldPath(LevelResource.ROOT).resolve("data");
            dir.toFile().mkdirs();
            CompoundTag root = new CompoundTag();
            for (var entry : loadouts.entrySet()) {
                root.put(entry.getKey().toString(), entry.getValue().toNbt());
            }
            NbtIo.writeCompressed(root, dir.resolve(FILE_NAME));
        } catch (Exception e) {
            MegaMod.LOGGER.warn("Failed to save citizen equipment: {}", e.getMessage());
        }
    }

    public void loadFromDisk(ServerLevel level) {
        try {
            Path file = level.getServer().getWorldPath(LevelResource.ROOT).resolve("data").resolve(FILE_NAME);
            if (!file.toFile().exists()) return;
            CompoundTag root = NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
            for (String key : root.keySet()) {
                try {
                    UUID id = UUID.fromString(key);
                    loadouts.put(id, CitizenLoadout.fromNbt(root.getCompoundOrEmpty(key)));
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            MegaMod.LOGGER.warn("Failed to load citizen equipment: {}", e.getMessage());
        }
    }
}
