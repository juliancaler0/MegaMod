package com.ultra.megamod.feature.citizen.visitor;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.citizen.CitizenManager;
import com.ultra.megamod.feature.citizen.data.CitizenJob;
import com.ultra.megamod.feature.citizen.data.ColonyStatisticsManager;
import com.ultra.megamod.feature.citizen.data.FactionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages tavern visitors per faction (colony).
 * Each faction has its own VisitorManager instance.
 * Visitors arrive at taverns and can be recruited into the colony.
 * They leave after 3 in-game days (72000 ticks) if not recruited.
 */
public class VisitorManager {

    // factionId -> VisitorManager instance
    private static final Map<String, VisitorManager> INSTANCES = new ConcurrentHashMap<>();
    private static final String FILE_PREFIX = "megamod_visitors_";

    // 3 in-game days = 3 * 24000 = 72000 ticks
    private static final long VISITOR_EXPIRY_TICKS = 72000L;

    private final String factionId;
    private final Map<UUID, VisitorData> activeVisitors = new LinkedHashMap<>();
    private boolean dirty = false;

    public VisitorManager(String factionId) {
        this.factionId = factionId;
    }

    // --- Singleton per faction ---

    public static VisitorManager get(ServerLevel level, String factionId) {
        return INSTANCES.computeIfAbsent(factionId, id -> {
            VisitorManager mgr = new VisitorManager(id);
            mgr.loadFromDisk(level);
            return mgr;
        });
    }

    public static void saveAll(ServerLevel level) {
        for (VisitorManager mgr : INSTANCES.values()) {
            mgr.saveToDisk(level);
        }
    }

    public static void resetAll() {
        INSTANCES.clear();
    }

    public static Collection<VisitorManager> allInstances() {
        return INSTANCES.values();
    }

    // --- Core operations ---

    /**
     * Spawns 1-3 visitors at the given tavern position.
     * Higher tavern level increases the max number and tier quality.
     *
     * @param level       the server level
     * @param tavernPos   the tavern hut block position
     * @param tavernLevel the tavern building level (1-5)
     */
    public void spawnVisitors(ServerLevel level, BlockPos tavernPos, int tavernLevel) {
        Random rand = new Random();
        int maxVisitors = Math.min(1 + tavernLevel, 4); // level 1: 2, level 2: 3, level 3+: 4
        int currentCount = (int) activeVisitors.values().stream()
                .filter(v -> !v.isRecruited()).count();

        int toSpawn = Math.min(1 + rand.nextInt(Math.max(1, tavernLevel)), maxVisitors - currentCount);
        if (toSpawn <= 0) return;

        long currentTick = level.getServer().getTickCount();

        for (int i = 0; i < toSpawn; i++) {
            VisitorData visitor = VisitorData.createRandom(rand, tavernLevel);
            visitor.setArrivalTick(currentTick);

            // Offset sitting positions around the tavern
            BlockPos seatPos = tavernPos.offset(
                rand.nextInt(5) - 2,
                0,
                rand.nextInt(5) - 2
            );
            visitor.setSittingPos(seatPos);

            activeVisitors.put(visitor.getVisitorId(), visitor);

            // Spawn the entity
            spawnVisitorEntity(level, visitor);
        }

        markDirty();
        MegaMod.LOGGER.debug("Spawned {} visitors at tavern for faction {}", toSpawn, factionId);
    }

    /**
     * Spawns a VisitorEntity in the world for the given visitor data.
     */
    private void spawnVisitorEntity(ServerLevel level, VisitorData data) {
        VisitorEntity entity = new VisitorEntity(
            VisitorEntityRegistry.VISITOR.get(), level
        );
        BlockPos pos = data.getSittingPos();
        entity.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        entity.setVisitorId(data.getVisitorId());
        entity.setCustomName(Component.literal(data.getName()));
        entity.setCustomNameVisible(true);
        entity.setPersistenceRequired();
        level.addFreshEntity(entity);
    }

    /**
     * Attempts to recruit a visitor. Checks if the player has the required items,
     * deducts them, converts the visitor to a citizen, and removes the visitor entity.
     *
     * @param visitorId the visitor's UUID
     * @param player    the recruiting player
     * @return true if recruitment succeeded
     */
    public boolean recruitVisitor(UUID visitorId, ServerPlayer player) {
        VisitorData visitor = activeVisitors.get(visitorId);
        if (visitor == null || visitor.isRecruited()) return false;

        ItemStack cost = visitor.getRecruitCost();
        if (!cost.isEmpty()) {
            // Check if player has the required items
            int needed = cost.getCount();
            int found = 0;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack slot = player.getInventory().getItem(i);
                if (slot.is(cost.getItem())) {
                    found += slot.getCount();
                }
            }

            if (found < needed) {
                player.sendSystemMessage(Component.literal(
                    "\u00A7cYou need " + visitor.getCostDisplayString() + " to recruit " + visitor.getName() + "!"));
                return false;
            }

            // Deduct items
            int remaining = needed;
            for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
                ItemStack slot = player.getInventory().getItem(i);
                if (slot.is(cost.getItem())) {
                    int take = Math.min(slot.getCount(), remaining);
                    slot.shrink(take);
                    remaining -= take;
                }
            }
        }

        visitor.setRecruited(true);

        // Register as a new citizen in CitizenManager
        ServerLevel level = (ServerLevel) player.level();
        String playerFaction = FactionManager.get(level).getPlayerFaction(player.getUUID());
        if (playerFaction == null) playerFaction = factionId;

        CitizenManager.get(level).registerCitizen(
            UUID.randomUUID(), // new entity ID for the citizen
            player.getUUID(),
            playerFaction,
            CitizenJob.RECRUIT, // visitors become generic recruits
            visitor.getName(),
            level.getServer().getTickCount()
        );

        // Track statistic
        try {
            ColonyStatisticsManager.get(level, playerFaction)
                .increment(ColonyStatisticsManager.VISITORS_RECRUITED);
        } catch (Exception ignored) {}

        // Remove visitor entity from world
        removeVisitorEntity(level, visitorId);
        activeVisitors.remove(visitorId);
        markDirty();

        player.sendSystemMessage(Component.literal(
            "\u00A7a\u00A7l\u2714 \u00A7e" + visitor.getName() + " \u00A7ahas been recruited to your colony!"));

        MegaMod.LOGGER.info("Visitor {} recruited by {} for faction {}",
            visitor.getName(), player.getGameProfile().name(), factionId);
        return true;
    }

    /**
     * Dismisses a visitor — they leave the colony (VISITORS_ABSCONDED stat).
     */
    public void dismissVisitor(UUID visitorId, ServerLevel level) {
        VisitorData visitor = activeVisitors.remove(visitorId);
        if (visitor == null) return;

        removeVisitorEntity(level, visitorId);
        markDirty();

        MegaMod.LOGGER.debug("Visitor {} dismissed from faction {}", visitor.getName(), factionId);
    }

    /**
     * Tick all visitors — remove expired ones (older than 3 in-game days).
     */
    public void tickVisitors(ServerLevel level) {
        long currentTick = level.getServer().getTickCount();
        List<UUID> expired = new ArrayList<>();

        for (VisitorData visitor : activeVisitors.values()) {
            if (visitor.isRecruited()) continue;
            if (currentTick - visitor.getArrivalTick() >= VISITOR_EXPIRY_TICKS) {
                expired.add(visitor.getVisitorId());
            }
        }

        for (UUID id : expired) {
            VisitorData visitor = activeVisitors.remove(id);
            if (visitor != null) {
                removeVisitorEntity(level, id);
                MegaMod.LOGGER.debug("Visitor {} expired (left tavern) in faction {}",
                    visitor.getName(), factionId);
            }
        }

        if (!expired.isEmpty()) {
            markDirty();
        }

        // Spawn new visitors at taverns if there's room
        int activeCount = (int) activeVisitors.values().stream()
                .filter(v -> !v.isRecruited()).count();
        if (activeCount < 4) {
            spawnVisitorsAtTaverns(level);
        }
    }

    /**
     * Finds tavern buildings for this faction and spawns visitors at them.
     */
    private void spawnVisitorsAtTaverns(ServerLevel level) {
        FactionManager fm = FactionManager.get(level);
        var faction = fm.getFaction(factionId);
        if (faction == null) return;

        // Search for tavern TileEntityColonyBuilding within the faction's area
        BlockPos center = faction.getTownChestPos();
        if (center == null || center.equals(BlockPos.ZERO)) return;

        int searchRadius = 128;
        for (int x = -searchRadius; x <= searchRadius; x += 8) {
            for (int z = -searchRadius; z <= searchRadius; z += 8) {
                for (int y = -16; y <= 16; y += 4) {
                    BlockPos pos = center.offset(x, y, z);
                    var be = level.getBlockEntity(pos);
                    if (be instanceof com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding tile
                            && "tavern".equals(tile.getBuildingId())) {
                        int tavernLevel = Math.max(1, tile.getBuildingLevel());
                        spawnVisitors(level, pos, tavernLevel);
                        return; // One tavern per tick cycle
                    }
                }
            }
        }
    }

    /**
     * Removes the VisitorEntity from the world by matching visitorId.
     */
    private void removeVisitorEntity(ServerLevel level, UUID visitorId) {
        for (net.minecraft.world.entity.Entity entity : level.getAllEntities()) {
            if (entity instanceof VisitorEntity ve && visitorId.equals(ve.getVisitorId())) {
                ve.discard();
                break;
            }
        }
    }

    // --- Queries ---

    public Map<UUID, VisitorData> getActiveVisitors() {
        return Collections.unmodifiableMap(activeVisitors);
    }

    public VisitorData getVisitor(UUID visitorId) {
        return activeVisitors.get(visitorId);
    }

    public int getVisitorCount() {
        return (int) activeVisitors.values().stream()
                .filter(v -> !v.isRecruited()).count();
    }

    // --- Persistence ---

    private void markDirty() {
        dirty = true;
    }

    public void saveToDisk(ServerLevel level) {
        if (!dirty) return;
        try {
            Path worldDir = level.getServer().getWorldPath(LevelResource.ROOT).resolve("data");
            java.io.File file = worldDir.resolve(FILE_PREFIX + factionId + ".dat").toFile();
            worldDir.toFile().mkdirs();

            CompoundTag root = new CompoundTag();
            root.putString("factionId", factionId);

            ListTag list = new ListTag();
            for (VisitorData visitor : activeVisitors.values()) {
                list.add(visitor.save());
            }
            root.put("visitors", list);

            NbtIo.writeCompressed(root, file.toPath());
            dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save visitors for faction {}", factionId, e);
        }
    }

    public void loadFromDisk(ServerLevel level) {
        try {
            Path worldDir = level.getServer().getWorldPath(LevelResource.ROOT).resolve("data");
            java.io.File file = worldDir.resolve(FILE_PREFIX + factionId + ".dat").toFile();
            if (!file.exists()) return;

            CompoundTag root = NbtIo.readCompressed(file.toPath(), NbtAccounter.unlimitedHeap());
            ListTag list = root.getListOrEmpty("visitors");
            activeVisitors.clear();
            for (Tag tag : list) {
                if (tag instanceof CompoundTag ct) {
                    VisitorData visitor = VisitorData.load(ct);
                    activeVisitors.put(visitor.getVisitorId(), visitor);
                }
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load visitors for faction {}", factionId, e);
        }
    }
}
