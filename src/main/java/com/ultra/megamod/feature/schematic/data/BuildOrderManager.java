package com.ultra.megamod.feature.schematic.data;

import com.mojang.logging.LogUtils;
import com.ultra.megamod.feature.schematic.placement.SchematicPlacement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Manages active build orders. Singleton per ServerLevel with NbtIo persistence.
 */
public class BuildOrderManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String FILE_NAME = "megamod_build_orders.dat";

    private static BuildOrderManager instance;
    private final Map<UUID, BuildOrder> orders = new LinkedHashMap<>();
    private boolean dirty = false;

    private BuildOrderManager() {}

    public static BuildOrderManager get(ServerLevel level) {
        if (instance == null) {
            instance = new BuildOrderManager();
            instance.loadFromDisk(level);
        }
        return instance;
    }

    public static void reset() {
        instance = null;
    }

    public BuildOrder createOrder(UUID ownerUUID, SchematicData schematic,
                                  SchematicPlacement placement) {
        BuildOrder order = BuildOrder.create(ownerUUID, schematic, placement, System.currentTimeMillis());
        orders.put(order.getOrderId(), order);
        dirty = true;
        return order;
    }

    public BuildOrder getOrder(UUID orderId) {
        return orders.get(orderId);
    }

    public List<BuildOrder> getOrdersForPlayer(UUID playerUUID) {
        List<BuildOrder> result = new ArrayList<>();
        for (BuildOrder order : orders.values()) {
            if (order.getOwnerUUID().equals(playerUUID)) {
                result.add(order);
            }
        }
        return result;
    }

    public List<BuildOrder> getAllOrders() {
        return new ArrayList<>(orders.values());
    }

    public void assignBuilder(UUID orderId, int builderEntityId) {
        BuildOrder order = orders.get(orderId);
        if (order != null) {
            order.setAssignedBuilderEntityId(builderEntityId);
            dirty = true;
        }
    }

    public void updateProgress(UUID orderId, int newIndex) {
        BuildOrder order = orders.get(orderId);
        if (order != null) {
            order.setProgressIndex(newIndex);
            dirty = true;
        }
    }

    public void removeOrder(UUID orderId) {
        if (orders.remove(orderId) != null) {
            dirty = true;
        }
    }

    // ── Persistence ────────────────────────────────────────────────────────

    public void loadFromDisk(ServerLevel level) {
        Path path = level.getServer().getWorldPath(LevelResource.ROOT).resolve("data").resolve(FILE_NAME);
        if (!Files.exists(path)) return;

        try {
            CompoundTag root = NbtIo.readCompressed(path, net.minecraft.nbt.NbtAccounter.unlimitedHeap());
            ListTag orderList = root.getListOrEmpty("Orders");
            orders.clear();

            for (int i = 0; i < orderList.size(); i++) {
                if (orderList.get(i) instanceof CompoundTag tag) {
                    BuildOrder order = loadOrder(tag);
                    if (order != null) {
                        orders.put(order.getOrderId(), order);
                    }
                }
            }
            LOGGER.info("Loaded {} build orders from disk", orders.size());
        } catch (IOException e) {
            LOGGER.error("Failed to load build orders", e);
        }
    }

    public void saveToDisk(ServerLevel level) {
        if (!dirty) return;

        Path dir = level.getServer().getWorldPath(LevelResource.ROOT).resolve("data");
        try {
            Files.createDirectories(dir);
            CompoundTag root = new CompoundTag();
            ListTag orderList = new ListTag();

            for (BuildOrder order : orders.values()) {
                orderList.add(saveOrder(order));
            }
            root.put("Orders", orderList);

            NbtIo.writeCompressed(root, dir.resolve(FILE_NAME));
            dirty = false;
        } catch (IOException e) {
            LOGGER.error("Failed to save build orders", e);
        }
    }

    public void markDirty() {
        dirty = true;
    }

    private static CompoundTag saveOrder(BuildOrder order) {
        CompoundTag tag = new CompoundTag();
        tag.putString("Id", order.getOrderId().toString());
        tag.putString("Owner", order.getOwnerUUID().toString());
        tag.putString("Name", order.getSchematicName());
        tag.putInt("OriginX", order.getOrigin().getX());
        tag.putInt("OriginY", order.getOrigin().getY());
        tag.putInt("OriginZ", order.getOrigin().getZ());
        tag.putInt("Rotation", order.getRotationIndex());
        tag.putInt("Mirror", order.getMirrorIndex());
        tag.putInt("Progress", order.getProgressIndex());
        tag.putInt("BuilderId", order.getAssignedBuilderEntityId());
        tag.putLong("Created", order.getCreatedTime());
        tag.putBoolean("AdminBypass", order.isAdminBypass());
        tag.putInt("SpeedMultiplier", order.getSpeedMultiplier());

        // Save build queue
        ListTag blocks = new ListTag();
        for (BuildOrder.BuildEntry entry : order.getBuildQueue()) {
            CompoundTag blockTag = new CompoundTag();
            blockTag.putInt("X", entry.worldPos().getX());
            blockTag.putInt("Y", entry.worldPos().getY());
            blockTag.putInt("Z", entry.worldPos().getZ());
            blockTag.putString("State", blockStateToString(entry.state()));
            blocks.add(blockTag);
        }
        tag.put("Blocks", blocks);

        return tag;
    }

    private static BuildOrder loadOrder(CompoundTag tag) {
        try {
            UUID orderId = UUID.fromString(tag.getStringOr("Id", ""));
            UUID ownerUUID = UUID.fromString(tag.getStringOr("Owner", ""));
            String name = tag.getStringOr("Name", "Unknown");
            BlockPos origin = new BlockPos(
                    tag.getIntOr("OriginX", 0),
                    tag.getIntOr("OriginY", 0),
                    tag.getIntOr("OriginZ", 0));
            int rotation = tag.getIntOr("Rotation", 0);
            int mirror = tag.getIntOr("Mirror", 0);
            long created = tag.getLongOr("Created", 0L);

            ListTag blocksList = tag.getListOrEmpty("Blocks");
            List<BuildOrder.BuildEntry> queue = new ArrayList<>(blocksList.size());
            for (int i = 0; i < blocksList.size(); i++) {
                if (blocksList.get(i) instanceof CompoundTag blockTag) {
                    BlockPos pos = new BlockPos(
                            blockTag.getIntOr("X", 0),
                            blockTag.getIntOr("Y", 0),
                            blockTag.getIntOr("Z", 0));
                    BlockState state = blockStateFromString(blockTag.getStringOr("State", "minecraft:air"));
                    if (!state.isAir()) {
                        queue.add(new BuildOrder.BuildEntry(pos, state));
                    }
                }
            }

            BuildOrder order = new BuildOrder(orderId, ownerUUID, name, origin, rotation, mirror, queue, created);
            order.setProgressIndex(tag.getIntOr("Progress", 0));
            order.setAssignedBuilderEntityId(tag.getIntOr("BuilderId", -1));
            order.setAdminBypass(tag.getBooleanOr("AdminBypass", false));
            order.setSpeedMultiplier(tag.getIntOr("SpeedMultiplier", 1));
            return order;
        } catch (Exception e) {
            LOGGER.warn("Failed to load build order: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Converts a BlockState to a string like "minecraft:oak_stairs[facing=north,half=bottom]"
     */
    private static String blockStateToString(BlockState state) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (state.getValues().isEmpty()) {
            return id.toString();
        }
        StringBuilder sb = new StringBuilder(id.toString());
        sb.append('[');
        boolean first = true;
        for (var entry : state.getValues().entrySet()) {
            if (!first) sb.append(',');
            sb.append(entry.getKey().getName()).append('=').append(entry.getValue().toString());
            first = false;
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Parses a block state string like "minecraft:oak_stairs[facing=north,half=bottom]"
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static BlockState blockStateFromString(String str) {
        int bracketStart = str.indexOf('[');
        String blockName = bracketStart > 0 ? str.substring(0, bracketStart) : str;
        Identifier blockId = Identifier.tryParse(blockName);
        if (blockId == null) return Blocks.AIR.defaultBlockState();

        Optional<Block> blockOpt = BuiltInRegistries.BLOCK.getOptional(blockId);
        if (blockOpt.isEmpty()) return Blocks.AIR.defaultBlockState();

        BlockState state = blockOpt.get().defaultBlockState();

        if (bracketStart > 0 && str.endsWith("]")) {
            String propsStr = str.substring(bracketStart + 1, str.length() - 1);
            for (String propStr : propsStr.split(",")) {
                String[] kv = propStr.split("=", 2);
                if (kv.length == 2) {
                    var prop = state.getBlock().getStateDefinition().getProperty(kv[0]);
                    if (prop != null) {
                        Optional<?> val = prop.getValue(kv[1]);
                        if (val.isPresent()) {
                            state = state.setValue((net.minecraft.world.level.block.state.properties.Property) prop,
                                    (Comparable) val.get());
                        }
                    }
                }
            }
        }
        return state;
    }
}
