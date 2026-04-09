package com.ultra.megamod.feature.casino;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.dimensions.DimensionHelper;
import com.ultra.megamod.feature.dimensions.MegaModDimensions;
import java.io.File;
import java.nio.file.Path;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

@EventBusSubscriber(modid = "megamod")
public class CasinoDimensionManager {
    private static boolean initialized = false;
    private static boolean dirty = false;
    private static final String FILE_NAME = "megamod_casino_dim.dat";
    private static final BlockPos SPAWN_POS = new BlockPos(15, 65, 5);
    private static final BlockPos CASINO_ORIGIN = new BlockPos(0, 64, 0);

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        ServerLevel overworld = event.getServer().overworld();
        loadFromDisk(overworld);
    }

    public static void enterCasino(ServerPlayer player) {
        ServerLevel overworld = player.level().getServer().overworld();
        ServerLevel casinoLevel = player.level().getServer().getLevel(MegaModDimensions.CASINO);
        if (casinoLevel == null) {
            player.sendSystemMessage(Component.literal("Casino dimension is not available!").withStyle(ChatFormatting.RED));
            MegaMod.LOGGER.warn("Casino dimension level not found -- cannot teleport player {}", player.getGameProfile().name());
            return;
        }
        if (!initialized) {
            player.sendSystemMessage(Component.literal("Building the casino...").withStyle(ChatFormatting.GOLD));
            CasinoPocketBuilder.buildCasino(casinoLevel, CASINO_ORIGIN);
            initialized = true;
            dirty = true;
            saveToDisk(overworld);
            MegaMod.LOGGER.info("Casino built at origin ({}, {}, {})", CASINO_ORIGIN.getX(), CASINO_ORIGIN.getY(), CASINO_ORIGIN.getZ());
        }
        DimensionHelper.teleportToDimension(player, MegaModDimensions.CASINO, SPAWN_POS, 0f, 0f);
        player.sendSystemMessage(Component.literal("Welcome to the MegaMod Casino!").withStyle(ChatFormatting.GOLD));
    }

    private static Path getSavePath(ServerLevel level) {
        File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
        File dataDir = new File(saveDir, "data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        return new File(dataDir, FILE_NAME).toPath();
    }

    public static void loadFromDisk(ServerLevel level) {
        try {
            Path path = getSavePath(level);
            if (!path.toFile().exists()) {
                MegaMod.LOGGER.info("No casino dimension data file found, starting fresh.");
                return;
            }
            CompoundTag root = NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap());
            initialized = root.getBooleanOr("initialized", false);
            MegaMod.LOGGER.info("Casino dimension data loaded. Initialized: {}", initialized);
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load casino dimension data!", e);
        }
    }

    public static void saveToDisk(ServerLevel level) {
        if (!dirty) {
            return;
        }
        try {
            Path path = getSavePath(level);
            CompoundTag root = new CompoundTag();
            root.putBoolean("initialized", initialized);
            NbtIo.writeCompressed(root, path);
            dirty = false;
            MegaMod.LOGGER.debug("Casino dimension data saved.");
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save casino dimension data!", e);
        }
    }

    public static void reset() {
        initialized = false;
        dirty = false;
    }

    public static boolean isInitialized() {
        return initialized;
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        if (initialized) {
            ServerLevel overworld = event.getServer().overworld();
            dirty = true;
            saveToDisk(overworld);
        }
        reset();
        MegaMod.LOGGER.info("Casino dimension data saved and reset on server stop.");
    }
}
