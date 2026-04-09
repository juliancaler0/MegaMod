/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.NbtAccounter
 *  net.minecraft.nbt.NbtIo
 *  net.minecraft.nbt.StringTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.player.AdvancementEvent$AdvancementEarnEvent
 *  net.neoforged.neoforge.event.server.ServerStoppingEvent
 *  net.neoforged.neoforge.event.tick.ServerTickEvent$Post
 */
package com.ultra.megamod.feature.museum;

import com.ultra.megamod.MegaMod;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class MuseumData {
    private static MuseumData INSTANCE;
    private final Map<UUID, Set<String>> donatedItems = new HashMap<UUID, Set<String>>();
    private final Map<UUID, Set<String>> donatedMobs = new HashMap<UUID, Set<String>>();
    private final Map<UUID, Set<String>> donatedArt = new HashMap<UUID, Set<String>>();
    private final Map<UUID, Set<String>> completedAchievements = new HashMap<UUID, Set<String>>();
    private final Map<UUID, Map<String, Long>> donationTimestamps = new HashMap<>();
    private final Map<UUID, Set<String>> claimedRewards = new HashMap<UUID, Set<String>>();
    private long currentDay = 0;
    private boolean dirty = false;
    private int tickCounter = 0;
    private static final int AUTO_SAVE_INTERVAL = 1200;

    public static MuseumData get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new MuseumData();
            INSTANCE.load(level);
        }
        return INSTANCE;
    }

    public void setCurrentDay(long day) {
        this.currentDay = day;
    }

    public long getDonationDay(UUID player, String id) {
        Map<String, Long> timestamps = this.donationTimestamps.get(player);
        if (timestamps == null) return -1;
        Long day = timestamps.get(id);
        return day != null ? day : -1;
    }

    public Map<String, Long> getAllTimestamps(UUID player) {
        return this.donationTimestamps.getOrDefault(player, Collections.emptyMap());
    }

    private void recordTimestamp(UUID player, String id) {
        Map<String, Long> timestamps = this.donationTimestamps.computeIfAbsent(player, k -> new HashMap<>());
        timestamps.put(id, this.currentDay);
    }

    public boolean donateItem(UUID player, String itemId) {
        Set set = this.donatedItems.computeIfAbsent(player, k -> new HashSet());
        if (set.add(itemId)) {
            this.recordTimestamp(player, itemId);
            this.markDirtyUrgent();
            return true;
        }
        return false;
    }

    public boolean donateMob(UUID player, String mobType) {
        Set set = this.donatedMobs.computeIfAbsent(player, k -> new HashSet());
        if (set.add(mobType)) {
            this.recordTimestamp(player, mobType);
            this.markDirtyUrgent();
            return true;
        }
        return false;
    }

    public boolean donateArt(UUID player, String artId) {
        Set set = this.donatedArt.computeIfAbsent(player, k -> new HashSet());
        if (set.add(artId)) {
            this.recordTimestamp(player, artId);
            this.markDirtyUrgent();
            return true;
        }
        return false;
    }

    public void recordAchievement(UUID player, String advancementId) {
        Set set = this.completedAchievements.computeIfAbsent(player, k -> new HashSet());
        if (set.add(advancementId)) {
            this.markDirtyUrgent();
        }
    }

    /**
     * Mark data as dirty and schedule a save within 5 seconds (100 ticks)
     * instead of waiting for the full 60-second auto-save cycle.
     */
    private void markDirtyUrgent() {
        this.dirty = true;
        if (this.tickCounter < AUTO_SAVE_INTERVAL - 100) {
            this.tickCounter = AUTO_SAVE_INTERVAL - 100;
        }
    }

    public Set<String> getDonatedItems(UUID player) {
        return this.donatedItems.getOrDefault(player, Collections.emptySet());
    }

    public Set<String> getDonatedMobs(UUID player) {
        return this.donatedMobs.getOrDefault(player, Collections.emptySet());
    }

    public Set<String> getDonatedArt(UUID player) {
        return this.donatedArt.getOrDefault(player, Collections.emptySet());
    }

    public Set<String> getCompletedAchievements(UUID player) {
        return this.completedAchievements.getOrDefault(player, Collections.emptySet());
    }

    public boolean claimReward(UUID player, String rewardId) {
        Set set = this.claimedRewards.computeIfAbsent(player, k -> new HashSet());
        if (set.add(rewardId)) {
            this.dirty = true;
            return true;
        }
        return false;
    }

    public boolean hasClaimedReward(UUID player, String rewardId) {
        return this.claimedRewards.getOrDefault(player, Collections.emptySet()).contains(rewardId);
    }

    public Set<String> getClaimedRewards(UUID player) {
        return this.claimedRewards.getOrDefault(player, Collections.emptySet());
    }

    private Path getSavePath(ServerLevel level) {
        File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
        File dataDir = new File(saveDir, "data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        return new File(dataDir, "megamod_museum.dat").toPath();
    }

    public void load(ServerLevel level) {
        Path path = this.getSavePath(level);
        if (!Files.exists(path, new LinkOption[0])) {
            MegaMod.LOGGER.info("No museum data file found, starting fresh.");
            return;
        }
        try {
            CompoundTag root = NbtIo.readCompressed((Path)path, (NbtAccounter)NbtAccounter.unlimitedHeap());
            for (String uuidStr : root.keySet()) {
                UUID uuid;
                try {
                    uuid = UUID.fromString(uuidStr);
                }
                catch (IllegalArgumentException e) {
                    continue;
                }
                CompoundTag playerTag = root.getCompoundOrEmpty(uuidStr);
                this.donatedItems.put(uuid, this.readStringSet(playerTag, "items"));
                this.donatedMobs.put(uuid, this.readStringSet(playerTag, "mobs"));
                this.donatedArt.put(uuid, this.readStringSet(playerTag, "art"));
                this.completedAchievements.put(uuid, this.readStringSet(playerTag, "achievements"));
                this.claimedRewards.put(uuid, this.readStringSet(playerTag, "rewards"));
                CompoundTag timestampsTag = playerTag.getCompoundOrEmpty("timestamps");
                if (!timestampsTag.keySet().isEmpty()) {
                    Map<String, Long> timestamps = new HashMap<>();
                    for (String tsKey : timestampsTag.keySet()) {
                        timestamps.put(tsKey, timestampsTag.getLongOr(tsKey, -1L));
                    }
                    this.donationTimestamps.put(uuid, timestamps);
                }
            }
            MegaMod.LOGGER.info("Museum data loaded for {} players.", (Object)this.donatedItems.size());
        }
        catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load museum data!", (Throwable)e);
        }
    }

    public void saveToDisk(ServerLevel level) {
        if (!this.dirty) {
            return;
        }
        Path path = this.getSavePath(level);
        try {
            CompoundTag root = new CompoundTag();
            HashSet<UUID> allPlayers = new HashSet<UUID>();
            allPlayers.addAll(this.donatedItems.keySet());
            allPlayers.addAll(this.donatedMobs.keySet());
            allPlayers.addAll(this.donatedArt.keySet());
            allPlayers.addAll(this.completedAchievements.keySet());
            allPlayers.addAll(this.donationTimestamps.keySet());
            allPlayers.addAll(this.claimedRewards.keySet());
            for (UUID uuid : allPlayers) {
                CompoundTag playerTag = new CompoundTag();
                this.writeStringSet(playerTag, "items", this.donatedItems.getOrDefault(uuid, Collections.emptySet()));
                this.writeStringSet(playerTag, "mobs", this.donatedMobs.getOrDefault(uuid, Collections.emptySet()));
                this.writeStringSet(playerTag, "art", this.donatedArt.getOrDefault(uuid, Collections.emptySet()));
                this.writeStringSet(playerTag, "achievements", this.completedAchievements.getOrDefault(uuid, Collections.emptySet()));
                this.writeStringSet(playerTag, "rewards", this.claimedRewards.getOrDefault(uuid, Collections.emptySet()));
                Map<String, Long> timestamps = this.donationTimestamps.get(uuid);
                if (timestamps != null && !timestamps.isEmpty()) {
                    CompoundTag timestampsTag = new CompoundTag();
                    for (Map.Entry<String, Long> tsEntry : timestamps.entrySet()) {
                        timestampsTag.putLong(tsEntry.getKey(), tsEntry.getValue());
                    }
                    playerTag.put("timestamps", (Tag)timestampsTag);
                }
                root.put(uuid.toString(), (Tag)playerTag);
            }
            NbtIo.writeCompressed((CompoundTag)root, (Path)path);
            this.dirty = false;
            MegaMod.LOGGER.debug("Museum data saved.");
        }
        catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save museum data!", (Throwable)e);
        }
    }

    public static void reset() {
        INSTANCE = null;
    }

    private Set<String> readStringSet(CompoundTag parent, String key) {
        HashSet<String> result = new HashSet<String>();
        ListTag list = parent.getListOrEmpty(key);
        for (int i = 0; i < list.size(); ++i) {
            Tag tag = list.get(i);
            if (!(tag instanceof StringTag)) continue;
            StringTag stringTag = (StringTag)tag;
            result.add(stringTag.value());
        }
        return result;
    }

    private void writeStringSet(CompoundTag parent, String key, Set<String> set) {
        ListTag list = new ListTag();
        for (String s : set) {
            list.add(StringTag.valueOf((String)s));
        }
        parent.put(key, (Tag)list);
    }

    void tick(ServerLevel level) {
        ++this.tickCounter;
        if (this.tickCounter >= 1200) {
            this.tickCounter = 0;
            this.saveToDisk(level);
        }
    }

    @EventBusSubscriber(modid="megamod")
    public static class Events {
        @SubscribeEvent
        public static void onServerStopping(ServerStoppingEvent event) {
            if (INSTANCE != null) {
                ServerLevel overworld = event.getServer().overworld();
                MuseumData.INSTANCE.dirty = true;
                INSTANCE.saveToDisk(overworld);
                MuseumData.reset();
                MegaMod.LOGGER.info("Museum data saved and reset on server stop.");
            }
        }

        @SubscribeEvent
        public static void onServerTick(ServerTickEvent.Post event) {
            if (INSTANCE != null) {
                ServerLevel overworld = event.getServer().overworld();
                INSTANCE.tick(overworld);
            }
        }

        @SubscribeEvent
        public static void onAdvancement(AdvancementEvent.AdvancementEarnEvent event) {
            Player player = event.getEntity();
            if (player instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer)player;
                ServerLevel level = serverPlayer.level();
                MuseumData data = MuseumData.get(level);
                String advancementId = event.getAdvancement().id().toString();
                data.recordAchievement(serverPlayer.getUUID(), advancementId);
            }
        }
    }
}

