package com.ultra.megamod.feature.alchemy;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Handles all alchemy effect gameplay logic via game bus events.
 */
@EventBusSubscriber(modid = MegaMod.MODID)
public class AlchemyEffects {

    // ==================== Inferno Boost: +50% fire damage ====================

    @SubscribeEvent
    public static void onLivingHurtInferno(LivingDamageEvent.Pre event) {
        DamageSource source = event.getSource();
        if (source.getEntity() instanceof Player player) {
            if (hasAlchemyEffect(player, AlchemyRegistry.INFERNO_BOOST)) {
                // Check if it's fire damage or if player is dealing fire aspect damage
                if (source.type().msgId().contains("fire") || source.type().msgId().contains("inFire") ||
                        source.type().msgId().contains("onFire") || source.type().msgId().contains("lava")) {
                    event.setNewDamage(event.getNewDamage() * 1.5f);
                }
            }
        }
    }

    // ==================== Berserker Rage: +Strength III damage boost, -50% defense ====================

    @SubscribeEvent
    public static void onLivingHurtBerserker(LivingDamageEvent.Pre event) {
        // Attacker has berserker rage: deal extra damage
        DamageSource source = event.getSource();
        if (source.getEntity() instanceof Player attacker) {
            if (hasAlchemyEffect(attacker, AlchemyRegistry.BERSERKER_RAGE)) {
                // +75% melee damage (Strength III equivalent)
                if (!source.type().msgId().contains("arrow") && !source.type().msgId().contains("trident")) {
                    event.setNewDamage(event.getNewDamage() * 1.75f);
                }
            }
        }

        // Defender has berserker rage: take 50% more damage
        if (event.getEntity() instanceof Player defender) {
            if (hasAlchemyEffect(defender, AlchemyRegistry.BERSERKER_RAGE)) {
                event.setNewDamage(event.getNewDamage() * 1.5f);
            }
        }
    }

    // ==================== Berserker Rage: Lifesteal (10% of damage dealt) ====================

    @SubscribeEvent
    public static void onDamageDealtBerserkerLifesteal(LivingDamageEvent.Post event) {
        DamageSource source = event.getSource();
        if (source.getEntity() instanceof Player player) {
            if (hasAlchemyEffect(player, AlchemyRegistry.BERSERKER_RAGE)) {
                float damageDealt = event.getNewDamage();
                float heal = damageDealt * 0.10f;
                if (heal > 0) {
                    player.heal(heal);
                }
            }
        }
    }

    // ==================== Eagle Eye: +50% ranged damage ====================

    @SubscribeEvent
    public static void onLivingHurtEagleEye(LivingDamageEvent.Pre event) {
        DamageSource source = event.getSource();
        Entity directEntity = source.getDirectEntity();
        if (directEntity instanceof Projectile && source.getEntity() instanceof Player player) {
            if (hasAlchemyEffect(player, AlchemyRegistry.EAGLE_EYE)) {
                event.setNewDamage(event.getNewDamage() * 1.5f);
            }
        }
    }

    // ==================== Void Walk: No fall damage ====================
    // Handled via LivingDamageEvent.Pre since LivingFallEvent may not exist

    @SubscribeEvent
    public static void onFallDamageVoidWalk(LivingDamageEvent.Pre event) {
        if (event.getEntity() instanceof Player player) {
            if (hasAlchemyEffect(player, AlchemyRegistry.VOID_WALK)) {
                DamageSource source = event.getSource();
                String msgId = source.type().msgId();
                if (msgId.contains("fall") || msgId.equals("fall") || msgId.equals("flyIntoWall")) {
                    event.setNewDamage(0.0f);
                }
            }
        }
    }

    // ==================== Stone Skin: Knockback immunity ====================

    @SubscribeEvent
    public static void onKnockbackStoneSkin(LivingKnockBackEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (hasAlchemyEffect(player, AlchemyRegistry.STONE_SKIN)) {
                event.setCanceled(true);
            }
        }
    }

    // ==================== Undying Grace: Totem effect on death ====================

    @SubscribeEvent
    public static void onDeathUndyingGrace(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (hasAlchemyEffect(player, AlchemyRegistry.UNDYING_GRACE)) {
                // Prevent death and apply totem effects
                event.setCanceled(true);
                player.setHealth(1.0f);

                // Remove the undying grace effect (one-time use)
                Holder<MobEffect> holder = AlchemyRegistry.holderOf(AlchemyRegistry.UNDYING_GRACE);
                player.removeEffect(holder);

                // Apply totem restoration effects
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));

                // Visual effect
                ServerLevel serverLevel = (ServerLevel) player.level();
                serverLevel.broadcastEntityEvent(player, (byte) 35); // Totem animation
            }
        }
    }

    // ==================== Midas Touch: Double coin drops ====================

    @SubscribeEvent
    public static void onMobDeathMidasTouch(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player) return; // Don't apply to player deaths

        DamageSource source = event.getSource();
        if (source.getEntity() instanceof ServerPlayer player) {
            if (hasAlchemyEffect(player, AlchemyRegistry.MIDAS_TOUCH)) {
                // Double the MegaCoin reward for this kill
                ServerLevel level = (ServerLevel) player.level();
                EconomyManager eco = EconomyManager.get(level);
                // Add bonus coins (base kill reward is usually 1-5)
                int bonusCoins = level.random.nextInt(5) + 1;
                eco.addWallet(player.getUUID(), bonusCoins);
                eco.saveToDisk(level);
            }
        }
    }

    // ==================== Phantom Phase: Reduced damage taken (spectral form) ====================

    @SubscribeEvent
    public static void onDamageTakenPhantom(LivingDamageEvent.Pre event) {
        if (event.getEntity() instanceof Player player) {
            if (hasAlchemyEffect(player, AlchemyRegistry.PHANTOM_PHASE)) {
                // 50% damage reduction while in spectral form
                event.setNewDamage(event.getNewDamage() * 0.5f);
            }
        }
    }

    // ==================== Titan: +50% melee damage + extended reach (reach handled conceptually) ====================

    @SubscribeEvent
    public static void onLivingHurtTitan(LivingDamageEvent.Pre event) {
        DamageSource source = event.getSource();
        if (source.getEntity() instanceof Player attacker) {
            if (hasAlchemyEffect(attacker, AlchemyRegistry.TITAN)) {
                // +50% melee damage
                if (!source.type().msgId().contains("arrow") && !source.type().msgId().contains("trident")) {
                    event.setNewDamage(event.getNewDamage() * 1.5f);
                }
            }
        }
    }

    // ==================== Chronos: Slow nearby mobs every second ====================

    @SubscribeEvent
    public static void onPlayerTickChronos(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (!hasAlchemyEffect(player, AlchemyRegistry.CHRONOS)) return;

        // Only process every 20 ticks (1 second)
        if (serverPlayer.tickCount % 20 != 0) return;

        ServerLevel level = (ServerLevel) player.level();
        AABB area = player.getBoundingBox().inflate(12.0); // 12-block radius
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area, e -> !(e instanceof Player))) {
            entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 2, false, true));
        }
    }

    // ==================== Blood Rage: Stacking damage + HP drain ====================

    // Track hit stacks per player (in-memory, resets when effect ends)
    private static final java.util.Map<java.util.UUID, Integer> BLOOD_RAGE_STACKS = new java.util.concurrent.ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onLivingHurtBloodRage(LivingDamageEvent.Pre event) {
        DamageSource source = event.getSource();
        if (source.getEntity() instanceof Player attacker) {
            if (hasAlchemyEffect(attacker, AlchemyRegistry.BLOOD_RAGE)) {
                // Each hit adds a stack (max 10), each stack = +5% damage
                java.util.UUID uuid = attacker.getUUID();
                int stacks = BLOOD_RAGE_STACKS.getOrDefault(uuid, 0);
                float bonus = 1.0f + (stacks * 0.05f); // 5% per stack, up to 50%
                event.setNewDamage(event.getNewDamage() * bonus);

                // Add a stack (max 10)
                if (stacks < 10) {
                    BLOOD_RAGE_STACKS.put(uuid, stacks + 1);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTickBloodRage(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        if (hasAlchemyEffect(player, AlchemyRegistry.BLOOD_RAGE)) {
            // Drain 0.5 HP every 2 seconds (40 ticks)
            if (serverPlayer.tickCount % 40 == 0) {
                if (player.getHealth() > 2.0f) { // Don't drain below 1 heart
                    player.hurt(player.damageSources().magic(), 0.5f);
                }
            }
        } else {
            // Clear stacks when effect is gone
            BLOOD_RAGE_STACKS.remove(player.getUUID());
        }
    }

    // ==================== Helper ====================

    private static boolean hasAlchemyEffect(LivingEntity entity, java.util.function.Supplier<MobEffect> effectSupplier) {
        try {
            Holder<MobEffect> holder = AlchemyRegistry.holderOf(effectSupplier);
            return entity.hasEffect(holder);
        } catch (Exception e) {
            return false;
        }
    }
}
