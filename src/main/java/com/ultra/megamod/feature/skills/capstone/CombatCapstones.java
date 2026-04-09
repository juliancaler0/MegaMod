package com.ultra.megamod.feature.skills.capstone;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = "megamod")
public class CombatCapstones {

    // ==================== Exploit Weakness (tactician_4) ====================
    // Tracks hit counts: player UUID -> (target entity ID -> [hitCount, lastHitTick])
    private static final Map<UUID, Map<Integer, long[]>> HIT_TRACKER = new HashMap<>();
    private static final int EXPLOIT_HIT_THRESHOLD = 3;
    private static final long EXPLOIT_WINDOW_TICKS = 200L; // 10 seconds

    // ==================== Executioner (blade_mastery_4) ====================

    /**
     * On damage post: if target would drop below 15% HP and attacker has Executioner,
     * execute non-boss mobs instantly.
     */
    @SubscribeEvent
    public static void onDamagePostExecutioner(LivingDamageEvent.Post event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide()) return;

        Entity sourceEntity = event.getSource().getEntity();
        if (!(sourceEntity instanceof ServerPlayer player)) return;
        if (!CapstoneManager.hasCapstoneTrigger(player, "blade_mastery_4")) return;
        boolean enhanced = CapstoneManager.hasCapstoneTrigger(player, "blade_mastery_5");

        // Don't execute bosses
        if (target instanceof WitherBoss || target instanceof Warden) return;
        if (target instanceof Player) return;
        // Check for EnderDragon parts
        String typeName = target.getType().getDescriptionId();
        if (typeName.contains("ender_dragon")) return;

        float healthAfterDamage = target.getHealth();
        float maxHealth = target.getMaxHealth();

        // If target is below 15% (or 20% if enhanced) HP after taking damage, execute
        if (healthAfterDamage > 0 && healthAfterDamage < maxHealth * (enhanced ? 0.20f : 0.15f)) {
            target.kill((ServerLevel) target.level());
            ServerLevel level = (ServerLevel) player.level();
            level.playSound(null, target.blockPosition(), SoundEvents.WITHER_SKELETON_AMBIENT,
                    SoundSource.PLAYERS, 0.8f, 0.6f);
            level.sendParticles(ParticleTypes.SOUL, target.getX(), target.getY() + 0.5, target.getZ(),
                    8, 0.3, 0.5, 0.3, 0.02);
        }
    }

    // ==================== Deadeye (ranged_precision_4) ====================

    /**
     * On damage pre: if damage is from a projectile fired by a sneaking player with Deadeye,
     * increase projectile damage by 50%.
     */
    @SubscribeEvent
    public static void onDamagePreDeadeye(LivingDamageEvent.Pre event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide()) return;

        DamageSource source = event.getSource();
        Entity directEntity = source.getDirectEntity();
        Entity ownerEntity = source.getEntity();

        if (!(directEntity instanceof Projectile)) return;
        if (!(ownerEntity instanceof ServerPlayer player)) return;
        if (!CapstoneManager.hasCapstoneTrigger(player, "ranged_precision_4")) return;
        boolean enhanced = CapstoneManager.hasCapstoneTrigger(player, "ranged_precision_5");

        // Player must not be sprinting (sneaking or standing still is fine)
        if (player.isSprinting()) return;

        float originalDamage = event.getOriginalDamage();
        float bonusDamage = originalDamage * (enhanced ? 0.75f : 0.5f);
        event.setNewDamage(originalDamage + bonusDamage);

        ServerLevel level = (ServerLevel) player.level();
        level.playSound(null, target.blockPosition(), SoundEvents.ARROW_HIT_PLAYER,
                SoundSource.PLAYERS, 1.2f, 0.5f);
    }

    // ==================== Fortress (shield_wall_4) ====================

    /**
     * On shield block: if player has Fortress, deal 30% of blocked damage back to attacker
     * and apply Slowness I for 20 ticks.
     */
    @SubscribeEvent
    public static void onShieldBlockFortress(LivingShieldBlockEvent event) {
        LivingEntity blocker = event.getEntity();
        if (blocker.level().isClientSide()) return;
        if (!(blocker instanceof ServerPlayer player)) return;
        if (!event.getBlocked()) return;
        if (!CapstoneManager.hasCapstoneTrigger(player, "shield_wall_4")) return;
        boolean enhanced = CapstoneManager.hasCapstoneTrigger(player, "shield_wall_5");

        float blockedDamage = event.getBlockedDamage();
        if (blockedDamage <= 0) return;

        DamageSource damageSource = event.getDamageSource();
        Entity attacker = damageSource.getEntity();
        if (!(attacker instanceof LivingEntity livingAttacker)) return;

        // Deal 30% (or 50% if enhanced) of blocked damage back
        float reflectedDamage = blockedDamage * (enhanced ? 0.5f : 0.3f);
        livingAttacker.hurt(player.damageSources().thorns(player), reflectedDamage);

        // Apply Slowness I for 60 ticks (3s)
        livingAttacker.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 0, false, true, true));
        if (enhanced) {
            livingAttacker.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, false, true, true));
        }

        ServerLevel level = (ServerLevel) player.level();
        level.playSound(null, player.blockPosition(), SoundEvents.SHIELD_BLOCK.value(),
                SoundSource.PLAYERS, 1.0f, 0.5f);
        level.sendParticles(ParticleTypes.CRIT, livingAttacker.getX(), livingAttacker.getY() + 1.0,
                livingAttacker.getZ(), 6, 0.3, 0.3, 0.3, 0.1);
    }

    // ==================== Undying Rage (berserker_4) ====================

    private static final long UNDYING_RAGE_COOLDOWN = 6000L; // 5 minutes

    /**
     * On damage pre: if player would die and has Undying Rage (not on cooldown),
     * cancel lethal damage, set health to 1, grant Resistance I + Strength II for 3s.
     */
    @SubscribeEvent
    public static void onDamagePreUndyingRage(LivingDamageEvent.Pre event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        if (!(entity instanceof ServerPlayer player)) return;
        if (!CapstoneManager.hasCapstoneTrigger(player, "berserker_4")) return;
        boolean enhanced = CapstoneManager.hasCapstoneTrigger(player, "berserker_5");

        float currentHealth = player.getHealth();
        float incomingDamage = event.getOriginalDamage();

        // Only trigger if this damage would kill the player
        if (currentHealth - incomingDamage > 0) return;

        ServerLevel level = (ServerLevel) player.level();
        long currentTick = level.getGameTime();

        long cooldown = enhanced ? 3600L : 6000L;
        if (CapstoneManager.isOnCooldown(player.getUUID(), "undying_rage", currentTick, cooldown)) {
            return;
        }

        // Cancel lethal damage
        event.setNewDamage(0.0f);

        // Set health to 1
        player.setHealth(1.0f);

        // Apply buffs: Resistance I + Strength II for 60 ticks (3s)
        player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 60, 0, false, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 60, 1, false, true, true));

        // Set cooldown
        CapstoneManager.setCooldown(player.getUUID(), "undying_rage", currentTick);

        // Play totem sound and send totem animation packet
        level.playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE,
                SoundSource.PLAYERS, 1.0f, 1.0f);
        player.connection.send(new ClientboundEntityEventPacket(player, (byte) 35));

        // Red particles
        level.sendParticles(ParticleTypes.ANGRY_VILLAGER, player.getX(), player.getY() + 1.0,
                player.getZ(), 15, 0.5, 0.5, 0.5, 0.1);
    }

    // ==================== Exploit Weakness (tactician_4) ====================

    /**
     * On damage pre: track hits per target. On every 3rd consecutive hit within 10s
     * on the same target, double the damage.
     */
    @SubscribeEvent
    public static void onDamagePreExploit(LivingDamageEvent.Pre event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide()) return;

        Entity sourceEntity = event.getSource().getEntity();
        if (!(sourceEntity instanceof ServerPlayer player)) return;
        if (!CapstoneManager.hasCapstoneTrigger(player, "tactician_4")) return;
        boolean enhanced = CapstoneManager.hasCapstoneTrigger(player, "tactician_5");

        UUID playerId = player.getUUID();
        int targetId = target.getId();
        ServerLevel level = (ServerLevel) player.level();
        long currentTick = level.getGameTime();

        Map<Integer, long[]> playerHits = HIT_TRACKER.computeIfAbsent(playerId, k -> new HashMap<>());

        // Clean up expired entries
        Iterator<Map.Entry<Integer, long[]>> it = playerHits.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, long[]> entry = it.next();
            if (currentTick - entry.getValue()[1] > EXPLOIT_WINDOW_TICKS) {
                it.remove();
            }
        }

        long[] hitData = playerHits.get(targetId);
        if (hitData == null) {
            // First hit on this target
            playerHits.put(targetId, new long[]{1, currentTick});
        } else {
            hitData[0]++;
            hitData[1] = currentTick;

            int threshold = enhanced ? 2 : EXPLOIT_HIT_THRESHOLD;
            if (hitData[0] >= threshold) {
                // Every 3rd hit: double damage
                float originalDamage = event.getOriginalDamage();
                event.setNewDamage(originalDamage * 2.0f);

                // Reset counter
                hitData[0] = 0;

                // Visual/audio feedback
                level.playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT,
                        SoundSource.PLAYERS, 1.0f, 1.5f);
                level.sendParticles(ParticleTypes.ENCHANTED_HIT, target.getX(), target.getY() + 1.0,
                        target.getZ(), 10, 0.3, 0.5, 0.3, 0.1);
            }
        }
    }

    // ==================== Cleanup ====================

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID uuid = event.getEntity().getUUID();
        HIT_TRACKER.remove(uuid);
        CapstoneManager.onPlayerLogout(uuid);
    }
}
