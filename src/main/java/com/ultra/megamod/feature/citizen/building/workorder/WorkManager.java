package com.ultra.megamod.feature.citizen.building.workorder;

import com.ultra.megamod.MegaMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages colony work orders (build, upgrade, repair, decoration, removal).
 * <p>
 * Singleton pattern matching existing managers (FactionManager, etc.).
 * Persists to {@code world/data/megamod_work_orders.dat} via NbtIo.
 */
public class WorkManager {

    private static WorkManager INSTANCE;
    private static final String FILE_NAME = "megamod_work_orders.dat";

    private final Map<UUID, ColonyWorkOrder> orders = new LinkedHashMap<>();
    private boolean dirty = false;

    // ========== Singleton ==========

    public static WorkManager get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new WorkManager();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    public static void reset() {
        INSTANCE = null;
    }

    // ========== Order Management ==========

    /**
     * Adds a work order. If an order with the same ID already exists, it is replaced.
     */
    public void addOrder(ColonyWorkOrder order) {
        orders.put(order.getOrderId(), order);
        markDirty();
    }

    /**
     * Removes a work order by its ID.
     *
     * @return true if the order existed and was removed
     */
    public boolean removeOrder(UUID orderId) {
        if (orders.remove(orderId) != null) {
            markDirty();
            return true;
        }
        return false;
    }

    /**
     * Returns the work order with the given ID, or null if not found.
     */
    public ColonyWorkOrder getOrder(UUID orderId) {
        return orders.get(orderId);
    }

    /**
     * Returns an unmodifiable view of all work orders.
     */
    public Collection<ColonyWorkOrder> getOrders() {
        return Collections.unmodifiableCollection(orders.values());
    }

    /**
     * Returns all work orders belonging to a specific colony.
     */
    public List<ColonyWorkOrder> getOrdersForColony(UUID colonyId) {
        return orders.values().stream()
                .filter(o -> o.getColonyId().equals(colonyId))
                .collect(Collectors.toList());
    }

    /**
     * Returns all unclaimed work orders (not yet assigned to a builder).
     * Sorted by priority descending (highest priority first).
     */
    public List<ColonyWorkOrder> getUnclaimedOrders() {
        return orders.values().stream()
                .filter(o -> !o.isClaimed())
                .sorted(Comparator.comparingInt(ColonyWorkOrder::getPriority).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Returns all unclaimed work orders for a specific colony.
     * Sorted by priority descending (highest priority first).
     */
    public List<ColonyWorkOrder> getUnclaimedOrdersForColony(UUID colonyId) {
        return orders.values().stream()
                .filter(o -> !o.isClaimed() && o.getColonyId().equals(colonyId))
                .sorted(Comparator.comparingInt(ColonyWorkOrder::getPriority).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Claims a work order, assigning it to the given builder.
     *
     * @param orderId   the work order ID
     * @param builderId the builder citizen's UUID
     * @return true if the order was found and successfully claimed
     */
    public boolean claimOrder(UUID orderId, UUID builderId) {
        ColonyWorkOrder order = orders.get(orderId);
        if (order == null || order.isClaimed()) {
            return false;
        }
        order.claim(builderId);
        markDirty();
        return true;
    }

    /**
     * Unclaims a work order, freeing it for another builder.
     *
     * @param orderId the work order ID
     * @return true if the order was found and unclaimed
     */
    public boolean unclaimOrder(UUID orderId) {
        ColonyWorkOrder order = orders.get(orderId);
        if (order == null || !order.isClaimed()) {
            return false;
        }
        order.unclaim();
        markDirty();
        return true;
    }

    /**
     * Completes a work order, removing it from the active list.
     * The caller is responsible for updating the building level after this call.
     *
     * @param orderId the work order ID
     * @return the completed order, or null if not found
     */
    public ColonyWorkOrder completeOrder(UUID orderId) {
        ColonyWorkOrder order = orders.remove(orderId);
        if (order != null) {
            markDirty();
        }
        return order;
    }

    /**
     * Returns the total number of active work orders.
     */
    public int getOrderCount() {
        return orders.size();
    }

    // ========== Persistence ==========

    private void markDirty() {
        dirty = true;
    }

    private void loadFromDisk(ServerLevel level) {
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (dataFile.exists()) {
                CompoundTag root = NbtIo.readCompressed(
                        (Path) dataFile.toPath(),
                        (NbtAccounter) NbtAccounter.unlimitedHeap()
                );
                CompoundTag ordersTag = root.getCompoundOrEmpty("orders");
                for (String key : ordersTag.keySet()) {
                    try {
                        ColonyWorkOrder order = ColonyWorkOrder.load(ordersTag.getCompoundOrEmpty(key));
                        orders.put(order.getOrderId(), order);
                    } catch (Exception e) {
                        MegaMod.LOGGER.warn("Failed to load work order: {}", key, e);
                    }
                }
                MegaMod.LOGGER.info("Loaded {} colony work orders", orders.size());
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load work order data", e);
        }
    }

    public void saveToDisk(ServerLevel level) {
        if (!dirty) return;
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataDir = new File(saveDir, "data");
            if (!dataDir.exists()) dataDir.mkdirs();
            File dataFile = new File(dataDir, FILE_NAME);
            CompoundTag root = new CompoundTag();
            CompoundTag ordersTag = new CompoundTag();
            for (Map.Entry<UUID, ColonyWorkOrder> entry : orders.entrySet()) {
                ordersTag.put(entry.getKey().toString(), (Tag) entry.getValue().save());
            }
            root.put("orders", (Tag) ordersTag);
            NbtIo.writeCompressed((CompoundTag) root, (Path) dataFile.toPath());
            dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save work order data", e);
        }
    }
}
