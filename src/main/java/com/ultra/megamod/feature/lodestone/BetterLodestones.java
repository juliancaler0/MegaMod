/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.NbtAccounter
 *  net.minecraft.nbt.NbtIo
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.storage.LevelResource
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.EntityTeleportEvent$EnderPearl
 *  net.neoforged.neoforge.event.server.ServerStartedEvent
 *  net.neoforged.neoforge.event.server.ServerStoppingEvent
 *  net.neoforged.neoforge.event.tick.LevelTickEvent$Post
 *  net.neoforged.neoforge.event.tick.PlayerTickEvent$Post
 */
package com.ultra.megamod.feature.lodestone;

import com.ultra.megamod.MegaMod;
import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid="megamod")
public class BetterLodestones {
    private static final double NAMING_RANGE = 1.5;
    private static final double DISPLAY_RANGE = 8.0;
    private static final double WAYPOINT_ACTIVATION_RANGE = 96.0;
    private static final double WAYPOINT_LANDING_RANGE = 3.0;
    private static final String DATA_FILE_NAME = "megamod_lodestones.dat";
    private static final Map<String, Map<BlockPos, String>> NAMES = new HashMap<String, Map<BlockPos, String>>();
    private static final Map<String, Map<BlockPos, Boolean>> WAYPOINTS = new HashMap<String, Map<BlockPos, Boolean>>();

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        NAMES.clear();
        WAYPOINTS.clear();
        ServerLevel overworld = event.getServer().overworld();
        File dataDir = overworld.getServer().getWorldPath(LevelResource.ROOT).resolve("data").toFile();
        File dataFile = new File(dataDir, DATA_FILE_NAME);
        if (dataFile.exists()) {
            try {
                CompoundTag root = NbtIo.readCompressed((Path)dataFile.toPath(), (NbtAccounter)NbtAccounter.unlimitedHeap());
                BetterLodestones.loadFromTag(root);
            }
            catch (Exception e) {
                MegaMod.LOGGER.error("Failed to load lodestone data", (Throwable)e);
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ServerLevel overworld = event.getServer().overworld();
        File dataDir = overworld.getServer().getWorldPath(LevelResource.ROOT).resolve("data").toFile();
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        File dataFile = new File(dataDir, DATA_FILE_NAME);
        try {
            CompoundTag root = BetterLodestones.saveToTag();
            NbtIo.writeCompressed((CompoundTag)root, (Path)dataFile.toPath());
        }
        catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save lodestone data", (Throwable)e);
        }
    }

    private static void loadFromTag(CompoundTag root) {
        if (root.contains("Dimensions")) {
            CompoundTag dims = root.getCompoundOrEmpty("Dimensions");
            for (String dimKey : dims.keySet()) {
                CompoundTag dimTag = dims.getCompoundOrEmpty(dimKey);
                ListTag namesList = dimTag.getListOrEmpty("Names");
                HashMap<BlockPos, String> dimNames = new HashMap<BlockPos, String>();
                for (int i = 0; i < namesList.size(); ++i) {
                    CompoundTag entry = namesList.getCompoundOrEmpty(i);
                    BlockPos pos = new BlockPos(entry.getIntOr("x", 0), entry.getIntOr("y", 0), entry.getIntOr("z", 0));
                    String name = entry.getStringOr("name", "");
                    if (name.isEmpty()) continue;
                    dimNames.put(pos, name);
                }
                if (!dimNames.isEmpty()) {
                    NAMES.put(dimKey, dimNames);
                }
                ListTag waypointList = dimTag.getListOrEmpty("Waypoints");
                HashMap<BlockPos, Boolean> dimWaypoints = new HashMap<BlockPos, Boolean>();
                for (int i = 0; i < waypointList.size(); ++i) {
                    CompoundTag entry = waypointList.getCompoundOrEmpty(i);
                    BlockPos pos = new BlockPos(entry.getIntOr("x", 0), entry.getIntOr("y", 0), entry.getIntOr("z", 0));
                    dimWaypoints.put(pos, true);
                }
                if (dimWaypoints.isEmpty()) continue;
                WAYPOINTS.put(dimKey, dimWaypoints);
            }
        }
    }

    private static CompoundTag saveToTag() {
        CompoundTag root = new CompoundTag();
        CompoundTag dims = new CompoundTag();
        HashSet<String> allDims = new HashSet<String>();
        allDims.addAll(NAMES.keySet());
        allDims.addAll(WAYPOINTS.keySet());
        for (String dimKey : allDims) {
            Map<BlockPos, Boolean> dimWaypoints;
            CompoundTag dimTag = new CompoundTag();
            Map<BlockPos, String> dimNames = NAMES.get(dimKey);
            if (dimNames != null && !dimNames.isEmpty()) {
                ListTag namesList = new ListTag();
                for (Map.Entry<BlockPos, String> entry : dimNames.entrySet()) {
                    CompoundTag entryTag = new CompoundTag();
                    entryTag.putInt("x", entry.getKey().getX());
                    entryTag.putInt("y", entry.getKey().getY());
                    entryTag.putInt("z", entry.getKey().getZ());
                    entryTag.putString("name", entry.getValue());
                    namesList.add((Tag)entryTag);
                }
                dimTag.put("Names", (Tag)namesList);
            }
            if ((dimWaypoints = WAYPOINTS.get(dimKey)) != null && !dimWaypoints.isEmpty()) {
                ListTag waypointList = new ListTag();
                for (Map.Entry<BlockPos, Boolean> entry : dimWaypoints.entrySet()) {
                    CompoundTag entryTag = new CompoundTag();
                    entryTag.putInt("x", entry.getKey().getX());
                    entryTag.putInt("y", entry.getKey().getY());
                    entryTag.putInt("z", entry.getKey().getZ());
                    waypointList.add((Tag)entryTag);
                }
                dimTag.put("Waypoints", (Tag)waypointList);
            }
            dims.put(dimKey, (Tag)dimTag);
        }
        root.put("Dimensions", (Tag)dims);
        return root;
    }

    private static Map<BlockPos, String> getNamesForDim(String dimKey) {
        return NAMES.computeIfAbsent(dimKey, k -> new HashMap());
    }

    private static Map<BlockPos, Boolean> getWaypointsForDim(String dimKey) {
        return WAYPOINTS.computeIfAbsent(dimKey, k -> new HashMap());
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        Level level = event.getLevel();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        if (serverLevel.getGameTime() % 5L != 0L) {
            return;
        }
        String dimKey = serverLevel.dimension().identifier().toString();
        BetterLodestones.cleanupBrokenLodestones(serverLevel, dimKey);
        for (Player player : serverLevel.players()) {
            AABB playerArea = player.getBoundingBox().inflate(16.0);
            List<ItemEntity> paperEntities = serverLevel.getEntitiesOfClass(ItemEntity.class, playerArea, item -> item.getItem().is(Items.PAPER) && item.getItem().has(DataComponents.CUSTOM_NAME));
            block0: for (ItemEntity paperEntity : paperEntities) {
                BlockPos entityPos = paperEntity.blockPosition();
                for (BlockPos checkPos : BlockPos.betweenClosed((BlockPos)entityPos.offset(-1, -1, -1), (BlockPos)entityPos.offset(1, 1, 1))) {
                    Component customName;
                    String name;
                    double dist;
                    BlockState state = serverLevel.getBlockState(checkPos);
                    if (!state.is(Blocks.LODESTONE) || (dist = paperEntity.position().distanceTo(Vec3.atCenterOf((Vec3i)checkPos))) > 1.5 || (name = (customName = paperEntity.getItem().getHoverName()).getString()).isEmpty()) continue;
                    BetterLodestones.getNamesForDim(dimKey).put(checkPos.immutable(), name);
                    paperEntity.getItem().shrink(1);
                    if (!paperEntity.getItem().isEmpty()) continue block0;
                    paperEntity.discard();
                    continue block0;
                }
            }
            List<ItemEntity> pearlEntities = serverLevel.getEntitiesOfClass(ItemEntity.class, playerArea, item -> item.getItem().is(Items.ENDER_PEARL));
            block2: for (ItemEntity pearlEntity : pearlEntities) {
                BlockPos entityPos = pearlEntity.blockPosition();
                for (BlockPos checkPos : BlockPos.betweenClosed((BlockPos)entityPos.offset(-1, -1, -1), (BlockPos)entityPos.offset(1, 1, 1))) {
                    double dist;
                    BlockState state = serverLevel.getBlockState(checkPos);
                    if (!state.is(Blocks.LODESTONE) || (dist = pearlEntity.position().distanceTo(Vec3.atCenterOf((Vec3i)checkPos))) > 1.5) continue;
                    BlockPos immutablePos = checkPos.immutable();
                    Map<BlockPos, Boolean> dimWaypoints = BetterLodestones.getWaypointsForDim(dimKey);
                    if (dimWaypoints.containsKey(immutablePos)) continue;
                    dimWaypoints.put(immutablePos, true);
                    pearlEntity.getItem().shrink(1);
                    if (!pearlEntity.getItem().isEmpty()) continue block2;
                    pearlEntity.discard();
                    continue block2;
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        ServerLevel level = player2.level();
        if (level.getGameTime() % 10L != 0L) {
            return;
        }
        String dimKey = level.dimension().identifier().toString();
        Map<BlockPos, String> dimNames = NAMES.get(dimKey);
        if (dimNames == null || dimNames.isEmpty()) {
            return;
        }
        Vec3 playerPos = player2.position();
        String nearestName = null;
        double nearestDistSq = Double.MAX_VALUE;
        for (Map.Entry<BlockPos, String> entry : dimNames.entrySet()) {
            BlockPos pos = entry.getKey();
            double distSq = playerPos.distanceToSqr(Vec3.atCenterOf((Vec3i)pos));
            if (!(distSq <= 64.0) || !(distSq < nearestDistSq) || !level.getBlockState(pos).is(Blocks.LODESTONE)) continue;
            nearestName = entry.getValue();
            nearestDistSq = distSq;
        }
        if (nearestName != null) {
            player2.displayClientMessage((Component)Component.literal(nearestName), true);
        }
    }

    @SubscribeEvent
    public static void onEnderPearlTeleport(EntityTeleportEvent.EnderPearl event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player = (ServerPlayer)entity;
        ServerLevel level = player.level();
        String dimKey = level.dimension().identifier().toString();
        Map<BlockPos, Boolean> dimWaypoints = WAYPOINTS.get(dimKey);
        if (dimWaypoints == null || dimWaypoints.isEmpty()) {
            return;
        }
        Vec3 landingPos = new Vec3(event.getTargetX(), event.getTargetY(), event.getTargetZ());
        BlockPos nearestWaypoint = null;
        double nearestDistSq = Double.MAX_VALUE;
        for (Map.Entry<BlockPos, Boolean> entry : dimWaypoints.entrySet()) {
            BlockPos waypointPos = entry.getKey();
            double distSq = landingPos.distanceToSqr(Vec3.atCenterOf((Vec3i)waypointPos));
            if (!(distSq <= 9.0) || !(distSq < nearestDistSq) || !level.getBlockState(waypointPos).is(Blocks.LODESTONE)) continue;
            nearestWaypoint = waypointPos;
            nearestDistSq = distSq;
        }
        if (nearestWaypoint != null) {
            event.setTargetX((double)nearestWaypoint.getX() + 0.5);
            event.setTargetY((double)nearestWaypoint.getY() + 1.0);
            event.setTargetZ((double)nearestWaypoint.getZ() + 0.5);
        }
    }

    private static void cleanupBrokenLodestones(ServerLevel level, String dimKey) {
        Map<BlockPos, Boolean> dimWaypoints;
        if (level.getGameTime() % 100L != 0L) {
            return;
        }
        Map<BlockPos, String> dimNames = NAMES.get(dimKey);
        if (dimNames != null) {
            Iterator<Map.Entry<BlockPos, String>> nameIt = dimNames.entrySet().iterator();
            while (nameIt.hasNext()) {
                Map.Entry<BlockPos, String> entry = nameIt.next();
                if (level.getBlockState(entry.getKey()).is(Blocks.LODESTONE)) continue;
                nameIt.remove();
            }
        }
        if ((dimWaypoints = WAYPOINTS.get(dimKey)) != null) {
            Iterator<Map.Entry<BlockPos, Boolean>> wpIt = dimWaypoints.entrySet().iterator();
            while (wpIt.hasNext()) {
                Map.Entry<BlockPos, Boolean> entry = wpIt.next();
                if (level.getBlockState(entry.getKey()).is(Blocks.LODESTONE)) continue;
                wpIt.remove();
            }
        }
    }
}

