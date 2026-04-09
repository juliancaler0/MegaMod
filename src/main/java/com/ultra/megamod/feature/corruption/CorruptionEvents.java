package com.ultra.megamod.feature.corruption;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.corruption.CorruptionManager.CorruptionZone;
import com.ultra.megamod.feature.toggles.FeatureToggleManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import com.ultra.megamod.feature.corruption.network.CorruptionSyncPayload;
import com.ultra.megamod.feature.corruption.network.CorruptionZoneSyncPayload;

import java.util.*;

@EventBusSubscriber(modid = MegaMod.MODID)
public class CorruptionEvents {

    private static final Random RANDOM = new Random();
    private static long lastSaveTick = 0;
    private static final long SAVE_INTERVAL = 6000; // auto-save every 5 minutes

    // Track last action bar message time per player (to avoid spam)
    private static final Map<UUID, Long> lastActionBarTime = new HashMap<>();
    private static final long ACTION_BAR_COOLDOWN = 60; // ticks between action bar messages

    // Track last debuff application per player
    private static final Map<UUID, Long> lastDebuffTime = new HashMap<>();
    private static final long DEBUFF_INTERVAL = 100; // apply debuffs every 5 seconds (100 ticks)

    // Track last ambient damage per player
    private static final Map<UUID, Long> lastAmbientDamageTime = new HashMap<>();
    private static final long AMBIENT_DAMAGE_INTERVAL = 100; // every 5 seconds

    // Track last corruption sync packet time per player
    private static final Map<UUID, Long> lastSyncTime = new HashMap<>();
    private static final long SYNC_INTERVAL = 100; // send sync every 5 seconds

    // Track last zone boundary sync time per player (less frequent than strength sync)
    private static final Map<UUID, Long> lastZoneSyncTime = new HashMap<>();
    private static final long ZONE_SYNC_INTERVAL = 200; // send zone boundaries every 10 seconds
    private static final int ZONE_SYNC_RANGE = 128; // sync zones within 128 blocks of the player

    // Track last mob spawn per zone
    private static final Map<Integer, Long> lastZoneMobSpawnTime = new HashMap<>();
    private static final long ZONE_MOB_SPAWN_INTERVAL = 400; // every 20 seconds per zone

    // ---- Server Lifecycle ----

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        ServerLevel level = event.getServer().overworld();
        CorruptionManager.get(level);
        PurgeManager.get(level);
        long tick = event.getServer().getTickCount();
        CorruptionSpreadHandler.resetTicks(tick);
        lastSaveTick = tick;
        lastActionBarTime.clear();
        lastDebuffTime.clear();
        lastAmbientDamageTime.clear();
        lastSyncTime.clear();
        lastZoneSyncTime.clear();
        lastZoneMobSpawnTime.clear();
        MegaMod.LOGGER.info("Corruption system loaded: {} active zones", CorruptionManager.get(level).getActiveZoneCount());
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ServerLevel level = event.getServer().overworld();
        CorruptionManager.get(level).saveToDisk(level);
        PurgeManager.get(level).saveToDisk(level);
        CorruptionManager.reset();
        PurgeManager.reset();
        lastActionBarTime.clear();
        lastDebuffTime.clear();
        lastAmbientDamageTime.clear();
        lastSyncTime.clear();
        lastZoneSyncTime.clear();
        lastZoneMobSpawnTime.clear();
    }

    // ---- Server Tick ----

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        ServerLevel level = event.getServer().overworld();
        if (!FeatureToggleManager.get(level).isEnabled("corruption")) return;

        long tick = level.getServer().getTickCount();

        // Run spread handler
        CorruptionSpreadHandler.tick(level);

        // Run purge tick
        PurgeManager.get(level).tick(level);

        // Corruption zone mob spawning
        processZoneMobSpawning(level, tick);

        // Periodic save
        if (tick - lastSaveTick >= SAVE_INTERVAL) {
            lastSaveTick = tick;
            CorruptionManager cm = CorruptionManager.get(level);
            PurgeManager pm = PurgeManager.get(level);
            if (cm.isDirty()) cm.saveToDisk(level);
            if (pm.isDirty()) pm.saveToDisk(level);
        }
    }

    // ---- Mob Spawning in Corruption Zones ----

    /**
     * Every 400 ticks per zone, attempt to spawn hostile mobs near players in the zone.
     */
    private static void processZoneMobSpawning(ServerLevel level, long tick) {
        CorruptionManager cm = CorruptionManager.get(level);
        List<ServerPlayer> players = level.getServer().getPlayerList().getPlayers();

        for (CorruptionZone zone : cm.getActiveZones()) {
            Long lastSpawn = lastZoneMobSpawnTime.get(zone.zoneId);
            if (lastSpawn != null && tick - lastSpawn < ZONE_MOB_SPAWN_INTERVAL) continue;

            // Find players in this zone
            List<ServerPlayer> playersInZone = new ArrayList<>();
            for (ServerPlayer player : players) {
                if (player.level() == level && zone.containsBlock(player.blockPosition().getX(), player.blockPosition().getZ())) {
                    playersInZone.add(player);
                }
            }

            if (playersInZone.isEmpty()) continue;

            lastZoneMobSpawnTime.put(zone.zoneId, tick);

            // Determine mob count by tier
            int mobCount = switch (zone.tier) {
                case 1 -> 2 + RANDOM.nextInt(3);  // 2-4
                case 2 -> 3 + RANDOM.nextInt(3);  // 3-5
                case 3 -> 4 + RANDOM.nextInt(4);  // 4-7
                case 4 -> 5 + RANDOM.nextInt(6);  // 5-10
                default -> 2;
            };

            // Pick a random player in the zone to spawn near
            ServerPlayer target = playersInZone.get(RANDOM.nextInt(playersInZone.size()));
            spawnCorruptedMobsNear(level, target.blockPosition(), zone.tier, mobCount);
        }
    }

    /**
     * Spawn corrupted mobs near a position.
     */
    private static void spawnCorruptedMobsNear(ServerLevel level, BlockPos center, int tier, int count) {
        for (int i = 0; i < count; i++) {
            EntityType<?> mobType = getRandomMobType(tier);
            int offsetX = RANDOM.nextInt(20) - 10;
            int offsetZ = RANDOM.nextInt(20) - 10;
            int spawnX = center.getX() + offsetX;
            int spawnZ = center.getZ() + offsetZ;
            int spawnY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawnX, spawnZ);

            try {
                Mob mob = (Mob) mobType.create(level, EntitySpawnReason.MOB_SUMMONED);
                if (mob != null) {
                    mob.setPos(spawnX + 0.5, spawnY, spawnZ + 0.5);

                    // Buff stats: +25% HP, +15% damage per tier
                    if (mob.getAttribute(Attributes.MAX_HEALTH) != null) {
                        double baseHP = mob.getAttribute(Attributes.MAX_HEALTH).getBaseValue();
                        mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(baseHP * (1.0 + 0.25 * tier));
                        mob.setHealth(mob.getMaxHealth());
                    }
                    if (mob.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
                        double baseDmg = mob.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue();
                        mob.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(baseDmg * (1.0 + 0.15 * tier));
                    }

                    mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 6000, 0, true, false));
                    mob.addTag("megamod_corrupted");
                    mob.setPersistenceRequired();
                    level.addFreshEntity(mob);
                }
            } catch (Exception ignored) {}
        }
    }

    /**
     * Get a random mob type based on tier.
     */
    private static EntityType<?> getRandomMobType(int tier) {
        List<EntityType<?>> types = new ArrayList<>();
        types.add(EntityType.ZOMBIE);
        types.add(EntityType.SKELETON);
        if (tier >= 2) {
            types.add(EntityType.SPIDER);
            types.add(EntityType.CREEPER);
        }
        if (tier >= 3) {
            types.add(EntityType.WITCH);
            types.add(EntityType.PHANTOM);
            types.add(EntityType.STRAY);
        }
        if (tier >= 4) {
            types.add(EntityType.WITHER_SKELETON);
            types.add(EntityType.EVOKER);
            types.add(EntityType.RAVAGER);
        }
        return types.get(RANDOM.nextInt(types.size()));
    }

    // ---- Natural Mob Spawns in Corruption (buff existing mobs) ----

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof Monster mob)) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!FeatureToggleManager.get(level).isEnabled("corruption")) return;

        // Skip already-corrupted mobs
        if (mob.getTags().contains("megamod_corrupted")) return;

        BlockPos pos = mob.blockPosition();
        CorruptionManager manager = CorruptionManager.get(level);
        int tier = manager.getTierAt(pos.getX(), pos.getZ());
        if (tier <= 0) return;

        // Boost mob stats: +25% HP per tier, +15% damage per tier
        if (mob.getAttribute(Attributes.MAX_HEALTH) != null) {
            double baseHP = mob.getAttribute(Attributes.MAX_HEALTH).getBaseValue();
            double multiplier = 1.0 + 0.25 * tier;
            mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(baseHP * multiplier);
            mob.setHealth(mob.getMaxHealth());
        }

        if (mob.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            double baseDmg = mob.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue();
            double multiplier = 1.0 + 0.15 * tier;
            mob.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(baseDmg * multiplier);
        }

        // Purple glow effect
        mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 6000, 0, true, false));

        // Tag as corrupted
        mob.addTag("megamod_corrupted");

        // 30% chance to spawn an extra hostile mob at night
        // isNight() removed in 1.21.11 - use day time check
        long dayTime = level.getDayTime() % 24000L;
        boolean isNight = dayTime >= 13000 && dayTime <= 23000;
        if (isNight && RANDOM.nextFloat() < 0.30f) {
            spawnCorruptedMobsNear(level, pos, tier, 1);
        }
    }

    // ---- Mob Death (Purge Kill Tracking + Corruption Shard Drops) ----

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        LivingEntity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel level)) return;
        if (!FeatureToggleManager.get(level).isEnabled("corruption")) return;

        if (!(entity instanceof Mob mob)) return;
        if (!mob.getTags().contains("megamod_corrupted")) return;

        // Determine killer
        Entity source = event.getSource().getEntity();
        UUID killerUuid = null;
        if (source instanceof ServerPlayer killer) {
            killerUuid = killer.getUUID();
        } else if (source instanceof Mob attackerMob) {
            // Check if the attacker is a citizen (recruited) -- count for purge
            if (attackerMob.getTags().contains("megamod_citizen")) {
                String ownerStr = attackerMob.getPersistentData().getStringOr("megamod_owner", "");
                if (!ownerStr.isEmpty()) {
                    try { killerUuid = UUID.fromString(ownerStr); } catch (Exception ignored) {}
                }
            }
        }

        // Register kill for purge if purge is active
        if (killerUuid != null) {
            PurgeManager purge = PurgeManager.get(level);
            if (purge.hasPurgeActive()) {
                purge.recordKill(killerUuid, mob.blockPosition(), level);
            }
        }

        // Drop corruption_shard from corrupted mobs (25% chance)
        if (RANDOM.nextFloat() < 0.25f) {
            int shardCount = 1 + RANDOM.nextInt(2); // 1-2 shards
            ItemStack shards = new ItemStack(CorruptionRegistry.CORRUPTION_SHARD.get(), shardCount);
            mob.spawnAtLocation(level, shards);
        }
    }

    // ---- Player Tick (Debuffs and Ambient Damage in corrupted areas) ----

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        ServerLevel level = serverPlayer.level();
        if (!FeatureToggleManager.get(level).isEnabled("corruption")) return;

        BlockPos pos = serverPlayer.blockPosition();
        CorruptionManager manager = CorruptionManager.get(level);
        int tier = manager.getTierAt(pos.getX(), pos.getZ());

        long tick = level.getServer().getTickCount();
        UUID uuid = serverPlayer.getUUID();

        // Send corruption sync payload to client (even if tier is 0 to clear overlay)
        Long lastSync = lastSyncTime.get(uuid);
        if (lastSync == null || tick - lastSync >= SYNC_INTERVAL) {
            lastSyncTime.put(uuid, tick);
            PacketDistributor.sendToPlayer(serverPlayer, new CorruptionSyncPayload(tier));
        }

        // Send zone boundary data to client for particle rendering
        Long lastZoneSync = lastZoneSyncTime.get(uuid);
        if (lastZoneSync == null || tick - lastZoneSync >= ZONE_SYNC_INTERVAL) {
            lastZoneSyncTime.put(uuid, tick);
            List<CorruptionZone> nearbyZones = manager.getZonesInRange(pos, ZONE_SYNC_RANGE);
            List<CorruptionZoneSyncPayload.ZoneEntry> entries = new ArrayList<>();
            for (CorruptionZone z : nearbyZones) {
                entries.add(new CorruptionZoneSyncPayload.ZoneEntry(
                        z.centerX, z.centerZ, z.radius, z.tier, z.corruptionLevel));
            }
            PacketDistributor.sendToPlayer(serverPlayer, new CorruptionZoneSyncPayload(entries));
        }

        if (tier <= 0) return;

        // Action bar message (throttled)
        Long lastMsg = lastActionBarTime.get(uuid);
        if (lastMsg == null || tick - lastMsg >= ACTION_BAR_COOLDOWN) {
            lastActionBarTime.put(uuid, tick);
            serverPlayer.displayClientMessage(
                    Component.literal("You feel the corruption seeping into your bones...")
                            .withStyle(ChatFormatting.DARK_PURPLE),
                    true
            );
        }

        // Apply debuffs at intervals (every 100 ticks = 5 seconds)
        Long lastDebuff = lastDebuffTime.get(uuid);
        if (lastDebuff == null || tick - lastDebuff >= DEBUFF_INTERVAL) {
            lastDebuffTime.put(uuid, tick);

            // Slowness I always in corruption zones
            serverPlayer.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 200, 0, true, false));

            // Mining Fatigue I in tier 3+ zones
            if (tier >= 3) {
                serverPlayer.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 200, 0, true, false));
            }

            // Darkness effect in tier 4 zones
            if (tier >= 4) {
                serverPlayer.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 200, 0, true, false));
            }

            // Wither I in tier 4 zones (1 heart per 5 seconds)
            if (tier >= 4) {
                serverPlayer.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 0, true, false));
            }
        }

        // Ambient damage: players in corruption zones take 0.5 hearts every 5 seconds
        Long lastDmg = lastAmbientDamageTime.get(uuid);
        if (lastDmg == null || tick - lastDmg >= AMBIENT_DAMAGE_INTERVAL) {
            lastAmbientDamageTime.put(uuid, tick);
            serverPlayer.hurt(level.damageSources().magic(), 1.0f); // 0.5 hearts = 1 damage
        }
    }

    // ---- Player Login (warn about corrupted territory) ----

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerLevel level = player.level();
        if (!FeatureToggleManager.get(level).isEnabled("corruption")) return;

        // Check if player's position is in a corruption zone
        CorruptionManager cm = CorruptionManager.get(level);
        CorruptionZone zone = cm.getZoneAt(player.blockPosition());
        if (zone != null) {
            player.sendSystemMessage(
                    Component.literal("[Corruption] ")
                            .withStyle(ChatFormatting.DARK_PURPLE)
                            .append(Component.literal("Warning: You are in a corruption zone! (Tier " + zone.tier + ")")
                                    .withStyle(ChatFormatting.RED))
            );
        }

        // Check if any corruption is near their territory (within 256 blocks)
        List<CorruptionZone> nearby = cm.getZonesInRange(player.blockPosition(), 256);
        if (!nearby.isEmpty() && zone == null) {
            player.sendSystemMessage(
                    Component.literal("[Corruption] ")
                            .withStyle(ChatFormatting.DARK_PURPLE)
                            .append(Component.literal("There are " + nearby.size() + " corruption zone(s) nearby. Stay vigilant!")
                                    .withStyle(ChatFormatting.LIGHT_PURPLE))
            );
        }
    }

    // ---- Block Place (blocks placed in corruption have a chance to be corrupted) ----

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!FeatureToggleManager.get(level).isEnabled("corruption")) return;

        Entity entity = event.getEntity();
        if (!(entity instanceof ServerPlayer player)) return;

        BlockPos pos = event.getPos();
        CorruptionManager manager = CorruptionManager.get(level);
        int tier = manager.getTierAt(pos.getX(), pos.getZ());
        if (tier <= 0) return;

        // 10% chance block immediately decays (replaced with air)
        if (RANDOM.nextFloat() < 0.10f) {
            level.getServer().execute(() -> {
                level.removeBlock(pos, false);
            });

            player.displayClientMessage(
                    Component.literal("The corruption dissolves your placed block!")
                            .withStyle(ChatFormatting.DARK_PURPLE),
                    true
            );
        }
    }

    // ---- Block Break (Corruption Shard drops from blocks) ----

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!FeatureToggleManager.get(level).isEnabled("corruption")) return;

        BlockPos pos = event.getPos();
        CorruptionManager manager = CorruptionManager.get(level);
        int tier = manager.getTierAt(pos.getX(), pos.getZ());
        if (tier <= 0) return;

        // 5% chance to also drop corruption shard
        if (RANDOM.nextFloat() < 0.05f) {
            ItemStack shard = new ItemStack(CorruptionRegistry.CORRUPTION_SHARD.get(), 1);
            net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(
                    level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, shard);
            level.addFreshEntity(itemEntity);
        }
    }
}
