package com.ultra.megamod.feature.computer.admin;

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

/**
 * Admin-defined command aliases. Aliases map a short name to a full command string.
 * Supports {player} placeholder for the executing player's name.
 */
public class CommandAliasManager {

    private static CommandAliasManager INSTANCE;
    private static final String FILE_NAME = "megamod_command_aliases.dat";

    private final Map<String, String> aliases = new LinkedHashMap<>();
    private boolean dirty = false;

    public static CommandAliasManager get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new CommandAliasManager();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    public static void reset() { INSTANCE = null; }

    public Map<String, String> getAliases() {
        return Collections.unmodifiableMap(aliases);
    }

    public String addAlias(String name, String command) {
        name = name.toLowerCase(Locale.ROOT).trim();
        if (name.isEmpty()) return "Alias name cannot be empty.";
        if (name.contains(" ")) return "Alias name cannot contain spaces.";
        if (command.trim().isEmpty()) return "Command cannot be empty.";
        aliases.put(name, command.trim());
        dirty = true;
        return null;
    }

    public boolean removeAlias(String name) {
        if (aliases.remove(name.toLowerCase(Locale.ROOT)) != null) {
            dirty = true;
            return true;
        }
        return false;
    }

    /**
     * Resolve an alias. Returns the full command with {player} replaced, or null if not an alias.
     */
    public String resolve(String aliasName, String playerName) {
        String cmd = aliases.get(aliasName.toLowerCase(Locale.ROOT));
        if (cmd == null) return null;
        return cmd.replace("{player}", playerName);
    }

    // --- Persistence ---

    public void loadFromDisk(ServerLevel level) {
        aliases.clear();
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (dataFile.exists()) {
                CompoundTag root = NbtIo.readCompressed((Path) dataFile.toPath(), (NbtAccounter) NbtAccounter.unlimitedHeap());
                CompoundTag aliasTag = root.getCompoundOrEmpty("aliases");
                for (String key : aliasTag.keySet()) {
                    String cmd = aliasTag.getStringOr(key, "");
                    if (!cmd.isEmpty()) aliases.put(key, cmd);
                }
            }
        } catch (Exception e) { MegaMod.LOGGER.error("Failed to load command alias data", e); }
        dirty = false;
    }

    public void saveToDisk(ServerLevel level) {
        if (!dirty) return;
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataDir = new File(saveDir, "data");
            if (!dataDir.exists()) dataDir.mkdirs();
            File dataFile = new File(dataDir, FILE_NAME);

            CompoundTag root = new CompoundTag();
            CompoundTag aliasTag = new CompoundTag();
            for (Map.Entry<String, String> e : aliases.entrySet()) {
                aliasTag.putString(e.getKey(), e.getValue());
            }
            root.put("aliases", (Tag) aliasTag);
            NbtIo.writeCompressed((CompoundTag) root, (Path) dataFile.toPath());
            dirty = false;
        } catch (Exception e) { MegaMod.LOGGER.error("Failed to save command alias data", e); }
    }
}
