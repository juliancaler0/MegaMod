/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.NbtAccounter
 *  net.minecraft.nbt.NbtIo
 *  net.minecraft.nbt.Tag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.storage.LevelResource
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.server.ServerStoppingEvent
 *  net.neoforged.neoforge.event.tick.ServerTickEvent$Post
 */
package com.ultra.megamod.feature.relics.research;

import com.ultra.megamod.MegaMod;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid="megamod")
public class ResearchManager {
    private static ResearchManager INSTANCE;
    private static final String FILE_NAME = "megamod_research.dat";
    private final Map<UUID, Set<String>> playerResearch = new HashMap<UUID, Set<String>>();
    private boolean dirty = false;

    public static ResearchManager get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new ResearchManager();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    public static void reset() {
        INSTANCE = null;
    }

    public boolean isResearched(UUID playerId, String relicId) {
        Set<String> researched = this.playerResearch.get(playerId);
        return researched != null && researched.contains(relicId);
    }

    public void research(UUID playerId, String relicId) {
        this.playerResearch.computeIfAbsent(playerId, k -> new HashSet()).add(relicId);
        this.dirty = true;
    }

    public int getResearchedCount(UUID playerId) {
        Set<String> researched = this.playerResearch.get(playerId);
        return researched == null ? 0 : researched.size();
    }

    private void loadFromDisk(ServerLevel level) {
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (dataFile.exists()) {
                CompoundTag root = NbtIo.readCompressed((Path)dataFile.toPath(), (NbtAccounter)NbtAccounter.unlimitedHeap());
                CompoundTag players = root.getCompoundOrEmpty("players");
                for (String key : players.keySet()) {
                    UUID uuid = UUID.fromString(key);
                    CompoundTag pData = players.getCompoundOrEmpty(key);
                    String csv = pData.getStringOr("researched", "");
                    Set researched = Arrays.stream(csv.split(",")).filter(s -> !s.isEmpty()).collect(Collectors.toCollection(HashSet::new));
                    this.playerResearch.put(uuid, researched);
                }
            }
        }
        catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load research data", e);
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
            CompoundTag players = new CompoundTag();
            for (Map.Entry<UUID, Set<String>> entry : this.playerResearch.entrySet()) {
                CompoundTag pData = new CompoundTag();
                pData.putString("researched", String.join((CharSequence)",", (Iterable<? extends CharSequence>)entry.getValue()));
                players.put(entry.getKey().toString(), (Tag)pData);
            }
            root.put("players", (Tag)players);
            NbtIo.writeCompressed((CompoundTag)root, (Path)dataFile.toPath());
            this.dirty = false;
        }
        catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save research data", e);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        if (INSTANCE != null) {
            ServerLevel overworld = event.getServer().overworld();
            ResearchManager.INSTANCE.dirty = true;
            INSTANCE.saveToDisk(overworld);
        }
        ResearchManager.reset();
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (INSTANCE == null) {
            return;
        }
        ServerLevel overworld = event.getServer().overworld();
        if (overworld.getGameTime() % 1200L != 600L) {
            return;
        }
        INSTANCE.saveToDisk(overworld);
    }
}

