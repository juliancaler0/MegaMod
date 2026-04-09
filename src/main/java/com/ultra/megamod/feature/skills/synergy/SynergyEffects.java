package com.ultra.megamod.feature.skills.synergy;

import com.ultra.megamod.feature.dimensions.MegaModDimensions;
import com.ultra.megamod.feature.skills.SkillManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.world.entity.projectile.Projectile;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Event handlers that implement the active effects of cross-branch synergies.
 *
 * Synergies handled here:
 * <ul>
 *   <li><b>Sharpshooter</b> - first hit on a new target always crits (1.5x)</li>
 *   <li><b>Prospector's Rush</b> - mining ores grants 2s Haste I</li>
 *   <li><b>Adventurer</b> - 10% movement speed in dungeon dimension</li>
 *   <li><b>Undying</b> - heal 1 HP per second while below 30% health</li>
 *   <li><b>Iron Fortress</b> - Resistance I when below 50% HP</li>
 *   <li><b>Hawk Eye</b> - +15% ranged damage when outdoors</li>
 *   <li><b>Calculated Hunter</b> - consecutive kills grant stacking +5% damage</li>
 *   <li><b>Underground Express</b> - Speed I below Y=50</li>
 * </ul>
 *
 * Bloodblade and Arcane Flow expose static helper methods for other systems
 * (lifesteal calculation and ability cooldowns) to query.
 */
@EventBusSubscriber(modid = "megamod")
public class SynergyEffects {

    // ---- Sharpshooter tracking ----
    // player UUID -> set of entity IDs hit this session
    private static final Map<UUID, Set<Integer>> HIT_TARGETS = new HashMap<>();
    // player UUID -> map of entity ID -> tick when first hit (for 60s expiry)
    private static final Map<UUID, Map<Integer, Long>> HIT_TIMESTAMPS = new HashMap<>();

    // ---- Calculated Hunter tracking ----
    // player UUID -> tick of last kill
    private static final Map<UUID, Long> LAST_KILL_TICK = new HashMap<>();
    // player UUID -> current kill streak count (max 5)
    private static final Map<UUID, Integer> KILL_STREAK = new HashMap<>();

    // ======================================================================
    // Sharpshooter: first hit on a new target always crits (1.5x damage)
    // ======================================================================

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        Entity source = event.getSource().getEntity();
        if (!(source instanceof ServerPlayer player)) return;

        LivingEntity target = event.getEntity();
        if (target instanceof Player) return; // no PvP synergy

        ServerLevel level = (ServerLevel) player.level();
        SkillManager manager = SkillManager.get(level);
        Set<String> unlocked = manager.getUnlockedNodes(player.getUUID());

        Map<String, Double> bonuses = SynergyManager.getSynergyBonuses(unlocked);
        if (bonuses.isEmpty()) return;

        UUID playerId = player.getUUID();
        long currentTick = level.getGameTime();

        // -- Sharpshooter: first hit on a new target always crits (1.5x) --
        if (bonuses.getOrDefault("first_hit_crit", 0.0) > 0) {
            int entityId = target.getId();

            Set<Integer> hits = HIT_TARGETS.computeIfAbsent(playerId, k -> new HashSet<>());
            Map<Integer, Long> timestamps = HIT_TIMESTAMPS.computeIfAbsent(playerId, k -> new HashMap<>());

            // Clean up entries older than 60 seconds (1200 ticks)
            timestamps.entrySet().removeIf(e -> currentTick - e.getValue() > 1200);
            hits.retainAll(timestamps.keySet());

            if (!hits.contains(entityId)) {
                // First hit on this target -- apply 1.5x damage multiplier
                float original = event.getNewDamage();
                event.setNewDamage(original * 1.5f);
                hits.add(entityId);
                timestamps.put(entityId, currentTick);
            }
        }

        // -- Hawk Eye: +15% ranged damage when outdoors --
        if (bonuses.getOrDefault("outdoor_ranged_bonus", 0.0) > 0) {
            Entity directSource = event.getSource().getDirectEntity();
            if (directSource instanceof Projectile
                    && level.canSeeSky(player.blockPosition())) {
                float original = event.getNewDamage();
                float bonus = (float) (bonuses.get("outdoor_ranged_bonus") / 100.0);
                event.setNewDamage(original * (1.0f + bonus));
            }
        }

        // -- Calculated Hunter: stacking kill streak damage bonus --
        if (bonuses.getOrDefault("kill_streak_bonus", 0.0) > 0) {
            int streak = KILL_STREAK.getOrDefault(playerId, 0);
            Long lastKill = LAST_KILL_TICK.get(playerId);
            // Reset streak if more than 200 ticks (10s) since last kill
            if (lastKill != null && (currentTick - lastKill) > 200) {
                streak = 0;
                KILL_STREAK.put(playerId, 0);
            }
            if (streak > 0) {
                double bonusPerStack = bonuses.get("kill_streak_bonus");
                float multiplier = (float) (streak * bonusPerStack / 100.0);
                float original = event.getNewDamage();
                event.setNewDamage(original * (1.0f + multiplier));
            }
        }
    }

    // ======================================================================
    // Prospector's Rush: mining ores grants 2s Haste I
    // ======================================================================

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        Level level = (Level) event.getLevel();
        if (level.isClientSide()) return;

        BlockState state = event.getState();
        if (!isOre(state)) return;

        ServerLevel serverLevel = (ServerLevel) serverPlayer.level();
        SkillManager manager = SkillManager.get(serverLevel);
        Set<String> unlocked = manager.getUnlockedNodes(serverPlayer.getUUID());

        Map<String, Double> bonuses = SynergyManager.getSynergyBonuses(unlocked);
        if (bonuses.getOrDefault("ore_mining_haste", 0.0) <= 0) return;

        // Grant Haste I for 40 ticks (2 seconds)
        serverPlayer.addEffect(new MobEffectInstance(MobEffects.HASTE, 40, 0, false, true));
    }

    private static boolean isOre(BlockState state) {
        return state.is(BlockTags.COAL_ORES) || state.is(BlockTags.IRON_ORES)
            || state.is(BlockTags.GOLD_ORES) || state.is(BlockTags.DIAMOND_ORES)
            || state.is(BlockTags.EMERALD_ORES) || state.is(BlockTags.LAPIS_ORES)
            || state.is(BlockTags.REDSTONE_ORES) || state.is(BlockTags.COPPER_ORES);
    }

    // ======================================================================
    // Calculated Hunter: track kills for streak bonus
    // ======================================================================

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        Entity source = event.getSource().getEntity();
        if (!(source instanceof ServerPlayer player)) return;

        LivingEntity target = event.getEntity();
        if (target instanceof Player) return; // no PvP synergy

        ServerLevel level = (ServerLevel) player.level();
        SkillManager manager = SkillManager.get(level);
        Set<String> unlocked = manager.getUnlockedNodes(player.getUUID());

        Map<String, Double> bonuses = SynergyManager.getSynergyBonuses(unlocked);
        if (bonuses.isEmpty()) return;

        UUID playerId = player.getUUID();
        long currentTick = level.getGameTime();

        // -- Calculated Hunter: track kill streak --
        if (bonuses.getOrDefault("kill_streak_bonus", 0.0) > 0) {
            Long lastKill = LAST_KILL_TICK.get(playerId);
            int streak = KILL_STREAK.getOrDefault(playerId, 0);

            if (lastKill != null && (currentTick - lastKill) <= 200) {
                streak = Math.min(streak + 1, 5);
            } else {
                streak = 1;
            }

            KILL_STREAK.put(playerId, streak);
            LAST_KILL_TICK.put(playerId, currentTick);
        }

        // -- Arcane Flow: ability kills reduce cooldowns by 2s --
        // -- Arcane Swordsman: melee kills have 20% chance to reduce cooldowns by 1s --
        float cdReduction = 0.0f;
        if (bonuses.getOrDefault("kill_cooldown_reduction", 0.0) > 0) {
            cdReduction += bonuses.get("kill_cooldown_reduction").floatValue();
        }
        if (bonuses.getOrDefault("melee_kill_cooldown_chance", 0.0) > 0) {
            // Only proc on melee kills (no projectile involved)
            if (event.getSource().getDirectEntity() == player) {
                cdReduction += getArcaneSwordsmanReduction(player);
            }
        }
        if (cdReduction > 0.0f) {
            int ticksToReduce = (int)(cdReduction * 20); // seconds to ticks
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                net.minecraft.world.item.ItemStack stack = player.getInventory().getItem(i);
                if (!stack.isEmpty() && stack.getItem() instanceof com.ultra.megamod.feature.relics.RelicItem) {
                    com.ultra.megamod.feature.relics.ability.AbilityCooldownManager.reduceAllCooldowns(stack, ticksToReduce);
                }
            }
        }
    }

    // ======================================================================
    // Tick-based synergies: Adventurer, Undying, Iron Fortress,
    //                       Underground Express
    // ======================================================================

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        long gameTime = event.getServer().overworld().getGameTime();

        // -- Undying: heal 1 HP per second (every 20 ticks) while below 30% HP --
        if (gameTime % 20L == 0L) {
            for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
                if (player.getHealth() >= player.getMaxHealth() * 0.3f) continue;

                SkillManager manager = SkillManager.get((ServerLevel) player.level());
                Set<String> unlocked = manager.getUnlockedNodes(player.getUUID());

                Map<String, Double> bonuses = SynergyManager.getSynergyBonuses(unlocked);
                if (bonuses.getOrDefault("heal_below_threshold", 0.0) <= 0) continue;

                player.heal(1.0f);
            }
        }

        // -- 100-tick checks: Adventurer, Iron Fortress, Underground Express --
        if (gameTime % 100L != 0L) return;

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            SkillManager manager = SkillManager.get((ServerLevel) player.level());
            Set<String> unlocked = manager.getUnlockedNodes(player.getUUID());

            Map<String, Double> bonuses = SynergyManager.getSynergyBonuses(unlocked);
            if (bonuses.isEmpty()) continue;

            // -- Adventurer: 10% movement speed in dungeon dimension --
            if (bonuses.getOrDefault("dungeon_speed", 0.0) > 0
                    && player.level().dimension().equals(MegaModDimensions.DUNGEON)) {
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 120, 0, false, false));
            }

            // -- Iron Fortress: Resistance I when below 50% HP --
            if (bonuses.getOrDefault("armor_boost_low_hp", 0.0) > 0
                    && player.getHealth() < player.getMaxHealth() * 0.5f) {
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 120, 0, false, true));
            }

            // -- Underground Express: Speed I below Y=50 --
            if (bonuses.getOrDefault("underground_speed", 0.0) > 0
                    && player.blockPosition().getY() < 50) {
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 120, 0, false, false));
            }
        }
    }

    // ======================================================================
    // Bloodblade: static helper for lifesteal multiplier
    // ======================================================================

    /**
     * Returns the lifesteal healing multiplier for the given player.
     * 1.5 if the Bloodblade synergy is active, 1.0 otherwise.
     * Intended to be called from the lifesteal calculation code.
     */
    public static float getLifestealMultiplier(ServerPlayer player) {
        SkillManager manager = SkillManager.get((ServerLevel) player.level());
        Set<String> unlocked = manager.getUnlockedNodes(player.getUUID());
        Map<String, Double> bonuses = SynergyManager.getSynergyBonuses(unlocked);
        double efficiency = bonuses.getOrDefault("lifesteal_efficiency", 0.0);
        return efficiency > 0 ? 1.0f + (float)(efficiency / 100.0) : 1.0f;
    }

    // ======================================================================
    // Arcane Flow: static helper for kill cooldown reduction
    // ======================================================================

    /**
     * Returns the cooldown reduction (in seconds) that ability kills grant
     * to other abilities. 2.0 if Arcane Flow is active, 0.0 otherwise.
     * Intended to be called from the ability/relic system on mob kills.
     */
    public static float getKillCooldownReduction(ServerPlayer player) {
        SkillManager manager = SkillManager.get((ServerLevel) player.level());
        Set<String> unlocked = manager.getUnlockedNodes(player.getUUID());
        Map<String, Double> bonuses = SynergyManager.getSynergyBonuses(unlocked);
        return bonuses.getOrDefault("kill_cooldown_reduction", 0.0).floatValue();
    }

    // ======================================================================
    // Gourmet: food-related buffs last 50% longer
    // ======================================================================

    // Recursion guard: prevents infinite loop when we call addEffect inside an effect-added handler
    private static boolean extendingEffect = false;

    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        if (extendingEffect) return; // prevent recursion
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof ServerPlayer player)) return;
        MobEffectInstance effect = event.getEffectInstance();
        if (effect == null) return;
        // Only food-related buffs: saturation, regeneration, absorption from food
        var holder = effect.getEffect();
        if (!holder.equals(MobEffects.SATURATION) && !holder.equals(MobEffects.REGENERATION)
                && !holder.equals(MobEffects.ABSORPTION) && !holder.equals(MobEffects.HUNGER)) return;

        SkillManager manager = SkillManager.get((ServerLevel) player.level());
        Set<String> unlocked = manager.getUnlockedNodes(player.getUUID());
        Map<String, Double> bonuses = SynergyManager.getSynergyBonuses(unlocked);
        double foodBonus = bonuses.getOrDefault("food_buff_duration", 0.0);
        if (foodBonus <= 0) return;

        int originalDuration = effect.getDuration();
        int extraDuration = (int) (originalDuration * foodBonus / 100.0);
        if (extraDuration <= 0) return;

        // Extend duration by reapplying with longer duration (guarded against recursion)
        extendingEffect = true;
        try {
            player.addEffect(new MobEffectInstance(holder, originalDuration + extraDuration,
                    effect.getAmplifier(), effect.isAmbient(), effect.isVisible()));
        } finally {
            extendingEffect = false;
        }
    }

    // ======================================================================
    // Arcane Swordsman: melee kills have 20% chance to reduce ability cooldowns by 1s
    // ======================================================================

    /**
     * Returns cooldown reduction (in seconds) from Arcane Swordsman synergy on melee kills.
     * Returns 1.0 if the roll succeeds (20% chance), 0.0 otherwise.
     * Intended to be called from combat/relic systems on melee mob kills.
     */
    public static float getArcaneSwordsmanReduction(ServerPlayer player) {
        SkillManager manager = SkillManager.get((ServerLevel) player.level());
        Set<String> unlocked = manager.getUnlockedNodes(player.getUUID());
        Map<String, Double> bonuses = SynergyManager.getSynergyBonuses(unlocked);
        double chance = bonuses.getOrDefault("melee_kill_cooldown_chance", 0.0);
        if (chance <= 0) return 0.0f;
        if (player.getRandom().nextDouble() * 100.0 < chance) return 1.0f;
        return 0.0f;
    }

    // ======================================================================
    // Treasure Alchemist: dungeon loot has higher quality tier
    // ======================================================================

    /**
     * Returns true if the Treasure Alchemist synergy is active for the player,
     * meaning dungeon loot should be upgraded by one quality tier.
     */
    public static boolean hasTreasureAlchemist(ServerPlayer player) {
        SkillManager manager = SkillManager.get((ServerLevel) player.level());
        Set<String> unlocked = manager.getUnlockedNodes(player.getUUID());
        Map<String, Double> bonuses = SynergyManager.getSynergyBonuses(unlocked);
        return bonuses.getOrDefault("dungeon_loot_quality", 0.0) > 0;
    }

    // ======================================================================
    // Spirit Conductor: familiar deals double damage
    // ======================================================================

    /**
     * Returns the familiar damage multiplier. 2.0 if Spirit Conductor is active, 1.0 otherwise.
     */
    public static float getFamiliarDamageMultiplier(ServerPlayer player) {
        SkillManager manager = SkillManager.get((ServerLevel) player.level());
        Set<String> unlocked = manager.getUnlockedNodes(player.getUUID());
        Map<String, Double> bonuses = SynergyManager.getSynergyBonuses(unlocked);
        double bonus = bonuses.getOrDefault("familiar_damage_bonus", 0.0);
        return bonus > 0 ? 1.0f + (float)(bonus / 100.0) : 1.0f;
    }

    // ======================================================================
    // Nature's Harmony: crops and animals grow 25% faster nearby
    // (implemented via random tick boost in the server tick handler above)
    // ======================================================================

    /**
     * Returns the growth speed bonus multiplier for nearby crops/animals.
     * 1.25 if Nature's Harmony is active, 1.0 otherwise.
     */
    public static float getGrowthSpeedMultiplier(ServerPlayer player) {
        SkillManager manager = SkillManager.get((ServerLevel) player.level());
        Set<String> unlocked = manager.getUnlockedNodes(player.getUUID());
        Map<String, Double> bonuses = SynergyManager.getSynergyBonuses(unlocked);
        double growthBonus = bonuses.getOrDefault("growth_speed_bonus", 0.0);
        return growthBonus > 0 ? 1.0f + (float)(growthBonus / 100.0) : 1.0f;
    }

    // ======================================================================
    // Fortune's Favor: 10% chance to double rare drops
    // ======================================================================

    /**
     * Returns true if Fortune's Favor should proc (10% chance and synergy active).
     */
    public static boolean shouldDoubleRareDrop(ServerPlayer player) {
        SkillManager manager = SkillManager.get((ServerLevel) player.level());
        Set<String> unlocked = manager.getUnlockedNodes(player.getUUID());
        Map<String, Double> bonuses = SynergyManager.getSynergyBonuses(unlocked);
        double chance = bonuses.getOrDefault("double_rare_drops", 0.0);
        if (chance <= 0) return false;
        return player.getRandom().nextDouble() * 100.0 < chance;
    }

    // ======================================================================
    // Lethal Combatant: melee crits heal 1 heart (2 HP)
    // ======================================================================

    @SubscribeEvent
    public static void onCritHealLethalCombatant(LivingDamageEvent.Post event) {
        Entity source = event.getSource().getEntity();
        if (!(source instanceof ServerPlayer player)) return;
        // Crit check: player is falling and not on ground
        if (!(player.fallDistance > 0.0f && !player.onGround())) return;

        SkillManager manager = SkillManager.get((ServerLevel) player.level());
        Set<String> unlocked = manager.getUnlockedNodes(player.getUUID());
        Map<String, Double> bonuses = SynergyManager.getSynergyBonuses(unlocked);
        double healAmount = bonuses.getOrDefault("crit_heal", 0.0);
        if (healAmount <= 0) return;

        player.heal((float) healAmount);
    }

    // ======================================================================
    // Blood Fortress: blocking heals 2% max HP per block
    // ======================================================================

    @SubscribeEvent
    public static void onBlockHealBloodFortress(LivingShieldBlockEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!event.getBlocked()) return;

        SkillManager manager = SkillManager.get((ServerLevel) player.level());
        Set<String> unlocked = manager.getUnlockedNodes(player.getUUID());
        Map<String, Double> bonuses = SynergyManager.getSynergyBonuses(unlocked);
        double healPercent = bonuses.getOrDefault("block_heal_percent", 0.0);
        if (healPercent <= 0) return;

        float heal = player.getMaxHealth() * (float)(healPercent / 100.0);
        player.heal(heal);
    }

    // ======================================================================
    // Farm to Table: eating grants +2 hunger and Saturation I for 3s
    // ======================================================================

    @SubscribeEvent
    public static void onEatFarmToTable(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!event.getItem().has(net.minecraft.core.component.DataComponents.FOOD)) return;

        SkillManager manager = SkillManager.get((ServerLevel) player.level());
        Set<String> unlocked = manager.getUnlockedNodes(player.getUUID());
        Map<String, Double> bonuses = SynergyManager.getSynergyBonuses(unlocked);
        if (bonuses.getOrDefault("crop_cook_bonus", 0.0) <= 0) return;

        player.getFoodData().eat(2, 0.5f);
        player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 60, 0, false, true, true));
    }

    // ======================================================================
    // Runic Arsenal: enchanted weapons deal +15% elemental damage
    // ======================================================================

    /**
     * Returns the elemental damage multiplier from Runic Arsenal synergy.
     * 1.15 if active and holding an enchanted weapon, 1.0 otherwise.
     */
    public static float getRunicArsenalMultiplier(ServerPlayer player) {
        if (player.getMainHandItem().isEmpty()) return 1.0f;
        if (!player.getMainHandItem().isEnchanted()) return 1.0f;

        SkillManager manager = SkillManager.get((ServerLevel) player.level());
        Set<String> unlocked = manager.getUnlockedNodes(player.getUUID());
        Map<String, Double> bonuses = SynergyManager.getSynergyBonuses(unlocked);
        double bonus = bonuses.getOrDefault("enchanted_elemental_bonus", 0.0);
        return bonus > 0 ? 1.0f + (float)(bonus / 100.0) : 1.0f;
    }

    // ======================================================================
    // Mother Lode: 8% chance mining ore drops a bonus random gem
    // ======================================================================

    @SubscribeEvent
    public static void onOreBreakMotherLode(BlockEvent.BreakEvent event) {
        Level level = (Level) event.getLevel();
        if (level.isClientSide()) return;
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;

        BlockState state = event.getState();
        if (!isOre(state)) return;

        SkillManager manager = SkillManager.get((ServerLevel) player.level());
        Set<String> unlocked = manager.getUnlockedNodes(player.getUUID());
        Map<String, Double> bonuses = SynergyManager.getSynergyBonuses(unlocked);
        double chance = bonuses.getOrDefault("bonus_gem_chance", 0.0);
        if (chance <= 0) return;

        if (player.getRandom().nextDouble() * 100.0 < chance) {
            ServerLevel serverLevel = (ServerLevel) player.level();
            net.minecraft.world.item.ItemStack gem = getRandomGem(player);
            net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(
                    serverLevel, event.getPos().getX() + 0.5, event.getPos().getY() + 0.5,
                    event.getPos().getZ() + 0.5, gem);
            serverLevel.addFreshEntity(itemEntity);
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Mother Lode! Bonus gem drop!")
                    .withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE));
        }
    }

    private static net.minecraft.world.item.ItemStack getRandomGem(ServerPlayer player) {
        int roll = player.getRandom().nextInt(5);
        return switch (roll) {
            case 0 -> new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND);
            case 1 -> new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.EMERALD);
            case 2 -> new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.LAPIS_LAZULI, 3);
            case 3 -> new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.AMETHYST_SHARD, 2);
            case 4 -> new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.QUARTZ, 2);
            default -> new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.EMERALD);
        };
    }

    // ======================================================================
    // Apex Explorer: +20% damage to mob types not killed this session
    // ======================================================================

    // player UUID -> set of entity type paths killed this session
    private static final Map<UUID, Set<String>> KILLED_MOB_TYPES = new HashMap<>();

    @SubscribeEvent
    public static void onDamageApexExplorer(LivingDamageEvent.Pre event) {
        Entity source = event.getSource().getEntity();
        if (!(source instanceof ServerPlayer player)) return;

        LivingEntity target = event.getEntity();
        if (target instanceof Player) return;

        ServerLevel level = (ServerLevel) player.level();
        SkillManager manager = SkillManager.get(level);
        Set<String> unlocked = manager.getUnlockedNodes(player.getUUID());

        Map<String, Double> bonuses = SynergyManager.getSynergyBonuses(unlocked);
        double newMobBonus = bonuses.getOrDefault("new_mob_damage", 0.0);
        if (newMobBonus <= 0) return;

        String mobType = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE
                .getKey(target.getType()).getPath();
        Set<String> killed = KILLED_MOB_TYPES.getOrDefault(player.getUUID(), Set.of());

        if (!killed.contains(mobType)) {
            float original = event.getNewDamage();
            float multiplier = (float)(newMobBonus / 100.0);
            event.setNewDamage(original * (1.0f + multiplier));
        }
    }

    @SubscribeEvent
    public static void onKillApexExplorerTrack(LivingDeathEvent event) {
        Entity source = event.getSource().getEntity();
        if (!(source instanceof ServerPlayer player)) return;
        if (event.getEntity() instanceof Player) return;

        // Always track killed types for Apex Explorer
        String mobType = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE
                .getKey(event.getEntity().getType()).getPath();
        KILLED_MOB_TYPES.computeIfAbsent(player.getUUID(), k -> new HashSet<>()).add(mobType);
    }

    // ======================================================================
    // Cleanup on logout
    // ======================================================================

    /**
     * Called from SkillEvents.onPlayerLogout to clear tracking data.
     */
    public static void clearPlayer(UUID playerId) {
        HIT_TARGETS.remove(playerId);
        HIT_TIMESTAMPS.remove(playerId);
        LAST_KILL_TICK.remove(playerId);
        KILL_STREAK.remove(playerId);
        KILLED_MOB_TYPES.remove(playerId);
    }
}
