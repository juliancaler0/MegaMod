package com.ultra.megamod.feature.combat;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.skills.SkillBranch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side singleton managing class selection per player.
 * Players choose their class on first join; the choice gates class weapons,
 * armor, spells, and skill tree branches.
 *
 * Persistence: NbtIo to world/data/megamod_player_classes.dat
 */
public class PlayerClassManager {

    private static PlayerClassManager instance;
    private static final String FILE_NAME = "megamod_player_classes.dat";

    private final Map<UUID, PlayerClass> playerClasses = new ConcurrentHashMap<>();
    /** Permanently learned spells per player (bypass class/level requirements). */
    private final Map<UUID, Set<String>> learnedSpells = new ConcurrentHashMap<>();
    private boolean dirty = false;

    /**
     * The five player archetypes. NONE means the player hasn't chosen yet.
     */
    public enum PlayerClass {
        NONE("None", "No class selected.", "", 0xAAAAAA, null),
        PALADIN("Paladin",
                "Holy knight and healer. Excel at dungeon tanking and keeping your party alive. Claymores, hammers, shields, and healing magic make you the backbone of any group.",
                "Best for: Dungeons, Party Play, Colony Defense",
                0xFFD700, SkillBranch.PALADIN),
        WARRIOR("Warrior",
                "Berserker melee fighter. Charge into battle with devastating area attacks. Double axes, glaives, and war cries make you the ultimate damage dealer up close.",
                "Best for: Dungeons, Arena, PvP Combat",
                0xFF4444, SkillBranch.WARRIOR),
        WIZARD("Wizard",
                "Elemental spellcaster. Rain fire, frost, and arcane destruction from a distance. Wands and staves channel immense magical power.",
                "Best for: Dungeons, Boss Fights, Exploration",
                0xAA44FF, SkillBranch.WIZARD),
        ROGUE("Rogue",
                "Shadow assassin. Strike from stealth with lethal precision. Daggers and sickles paired with evasion make you deadly in 1v1 encounters.",
                "Best for: Arena, PvP, Bounty Hunting",
                0x22AA22, SkillBranch.ROGUE),
        RANGER("Ranger",
                "Master marksman and wilderness expert. Bows, crossbows, and nature magic let you control the battlefield from range.",
                "Best for: Exploration, Colony Defense, Dungeon Support",
                0x44BB44, SkillBranch.RANGER);

        private final String displayName;
        private final String description;
        private final String bestFor;
        private final int color;
        private final SkillBranch branch;

        PlayerClass(String displayName, String description, String bestFor, int color, SkillBranch branch) {
            this.displayName = displayName;
            this.description = description;
            this.bestFor = bestFor;
            this.color = color;
            this.branch = branch;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public String getBestFor() { return bestFor; }
        public int getColor() { return color; }

        /** Returns the SkillBranch this class maps to, or null for NONE. */
        public SkillBranch toBranch() { return branch; }
    }

    /** Set of all SkillBranch values that represent a class archetype. */
    public static final Set<SkillBranch> CLASS_BRANCHES = Set.of(
            SkillBranch.PALADIN, SkillBranch.WARRIOR, SkillBranch.WIZARD,
            SkillBranch.ROGUE, SkillBranch.RANGER
    );

    /** Returns true if the given SkillBranch is a class archetype branch. */
    public static boolean isClassBranch(SkillBranch branch) {
        return branch != null && CLASS_BRANCHES.contains(branch);
    }

    // ==================== Singleton ====================

    public static PlayerClassManager get(ServerLevel level) {
        if (instance == null) {
            instance = new PlayerClassManager();
            instance.loadFromDisk(level);
        }
        return instance;
    }

    public static void reset() {
        instance = null;
    }

    // ==================== Query / Mutate ====================

    public PlayerClass getPlayerClass(UUID playerId) {
        return playerClasses.getOrDefault(playerId, PlayerClass.NONE);
    }

    public boolean hasChosenClass(UUID playerId) {
        return getPlayerClass(playerId) != PlayerClass.NONE;
    }

    public void setClass(UUID playerId, PlayerClass cls) {
        playerClasses.put(playerId, cls);
        dirty = true;
    }

    /**
     * Checks whether a player's class allows them to use items locked behind
     * the given branch(es). For class archetype locks, the player's chosen class
     * must match branchA or branchB. Returns true if allowed.
     */
    public boolean classAllowsBranch(UUID playerId, SkillBranch branchA, SkillBranch branchB) {
        PlayerClass cls = getPlayerClass(playerId);
        if (cls == PlayerClass.NONE) return false;
        SkillBranch playerBranch = cls.toBranch();
        if (playerBranch == null) return false;
        if (branchA != null && playerBranch == branchA) return true;
        if (branchB != null && playerBranch == branchB) return true;
        return false;
    }

    /**
     * Returns all player class data (read-only view for admin panels).
     */
    public Map<UUID, PlayerClass> getAllClasses() {
        return Map.copyOf(playerClasses);
    }

    // ==================== Learned Spells ====================

    /**
     * Permanently learn a spell, bypassing class/level requirements.
     * Used by SpellScrollItem to grant spell access via consumable scrolls.
     */
    public void learnSpell(UUID playerId, String spellId) {
        learnedSpells.computeIfAbsent(playerId, k -> ConcurrentHashMap.newKeySet()).add(spellId);
        dirty = true;
    }

    /**
     * Returns true if the player has permanently learned the given spell via a scroll.
     */
    public boolean hasLearnedSpell(UUID playerId, String spellId) {
        Set<String> learned = learnedSpells.get(playerId);
        return learned != null && learned.contains(spellId);
    }

    /**
     * Returns all permanently learned spells for a player (read-only).
     */
    public Set<String> getLearnedSpells(UUID playerId) {
        Set<String> learned = learnedSpells.get(playerId);
        return learned != null ? Set.copyOf(learned) : Set.of();
    }

    // ==================== Persistence ====================

    public void saveToDisk(ServerLevel level) {
        if (!dirty) return;
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataDir = new File(saveDir, "data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            File dataFile = new File(dataDir, FILE_NAME);
            CompoundTag root = new CompoundTag();
            CompoundTag playersTag = new CompoundTag();
            for (Map.Entry<UUID, PlayerClass> entry : playerClasses.entrySet()) {
                playersTag.putString(entry.getKey().toString(), entry.getValue().name());
            }
            root.put("players", (Tag) playersTag);

            // Save learned spells
            CompoundTag spellsTag = new CompoundTag();
            for (Map.Entry<UUID, Set<String>> entry : learnedSpells.entrySet()) {
                CompoundTag playerSpells = new CompoundTag();
                int idx = 0;
                for (String spellId : entry.getValue()) {
                    playerSpells.putString("s" + idx, spellId);
                    idx++;
                }
                playerSpells.putInt("count", idx);
                spellsTag.put(entry.getKey().toString(), (Tag) playerSpells);
            }
            root.put("learnedSpells", (Tag) spellsTag);

            NbtIo.writeCompressed(root, dataFile.toPath());
            dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save player class data", e);
        }
    }

    public void loadFromDisk(ServerLevel level) {
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (dataFile.exists()) {
                CompoundTag root = NbtIo.readCompressed((Path) dataFile.toPath(), (NbtAccounter) NbtAccounter.unlimitedHeap());
                CompoundTag playersTag = root.getCompoundOrEmpty("players");
                for (String key : playersTag.keySet()) {
                    UUID uuid = UUID.fromString(key);
                    String className = playersTag.getStringOr(key, "NONE");
                    try {
                        PlayerClass cls = PlayerClass.valueOf(className);
                        playerClasses.put(uuid, cls);
                    } catch (IllegalArgumentException e) {
                        MegaMod.LOGGER.warn("Unknown player class '{}' for player {}, defaulting to NONE", className, key);
                        playerClasses.put(uuid, PlayerClass.NONE);
                    }
                }

                // Load learned spells
                CompoundTag spellsTag = root.getCompoundOrEmpty("learnedSpells");
                for (String key : spellsTag.keySet()) {
                    UUID uuid = UUID.fromString(key);
                    CompoundTag playerSpells = spellsTag.getCompoundOrEmpty(key);
                    int count = playerSpells.getIntOr("count", 0);
                    Set<String> spells = ConcurrentHashMap.newKeySet();
                    for (int i = 0; i < count; i++) {
                        String spellId = playerSpells.getStringOr("s" + i, "");
                        if (!spellId.isEmpty()) {
                            spells.add(spellId);
                        }
                    }
                    if (!spells.isEmpty()) {
                        learnedSpells.put(uuid, spells);
                    }
                }
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load player class data", e);
        }
    }

    private void markDirty() {
        this.dirty = true;
    }
}
