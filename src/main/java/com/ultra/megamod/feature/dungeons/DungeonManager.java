/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.NbtAccounter
 *  net.minecraft.nbt.NbtIo
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.storage.LevelResource
 *  net.neoforged.neoforge.network.PacketDistributor
 */
package com.ultra.megamod.feature.dungeons;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.dimensions.DimensionHelper;
import com.ultra.megamod.feature.dimensions.MegaModDimensions;
import com.ultra.megamod.feature.dimensions.PocketManager;
import com.ultra.megamod.feature.dungeons.insurance.InsuranceManager;
import com.ultra.megamod.feature.dungeons.DungeonTheme;
import com.ultra.megamod.feature.dungeons.DungeonTier;
import com.ultra.megamod.feature.dungeons.boss.DungeonAltarBlock;
import com.ultra.megamod.feature.dungeons.boss.FogWallBlock;
import com.ultra.megamod.feature.dungeons.entity.DungeonEntityRegistry;
import com.ultra.megamod.feature.dungeons.entity.DungeonMobEntity;
import com.ultra.megamod.feature.dungeons.entity.DungeonSlimeEntity;
import com.ultra.megamod.feature.dungeons.entity.HollowEntity;
import com.ultra.megamod.feature.dungeons.entity.RatEntity;
import com.ultra.megamod.feature.dungeons.entity.UndeadKnightEntity;
import com.ultra.megamod.feature.dungeons.entity.NagaEntity;
import com.ultra.megamod.feature.dungeons.entity.GrottolEntity;
import com.ultra.megamod.feature.dungeons.entity.LanternEntity;
import com.ultra.megamod.feature.dungeons.entity.FoliaathEntity;
import com.ultra.megamod.feature.dungeons.entity.UmvuthanaEntity;
import com.ultra.megamod.feature.dungeons.entity.SpawnerCarrierEntity;
import com.ultra.megamod.feature.dungeons.entity.BluffEntity;
import com.ultra.megamod.feature.dungeons.entity.BabyFoliaathEntity;
import com.ultra.megamod.feature.dungeons.generation.DungeonJigsawGenerator;
import com.ultra.megamod.feature.dungeons.generation.RoomTemplate;
import com.ultra.megamod.feature.dungeons.loot.DungeonChestLoot;
import com.ultra.megamod.feature.dungeons.network.DungeonSyncPayload;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import com.ultra.megamod.feature.furniture.FurnitureRegistry;
import com.ultra.megamod.feature.furniture.DungeonChestBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.network.PacketDistributor;

public class DungeonManager {
    private static DungeonManager INSTANCE;
    private static final String FILE_NAME = "megamod_dungeons.dat";
    private static final long ABANDON_TIMEOUT_TICKS = 6000L; // 5 minutes
    private final Map<String, DungeonInstance> instances = new HashMap<String, DungeonInstance>();
    private boolean dirty = false;

    public static DungeonManager get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new DungeonManager();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    public static void reset() {
        INSTANCE = null;
    }

    public String createDungeon(ServerPlayer player, DungeonTier tier) {
        UUID uuid = player.getUUID();
        if (this.isPlayerInDungeon(uuid)) {
            player.sendSystemMessage((Component)Component.literal((String)"You are already in a dungeon!").withStyle(ChatFormatting.RED));
            return null;
        }
        ServerLevel overworld = player.level().getServer().overworld();
        String instanceId = uuid.toString().substring(0, 8) + "-" + System.currentTimeMillis();
        PocketManager pocketManager = PocketManager.get(overworld);
        BlockPos origin = pocketManager.allocateDungeonPocket(instanceId);
        ServerLevel dungeonLevel = player.level().getServer().getLevel(MegaModDimensions.DUNGEON);
        if (dungeonLevel == null) {
            MegaMod.LOGGER.error("Dungeon dimension not found!");
            player.sendSystemMessage((Component)Component.literal((String)"Error: Dungeon dimension not available.").withStyle(ChatFormatting.RED));
            pocketManager.freeDungeonPocket(instanceId);
            return null;
        }
        DungeonTheme theme = DungeonTheme.random(dungeonLevel.getRandom());
        long startTime = overworld.getGameTime();
        DungeonInstance instance = new DungeonInstance(instanceId, tier, theme, uuid, origin, startTime);
        dungeonLevel.getChunk(origin);

        // Use data-driven jigsaw generation (no theme swap — uses DNL base stone palette)
        DungeonJigsawGenerator.DungeonLayout layout = DungeonJigsawGenerator.generate(
                dungeonLevel, origin, tier, dungeonLevel.getRandom());

        instance.totalRooms = Math.max(1, layout.getTotalPieces() - 1); // exclude entrance from count
        instance.bossAlive = true;

        // Boss chest positions are now set when ChaosSpawner gatekeeper dies (in ChaosSpawnerEntity.die())

        this.instances.put(instanceId, instance);

        // Spawn entities in all piece bounding boxes
        this.spawnDungeonEntitiesFromLayout(dungeonLevel, layout, tier, instanceId);

        this.markDirty();

        // Store and use the entrance position from the layout
        instance.entrancePos = layout.getEntrancePos();
        BlockPos spawnPos = instance.entrancePos;
        dungeonLevel.getChunk(spawnPos);
        // Safety: if entrance is on spikes, shift up one block
        if (dungeonLevel.getBlockState(spawnPos.below()).getBlock() instanceof com.ultra.megamod.feature.dungeons.block.SpikeBlock) {
            spawnPos = spawnPos.above();
        }
        // Safety: if spawn is inside a solid block, scan upward to find air
        spawnPos = ensureNotInsideBlock(dungeonLevel, spawnPos);
        DimensionHelper.teleportToDimension(player, MegaModDimensions.DUNGEON, spawnPos);
        player.sendSystemMessage((Component)Component.literal((String)("Entering Dungeon")).withStyle(new ChatFormatting[]{ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD}));
        player.sendSystemMessage((Component)Component.literal((String)("Tier: " + tier.getDisplayName() + " | Rooms: " + instance.totalRooms)).withStyle(ChatFormatting.GRAY));
        this.syncToPlayer(player, instance);
        MegaMod.LOGGER.info("Created dungeon {} for player {} (tier={})", new Object[]{instanceId, player.getGameProfile().name(), tier.getDisplayName()});
        return instanceId;
    }

    public DungeonInstance getDungeonForPlayer(UUID playerUUID) {
        for (DungeonInstance instance : this.instances.values()) {
            if (instance.containsPlayer(playerUUID)) return instance;
        }
        return null;
    }

    public Map<String, DungeonInstance> getAllInstances() {
        return Collections.unmodifiableMap(this.instances);
    }

    public DungeonInstance getInstance(String instanceId) {
        return this.instances.get(instanceId);
    }

    public int getActiveInstanceCount() {
        return this.instances.size();
    }

    public boolean isPlayerInDungeon(UUID playerUUID) {
        return this.getDungeonForPlayer(playerUUID) != null;
    }

    public void completeDungeon(String instanceId) {
        DungeonInstance instance = this.instances.get(instanceId);
        if (instance != null) {
            instance.activeBossCount = Math.max(0, instance.activeBossCount - 1);
            if (instance.activeBossCount <= 0) {
                instance.cleared = true;
                instance.bossAlive = false;
                instance.completedTime = System.currentTimeMillis();
                MegaMod.LOGGER.info("Dungeon {} completed!", (Object)instanceId);
                // Notify quest system of dungeon completion for all players in instance
                try {
                    net.minecraft.server.MinecraftServer server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
                    if (server != null) {
                        net.minecraft.server.level.ServerLevel overworld = server.overworld();
                        // Notify dungeon owner
                        com.ultra.megamod.feature.quests.QuestEventListener.onDungeonComplete(instance.playerUUID, overworld);
                        // Notify party members
                        for (java.util.UUID pid : instance.partyPlayers) {
                            if (!pid.equals(instance.playerUUID)) {
                                com.ultra.megamod.feature.quests.QuestEventListener.onDungeonComplete(pid, overworld);
                            }
                        }
                    }
                } catch (Exception ignored) {}
            } else {
                MegaMod.LOGGER.info("Boss defeated in dungeon {} ({} bosses remaining)", instanceId, instance.activeBossCount);
            }
            this.markDirty();
        }
    }

    public void bossSpawned(String instanceId) {
        DungeonInstance instance = this.instances.get(instanceId);
        if (instance != null) {
            instance.activeBossCount++;
            this.markDirty();
        }
    }

    public void chaosSpawnerDefeated(String instanceId) {
        DungeonInstance instance = this.instances.get(instanceId);
        if (instance != null) {
            instance.chaosSpawnerAlive = false;
            this.markDirty();
            MegaMod.LOGGER.info("ChaosSpawner gatekeeper defeated in dungeon {}", instanceId);
        }
    }

    public void placeBossLootChests(String instanceId, ServerLevel dungeonLevel) {
        DungeonInstance instance = this.instances.get(instanceId);
        if (instance == null || instance.bossChestPositions.isEmpty()) {
            return;
        }
        BlockState accent = Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
        for (int i = 0; i < instance.bossChestPositions.size(); ++i) {
            BlockPos chestPos = instance.bossChestPositions.get(i);
            // 2-block wide base under the chest
            placeChestBase(dungeonLevel, chestPos, accent);
            dungeonLevel.setBlock(chestPos, FurnitureRegistry.DUNGEON_CHEST_DECOR.get().defaultBlockState(), 3);
            BlockEntity be = dungeonLevel.getBlockEntity(chestPos);
            if (be instanceof DungeonChestBlockEntity chest) {
                chest.setPendingLoot(instance.tier, RoomTemplate.RoomType.BOSS, instance.bossChestPositions.size());
            }
        }
        MegaMod.LOGGER.info("Placed {} boss loot chests for dungeon {}", instance.bossChestPositions.size(), instanceId);
    }

    public void roomCleared(String instanceId) {
        DungeonInstance instance = this.instances.get(instanceId);
        if (instance != null) {
            ++instance.roomsCleared;
            this.markDirty();
        }
    }

    public void removePlayerFromDungeon(ServerPlayer player) {
        DungeonInstance instance = this.getDungeonForPlayer(player.getUUID());
        if (instance != null) {
            // Record analytics for admin Dungeon Analytics tab (voluntary extract)
            com.ultra.megamod.feature.computer.network.handlers.DungeonAnalyticsHandler.recordRun(
                    player.getGameProfile().name(), player.getUUID().toString(),
                    instance.tier.getDisplayName(), instance.theme.getDisplayName(),
                    "Extracted", instance.startTimeMs, System.currentTimeMillis(), "");
            // If this is a party member (not the owner), just remove them
            if (!instance.playerUUID.equals(player.getUUID())) {
                instance.partyPlayers.remove(player.getUUID());
                DimensionHelper.teleportBack(player);
                PacketDistributor.sendToPlayer((ServerPlayer)player, (CustomPacketPayload)new DungeonSyncPayload("", "", "", 0, 0, false), (CustomPacketPayload[])new CustomPacketPayload[0]);
                this.markDirty();
                return;
            }
            // Owner leaving - teleport all party members out too
            ServerLevel overworld = player.level().getServer().overworld();
            for (UUID partyId : new ArrayList<>(instance.partyPlayers)) {
                ServerPlayer partyMember = overworld.getServer().getPlayerList().getPlayer(partyId);
                if (partyMember != null) {
                    DimensionHelper.teleportBack(partyMember);
                    PacketDistributor.sendToPlayer((ServerPlayer)partyMember, (CustomPacketPayload)new DungeonSyncPayload("", "", "", 0, 0, false), (CustomPacketPayload[])new CustomPacketPayload[0]);
                    partyMember.sendSystemMessage(Component.literal("The dungeon owner left. You have been teleported out.").withStyle(ChatFormatting.YELLOW));
                }
            }
            this.cleanupDungeon(instance.instanceId, overworld);
            DimensionHelper.teleportBack(player);
            PacketDistributor.sendToPlayer((ServerPlayer)player, (CustomPacketPayload)new DungeonSyncPayload("", "", "", 0, 0, false), (CustomPacketPayload[])new CustomPacketPayload[0]);
        }
    }

    public void removePlayerFromDungeonOnDeath(ServerPlayer player) {
        DungeonInstance instance = this.getDungeonForPlayer(player.getUUID());
        if (instance == null) return;
        // Remove from party set (don't cleanup dungeon — other party members may still be alive)
        instance.partyPlayers.remove(player.getUUID());
        // If this is the owner, we don't remove from instanceId — just let the party check handle cleanup
        this.markDirty();
    }

    public boolean hasRemainingPlayersInDungeon(String instanceId, ServerLevel overworld) {
        DungeonInstance instance = this.instances.get(instanceId);
        if (instance == null) return false;
        // Check if the owner is in the dungeon dimension
        ServerPlayer owner = overworld.getServer().getPlayerList().getPlayer(instance.playerUUID);
        if (owner != null && owner.isAlive() && owner.level().dimension().equals(com.ultra.megamod.feature.dimensions.MegaModDimensions.DUNGEON)) {
            return true;
        }
        // Check party members
        for (UUID partyId : instance.partyPlayers) {
            ServerPlayer member = overworld.getServer().getPlayerList().getPlayer(partyId);
            if (member != null && member.isAlive() && member.level().dimension().equals(com.ultra.megamod.feature.dimensions.MegaModDimensions.DUNGEON)) {
                return true;
            }
        }
        return false;
    }

    public void addPlayerToDungeon(UUID playerUuid, String instanceId) {
        DungeonInstance instance = this.instances.get(instanceId);
        if (instance != null) {
            instance.partyPlayers.add(playerUuid);
            this.markDirty();
        }
    }

    public void cleanupDungeon(String instanceId, ServerLevel overworld) {
        DungeonInstance instance = this.instances.remove(instanceId);
        if (instance != null) {
            ServerLevel dungeonLevel = overworld.getServer().getLevel(MegaModDimensions.DUNGEON);
            if (dungeonLevel != null) {
                // Force-load chunks around the pocket origin so items in unloaded chunks are captured
                BlockPos origin = instance.blockPos;
                if (origin != null) {
                    int chunkRadius = 8; // ~128 blocks, covers dungeon extent
                    int originCX = origin.getX() >> 4;
                    int originCZ = origin.getZ() >> 4;
                    for (int cx = originCX - chunkRadius; cx <= originCX + chunkRadius; cx++) {
                        for (int cz = originCZ - chunkRadius; cz <= originCZ + chunkRadius; cz++) {
                            dungeonLevel.getChunk(cx, cz);
                        }
                    }
                }

                // Now remove ALL non-player entities from the dungeon dimension
                List<Entity> toDiscard = new ArrayList<>();
                for (Entity e : dungeonLevel.getAllEntities()) {
                    if (!(e instanceof ServerPlayer)) {
                        toDiscard.add(e);
                    }
                }
                for (Entity e : toDiscard) {
                    e.discard();
                }
                MegaMod.LOGGER.info("Cleaned dungeon dimension — removed {} entities", toDiscard.size());
            }
            PocketManager.get(overworld).freeDungeonPocket(instanceId);
            InsuranceManager.clearDungeonInsurance(instanceId);
            this.markDirty();
            MegaMod.LOGGER.info("Cleaned up dungeon instance {}", (Object)instanceId);
        }
    }

    public void markAbandoned(UUID playerUUID, long currentTime) {
        DungeonInstance instance = this.getDungeonForPlayer(playerUUID);
        if (instance != null) {
            instance.abandoned = true;
            instance.abandonedTime = currentTime;
            this.markDirty();
        }
    }

    public void unmarkAbandoned(UUID playerUUID) {
        DungeonInstance instance = this.getDungeonForPlayer(playerUUID);
        if (instance != null) {
            instance.abandoned = false;
            instance.abandonedTime = 0L;
            this.markDirty();
        }
    }

    public void cleanupExpiredDungeons(ServerLevel overworld, long currentTime) {
        ArrayList<String> toRemove = new ArrayList<String>();
        for (DungeonInstance instance : this.instances.values()) {
            // Cleanup abandoned dungeons after 5-min timeout
            if (instance.abandoned && currentTime - instance.abandonedTime > ABANDON_TIMEOUT_TICKS) {
                toRemove.add(instance.instanceId);
                continue;
            }
            // Cleanup cleared dungeons after 5-min timeout (completedTime is millis)
            if (instance.cleared && instance.completedTime > 0
                    && System.currentTimeMillis() - instance.completedTime > 300_000L) {
                toRemove.add(instance.instanceId);
            }
        }
        for (String id : toRemove) {
            this.cleanupDungeon(id, overworld);
            MegaMod.LOGGER.info("Auto-cleaned dungeon {}", (Object)id);
        }
    }

    public void syncToPlayer(ServerPlayer player, DungeonInstance instance) {
        PacketDistributor.sendToPlayer((ServerPlayer)player, (CustomPacketPayload)new DungeonSyncPayload(instance.instanceId, instance.tier.getDisplayName(), instance.theme.getDisplayName(), instance.roomsCleared, instance.totalRooms, instance.bossAlive), (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    // Legacy spawnDungeonEntities removed — mob spawning uses spawnDungeonEntitiesFromLayout

    /**
     * Spawn dungeon entities using the jigsaw layout's bounding boxes.
     * For the boss room, places the dungeon altar at the center of the floor.
     * For all other pieces, spawns mobs at valid floor positions within the bounds.
     * Uses a flat weighted mob pool (no theme-based selection).
     */
    private void spawnDungeonEntitiesFromLayout(ServerLevel level, DungeonJigsawGenerator.DungeonLayout layout,
                                                  DungeonTier tier, String instanceId) {
        BoundingBox bossRoom = layout.getBossRoom();
        int totalMobs = 0;
        int roomCount = 0;
        int smallPieceCount = 0;

        // Phase 1: Spawn mobs and place altar in boss room
        for (BoundingBox bounds : layout.getAllPieces()) {
            boolean isBossRoom = bossRoom != null && bounds.equals(bossRoom);
            int spawned = this.spawnMobsInArea(level, bounds, tier, isBossRoom, instanceId);
            totalMobs += spawned;
            int w = bounds.maxX() - bounds.minX() + 1;
            int d = bounds.maxZ() - bounds.minZ() + 1;
            if (w >= 9 && d >= 9) roomCount++;
            else smallPieceCount++;
        }

        // Phase 2: Place scattered treasure chests (tier-scaled)
        int[] scatteredRange = tier.getScatteredChestRange();
        int scatteredTarget = scatteredRange[0] + (scatteredRange[1] > scatteredRange[0]
                ? level.getRandom().nextInt(scatteredRange[1] - scatteredRange[0] + 1) : 0);
        if (scatteredTarget > 0) {
            placeScatteredChests(level, layout, tier, scatteredTarget);
        }

        // Boss chest count
        DungeonInstance inst = this.instances.get(instanceId);
        int bossChests = inst != null ? inst.bossChestPositions.size() : 0;

        // Summary log
        MegaMod.LOGGER.info("=== DUNGEON SUMMARY [{}] tier={} ===", instanceId, tier.getDisplayName());
        MegaMod.LOGGER.info("  Pieces: {} total ({} rooms + {} corridors/small)", layout.getTotalPieces(), roomCount, smallPieceCount);
        MegaMod.LOGGER.info("  Mobs: {} spawned (cap/room: {})", totalMobs, tier.getMobCap());
        MegaMod.LOGGER.info("  Chests: {} boss + {} scattered = {} custom total (vanilla DNL loot untouched)",
                bossChests, scatteredTarget, bossChests + scatteredTarget);
        MegaMod.LOGGER.info("  Boss room: {}", bossRoom != null ? "YES" : "MISSING");
    }

    /**
     * Place our custom TREASURE-type loot chests scattered through rooms.
     */
    private void placeScatteredChests(ServerLevel level, DungeonJigsawGenerator.DungeonLayout layout,
                                       DungeonTier tier, int target) {
        BoundingBox bossRoom = layout.getBossRoom();
        List<BoundingBox> candidates = new ArrayList<>();
        for (BoundingBox bb : layout.getAllPieces()) {
            if (bossRoom != null && bb.equals(bossRoom)) continue; // skip boss room
            int w = bb.maxX() - bb.minX() + 1;
            int d = bb.maxZ() - bb.minZ() + 1;
            if (w >= 9 && d >= 9) candidates.add(bb); // rooms only, not corridors
        }
        if (candidates.isEmpty()) return;

        int placed = 0;
        int attempts = 0;
        while (placed < target && attempts < target * 10) {
            attempts++;
            BoundingBox room = candidates.get(level.getRandom().nextInt(candidates.size()));
            BlockPos pos = findValidFloorPosition(level, room);
            if (pos == null) continue;

            // 2-block wide base under the chest
            placeChestBase(level, pos, Blocks.STONE_BRICKS.defaultBlockState());
            level.setBlock(pos, FurnitureRegistry.DUNGEON_CHEST_DECOR.get().defaultBlockState(), 3);
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof DungeonChestBlockEntity chest) {
                chest.setPendingLoot(tier, RoomTemplate.RoomType.TREASURE);
                placed++;
            }
        }
        if (placed > 0) {
            MegaMod.LOGGER.info("Placed {} scattered treasure chests (tier={})", placed, tier.getDisplayName());
        }
    }

    /**
     * Spawn mobs within a piece bounding box, or place the altar if it's the boss room.
     *
     * @param level      the dungeon dimension
     * @param bounds     bounding box of the placed piece
     * @param tier       difficulty tier
     * @param isBossRoom true if this piece is the grand_hall / boss room
     * @param instanceId the dungeon instance ID (for storing boss chest positions)
     */
    private int spawnMobsInArea(ServerLevel level, BoundingBox bounds, DungeonTier tier,
                                boolean isBossRoom, String instanceId) {
        if (isBossRoom) {
            // Spawn ChaosSpawnerEntity gatekeeper at the center of the boss room floor
            int centerX = bounds.minX() + (bounds.maxX() - bounds.minX()) / 2;
            int centerZ = bounds.minZ() + (bounds.maxZ() - bounds.minZ()) / 2;
            BlockPos spawnPos = null;
            for (int y = bounds.minY() + 1; y <= bounds.maxY() - 2; y++) {
                BlockPos candidate = new BlockPos(centerX, y, centerZ);
                if (!level.getBlockState(candidate.below()).isAir()
                        && level.getBlockState(candidate).isAir()
                        && level.getBlockState(candidate.above()).isAir()) {
                    spawnPos = candidate;
                    break;
                }
            }
            if (spawnPos == null) {
                spawnPos = new BlockPos(centerX, bounds.minY() + 1, centerZ);
            }

            com.ultra.megamod.feature.dungeons.entity.ChaosSpawnerEntity gatekeeper =
                    new com.ultra.megamod.feature.dungeons.entity.ChaosSpawnerEntity(
                            DungeonEntityRegistry.CHAOS_SPAWNER_GATEKEEPER.get(), level);
            gatekeeper.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
            gatekeeper.setState(com.ultra.megamod.feature.dungeons.entity.ChaosSpawnerEntity.State.SLEEPING);
            gatekeeper.setDifficultyMultiplier(tier.getDifficultyMultiplier());
            gatekeeper.setDungeonInstanceId(instanceId);
            gatekeeper.setSpawnPoint(spawnPos);
            level.addFreshEntity((Entity) gatekeeper);

            DungeonInstance inst = this.instances.get(instanceId);
            if (inst != null) {
                inst.chaosSpawnerAlive = true;
            }

            MegaMod.LOGGER.info("Spawned ChaosSpawner gatekeeper at {} for instance {}", spawnPos, instanceId);
            return 0; // boss room doesn't spawn regular mobs
        }

        // Skip very small pieces (corridors, fallbacks) — only spawn in pieces large enough to be rooms
        int width = bounds.maxX() - bounds.minX() + 1;
        int depth = bounds.maxZ() - bounds.minZ() + 1;
        if (width < 9 || depth < 9) {
            // Small piece — 30% chance for ambient rats (like corridor behavior)
            int ratSpawned = 0;
            if (level.getRandom().nextFloat() < 0.3f) {
                int ratCount = 1 + level.getRandom().nextInt(2);
                for (int i = 0; i < ratCount; i++) {
                    BlockPos ratPos = findValidFloorPosition(level, bounds);
                    if (ratPos != null) {
                        RatEntity.create(level, tier, ratPos);
                        ratSpawned++;
                    }
                }
            }
            return ratSpawned;
        }

        // Infernal+ grand_hall rooms (very large, 20+ blocks wide) have a chance to spawn a bonus altar
        if (tier.getLevel() >= DungeonTier.INFERNAL.getLevel() && width >= 20 && depth >= 20 && level.getRandom().nextFloat() < 0.35f) {
            int centerX = bounds.minX() + (bounds.maxX() - bounds.minX()) / 2;
            int centerZ = bounds.minZ() + (bounds.maxZ() - bounds.minZ()) / 2;
            BlockPos altarPos = null;
            for (int y = bounds.minY() + 1; y <= bounds.maxY() - 2; y++) {
                BlockPos candidate = new BlockPos(centerX, y, centerZ);
                if (!level.getBlockState(candidate.below()).isAir()
                        && level.getBlockState(candidate).isAir()
                        && level.getBlockState(candidate.above()).isAir()) {
                    altarPos = candidate;
                    break;
                }
            }
            if (altarPos == null) {
                altarPos = new BlockPos(centerX, bounds.minY() + 1, centerZ);
            }
            level.setBlock(altarPos, DungeonEntityRegistry.DUNGEON_ALTAR_BLOCK.get().defaultBlockState(), 3);
            MegaMod.LOGGER.info("Spawned bonus altar in Infernal grand_hall room at {}", altarPos);
        }

        // Regular room — mob count and cap scale with tier
        int baseMobs = 2 + level.getRandom().nextInt(2); // 2-3 base
        int tierBonus = (int)(tier.getDifficultyMultiplier() * 2.5f); // 2-10 extra
        int mobCap = tier.getMobCap();
        int mobCount = Math.min(baseMobs + tierBonus, mobCap);

        int spawned = 0;
        int maxAttempts = mobCount * 5; // prevent infinite loop on bad geometry
        int attempts = 0;

        while (spawned < mobCount && attempts < maxAttempts) {
            attempts++;
            BlockPos spawnPos = findValidFloorPosition(level, bounds);
            if (spawnPos != null) {
                spawnRandomMob(level, tier, spawnPos);
                spawned++;
            }
        }

        if (spawned > 0) {
            MegaMod.LOGGER.debug("Spawned {} mobs in area at ({},{},{}) ({}x{})", spawned,
                    bounds.minX() + width / 2, bounds.minY(), bounds.minZ() + depth / 2, width, depth);
        }
        return spawned;
    }

    /**
     * Find a valid floor position within the given bounding box.
     * Checks that the position has air above and a solid floor below.
     *
     * @return a valid spawn position, or null if none found after a few attempts
     */

    /** Place a 3x3 cross of base blocks under a chest position for a wider visual base. */
    private static void placeChestBase(ServerLevel level, BlockPos chestPos, BlockState base) {
        BlockPos below = chestPos.below();
        level.setBlock(below, base, 3);
        level.setBlock(below.north(), base, 3);
        level.setBlock(below.south(), base, 3);
        level.setBlock(below.east(), base, 3);
        level.setBlock(below.west(), base, 3);
    }

    private BlockPos findValidFloorPosition(ServerLevel level, BoundingBox bounds) {
        int width = bounds.maxX() - bounds.minX() + 1;
        int depth = bounds.maxZ() - bounds.minZ() + 1;
        int height = bounds.maxY() - bounds.minY() + 1;

        // Try up to 10 random positions
        for (int attempt = 0; attempt < 10; attempt++) {
            int x = bounds.minX() + 2 + level.getRandom().nextInt(Math.max(1, width - 4));
            int z = bounds.minZ() + 2 + level.getRandom().nextInt(Math.max(1, depth - 4));

            // Scan from floor upward to find a valid position (air with solid below)
            for (int y = bounds.minY() + 1; y < bounds.minY() + Math.min(height - 1, 4); y++) {
                BlockPos pos = new BlockPos(x, y, z);
                BlockPos below = pos.below();
                BlockPos above = pos.above();

                boolean floorSolid = !level.getBlockState(below).isAir();
                boolean posAir = level.getBlockState(pos).isAir();
                boolean aboveAir = level.getBlockState(above).isAir();
                // Don't spawn on spike blocks
                boolean floorIsSpike = level.getBlockState(below).getBlock() instanceof com.ultra.megamod.feature.dungeons.block.SpikeBlock;

                if (floorSolid && !floorIsSpike && posAir && aboveAir) {
                    return pos;
                }
            }
        }
        return null;
    }

    /**
     * Flat weighted mob pool matching the plan's mobs/random.json weights.
     * Total weight = 141 (excluding the 100 empty weight which is for jigsaw sparsity).
     */
    private static final int[][] MOB_WEIGHTS = {
            // {weight, mobIndex}
            {20, 0},  // DungeonMob
            {15, 1},  // UndeadKnight
            {15, 2},  // Rat
            {10, 3},  // Hollow
            {10, 4},  // DungeonSlime
            {3,  5},  // Naga (reduced from 8)
            {5,  6},  // Grottol
            {5,  7},  // Lantern
            {5,  8},  // Foliaath
            {3,  9},  // Umvuthana (warrior)
            {4,  10}, // SpawnerCarrier
            {15, 11}, // Zombie (vanilla)
            {15, 12}, // Skeleton (vanilla)
            {10, 13}, // Spider (vanilla)
            {1,  14}, // Bluff
            {2,  15}, // UmvuthanaRaptor (spawns pack)
            {1,  16}, // UmvuthanaCrane (healer)
    };
    private static final int MOB_TOTAL_WEIGHT;
    static {
        int tw = 0;
        for (int[] mw : MOB_WEIGHTS) tw += mw[0];
        MOB_TOTAL_WEIGHT = tw;
    }

    private void spawnRandomMob(ServerLevel level, DungeonTier tier, BlockPos pos) {
        int roll = level.getRandom().nextInt(MOB_TOTAL_WEIGHT);
        int cumulative = 0;
        int mobIndex = 0;
        for (int[] mw : MOB_WEIGHTS) {
            cumulative += mw[0];
            if (roll < cumulative) {
                mobIndex = mw[1];
                break;
            }
        }
        switch (mobIndex) {
            case 0  -> DungeonMobEntity.create(level, tier, pos);
            case 1  -> UndeadKnightEntity.create(level, tier, pos);
            case 2  -> RatEntity.create(level, tier, pos);
            case 3  -> HollowEntity.create(level, tier, pos);
            case 4  -> DungeonSlimeEntity.create(level, tier, pos);
            case 5  -> NagaEntity.create(level, tier, pos);
            case 6  -> GrottolEntity.create(level, tier, pos);
            case 7  -> LanternEntity.create(level, tier, pos);
            case 8  -> FoliaathEntity.create(level, tier, pos);
            case 9  -> UmvuthanaEntity.create(level, tier, pos);
            case 10 -> SpawnerCarrierEntity.create(level, tier, pos);
            case 11 -> spawnScaledVanillaMob(level, tier, EntityType.ZOMBIE, pos);
            case 12 -> spawnScaledVanillaMob(level, tier, EntityType.SKELETON, pos);
            case 13 -> spawnScaledVanillaMob(level, tier, EntityType.SPIDER, pos);
            case 14 -> BluffEntity.create(level, tier, pos);
            case 15 -> com.ultra.megamod.feature.dungeons.entity.UmvuthanaRaptorEntity.create(level, tier, pos);
            case 16 -> com.ultra.megamod.feature.dungeons.entity.UmvuthanaCraneEntity.create(level, tier, pos);
        }
    }

    private void spawnScaledVanillaMob(ServerLevel level, DungeonTier tier, EntityType<? extends Mob> type, BlockPos pos) {
        Mob mob = (Mob) type.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (mob == null) return;
        mob.setPos((double) pos.getX() + 0.5, pos.getY(), (double) pos.getZ() + 0.5);
        mob.setYRot(level.getRandom().nextFloat() * 360.0f);
        mob.setPersistenceRequired();
        // Scale HP and damage with tier
        float mult = tier.getDifficultyMultiplier();
        if (mob.getAttribute(Attributes.MAX_HEALTH) != null) {
            double baseHP = mob.getAttribute(Attributes.MAX_HEALTH).getBaseValue();
            mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(baseHP * mult);
            mob.setHealth((float) (baseHP * mult));
        }
        if (mob.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            double baseDmg = mob.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue();
            mob.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(baseDmg * mult);
        }
        // Apply tier-appropriate effects (Resistance, Speed, Strength etc.)
        tier.applyMobEffects(mob);
        level.addFreshEntity((Entity) mob);
    }

    private void spawnEliteMob(ServerLevel level, DungeonTier tier, BlockPos pos) {
        // Elite mob: an UndeadKnight with 3x HP and custom name
        UndeadKnightEntity elite = new UndeadKnightEntity(DungeonEntityRegistry.UNDEAD_KNIGHT.get(), level);
        elite.setPos((double) pos.getX() + 0.5, pos.getY(), (double) pos.getZ() + 0.5);
        elite.setYRot(level.getRandom().nextFloat() * 360.0f);
        elite.applyDungeonScaling(tier);
        // Triple the HP
        float currentMax = elite.getMaxHealth();
        elite.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) (currentMax * 3.0f));
        elite.setHealth(currentMax * 3.0f);
        elite.setCustomName(net.minecraft.network.chat.Component.literal("Elite Guardian").withStyle(net.minecraft.ChatFormatting.DARK_RED));
        elite.setCustomNameVisible(true);
        level.addFreshEntity((Entity) elite);
    }

    private void placeFogWall(ServerLevel level, BlockPos doorCenter, int roomHeight) {
        BlockState fogState = ((FogWallBlock)((Object)DungeonEntityRegistry.FOG_WALL_BLOCK.get())).defaultBlockState();
        for (int dx = -1; dx <= 1; ++dx) {
            for (int dy = 1; dy <= 3; ++dy) {
                level.setBlock(doorCenter.offset(dx, dy, 0), fogState, 3);
            }
        }
    }

    private static int countCombatAndBossRooms(List<RoomTemplate> rooms) {
        int count = 0;
        for (RoomTemplate room : rooms) {
            RoomTemplate.RoomType t = room.type();
            if (t == RoomTemplate.RoomType.COMBAT || t == RoomTemplate.RoomType.BOSS
                    || t == RoomTemplate.RoomType.MINI_BOSS || t == RoomTemplate.RoomType.AMBUSH
                    || t == RoomTemplate.RoomType.FLOODED || t == RoomTemplate.RoomType.ARENA
                    || t == RoomTemplate.RoomType.GAUNTLET || t == RoomTemplate.RoomType.SNAKE
                    || t == RoomTemplate.RoomType.CHECKER || t == RoomTemplate.RoomType.BRIDGE
                    || t == RoomTemplate.RoomType.PLATFORM || t == RoomTemplate.RoomType.GRAND_HALL
                    || t == RoomTemplate.RoomType.PRISON || t == RoomTemplate.RoomType.ZIGZAG
                    || t == RoomTemplate.RoomType.CROSS || t == RoomTemplate.RoomType.MAZE
                    || t == RoomTemplate.RoomType.VERTICAL) {
                ++count;
            }
        }
        return count;
    }

    /**
     * Ensures the spawn position isn't inside a solid block.
     * Scans upward up to 10 blocks to find 2 vertical air blocks; if none found,
     * clears a small pocket at the original position.
     */
    private static BlockPos ensureNotInsideBlock(ServerLevel level, BlockPos pos) {
        // Already safe — feet and head are both air
        if (level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir()) {
            return pos;
        }
        // Scan upward for a safe spot
        for (int dy = 1; dy <= 10; dy++) {
            BlockPos check = pos.above(dy);
            if (level.getBlockState(check).isAir() && level.getBlockState(check.above()).isAir()) {
                return check;
            }
        }
        // No safe spot found — carve out a small pocket
        MegaMod.LOGGER.warn("DungeonManager: Spawn {} is inside blocks, clearing pocket", pos);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = 0; dy <= 2; dy++) {
                    level.setBlockAndUpdate(pos.offset(dx, dy, dz),
                            net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
                }
            }
        }
        return pos;
    }

    private void markDirty() {
        this.dirty = true;
    }

    private void loadFromDisk(ServerLevel level) {
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (dataFile.exists()) {
                CompoundTag root = NbtIo.readCompressed((Path)dataFile.toPath(), (NbtAccounter)NbtAccounter.unlimitedHeap());
                CompoundTag instancesTag = root.getCompoundOrEmpty("instances");
                for (String key : instancesTag.keySet()) {
                    CompoundTag tag = instancesTag.getCompoundOrEmpty(key);
                    DungeonInstance instance = new DungeonInstance(key, DungeonTier.fromName(tag.getStringOr("tier", "NORMAL")), DungeonTheme.fromName(tag.getStringOr("theme", "NETHER_FORTRESS")), UUID.fromString(tag.getStringOr("playerUUID", "00000000-0000-0000-0000-000000000000")), new BlockPos(tag.getIntOr("posX", 0), tag.getIntOr("posY", 64), tag.getIntOr("posZ", 0)), tag.getLongOr("startTime", 0L));
                    instance.cleared = tag.getBooleanOr("cleared", false);
                    instance.totalRooms = tag.getIntOr("totalRooms", 0);
                    instance.roomsCleared = tag.getIntOr("roomsCleared", 0);
                    instance.bossAlive = tag.getBooleanOr("bossAlive", true);
                    instance.abandoned = tag.getBooleanOr("abandoned", false);
                    instance.abandonedTime = tag.getLongOr("abandonedTime", 0L);
                    instance.chaosSpawnerAlive = tag.getBooleanOr("chaosSpawnerAlive", true);
                    instance.activeBossCount = tag.getIntOr("activeBossCount", 0);
                    instance.completedTime = tag.getLongOr("completedTime", 0L);
                    int chestCount = tag.getIntOr("bossChestCount", 0);
                    for (int ci = 0; ci < chestCount; ++ci) {
                        instance.bossChestPositions.add(new BlockPos(
                                tag.getIntOr("bossChestX" + ci, 0),
                                tag.getIntOr("bossChestY" + ci, 0),
                                tag.getIntOr("bossChestZ" + ci, 0)));
                    }
                    // Restore party members
                    int partyCount = tag.getIntOr("partyCount", 0);
                    for (int pi = 0; pi < partyCount; ++pi) {
                        String puuid = tag.getStringOr("party" + pi, "");
                        if (!puuid.isEmpty()) {
                            instance.partyPlayers.add(UUID.fromString(puuid));
                        }
                    }
                    this.instances.put(key, instance);
                }
                MegaMod.LOGGER.info("Loaded {} dungeon instances from disk", (Object)this.instances.size());
            }
        }
        catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load dungeon data", (Throwable)e);
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
            CompoundTag instancesTag = new CompoundTag();
            for (Map.Entry<String, DungeonInstance> entry : this.instances.entrySet()) {
                DungeonInstance inst = entry.getValue();
                CompoundTag tag = new CompoundTag();
                tag.putString("tier", inst.tier.name());
                tag.putString("theme", inst.theme.name());
                tag.putString("playerUUID", inst.playerUUID.toString());
                tag.putInt("posX", inst.blockPos.getX());
                tag.putInt("posY", inst.blockPos.getY());
                tag.putInt("posZ", inst.blockPos.getZ());
                tag.putLong("startTime", inst.startTime);
                tag.putBoolean("cleared", inst.cleared);
                tag.putInt("totalRooms", inst.totalRooms);
                tag.putInt("roomsCleared", inst.roomsCleared);
                tag.putBoolean("bossAlive", inst.bossAlive);
                tag.putBoolean("abandoned", inst.abandoned);
                tag.putLong("abandonedTime", inst.abandonedTime);
                tag.putBoolean("chaosSpawnerAlive", inst.chaosSpawnerAlive);
                tag.putInt("activeBossCount", inst.activeBossCount);
                tag.putLong("completedTime", inst.completedTime);
                tag.putInt("bossChestCount", inst.bossChestPositions.size());
                for (int ci = 0; ci < inst.bossChestPositions.size(); ++ci) {
                    BlockPos cp = inst.bossChestPositions.get(ci);
                    tag.putInt("bossChestX" + ci, cp.getX());
                    tag.putInt("bossChestY" + ci, cp.getY());
                    tag.putInt("bossChestZ" + ci, cp.getZ());
                }
                // Save party members
                List<UUID> partyList = new ArrayList<>(inst.partyPlayers);
                tag.putInt("partyCount", partyList.size());
                for (int pi = 0; pi < partyList.size(); ++pi) {
                    tag.putString("party" + pi, partyList.get(pi).toString());
                }
                instancesTag.put(entry.getKey(), (Tag)tag);
            }
            root.put("instances", (Tag)instancesTag);
            NbtIo.writeCompressed((CompoundTag)root, (Path)dataFile.toPath());
            this.dirty = false;
        }
        catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save dungeon data", (Throwable)e);
        }
    }

    public static class DungeonInstance {
        public final String instanceId;
        public final DungeonTier tier;
        public final DungeonTheme theme;
        public final UUID playerUUID;
        public final BlockPos blockPos;
        public final long startTime;
        public boolean cleared;
        public int totalRooms;
        public int roomsCleared;
        public boolean bossAlive;
        public boolean abandoned;
        public long abandonedTime;
        public boolean chaosSpawnerAlive;
        public int activeBossCount;
        public long completedTime;
        public List<BlockPos> bossChestPositions = new ArrayList<>();
        public final Set<UUID> partyPlayers = new HashSet<>();
        public BlockPos entrancePos;
        public long startTimeMs;

        public DungeonInstance(String instanceId, DungeonTier tier, DungeonTheme theme, UUID playerUUID, BlockPos blockPos, long startTime) {
            this.instanceId = instanceId;
            this.tier = tier;
            this.theme = theme;
            this.playerUUID = playerUUID;
            this.blockPos = blockPos;
            this.startTime = startTime;
            this.cleared = false;
            this.totalRooms = 0;
            this.roomsCleared = 0;
            this.bossAlive = true;
            this.abandoned = false;
            this.abandonedTime = 0L;
            this.chaosSpawnerAlive = true;
            this.activeBossCount = 0;
            this.completedTime = 0L;
            this.startTimeMs = System.currentTimeMillis();
        }

        public boolean containsPlayer(UUID uuid) {
            return this.playerUUID.equals(uuid) || this.partyPlayers.contains(uuid);
        }
    }
}

