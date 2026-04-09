/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.living.LivingDeathEvent
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$ItemCraftedEvent
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$PlayerLoggedInEvent
 *  net.neoforged.neoforge.event.level.BlockEvent$BreakEvent
 *  net.neoforged.neoforge.event.server.ServerStoppingEvent
 *  net.neoforged.neoforge.event.tick.ServerTickEvent$Post
 *  net.neoforged.neoforge.network.PacketDistributor
 */
package com.ultra.megamod.feature.skills;

import com.ultra.megamod.feature.attributes.AttributeHelper;
import com.ultra.megamod.feature.attributes.MegaModAttributes;
import com.ultra.megamod.feature.skills.SkillAttributeApplier;
import com.ultra.megamod.feature.skills.SkillManager;
import com.ultra.megamod.feature.skills.SkillTreeType;
import com.ultra.megamod.feature.skills.locks.SkillLockManager;
import com.ultra.megamod.feature.skills.capstone.CapstoneManager;
import com.ultra.megamod.feature.skills.network.SkillSyncPayload;
import com.ultra.megamod.feature.skills.prestige.PrestigeManager;
import com.ultra.megamod.feature.skills.synergy.SynergyEffects;
import com.ultra.megamod.feature.skills.synergy.SynergyManager;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.TradeWithVillagerEvent;
import net.neoforged.neoforge.event.enchanting.EnchantmentLevelSetEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid="megamod")
public class SkillEvents {
    private static final Map<UUID, Set<Long>> VISITED_CHUNKS = new HashMap<>();
    private static final Map<UUID, long[]> GLOBAL_XP_TRACKER = new HashMap<>();
    private static final Map<UUID, double[]> RIDE_POSITIONS = new HashMap<>();

    private static final Set<String> DUNGEON_BOSS_TYPES = Set.of(
        "wraith", "ossukage", "dungeon_keeper", "frostmaw",
        "wroughtnaut", "umvuthi", "chaos_spawner", "sculptor"
    );

    /**
     * Apply XP bonus from the player's tree-specific XP bonus attribute.
     * E.g., +10% combat_xp_bonus means 10 XP becomes 11.
     */
    private static int applyXpBonus(ServerPlayer player, SkillTreeType tree, int baseXp) {
        var bonusAttr = switch (tree) {
            case COMBAT -> MegaModAttributes.COMBAT_XP_BONUS;
            case MINING -> MegaModAttributes.MINING_XP_BONUS;
            case FARMING -> MegaModAttributes.FARMING_XP_BONUS;
            case ARCANE -> MegaModAttributes.ARCANE_XP_BONUS;
            case SURVIVAL -> MegaModAttributes.SURVIVAL_XP_BONUS;
        };
        double bonus = AttributeHelper.getValue(player, bonusAttr);
        int xp = baseXp;
        if (bonus > 0) {
            xp = (int) (xp * (1.0 + bonus / 100.0));
        }
        // Global XP event multiplier (set by admin, applies to all players)
        SkillManager skills = SkillManager.get(player.level());
        double globalMult = skills.getAdminXpMultiplier();
        if (globalMult > 1.0) {
            xp = (int) (xp * globalMult);
        }
        // Admin-only XP boost (only for NeverNotch & Dev)
        if (com.ultra.megamod.feature.computer.admin.AdminSystem.isAdmin(player)) {
            double adminBoost = skills.getAdminOnlyXpBoost();
            if (adminBoost > 1.0) {
                xp = (int) (xp * adminBoost);
            }
        }
        return xp;
    }

    private static boolean checkGlobalCap(UUID uuid, int xp, long currentTick) {
        long[] data = GLOBAL_XP_TRACKER.computeIfAbsent(uuid, k -> new long[]{0, currentTick});
        if (currentTick - data[1] > 1200) { // 60 seconds
            data[0] = 0;
            data[1] = currentTick;
        }
        if (data[0] + xp > 750) return false;
        data[0] += xp;
        return true;
    }

    @SubscribeEvent
    public static void onMobKill(LivingDeathEvent event) {
        Entity entity = event.getSource().getEntity();
        if (!(entity instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player = (ServerPlayer)entity;
        if (event.getEntity() instanceof Player) {
            return;
        }
        LivingEntity mob = event.getEntity();
        ServerLevel level = (ServerLevel) player.level();
        SkillManager manager = SkillManager.get(level);
        long currentTick = level.getGameTime();

        // Combat XP
        int xp = SkillEvents.getCombatXp(mob);
        if (xp > 0) {
            if (manager.checkAntiAbuse(player.getUUID(), "combat", xp)
                    && checkGlobalCap(player.getUUID(), xp, currentTick)) {
                int levelsGained = manager.addXp(player.getUUID(), SkillTreeType.COMBAT, applyXpBonus(player, SkillTreeType.COMBAT, xp));
                if (levelsGained > 0) {
                    int newLvl = manager.getLevel(player.getUUID(), SkillTreeType.COMBAT);
                    player.sendSystemMessage(Component.literal("Combat skill leveled up! Now level " + newLvl + " (+1 skill point)").withStyle(ChatFormatting.GREEN));
                    checkMilestones(player, SkillTreeType.COMBAT, newLvl, levelsGained);
                }
            }
        }

        // Dungeon mob Survival + Arcane XP
        Identifier mobId = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        if ("megamod".equals(mobId.getNamespace())) {
            String path = mobId.getPath();
            boolean isBoss = DUNGEON_BOSS_TYPES.contains(path);
            int survivalXp = isBoss ? 100 : 8;
            if (manager.checkAntiAbuse(player.getUUID(), "dungeon", survivalXp)
                    && checkGlobalCap(player.getUUID(), survivalXp, currentTick)) {
                int survLevels = manager.addXp(player.getUUID(), SkillTreeType.SURVIVAL, applyXpBonus(player, SkillTreeType.SURVIVAL, survivalXp));
                if (survLevels > 0) {
                    int survNewLvl = manager.getLevel(player.getUUID(), SkillTreeType.SURVIVAL);
                    player.sendSystemMessage(Component.literal("Survival skill leveled up! Now level " + survNewLvl + " (+1 skill point)").withStyle(ChatFormatting.GREEN));
                    checkMilestones(player, SkillTreeType.SURVIVAL, survNewLvl, survLevels);
                }
            }
            // Dungeon boss kills also grant Arcane XP (dungeon completion)
            if (isBoss) {
                int arcaneXp = 75;
                if (manager.checkAntiAbuse(player.getUUID(), "dungeon_arcane", arcaneXp)
                        && checkGlobalCap(player.getUUID(), arcaneXp, currentTick)) {
                    int arcaneLevels = manager.addXp(player.getUUID(), SkillTreeType.ARCANE, applyXpBonus(player, SkillTreeType.ARCANE, arcaneXp));
                    if (arcaneLevels > 0) {
                        int arcaneNewLvl = manager.getLevel(player.getUUID(), SkillTreeType.ARCANE);
                        player.sendSystemMessage(Component.literal("Arcane skill leveled up! Now level " + arcaneNewLvl + " (+1 skill point)").withStyle(ChatFormatting.GREEN));
                        checkMilestones(player, SkillTreeType.ARCANE, arcaneNewLvl, arcaneLevels);
                    }
                }
            }
        }
    }

    private static int getCombatXp(LivingEntity mob) {
        String typeName;
        return switch (typeName = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).getPath()) {
            case "zombie", "husk", "drowned", "skeleton", "stray", "spider", "cave_spider", "slime", "magma_cube", "silverfish", "phantom", "zombified_piglin" -> 5;
            case "creeper", "witch", "guardian", "piglin_brute", "vindicator", "pillager", "hoglin" -> 10;
            case "enderman", "blaze", "wither_skeleton", "ghast", "shulker" -> 20;
            case "elder_guardian", "ravager", "evoker" -> 35;
            case "warden" -> 40;
            case "wither" -> 45;
            case "ender_dragon" -> 50;
            default -> 0;
        };
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer) player;
        Level level = (Level)event.getLevel();
        if (level.isClientSide()) {
            return;
        }
        Block block = event.getState().getBlock();
        ServerLevel serverLevel = (ServerLevel) player2.level();
        SkillManager manager = SkillManager.get(serverLevel);
        long currentTick = serverLevel.getGameTime();

        // Mining XP
        int xp = SkillEvents.getMiningXp(block);
        if (xp > 0) {
            if (manager.checkAntiAbuse(player2.getUUID(), "mining", xp)
                    && checkGlobalCap(player2.getUUID(), xp, currentTick)) {
                int levelsGained = manager.addXp(player2.getUUID(), SkillTreeType.MINING, applyXpBonus(player2, SkillTreeType.MINING, xp));
                if (levelsGained > 0) {
                    int miningNewLvl = manager.getLevel(player2.getUUID(), SkillTreeType.MINING);
                    player2.sendSystemMessage(Component.literal("Mining skill leveled up! Now level " + miningNewLvl + " (+1 skill point)").withStyle(ChatFormatting.GREEN));
                    checkMilestones(player2, SkillTreeType.MINING, miningNewLvl, levelsGained);
                }
            }
        }

        // Farming XP from crop harvest (only mature crops)
        int farmXp = SkillEvents.getFarmingXp(block, event.getState());
        if (farmXp > 0) {
            if (manager.checkAntiAbuse(player2.getUUID(), "farming", farmXp)
                    && checkGlobalCap(player2.getUUID(), farmXp, currentTick)) {
                int farmLevels = manager.addXp(player2.getUUID(), SkillTreeType.FARMING, applyXpBonus(player2, SkillTreeType.FARMING, farmXp));
                if (farmLevels > 0) {
                    int farmNewLvl = manager.getLevel(player2.getUUID(), SkillTreeType.FARMING);
                    player2.sendSystemMessage(Component.literal("Farming skill leveled up! Now level " + farmNewLvl + " (+1 skill point)").withStyle(ChatFormatting.GREEN));
                    checkMilestones(player2, SkillTreeType.FARMING, farmNewLvl, farmLevels);
                }
            }
        }
    }

    private static int getMiningXp(Block block) {
        if (block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE) {
            return 5;
        }
        if (block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE) {
            return 5;
        }
        if (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE) {
            return 7;
        }
        if (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE) {
            return 10;
        }
        if (block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE) {
            return 8;
        }
        if (block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE) {
            return 7;
        }
        if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE) {
            return 15;
        }
        if (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE) {
            return 12;
        }
        if (block == Blocks.NETHER_QUARTZ_ORE) {
            return 6;
        }
        if (block == Blocks.NETHER_GOLD_ORE) {
            return 8;
        }
        if (block == Blocks.ANCIENT_DEBRIS) {
            return 20;
        }
        return 0;
    }

    private static int getFarmingXp(Block block, net.minecraft.world.level.block.state.BlockState state) {
        // Melon, pumpkin, sugar cane don't have growth stages - always grant XP
        if (block == Blocks.MELON || block == Blocks.PUMPKIN || block == Blocks.SUGAR_CANE) {
            return 5;
        }
        // CropBlock types: only grant XP if mature
        if (block instanceof net.minecraft.world.level.block.CropBlock cropBlock) {
            if (!cropBlock.isMaxAge(state)) return 0;
            return 5;
        }
        // Nether wart: check age 3 (max)
        if (block == Blocks.NETHER_WART) {
            int age = state.getValue(net.minecraft.world.level.block.NetherWartBlock.AGE);
            return age >= 3 ? 6 : 0;
        }
        // Sweet berry bush: check age 3 (max)
        if (block == Blocks.SWEET_BERRY_BUSH) {
            int age = state.getValue(net.minecraft.world.level.block.SweetBerryBushBlock.AGE);
            return age >= 3 ? 4 : 0;
        }
        // Cocoa: check age 2 (max)
        if (block == Blocks.COCOA) {
            int age = state.getValue(net.minecraft.world.level.block.CocoaBlock.AGE);
            return age >= 2 ? 5 : 0;
        }
        return 0;
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer) player;
        ItemStack crafted = event.getCrafting();
        ServerLevel serverLevel = (ServerLevel) player2.level();
        SkillManager manager = SkillManager.get(serverLevel);
        SkillTreeType tree = null;
        int xp = 0;
        if (SkillEvents.isFood(crafted)) {
            tree = SkillTreeType.FARMING;
            xp = 8;
        } else if (SkillEvents.isTool(crafted)) {
            tree = SkillTreeType.MINING;
            xp = 8;
        } else if (SkillEvents.isArmor(crafted)) {
            tree = SkillTreeType.COMBAT;
            xp = 8;
        } else if (SkillEvents.isWeapon(crafted)) {
            tree = SkillTreeType.COMBAT;
            xp = 8;
        } else if (SkillEvents.isArcane(crafted)) {
            tree = SkillTreeType.ARCANE;
            xp = 10;
        } else {
            tree = SkillTreeType.SURVIVAL;
            xp = 3;
        }
        if (!manager.checkAntiAbuse(player2.getUUID(), "craft_" + tree.name().toLowerCase(), xp)) {
            return;
        }
        long currentTick = serverLevel.getGameTime();
        if (!checkGlobalCap(player2.getUUID(), xp, currentTick)) {
            return;
        }
        int levelsGained = manager.addXp(player2.getUUID(), tree, applyXpBonus(player2, tree, xp));
        if (levelsGained > 0) {
            int craftNewLvl = manager.getLevel(player2.getUUID(), tree);
            player2.sendSystemMessage(Component.literal(tree.getDisplayName() + " skill leveled up! Now level " + craftNewLvl + " (+1 skill point)").withStyle(ChatFormatting.GREEN));
            checkMilestones(player2, tree, craftNewLvl, levelsGained);
        }
    }

    private static boolean isFood(ItemStack stack) {
        return stack.getItem() == Items.BREAD || stack.getItem() == Items.CAKE || stack.getItem() == Items.COOKIE || stack.getItem() == Items.PUMPKIN_PIE || stack.getItem() == Items.MUSHROOM_STEW || stack.getItem() == Items.RABBIT_STEW || stack.getItem() == Items.BEETROOT_SOUP || stack.getItem() == Items.GOLDEN_APPLE || stack.getItem() == Items.GOLDEN_CARROT || stack.getItem() == Items.SUSPICIOUS_STEW;
    }

    private static boolean isTool(ItemStack stack) {
        return stack.getItem() == Items.WOODEN_PICKAXE || stack.getItem() == Items.STONE_PICKAXE || stack.getItem() == Items.IRON_PICKAXE || stack.getItem() == Items.GOLDEN_PICKAXE || stack.getItem() == Items.DIAMOND_PICKAXE || stack.getItem() == Items.NETHERITE_PICKAXE || stack.getItem() == Items.WOODEN_SHOVEL || stack.getItem() == Items.STONE_SHOVEL || stack.getItem() == Items.IRON_SHOVEL || stack.getItem() == Items.GOLDEN_SHOVEL || stack.getItem() == Items.DIAMOND_SHOVEL || stack.getItem() == Items.NETHERITE_SHOVEL || stack.getItem() == Items.WOODEN_AXE || stack.getItem() == Items.STONE_AXE || stack.getItem() == Items.IRON_AXE || stack.getItem() == Items.GOLDEN_AXE || stack.getItem() == Items.DIAMOND_AXE || stack.getItem() == Items.NETHERITE_AXE || stack.getItem() == Items.WOODEN_HOE || stack.getItem() == Items.STONE_HOE || stack.getItem() == Items.IRON_HOE || stack.getItem() == Items.GOLDEN_HOE || stack.getItem() == Items.DIAMOND_HOE || stack.getItem() == Items.NETHERITE_HOE;
    }

    private static boolean isArmor(ItemStack stack) {
        return stack.getItem() == Items.LEATHER_HELMET || stack.getItem() == Items.LEATHER_CHESTPLATE || stack.getItem() == Items.LEATHER_LEGGINGS || stack.getItem() == Items.LEATHER_BOOTS || stack.getItem() == Items.CHAINMAIL_HELMET || stack.getItem() == Items.CHAINMAIL_CHESTPLATE || stack.getItem() == Items.CHAINMAIL_LEGGINGS || stack.getItem() == Items.CHAINMAIL_BOOTS || stack.getItem() == Items.IRON_HELMET || stack.getItem() == Items.IRON_CHESTPLATE || stack.getItem() == Items.IRON_LEGGINGS || stack.getItem() == Items.IRON_BOOTS || stack.getItem() == Items.GOLDEN_HELMET || stack.getItem() == Items.GOLDEN_CHESTPLATE || stack.getItem() == Items.GOLDEN_LEGGINGS || stack.getItem() == Items.GOLDEN_BOOTS || stack.getItem() == Items.DIAMOND_HELMET || stack.getItem() == Items.DIAMOND_CHESTPLATE || stack.getItem() == Items.DIAMOND_LEGGINGS || stack.getItem() == Items.DIAMOND_BOOTS || stack.getItem() == Items.NETHERITE_HELMET || stack.getItem() == Items.NETHERITE_CHESTPLATE || stack.getItem() == Items.NETHERITE_LEGGINGS || stack.getItem() == Items.NETHERITE_BOOTS;
    }

    private static boolean isWeapon(ItemStack stack) {
        return stack.getItem() == Items.WOODEN_SWORD || stack.getItem() == Items.STONE_SWORD || stack.getItem() == Items.IRON_SWORD || stack.getItem() == Items.GOLDEN_SWORD || stack.getItem() == Items.DIAMOND_SWORD || stack.getItem() == Items.NETHERITE_SWORD || stack.getItem() == Items.BOW || stack.getItem() == Items.CROSSBOW || stack.getItem() == Items.TRIDENT || stack.getItem() == Items.MACE;
    }

    private static boolean isArcane(ItemStack stack) {
        return stack.getItem() == Items.ENCHANTING_TABLE || stack.getItem() == Items.BREWING_STAND || stack.getItem() == Items.ENDER_EYE || stack.getItem() == Items.ENDER_CHEST || stack.getItem() == Items.BEACON || stack.getItem() == Items.CONDUIT || stack.getItem() == Items.END_CRYSTAL;
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer) player;
        // Wire up prestige checker for 3rd branch unlock
        ServerLevel sl = (ServerLevel) player2.level();
        SkillManager.setPrestigeChecker((uuid, tree) ->
                PrestigeManager.get(sl.getServer().overworld()).hasThirdBranchUnlock(uuid, tree));
        SkillAttributeApplier.recalculate(player2);
        SkillEvents.syncToClient(player2);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        // Skill attribute modifiers are transient and lost when the player entity
        // is recreated on respawn. Reapply them from the persisted skill data.
        if (event.getEntity() instanceof ServerPlayer sp) {
            SkillAttributeApplier.recalculate(sp);
            SkillEvents.syncToClient(sp);
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        ServerLevel overworld = event.getServer().overworld();
        long gameTime = overworld.getGameTime();
        if (gameTime % 1200L == 0L) {
            SkillManager.get(overworld).saveToDisk(overworld);
            PrestigeManager.get(overworld).saveToDisk(overworld);
        }
        if (gameTime % 100L == 0L) {
            for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
                checkChunkExploration(player);
                checkRidingDistance(player);
                // Periodic skill data sync so client XP bar / points stay up-to-date
                syncToClient(player);
            }
        }
    }

    private static void checkChunkExploration(ServerPlayer player) {
        int chunkX = player.blockPosition().getX() >> 4;
        int chunkZ = player.blockPosition().getZ() >> 4;
        long chunkKey = ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
        Set<Long> visited = VISITED_CHUNKS.computeIfAbsent(player.getUUID(), k -> new HashSet<>());
        if (visited.add(chunkKey)) {
            ServerLevel level = (ServerLevel) player.level();
            SkillManager manager = SkillManager.get(level);
            if (!manager.checkAntiAbuse(player.getUUID(), "exploration", 2)) return;
            long currentTick = level.getGameTime();
            if (!checkGlobalCap(player.getUUID(), 2, currentTick)) return;
            int levelsGained = manager.addXp(player.getUUID(), SkillTreeType.SURVIVAL, applyXpBonus(player, SkillTreeType.SURVIVAL, 2));
            level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.3f, 1.8f);
            if (levelsGained > 0) {
                int explNewLvl = manager.getLevel(player.getUUID(), SkillTreeType.SURVIVAL);
                player.sendSystemMessage(Component.literal("Survival skill leveled up! Now level " + explNewLvl + " (+1 skill point)").withStyle(ChatFormatting.GREEN));
                checkMilestones(player, SkillTreeType.SURVIVAL, explNewLvl, levelsGained);
            }
        }
    }

    private static void checkRidingDistance(ServerPlayer player) {
        if (!player.isPassenger()) {
            RIDE_POSITIONS.remove(player.getUUID());
            return;
        }
        // Check if riding a boat/raft or horse/donkey/mule
        String vehicleType = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE
                .getKey(player.getVehicle().getType()).getPath();
        boolean isBoat = vehicleType.contains("boat") || vehicleType.contains("raft");
        boolean isHorse = vehicleType.equals("horse") || vehicleType.equals("donkey") || vehicleType.equals("mule");
        if (!isBoat && !isHorse) {
            RIDE_POSITIONS.remove(player.getUUID());
            return;
        }
        double px = player.getX();
        double pz = player.getZ();
        UUID uuid = player.getUUID();
        double[] lastPos = RIDE_POSITIONS.get(uuid);
        if (lastPos != null) {
            double dist = Math.sqrt((px - lastPos[0]) * (px - lastPos[0]) + (pz - lastPos[1]) * (pz - lastPos[1]));
            // Grant 1 Survival XP per 30 blocks traveled by boat/horse
            String abuseKey = isBoat ? "boat" : "horse";
            if (dist >= 30.0) {
                ServerLevel level = (ServerLevel) player.level();
                SkillManager manager = SkillManager.get(level);
                int xp = (int) (dist / 30.0);
                long currentTick = level.getGameTime();
                if (manager.checkAntiAbuse(uuid, abuseKey, xp) && checkGlobalCap(uuid, xp, currentTick)) {
                    manager.addXp(uuid, SkillTreeType.SURVIVAL, applyXpBonus(player, SkillTreeType.SURVIVAL, xp));
                }
                RIDE_POSITIONS.put(uuid, new double[]{px, pz});
            }
        } else {
            RIDE_POSITIONS.put(uuid, new double[]{px, pz});
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID uuid = event.getEntity().getUUID();
        VISITED_CHUNKS.remove(uuid);
        GLOBAL_XP_TRACKER.remove(uuid);
        RIDE_POSITIONS.remove(uuid);
        SynergyEffects.clearPlayer(uuid);
        CapstoneManager.onPlayerLogout(uuid);
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ServerLevel overworld = event.getServer().overworld();
        SkillManager.get(overworld).saveToDisk(overworld);
        PrestigeManager.get(overworld).saveToDisk(overworld);
        SkillManager.reset();
        PrestigeManager.reset();
        VISITED_CHUNKS.clear();
        GLOBAL_XP_TRACKER.clear();
        RIDE_POSITIONS.clear();
        CapstoneManager.clearAll();
    }

    @SubscribeEvent
    public static void onFishing(ItemFishedEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer serverPlayer = (ServerPlayer) player;
        ServerLevel level = (ServerLevel) serverPlayer.level();
        SkillManager manager = SkillManager.get(level);
        // Treasure catches (non-fish items) grant more XP
        boolean isTreasure = false;
        for (ItemStack drop : event.getDrops()) {
            if (drop.getItem() != Items.COD && drop.getItem() != Items.SALMON
                    && drop.getItem() != Items.TROPICAL_FISH && drop.getItem() != Items.PUFFERFISH) {
                isTreasure = true;
                break;
            }
        }
        int xp = isTreasure ? 10 : 5;
        if (!manager.checkAntiAbuse(serverPlayer.getUUID(), "fishing", xp)) {
            return;
        }
        long currentTick = level.getGameTime();
        if (!checkGlobalCap(serverPlayer.getUUID(), xp, currentTick)) {
            return;
        }
        int levelsGained = manager.addXp(serverPlayer.getUUID(), SkillTreeType.FARMING, applyXpBonus(serverPlayer, SkillTreeType.FARMING, xp));
        if (levelsGained > 0) {
            int fishNewLvl = manager.getLevel(serverPlayer.getUUID(), SkillTreeType.FARMING);
            serverPlayer.sendSystemMessage(Component.literal("Farming skill leveled up! Now level " + fishNewLvl + " (+1 skill point)").withStyle(ChatFormatting.GREEN));
            checkMilestones(serverPlayer, SkillTreeType.FARMING, fishNewLvl, levelsGained);
        }
    }

    @SubscribeEvent
    public static void onAnimalBreeding(BabyEntitySpawnEvent event) {
        Player player = event.getCausedByPlayer();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer serverPlayer = (ServerPlayer) player;
        ServerLevel level = (ServerLevel) serverPlayer.level();
        SkillManager manager = SkillManager.get(level);
        int xp = 5;
        if (!manager.checkAntiAbuse(serverPlayer.getUUID(), "breeding", xp)) {
            return;
        }
        long currentTick = level.getGameTime();
        if (!checkGlobalCap(serverPlayer.getUUID(), xp, currentTick)) {
            return;
        }
        int levelsGained = manager.addXp(serverPlayer.getUUID(), SkillTreeType.FARMING, applyXpBonus(serverPlayer, SkillTreeType.FARMING, xp));
        if (levelsGained > 0) {
            int breedNewLvl = manager.getLevel(serverPlayer.getUUID(), SkillTreeType.FARMING);
            serverPlayer.sendSystemMessage(Component.literal("Farming skill leveled up! Now level " + breedNewLvl + " (+1 skill point)").withStyle(ChatFormatting.GREEN));
            checkMilestones(serverPlayer, SkillTreeType.FARMING, breedNewLvl, levelsGained);
        }
    }

    @SubscribeEvent
    public static void onEnchanting(EnchantmentLevelSetEvent event) {
        Level eventLevel = event.getLevel();
        if (eventLevel.isClientSide()) return;
        // EnchantmentLevelSetEvent has no player accessor; find nearest player to the enchanting table
        Player player = eventLevel.getNearestPlayer(event.getPos().getX() + 0.5, event.getPos().getY() + 0.5, event.getPos().getZ() + 0.5, 5.0, false);
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer serverPlayer = (ServerPlayer) player;
        ServerLevel level = (ServerLevel) serverPlayer.level();
        SkillManager manager = SkillManager.get(level);
        // Enchanter cost reduction: increase enchant power (better enchants for same XP)
        int enchantLevel = event.getEnchantLevel();
        SkillManager mgr = SkillManager.get(level);
        if (mgr.isNodeUnlocked(serverPlayer.getUUID(), "enchanter_5")) {
            event.setEnchantLevel(enchantLevel + 3); // 30% more power (~3 extra bookshelves)
        } else if (mgr.isNodeUnlocked(serverPlayer.getUUID(), "enchanter_3")) {
            event.setEnchantLevel(enchantLevel + 2); // 20% more power
        } else if (mgr.isNodeUnlocked(serverPlayer.getUUID(), "enchanter_2")) {
            event.setEnchantLevel(enchantLevel + 1); // 10% more power
        }
        int xp = 5 + enchantLevel * 3;
        if (!manager.checkAntiAbuse(serverPlayer.getUUID(), "enchanting", xp)) {
            return;
        }
        long currentTick = level.getGameTime();
        if (!checkGlobalCap(serverPlayer.getUUID(), xp, currentTick)) {
            return;
        }
        int levelsGained = manager.addXp(serverPlayer.getUUID(), SkillTreeType.ARCANE, applyXpBonus(serverPlayer, SkillTreeType.ARCANE, xp));
        if (levelsGained > 0) {
            int enchNewLvl = manager.getLevel(serverPlayer.getUUID(), SkillTreeType.ARCANE);
            serverPlayer.sendSystemMessage(Component.literal("Arcane skill leveled up! Now level " + enchNewLvl + " (+1 skill point)").withStyle(ChatFormatting.GREEN));
            checkMilestones(serverPlayer, SkillTreeType.ARCANE, enchNewLvl, levelsGained);
        }
    }

    // ==================== New XP Sources ====================

    @SubscribeEvent
    public static void onSleepFinished(net.neoforged.neoforge.event.level.SleepFinishedTimeEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        for (ServerPlayer player : serverLevel.getServer().getPlayerList().getPlayers()) {
            if (!player.isSleeping() && player.getSleepTimer() > 0) continue;
            SkillManager manager = SkillManager.get(serverLevel);
            long currentTick = serverLevel.getGameTime();
            if (manager.checkAntiAbuse(player.getUUID(), "sleep", 5)
                    && checkGlobalCap(player.getUUID(), 5, currentTick)) {
                int levelsGained = manager.addXp(player.getUUID(), SkillTreeType.SURVIVAL, applyXpBonus(player, SkillTreeType.SURVIVAL, 5));
                if (levelsGained > 0) {
                    int newLvl = manager.getLevel(player.getUUID(), SkillTreeType.SURVIVAL);
                    player.sendSystemMessage(Component.literal("Survival skill leveled up! Now level " + newLvl + " (+1 skill point)").withStyle(ChatFormatting.GREEN));
                    checkMilestones(player, SkillTreeType.SURVIVAL, newLvl, levelsGained);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onBigDamageSurvival(net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        float damage = event.getOriginalDamage();
        if (damage < 6.0f) return; // Only big hits (3+ hearts)
        if (player.getHealth() <= 0) return; // Must survive
        ServerLevel level = (ServerLevel) player.level();
        SkillManager manager = SkillManager.get(level);
        int xp = Math.min((int) (damage / 2), 10); // 3-10 XP based on damage
        long currentTick = level.getGameTime();
        if (manager.checkAntiAbuse(player.getUUID(), "damage_survival", xp)
                && checkGlobalCap(player.getUUID(), xp, currentTick)) {
            manager.addXp(player.getUUID(), SkillTreeType.SURVIVAL, applyXpBonus(player, SkillTreeType.SURVIVAL, xp));
        }
    }

    @SubscribeEvent
    public static void onVillagerTrade(TradeWithVillagerEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer serverPlayer = (ServerPlayer) player;
        ServerLevel level = (ServerLevel) serverPlayer.level();
        SkillManager manager = SkillManager.get(level);
        int xp = 4;
        if (!manager.checkAntiAbuse(serverPlayer.getUUID(), "trading", xp)) {
            return;
        }
        long currentTick = level.getGameTime();
        if (!checkGlobalCap(serverPlayer.getUUID(), xp, currentTick)) {
            return;
        }
        int levelsGained = manager.addXp(serverPlayer.getUUID(), SkillTreeType.SURVIVAL, applyXpBonus(serverPlayer, SkillTreeType.SURVIVAL, xp));
        if (levelsGained > 0) {
            int tradeNewLvl = manager.getLevel(serverPlayer.getUUID(), SkillTreeType.SURVIVAL);
            serverPlayer.sendSystemMessage(Component.literal("Survival skill leveled up! Now level " + tradeNewLvl + " (+1 skill point)").withStyle(ChatFormatting.GREEN));
            checkMilestones(serverPlayer, SkillTreeType.SURVIVAL, tradeNewLvl, levelsGained);
        }
    }

    // ==================== New Arcane & Survival XP Sources ====================

    @SubscribeEvent
    public static void onBrew(net.neoforged.neoforge.event.brewing.PlayerBrewedPotionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerLevel level = (ServerLevel) player.level();
        SkillManager manager = SkillManager.get(level);
        int xp = 8;
        if (!manager.checkAntiAbuse(player.getUUID(), "brewing", xp)) return;
        long currentTick = level.getGameTime();
        if (!checkGlobalCap(player.getUUID(), xp, currentTick)) return;
        int levelsGained = manager.addXp(player.getUUID(), SkillTreeType.ARCANE, applyXpBonus(player, SkillTreeType.ARCANE, xp));
        if (levelsGained > 0) {
            int newLvl = manager.getLevel(player.getUUID(), SkillTreeType.ARCANE);
            player.sendSystemMessage(Component.literal("Arcane skill leveled up! Now level " + newLvl + " (+1 skill point)").withStyle(ChatFormatting.GREEN));
            checkMilestones(player, SkillTreeType.ARCANE, newLvl, levelsGained);
        }
    }

    /**
     * Awards Arcane XP when a player takes the result from an anvil (repair/combine/rename).
     */
    @SubscribeEvent
    public static void onAnvilCraft(net.neoforged.neoforge.event.entity.player.AnvilCraftEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerLevel level = (ServerLevel) player.level();
        SkillManager manager = SkillManager.get(level);
        int xp = 5;
        if (!manager.checkAntiAbuse(player.getUUID(), "anvil", xp)) return;
        long currentTick = level.getGameTime();
        if (!checkGlobalCap(player.getUUID(), xp, currentTick)) return;
        int levelsGained = manager.addXp(player.getUUID(), SkillTreeType.ARCANE, applyXpBonus(player, SkillTreeType.ARCANE, xp));
        if (levelsGained > 0) {
            int newLvl = manager.getLevel(player.getUUID(), SkillTreeType.ARCANE);
            player.sendSystemMessage(Component.literal("Arcane skill leveled up! Now level " + newLvl + " (+1 skill point)").withStyle(ChatFormatting.GREEN));
            checkMilestones(player, SkillTreeType.ARCANE, newLvl, levelsGained);
        }
    }

    /** Awards Arcane XP when a relic ability is successfully cast. Called from AbilityCastHandler. */
    public static void onAbilityCast(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        SkillManager manager = SkillManager.get(level);
        int xp = 3;
        if (!manager.checkAntiAbuse(player.getUUID(), "ability_cast", xp)) return;
        long currentTick = level.getGameTime();
        if (!checkGlobalCap(player.getUUID(), xp, currentTick)) return;
        manager.addXp(player.getUUID(), SkillTreeType.ARCANE, applyXpBonus(player, SkillTreeType.ARCANE, xp));
    }

    @SubscribeEvent
    public static void onEat(net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack stack = event.getItem();
        if (!stack.has(net.minecraft.core.component.DataComponents.FOOD)) return;
        ServerLevel level = (ServerLevel) player.level();
        SkillManager manager = SkillManager.get(level);
        int xp = 1;
        if (!manager.checkAntiAbuse(player.getUUID(), "eating", xp)) return;
        long currentTick = level.getGameTime();
        if (!checkGlobalCap(player.getUUID(), xp, currentTick)) return;
        manager.addXp(player.getUUID(), SkillTreeType.SURVIVAL, applyXpBonus(player, SkillTreeType.SURVIVAL, xp));
    }

    @SubscribeEvent
    public static void onTameAnimal(net.neoforged.neoforge.event.entity.living.AnimalTameEvent event) {
        if (!(event.getTamer() instanceof ServerPlayer player)) return;
        ServerLevel level = (ServerLevel) player.level();
        SkillManager manager = SkillManager.get(level);
        int xp = 10;
        if (!manager.checkAntiAbuse(player.getUUID(), "taming", xp)) return;
        long currentTick = level.getGameTime();
        if (!checkGlobalCap(player.getUUID(), xp, currentTick)) return;
        int levelsGained = manager.addXp(player.getUUID(), SkillTreeType.SURVIVAL, applyXpBonus(player, SkillTreeType.SURVIVAL, xp));
        if (levelsGained > 0) {
            int newLvl = manager.getLevel(player.getUUID(), SkillTreeType.SURVIVAL);
            player.sendSystemMessage(Component.literal("Survival skill leveled up! Now level " + newLvl + " (+1 skill point)").withStyle(ChatFormatting.GREEN));
            checkMilestones(player, SkillTreeType.SURVIVAL, newLvl, levelsGained);
        }
    }

    private static void checkMilestones(ServerPlayer player, SkillTreeType tree, int newLevel, int levelsGained) {
        if (levelsGained <= 0) return;
        // Recalculate attributes (passive mastery depends on unspent points which change on level-up)
        SkillAttributeApplier.recalculate(player);
        // Immediately sync updated XP/level/points to client on level-up
        syncToClient(player);
        ServerLevel level = (ServerLevel) player.level();
        // Check if a milestone was crossed
        int oldLevel = newLevel - levelsGained;
        // Level 10 milestone
        if (oldLevel < 10 && newLevel >= 10) {
            player.connection.send(new ClientboundSetTitleTextPacket(Component.literal("Novice " + tree.getDisplayName() + "!").withStyle(ChatFormatting.GREEN)));
            player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal("+25 MegaCoins").withStyle(ChatFormatting.GOLD)));
            EconomyManager.get(level.getServer().overworld()).addWallet(player.getUUID(), 25);
            player.sendSystemMessage(Component.literal("Milestone: Novice " + tree.getDisplayName() + "! +25 MegaCoins").withStyle(ChatFormatting.GOLD));
        }
        // Level 25 milestone
        if (oldLevel < 25 && newLevel >= 25) {
            player.connection.send(new ClientboundSetTitleTextPacket(Component.literal("Expert " + tree.getDisplayName() + "!").withStyle(ChatFormatting.AQUA)));
            player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal("+75 MegaCoins").withStyle(ChatFormatting.GOLD)));
            EconomyManager.get(level.getServer().overworld()).addWallet(player.getUUID(), 75);
            player.sendSystemMessage(Component.literal("Milestone: Expert " + tree.getDisplayName() + "! +75 MegaCoins").withStyle(ChatFormatting.GOLD));
        }
        // Level 50 milestone
        if (oldLevel < 50 && newLevel >= 50) {
            player.connection.send(new ClientboundSetTitleTextPacket(Component.literal("Master " + tree.getDisplayName() + "!").withStyle(ChatFormatting.GOLD)));
            player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal("+250 MegaCoins").withStyle(ChatFormatting.GOLD)));
            EconomyManager.get(level.getServer().overworld()).addWallet(player.getUUID(), 250);
            level.playSound(null, player.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0f, 1.0f);
            player.sendSystemMessage(Component.literal("Milestone: Master " + tree.getDisplayName() + "! +250 MegaCoins").withStyle(ChatFormatting.GOLD));
        }

        // ── Spell unlock notifications ──
        // When the player crosses a spell tier threshold, notify them of newly available spells
        checkSpellUnlockThresholds(player, level, oldLevel, newLevel);
    }

    /**
     * Checks if the player crossed a spell unlock threshold (level 5, 15, or 25)
     * and sends a notification with the enchanting table sound, title, and chat listing.
     */
    private static void checkSpellUnlockThresholds(ServerPlayer player, ServerLevel level, int oldLevel, int newLevel) {
        // T2 spells unlock at level 5
        if (oldLevel < 5 && newLevel >= 5) {
            notifySpellUnlocks(player, level, 2);
        }
        // T3 spells unlock at level 15
        if (oldLevel < 15 && newLevel >= 15) {
            notifySpellUnlocks(player, level, 3);
        }
        // T4 spells unlock at level 25
        if (oldLevel < 25 && newLevel >= 25) {
            notifySpellUnlocks(player, level, 4);
        }
    }

    /**
     * Sends a spell unlock notification for the given tier.
     * Plays enchanting table sound, shows title/subtitle, and lists newly unlocked spells in chat.
     */
    private static void notifySpellUnlocks(ServerPlayer player, ServerLevel level, int tier) {
        // Determine which class the player has selected
        com.ultra.megamod.feature.combat.PlayerClassManager pcm = com.ultra.megamod.feature.combat.PlayerClassManager.get(level);
        com.ultra.megamod.feature.combat.PlayerClassManager.PlayerClass playerClass = pcm.getPlayerClass(player.getUUID());
        if (playerClass == com.ultra.megamod.feature.combat.PlayerClassManager.PlayerClass.NONE) return;

        // Collect spells at the given tier that match the player's class
        String className = playerClass.name();
        java.util.List<com.ultra.megamod.feature.combat.spell.SpellDefinition> newSpells = new java.util.ArrayList<>();
        for (com.ultra.megamod.feature.combat.spell.SpellDefinition spell : com.ultra.megamod.feature.combat.spell.SpellRegistry.ALL_SPELLS.values()) {
            if (spell.tier() == tier && className.equalsIgnoreCase(spell.classRequirement())) {
                newSpells.add(spell);
            }
        }

        if (newSpells.isEmpty()) return;

        // Play enchanting table ambient sound
        level.playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0f, 1.0f);

        // Show title/subtitle
        player.connection.send(new ClientboundSetTitleTextPacket(
                Component.literal("New Spells Unlocked!").withStyle(ChatFormatting.LIGHT_PURPLE)));
        player.connection.send(new ClientboundSetSubtitleTextPacket(
                Component.literal("Check your spell book").withStyle(ChatFormatting.GRAY)));

        // Chat message listing the spells
        player.sendSystemMessage(Component.literal("--- Tier " + tier + " Spells Unlocked ---").withStyle(ChatFormatting.LIGHT_PURPLE));
        for (com.ultra.megamod.feature.combat.spell.SpellDefinition spell : newSpells) {
            player.sendSystemMessage(Component.literal("  \u2726 " + spell.name() + " (" + spell.school().displayName + ")").withStyle(ChatFormatting.AQUA));
        }
        player.sendSystemMessage(Component.literal("Use your wand or staff to cast these new spells!").withStyle(ChatFormatting.GRAY));
    }

    public static void syncToClient(ServerPlayer player) {
        SkillManager manager = SkillManager.get((ServerLevel) player.level());
        String json = SkillEvents.buildSyncJson(player.getUUID(), manager, player);
        PacketDistributor.sendToPlayer(player, new SkillSyncPayload(json));
    }

    private static String buildSyncJson(UUID uuid, SkillManager manager, ServerPlayer requester) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"levels\":{");
        boolean first = true;
        for (SkillTreeType type : SkillTreeType.values()) {
            if (!first) {
                sb.append(",");
            }
            sb.append("\"").append(type.name()).append("\":").append(manager.getLevel(uuid, type));
            first = false;
        }
        sb.append("},");
        sb.append("\"xp\":{");
        first = true;
        for (SkillTreeType type : SkillTreeType.values()) {
            if (!first) {
                sb.append(",");
            }
            sb.append("\"").append(type.name()).append("\":").append(manager.getXp(uuid, type));
            first = false;
        }
        sb.append("},");
        sb.append("\"points\":").append(manager.getAvailablePoints(uuid)).append(",");
        sb.append("\"tree_points\":{");
        first = true;
        for (SkillTreeType type : SkillTreeType.values()) {
            if (!first) {
                sb.append(",");
            }
            sb.append("\"").append(type.name()).append("\":").append(manager.getAvailablePoints(uuid, type));
            first = false;
        }
        sb.append("},");
        sb.append("\"nodes\":[");
        first = true;
        for (String nodeId : manager.getUnlockedNodes(uuid)) {
            if (!first) {
                sb.append(",");
            }
            sb.append("\"").append(nodeId).append("\"");
            first = false;
        }
        sb.append("],");
        // Active cross-branch synergies
        sb.append("\"synergies\":[");
        Set<String> unlocked = manager.getUnlockedNodes(uuid);
        first = true;
        for (SynergyManager.Synergy syn : SynergyManager.getActiveSynergies(unlocked)) {
            if (!first) {
                sb.append(",");
            }
            sb.append("\"").append(syn.id()).append("\"");
            first = false;
        }
        sb.append("],");
        // Leaderboard: top 10 players by combined level
        sb.append("\"leaderboard\":[");
        Map<UUID, SkillManager.PlayerSkillData> allData = manager.getAllPlayerData();
        java.util.List<Map.Entry<UUID, Integer>> ranked = new java.util.ArrayList<>();
        for (Map.Entry<UUID, SkillManager.PlayerSkillData> entry : allData.entrySet()) {
            int total = 0;
            for (SkillTreeType t : SkillTreeType.values()) {
                total += entry.getValue().getLevel(t);
            }
            ranked.add(Map.entry(entry.getKey(), total));
        }
        ranked.sort(java.util.Comparator.<Map.Entry<UUID, Integer>, Integer>comparing(Map.Entry::getValue).reversed());
        int count = Math.min(10, ranked.size());
        for (int i = 0; i < count; i++) {
            Map.Entry<UUID, Integer> entry = ranked.get(i);
            UUID entryUuid = entry.getKey();
            int totalLvl = entry.getValue();
            String name = "Unknown";
            ServerPlayer online = requester.level().getServer().getPlayerList().getPlayer(entryUuid);
            if (online != null) {
                name = online.getGameProfile().name();
            } else {
                name = entryUuid.toString().substring(0, 8);
            }
            if (i > 0) sb.append(",");
            sb.append("{\"name\":\"").append(name).append("\",\"level\":").append(totalLvl).append("}");
        }
        sb.append("],");
        // Admin lock bypass flag for client tooltip display
        boolean adminBypass = SkillLockManager.isAdminBypassing(requester);
        sb.append("\"admin_lock_bypass\":").append(adminBypass).append(",");
        // Per-tree prestige levels
        ServerLevel ow = requester.level().getServer().overworld();
        PrestigeManager prestigeMgr = PrestigeManager.get(ow);
        sb.append("\"prestige\":{");
        first = true;
        for (SkillTreeType type : SkillTreeType.values()) {
            if (!first) sb.append(",");
            sb.append("\"").append(type.name()).append("\":").append(prestigeMgr.getPrestigeLevel(uuid, type));
            first = false;
        }
        sb.append("},");
        // Computed skill bonuses for overlay display (includes diminishing returns, prestige, mastery)
        sb.append("\"skill_bonuses\":{");
        Map<String, Double> bonuses = SkillAttributeApplier.getComputedBonuses(uuid);
        first = true;
        for (Map.Entry<String, Double> b : bonuses.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(b.getKey()).append("\":").append(b.getValue());
            first = false;
        }
        sb.append("}");
        sb.append("}");
        return sb.toString();
    }
}

