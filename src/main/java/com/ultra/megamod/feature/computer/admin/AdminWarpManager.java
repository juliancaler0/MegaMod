package com.ultra.megamod.feature.computer.admin;

import com.ultra.megamod.MegaMod;
import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

public class AdminWarpManager {
    private static AdminWarpManager INSTANCE;
    private static final String FILE_NAME = "megamod_admin_warps.dat";
    private final Map<String, WarpPoint> warps = new LinkedHashMap<String, WarpPoint>();
    private boolean dirty = false;

    public record WarpPoint(double x, double y, double z, float yRot, float xRot, String dimension) {}

    public static AdminWarpManager get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new AdminWarpManager();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    public static void reset() {
        INSTANCE = null;
    }

    public Map<String, WarpPoint> getAllWarps() {
        return Collections.unmodifiableMap(this.warps);
    }

    public void saveWarp(String name, ServerPlayer player) {
        this.warps.put(name, new WarpPoint(
            player.getX(), player.getY(), player.getZ(),
            player.getYRot(), player.getXRot(),
            player.level().dimension().identifier().toString()
        ));
        this.dirty = true;
    }

    public void deleteWarp(String name) {
        this.warps.remove(name);
        this.dirty = true;
    }

    public WarpPoint getWarp(String name) {
        return this.warps.get(name);
    }

    private void loadFromDisk(ServerLevel level) {
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (dataFile.exists()) {
                CompoundTag root = NbtIo.readCompressed((Path)dataFile.toPath(), (NbtAccounter)NbtAccounter.unlimitedHeap());
                CompoundTag warpsTag = root.getCompoundOrEmpty("warps");
                for (String key : warpsTag.keySet()) {
                    CompoundTag wTag = warpsTag.getCompoundOrEmpty(key);
                    this.warps.put(key, new WarpPoint(
                        wTag.getDoubleOr("x", 0), wTag.getDoubleOr("y", 64), wTag.getDoubleOr("z", 0),
                        wTag.getFloatOr("yRot", 0), wTag.getFloatOr("xRot", 0),
                        wTag.getStringOr("dim", "minecraft:overworld")
                    ));
                }
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load warp data", e);
        }
    }

    public void saveToDisk(ServerLevel level) {
        if (!this.dirty) return;
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataDir = new File(saveDir, "data");
            if (!dataDir.exists()) dataDir.mkdirs();
            File dataFile = new File(dataDir, FILE_NAME);
            CompoundTag root = new CompoundTag();
            CompoundTag warpsTag = new CompoundTag();
            for (Map.Entry<String, WarpPoint> entry : this.warps.entrySet()) {
                CompoundTag wTag = new CompoundTag();
                WarpPoint wp = entry.getValue();
                wTag.putDouble("x", wp.x());
                wTag.putDouble("y", wp.y());
                wTag.putDouble("z", wp.z());
                wTag.putFloat("yRot", wp.yRot());
                wTag.putFloat("xRot", wp.xRot());
                wTag.putString("dim", wp.dimension());
                warpsTag.put(entry.getKey(), (Tag)wTag);
            }
            root.put("warps", (Tag)warpsTag);
            NbtIo.writeCompressed((CompoundTag)root, (Path)dataFile.toPath());
            this.dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save warp data", e);
        }
    }
}
