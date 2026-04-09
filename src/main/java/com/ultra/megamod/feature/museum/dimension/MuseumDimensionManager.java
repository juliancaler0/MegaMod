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
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.storage.LevelResource
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.server.ServerStoppingEvent
 */
package com.ultra.megamod.feature.museum.dimension;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.dimensions.DimensionHelper;
import com.ultra.megamod.feature.dimensions.MegaModDimensions;
import com.ultra.megamod.feature.dimensions.PocketBuilder;
import com.ultra.megamod.feature.dimensions.PocketManager;
import com.ultra.megamod.feature.museum.CuratorEntity;
import com.ultra.megamod.feature.museum.MuseumBlock;
import com.ultra.megamod.feature.museum.MuseumRegistry;
import com.ultra.megamod.feature.museum.dimension.MuseumDisplayManager;
import com.ultra.megamod.feature.museum.dimension.MuseumStructure;
import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

@EventBusSubscriber(modid="megamod")
public class MuseumDimensionManager {
    private static MuseumDimensionManager INSTANCE;
    private static final String FILE_NAME = "megamod_museum_dim.dat";
    private final Set<UUID> initializedMuseums = new HashSet<UUID>();
    private boolean dirty = false;

    public static MuseumDimensionManager get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new MuseumDimensionManager();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    public static void reset() {
        INSTANCE = null;
    }

    public void enterMuseum(ServerPlayer player) {
        ServerLevel overworld = player.level().getServer().overworld();
        PocketManager pockets = PocketManager.get(overworld);
        UUID playerId = player.getUUID();
        BlockPos origin = pockets.getOrCreateMuseumPocket(playerId);
        pockets.saveToDisk(overworld);
        ServerLevel museumLevel = player.level().getServer().getLevel(MegaModDimensions.MUSEUM);
        if (museumLevel == null) {
            player.sendSystemMessage((Component)Component.literal((String)"Museum dimension is not available!").withStyle(ChatFormatting.RED));
            MegaMod.LOGGER.warn("Museum dimension level not found \u2014 cannot teleport player {}", (Object)player.getGameProfile().name());
            return;
        }
        if (!this.isMuseumInitialized(playerId)) {
            player.sendSystemMessage((Component)Component.literal((String)"Building your personal museum...").withStyle(ChatFormatting.GOLD));
            PocketBuilder.buildMuseumShell(museumLevel, origin);
            MuseumStructure.buildCorridorStubs(museumLevel, origin);
            BlockPos museumBlockPos = origin.offset(10, 1, 10);
            museumLevel.setBlock(museumBlockPos, ((MuseumBlock)((Object)MuseumRegistry.MUSEUM_BLOCK.get())).defaultBlockState(), 3);
            CuratorEntity curator = new CuratorEntity(MuseumRegistry.CURATOR_ENTITY.get(), (Level)museumLevel);
            curator.setPos((double)(origin.getX() + 10) + 0.5, (double)origin.getY() + 1.0, (double)(origin.getZ() + 12) + 0.5);
            curator.setYRot(180.0f);
            curator.setYBodyRot(180.0f);
            curator.setYHeadRot(180.0f);
            museumLevel.addFreshEntity((Entity)curator);
            this.setMuseumInitialized(playerId);
            this.saveToDisk(overworld);
            MegaMod.LOGGER.info("Built museum for player {} at origin ({}, {}, {})", new Object[]{player.getGameProfile().name(), origin.getX(), origin.getY(), origin.getZ()});
        }
        // Teleport player FIRST — this forces the museum dimension chunks and entities
        // to fully load. If we rebuild before teleporting, entities in unloaded chunks
        // survive the clear and duplicate on every visit.
        BlockPos spawnPos = origin.offset(10, 1, 4);
        DimensionHelper.teleportToDimension(player, MegaModDimensions.MUSEUM, spawnPos, 0.0f, 0.0f);
        // Now that player is in the museum, chunks are loaded — rebuild safely
        MuseumDisplayManager.rebuildWings(museumLevel, origin, playerId, player);
        player.sendSystemMessage((Component)Component.literal((String)"Welcome to your Museum!").withStyle(ChatFormatting.GOLD));
    }

    public void refreshMuseum(ServerPlayer player) {
        ServerLevel overworld = player.level().getServer().overworld();
        PocketManager pockets = PocketManager.get(overworld);
        UUID playerId = player.getUUID();
        BlockPos origin = pockets.getMuseumPocket(playerId);
        if (origin == null) return;
        ServerLevel museumLevel = player.level().getServer().getLevel(MegaModDimensions.MUSEUM);
        if (museumLevel == null) return;
        MuseumDisplayManager.rebuildWings(museumLevel, origin, playerId, player);
    }

    public boolean isMuseumInitialized(UUID playerId) {
        return this.initializedMuseums.contains(playerId);
    }

    public void setMuseumInitialized(UUID playerId) {
        this.initializedMuseums.add(playerId);
        this.dirty = true;
    }

    private Path getSavePath(ServerLevel level) {
        File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
        File dataDir = new File(saveDir, "data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        return new File(dataDir, FILE_NAME).toPath();
    }

    private void loadFromDisk(ServerLevel level) {
        try {
            Path path = this.getSavePath(level);
            if (!path.toFile().exists()) {
                MegaMod.LOGGER.info("No museum dimension data file found, starting fresh.");
                return;
            }
            CompoundTag root = NbtIo.readCompressed((Path)path, (NbtAccounter)NbtAccounter.unlimitedHeap());
            CompoundTag initialized = root.getCompoundOrEmpty("initialized");
            for (String key : initialized.keySet()) {
                try {
                    UUID uuid = UUID.fromString(key);
                    if (!initialized.getBooleanOr(key, false)) continue;
                    this.initializedMuseums.add(uuid);
                }
                catch (IllegalArgumentException illegalArgumentException) {}
            }
            MegaMod.LOGGER.info("Museum dimension data loaded: {} initialized museums.", (Object)this.initializedMuseums.size());
        }
        catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load museum dimension data!", (Throwable)e);
        }
    }

    public void saveToDisk(ServerLevel level) {
        if (!this.dirty) {
            return;
        }
        try {
            Path path = this.getSavePath(level);
            CompoundTag root = new CompoundTag();
            CompoundTag initialized = new CompoundTag();
            for (UUID uuid : this.initializedMuseums) {
                initialized.putBoolean(uuid.toString(), true);
            }
            root.put("initialized", (Tag)initialized);
            NbtIo.writeCompressed((CompoundTag)root, (Path)path);
            this.dirty = false;
            MegaMod.LOGGER.debug("Museum dimension data saved.");
        }
        catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save museum dimension data!", (Throwable)e);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        if (INSTANCE != null) {
            ServerLevel overworld = event.getServer().overworld();
            MuseumDimensionManager.INSTANCE.dirty = true;
            INSTANCE.saveToDisk(overworld);
            MuseumDimensionManager.reset();
            MegaMod.LOGGER.info("Museum dimension data saved and reset on server stop.");
        }
    }
}

