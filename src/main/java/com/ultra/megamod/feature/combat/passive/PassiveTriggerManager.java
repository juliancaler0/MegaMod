package com.ultra.megamod.feature.combat.passive;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.animation.SpellAnimationPayload;
import com.ultra.megamod.feature.combat.passive.PassiveTriggerRegistry.PassiveTrigger;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages passive weapon triggers — automatic effects that fire with a % chance
 * when weapons are used (melee hit, arrow hit, spell cast, shield block).
 */
@EventBusSubscriber(modid = MegaMod.MODID)
public class PassiveTriggerManager {

    private static final Map<String, Long> cooldowns = new ConcurrentHashMap<>();

    public static void onMeleeHit(ServerPlayer attacker, LivingEntity target, ServerLevel level) {
        String weaponId = getWeaponId(attacker.getMainHandItem());
        if (weaponId.isEmpty()) return;
        for (PassiveTrigger trigger : PassiveTriggerRegistry.getTriggersForWeapon(weaponId, PassiveTriggerType.MELEE_IMPACT)) {
            tryFireTrigger(attacker, target, trigger, level);
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        if (!(event.getSource().getDirectEntity() instanceof Arrow arrow)) return;
        if (!(arrow.getOwner() instanceof ServerPlayer attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (!(attacker.level() instanceof ServerLevel level)) return;
        String weaponId = getWeaponId(attacker.getMainHandItem());
        if (weaponId.isEmpty()) return;
        for (PassiveTrigger trigger : PassiveTriggerRegistry.getTriggersForWeapon(weaponId, PassiveTriggerType.ARROW_HIT)) {
            tryFireTrigger(attacker, target, trigger, level);
        }
    }

    @SubscribeEvent
    public static void onShieldBlock(LivingShieldBlockEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer blocker)) return;
        if (!(blocker.level() instanceof ServerLevel level)) return;
        String shieldId = getWeaponId(blocker.getOffhandItem());
        if (shieldId.isEmpty()) shieldId = getWeaponId(blocker.getMainHandItem());
        if (shieldId.isEmpty()) return;
        LivingEntity attacker = event.getDamageSource().getEntity() instanceof LivingEntity le ? le : null;
        for (PassiveTrigger trigger : PassiveTriggerRegistry.getTriggersForWeapon(shieldId, PassiveTriggerType.ON_BLOCK)) {
            tryFireTrigger(blocker, attacker, trigger, level);
        }
    }

    public static void onSpellCast(ServerPlayer caster, ServerLevel level) {
        String weaponId = getWeaponId(caster.getMainHandItem());
        if (weaponId.isEmpty()) return;
        for (PassiveTrigger trigger : PassiveTriggerRegistry.getTriggersForWeapon(weaponId, PassiveTriggerType.SPELL_CAST)) {
            tryFireTrigger(caster, null, trigger, level);
        }
    }

    private static void tryFireTrigger(ServerPlayer player, LivingEntity target,
                                        PassiveTrigger trigger, ServerLevel level) {
        String cdKey = player.getUUID() + ":" + trigger.effectId();
        Long expiry = cooldowns.get(cdKey);
        if (expiry != null && level.getGameTime() < expiry) return;
        // Apply global admin-tunable multiplier so the combat tab slider can scale
        // all passive fire rates at once. 0 disables, 1 = vanilla, 5 = 5x.
        float procScale = com.ultra.megamod.feature.combat.animation.config.BetterCombatConfig.passive_proc_multiplier;
        float effectiveChance = Math.min(1.0f, trigger.chance() * procScale);
        if (player.getRandom().nextFloat() > effectiveChance) return;
        cooldowns.put(cdKey, level.getGameTime() + trigger.cooldownTicks());
        broadcastPassiveAnimation(player, trigger.effectId());
        executeEffect(player, target, trigger.effectId(), level);
    }

    /**
     * Broadcast a brief cast-release animation to nearby clients when a passive fires.
     * Picks an animation that fits the effect category (AOE burst, buff flare, single-target hit).
     */
    private static void broadcastPassiveAnimation(ServerPlayer player, String effectId) {
        String animId = animationForEffect(effectId);
        if (animId == null) return;
        SpellAnimationPayload payload = new SpellAnimationPayload(
                player.getId(),
                1, // RELEASE — one-shot cast animation
                animId,
                1.5f,  // play slightly sped up so it doesn't block attack timing
                false
        );
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, payload);
    }

    private static String animationForEffect(String effectId) {
        return switch (effectId) {
            // Area bursts — both hands flare outward
            case "exploding_hit", "shockwave", "flame_cloud", "poison_cloud",
                 "scorched_earth" -> "off_hand_area_release";
            // Self buffs / heals — shout-style pose
            case "radiance_heal", "rampaging_buff", "guarding_shield",
                 "unyielding_shield" -> "one_handed_shout_release";
            // Single-target projectile-style passives — one-hand thrust
            case "wither_apply", "slowing_hit", "stunning_hit", "leeching_hit",
                 "bonus_shot" -> "one_handed_projectile_release";
            // Block passives — short flare
            case "spiked_shield" -> "off_hand_area_release";
            default -> null;
        };
    }

    private static float getPlayerAttackDamage(ServerPlayer player) {
        return (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    private static void executeEffect(ServerPlayer player, LivingEntity target, String effectId, ServerLevel level) {
        switch (effectId) {
            case "radiance_heal" -> {
                float heal = player.getMaxHealth() * 0.15f;
                player.heal(heal);
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1, player.getZ(), 8, 0.5, 0.5, 0.5, 0);
                for (Player ally : level.getEntitiesOfClass(Player.class, player.getBoundingBox().inflate(3), p -> p != player && p.isAlive()))
                    ally.heal(heal * 0.5f);
            }
            case "wither_apply" -> { if (target != null) target.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 1)); }
            case "rampaging_buff" -> player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 100, 0));
            case "flame_cloud" -> {
                if (target != null) {
                    target.igniteForSeconds(4);
                    level.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + 0.5, target.getZ(), 30, 1.5, 0.5, 1.5, 0.05);
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(2), e -> e != player && e.isAlive()))
                        e.igniteForSeconds(2);
                }
            }
            case "poison_cloud" -> {
                if (target != null)
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(2.5), e -> e != player && e.isAlive()))
                        e.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1));
            }
            case "bonus_shot" -> {
                if (target != null) {
                    Vec3 dir = target.position().add(0, target.getBbHeight() * 0.5, 0).subtract(player.getEyePosition()).normalize();
                    Arrow arrow = new Arrow(level, player, player.getMainHandItem().copy(), null);
                    arrow.shoot(dir.x, dir.y, dir.z, 3.0f, 2.0f);
                    arrow.setBaseDamage(1.2); // Reduced damage bonus shot
                    arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                    level.addFreshEntity(arrow);
                }
            }
            case "spiked_shield" -> { if (target != null) target.hurt(player.damageSources().thorns(player), 4.0f); }
            case "guarding_shield" -> player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 40, 1));
            case "unyielding_shield" -> {
                if (target != null) {
                    Vec3 push = target.position().subtract(player.position()).normalize();
                    target.push(push.x * 0.8, 0.2, push.z * 0.8);
                }
            }
            case "exploding_hit" -> {
                if (target != null) {
                    level.sendParticles(ParticleTypes.EXPLOSION, target.getX(), target.getY() + 1, target.getZ(), 1, 0, 0, 0, 0);
                    level.playSound(null, target.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.6f, 1.2f);
                    float dmg = getPlayerAttackDamage(player) * 0.3f;
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(2.5), e -> e != player && e.isAlive())) {
                        e.hurt(player.damageSources().explosion(null, player), dmg);
                        Vec3 push = e.position().subtract(target.position()).normalize().scale(0.5);
                        e.push(push.x, 0.3, push.z);
                    }
                }
            }
            case "slowing_hit" -> { if (target != null) target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1)); }
            case "leeching_hit" -> { if (target != null) player.heal(getPlayerAttackDamage(player) * 0.2f); }
            case "shockwave" -> {
                Vec3 c = player.position();
                level.sendParticles(ParticleTypes.SWEEP_ATTACK, c.x, c.y + 0.5, c.z, 12, 2.0, 0.2, 2.0, 0);
                float dmg = getPlayerAttackDamage(player) * 0.25f;
                for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(3), e -> e != player && e.isAlive())) {
                    e.hurt(player.damageSources().playerAttack(player), dmg);
                    Vec3 push = e.position().subtract(c).normalize().scale(0.8);
                    e.push(push.x, 0.3, push.z);
                }
            }
            case "stunning_hit" -> {
                if (target != null) {
                    target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 4));
                    target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 1));
                }
            }
        }
    }

    private static String getWeaponId(ItemStack stack) {
        if (stack.isEmpty()) return "";
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    public static void cleanupCooldowns(long currentTick) {
        cooldowns.entrySet().removeIf(e -> e.getValue() < currentTick);
    }
}
