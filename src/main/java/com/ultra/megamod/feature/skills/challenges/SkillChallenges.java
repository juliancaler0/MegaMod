package com.ultra.megamod.feature.skills.challenges;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.economy.EconomyManager;
import com.ultra.megamod.feature.skills.SkillManager;
import com.ultra.megamod.feature.skills.SkillTreeType;
import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.brewing.PlayerBrewedPotionEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.TradeWithVillagerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = "megamod")
public class SkillChallenges {
    private static final String FILE_NAME = "megamod_challenges.dat";
    private static final int REWARD_XP = 300;
    private static final int REWARD_COINS = 50;
    private static final int CHALLENGES_PER_WEEK = 3;

    // Challenge pool: {name, type, target, relatedTree}
    private static final Challenge[] CHALLENGE_POOL = {
        new Challenge("Mine 500 Ores", "mine", 500, SkillTreeType.MINING),
        new Challenge("Kill 200 Mobs", "kill", 200, SkillTreeType.COMBAT),
        new Challenge("Craft 100 Items", "craft", 100, SkillTreeType.MINING),
        new Challenge("Explore 300 Chunks", "explore", 300, SkillTreeType.SURVIVAL),
        new Challenge("Catch 50 Fish", "fish", 50, SkillTreeType.FARMING),
        new Challenge("Trade 30 Times", "trade", 30, SkillTreeType.SURVIVAL),
        new Challenge("Breed 20 Animals", "breed", 20, SkillTreeType.FARMING),
        new Challenge("Mine 200 Deepslate Ores", "mine", 200, SkillTreeType.MINING),
        new Challenge("Kill 50 Undead", "kill", 50, SkillTreeType.COMBAT),
        new Challenge("Harvest 150 Crops", "harvest", 150, SkillTreeType.FARMING),
        new Challenge("Craft 50 Tools", "craft_tools", 50, SkillTreeType.MINING),
        new Challenge("Kill 100 Mobs", "kill", 100, SkillTreeType.COMBAT),
        new Challenge("Explore 150 Chunks", "explore", 150, SkillTreeType.SURVIVAL),
        new Challenge("Catch 25 Fish", "fish", 25, SkillTreeType.FARMING),
        new Challenge("Mine 1000 Blocks", "mine_any", 1000, SkillTreeType.MINING),
        new Challenge("Enchant 10 Items", "enchant", 10, SkillTreeType.ARCANE),
        new Challenge("Brew 15 Potions", "brew", 15, SkillTreeType.ARCANE),
        new Challenge("Use 20 Relic Abilities", "ability_use", 20, SkillTreeType.ARCANE),
        new Challenge("Complete 3 Dungeons", "dungeon_clear", 3, SkillTreeType.SURVIVAL),
        new Challenge("Earn 200 MegaCoins", "earn_coins", 200, SkillTreeType.SURVIVAL),
        new Challenge("Discover 5 New Biomes", "discover_biome", 5, SkillTreeType.SURVIVAL),
        new Challenge("Breed 5 Different Species", "breed_species", 5, SkillTreeType.FARMING),
        new Challenge("Smelt 100 Items", "smelt", 100, SkillTreeType.MINING),
        new Challenge("Kill 30 Mobs with Crits", "crit_kill", 30, SkillTreeType.COMBAT),
        new Challenge("Walk 5000 Blocks", "walk", 5000, SkillTreeType.SURVIVAL),
        new Challenge("Harvest 50 Sweet Berries", "harvest_berries", 50, SkillTreeType.FARMING),
        new Challenge("Mine 50 Diamond Ore", "mine_diamond", 50, SkillTreeType.MINING),
        new Challenge("Kill 5 Dungeon Bosses", "kill_boss", 5, SkillTreeType.COMBAT),
        new Challenge("Clear a Nightmare Dungeon", "dungeon_nightmare", 1, SkillTreeType.SURVIVAL),
        new Challenge("Discover 3 New Relics", "discover_relic", 3, SkillTreeType.ARCANE),
        new Challenge("Spend 500 MegaCoins", "spend_coins", 500, SkillTreeType.SURVIVAL),
        new Challenge("Mine 50 Blocks Below Y=-40", "mine_deep", 50, SkillTreeType.MINING),
        new Challenge("Build 3 Colony Structures", "colony_build", 3, SkillTreeType.SURVIVAL),
    };

    // State
    private static long currentWeek = -1;
    private static int[] activeIndices = new int[CHALLENGES_PER_WEEK];
    private static final Map<UUID, int[]> playerProgress = new HashMap<>();
    private static boolean dirty = false;
    private static boolean loaded = false;

    public record Challenge(String name, String type, int target, SkillTreeType tree) {}

    private static long getWeekNumber() {
        return System.currentTimeMillis() / (7L * 24 * 60 * 60 * 1000);
    }

    private static void ensureLoaded(ServerLevel level) {
        if (!loaded) {
            loadFromDisk(level);
            loaded = true;
        }
    }

    private static void checkWeekRotation() {
        long week = getWeekNumber();
        if (week != currentWeek) {
            currentWeek = week;
            playerProgress.clear();
            // Pick 3 challenges based on week number (deterministic)
            int seed = (int) (week * 7919);
            for (int i = 0; i < CHALLENGES_PER_WEEK; i++) {
                activeIndices[i] = Math.abs((seed + i * 31) % CHALLENGE_POOL.length);
                // Avoid duplicates
                for (int j = 0; j < i; j++) {
                    if (activeIndices[i] == activeIndices[j]) {
                        activeIndices[i] = (activeIndices[i] + 1) % CHALLENGE_POOL.length;
                        j = -1; // restart duplicate check
                    }
                }
            }
            dirty = true;
        }
    }

    private static int[] getProgress(UUID playerId) {
        return playerProgress.computeIfAbsent(playerId, k -> new int[CHALLENGES_PER_WEEK]);
    }

    private static void incrementProgress(ServerPlayer player, String type, int amount) {
        ServerLevel level = (ServerLevel) player.level();
        ensureLoaded(level);
        checkWeekRotation();
        int[] progress = getProgress(player.getUUID());
        for (int i = 0; i < CHALLENGES_PER_WEEK; i++) {
            Challenge c = CHALLENGE_POOL[activeIndices[i]];
            if (!c.type().equals(type)) continue;
            if (progress[i] >= c.target()) continue; // already completed
            progress[i] = Math.min(progress[i] + amount, c.target());
            dirty = true;
            if (progress[i] >= c.target()) {
                // Challenge completed!
                SkillManager manager = SkillManager.get(level);
                manager.addXp(player.getUUID(), c.tree(), REWARD_XP);
                EconomyManager.get(level.getServer().overworld()).addWallet(player.getUUID(), REWARD_COINS);
                player.sendSystemMessage(Component.literal("Challenge Complete: " + c.name() + "! +" + REWARD_XP + " " + c.tree().getDisplayName() + " XP, +" + REWARD_COINS + " MC").withStyle(ChatFormatting.GOLD));
            }
        }
    }

    /**
     * Public API for other systems to increment challenge progress.
     */
    public static void addProgress(ServerPlayer player, String type, int amount) {
        incrementProgress(player, type, amount);
    }

    // === Event Hooks ===

    @SubscribeEvent
    public static void onMobKill(LivingDeathEvent event) {
        Entity source = event.getSource().getEntity();
        if (!(source instanceof ServerPlayer player)) return;
        if (event.getEntity() instanceof Player) return;
        incrementProgress(player, "kill", 1);
        // Check for dungeon boss kill
        net.minecraft.resources.Identifier mobId = BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType());
        if ("megamod".equals(mobId.getNamespace())) {
            String mobPath = mobId.getPath();
            if (mobPath.contains("boss") || mobPath.equals("wraith") || mobPath.equals("ossukage")
                    || mobPath.equals("dungeon_keeper") || mobPath.equals("frostmaw")
                    || mobPath.equals("wroughtnaut") || mobPath.equals("umvuthi")
                    || mobPath.equals("chaos_spawner") || mobPath.equals("sculptor")) {
                incrementProgress(player, "kill_boss", 1);
            }
        }
        // Check if this was a crit kill (tagged by AttributeEvents)
        if (event.getEntity().getPersistentData().getBooleanOr("megamod_was_crit", false)) {
            incrementProgress(player, "crit_kill", 1);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (((net.minecraft.world.level.Level) event.getLevel()).isClientSide()) return;
        String blockName = BuiltInRegistries.BLOCK.getKey(event.getState().getBlock()).getPath();
        if (blockName.contains("ore")) {
            incrementProgress(player, "mine", 1);
        }
        if (blockName.contains("wheat") || blockName.contains("carrots") || blockName.contains("potatoes")
                || blockName.contains("beetroot") || blockName.contains("nether_wart")
                || blockName.contains("melon") || blockName.contains("pumpkin")) {
            incrementProgress(player, "harvest", 1);
        }
        if (blockName.contains("diamond_ore")) {
            incrementProgress(player, "mine_diamond", 1);
        }
        if (blockName.contains("sweet_berry")) {
            incrementProgress(player, "harvest_berries", 1);
        }
        // Mine deep: blocks mined below Y=-40
        if (event.getPos().getY() < -40) {
            incrementProgress(player, "mine_deep", 1);
        }
        incrementProgress(player, "mine_any", 1);
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        incrementProgress(player, "craft", 1);
        ItemStack stack = event.getCrafting();
        String itemName = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        if (itemName.contains("pickaxe") || itemName.contains("axe") || itemName.contains("shovel")
                || itemName.contains("hoe") || itemName.contains("sword")) {
            incrementProgress(player, "craft_tools", 1);
        }
    }

    @SubscribeEvent
    public static void onFishing(ItemFishedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        incrementProgress(player, "fish", 1);
    }

    @SubscribeEvent
    public static void onTrade(TradeWithVillagerEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        incrementProgress(player, "trade", 1);
    }

    // Track unique species bred per player per week for breed_species challenge
    private static final Map<UUID, Set<String>> breedSpeciesTracker = new HashMap<>();

    @SubscribeEvent
    public static void onBreeding(BabyEntitySpawnEvent event) {
        if (!(event.getCausedByPlayer() instanceof ServerPlayer player)) return;
        incrementProgress(player, "breed", 1);
        // Track unique species
        String species = BuiltInRegistries.ENTITY_TYPE.getKey(event.getParentA().getType()).getPath();
        UUID uuid = player.getUUID();
        Set<String> bred = breedSpeciesTracker.computeIfAbsent(uuid, k -> new HashSet<>());
        if (bred.add(species)) {
            incrementProgress(player, "breed_species", 1);
        }
    }

    @SubscribeEvent
    public static void onBrew(PlayerBrewedPotionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        incrementProgress(player, "brew", 1);
    }

    @SubscribeEvent
    public static void onSmelt(PlayerEvent.ItemSmeltedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        incrementProgress(player, "smelt", event.getSmelting().getCount());
    }

    @SubscribeEvent
    public static void onEnchant(net.neoforged.neoforge.event.enchanting.EnchantmentLevelSetEvent event) {
        // EnchantmentLevelSetEvent has no player accessor; find nearest player to the enchanting table
        net.minecraft.world.level.Level eventLevel = event.getLevel();
        if (eventLevel.isClientSide()) return;
        net.minecraft.world.entity.player.Player nearest = eventLevel.getNearestPlayer(
            event.getPos().getX() + 0.5, event.getPos().getY() + 0.5, event.getPos().getZ() + 0.5, 5.0, false);
        if (!(nearest instanceof ServerPlayer player)) return;
        incrementProgress(player, "enchant", 1);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        ServerLevel overworld = event.getServer().overworld();
        long gameTime = overworld.getGameTime();
        ensureLoaded(overworld);
        checkWeekRotation();
        // Wire earn_coins callback once
        if (EconomyManager.onCoinsEarned == null) {
            EconomyManager.onCoinsEarned = (uuid, amount) -> {
                ServerPlayer sp = event.getServer().getPlayerList().getPlayer(uuid);
                if (sp != null) incrementProgress(sp, "earn_coins", amount);
            };
        }
        // Wire spend_coins callback once
        if (EconomyManager.onCoinsSpent == null) {
            EconomyManager.onCoinsSpent = (uuid, amount) -> {
                ServerPlayer sp = event.getServer().getPlayerList().getPlayer(uuid);
                if (sp != null) incrementProgress(sp, "spend_coins", amount);
            };
        }
        // Check exploration progress
        if (gameTime % 100L == 0L) {
            for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
                // Track chunk for challenge (separate from skill XP chunk tracking)
                int cx = player.blockPosition().getX() >> 4;
                int cz = player.blockPosition().getZ() >> 4;
                long chunkKey = ((long) cx << 32) | (cz & 0xFFFFFFFFL);
                UUID uuid = player.getUUID();
                // Use simple set stored per-player to track visited chunks this week
                if (exploredChunks.computeIfAbsent(uuid, k -> new HashSet<>()).add(chunkKey)) {
                    incrementProgress(player, "explore", 1);
                }
                // Track walking distance for challenges
                double[] lastPos = lastPositions.get(uuid);
                double px = player.getX();
                double pz = player.getZ();
                if (lastPos != null) {
                    double dist = Math.sqrt((px - lastPos[0]) * (px - lastPos[0]) + (pz - lastPos[1]) * (pz - lastPos[1]));
                    if (dist > 1.0 && dist < 100.0) { // Sanity check, no teleports
                        incrementProgress(player, "walk", (int) dist);
                    }
                }
                lastPositions.put(uuid, new double[]{px, pz});
            }
        }
        // Save periodically
        if (gameTime % 1200L == 0L) {
            saveToDisk(overworld);
        }
    }

    private static final Map<UUID, Set<Long>> exploredChunks = new HashMap<>();
    private static final Map<UUID, double[]> lastPositions = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        exploredChunks.remove(event.getEntity().getUUID());
        lastPositions.remove(event.getEntity().getUUID());
        breedSpeciesTracker.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ServerLevel overworld = event.getServer().overworld();
        saveToDisk(overworld);
        playerProgress.clear();
        exploredChunks.clear();
        lastPositions.clear();
        breedSpeciesTracker.clear();
        loaded = false;
        currentWeek = -1;
    }

    // === Public Accessors for Computer App ===

    public static Challenge[] getActiveChallenges(ServerLevel level) {
        ensureLoaded(level);
        checkWeekRotation();
        Challenge[] active = new Challenge[CHALLENGES_PER_WEEK];
        for (int i = 0; i < CHALLENGES_PER_WEEK; i++) {
            active[i] = CHALLENGE_POOL[activeIndices[i]];
        }
        return active;
    }

    public static int[] getPlayerProgress(UUID playerId, ServerLevel level) {
        ensureLoaded(level);
        checkWeekRotation();
        return getProgress(playerId);
    }

    public static int getRewardXp() { return REWARD_XP; }
    public static int getRewardCoins() { return REWARD_COINS; }

    public static long getMillisUntilNextWeek() {
        long weekMs = 7L * 24 * 60 * 60 * 1000;
        long currentMs = System.currentTimeMillis();
        long nextWeekStart = (currentMs / weekMs + 1) * weekMs;
        return nextWeekStart - currentMs;
    }

    // === Commands ===

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("megamod").then(
                Commands.literal("challenges").executes(context -> {
                    ServerPlayer player = ((CommandSourceStack) context.getSource()).getPlayerOrException();
                    ServerLevel level = player.level();
                    ensureLoaded(level);
                    checkWeekRotation();
                    int[] progress = getProgress(player.getUUID());
                    player.sendSystemMessage(Component.literal("\u00a76=== Weekly Challenges ===").withStyle(ChatFormatting.GOLD));
                    for (int i = 0; i < CHALLENGES_PER_WEEK; i++) {
                        Challenge c = CHALLENGE_POOL[activeIndices[i]];
                        boolean done = progress[i] >= c.target();
                        String status = done ? "\u00a7a\u2714 " : "\u00a7e> ";
                        String prog = progress[i] + "/" + c.target();
                        ChatFormatting color = done ? ChatFormatting.GREEN : ChatFormatting.YELLOW;
                        player.sendSystemMessage(Component.literal(status + c.name() + " [" + prog + "] \u00a77(+" + REWARD_XP + " " + c.tree().getDisplayName() + " XP, +" + REWARD_COINS + " MC)").withStyle(color));
                    }
                    return 1;
                })
            )
        );
    }

    // === Persistence ===

    private static void loadFromDisk(ServerLevel level) {
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (dataFile.exists()) {
                CompoundTag root = NbtIo.readCompressed((Path) dataFile.toPath(), (NbtAccounter) NbtAccounter.unlimitedHeap());
                currentWeek = root.getLongOr("week", -1);
                for (int i = 0; i < CHALLENGES_PER_WEEK; i++) {
                    activeIndices[i] = root.getIntOr("idx" + i, 0);
                    if (activeIndices[i] >= CHALLENGE_POOL.length) activeIndices[i] = 0;
                }
                CompoundTag players = root.getCompoundOrEmpty("players");
                for (String key : players.keySet()) {
                    UUID uuid = UUID.fromString(key);
                    CompoundTag pTag = players.getCompoundOrEmpty(key);
                    int[] progress = new int[CHALLENGES_PER_WEEK];
                    for (int i = 0; i < CHALLENGES_PER_WEEK; i++) {
                        progress[i] = pTag.getIntOr("p" + i, 0);
                    }
                    playerProgress.put(uuid, progress);
                }
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load skill challenge data", e);
        }
    }

    private static void saveToDisk(ServerLevel level) {
        if (!dirty) return;
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataDir = new File(saveDir, "data");
            if (!dataDir.exists()) dataDir.mkdirs();
            File dataFile = new File(dataDir, FILE_NAME);
            CompoundTag root = new CompoundTag();
            root.putLong("week", currentWeek);
            for (int i = 0; i < CHALLENGES_PER_WEEK; i++) {
                root.putInt("idx" + i, activeIndices[i]);
            }
            CompoundTag players = new CompoundTag();
            for (Map.Entry<UUID, int[]> entry : playerProgress.entrySet()) {
                CompoundTag pTag = new CompoundTag();
                int[] progress = entry.getValue();
                for (int i = 0; i < CHALLENGES_PER_WEEK; i++) {
                    pTag.putInt("p" + i, progress[i]);
                }
                players.put(entry.getKey().toString(), (Tag) pTag);
            }
            root.put("players", (Tag) players);
            NbtIo.writeCompressed((CompoundTag) root, (Path) dataFile.toPath());
            dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save skill challenge data", e);
        }
    }
}
