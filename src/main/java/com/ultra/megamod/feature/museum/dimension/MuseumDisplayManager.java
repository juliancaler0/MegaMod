package com.ultra.megamod.feature.museum.dimension;

import net.minecraft.world.level.block.Block;
import com.ultra.megamod.feature.museum.MuseumData;
import com.ultra.megamod.feature.museum.MuseumRegistry;
import com.ultra.megamod.feature.museum.catalog.AchievementCatalog;
import com.ultra.megamod.feature.museum.catalog.AquariumCatalog;
import com.ultra.megamod.feature.museum.catalog.ArtCatalog;
import com.ultra.megamod.feature.museum.catalog.ItemCatalog;
import com.ultra.megamod.feature.museum.catalog.WildlifeCatalog;
import com.ultra.megamod.feature.museum.paintings.MasterpieceRegistry;
import com.ultra.megamod.feature.furniture.FurnitureRegistry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import com.ultra.megamod.feature.economy.EconomyManager;

public class MuseumDisplayManager {
    private static final int HALL_WIDTH = 15;
    private static final int HALL_HEIGHT = 8;
    private static final int MIN_HALL_LENGTH = 7;
    private static final int MAIN_WIDTH = 21;
    private static final int MAIN_DEPTH = 21;

    private static final int AQUARIUM_PAIR_LENGTH = 7;
    private static final int AQUARIUM_LARGE_LENGTH = 9;
    private static final int WILDLIFE_PAIR_LENGTH = 7;
    private static final int WILDLIFE_LARGE_LENGTH = 9;
    private static final int ART_PAIR_LENGTH = 6;
    private static final int ITEMS_PAIR_LENGTH = 3;
    private static final int MAX_DISPLAYED_ITEMS = 100;
    private static final int MAX_EMPTY_PEDESTALS = 20;

    private static final String MUSEUM_TAG = "museum_exhibit";

    // Guard against concurrent rebuilds of the same museum pocket
    private static final Set<BlockPos> rebuildingOrigins = java.util.Collections.synchronizedSet(new HashSet<>());

    private MuseumDisplayManager() {
    }

    // ─── Main Entry Point ───

    public static void rebuildWings(ServerLevel level, BlockPos origin, UUID playerId, ServerPlayer player) {
        // Prevent concurrent rebuilds for the same museum pocket
        if (!rebuildingOrigins.add(origin)) {
            // Another rebuild is in progress for this museum — skip
            if (player != null) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Museum is updating, please wait...").withStyle(net.minecraft.ChatFormatting.YELLOW));
            }
            return;
        }
        try {
            rebuildWingsInternal(level, origin, playerId, player);
        } finally {
            rebuildingOrigins.remove(origin);
        }
    }

    private static void rebuildWingsInternal(ServerLevel level, BlockPos origin, UUID playerId, ServerPlayer player) {
        // Force-load museum chunks before clearing entities to prevent mob duplication
        // (entities in unloaded chunks survive the clear and duplicate on rebuild)
        forceLoadMuseumChunks(level, origin);

        MuseumData data = MuseumData.get(level);
        clearAllMuseumEntities(level, origin);

        Set<String> items = data.getDonatedItems(playerId);
        Set<String> mobs = data.getDonatedMobs(playerId);
        Set<String> art = data.getDonatedArt(playerId);
        Set<String> achievements = data.getCompletedAchievements(playerId);

        int centerX = origin.getX() + 10;
        int floorY = origin.getY();
        int centerZ = origin.getZ() + 10;

        Set<String> aquatic = filterAquatic(mobs);
        Set<String> wildlife = filterWildlife(mobs);

        // Sort mobs: normal first, large at end (small exhibits near entrance, big at back)
        List<String> sortedAquatic = sortBySize(new ArrayList<>(aquatic));
        List<String> sortedWildlife = sortBySize(new ArrayList<>(wildlife));

        List<String> itemList = new ArrayList<>();
        for (String id : items) {
            if (!isNonItemDonation(id)) {
                itemList.add(id);
            }
        }

        // Main hall decorations
        decorateMainHall(level, origin);

        // Filter art and achievements against their catalogs to prevent over-counting
        long validArt = art.stream().filter(ArtCatalog::contains).count();
        long validAchievements = achievements.stream().filter(AchievementCatalog::contains).count();

        // Central trophy (floating label only)
        int totalDonated = itemList.size() + aquatic.size() + wildlife.size() + (int) validArt + (int) validAchievements;
        int totalCatalog = ItemCatalog.getTotalItemCount() + AquariumCatalog.getTotalCount()
                         + WildlifeCatalog.getTotalCount() + ArtCatalog.getTotalCount()
                         + AchievementCatalog.getTotalCount();
        int overallPercent = totalCatalog > 0 ? Math.min(100, totalDonated * 100 / totalCatalog) : 0;
        placeCentralTrophy(level, origin, overallPercent);

        // Track completed wings for effects
        int completedWings = 0;

        // Items Wing (South) — items only, no achievements
        BlockPos southWall = new BlockPos(centerX - 1, floorY, origin.getZ() + MAIN_DEPTH - 1);
        buildAndPopulateItemsWing(level, southWall, Direction.SOUTH, itemList);
        placeEntranceLabel(level, southWall, Direction.SOUTH, "Collection", itemList.size(), ItemCatalog.getTotalItemCount());
        placeCorridorArch(level, southWall, Direction.SOUTH);
        if (player != null) {
            double sx = southWall.getX() + 1.5;
            double sy = southWall.getY() + 2.0;
            double sz = southWall.getZ() + 0.5;
            level.sendParticles((ParticleOptions) ParticleTypes.END_ROD, sx, sy, sz, 15, 1.0, 0.8, 0.3, 0.02);
            level.playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.AMBIENT, 0.3f, 0.8f);
        }
        if (itemList.size() >= ItemCatalog.getTotalItemCount()) completedWings++;

        // Art Wing (North)
        BlockPos northWall = new BlockPos(centerX - 1, floorY, origin.getZ());
        buildAndPopulateArtWing(level, northWall, Direction.NORTH, new ArrayList<>(art));
        placeEntranceLabel(level, northWall, Direction.NORTH, "Art Gallery", art.size(), ArtCatalog.getTotalCount());
        placeCorridorArch(level, northWall, Direction.NORTH);
        if (player != null) {
            double nx = northWall.getX() + 1.5;
            double ny = northWall.getY() + 2.5;
            double nz = northWall.getZ() + 0.5;
            level.sendParticles((ParticleOptions) ParticleTypes.ENCHANT, nx, ny, nz, 15, 1.0, 0.5, 0.3, 0.5);
            level.playSound(null, player.blockPosition(), (SoundEvent) SoundEvents.NOTE_BLOCK_HARP.value(), SoundSource.AMBIENT, 0.3f, 1.0f);
        }
        if (art.size() >= ArtCatalog.getTotalCount()) completedWings++;

        // Aquarium Wing (East)
        BlockPos eastWall = new BlockPos(origin.getX() + MAIN_DEPTH - 1, floorY, centerZ - 1);
        buildAndPopulateAquariumWing(level, eastWall, Direction.EAST, sortedAquatic);
        placeEntranceLabel(level, eastWall, Direction.EAST, "Aquarium", aquatic.size(), AquariumCatalog.getTotalCount());
        placeCorridorArch(level, eastWall, Direction.EAST);
        if (player != null) {
            double ax = eastWall.getX() + 0.5;
            double ay = eastWall.getY() + 2.0;
            double az = eastWall.getZ() + 1.5;
            level.sendParticles((ParticleOptions) ParticleTypes.BUBBLE_POP, ax, ay, az, 20, 0.3, 0.8, 1.0, 0.02);
            level.sendParticles((ParticleOptions) ParticleTypes.FALLING_WATER, ax, ay + 1.0, az, 10, 0.3, 0.3, 1.0, 0.0);
            level.playSound(null, player.blockPosition(), SoundEvents.WATER_AMBIENT, SoundSource.AMBIENT, 0.2f, 1.0f);
        }
        if (aquatic.size() >= AquariumCatalog.getTotalCount()) completedWings++;

        // Wildlife Wing (West)
        BlockPos westWall = new BlockPos(origin.getX(), floorY, centerZ - 1);
        buildAndPopulateWildlifeWing(level, westWall, Direction.WEST, sortedWildlife);
        placeEntranceLabel(level, westWall, Direction.WEST, "Wildlife", wildlife.size(), WildlifeCatalog.getTotalCount());
        placeCorridorArch(level, westWall, Direction.WEST);
        if (player != null) {
            double wx = westWall.getX() + 0.5;
            double wy = westWall.getY() + 2.0;
            double wz = westWall.getZ() + 1.5;
            level.sendParticles((ParticleOptions) ParticleTypes.COMPOSTER, wx, wy, wz, 15, 0.3, 0.5, 1.0, 0.02);
            level.playSound(null, player.blockPosition(), SoundEvents.FOX_AMBIENT, SoundSource.AMBIENT, 0.2f, 1.2f);
        }
        if (wildlife.size() >= WildlifeCatalog.getTotalCount()) completedWings++;

        // Achievement tracking — auto-grant museum wing completion achievements
        boolean itemsComplete = itemList.size() >= ItemCatalog.getTotalItemCount();
        boolean artComplete = art.size() >= ArtCatalog.getTotalCount();
        boolean aquariumComplete = aquatic.size() >= AquariumCatalog.getTotalCount();
        boolean wildlifeComplete = wildlife.size() >= WildlifeCatalog.getTotalCount();

        if (itemsComplete) data.recordAchievement(playerId, "megamod:museum/master_collector");
        if (artComplete) data.recordAchievement(playerId, "megamod:museum/art_connoisseur");
        if (aquariumComplete) data.recordAchievement(playerId, "megamod:museum/ocean_explorer");
        if (wildlifeComplete) data.recordAchievement(playerId, "megamod:museum/beast_master");

        // Refresh achievements set after potential new grants
        achievements = data.getCompletedAchievements(playerId);
        boolean achievementsComplete = achievements.size() >= AchievementCatalog.getTotalCount();
        if (achievementsComplete) completedWings++;

        // The "Completionist" achievement requires ALL other 4 museum achievements
        if (itemsComplete && artComplete && aquariumComplete && wildlifeComplete) {
            data.recordAchievement(playerId, "megamod:museum/completionist");
            achievements = data.getCompletedAchievements(playerId);
            completedWings++;
        }

        // Basement staircase and achievement hallway
        buildBasementStaircase(level, origin);
        buildAndPopulateAchievementBasement(level, origin, new ArrayList<>(achievements));
        if (player != null) {
            // Soul flame particles near the basement staircase entrance
            double bx = origin.getX() + 10.5;
            double by = origin.getY() + 1.0;
            double bz = origin.getZ() + 18.5;
            level.sendParticles((ParticleOptions) ParticleTypes.SOUL_FIRE_FLAME, bx, by, bz, 12, 0.5, 0.3, 0.5, 0.01);
            level.playSound(null, player.blockPosition(), SoundEvents.SOUL_SAND_BREAK, SoundSource.AMBIENT, 0.15f, 0.8f);
        }

        // Welcome particles at museum entrance
        if (player != null) {
            double ex = origin.getX() + 10.5;
            double ey = origin.getY() + 2.0;
            double ez = origin.getZ() + 4.5;
            level.sendParticles((ParticleOptions) ParticleTypes.END_ROD, ex, ey, ez, 20, 1.5, 1.0, 1.5, 0.02);
        }

        // Completion effects
        if (player != null) {
            playCompletionEffects(level, origin, player, completedWings, overallPercent);
        }

        // Wing ambiance sounds (play deeper into each wing)
        if (player != null) {
            playWingAmbiance(level, origin, player);
        }

        // Milestone completion rewards
        if (player != null) {
            int aqPct = AquariumCatalog.getTotalCount() > 0 ? aquatic.size() * 100 / AquariumCatalog.getTotalCount() : 0;
            int wlPct = WildlifeCatalog.getTotalCount() > 0 ? wildlife.size() * 100 / WildlifeCatalog.getTotalCount() : 0;
            int artPct = ArtCatalog.getTotalCount() > 0 ? (int)validArt * 100 / ArtCatalog.getTotalCount() : 0;
            int itemPct = ItemCatalog.getTotalItemCount() > 0 ? itemList.size() * 100 / ItemCatalog.getTotalItemCount() : 0;
            checkAndGrantRewards(level, player, playerId, data, aqPct, wlPct, artPct, itemPct, overallPercent);
        }
    }

    // ─── Chunk Loading (prevents mob duplication) ───

    private static void forceLoadMuseumChunks(ServerLevel level, BlockPos origin) {
        // Load all chunks in a radius that covers the main hall + all possible wing extensions.
        // In a void dimension, empty chunks are cheap to load.
        int centerX = origin.getX() + 10;
        int centerZ = origin.getZ() + 10;
        int blockRadius = 100;
        for (int bx = centerX - blockRadius; bx <= centerX + blockRadius; bx += 16) {
            for (int bz = centerZ - blockRadius; bz <= centerZ + blockRadius; bz += 16) {
                level.getChunk(new BlockPos(bx, origin.getY(), bz));
            }
        }
    }

    // ─── Completion Sound & Particle Effects ───

    private static void playCompletionEffects(ServerLevel level, BlockPos origin, ServerPlayer player,
                                               int completedWings, int overallPercent) {
        double cx = origin.getX() + 10.5;
        double cy = origin.getY() + 2.0;
        double cz = origin.getZ() + 10.5;

        if (completedWings > 0) {
            // Wing completion: totem-style particles + level-up chime
            level.sendParticles((ParticleOptions) ParticleTypes.TOTEM_OF_UNDYING,
                cx, cy, cz, 30 * completedWings, 1.5, 2.0, 1.5, 0.3);
            level.sendParticles((ParticleOptions) ParticleTypes.END_ROD,
                cx, cy + 1.0, cz, 15 * completedWings, 2.0, 1.5, 2.0, 0.05);
            level.playSound(null, player.blockPosition(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.2f);

            // Chat message per completed wing count
            if (completedWings == 1) {
                player.sendSystemMessage(Component.literal("A wing of your museum is complete!").withStyle(net.minecraft.ChatFormatting.GOLD));
            } else {
                player.sendSystemMessage(Component.literal(completedWings + " wings of your museum are complete!").withStyle(net.minecraft.ChatFormatting.GOLD));
            }
        }

        if (overallPercent >= 100) {
            // Full museum completion: massive firework burst + special sounds
            level.sendParticles((ParticleOptions) ParticleTypes.TOTEM_OF_UNDYING,
                cx, cy, cz, 200, 3.0, 3.0, 3.0, 0.6);
            level.sendParticles((ParticleOptions) ParticleTypes.ENCHANT,
                cx, cy + 2.0, cz, 100, 2.0, 2.0, 2.0, 1.0);
            level.sendParticles((ParticleOptions) ParticleTypes.END_ROD,
                cx, cy + 1.0, cz, 80, 3.0, 3.0, 3.0, 0.1);
            level.playSound(null, player.blockPosition(),
                SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.5f, 1.0f);
            level.playSound(null, player.blockPosition(),
                SoundEvents.FIREWORK_ROCKET_TWINKLE, SoundSource.PLAYERS, 1.0f, 1.0f);
            player.sendSystemMessage(Component.literal("Your Museum Collection is COMPLETE!").withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE, net.minecraft.ChatFormatting.BOLD));
        } else if (overallPercent >= 75) {
            // 75% milestone
            level.sendParticles((ParticleOptions) ParticleTypes.ENCHANT,
                cx, cy + 1.0, cz, 40, 1.5, 1.5, 1.5, 0.5);
            level.playSound(null, player.blockPosition(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8f, 1.5f);
        } else if (overallPercent >= 50) {
            // 50% milestone
            level.sendParticles((ParticleOptions) ParticleTypes.ENCHANT,
                cx, cy + 1.0, cz, 20, 1.0, 1.0, 1.0, 0.3);
        }
    }

    // ─── Wing Ambiance ───

    private static void playWingAmbiance(ServerLevel level, BlockPos origin, ServerPlayer player) {
        int ox = origin.getX();
        int oy = origin.getY();
        int oz = origin.getZ();

        // Aquarium wing (East) — bubble and water sounds deeper in
        level.playSound(null, new BlockPos(ox + 25, oy + 2, oz + 10),
            SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_AMBIENT, SoundSource.AMBIENT, 0.15f, 1.0f);
        level.playSound(null, new BlockPos(ox + 30, oy + 2, oz + 10),
            SoundEvents.WATER_AMBIENT, SoundSource.AMBIENT, 0.12f, 0.8f);
        level.playSound(null, new BlockPos(ox + 35, oy + 2, oz + 10),
            SoundEvents.DROWNED_AMBIENT, SoundSource.AMBIENT, 0.08f, 1.2f);

        // Wildlife wing (West) — animal sounds deeper in
        level.playSound(null, new BlockPos(ox - 5, oy + 2, oz + 10),
            SoundEvents.FOX_AMBIENT, SoundSource.AMBIENT, 0.1f, 1.0f);
        level.playSound(null, new BlockPos(ox - 10, oy + 2, oz + 10),
            SoundEvents.PARROT_AMBIENT, SoundSource.AMBIENT, 0.12f, 1.1f);
        level.playSound(null, new BlockPos(ox - 15, oy + 2, oz + 10),
            SoundEvents.CAT_PURREOW, SoundSource.AMBIENT, 0.08f, 1.0f);

        // Art wing (North) — soft music ambiance
        level.playSound(null, new BlockPos(ox + 10, oy + 2, oz - 5),
            (SoundEvent) SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.AMBIENT, 0.1f, 1.2f);
        level.playSound(null, new BlockPos(ox + 10, oy + 2, oz - 10),
            (SoundEvent) SoundEvents.NOTE_BLOCK_HARP.value(), SoundSource.AMBIENT, 0.08f, 0.8f);

        // Items wing (South) — enchanting table sounds
        level.playSound(null, new BlockPos(ox + 10, oy + 2, oz + 25),
            SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.AMBIENT, 0.08f, 0.6f);

        // Basement (Achievements) — eerie soul sounds
        level.playSound(null, new BlockPos(ox + 17, oy - 8, oz + 18),
            SoundEvents.SOUL_SAND_BREAK, SoundSource.AMBIENT, 0.1f, 0.7f);
    }

    // ─── Milestone Rewards ───

    private static void checkAndGrantRewards(ServerLevel level, ServerPlayer player, UUID playerId,
                                              MuseumData data, int aqPct, int wlPct, int artPct, int itemPct, int overallPct) {
        EconomyManager economy = EconomyManager.get(level);

        // Per-wing milestones: [wing_name]_[threshold]
        checkWingMilestone(level, player, playerId, data, economy, "aquarium", aqPct);
        checkWingMilestone(level, player, playerId, data, economy, "wildlife", wlPct);
        checkWingMilestone(level, player, playerId, data, economy, "art", artPct);
        checkWingMilestone(level, player, playerId, data, economy, "items", itemPct);

        // Overall milestones
        if (overallPct >= 25 && data.claimReward(playerId, "overall_25")) {
            economy.addWallet(playerId, 100);
            player.sendSystemMessage(Component.literal("Museum Milestone: 25% Complete! +100 MegaCoins").withStyle(net.minecraft.ChatFormatting.GOLD));
        }
        if (overallPct >= 50 && data.claimReward(playerId, "overall_50")) {
            economy.addWallet(playerId, 300);
            player.sendSystemMessage(Component.literal("Museum Milestone: 50% Complete! +300 MegaCoins").withStyle(net.minecraft.ChatFormatting.GOLD));
        }
        if (overallPct >= 75 && data.claimReward(playerId, "overall_75")) {
            economy.addWallet(playerId, 750);
            ItemEntity reward = new ItemEntity(level, player.getX(), player.getY() + 0.5, player.getZ(),
                new ItemStack((ItemLike) Items.ENCHANTED_GOLDEN_APPLE));
            reward.setNoPickUpDelay();
            level.addFreshEntity(reward);
            player.sendSystemMessage(Component.literal("Museum Milestone: 75% Complete! +750 MegaCoins + Enchanted Golden Apple!").withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE));
        }
        if (overallPct >= 100 && data.claimReward(playerId, "overall_100")) {
            economy.addWallet(playerId, 2000);
            ItemEntity reward = new ItemEntity(level, player.getX(), player.getY() + 0.5, player.getZ(),
                new ItemStack((ItemLike) Items.NETHER_STAR));
            reward.setNoPickUpDelay();
            level.addFreshEntity(reward);
            player.sendSystemMessage(Component.literal("MUSEUM COMPLETE! +2000 MegaCoins + Nether Star!").withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE, net.minecraft.ChatFormatting.BOLD));
        }
    }

    private static void checkWingMilestone(ServerLevel level, ServerPlayer player, UUID playerId,
                                            MuseumData data, EconomyManager economy, String wing, int pct) {
        if (pct >= 25 && data.claimReward(playerId, wing + "_25")) {
            economy.addWallet(playerId, 50);
            player.sendSystemMessage(Component.literal(capitalize(wing) + " Wing 25%! +50 MegaCoins").withStyle(net.minecraft.ChatFormatting.GREEN));
        }
        if (pct >= 50 && data.claimReward(playerId, wing + "_50")) {
            economy.addWallet(playerId, 200);
            player.sendSystemMessage(Component.literal(capitalize(wing) + " Wing 50%! +200 MegaCoins").withStyle(net.minecraft.ChatFormatting.GREEN));
        }
        if (pct >= 75 && data.claimReward(playerId, wing + "_75")) {
            economy.addWallet(playerId, 500);
            player.sendSystemMessage(Component.literal(capitalize(wing) + " Wing 75%! +500 MegaCoins").withStyle(net.minecraft.ChatFormatting.GOLD));
        }
        if (pct >= 100 && data.claimReward(playerId, wing + "_100")) {
            economy.addWallet(playerId, 1000);
            player.sendSystemMessage(Component.literal(capitalize(wing) + " Wing COMPLETE! +1000 MegaCoins!").withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE));
            // Play extra celebration sound
            level.playSound(null, player.blockPosition(),
                SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0f, 1.2f);
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    // ─── Main Hall Decoration ───

    private static void decorateMainHall(ServerLevel level, BlockPos origin) {
        int ox = origin.getX();
        int oy = origin.getY();
        int oz = origin.getZ();
        BlockState carpet = Blocks.RED_CARPET.defaultBlockState();

        // Carpet cross (3 wide) — connects wing corridor entrances
        for (int z = 0; z <= 20; z++) {
            for (int x = 9; x <= 11; x++) {
                BlockPos pos = new BlockPos(ox + x, oy + 1, oz + z);
                if (level.getBlockState(pos).isAir()) level.setBlock(pos, carpet, 3);
            }
        }
        for (int x = 0; x <= 20; x++) {
            for (int z = 9; z <= 11; z++) {
                BlockPos pos = new BlockPos(ox + x, oy + 1, oz + z);
                if (level.getBlockState(pos).isAir()) level.setBlock(pos, carpet, 3);
            }
        }

        // Quartz pillars at 4 symmetrical positions (skip SE near staircase)
        BlockState pillar = Blocks.QUARTZ_PILLAR.defaultBlockState();
        int[][] pillarPositions = {{5, 5}, {5, 15}, {15, 5}};
        for (int[] pp : pillarPositions) {
            for (int h = 1; h <= 8; h++) {
                level.setBlock(new BlockPos(ox + pp[0], oy + h, oz + pp[1]), pillar, 3);
            }
        }

        // Railing around the staircase opening — leave x=16-18 open for entry
        BlockState railing = Blocks.STONE_BRICK_WALL.defaultBlockState();
        level.setBlock(new BlockPos(ox + 15, oy + 1, oz + 14), railing, 3);
        level.setBlock(new BlockPos(ox + 19, oy + 1, oz + 14), railing, 3);
        for (int z = 15; z <= 16; z++) {
            level.setBlock(new BlockPos(ox + 15, oy + 1, oz + z), railing, 3);
            level.setBlock(new BlockPos(ox + 19, oy + 1, oz + z), railing, 3);
        }

        // === LIGHTING ===
        // Hanging lamps from ceiling — 4 chandeliers in each quadrant
        BlockState hangingLamp = FurnitureRegistry.MED_LAMP_HANGING.get().defaultBlockState();
        level.setBlock(new BlockPos(ox + 5, oy + 8, oz + 5), hangingLamp, 3);
        level.setBlock(new BlockPos(ox + 15, oy + 8, oz + 5), hangingLamp, 3);
        level.setBlock(new BlockPos(ox + 5, oy + 8, oz + 15), hangingLamp, 3);
        level.setBlock(new BlockPos(ox + 15, oy + 8, oz + 15), hangingLamp, 3);
        // Wall sconces along north and south walls
        placeFurniture(level, ox + 4, oy + 4, oz + 1, FurnitureRegistry.CLASSIC_WALL_LAMP.get(), Direction.SOUTH);
        placeFurniture(level, ox + 16, oy + 4, oz + 1, FurnitureRegistry.CLASSIC_WALL_LAMP.get(), Direction.SOUTH);
        placeFurniture(level, ox + 4, oy + 4, oz + 19, FurnitureRegistry.CLASSIC_WALL_LAMP.get(), Direction.NORTH);
        placeFurniture(level, ox + 16, oy + 4, oz + 19, FurnitureRegistry.CLASSIC_WALL_LAMP.get(), Direction.NORTH);

        // === CORNER PLANTS — one in each accessible corner ===
        placeFurniture(level, ox + 1, oy + 1, oz + 1, FurnitureRegistry.OFFICE_POTTED_PLANT.get(), Direction.SOUTH);
        placeFurniture(level, ox + 19, oy + 1, oz + 1, FurnitureRegistry.OFFICE_POTTED_PLANT.get(), Direction.SOUTH);
        placeFurniture(level, ox + 1, oy + 1, oz + 19, FurnitureRegistry.OFFICE_POTTED_PLANT.get(), Direction.NORTH);

        // === NW QUADRANT — Reading nook (facing south toward carpet) ===
        // Sofa against west wall facing EAST, with end table + lamp beside it
        placeFurniture(level, ox + 2, oy + 1, oz + 6, FurnitureRegistry.VINTAGE_LEATHER_SOFA.get(), Direction.EAST);
        placeFurniture(level, ox + 2, oy + 1, oz + 4, FurnitureRegistry.MED_END_TABLE.get(), Direction.EAST);
        placeFurniture(level, ox + 2, oy + 2, oz + 4, FurnitureRegistry.CLASSIC_TABLE_LAMP.get(), Direction.EAST);
        // Coffee table in front of sofa (1 block east, clear of clipping)
        placeFurniture(level, ox + 4, oy + 1, oz + 6, FurnitureRegistry.MED_COFFEE_TABLE.get(), Direction.EAST);
        // Book stacks on the coffee table
        placeFurniture(level, ox + 4, oy + 2, oz + 6, FurnitureRegistry.MED_BOOK_STACK_HORIZONTAL_1.get(), Direction.EAST);

        // === NE QUADRANT — Display corner ===
        // Classic showcase with jar on top
        placeFurniture(level, ox + 17, oy + 1, oz + 3, FurnitureRegistry.CLASSIC_SHOWCASE_CORNER.get(), Direction.SOUTH);
        placeFurniture(level, ox + 17, oy + 1, oz + 5, FurnitureRegistry.CLASSIC_JAR.get(), Direction.SOUTH);
        // Vintage clock on east wall
        placeFurniture(level, ox + 19, oy + 4, oz + 4, FurnitureRegistry.VINTAGE_CLOCK.get(), Direction.WEST);
        // Candle dish accent
        placeFurniture(level, ox + 18, oy + 1, oz + 7, FurnitureRegistry.MED_CANDLE_DISH.get(), Direction.WEST);

        // === SW QUADRANT — Guest seating ===
        // Chair facing north toward the carpet crossing
        placeFurniture(level, ox + 3, oy + 1, oz + 14, FurnitureRegistry.VINTAGE_CHAIR.get(), Direction.NORTH);
        placeFurniture(level, ox + 3, oy + 1, oz + 13, FurnitureRegistry.MED_END_TABLE.get(), Direction.NORTH);
        placeFurniture(level, ox + 3, oy + 2, oz + 13, FurnitureRegistry.MED_CANDLE_DISH.get(), Direction.NORTH);
        // Decorative pot beside the chair
        placeFurniture(level, ox + 2, oy + 1, oz + 16, FurnitureRegistry.MED_POT_1.get(), Direction.EAST);
        // Painting on south wall
        placeFurniture(level, ox + 6, oy + 4, oz + 19, FurnitureRegistry.VINTAGE_PAINTING.get(), Direction.NORTH);

        // === WING ENTRANCE ACCENTS ===
        // North wing (Art) — classic candles flanking entrance
        placeFurniture(level, ox + 8, oy + 1, oz + 1, FurnitureRegistry.CLASSIC_CANDLE.get(), Direction.SOUTH);
        placeFurniture(level, ox + 12, oy + 1, oz + 1, FurnitureRegistry.CLASSIC_CANDLE.get(), Direction.SOUTH);
        // South wing (Items) — showcase corners flanking entrance
        placeFurniture(level, ox + 8, oy + 1, oz + 19, FurnitureRegistry.MED_POT_2.get(), Direction.NORTH);
        placeFurniture(level, ox + 12, oy + 1, oz + 19, FurnitureRegistry.MED_POT_3.get(), Direction.NORTH);
        // East wing (Aquarium) — Caribbean pots flanking entrance
        placeFurniture(level, ox + 19, oy + 1, oz + 8, FurnitureRegistry.CLASSIC_FLOWER.get(), Direction.WEST);
        placeFurniture(level, ox + 19, oy + 1, oz + 12, FurnitureRegistry.CLASSIC_FLOWER.get(), Direction.WEST);
        // West wing (Wildlife) — natural pots flanking entrance
        placeFurniture(level, ox + 1, oy + 1, oz + 8, FurnitureRegistry.CLASSIC_FLOWER.get(), Direction.EAST);
        placeFurniture(level, ox + 1, oy + 1, oz + 12, FurnitureRegistry.CLASSIC_FLOWER.get(), Direction.EAST);
    }

    /** Place a directional furniture block at the given world position. */
    private static void placeFurniture(ServerLevel level, int x, int y, int z, Block block, Direction facing) {
        BlockState state = block.defaultBlockState();
        if (state.hasProperty(HorizontalDirectionalBlock.FACING)) {
            state = state.setValue(HorizontalDirectionalBlock.FACING, facing);
        }
        level.setBlock(new BlockPos(x, y, z), state, 3);
    }

    // ─── Central Trophy ───

    private static void placeCentralTrophy(ServerLevel level, BlockPos origin, int overallPercent) {
        // Trophy item + label floating above museum block at origin.offset(10, 1, 10)
        BlockPos museumBlockPos = origin.offset(10, 0, 10);
        ItemStack trophyItem;
        if (overallPercent >= 75) {
            trophyItem = new ItemStack((ItemLike) Items.NETHERITE_BLOCK);
        } else if (overallPercent >= 50) {
            trophyItem = new ItemStack((ItemLike) Items.DIAMOND_BLOCK);
        } else if (overallPercent >= 25) {
            trophyItem = new ItemStack((ItemLike) Items.GOLD_BLOCK);
        } else {
            trophyItem = new ItemStack((ItemLike) Items.IRON_BLOCK);
        }
        // Floating item above the museum block
        placeItemOnPedestal(level, museumBlockPos.above(1), trophyItem);
        // Label above the item
        placeNameLabel(level, origin.offset(10, 5, 10), "Collection: " + overallPercent + "%");
    }

    // ─── Entrance Labels with Progress ───

    private static void placeEntranceLabel(ServerLevel level, BlockPos wallPos, Direction outward, String wingName, int donated, int total) {
        int dx = outward.getStepX();
        int dz = outward.getStepZ();
        int px = (dx != 0) ? 0 : 1;
        int pz = (dx != 0) ? 1 : 0;
        BlockPos labelPos = wallPos.offset(-dx + 1 * px, 5, -dz + 1 * pz);
        placeNameLabel(level, labelPos, wingName + " (" + donated + "/" + total + ")");

        // Decorative lanterns flanking the entrance
        BlockState lantern = Blocks.LANTERN.defaultBlockState();
        level.setBlock(wallPos.offset(-2 * px + dx, 3, -2 * pz + dz), lantern, 3);
        level.setBlock(wallPos.offset(4 * px + dx, 3, 4 * pz + dz), lantern, 3);
    }

    // ─── Corridor Entrance Arches ───

    private static void placeCorridorArch(ServerLevel level, BlockPos wallPos, Direction outward) {
        int dx = outward.getStepX();
        int dz = outward.getStepZ();
        int px = (dx != 0) ? 0 : 1;
        int pz = (dx != 0) ? 1 : 0;
        BlockState chiseled = Blocks.CHISELED_STONE_BRICKS.defaultBlockState();

        // Left pillar (one block left of corridor opening)
        for (int h = 1; h <= 4; h++) {
            level.setBlock(wallPos.offset(-1 * px, h, -1 * pz), chiseled, 3);
        }
        // Right pillar (one block right of corridor opening)
        for (int h = 1; h <= 4; h++) {
            level.setBlock(wallPos.offset(3 * px, h, 3 * pz), chiseled, 3);
        }
        // Lintel across the top
        for (int w = -1; w <= 3; w++) {
            level.setBlock(wallPos.offset(w * px, 4, w * pz), chiseled, 3);
        }
        // Decorative accent row above lintel
        BlockState accent = Blocks.CHISELED_DEEPSLATE.defaultBlockState();
        for (int w = 0; w <= 2; w++) {
            level.setBlock(wallPos.offset(w * px, 5, w * pz), accent, 3);
        }
    }

    // ─── Entity Clearing ───

    private static void clearAllMuseumEntities(ServerLevel level, BlockPos origin) {
        // Force-load museum chunks and KEEP them loaded (void dimension, negligible cost).
        // Previously we unforced immediately after discard(), but discard() is deferred —
        // the entity stays alive in memory until the next tick, so unforcing saves it
        // back to disk, causing duplicates on next visit.
        int chunkRadius = 20; // 320 blocks in each direction
        int centerCX = origin.getX() >> 4;
        int centerCZ = origin.getZ() >> 4;
        for (int cx = centerCX - chunkRadius; cx <= centerCX + chunkRadius; cx++) {
            for (int cz = centerCZ - chunkRadius; cz <= centerCZ + chunkRadius; cz++) {
                level.setChunkForced(cx, cz, true);
            }
        }
        // Kill all museum entities — use remove(KILLED) for immediate processing
        // rather than discard() which defers removal to next tick
        AABB searchArea = new AABB(
            origin.getX() - 350, origin.getY() - 50, origin.getZ() - 350,
            origin.getX() + 350, origin.getY() + 80, origin.getZ() + 350
        );
        List<Entity> toRemove = level.getEntities((Entity) null, searchArea, entity ->
            entity.getTags().contains(MUSEUM_TAG)
            || (entity instanceof ArmorStand && entity.isInvisible() && entity.isCustomNameVisible())
            || (entity instanceof ItemEntity ie && ie.isInvulnerable() && ie.isNoGravity())
        );
        for (Entity entity : toRemove) {
            entity.remove(Entity.RemovalReason.KILLED);
        }
        // Safety net: catch anything outside the AABB
        List<Entity> stragglers = new ArrayList<>();
        level.getAllEntities().forEach(entity -> {
            if (!entity.isRemoved() && entity.getTags().contains(MUSEUM_TAG)) {
                stragglers.add(entity);
            }
        });
        for (Entity entity : stragglers) {
            entity.remove(Entity.RemovalReason.KILLED);
        }
    }

    // ─── Hall Building ───

    private static BlockPos buildHall(ServerLevel level, BlockPos wallPos, Direction outward, int hallLength,
                                       BlockState floorBlock, BlockState wallBlock, BlockState ceilingBlock) {
        int dx = outward.getStepX();
        int dz = outward.getStepZ();
        int px = (dx != 0) ? 0 : 1;
        int pz = (dx != 0) ? 1 : 0;

        BlockPos corridorEnd = MuseumStructure.getCorridorEnd(wallPos, outward);
        BlockPos hallOrigin = corridorEnd.offset(-6 * px, 0, -6 * pz);

        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState light = Blocks.SEA_LANTERN.defaultBlockState();

        for (int depth = 1; depth < hallLength; depth++) {
            for (int w = 0; w < HALL_WIDTH; w++) {
                for (int h = 0; h < HALL_HEIGHT; h++) {
                    BlockPos pos = hallOrigin.offset(depth * dx + w * px, h, depth * dz + w * pz);
                    boolean isWall = (w == 0 || w == HALL_WIDTH - 1);
                    boolean isFloor = (h == 0);
                    boolean isCeiling = (h == HALL_HEIGHT - 1);

                    if (isFloor) {
                        level.setBlock(pos, floorBlock, 3);
                    } else if (isCeiling) {
                        if (w >= 5 && w <= 9 && depth % 4 == 2) {
                            level.setBlock(pos, light, 3);
                        } else {
                            level.setBlock(pos, ceilingBlock, 3);
                        }
                    } else if (isWall) {
                        level.setBlock(pos, wallBlock, 3);
                    } else {
                        level.setBlock(pos, air, 3);
                    }
                }
            }
        }

        // Front wall
        for (int w = 0; w < HALL_WIDTH; w++) {
            for (int h = 0; h < HALL_HEIGHT; h++) {
                BlockPos pos = hallOrigin.offset(w * px, h, w * pz);
                boolean isCorridorOpening = (w >= 6 && w <= 8 && h >= 1 && h <= 3);
                if (h == 0) {
                    level.setBlock(pos, floorBlock, 3);
                } else if (isCorridorOpening) {
                    level.setBlock(pos, air, 3);
                } else if (h == HALL_HEIGHT - 1) {
                    level.setBlock(pos, ceilingBlock, 3);
                } else {
                    level.setBlock(pos, wallBlock, 3);
                }
            }
        }

        // Back wall
        for (int w = 0; w < HALL_WIDTH; w++) {
            for (int h = 0; h < HALL_HEIGHT; h++) {
                BlockPos pos = hallOrigin.offset(hallLength * dx + w * px, h, hallLength * dz + w * pz);
                if (h == 0) {
                    level.setBlock(pos, floorBlock, 3);
                } else {
                    level.setBlock(pos, wallBlock, 3);
                }
            }
        }

        return hallOrigin;
    }

    private static int calcMobHallLength(List<String> mobIds, int normalPairLen, int largePairLen) {
        int length = 5; // depthStart(3) + buffer
        // Only count non-boss mobs for the normal layout
        List<String> displayMobs = new ArrayList<>();
        for (String mob : mobIds) {
            if (!isBossMob(mob)) displayMobs.add(mob);
        }
        int i = 0;
        while (i < displayMobs.size()) {
            if (isLargeMob(displayMobs.get(i))) {
                length += largePairLen;
                i++;
                // Two large mobs fit side by side at the same depth
                if (i < displayMobs.size() && isLargeMob(displayMobs.get(i))) {
                    i++;
                }
            } else {
                length += normalPairLen;
                i += 2; // pair of two normal mobs
            }
        }
        // Extra space for boss rooms at the end
        // Wither/Warden branch off as side halls before the dragon — add junction space
        boolean hasWitherOrWarden = false;
        for (String mob : mobIds) {
            String lower = mob.toLowerCase();
            if (lower.contains("ender_dragon")) {
                length += 40;
            } else if (lower.contains("elder_guardian")) {
                length += 18;
            } else if (lower.contains("wither") || lower.contains("warden")) {
                hasWitherOrWarden = true;
            }
        }
        if (hasWitherOrWarden) {
            length += 8; // junction space for side hall branches
        }
        return Math.max(length, MIN_HALL_LENGTH);
    }

    private static int calcHallLength(int exhibitCount, int pairLength) {
        int pairs = (exhibitCount + 1) / 2;
        int length = pairs * pairLength + 2;
        return Math.max(length, MIN_HALL_LENGTH);
    }

    // ─── Wing Decorations (carpet, sconces, theme accents) ───

    private static void placeWingCarpet(ServerLevel level, BlockPos hallOrigin,
                                         int dx, int dz, int px, int pz, int hallLength, BlockState carpetBlock) {
        for (int depth = 1; depth < hallLength; depth++) {
            for (int w = 6; w <= 8; w++) {
                BlockPos pos = hallOrigin.offset(depth * dx + w * px, 1, depth * dz + w * pz);
                level.setBlock(pos, carpetBlock, 3);
            }
        }
    }

    private static void placeWallSconces(ServerLevel level, BlockPos hallOrigin,
                                          int dx, int dz, int px, int pz, int hallLength,
                                          BlockState leftSconce, BlockState rightSconce) {
        for (int depth = 4; depth < hallLength; depth += 5) {
            BlockPos left = hallOrigin.offset(depth * dx + 0 * px, 4, depth * dz + 0 * pz);
            BlockPos right = hallOrigin.offset(depth * dx + (HALL_WIDTH - 1) * px, 4, depth * dz + (HALL_WIDTH - 1) * pz);
            level.setBlock(left, leftSconce, 3);
            level.setBlock(right, rightSconce, 3);
        }
    }

    private static void applyWingTheme(ServerLevel level, BlockPos hallOrigin,
                                        int dx, int dz, int px, int pz, int hallLength, String wingType) {
        switch (wingType) {
            case "aquarium" -> {
                BlockState accent = Blocks.DARK_PRISMARINE.defaultBlockState();
                for (int depth = 2; depth < hallLength; depth += 3) {
                    for (int w : new int[]{0, HALL_WIDTH - 1}) {
                        BlockPos pos = hallOrigin.offset(depth * dx + w * px, 2, depth * dz + w * pz);
                        level.setBlock(pos, accent, 3);
                        pos = hallOrigin.offset(depth * dx + w * px, 5, depth * dz + w * pz);
                        level.setBlock(pos, accent, 3);
                    }
                }
            }
            case "wildlife" -> {
                BlockState mossy = Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
                BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
                for (int depth = 2; depth < hallLength; depth += 3) {
                    for (int w : new int[]{0, HALL_WIDTH - 1}) {
                        level.setBlock(hallOrigin.offset(depth * dx + w * px, 1, depth * dz + w * pz), mossy, 3);
                        level.setBlock(hallOrigin.offset(depth * dx + w * px, 3, depth * dz + w * pz), mossy, 3);
                    }
                }
                // Leaves on ceiling above enclosures
                for (int depth = 4; depth < hallLength; depth += 7) {
                    for (int w = 2; w <= 5; w++) {
                        BlockPos pos = hallOrigin.offset(depth * dx + w * px, HALL_HEIGHT - 1, depth * dz + w * pz);
                        level.setBlock(pos, leaves, 3);
                    }
                    for (int w = 9; w <= 12; w++) {
                        BlockPos pos = hallOrigin.offset(depth * dx + w * px, HALL_HEIGHT - 1, depth * dz + w * pz);
                        level.setBlock(pos, leaves, 3);
                    }
                }
            }
            case "art" -> {
                BlockState strippedLog = Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState();
                BlockState chandelier = FurnitureRegistry.MED_LAMP_HANGING.get().defaultBlockState();
                for (int depth = 3; depth < hallLength; depth += 4) {
                    for (int w : new int[]{0, HALL_WIDTH - 1}) {
                        for (int h = 1; h <= 6; h++) {
                            level.setBlock(hallOrigin.offset(depth * dx + w * px, h, depth * dz + w * pz), strippedLog, 3);
                        }
                    }
                }
                // Chandeliers on ceiling
                for (int depth = 5; depth < hallLength; depth += 8) {
                    for (int w = 6; w <= 8; w++) {
                        level.setBlock(hallOrigin.offset(depth * dx + w * px, HALL_HEIGHT - 1, depth * dz + w * pz), chandelier, 3);
                    }
                }
            }
            case "items" -> {
                Direction accentLeft = (px != 0) ? Direction.EAST : Direction.SOUTH;
                Direction accentRight = (px != 0) ? Direction.WEST : Direction.NORTH;
                BlockState accentL = FurnitureRegistry.VINTAGE_SHOWCASE.get().defaultBlockState()
                    .setValue(HorizontalDirectionalBlock.FACING, accentLeft);
                BlockState accentR = FurnitureRegistry.VINTAGE_SHOWCASE.get().defaultBlockState()
                    .setValue(HorizontalDirectionalBlock.FACING, accentRight);
                for (int depth = 3; depth < hallLength; depth += 3) {
                    level.setBlock(hallOrigin.offset(depth * dx + 0 * px, 1, depth * dz + 0 * pz), accentL, 3);
                    level.setBlock(hallOrigin.offset(depth * dx + (HALL_WIDTH - 1) * px, 1, depth * dz + (HALL_WIDTH - 1) * pz), accentR, 3);
                }
            }
        }
    }

    private static void applyCompletionReward(ServerLevel level, BlockPos hallOrigin,
                                               int dx, int dz, int px, int pz, int hallLength,
                                               String wingName) {
        BlockState gold = Blocks.GOLD_BLOCK.defaultBlockState();
        // Replace walls with gold
        for (int depth = 0; depth <= hallLength; depth++) {
            for (int h = 1; h <= 6; h++) {
                level.setBlock(hallOrigin.offset(depth * dx + 0 * px, h, depth * dz + 0 * pz), gold, 3);
                level.setBlock(hallOrigin.offset(depth * dx + (HALL_WIDTH - 1) * px, h, depth * dz + (HALL_WIDTH - 1) * pz), gold, 3);
            }
        }
        // Glowstone tower at back wall center
        for (int h = 1; h <= 5; h++) {
            BlockPos pos = hallOrigin.offset(hallLength * dx + 7 * px, h, hallLength * dz + 7 * pz);
            level.setBlock(pos, Blocks.GLOWSTONE.defaultBlockState(), 3);
        }

    }

    // ─── Aquarium Wing (East) ───

    private static void buildAndPopulateAquariumWing(ServerLevel level, BlockPos wallPos, Direction outward, List<String> mobIds) {
        int hallLength = calcMobHallLength(mobIds, AQUARIUM_PAIR_LENGTH, AQUARIUM_LARGE_LENGTH);
        BlockPos hallOrigin = buildHall(level, wallPos, outward, hallLength,
            Blocks.PRISMARINE.defaultBlockState(),
            Blocks.PRISMARINE_BRICKS.defaultBlockState(),
            Blocks.PRISMARINE_BRICKS.defaultBlockState());

        int dx = outward.getStepX();
        int dz = outward.getStepZ();
        int px = (dx != 0) ? 0 : 1;
        int pz = (dx != 0) ? 1 : 0;

        placeWingCarpet(level, hallOrigin, dx, dz, px, pz, hallLength, Blocks.BLUE_CARPET.defaultBlockState());
        BlockState seaLanternSconce = Blocks.SEA_LANTERN.defaultBlockState();
        placeWallSconces(level, hallOrigin, dx, dz, px, pz, hallLength, seaLanternSconce, seaLanternSconce);
        applyWingTheme(level, hallOrigin, dx, dz, px, pz, hallLength, "aquarium");

        // Tropical entrance decorations (before first tank at depth=3)
        level.setBlock(hallOrigin.offset(1 * dx + 2 * px, 1, 1 * dz + 2 * pz),
            FurnitureRegistry.CARIBBEAN_BEACH_SIGN.get().defaultBlockState(), 3);
        level.setBlock(hallOrigin.offset(1 * dx + 12 * px, 1, 1 * dz + 12 * pz),
            FurnitureRegistry.CARIBBEAN_SHIP_WHEEL_CLOCK.get().defaultBlockState(), 3);

        // Filter out boss mobs upfront — they get their own room at the end
        List<String> displayMobs = new ArrayList<>();
        for (String mob : mobIds) {
            if (!isBossMob(mob)) displayMobs.add(mob);
        }

        int depthStart = 3;
        int currentDepth = depthStart;
        int displayIdx = 0;

        while (displayIdx < displayMobs.size()) {
            String mobId = displayMobs.get(displayIdx);
            boolean large = isLargeMob(mobId);

            if (large) {
                // Place large pens side by side (left then right at the same depth)
                for (int side = 0; side < 2 && displayIdx < displayMobs.size(); side++) {
                    String largeMob = displayMobs.get(displayIdx);
                    if (!isLargeMob(largeMob)) break;
                    boolean isLeft = (side == 0);
                    int widthStart = isLeft ? 1 : 9;
                    buildAquariumTank(level, hallOrigin, dx, dz, px, pz, currentDepth, widthStart, 7, 5, 6);
                    decorateAquariumTank(level, hallOrigin, dx, dz, px, pz, currentDepth, widthStart, largeMob, 5, 7);
                    BlockPos mobPos = hallOrigin.offset((currentDepth + 3) * dx + (widthStart + 2) * px, 2, (currentDepth + 3) * dz + (widthStart + 2) * pz);
                    spawnMuseumMob(level, largeMob, mobPos, false);
                    int labelWidth = isLeft ? 3 : 11;
                    BlockPos labelPos = hallOrigin.offset((currentDepth + 3) * dx + labelWidth * px, 4, (currentDepth + 3) * dz + labelWidth * pz);
                    placeNameLabel(level, labelPos, prettifyMobName(largeMob));
                    displayIdx++;
                }
                currentDepth += AQUARIUM_LARGE_LENGTH;
            } else {
                for (int side = 0; side < 2 && displayIdx < displayMobs.size(); side++) {
                    String pairMob = displayMobs.get(displayIdx);
                    if (isLargeMob(pairMob)) break;
                    boolean isLeft = (side == 0);
                    int widthStart = isLeft ? 1 : 9;
                    buildAquariumTank(level, hallOrigin, dx, dz, px, pz, currentDepth, widthStart, 5, 5, 5);
                    decorateAquariumTank(level, hallOrigin, dx, dz, px, pz, currentDepth, widthStart, pairMob, 5, 5);
                    BlockPos mobPos = hallOrigin.offset((currentDepth + 2) * dx + (widthStart + 2) * px, 2, (currentDepth + 2) * dz + (widthStart + 2) * pz);
                    spawnMuseumMob(level, pairMob, mobPos, false);
                    int labelWidth = isLeft ? 3 : 11;
                    BlockPos labelPos = hallOrigin.offset((currentDepth + 2) * dx + labelWidth * px, 3, (currentDepth + 2) * dz + labelWidth * pz);
                    placeNameLabel(level, labelPos, prettifyMobName(pairMob));
                    displayIdx++;
                }
                currentDepth += AQUARIUM_PAIR_LENGTH;
            }
        }

        // Boss room at the end: Elder Guardian gets a massive single tank with glass viewing wall
        if (mobIds.contains("minecraft:elder_guardian")) {
            int bossDepth = currentDepth + 2;
            // One big tank spanning w=1..13 (13 wide), 11 deep, 8 tall
            // Glass wall on the entrance-facing side (d=0) so players can see in from the hallway
            int bossTankWidth = 13; // w=1 to w=13
            int bossTankDepth = 11;
            int bossTankHeight = 8;
            BlockState bPrismarine = Blocks.PRISMARINE_BRICKS.defaultBlockState();
            BlockState bDarkPrismarine = Blocks.DARK_PRISMARINE.defaultBlockState();
            BlockState bFloor = Blocks.PRISMARINE.defaultBlockState();
            BlockState bGlass = Blocks.GLASS.defaultBlockState();
            BlockState bWater = Blocks.WATER.defaultBlockState();
            BlockState bLantern = Blocks.SEA_LANTERN.defaultBlockState();

            for (int d = 0; d < bossTankDepth; d++) {
                for (int w = 1; w <= bossTankWidth; w++) {
                    int ad = bossDepth + d;
                    for (int h = 0; h < bossTankHeight; h++) {
                        BlockPos pos = hallOrigin.offset(ad * dx + w * px, h, ad * dz + w * pz);
                        boolean edgeD = (d == 0 || d == bossTankDepth - 1);
                        boolean edgeW = (w == 1 || w == bossTankWidth);
                        boolean corner = edgeD && edgeW;
                        if (h == 0) {
                            // Floor with sea lanterns for lighting
                            if ((d == 3 || d == 7) && (w == 4 || w == 10)) {
                                level.setBlock(pos, bLantern, 3);
                            } else {
                                level.setBlock(pos, bFloor, 3);
                            }
                        } else if (h == bossTankHeight - 1) {
                            // Glass ceiling
                            level.setBlock(pos, bGlass, 3);
                        } else if (corner) {
                            level.setBlock(pos, bDarkPrismarine, 3);
                        } else if (d == 0) {
                            // Front wall (facing main room): ALL GLASS for viewing
                            level.setBlock(pos, bGlass, 3);
                        } else if (edgeD || edgeW) {
                            // Side and back walls: prismarine with lanterns
                            if (h == 3 && edgeW && (d == bossTankDepth / 3 || d == 2 * bossTankDepth / 3)) {
                                level.setBlock(pos, bLantern, 3);
                            } else {
                                level.setBlock(pos, bPrismarine, 3);
                            }
                        } else {
                            level.setBlock(pos, bWater, 3);
                        }
                    }
                }
            }
            // Coral and sea decorations scattered on the tank floor
            level.setBlock(hallOrigin.offset((bossDepth + 2) * dx + 3 * px, 1, (bossDepth + 2) * dz + 3 * pz), Blocks.TUBE_CORAL_BLOCK.defaultBlockState(), 3);
            level.setBlock(hallOrigin.offset((bossDepth + 4) * dx + 7 * px, 1, (bossDepth + 4) * dz + 7 * pz), Blocks.BRAIN_CORAL_BLOCK.defaultBlockState(), 3);
            level.setBlock(hallOrigin.offset((bossDepth + 6) * dx + 11 * px, 1, (bossDepth + 6) * dz + 11 * pz), Blocks.HORN_CORAL_BLOCK.defaultBlockState(), 3);
            level.setBlock(hallOrigin.offset((bossDepth + 3) * dx + 10 * px, 1, (bossDepth + 3) * dz + 10 * pz), Blocks.FIRE_CORAL_BLOCK.defaultBlockState(), 3);
            level.setBlock(hallOrigin.offset((bossDepth + 7) * dx + 4 * px, 1, (bossDepth + 7) * dz + 4 * pz), Blocks.BUBBLE_CORAL_BLOCK.defaultBlockState(), 3);
            // Spawn Elder Guardian in the center of the tank
            BlockPos bossPos = hallOrigin.offset((bossDepth + bossTankDepth / 2) * dx + 7 * px, 4, (bossDepth + bossTankDepth / 2) * dz + 7 * pz);
            spawnMuseumMob(level, "minecraft:elder_guardian", bossPos, false);
            // Label above the glass viewing wall
            placeNameLabel(level, hallOrigin.offset(bossDepth * dx + 7 * px, bossTankHeight, bossDepth * dz + 7 * pz), "Elder Guardian");
        }

        if (mobIds.size() >= AquariumCatalog.getTotalCount()) {
            applyCompletionReward(level, hallOrigin, dx, dz, px, pz, hallLength, "aquarium");
        }
    }

    private static void buildAquariumTank(ServerLevel level, BlockPos hallOrigin,
                                           int dx, int dz, int px, int pz,
                                           int depthStart, int widthStart,
                                           int tankDepth, int tankWidth, int tankHeight) {
        BlockState prismarine = Blocks.PRISMARINE_BRICKS.defaultBlockState();
        BlockState prismarineFloor = Blocks.PRISMARINE.defaultBlockState();
        BlockState glass = Blocks.GLASS.defaultBlockState();
        BlockState water = Blocks.WATER.defaultBlockState();
        BlockState seaLantern = Blocks.SEA_LANTERN.defaultBlockState();

        for (int d = 0; d < tankDepth; d++) {
            for (int w = 0; w < tankWidth; w++) {
                int actualDepth = depthStart + d;
                int actualWidth = widthStart + w;

                for (int h = 0; h < tankHeight; h++) {
                    BlockPos pos = hallOrigin.offset(actualDepth * dx + actualWidth * px, h, actualDepth * dz + actualWidth * pz);

                    boolean isEdgeD = (d == 0 || d == tankDepth - 1);
                    boolean isEdgeW = (w == 0 || w == tankWidth - 1);
                    boolean isFloor = (h == 0);
                    boolean isTop = (h == tankHeight - 1);

                    if (isFloor) {
                        level.setBlock(pos, prismarineFloor, 3);
                    } else if (isTop) {
                        level.setBlock(pos, glass, 3);
                    } else if (isEdgeD && isEdgeW) {
                        level.setBlock(pos, prismarine, 3);
                    } else if (isEdgeD || isEdgeW) {
                        boolean isWalkwaySide = (widthStart < 6) ? (w == tankWidth - 1) : (w == 0);
                        if (isWalkwaySide) {
                            level.setBlock(pos, glass, 3);
                        } else if (d == 2 && isEdgeW && h == 2) {
                            level.setBlock(pos, seaLantern, 3);
                        } else {
                            level.setBlock(pos, prismarine, 3);
                        }
                    } else {
                        level.setBlock(pos, water, 3);
                    }
                }
            }
        }

        // Tank decorations (coral/kelp on tank floor, inside water volume)
        int pattern = (depthStart + widthStart) % 3;
        BlockPos tankFloor1 = hallOrigin.offset((depthStart + 1) * dx + (widthStart + 1) * px, 1, (depthStart + 1) * dz + (widthStart + 1) * pz);
        BlockPos tankFloor2 = hallOrigin.offset((depthStart + 3) * dx + (widthStart + 3) * px, 1, (depthStart + 3) * dz + (widthStart + 3) * pz);
        if (pattern == 0) {
            level.setBlock(tankFloor1, Blocks.TUBE_CORAL_BLOCK.defaultBlockState(), 3);
            level.setBlock(tankFloor2, Blocks.BRAIN_CORAL_BLOCK.defaultBlockState(), 3);
        } else if (pattern == 1) {
            level.setBlock(tankFloor1, Blocks.HORN_CORAL_BLOCK.defaultBlockState(), 3);
            level.setBlock(tankFloor2, Blocks.FIRE_CORAL_BLOCK.defaultBlockState(), 3);
        } else {
            level.setBlock(tankFloor1, Blocks.BUBBLE_CORAL_BLOCK.defaultBlockState(), 3);
            level.setBlock(tankFloor2, Blocks.TUBE_CORAL_BLOCK.defaultBlockState(), 3);
        }
    }

    private static void decorateAquariumTank(ServerLevel level, BlockPos hallOrigin,
                                              int dx, int dz, int px, int pz,
                                              int depthStart, int widthStart,
                                              String mobType, int tankWidth, int tankDepth) {
        String lower = mobType.toLowerCase();
        BlockState coral = null;
        BlockState plant = Blocks.SEAGRASS.defaultBlockState();

        if (lower.contains("tropical") || lower.contains("pufferfish")) {
            coral = Blocks.FIRE_CORAL_BLOCK.defaultBlockState();
        } else if (lower.contains("dolphin") || lower.contains("turtle")) {
            coral = Blocks.BRAIN_CORAL_BLOCK.defaultBlockState();
        } else if (lower.contains("guardian") || lower.contains("elder_guardian")) {
            coral = Blocks.PRISMARINE.defaultBlockState();
            plant = Blocks.KELP_PLANT.defaultBlockState();
        } else if (lower.contains("squid") || lower.contains("glow_squid")) {
            coral = Blocks.TUBE_CORAL_BLOCK.defaultBlockState();
        } else {
            coral = Blocks.HORN_CORAL_BLOCK.defaultBlockState();
        }

        // Place decorations on the tank floor
        int[][] decoPositions = {{1, 1}, {tankDepth - 2, tankWidth - 2}, {1, tankWidth - 2}, {tankDepth - 2, 1}};
        for (int i = 0; i < decoPositions.length && i < 4; i++) {
            int d = depthStart + decoPositions[i][0];
            int w = widthStart + decoPositions[i][1];
            BlockPos pos = hallOrigin.offset(d * dx + w * px, 1, d * dz + w * pz);
            if (i < 2 && coral != null) {
                level.setBlock(pos, coral, 3);
            } else {
                level.setBlock(pos, plant, 3);
            }
        }
    }

    // ─── Wildlife Wing (West) ───

    private static void buildAndPopulateWildlifeWing(ServerLevel level, BlockPos wallPos, Direction outward, List<String> mobIds) {
        int hallLength = calcMobHallLength(mobIds, WILDLIFE_PAIR_LENGTH, WILDLIFE_LARGE_LENGTH);
        BlockPos hallOrigin = buildHall(level, wallPos, outward, hallLength,
            Blocks.POLISHED_DEEPSLATE.defaultBlockState(),
            Blocks.STONE_BRICKS.defaultBlockState(),
            Blocks.STONE_BRICKS.defaultBlockState());

        int dx = outward.getStepX();
        int dz = outward.getStepZ();
        int px = (dx != 0) ? 0 : 1;
        int pz = (dx != 0) ? 1 : 0;

        Direction inwardLeft = (px != 0) ? Direction.EAST : Direction.SOUTH;
        Direction inwardRight = (px != 0) ? Direction.WEST : Direction.NORTH;

        placeWingCarpet(level, hallOrigin, dx, dz, px, pz, hallLength, Blocks.GREEN_CARPET.defaultBlockState());
        placeWallSconces(level, hallOrigin, dx, dz, px, pz, hallLength,
            FurnitureRegistry.DUNGEON_TORCH_DECOR.get().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, inwardLeft),
            FurnitureRegistry.DUNGEON_TORCH_DECOR.get().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, inwardRight));
        applyWingTheme(level, hallOrigin, dx, dz, px, pz, hallLength, "wildlife");

        // Filter out boss mobs upfront — they get their own room at the end
        List<String> displayMobs = new ArrayList<>();
        for (String mob : mobIds) {
            if (!isBossMob(mob)) {
                displayMobs.add(mob);
            }
        }

        int depthStart = 3;
        int currentDepth = depthStart;
        int displayIdx = 0;

        while (displayIdx < displayMobs.size()) {
            String mobId = displayMobs.get(displayIdx);
            boolean large = isLargeMob(mobId);

            if (large) {
                // Place large pens side by side (left then right at the same depth)
                for (int side = 0; side < 2 && displayIdx < displayMobs.size(); side++) {
                    String largeMob = displayMobs.get(displayIdx);
                    if (!isLargeMob(largeMob)) break;
                    boolean isLeft = (side == 0);
                    int widthStart = isLeft ? 1 : 9;
                    buildWildlifeEnclosure(level, hallOrigin, dx, dz, px, pz, currentDepth, widthStart, largeMob, 5, 7);
                    BlockPos mobPos = hallOrigin.offset((currentDepth + 3) * dx + (widthStart + 2) * px, 1, (currentDepth + 3) * dz + (widthStart + 2) * pz);
                    spawnMuseumMob(level, largeMob, mobPos, false);
                    int labelWidth = isLeft ? 3 : 11;
                    BlockPos labelPos = hallOrigin.offset((currentDepth + 3) * dx + labelWidth * px, 5, (currentDepth + 3) * dz + labelWidth * pz);
                    placeNameLabel(level, labelPos, prettifyMobName(largeMob));
                    displayIdx++;
                }
                currentDepth += WILDLIFE_LARGE_LENGTH;
            } else {
                // Normal pair: two small enclosures side by side
                for (int side = 0; side < 2 && displayIdx < displayMobs.size(); side++) {
                    String pairMob = displayMobs.get(displayIdx);
                    if (isLargeMob(pairMob)) break;
                    boolean isLeft = (side == 0);
                    int widthStart = isLeft ? 1 : 9;
                    buildWildlifeEnclosure(level, hallOrigin, dx, dz, px, pz, currentDepth, widthStart, pairMob, 5, 5);
                    BlockPos mobPos = hallOrigin.offset((currentDepth + 2) * dx + (widthStart + 2) * px, 1, (currentDepth + 2) * dz + (widthStart + 2) * pz);
                    spawnMuseumMob(level, pairMob, mobPos, false);
                    BlockPos labelPos = hallOrigin.offset((currentDepth + 2) * dx + (widthStart + 2) * px, 4, (currentDepth + 2) * dz + (widthStart + 2) * pz);
                    placeNameLabel(level, labelPos, prettifyMobName(pairMob));
                    displayIdx++;
                }
                currentDepth += WILDLIFE_PAIR_LENGTH;
            }
        }

        // Wither & Warden boss rooms — branch off as perpendicular side halls
        // Calculate branchDepth now for dragon room positioning, but build AFTER
        // completion reward so side halls punch through any gold walls
        boolean hasWither = mobIds.contains("minecraft:wither");
        boolean hasWarden = mobIds.contains("minecraft:warden");
        int branchDepth = -1;

        if (hasWither || hasWarden) {
            branchDepth = currentDepth + 2;
            currentDepth = branchDepth + 3;
        }

        // Boss room at the end: Ender Dragon gets a massive chamber to fly around in
        if (mobIds.contains("minecraft:ender_dragon")) {
            int bossDepth = currentDepth + 2;
            // 25 wide × 30 deep × 20 tall — enormous room so the dragon can actually fly
            int dragonWidth = 25;
            int dragonDepth = 30;
            int dragonHeight = 20;
            // Center the wider room relative to the hall (hall is 15 wide, room is 25)
            int widthOffset = -(dragonWidth - HALL_WIDTH) / 2; // -5
            BlockState endStone = Blocks.END_STONE.defaultBlockState();
            BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
            BlockState air = Blocks.AIR.defaultBlockState();
            BlockState barrier = Blocks.BARRIER.defaultBlockState();
            BlockState purpur = Blocks.PURPUR_BLOCK.defaultBlockState();

            for (int d = 0; d < dragonDepth; d++) {
                for (int w = 0; w < dragonWidth; w++) {
                    int ad = bossDepth + d;
                    int aw = w + widthOffset;

                    // Floor — end stone with purpur ring pattern
                    BlockPos floorPos = hallOrigin.offset(ad * dx + aw * px, 0, ad * dz + aw * pz);
                    boolean floorRing = (d == 0 || d == dragonDepth - 1 || w == 0 || w == dragonWidth - 1
                            || d == 1 || d == dragonDepth - 2 || w == 1 || w == dragonWidth - 2);
                    level.setBlock(floorPos, floorRing ? purpur : endStone, 3);

                    boolean isEdgeD = (d == 0 || d == dragonDepth - 1);
                    boolean isEdgeW = (w == 0 || w == dragonWidth - 1);
                    boolean isCorner = isEdgeD && isEdgeW;

                    for (int h = 1; h < dragonHeight; h++) {
                        BlockPos pos = hallOrigin.offset(ad * dx + aw * px, h, ad * dz + aw * pz);
                        boolean isCeiling = (h == dragonHeight - 1);

                        if (isCeiling) {
                            level.setBlock(pos, isCorner ? obsidian : barrier, 3);
                        } else if (isCorner) {
                            // Obsidian pillars at all 4 corners only
                            level.setBlock(pos, obsidian, 3);
                        } else if (isEdgeD || isEdgeW) {
                            // All barrier walls — fully transparent so you can see the dragon
                            level.setBlock(pos, barrier, 3);
                        } else {
                            level.setBlock(pos, air, 3);
                        }
                    }
                }
            }

            // Obsidian pillars — 4 pillars around the room (like end pillars)
            int pillarCW = dragonWidth / 2 + widthOffset;
            int[][] pillarPositions = {
                {bossDepth + dragonDepth / 3, 6 + widthOffset},
                {bossDepth + dragonDepth / 3, dragonWidth - 7 + widthOffset},
                {bossDepth + 2 * dragonDepth / 3, 6 + widthOffset},
                {bossDepth + 2 * dragonDepth / 3, dragonWidth - 7 + widthOffset}
            };
            for (int[] pillar : pillarPositions) {
                for (int h = 1; h <= 6; h++) {
                    level.setBlock(hallOrigin.offset(pillar[0] * dx + pillar[1] * px, h, pillar[0] * dz + pillar[1] * pz), obsidian, 3);
                }
                level.setBlock(hallOrigin.offset(pillar[0] * dx + pillar[1] * px, 7, pillar[0] * dz + pillar[1] * pz), FurnitureRegistry.DUNGEON_TORCH_DECOR.get().defaultBlockState(), 3);
            }

            // Central obsidian pedestal with end crystal aesthetic
            int centerD = bossDepth + dragonDepth / 2;
            for (int h = 1; h <= 5; h++) {
                level.setBlock(hallOrigin.offset(centerD * dx + pillarCW * px, h, centerD * dz + pillarCW * pz), obsidian, 3);
            }
            level.setBlock(hallOrigin.offset(centerD * dx + pillarCW * px, 6, centerD * dz + pillarCW * pz), FurnitureRegistry.DUNGEON_TORCH_DECOR.get().defaultBlockState(), 3);

            // Spawn dragon in the center with plenty of room to fly
            BlockPos bossPos = hallOrigin.offset(centerD * dx + pillarCW * px, 8, centerD * dz + pillarCW * pz);
            spawnMuseumMob(level, "minecraft:ender_dragon", bossPos, false);
            placeNameLabel(level, hallOrigin.offset((bossDepth + 2) * dx + pillarCW * px, 8, (bossDepth + 2) * dz + pillarCW * pz), "Ender Dragon");
            currentDepth = bossDepth + dragonDepth + 2;
        }

        if (mobIds.size() >= WildlifeCatalog.getTotalCount()) {
            applyCompletionReward(level, hallOrigin, dx, dz, px, pz, hallLength, "wildlife");
        }

        // Build Wither/Warden side halls AFTER completion reward so corridors punch through gold walls
        if (branchDepth >= 0) {
            if (hasWither) {
                buildSideHallBranch(level, hallOrigin, dx, dz, px, pz, branchDepth,
                    -px, -pz,
                    "minecraft:wither", "Wither",
                    Blocks.SOUL_SAND.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(),
                    Blocks.SOUL_LANTERN.defaultBlockState(),
                    Blocks.STONE_BRICKS.defaultBlockState());
            }
            if (hasWarden) {
                buildSideHallBranch(level, hallOrigin, dx, dz, px, pz, branchDepth,
                    px, pz,
                    "minecraft:warden", "Warden",
                    Blocks.SCULK.defaultBlockState(), Blocks.DEEPSLATE_BRICKS.defaultBlockState(),
                    Blocks.SCULK_CATALYST.defaultBlockState(),
                    Blocks.STONE_BRICKS.defaultBlockState());
            }

            // Carpet connecting main wing path (w=6-8) to side corridor entrances
            // Fills the 5 blocks on each side of the center carpet at the branch junction
            BlockState junctionCarpet = Blocks.GREEN_CARPET.defaultBlockState();
            for (int dOff = -1; dOff <= 1; dOff++) {
                int d = branchDepth + dOff;
                // Left side: w=1 to w=5 (toward Wither corridor)
                if (hasWither) {
                    for (int w = 1; w <= 5; w++) {
                        BlockPos pos = hallOrigin.offset(d * dx + w * px, 1, d * dz + w * pz);
                        level.setBlock(pos, junctionCarpet, 3);
                    }
                }
                // Right side: w=9 to w=13 (toward Warden corridor)
                if (hasWarden) {
                    for (int w = 9; w <= 13; w++) {
                        BlockPos pos = hallOrigin.offset(d * dx + w * px, 1, d * dz + w * pz);
                        level.setBlock(pos, junctionCarpet, 3);
                    }
                }
            }
        }
    }

    private static int buildBossEnclosure(ServerLevel level, BlockPos hallOrigin,
                                           int dx, int dz, int px, int pz, int currentDepth,
                                           String mobId, String label, int roomWidth, int roomDepth, int roomHeight,
                                           BlockState floorBlock, BlockState wallBlock, BlockState accentBlock) {
        int bossDepth = currentDepth + 2;
        int widthOffset = -(roomWidth - HALL_WIDTH) / 2;
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState barrier = Blocks.BARRIER.defaultBlockState();

        for (int d = 0; d < roomDepth; d++) {
            for (int w = 0; w < roomWidth; w++) {
                int ad = bossDepth + d;
                int aw = w + widthOffset;

                // Floor
                BlockPos floorPos = hallOrigin.offset(ad * dx + aw * px, 0, ad * dz + aw * pz);
                boolean floorEdge = (d == 0 || d == roomDepth - 1 || w == 0 || w == roomWidth - 1);
                level.setBlock(floorPos, floorEdge ? wallBlock : floorBlock, 3);

                boolean isEdgeD = (d == 0 || d == roomDepth - 1);
                boolean isEdgeW = (w == 0 || w == roomWidth - 1);
                boolean isCorner = isEdgeD && isEdgeW;

                for (int h = 1; h < roomHeight; h++) {
                    BlockPos pos = hallOrigin.offset(ad * dx + aw * px, h, ad * dz + aw * pz);
                    boolean isCeiling = (h == roomHeight - 1);

                    if (isCeiling) {
                        level.setBlock(pos, isCorner ? wallBlock : barrier, 3);
                    } else if (isCorner) {
                        level.setBlock(pos, wallBlock, 3);
                    } else if (isEdgeD || isEdgeW) {
                        level.setBlock(pos, barrier, 3);
                    } else {
                        level.setBlock(pos, air, 3);
                    }
                }
            }
        }

        // 4 accent pillars inside the room
        int cw = roomWidth / 2 + widthOffset;
        int[][] pillars = {
            {bossDepth + roomDepth / 3, 3 + widthOffset},
            {bossDepth + roomDepth / 3, roomWidth - 4 + widthOffset},
            {bossDepth + 2 * roomDepth / 3, 3 + widthOffset},
            {bossDepth + 2 * roomDepth / 3, roomWidth - 4 + widthOffset}
        };
        for (int[] p : pillars) {
            for (int h = 1; h <= 3; h++) {
                level.setBlock(hallOrigin.offset(p[0] * dx + p[1] * px, h, p[0] * dz + p[1] * pz), wallBlock, 3);
            }
            level.setBlock(hallOrigin.offset(p[0] * dx + p[1] * px, 4, p[0] * dz + p[1] * pz), accentBlock, 3);
        }

        // Spawn mob in the center
        int centerD = bossDepth + roomDepth / 2;
        BlockPos bossPos = hallOrigin.offset(centerD * dx + cw * px, 1, centerD * dz + cw * pz);
        spawnMuseumMob(level, mobId, bossPos, false);
        placeNameLabel(level, hallOrigin.offset((bossDepth + 2) * dx + cw * px, 5, (bossDepth + 2) * dz + cw * pz), label);

        return bossDepth + roomDepth + 2;
    }

    /**
     * Builds a perpendicular side-hall branch off the main wildlife wing.
     * Creates a 3-wide, 4-tall, 5-deep corridor in the given side direction,
     * then places a boss enclosure room at the end of the corridor.
     *
     * @param hallOrigin   origin of the main wildlife hall
     * @param dx, dz       main hall depth direction
     * @param px, pz       main hall perpendicular (width) direction
     * @param branchDepth  depth along the main hall where the branch starts
     * @param sideDx, sideDz  direction the branch extends (perpendicular to main hall)
     * @param mobId        boss mob ID
     * @param label        display label
     * @param floorBlock   boss room floor material
     * @param wallBlock    boss room wall material
     * @param accentBlock  boss room accent/pillar material
     * @param corridorWall corridor wall material (matches main wing)
     */
    private static void buildSideHallBranch(ServerLevel level, BlockPos hallOrigin,
                                             int dx, int dz, int px, int pz, int branchDepth,
                                             int sideDx, int sideDz,
                                             String mobId, String label,
                                             BlockState floorBlock, BlockState wallBlock,
                                             BlockState accentBlock, BlockState corridorWall) {
        // Coordinate system: pos = hallOrigin + depth*(dx,dz) + width*(px,pz)
        // Left branch: wall at w=0, corridor goes negative. Right: wall at w=14, goes positive.
        boolean isLeft = (sideDx == -px && sideDz == -pz);
        int corridorLen = 10;
        int corridorH = 5;
        int corridorHalfW = 2;   // 5 blocks wide centered on branchDepth
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState floor = Blocks.POLISHED_DEEPSLATE.defaultBlockState();
        BlockState carpet = Blocks.GREEN_CARPET.defaultBlockState();
        BlockState light = Blocks.SEA_LANTERN.defaultBlockState();

        // Width range for the corridor (inclusive)
        // Left: clear from w=0 (wall) through w=-(corridorLen) (outward)
        // Right: clear from w=HALL_WIDTH-1 (wall) through w=HALL_WIDTH-1+corridorLen (outward)
        int wStart = isLeft ? -corridorLen : HALL_WIDTH - 1;
        int wEnd   = isLeft ? 0 : HALL_WIDTH - 1 + corridorLen;

        // 1. Build the entire corridor + clear wall opening in one pass
        for (int w = wStart; w <= wEnd; w++) {
            for (int dOff = -corridorHalfW; dOff <= corridorHalfW; dOff++) {
                int d = branchDepth + dOff;
                boolean isEdge = (dOff == -corridorHalfW || dOff == corridorHalfW);

                // Floor
                level.setBlock(hallOrigin.offset(d * dx + w * px, 0, d * dz + w * pz), floor, 3);

                // Carpet on center 3 blocks
                if (!isEdge) {
                    level.setBlock(hallOrigin.offset(d * dx + w * px, 1, d * dz + w * pz), carpet, 3);
                }

                // Walls and ceiling (skip carpet height for non-edges)
                for (int h = 1; h < corridorH; h++) {
                    BlockPos pos = hallOrigin.offset(d * dx + w * px, h, d * dz + w * pz);
                    boolean isCeiling = (h == corridorH - 1);
                    if (isCeiling) {
                        // Light every 4 blocks along the corridor
                        int distFromWall = isLeft ? -w : w - (HALL_WIDTH - 1);
                        level.setBlock(pos, (distFromWall % 4 == 2) ? light : corridorWall, 3);
                    } else if (isEdge) {
                        level.setBlock(pos, corridorWall, 3);
                    } else if (h > 1) {
                        level.setBlock(pos, air, 3);
                    }
                    // h==1 already has carpet or wall, don't overwrite
                }
            }
        }

        // 2. Boss room at end of corridor (15x15x10)
        // Room origin: just past the last corridor block, centered on branchDepth
        int farW = isLeft ? wStart - 1 : wEnd + 1;
        BlockPos roomOrigin = hallOrigin.offset(
            (branchDepth - 7) * dx + farW * px,
            0,
            (branchDepth - 7) * dz + farW * pz
        );
        // Room depth = side direction, room width = main hall direction
        buildBossEnclosure(level, roomOrigin, sideDx, sideDz, dx, dz, 0,
            mobId, label, 15, 15, 10,
            floorBlock, wallBlock, accentBlock);

        // 3. Cut entrance in boss room wall — clear 5 blocks wide centered on corridor
        for (int rw = 5; rw <= 9; rw++) {
            for (int h = 0; h < corridorH; h++) {
                BlockPos pos = roomOrigin.offset(0 * sideDx + rw * dx, h, 0 * sideDz + rw * dz);
                if (h == 0) {
                    level.setBlock(pos, floor, 3);
                } else {
                    level.setBlock(pos, air, 3);
                }
            }
        }
        // Carpet continuation into the room entrance
        for (int rw = 6; rw <= 8; rw++) {
            BlockPos pos = roomOrigin.offset(0 * sideDx + rw * dx, 1, 0 * sideDz + rw * dz);
            level.setBlock(pos, carpet, 3);
        }

        // Boss room atmosphere — themed furniture in corners (safe spots away from pillars & mob center)
        if (mobId.contains("wither")) {
            level.setBlock(roomOrigin.offset(4 * sideDx + 2 * dx, 1, 4 * sideDz + 2 * dz),
                FurnitureRegistry.DUNGEON_SKELETON.get().defaultBlockState(), 3);
            level.setBlock(roomOrigin.offset(4 * sideDx + 12 * dx, 1, 4 * sideDz + 12 * dz),
                FurnitureRegistry.DUNGEON_VASE.get().defaultBlockState(), 3);
            level.setBlock(roomOrigin.offset(14 * sideDx + 2 * dx, 1, 14 * sideDz + 2 * dz),
                FurnitureRegistry.DUNGEON_SWORD_BONE.get().defaultBlockState(), 3);
            level.setBlock(roomOrigin.offset(14 * sideDx + 12 * dx, 1, 14 * sideDz + 12 * dz),
                FurnitureRegistry.DUNGEON_FLAG_BONE.get().defaultBlockState(), 3);
        } else if (mobId.contains("warden")) {
            level.setBlock(roomOrigin.offset(4 * sideDx + 2 * dx, 1, 4 * sideDz + 2 * dz),
                FurnitureRegistry.MED_CRYSTAL.get().defaultBlockState(), 3);
            level.setBlock(roomOrigin.offset(4 * sideDx + 12 * dx, 1, 4 * sideDz + 12 * dz),
                FurnitureRegistry.MED_GLOW_MUSHROOM_PATCH_LARGE.get().defaultBlockState(), 3);
            level.setBlock(roomOrigin.offset(14 * sideDx + 2 * dx, 1, 14 * sideDz + 2 * dz),
                FurnitureRegistry.MED_GLOW_MUSHROOM_PATCH.get().defaultBlockState(), 3);
            level.setBlock(roomOrigin.offset(14 * sideDx + 12 * dx, 1, 14 * sideDz + 12 * dz),
                FurnitureRegistry.MED_CRYSTAL.get().defaultBlockState(), 3);
        }
    }

    private static void buildWildlifeEnclosure(ServerLevel level, BlockPos hallOrigin,
                                                int dx, int dz, int px, int pz,
                                                int depthStart, int widthStart, String mobType,
                                                int encWidth, int encDepth) {
        BlockState floorBlock = getWildlifeFloorBlock(mobType);
        BlockState fence = getWildlifeWallBlock(mobType);
        BlockState partition = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState decoration = getWildlifeDecoration(mobType);
        boolean large = (encDepth > 5);
        int barrierHeight = large ? 4 : 3;

        for (int d = 0; d < encDepth; d++) {
            for (int w = 0; w < encWidth; w++) {
                int actualDepth = depthStart + d;
                int actualWidth = widthStart + w;
                BlockPos floorPos = hallOrigin.offset(actualDepth * dx + actualWidth * px, 0, actualDepth * dz + actualWidth * pz);
                level.setBlock(floorPos, floorBlock, 3);

                boolean isEdgeD = (d == 0 || d == encDepth - 1);
                boolean isEdgeW = (w == 0 || w == encWidth - 1);
                BlockPos fencePos = hallOrigin.offset(actualDepth * dx + actualWidth * px, 1, actualDepth * dz + actualWidth * pz);

                if (isEdgeD && isEdgeW) {
                    level.setBlock(fencePos, partition, 3);
                } else if (isEdgeD || isEdgeW) {
                    boolean isWalkwaySide;
                    if (large) {
                        isWalkwaySide = false; // large enclosures: all sides walled
                    } else {
                        isWalkwaySide = (widthStart < 6) ? (w == encWidth - 1) : (w == 0);
                    }
                    if (isWalkwaySide) {
                        level.setBlock(fencePos, fence, 3);
                    } else {
                        level.setBlock(fencePos, partition, 3);
                    }
                } else {
                    // Interior decoration — scattered biome-appropriate blocks
                    BlockState accentBlock = getWildlifeAccentBlock(mobType);
                    if (d == 1 && w == 1 && decoration != null) {
                        level.setBlock(fencePos, decoration, 3);
                    } else if (d == encDepth - 2 && w == encWidth - 2 && accentBlock != null) {
                        level.setBlock(fencePos, accentBlock, 3);
                    } else if (large && d == 2 && w == encWidth - 2 && decoration != null) {
                        level.setBlock(fencePos, decoration, 3);
                    } else if (d == 2 && w == 1) {
                        // Furniture nature piece — rocks, mushrooms, logs per biome
                        level.setBlock(fencePos, getWildlifeFurnitureDecor(mobType), 3);
                    } else if (large && d == 4 && w == 1) {
                        level.setBlock(fencePos, getWildlifeFurnitureExtra(mobType), 3);
                    }
                }

                // Barrier walls on edges only + barrier ceiling to contain mobs
                if (isEdgeD || isEdgeW) {
                    for (int bh = 2; bh <= barrierHeight; bh++) {
                        BlockPos barrierPos = hallOrigin.offset(actualDepth * dx + actualWidth * px, bh, actualDepth * dz + actualWidth * pz);
                        level.setBlock(barrierPos, Blocks.BARRIER.defaultBlockState(), 3);
                    }
                } else {
                    // Barrier ceiling only (top layer) — keeps interior open for mob movement
                    BlockPos ceilPos = hallOrigin.offset(actualDepth * dx + actualWidth * px, barrierHeight, actualDepth * dz + actualWidth * pz);
                    level.setBlock(ceilPos, Blocks.BARRIER.defaultBlockState(), 3);
                }
            }
        }
    }

    // ─── Shared Mob Spawning ───

    private static void spawnMuseumMob(ServerLevel level, String mobId, BlockPos pos, boolean noAi) {
        try {
            Identifier id = Identifier.tryParse(mobId);
            if (id == null) return;
            var optType = BuiltInRegistries.ENTITY_TYPE.get(id);
            if (optType.isEmpty()) return;
            Entity entity = optType.get().value().create(level, EntitySpawnReason.MOB_SUMMONED);
            if (entity == null) return;
            entity.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            entity.setInvulnerable(true);
            entity.setSilent(true);
            entity.addTag(MUSEUM_TAG);
            if (entity instanceof Mob mob) {
                mob.setPersistenceRequired();
                String lower = mobId.toLowerCase();
                // Flying mobs get noAi so they don't escape their pens
                boolean isFlying = lower.contains("vex") || lower.contains("phantom")
                    || lower.contains("ghast") || lower.contains("bat") || lower.contains("blaze")
                    || lower.contains("allay") || lower.contains("bee");
                // Boss mobs get noAi to prevent destructive behavior (except dragon/guardian who have big rooms)
                boolean isFreeBoss = lower.contains("ender_dragon") || lower.contains("elder_guardian");
                if (noAi || isFlying || (isBossMob(mobId) && !isFreeBoss)) {
                    mob.setNoAi(true);
                }
            }
            // Hide Wither boss bar via reflection
            if (entity instanceof WitherBoss wither) {
                try {
                    java.lang.reflect.Field bossField = WitherBoss.class.getDeclaredField("bossEvent");
                    bossField.setAccessible(true);
                    ServerBossEvent bossEvent = (ServerBossEvent) bossField.get(wither);
                    bossEvent.setVisible(false);
                } catch (Exception e) {
                    // Fallback: try obfuscated name patterns
                    for (java.lang.reflect.Field f : WitherBoss.class.getDeclaredFields()) {
                        if (ServerBossEvent.class.isAssignableFrom(f.getType())) {
                            f.setAccessible(true);
                            ((ServerBossEvent) f.get(wither)).setVisible(false);
                            break;
                        }
                    }
                }
            }
            level.addFreshEntity(entity);
        } catch (Exception ignored) {
        }
    }

    // ─── Art Wing (North) — Painting Gallery ───

    private static void buildAndPopulateArtWing(ServerLevel level, BlockPos wallPos, Direction outward, List<String> artIds) {
        int hallLength = calcHallLength(artIds.size(), ART_PAIR_LENGTH);
        BlockPos hallOrigin = buildHall(level, wallPos, outward, hallLength,
            Blocks.DARK_OAK_PLANKS.defaultBlockState(),
            Blocks.STONE_BRICKS.defaultBlockState(),
            Blocks.STONE_BRICKS.defaultBlockState());

        int dx = outward.getStepX();
        int dz = outward.getStepZ();
        int px = (dx != 0) ? 0 : 1;
        int pz = (dx != 0) ? 1 : 0;

        Direction inwardLeft = (px != 0) ? Direction.EAST : Direction.SOUTH;
        Direction inwardRight = (px != 0) ? Direction.WEST : Direction.NORTH;

        placeWingCarpet(level, hallOrigin, dx, dz, px, pz, hallLength, Blocks.RED_CARPET.defaultBlockState());
        placeWallSconces(level, hallOrigin, dx, dz, px, pz, hallLength,
            FurnitureRegistry.CLASSIC_WALL_LAMP.get().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, inwardLeft),
            FurnitureRegistry.CLASSIC_WALL_LAMP.get().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, inwardRight));
        applyWingTheme(level, hallOrigin, dx, dz, px, pz, hallLength, "art");

        int index = 0;
        int depthStart = 3;

        while (index < artIds.size()) {
            int depth = depthStart + (index / 2) * ART_PAIR_LENGTH;
            boolean isLeft = (index % 2 == 0);

            String artId = artIds.get(index);

            // Try to place a wall-mounted painting first
            int paintingW = isLeft ? 1 : 13;
            Direction paintingFacing = isLeft ? getFrameFacingLeft(outward) : getFrameFacingRight(outward);
            BlockPos hangPos = hallOrigin.offset(depth * dx + paintingW * px, 3, depth * dz + paintingW * pz);

            boolean paintingPlaced = tryPlacePainting(level, artId, hangPos, paintingFacing);

            if (paintingPlaced) {
                // Name label above painting
                int labelWidth = isLeft ? 1 : 13;
                BlockPos labelPos = hallOrigin.offset(depth * dx + labelWidth * px, 6, depth * dz + labelWidth * pz);
                placeNameLabel(level, labelPos, prettifyArtName(artId));

                // Spotlight above painting
                BlockPos spotlightPos = hallOrigin.offset(depth * dx + paintingW * px, 5, depth * dz + paintingW * pz);
                Direction spotFacing = isLeft ? inwardLeft : inwardRight;
                level.setBlock(spotlightPos, FurnitureRegistry.CLASSIC_WALL_LAMP_DOUBLE.get().defaultBlockState()
                    .setValue(HorizontalDirectionalBlock.FACING, spotFacing), 3);
            } else {
                // Fallback: pedestal display
                int pedestalWidth = isLeft ? 3 : HALL_WIDTH - 4;
                BlockPos pedestalPos = hallOrigin.offset(depth * dx + pedestalWidth * px, 0, depth * dz + pedestalWidth * pz);
                ItemStack displayItem = MasterpieceRegistry.getItemForVariant(artId);
                if (displayItem.isEmpty()) {
                    displayItem = Items.PAINTING.getDefaultInstance();
                }
                buildPedestal(level, pedestalPos);
                placeItemOnPedestal(level, pedestalPos, displayItem);
                placeNameLabel(level, pedestalPos.above(4), prettifyArtName(artId));
            }

            // Sittable tavern benches in the center aisle every 4th painting
            if (index % 4 == 3) {
                int benchDepth = depth;
                BlockState benchLeft = FurnitureRegistry.BL_TAVERN_BENCH.get().defaultBlockState()
                    .setValue(HorizontalDirectionalBlock.FACING, inwardLeft);
                BlockState benchRight = FurnitureRegistry.BL_TAVERN_BENCH.get().defaultBlockState()
                    .setValue(HorizontalDirectionalBlock.FACING, inwardRight);
                // Left bench at w=5 (faces right wall paintings)
                level.setBlock(hallOrigin.offset(benchDepth * dx + 5 * px, 1, benchDepth * dz + 5 * pz), benchLeft, 3);
                // Right bench at w=9 (faces left wall paintings)
                level.setBlock(hallOrigin.offset(benchDepth * dx + 9 * px, 1, benchDepth * dz + 9 * pz), benchRight, 3);
            }

            index++;
        }

        if (artIds.size() >= ArtCatalog.getTotalCount()) {
            applyCompletionReward(level, hallOrigin, dx, dz, px, pz, hallLength, "art");
        }
    }

    // ─── Painting Placement Helpers ───

    private static boolean tryPlacePainting(ServerLevel level, String variantName, BlockPos hangPos, Direction facing) {
        try {
            ResourceKey<PaintingVariant> key = ResourceKey.create(
                Registries.PAINTING_VARIANT,
                Identifier.fromNamespaceAndPath("megamod", variantName));
            Optional<Holder.Reference<PaintingVariant>> holder = level.registryAccess()
                .lookupOrThrow(Registries.PAINTING_VARIANT)
                .get(key);
            if (holder.isEmpty()) return false;

            Painting painting = new Painting(level, hangPos, facing, holder.get());
            if (painting.survives()) {
                painting.setInvulnerable(true);
                painting.addTag(MUSEUM_TAG);
                level.addFreshEntity(painting);
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private static Direction getFrameFacingLeft(Direction outward) {
        if (outward.getStepX() != 0) return Direction.SOUTH;
        else return Direction.EAST;
    }

    private static Direction getFrameFacingRight(Direction outward) {
        if (outward.getStepX() != 0) return Direction.NORTH;
        else return Direction.WEST;
    }

    // ─── Items Wing (South) — Pedestal Gallery ───

    private static void buildAndPopulateItemsWing(ServerLevel level, BlockPos wallPos, Direction outward,
                                                   List<String> itemIds) {
        int displayedItems = Math.min(itemIds.size(), MAX_DISPLAYED_ITEMS);
        int itemExhibits = Math.min(displayedItems + MAX_EMPTY_PEDESTALS, MAX_DISPLAYED_ITEMS + MAX_EMPTY_PEDESTALS);
        int hallLength = calcHallLength(itemExhibits, ITEMS_PAIR_LENGTH);
        BlockPos hallOrigin = buildHall(level, wallPos, outward, hallLength,
            Blocks.POLISHED_DEEPSLATE.defaultBlockState(),
            Blocks.STONE_BRICKS.defaultBlockState(),
            Blocks.STONE_BRICKS.defaultBlockState());

        int dx = outward.getStepX();
        int dz = outward.getStepZ();
        int px = (dx != 0) ? 0 : 1;
        int pz = (dx != 0) ? 1 : 0;

        Direction inwardLeft = (px != 0) ? Direction.EAST : Direction.SOUTH;
        Direction inwardRight = (px != 0) ? Direction.WEST : Direction.NORTH;

        placeWingCarpet(level, hallOrigin, dx, dz, px, pz, hallLength, Blocks.YELLOW_CARPET.defaultBlockState());
        placeWallSconces(level, hallOrigin, dx, dz, px, pz, hallLength,
            FurnitureRegistry.CLASSIC_WALL_LAMP.get().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, inwardLeft),
            FurnitureRegistry.CLASSIC_WALL_LAMP.get().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, inwardRight));
        applyWingTheme(level, hallOrigin, dx, dz, px, pz, hallLength, "items");

        int index = 0;
        int depthStart = 3;

        // ── Donated Items (capped to prevent excessively long halls) ──
        while (index < itemIds.size() && index < MAX_DISPLAYED_ITEMS) {
            int depth = depthStart + (index / 2) * ITEMS_PAIR_LENGTH;
            boolean isLeft = (index % 2 == 0);
            int pedestalWidth = isLeft ? 3 : HALL_WIDTH - 4;

            BlockPos pedestalPos = hallOrigin.offset(depth * dx + pedestalWidth * px, 0, depth * dz + pedestalWidth * pz);
            ItemStack displayItem = resolveItemStack(itemIds.get(index));
            if (!displayItem.isEmpty()) {
                buildPedestal(level, pedestalPos);
                placeItemOnPedestal(level, pedestalPos, displayItem);
                placeNameLabel(level, pedestalPos.above(4), getItemDisplayName(displayItem, itemIds.get(index)));
            }
            index++;
        }

        // Empty "???" pedestals for undiscovered items
        Set<String> donatedItemSet = new HashSet<>(itemIds);
        int emptyPlaced = 0;
        for (List<String> catItems : ItemCatalog.ITEMS_BY_CATEGORY.values()) {
            for (String itemId : catItems) {
                if (emptyPlaced >= MAX_EMPTY_PEDESTALS) break;
                if (donatedItemSet.contains(itemId)) continue;
                int depth = depthStart + (index / 2) * ITEMS_PAIR_LENGTH;
                boolean isLeft = (index % 2 == 0);
                int pedestalWidth = isLeft ? 3 : HALL_WIDTH - 4;

                BlockPos pedestalPos = hallOrigin.offset(depth * dx + pedestalWidth * px, 0, depth * dz + pedestalWidth * pz);
                buildPedestal(level, pedestalPos);
                placeNameLabel(level, pedestalPos.above(4), "???");
                index++;
                emptyPlaced++;
            }
            if (emptyPlaced >= MAX_EMPTY_PEDESTALS) break;
        }

        boolean isComplete = itemIds.size() >= ItemCatalog.getTotalItemCount();
        if (isComplete) {
            applyCompletionReward(level, hallOrigin, dx, dz, px, pz, hallLength, "items");
        }
    }

    // ─── Basement Staircase (leads straight into achievement hall) ───

    private static final int BASEMENT_DEPTH = 10; // stairs descend 10 blocks below main hall

    private static void buildBasementStaircase(ServerLevel level, BlockPos origin) {
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState stairBlock = Blocks.STONE_BRICK_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, Direction.NORTH);
        BlockState stairWall = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState railing = Blocks.STONE_BRICK_WALL.defaultBlockState();
        BlockState floor = Blocks.DEEPSLATE_TILES.defaultBlockState();

        int lastStairZ = 15 + BASEMENT_DEPTH; // z=25

        // ── 1. Clear entrance in the main hall floor (3 wide opening) ──
        for (int x = 16; x <= 18; x++) {
            for (int z = 15; z <= 17; z++) {
                level.setBlock(origin.offset(x, 0, z), air, 3);
            }
        }

        // ── 2. Build shaft — SIDE WALLS ONLY (x=15/19), no back wall blocking entry ──
        for (int y = -(BASEMENT_DEPTH + 1); y <= 0; y++) {
            for (int x = 15; x <= 19; x++) {
                for (int z = 15; z <= lastStairZ; z++) {
                    BlockPos pos = origin.offset(x, y, z);
                    boolean isSideWall = (x == 15 || x == 19);

                    if (isSideWall) {
                        level.setBlock(pos, stairWall, 3);
                    } else if (y == -(BASEMENT_DEPTH + 1)) {
                        level.setBlock(pos, floor, 3);
                    } else {
                        level.setBlock(pos, air, 3);
                    }
                }
            }
        }

        // ── 3. Clear ALL blocks above the entrance so nothing obstructs ──
        // This runs AFTER shaft walls and removes any leftover blocks from
        // the main hall, decorations, or ceiling that might block the opening
        for (int x = 16; x <= 18; x++) {
            for (int y = 1; y <= 6; y++) {
                for (int z = 14; z <= 17; z++) {
                    level.setBlock(origin.offset(x, y, z), air, 3);
                }
            }
        }

        // ── 4. Place descending stairs ──
        for (int step = 0; step <= BASEMENT_DEPTH; step++) {
            int stairY = -step;
            int stairZ = 15 + step;
            for (int x = 16; x <= 18; x++) {
                level.setBlock(origin.offset(x, stairY, stairZ), stairBlock, 3);
                for (int below = -(BASEMENT_DEPTH + 1); below < stairY; below++) {
                    level.setBlock(origin.offset(x, below, stairZ), stairWall, 3);
                }
            }
        }

        // ── 5. Side railings ──
        for (int step = 0; step <= BASEMENT_DEPTH; step++) {
            int stairY = -step;
            int stairZ = 15 + step;
            level.setBlock(origin.offset(15, stairY + 1, stairZ), railing, 3);
            level.setBlock(origin.offset(19, stairY + 1, stairZ), railing, 3);
        }

        // ── 6. Ceiling ONLY for underground portion past the main hall (z=21+) ──
        // NO ceiling inside the main hall — keeps the staircase open and inviting
        for (int step = 6; step <= BASEMENT_DEPTH; step++) {
            int ceilingY = -step + 7; // raised 3 blocks higher for more headroom
            int stairZ2 = 15 + step;
            for (int x = 15; x <= 19; x++) {
                level.setBlock(origin.offset(x, ceilingY, stairZ2), stairWall, 3);
            }
        }

        // ── 7. Back wall at the bottom of the shaft ──
        for (int x = 15; x <= 19; x++) {
            for (int y = -(BASEMENT_DEPTH + 1); y <= 1; y++) {
                level.setBlock(origin.offset(x, y, lastStairZ + 1), stairWall, 3);
            }
        }

        // "Achievements" label above staircase entrance
        placeNameLabel(level, origin.offset(17, 3, 15), "Achievements");
    }

    // ─── Achievement Hallway (directly below staircase, going south) ───

    private static void buildAndPopulateAchievementBasement(ServerLevel level, BlockPos origin, List<String> achieveIds) {
        // Staircase ends at z = origin.Z + 15 + BASEMENT_DEPTH = z+25
        // wallPos.Z = origin.Z + 21 → corridorEnd.Z = +26 → hall starts at z=26, right after stairs
        // walkway w=6-8 maps to x = hallOrigin.X+6..+8 = (wallPos.X-6)+6..+8 = wallPos.X..+2 = x=16..18 (matches staircase)
        BlockPos wallPos = new BlockPos(origin.getX() + 16, origin.getY() - (BASEMENT_DEPTH + 1), origin.getZ() + 21);
        // Always build the hall long enough for the FULL achievement catalog + buffer
        int totalExhibits = AchievementCatalog.getTotalCount();
        int hallLength = calcHallLength(totalExhibits, ITEMS_PAIR_LENGTH) + 30;
        BlockPos hallOrigin = buildHall(level, wallPos, Direction.SOUTH, hallLength,
            Blocks.DEEPSLATE_TILES.defaultBlockState(),
            Blocks.POLISHED_BLACKSTONE.defaultBlockState(),
            Blocks.DEEPSLATE_BRICKS.defaultBlockState());

        int dx = 0, dz = 1, px = 1, pz = 0; // SOUTH direction

        // Widen the entrance opening (h=1-5 instead of default h=1-3)
        BlockState entranceAir = Blocks.AIR.defaultBlockState();
        for (int w = 6; w <= 8; w++) {
            for (int h = 4; h <= 5; h++) {
                BlockPos pos = hallOrigin.offset(w * px, h, w * pz);
                level.setBlock(pos, entranceAir, 3);
            }
        }

        Direction inwardLeft = (px != 0) ? Direction.EAST : Direction.SOUTH;
        Direction inwardRight = (px != 0) ? Direction.WEST : Direction.NORTH;

        placeWingCarpet(level, hallOrigin, dx, dz, px, pz, hallLength, Blocks.PURPLE_CARPET.defaultBlockState());
        placeWallSconces(level, hallOrigin, dx, dz, px, pz, hallLength,
            FurnitureRegistry.DUNGEON_TORCH_DECOR.get().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, inwardLeft),
            FurnitureRegistry.DUNGEON_TORCH_DECOR.get().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, inwardRight));

        // Dungeon atmosphere at entrance — barrels flanking the doorway
        level.setBlock(hallOrigin.offset(1 * dx + 2 * px, 1, 1 * dz + 2 * pz),
            FurnitureRegistry.DUNGEON_WOOD_BARREL.get().defaultBlockState(), 3);
        level.setBlock(hallOrigin.offset(1 * dx + 12 * px, 1, 1 * dz + 12 * pz),
            FurnitureRegistry.DUNGEON_WOOD_BARREL.get().defaultBlockState(), 3);
        // Weapon stand at back of hall
        level.setBlock(hallOrigin.offset((hallLength - 1) * dx + 7 * px, 1, (hallLength - 1) * dz + 7 * pz),
            FurnitureRegistry.DUNGEON_WEAPONSTAND.get().defaultBlockState(), 3);

        int index = 0;
        int depthStart = 3;

        // Donated achievements — enhanced with gold trim and glowstone accents
        while (index < achieveIds.size()) {
            int depth = depthStart + (index / 2) * ITEMS_PAIR_LENGTH;
            boolean isLeft = (index % 2 == 0);
            int pedestalWidth = isLeft ? 3 : HALL_WIDTH - 4;
            BlockPos pedestalPos = hallOrigin.offset(depth * dx + pedestalWidth * px, 0, depth * dz + pedestalWidth * pz);
            ItemStack icon = getAdvancementIcon(level, achieveIds.get(index));
            buildPedestal(level, pedestalPos);
            placeItemOnPedestal(level, pedestalPos, icon);
            placeNameLabel(level, pedestalPos.above(4), simplifyAdvancementId(achieveIds.get(index)));

            // Gold trim around earned achievement pedestals
            BlockState goldTrim = Blocks.GOLD_BLOCK.defaultBlockState();
            level.setBlock(pedestalPos.above(0).north(), goldTrim, 3);
            level.setBlock(pedestalPos.above(0).south(), goldTrim, 3);
            level.setBlock(pedestalPos.above(0).east(), goldTrim, 3);
            level.setBlock(pedestalPos.above(0).west(), goldTrim, 3);

            index++;
        }

        // Empty "???" pedestals for ALL remaining achievements (hall is sized for full catalog)
        Set<String> donatedSet = new HashSet<>(achieveIds);
        for (AchievementCatalog.AchievementEntry entry : AchievementCatalog.ENTRIES) {
            if (donatedSet.contains(entry.advancementId())) continue;
            int depth = depthStart + (index / 2) * ITEMS_PAIR_LENGTH;
            boolean isLeft = (index % 2 == 0);
            int pedestalWidth = isLeft ? 3 : HALL_WIDTH - 4;
            BlockPos pedestalPos = hallOrigin.offset(depth * dx + pedestalWidth * px, 0, depth * dz + pedestalWidth * pz);
            buildPedestal(level, pedestalPos);
            placeNameLabel(level, pedestalPos.above(4), "???");
            index++;
        }

        if (achieveIds.size() >= AchievementCatalog.getTotalCount()) {
            applyCompletionReward(level, hallOrigin, dx, dz, px, pz, hallLength, "achievements");
        }
    }

    // ─── Pedestal Helpers ───

    private static void buildPedestal(ServerLevel level, BlockPos floorPos) {
        level.setBlock(floorPos.above(1), MuseumRegistry.PEDESTAL_BLOCK.get().defaultBlockState(), 3);
    }

    private static void placeItemOnPedestal(ServerLevel level, BlockPos floorPos, ItemStack item) {
        ItemEntity itemEntity = new ItemEntity(
            (Level) level,
            floorPos.getX() + 0.5,
            floorPos.getY() + 1.85,
            floorPos.getZ() + 0.5,
            item.copy()
        );
        itemEntity.setNoGravity(true);
        itemEntity.setInvulnerable(true);
        itemEntity.setNeverPickUp();
        itemEntity.setUnlimitedLifetime();
        itemEntity.setDeltaMovement(0, 0, 0);
        itemEntity.addTag(MUSEUM_TAG);
        level.addFreshEntity((Entity) itemEntity);
    }

    // ─── Entity Placement Helpers ───

    private static void placeNameLabel(ServerLevel level, BlockPos pos, String name) {
        ArmorStand stand = new ArmorStand(EntityType.ARMOR_STAND, (Level) level);
        stand.setPos(pos.getX() + 0.5, pos.getY() - 1.0, pos.getZ() + 0.5);
        stand.setCustomName(Component.literal(name));
        stand.setCustomNameVisible(true);
        stand.setInvulnerable(true);
        stand.setNoGravity(true);
        stand.setInvisible(true);
        stand.addTag(MUSEUM_TAG);
        byte flags = stand.getEntityData().get(ArmorStand.DATA_CLIENT_FLAGS);
        stand.getEntityData().set(ArmorStand.DATA_CLIENT_FLAGS, (byte)(flags | 16));
        level.addFreshEntity((Entity) stand);
    }

    // ─── Wildlife Floor & Decoration ───

    private static BlockState getWildlifeFloorBlock(String mobType) {
        String lower = mobType.toLowerCase();
        if (lower.contains("camel") || lower.contains("husk")) return Blocks.SANDSTONE.defaultBlockState();
        if (lower.contains("polar_bear") || lower.contains("stray") || lower.contains("snow_golem")) return Blocks.SNOW_BLOCK.defaultBlockState();
        if (lower.contains("panda") || lower.contains("parrot") || lower.contains("ocelot")) return Blocks.MOSS_BLOCK.defaultBlockState();
        if (lower.contains("blaze") || lower.contains("ghast") || lower.contains("hoglin") || lower.contains("piglin") || lower.contains("magma_cube")) return Blocks.NETHERRACK.defaultBlockState();
        if (lower.contains("enderman") || lower.contains("shulker") || lower.contains("endermite")) return Blocks.END_STONE.defaultBlockState();
        if (lower.contains("cave_spider") || lower.contains("silverfish") || lower.contains("bat")) return Blocks.STONE.defaultBlockState();
        if (lower.contains("guardian") || lower.contains("elder_guardian") || lower.contains("drowned")) return Blocks.PRISMARINE.defaultBlockState();
        return Blocks.GRASS_BLOCK.defaultBlockState();
    }

    private static BlockState getWildlifeDecoration(String mobType) {
        String lower = mobType.toLowerCase();
        if (lower.contains("camel") || lower.contains("husk")) return Blocks.DEAD_BUSH.defaultBlockState();
        if (lower.contains("polar_bear") || lower.contains("stray") || lower.contains("snow_golem")) return Blocks.SNOW.defaultBlockState();
        if (lower.contains("panda") || lower.contains("parrot") || lower.contains("ocelot")) return Blocks.FLOWERING_AZALEA.defaultBlockState();
        if (lower.contains("wolf") || lower.contains("fox") || lower.contains("rabbit")) return Blocks.FERN.defaultBlockState();
        if (lower.contains("blaze") || lower.contains("ghast") || lower.contains("magma_cube")) return Blocks.MAGMA_BLOCK.defaultBlockState();
        if (lower.contains("enderman") || lower.contains("shulker")) return Blocks.CHORUS_FLOWER.defaultBlockState();
        if (lower.contains("cow") || lower.contains("pig") || lower.contains("sheep") || lower.contains("chicken") || lower.contains("bee")) return Blocks.DANDELION.defaultBlockState();
        return Blocks.SHORT_GRASS.defaultBlockState();
    }

    private static BlockState getWildlifeWallBlock(String mobType) {
        String lower = mobType.toLowerCase();
        if (lower.contains("camel") || lower.contains("husk")) return Blocks.SANDSTONE_WALL.defaultBlockState();
        if (lower.contains("polar_bear") || lower.contains("stray") || lower.contains("snow_golem")) return Blocks.COBBLESTONE_WALL.defaultBlockState();
        if (lower.contains("panda") || lower.contains("parrot") || lower.contains("ocelot")) return Blocks.MOSSY_COBBLESTONE_WALL.defaultBlockState();
        if (lower.contains("blaze") || lower.contains("ghast") || lower.contains("hoglin") || lower.contains("piglin") || lower.contains("magma_cube")) return Blocks.NETHER_BRICK_WALL.defaultBlockState();
        if (lower.contains("enderman") || lower.contains("shulker") || lower.contains("endermite")) return Blocks.END_STONE_BRICK_WALL.defaultBlockState();
        if (lower.contains("cave_spider") || lower.contains("silverfish") || lower.contains("bat")) return Blocks.STONE_BRICK_WALL.defaultBlockState();
        return Blocks.OAK_FENCE.defaultBlockState();
    }

    private static BlockState getWildlifeAccentBlock(String mobType) {
        String lower = mobType.toLowerCase();
        if (lower.contains("camel") || lower.contains("husk")) return Blocks.CACTUS.defaultBlockState();
        if (lower.contains("polar_bear") || lower.contains("stray") || lower.contains("snow_golem")) return Blocks.PACKED_ICE.defaultBlockState();
        if (lower.contains("panda") || lower.contains("parrot") || lower.contains("ocelot")) return Blocks.BAMBOO.defaultBlockState();
        if (lower.contains("blaze") || lower.contains("ghast") || lower.contains("magma_cube")) return Blocks.SOUL_LANTERN.defaultBlockState();
        if (lower.contains("enderman") || lower.contains("shulker")) return Blocks.END_ROD.defaultBlockState();
        if (lower.contains("wolf") || lower.contains("fox") || lower.contains("rabbit")) return Blocks.SWEET_BERRY_BUSH.defaultBlockState();
        if (lower.contains("cave_spider") || lower.contains("silverfish") || lower.contains("bat")) return Blocks.COBWEB.defaultBlockState();
        if (lower.contains("pig") || lower.contains("cow") || lower.contains("sheep") || lower.contains("chicken")) return Blocks.HAY_BLOCK.defaultBlockState();
        return Blocks.FLOWER_POT.defaultBlockState();
    }

    private static BlockState getWildlifeFurnitureDecor(String mobType) {
        String lower = mobType.toLowerCase();
        if (lower.contains("camel") || lower.contains("husk")) return FurnitureRegistry.MED_ROCK_1.get().defaultBlockState();
        if (lower.contains("polar_bear") || lower.contains("stray") || lower.contains("snow_golem")) return FurnitureRegistry.MED_ROCK_2.get().defaultBlockState();
        if (lower.contains("panda") || lower.contains("parrot") || lower.contains("ocelot")) return FurnitureRegistry.MED_BROWN_MUSHROOM_PATCH.get().defaultBlockState();
        if (lower.contains("wolf") || lower.contains("fox") || lower.contains("rabbit")) return FurnitureRegistry.MED_LOG_PILE_SMALL_1.get().defaultBlockState();
        if (lower.contains("blaze") || lower.contains("ghast") || lower.contains("magma_cube")) return FurnitureRegistry.MED_CRYSTAL.get().defaultBlockState();
        if (lower.contains("enderman") || lower.contains("shulker")) return FurnitureRegistry.MED_CRYSTAL.get().defaultBlockState();
        if (lower.contains("cave_spider") || lower.contains("silverfish") || lower.contains("bat")) return FurnitureRegistry.MED_GLOW_MUSHROOM_PATCH.get().defaultBlockState();
        if (lower.contains("cow") || lower.contains("pig") || lower.contains("sheep") || lower.contains("chicken") || lower.contains("bee")) return FurnitureRegistry.MED_CLOVERS.get().defaultBlockState();
        return FurnitureRegistry.MED_PEBBLES_1.get().defaultBlockState();
    }

    private static BlockState getWildlifeFurnitureExtra(String mobType) {
        String lower = mobType.toLowerCase();
        if (lower.contains("camel") || lower.contains("husk")) return FurnitureRegistry.MED_PEBBLES_2.get().defaultBlockState();
        if (lower.contains("polar_bear") || lower.contains("stray") || lower.contains("snow_golem")) return FurnitureRegistry.MED_PEBBLES_3.get().defaultBlockState();
        if (lower.contains("panda") || lower.contains("parrot") || lower.contains("ocelot")) return FurnitureRegistry.MED_PLANT.get().defaultBlockState();
        if (lower.contains("wolf") || lower.contains("fox") || lower.contains("rabbit")) return FurnitureRegistry.MED_PEBBLES_1.get().defaultBlockState();
        if (lower.contains("cave_spider") || lower.contains("silverfish") || lower.contains("bat")) return FurnitureRegistry.MED_ROCK_3.get().defaultBlockState();
        if (lower.contains("cow") || lower.contains("pig") || lower.contains("sheep") || lower.contains("chicken")) return FurnitureRegistry.MED_PLANT.get().defaultBlockState();
        return FurnitureRegistry.MED_PEBBLES_2.get().defaultBlockState();
    }

    // ─── Advancement Icon ───

    private static ItemStack getAdvancementIcon(ServerLevel level, String advancementId) {
        // Museum wing completion trophies
        switch (advancementId) {
            case "megamod:museum/master_collector": return Items.NETHER_STAR.getDefaultInstance();
            case "megamod:museum/art_connoisseur": return Items.GOLDEN_APPLE.getDefaultInstance();
            case "megamod:museum/ocean_explorer": return Items.HEART_OF_THE_SEA.getDefaultInstance();
            case "megamod:museum/beast_master": return Items.TOTEM_OF_UNDYING.getDefaultInstance();
            case "megamod:museum/completionist": return Items.DRAGON_EGG.getDefaultInstance();
        }
        // Vanilla advancements
        try {
            Identifier id = Identifier.tryParse(advancementId);
            if (id != null) {
                var holder = level.getServer().getAdvancements().get(id);
                if (holder != null) {
                    var display = holder.value().display();
                    if (display.isPresent()) {
                        return display.get().getIcon();
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return new ItemStack((ItemLike) Items.GOLDEN_APPLE);
    }

    // ─── Filters ───

    private static boolean isNonItemDonation(String itemId) {
        if (itemId.contains("spawn_egg")) return true;
        if (!ItemCatalog.contains(itemId)) return true;
        return false;
    }

    private static Set<String> filterAquatic(Set<String> mobs) {
        HashSet<String> aquatic = new HashSet<>();
        for (String mob : mobs) {
            if (AquariumCatalog.contains(mob)) {
                aquatic.add(mob);
            }
        }
        return aquatic;
    }

    private static Set<String> filterWildlife(Set<String> mobs) {
        HashSet<String> wildlife = new HashSet<>();
        for (String mob : mobs) {
            if (!AquariumCatalog.contains(mob) && WildlifeCatalog.contains(mob)) {
                wildlife.add(mob);
            }
        }
        return wildlife;
    }

    // ─── Item Resolution ───

    private static ItemStack resolveItemStack(String itemId) {
        Identifier id = Identifier.tryParse(itemId);
        if (id == null) return ItemStack.EMPTY;
        return BuiltInRegistries.ITEM.get(id)
                .map(ref -> new ItemStack((ItemLike) ref.value()))
                .orElse(ItemStack.EMPTY);
    }

    private static String getItemDisplayName(ItemStack stack, String fallbackId) {
        if (!stack.isEmpty()) {
            return stack.getHoverName().getString();
        }
        return prettifyMobName(fallbackId);
    }

    // ─── Name Formatting ───

    private static String prettifyMobName(String mobType) {
        String name = mobType;
        int colon = name.indexOf(':');
        if (colon >= 0) name = name.substring(colon + 1);
        name = name.replace('_', ' ');
        StringBuilder sb = new StringBuilder();
        boolean capitalizeNext = true;
        for (char c : name.toCharArray()) {
            if (c == ' ') { sb.append(' '); capitalizeNext = true; }
            else if (capitalizeNext) { sb.append(Character.toUpperCase(c)); capitalizeNext = false; }
            else { sb.append(c); }
        }
        return sb.toString();
    }

    private static String prettifyArtName(String artId) {
        ItemStack paintingItem = MasterpieceRegistry.getItemForVariant(artId);
        if (!paintingItem.isEmpty()) return paintingItem.getHoverName().getString();
        return prettifyMobName(artId);
    }

    private static String simplifyAdvancementId(String advancementId) {
        String name = advancementId;
        int colon = name.indexOf(':');
        if (colon >= 0) name = name.substring(colon + 1);
        int slash = name.lastIndexOf('/');
        if (slash >= 0) name = name.substring(slash + 1);
        return prettifyMobName(":" + name);
    }

    // ─── Mob Size Classification ───

    private static boolean isLargeMob(String mobId) {
        String lower = mobId.toLowerCase();
        return lower.contains("ghast") || lower.contains("elder_guardian")
            || lower.contains("horse") || lower.contains("donkey") || lower.contains("mule")
            || lower.contains("camel") || lower.contains("ravager") || lower.contains("iron_golem")
            || lower.contains("polar_bear") || lower.contains("mooshroom")
            || lower.contains("warden") || lower.contains("hoglin") || lower.contains("zoglin")
            || lower.contains("ender_dragon") || lower.contains("wither")
            || lower.contains("sniffer") || lower.contains("llama") || lower.contains("dolphin");
    }

    private static boolean isBossMob(String mobId) {
        String lower = mobId.toLowerCase();
        return lower.contains("ender_dragon") || lower.contains("wither")
            || lower.contains("elder_guardian") || lower.contains("warden");
    }

    private static List<String> sortBySize(List<String> mobs) {
        List<String> normal = new ArrayList<>();
        List<String> large = new ArrayList<>();
        List<String> boss = new ArrayList<>();
        for (String mob : mobs) {
            if (isBossMob(mob)) {
                boss.add(mob);
            } else if (isLargeMob(mob)) {
                large.add(mob);
            } else {
                normal.add(mob);
            }
        }
        List<String> sorted = new ArrayList<>();
        sorted.addAll(normal);
        sorted.addAll(large);
        sorted.addAll(boss);
        return sorted;
    }
}
