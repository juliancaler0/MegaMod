package com.ultra.megamod.feature.skills.capstone;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = "megamod")
public class SurvivalCapstones {

    // ==================== Cartographer (explorer_4) ====================
    // Tracks last known biome per player
    private static final Map<UUID, String> LAST_BIOME = new HashMap<>();

    // ==================== Second Wind (endurance_4) ====================
    private static final long SECOND_WIND_COOLDOWN = 6000L; // 5 minutes

    // ==================== Predator's Mark (hunter_instinct_4) ====================
    // Tracks marked targets: player UUID -> target entity ID
    private static final Map<UUID, Integer> MARKED_TARGETS = new HashMap<>();

    // ==================== Windwalker (navigator_4) ====================
    // Tracks continuous sprinting: player UUID -> [sprintStartTick, lastBurstTick]
    private static final Map<UUID, long[]> SPRINT_TRACKER = new HashMap<>();

    // ==================== Dungeon boss types for Delver's Fortune ====================
    private static final Set<String> DUNGEON_BOSS_TYPES = Set.of(
            "wraith", "ossukage", "dungeon_keeper", "frostmaw",
            "wroughtnaut", "umvuthi", "chaos_spawner", "sculptor"
    );

    // ==================== Cartographer (explorer_4) ====================

    /**
     * Every 100 ticks: check if player entered a new biome. If so, grant Speed I for 10s.
     */
    @SubscribeEvent
    public static void onServerTickCartographer(ServerTickEvent.Post event) {
        long gameTime = event.getServer().overworld().getGameTime();
        if (gameTime % 100L != 0L) return;

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (!CapstoneManager.hasCapstoneTrigger(player, "explorer_4")) continue;
            boolean enhanced = CapstoneManager.hasCapstoneTrigger(player, "explorer_5");

            ServerLevel level = (ServerLevel) player.level();
            Holder<Biome> biomeHolder = level.getBiome(player.blockPosition());
            String currentBiome = biomeHolder.unwrapKey()
                    .map(key -> key.identifier().toString())
                    .orElse("unknown");

            String previousBiome = LAST_BIOME.get(player.getUUID());
            LAST_BIOME.put(player.getUUID(), currentBiome);

            if (previousBiome != null && !previousBiome.equals(currentBiome)) {
                // Entered a new biome - grant Speed for 200 ticks (10s) or 300 ticks (15s) if enhanced
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, enhanced ? 300 : 200, enhanced ? 1 : 0, false, true, true));

                // Bonus Survival XP for biome discovery
                com.ultra.megamod.feature.skills.SkillManager.get(level.getServer().overworld())
                        .addXp(player.getUUID(), com.ultra.megamod.feature.skills.SkillTreeType.SURVIVAL, enhanced ? 5 : 3);

                level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                        SoundSource.PLAYERS, 0.5f, 1.8f);
                level.sendParticles(ParticleTypes.COMPOSTER, player.getX(), player.getY() + 1.0,
                        player.getZ(), 8, 0.5, 0.5, 0.5, 0.05);

                // Extract readable biome name
                String biomeName = currentBiome.contains(":") ? currentBiome.split(":")[1] : currentBiome;
                biomeName = biomeName.replace("_", " ");
                player.sendSystemMessage(Component.literal("Cartographer: New biome discovered - " + biomeName + "!")
                        .withStyle(ChatFormatting.AQUA));
            }
        }
    }

    // ==================== Second Wind (endurance_4) ====================

    /**
     * On damage post: if player health drops below 20% max HP, trigger Second Wind.
     * Apply Regeneration II for 5s + Resistance I for 3s. 5 minute cooldown.
     */
    @SubscribeEvent
    public static void onDamagePostSecondWind(LivingDamageEvent.Post event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        if (!(entity instanceof ServerPlayer player)) return;
        if (!CapstoneManager.hasCapstoneTrigger(player, "endurance_4")) return;
        boolean enhanced = CapstoneManager.hasCapstoneTrigger(player, "endurance_5");

        float currentHealth = player.getHealth();
        float maxHealth = player.getMaxHealth();

        // Must be below 20% HP
        if (currentHealth >= maxHealth * 0.20f) return;
        if (currentHealth <= 0) return; // Already dead

        ServerLevel level = (ServerLevel) player.level();
        long currentTick = level.getGameTime();

        if (CapstoneManager.isOnCooldown(player.getUUID(), "second_wind", currentTick, SECOND_WIND_COOLDOWN)) {
            return;
        }

        CapstoneManager.setCooldown(player.getUUID(), "second_wind", currentTick);

        // If enhanced, remove all negative effects first
        if (enhanced) {
            player.getActiveEffects().stream()
                    .filter(e -> !e.getEffect().value().isBeneficial())
                    .map(MobEffectInstance::getEffect)
                    .toList()
                    .forEach(player::removeEffect);
        }

        // Apply Regeneration II for 100 ticks (5s) + Resistance I for 60 ticks (3s)
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1, false, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 60, 0, false, true, true));

        level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.PLAYERS, 1.0f, 0.8f);
        level.sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1.5,
                player.getZ(), 8, 0.5, 0.3, 0.5, 0.05);

        player.sendSystemMessage(Component.literal("Second Wind activated!")
                .withStyle(ChatFormatting.RED));
    }

    // ==================== Predator's Mark (hunter_instinct_4) ====================

    /**
     * On mob death: if killer has this node, find nearest mob of same type within 24 blocks
     * and apply Glowing for 10s. Mark that target for 30% bonus damage.
     */
    @SubscribeEvent
    public static void onDeathPredatorsMark(LivingDeathEvent event) {
        Entity sourceEntity = event.getSource().getEntity();
        if (!(sourceEntity instanceof ServerPlayer player)) return;
        if (!CapstoneManager.hasCapstoneTrigger(player, "hunter_instinct_4")) return;

        LivingEntity killed = event.getEntity();
        if (killed instanceof Player) return;

        ServerLevel level = (ServerLevel) player.level();

        // Find nearest mob of same type within 24 blocks
        AABB area = player.getBoundingBox().inflate(24.0);
        List<LivingEntity> sameType = level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e.isAlive() && e != killed && e.getType() == killed.getType());

        if (sameType.isEmpty()) return;

        // Pick closest
        LivingEntity closest = null;
        double closestDist = Double.MAX_VALUE;
        for (LivingEntity mob : sameType) {
            double dist = mob.distanceToSqr(player);
            if (dist < closestDist) {
                closestDist = dist;
                closest = mob;
            }
        }

        if (closest != null) {
            // Apply Glowing for 200 ticks (10s)
            closest.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0, false, true, true));

            // Store marked target
            MARKED_TARGETS.put(player.getUUID(), closest.getId());

            level.playSound(null, closest.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.PLAYERS, 0.6f, 0.5f);
            level.sendParticles(ParticleTypes.CRIT, closest.getX(), closest.getY() + 1.0,
                    closest.getZ(), 10, 0.3, 0.5, 0.3, 0.1);

            player.sendSystemMessage(Component.literal("Predator's Mark: Target marked!")
                    .withStyle(ChatFormatting.YELLOW));
        }
    }

    /**
     * On damage pre: if target is marked by this player, increase damage by 30%.
     */
    @SubscribeEvent
    public static void onDamagePrePredatorsMark(LivingDamageEvent.Pre event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide()) return;

        Entity sourceEntity = event.getSource().getEntity();
        if (!(sourceEntity instanceof ServerPlayer player)) return;
        if (!CapstoneManager.hasCapstoneTrigger(player, "hunter_instinct_4")) return;
        boolean enhanced = CapstoneManager.hasCapstoneTrigger(player, "hunter_instinct_5");

        Integer markedId = MARKED_TARGETS.get(player.getUUID());
        if (markedId == null || markedId != target.getId()) return;

        float originalDamage = event.getOriginalDamage();
        event.setNewDamage(originalDamage * (enhanced ? 1.50f : 1.30f));

        ServerLevel level = (ServerLevel) player.level();
        level.sendParticles(ParticleTypes.ENCHANTED_HIT, target.getX(), target.getY() + 1.0,
                target.getZ(), 6, 0.3, 0.4, 0.3, 0.1);

        // Mark persists while target has Glowing — clear when it expires or target dies
        if (!target.hasEffect(MobEffects.GLOWING)) {
            MARKED_TARGETS.remove(player.getUUID());
        }
    }

    // ==================== Windwalker (navigator_4) ====================

    /**
     * Every 20 ticks: for sprinting players with this node, reduce exhaustion.
     * Every 30 seconds of continuous sprinting, grant a 2s Speed II burst.
     */
    @SubscribeEvent
    public static void onServerTickWindwalker(ServerTickEvent.Post event) {
        long gameTime = event.getServer().overworld().getGameTime();
        if (gameTime % 20L != 0L) return;

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (!CapstoneManager.hasCapstoneTrigger(player, "navigator_4")) continue;
            boolean enhanced = CapstoneManager.hasCapstoneTrigger(player, "navigator_5");

            ServerLevel level = (ServerLevel) player.level();
            long currentTick = level.getGameTime();
            UUID playerId = player.getUUID();

            long sprintInterval = enhanced ? 300 : 600;

            if (player.isSprinting()) {
                // Reduce exhaustion while sprinting
                player.getFoodData().addExhaustion(-0.1f);

                long[] sprintData = SPRINT_TRACKER.get(playerId);
                if (sprintData == null) {
                    SPRINT_TRACKER.put(playerId, new long[]{currentTick, 0});
                } else {
                    long sprintDuration = currentTick - sprintData[0];
                    // Every 600 ticks (30s) or 300 ticks (15s) if enhanced, grant Speed II burst
                    if (sprintDuration > 0 && sprintDuration % sprintInterval == 0
                            && currentTick - sprintData[1] > sprintInterval) {
                        player.addEffect(new MobEffectInstance(MobEffects.SPEED, 80, 1, false, true, true));
                        sprintData[1] = currentTick;

                        level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP,
                                SoundSource.PLAYERS, 0.5f, 1.5f);
                        level.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY(),
                                player.getZ(), 5, 0.3, 0.1, 0.3, 0.05);
                    }
                }
            } else {
                // Reset sprint tracker when not sprinting
                SPRINT_TRACKER.remove(playerId);
            }
        }
    }

    // ==================== Delver's Fortune (dungeoneer_4) ====================

    /**
     * On dungeon boss death: if killer has this node, drop extra loot items.
     */
    @SubscribeEvent
    public static void onDeathDelversFortune(LivingDeathEvent event) {
        Entity sourceEntity = event.getSource().getEntity();
        if (!(sourceEntity instanceof ServerPlayer player)) return;
        if (!CapstoneManager.hasCapstoneTrigger(player, "dungeoneer_4")) return;
        boolean enhanced = CapstoneManager.hasCapstoneTrigger(player, "dungeoneer_5");

        LivingEntity killed = event.getEntity();
        Identifier mobId = BuiltInRegistries.ENTITY_TYPE.getKey(killed.getType());

        // Only trigger for megamod dungeon bosses
        if (!"megamod".equals(mobId.getNamespace())) return;
        String path = mobId.getPath();
        if (!DUNGEON_BOSS_TYPES.contains(path)) return;

        ServerLevel level = (ServerLevel) player.level();

        // Drop extra valuable items (enhanced: 2-5, normal: 1-3)
        int bonusCount = enhanced ? 2 + player.getRandom().nextInt(4) : 1 + player.getRandom().nextInt(3);
        for (int i = 0; i < bonusCount; i++) {
            ItemStack loot = getRandomDungeonLoot(player);
            ItemEntity itemEntity = new ItemEntity(level,
                    killed.getX(), killed.getY() + 0.5, killed.getZ(), loot);
            level.addFreshEntity(itemEntity);
        }

        level.playSound(null, killed.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                SoundSource.PLAYERS, 1.0f, 1.0f);
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, killed.getX(), killed.getY() + 1.0,
                killed.getZ(), 20, 0.5, 0.8, 0.5, 0.3);

        player.sendSystemMessage(Component.literal("Delver's Fortune: Bonus dungeon loot!")
                .withStyle(ChatFormatting.GOLD));
    }

    private static ItemStack getRandomDungeonLoot(ServerPlayer player) {
        int roll = player.getRandom().nextInt(10);
        return switch (roll) {
            case 0 -> new ItemStack(Items.DIAMOND, 1 + player.getRandom().nextInt(3));
            case 1 -> new ItemStack(Items.EMERALD, 2 + player.getRandom().nextInt(4));
            case 2 -> new ItemStack(Items.GOLD_BLOCK);
            case 3 -> new ItemStack(Items.IRON_BLOCK, 1 + player.getRandom().nextInt(2));
            case 4 -> new ItemStack(Items.ENDER_PEARL, 2 + player.getRandom().nextInt(3));
            case 5 -> new ItemStack(Items.BLAZE_ROD, 2 + player.getRandom().nextInt(4));
            case 6 -> new ItemStack(Items.EXPERIENCE_BOTTLE, 3 + player.getRandom().nextInt(5));
            case 7 -> new ItemStack(Items.GOLDEN_APPLE);
            case 8 -> new ItemStack(Items.ENCHANTED_BOOK);
            case 9 -> new ItemStack(Items.NETHERITE_SCRAP);
            default -> new ItemStack(Items.DIAMOND);
        };
    }

    // ==================== Cleanup ====================

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID uuid = event.getEntity().getUUID();
        LAST_BIOME.remove(uuid);
        MARKED_TARGETS.remove(uuid);
        SPRINT_TRACKER.remove(uuid);
    }
}
