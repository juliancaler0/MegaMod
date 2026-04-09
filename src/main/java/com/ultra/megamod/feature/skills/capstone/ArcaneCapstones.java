package com.ultra.megamod.feature.skills.capstone;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.ChatFormatting;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.enchanting.EnchantmentLevelSetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = "megamod")
public class ArcaneCapstones {

    // ==================== Mana Surge (mana_weaver_4) ====================
    // Tracks magic casts: player UUID -> [castCount, firstCastTick]
    private static final Map<UUID, long[]> MANA_SURGE_TRACKER = new HashMap<>();
    private static final int MANA_SURGE_THRESHOLD = 3;
    private static final long MANA_SURGE_WINDOW = 200L; // 10 seconds

    // ==================== Elemental Mastery (spell_blade_4) ====================
    // Tracks hit count for element cycling: player UUID -> hitCount
    private static final Map<UUID, Integer> ELEMENTAL_HIT_COUNTER = new HashMap<>();

    // ==================== Spectral Familiar (summoner_4) ====================
    // Tracks last attack time: player UUID -> lastAttackTick
    private static final Map<UUID, Long> FAMILIAR_COMBAT_TRACKER = new HashMap<>();

    // ==================== Arcane Resonance (relic_lore_4) ====================

    /**
     * On magic damage dealt by a player with this node: increase damage by 25%.
     * Detects magic damage via the damage source type containing "magic" or "indirect_magic".
     */
    @SubscribeEvent
    public static void onDamagePreArcaneResonance(LivingDamageEvent.Pre event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide()) return;

        DamageSource source = event.getSource();
        Entity sourceEntity = source.getEntity();
        if (!(sourceEntity instanceof ServerPlayer player)) return;
        if (!CapstoneManager.hasCapstoneTrigger(player, "relic_lore_4")) return;
        boolean enhanced = CapstoneManager.hasCapstoneTrigger(player, "relic_lore_5");

        // Check if damage type is magic-related
        String damageTypeKey = source.type().msgId();
        if (!damageTypeKey.contains("magic") && !damageTypeKey.contains("indirect_magic")
                && !damageTypeKey.contains("thorns") && !damageTypeKey.contains("wither")) {
            return;
        }

        float originalDamage = event.getOriginalDamage();
        float boosted = originalDamage * (enhanced ? 1.40f : 1.25f);
        event.setNewDamage(boosted);

        ServerLevel level = (ServerLevel) player.level();
        level.sendParticles(ParticleTypes.ENCHANT, target.getX(), target.getY() + 1.0,
                target.getZ(), 6, 0.3, 0.5, 0.3, 0.5);
    }

    // ==================== Enchantment Savant (enchanter_4) ====================

    /**
     * On enchantment level calculation: if player has this node, increase enchant level by 1.
     */
    @SubscribeEvent
    public static void onEnchantLevelSavant(EnchantmentLevelSetEvent event) {
        if (event.getLevel().isClientSide()) return;
        // Find nearest player to the enchanting table position
        Player nearest = event.getLevel().getNearestPlayer(
                event.getPos().getX() + 0.5, event.getPos().getY() + 0.5, event.getPos().getZ() + 0.5, 5.0, false);
        if (!(nearest instanceof ServerPlayer serverPlayer)) return;
        if (!CapstoneManager.hasCapstoneTrigger(serverPlayer, "enchanter_4")) return;
        boolean enhanced = CapstoneManager.hasCapstoneTrigger(serverPlayer, "enchanter_5");

        int currentLevel = event.getEnchantLevel();
        event.setEnchantLevel(currentLevel + (enhanced ? 3 : 2));
    }

    // ==================== Mana Surge (mana_weaver_4) ====================

    /**
     * On magic damage dealt: track frequency of magic hits. After 3 magic hits within 10s,
     * grant Haste II for 3s as a "Mana Surge" indicator buff.
     */
    @SubscribeEvent
    public static void onDamagePostManaSurge(LivingDamageEvent.Post event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide()) return;

        Entity sourceEntity = event.getSource().getEntity();
        if (!(sourceEntity instanceof ServerPlayer player)) return;
        if (!CapstoneManager.hasCapstoneTrigger(player, "mana_weaver_4")) return;
        boolean enhanced = CapstoneManager.hasCapstoneTrigger(player, "mana_weaver_5");

        // Only track magic-type damage
        String damageTypeKey = event.getSource().type().msgId();
        if (!damageTypeKey.contains("magic") && !damageTypeKey.contains("indirect_magic")
                && !damageTypeKey.contains("wither")) {
            return;
        }

        ServerLevel level = (ServerLevel) player.level();
        long currentTick = level.getGameTime();
        UUID playerId = player.getUUID();

        int surgeThreshold = enhanced ? 2 : MANA_SURGE_THRESHOLD;
        int surgeDuration = enhanced ? 100 : 60;

        long[] tracker = MANA_SURGE_TRACKER.get(playerId);
        if (tracker == null || (currentTick - tracker[1]) > MANA_SURGE_WINDOW) {
            // Reset tracker
            MANA_SURGE_TRACKER.put(playerId, new long[]{1, currentTick});
        } else {
            tracker[0]++;
            if (tracker[0] >= surgeThreshold) {
                // Mana Surge activates! Grant Strength I + Speed I for 3s (or 5s if enhanced)
                player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, surgeDuration, 0, false, true, true));
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, surgeDuration, 0, false, true, true));

                level.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE,
                        SoundSource.PLAYERS, 0.6f, 1.8f);
                level.sendParticles(ParticleTypes.WITCH, player.getX(), player.getY() + 1.0,
                        player.getZ(), 15, 0.5, 0.8, 0.5, 0.1);

                player.sendSystemMessage(Component.literal("Mana Surge activated!")
                        .withStyle(ChatFormatting.LIGHT_PURPLE));

                // Reset tracker
                MANA_SURGE_TRACKER.put(playerId, new long[]{0, currentTick});
            }
        }
    }

    // ==================== Elemental Mastery (spell_blade_4) ====================

    /**
     * On melee damage dealt: every 3rd hit cycles through elemental effects:
     * 1st cycle = fire (ignite 3s), 2nd = lightning (extra 2 damage), 3rd = shadow (Wither I 3s).
     */
    @SubscribeEvent
    public static void onDamagePostElementalMastery(LivingDamageEvent.Post event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide()) return;

        Entity sourceEntity = event.getSource().getEntity();
        if (!(sourceEntity instanceof ServerPlayer player)) return;
        if (!CapstoneManager.hasCapstoneTrigger(player, "spell_blade_4")) return;
        boolean enhanced = CapstoneManager.hasCapstoneTrigger(player, "spell_blade_5");

        // Only on direct melee attacks (not projectiles or magic)
        Entity directEntity = event.getSource().getDirectEntity();
        if (directEntity != sourceEntity) return; // Projectile or indirect damage

        UUID playerId = player.getUUID();
        int hitCount = ELEMENTAL_HIT_COUNTER.getOrDefault(playerId, 0) + 1;
        ELEMENTAL_HIT_COUNTER.put(playerId, hitCount);

        // Every 3rd (or 2nd if enhanced) hit triggers an elemental effect
        int triggerInterval = enhanced ? 2 : 3;
        if (hitCount % triggerInterval != 0) return;

        ServerLevel level = (ServerLevel) player.level();
        int cycle = (hitCount / triggerInterval) % 3; // 0 = fire, 1 = lightning, 2 = shadow

        switch (cycle) {
            case 0 -> {
                // Fire: ignite target for 3 seconds
                target.igniteForSeconds(3);
                level.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + 1.0,
                        target.getZ(), 10, 0.3, 0.5, 0.3, 0.05);
                level.playSound(null, target.blockPosition(), SoundEvents.FIRECHARGE_USE,
                        SoundSource.PLAYERS, 0.7f, 1.2f);
            }
            case 1 -> {
                // Lightning: extra 2 damage + visual
                target.hurt(player.damageSources().magic(), 2.0f);
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1.0,
                        target.getZ(), 12, 0.4, 0.6, 0.4, 0.1);
                level.playSound(null, target.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER,
                        SoundSource.PLAYERS, 0.3f, 2.0f);
            }
            case 2 -> {
                // Shadow: Wither I for 3 seconds (60 ticks)
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0, false, true, true));
                level.sendParticles(ParticleTypes.SOUL, target.getX(), target.getY() + 1.0,
                        target.getZ(), 8, 0.3, 0.5, 0.3, 0.03);
                level.playSound(null, target.blockPosition(), SoundEvents.WITHER_AMBIENT,
                        SoundSource.PLAYERS, 0.4f, 1.5f);
            }
        }
    }

    // ==================== Spectral Familiar (summoner_4) ====================

    /**
     * Track when players attack something (for familiar combat detection).
     */
    @SubscribeEvent
    public static void onDamagePostFamiliarTracker(LivingDamageEvent.Post event) {
        Entity sourceEntity = event.getSource().getEntity();
        if (!(sourceEntity instanceof ServerPlayer player)) return;
        if (!CapstoneManager.hasCapstoneTrigger(player, "summoner_4")) return;

        ServerLevel level = (ServerLevel) player.level();
        FAMILIAR_COMBAT_TRACKER.put(player.getUUID(), level.getGameTime());
    }

    /**
     * Every 60 ticks: if player has summoner_4 and is in combat (attacked something in last 5s),
     * deal 1.5 damage to the nearest hostile mob within 8 blocks + spawn particles.
     */
    @SubscribeEvent
    public static void onServerTickSpectralFamiliar(ServerTickEvent.Post event) {
        long gameTime = event.getServer().overworld().getGameTime();

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (!CapstoneManager.hasCapstoneTrigger(player, "summoner_4")) continue;
            boolean enhanced = CapstoneManager.hasCapstoneTrigger(player, "summoner_5");

            long tickInterval = enhanced ? 30L : 40L;
            if (gameTime % tickInterval != 0L) continue;

            Long lastAttack = FAMILIAR_COMBAT_TRACKER.get(player.getUUID());
            if (lastAttack == null) continue;

            ServerLevel level = (ServerLevel) player.level();
            long currentTick = level.getGameTime();

            // Must have attacked within last 5 seconds (100 ticks)
            if (currentTick - lastAttack > 100L) continue;

            // Find nearest hostile mob within 8 blocks
            AABB area = player.getBoundingBox().inflate(8.0);
            List<LivingEntity> nearbyMobs = level.getEntitiesOfClass(LivingEntity.class, area,
                    e -> e.isAlive() && !(e instanceof Player)
                            && e instanceof net.minecraft.world.entity.Mob);

            if (nearbyMobs.isEmpty()) continue;

            // Pick the closest one
            LivingEntity closest = null;
            double closestDist = Double.MAX_VALUE;
            for (LivingEntity mob : nearbyMobs) {
                double dist = mob.distanceToSqr(player);
                if (dist < closestDist) {
                    closestDist = dist;
                    closest = mob;
                }
            }

            if (closest != null) {
                // spell_range attribute extends familiar targeting range
                double spellRange = com.ultra.megamod.feature.attributes.AttributeHelper.getValue(
                        (LivingEntity) player, com.ultra.megamod.feature.attributes.MegaModAttributes.SPELL_RANGE);
                // Base range is 8, spell_range is a percentage bonus
                double effectiveRange = 8.0 * (1.0 + spellRange / 100.0);
                if (closest.distanceTo(player) > effectiveRange) {
                    closest = null; // out of range after spell_range check
                }
            }

            if (closest != null) {
                // Deal spectral damage (Spirit Conductor synergy amplifies)
                float familiarDamage = enhanced ? 10.0f : 6.0f;
                familiarDamage *= com.ultra.megamod.feature.skills.synergy.SynergyEffects.getFamiliarDamageMultiplier(player);
                closest.hurt(player.damageSources().magic(), familiarDamage);

                // Particles: END_ROD around the target
                level.sendParticles(ParticleTypes.END_ROD, closest.getX(), closest.getY() + 1.0,
                        closest.getZ(), 5, 0.3, 0.5, 0.3, 0.05);

                // Particles between player and target (familiar trail)
                double midX = (player.getX() + closest.getX()) / 2.0;
                double midY = (player.getY() + closest.getY()) / 2.0 + 1.0;
                double midZ = (player.getZ() + closest.getZ()) / 2.0;
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, midX, midY, midZ,
                        3, 0.5, 0.3, 0.5, 0.02);

                // Enhanced: chain to a second nearby target
                if (enhanced) {
                    LivingEntity chainTarget = null;
                    double chainDist = Double.MAX_VALUE;
                    for (LivingEntity mob : nearbyMobs) {
                        if (mob == closest || !mob.isAlive()) continue;
                        double dist = mob.distanceToSqr(closest);
                        if (dist < chainDist) {
                            chainDist = dist;
                            chainTarget = mob;
                        }
                    }
                    if (chainTarget != null) {
                        float chainDamage = familiarDamage * 0.6f;
                        chainTarget.hurt(player.damageSources().magic(), chainDamage);
                        // Chain lightning visual between primary and secondary target
                        level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                                (closest.getX() + chainTarget.getX()) / 2.0,
                                (closest.getY() + chainTarget.getY()) / 2.0 + 1.0,
                                (closest.getZ() + chainTarget.getZ()) / 2.0,
                                6, 0.4, 0.3, 0.4, 0.1);
                        level.sendParticles(ParticleTypes.END_ROD, chainTarget.getX(),
                                chainTarget.getY() + 1.0, chainTarget.getZ(),
                                3, 0.2, 0.4, 0.2, 0.03);
                    }
                }
            }
        }
    }
}
