package com.ultra.megamod.feature.combat.spell;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.attributes.AttributeHelper;
import com.ultra.megamod.feature.attributes.network.CombatTextSender;
import com.ultra.megamod.feature.combat.spell.SpellDefinition.*;
import com.ultra.megamod.feature.computer.admin.AdminSystem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Executes spells from SpellRegistry. This is the main entry point for casting spells
 * from relic abilities, weapon skills, or direct spell casting.
 */
public class SpellExecutor {

    // Cooldown tracking: playerUUID -> spellId -> expiry tick
    private static final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();

    /**
     * Attempt to cast a spell for a player.
     * @return true if the spell was cast successfully
     */
    public static boolean cast(ServerPlayer player, String spellId) {
        SpellDefinition spell = SpellRegistry.get(spellId);
        if (spell == null) {
            MegaMod.LOGGER.warn("Unknown spell: {}", spellId);
            return false;
        }
        return cast(player, spell);
    }

    public static boolean cast(ServerPlayer player, SpellDefinition spell) {
        ServerLevel level = (ServerLevel) player.level();
        boolean isAdmin = AdminSystem.isAdmin(player);

        // Check cooldown (admins bypass cooldowns)
        if (!isAdmin && isOnCooldown(player.getUUID(), spell.id())) return false;

        // Rune cost: tier 2+ spells require 1 rune, tier 3+ require 2 runes of matching school
        // Spell Infinity enchantment bypasses rune cost
        boolean hasSpellInfinity = hasEnchantment(player, "megamod:spell_infinity");
        if (!isAdmin && !hasSpellInfinity && spell.tier() >= 2 && spell.school() != null) {
            int runeCost = spell.tier() >= 3 ? 2 : 1;
            if (!com.ultra.megamod.feature.combat.runes.RuneRegistry.consumeRune(player, spell.school().name(), runeCost)) {
                player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("Not enough " + spell.school().name().toLowerCase() + " runes! (need " + runeCost + ")")
                        .withStyle(net.minecraft.ChatFormatting.RED), true);
                return false;
            }
        }

        // Apply cooldown (with CDR) — skip for admins
        if (!isAdmin) {
            float effectiveCooldown = spell.cooldownSeconds() * (float) AttributeHelper.getCooldownMultiplier(player);
            if (effectiveCooldown > 0) {
                setCooldown(player.getUUID(), spell.id(), level.getServer().getTickCount(), (int)(effectiveCooldown * 20));
            }
        }

        // Execute based on delivery type
        switch (spell.delivery()) {
            case DIRECT -> executeDirect(player, spell, level);
            case PROJECTILE -> executeProjectile(player, spell, level);
            case BEAM -> executeBeam(player, spell, level);
            case AREA -> executeArea(player, spell, level);
            case CLOUD -> executeCloud(player, spell, level);
            case TELEPORT -> executeTeleport(player, spell, level);
            case MELEE -> executeMelee(player, spell, level);
            case ARROW -> executeArrow(player, spell, level);
            case SPAWN -> executeSpawn(player, spell, level);
        }

        // Apply food exhaustion cost — skip for channeled spells (exhaust is applied once
        // when the channel starts in SpellCastManager.startCast, not on every sub-tick)
        if (spell.exhaust() > 0 && spell.castMode() != SpellDefinition.CastMode.CHANNELED) {
            player.causeFoodExhaustion(spell.exhaust() * 40f);
        }

        // Play release/cast sound — use SpellVisuals if configured, else fallback to school default
        String releaseSoundId = SpellCastManager.getReleaseSound(spell);
        float pitch = 1.0f + player.getRandom().nextFloat() * 0.2f;
        if (releaseSoundId != null) {
            try {
                var soundId = net.minecraft.resources.Identifier.fromNamespaceAndPath("megamod", releaseSoundId.replace("combat.", "combat/"));
                var soundEvent = net.minecraft.sounds.SoundEvent.createVariableRangeEvent(soundId);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    soundEvent, SoundSource.PLAYERS, 0.8f, pitch);
            } catch (Exception e) {
                // Fallback to school default
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    getSchoolCastSound(spell.school()), SoundSource.PLAYERS, 0.8f, pitch);
            }
        } else {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                getSchoolCastSound(spell.school()), SoundSource.PLAYERS, 0.8f, pitch);
        }

        // Notify quest system of successful spell cast
        try {
            com.ultra.megamod.feature.quests.QuestEventListener.onSpellCast(player, spell.id());
        } catch (Exception ignored) {}

        // Notify encyclopedia discovery system
        try {
            com.ultra.megamod.feature.encyclopedia.DiscoveryManager.onSpellCast(player.getUUID(), spell.id());
        } catch (Exception ignored) {}

        return true;
    }

    // ─── Delivery implementations ───

    private static void executeDirect(ServerPlayer player, SpellDefinition spell, ServerLevel level) {
        // Apply to aimed target or self
        LivingEntity target = getAimedTarget(player, spell.range());
        if (target == null && spell.target() == TargetType.SELF) target = player;
        if (target == null) return;

        applyImpact(player, target, spell, level);
    }

    private static void executeProjectile(ServerPlayer player, SpellDefinition spell, ServerLevel level) {
        // Spawn a SpellProjectile entity
        SpellProjectileEntity projectile = new SpellProjectileEntity(level, player, spell);
        Vec3 look = player.getLookAngle();
        float velocity = spell.projectile() != null ? spell.projectile().velocity() : 1.5f;
        projectile.shoot(look.x, look.y, look.z, velocity, 0);
        level.addFreshEntity(projectile);
    }

    private static void executeBeam(ServerPlayer player, SpellDefinition spell, ServerLevel level) {
        // Raycast and damage entities along the beam
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        float range = spell.range();

        // Track already-hit entities to prevent large entities spanning multiple blocks
        // from being damaged more than once per beam cast
        java.util.Set<UUID> hitEntities = new java.util.HashSet<>();

        // Determine the actual beam endpoint (stop at first block hit or max range)
        Vec3 maxEnd = start.add(look.scale(range));
        net.minecraft.world.phys.BlockHitResult blockHit = level.clip(
            new net.minecraft.world.level.ClipContext(start, maxEnd,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE, player));
        Vec3 beamEnd = blockHit.getType() != net.minecraft.world.phys.HitResult.Type.MISS
                ? blockHit.getLocation() : maxEnd;

        for (float d = 1; d <= range; d += 1.0f) {
            Vec3 point = start.add(look.scale(d));
            if (point.distanceToSqr(start) > beamEnd.distanceToSqr(start)) break;

            // School-specific particles at intervals along beam
            net.minecraft.core.particles.ParticleOptions particle = getSchoolParticle(spell.school());
            level.sendParticles(particle, point.x, point.y, point.z, 1, 0.05, 0.05, 0.05, 0);

            // Check for entities at this point
            AABB box = new AABB(point.x - 0.5, point.y - 0.5, point.z - 0.5,
                                point.x + 0.5, point.y + 0.5, point.z + 0.5);
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e != player && e.isAlive());
            for (LivingEntity entity : entities) {
                if (hitEntities.contains(entity.getUUID())) continue;
                hitEntities.add(entity.getUUID());
                applyImpact(player, entity, spell, level);
            }
        }

        // Send beam visual to nearby clients
        int schoolOrd = spell.school() != null ? spell.school().ordinal() : 0;
        BeamSyncPayload beamPayload = new BeamSyncPayload(
                start.x, start.y, start.z,
                beamEnd.x, beamEnd.y, beamEnd.z,
                schoolOrd, 400 // 400ms beam visual duration
        );
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, beamPayload);
    }

    private static net.minecraft.sounds.SoundEvent getSchoolCastSound(SpellSchool school) {
        if (school == null) return SoundEvents.EVOKER_CAST_SPELL;
        return switch (school) {
            case FIRE -> SoundEvents.FIRECHARGE_USE;
            case FROST -> SoundEvents.GLASS_BREAK;
            case ARCANE -> SoundEvents.EVOKER_CAST_SPELL;
            case HEALING -> SoundEvents.PLAYER_LEVELUP;
            case LIGHTNING -> SoundEvents.TRIDENT_THUNDER.value();
            case SOUL -> SoundEvents.SOUL_ESCAPE.value();
            case PHYSICAL_MELEE -> SoundEvents.PLAYER_ATTACK_STRONG;
            case PHYSICAL_RANGED -> SoundEvents.ARROW_SHOOT;
        };
    }

    private static net.minecraft.core.particles.ParticleOptions getSchoolParticle(SpellSchool school) {
        if (school == null) return ParticleTypes.END_ROD;
        return switch (school) {
            case FIRE -> ParticleTypes.FLAME;
            case FROST -> ParticleTypes.SNOWFLAKE;
            case ARCANE -> ParticleTypes.ENCHANT;
            case HEALING -> ParticleTypes.HAPPY_VILLAGER;
            case LIGHTNING -> ParticleTypes.ELECTRIC_SPARK;
            case SOUL -> ParticleTypes.SOUL;
            case PHYSICAL_MELEE -> ParticleTypes.CRIT;
            case PHYSICAL_RANGED -> ParticleTypes.CRIT;
        };
    }

    private static void executeArea(ServerPlayer player, SpellDefinition spell, ServerLevel level) {
        Vec3 center = player.position();
        // If AIM target, center on aimed location
        if (spell.target() == TargetType.AIM) {
            LivingEntity target = getAimedTarget(player, spell.range());
            if (target != null) center = target.position();
        }

        AreaConfig area = spell.area();
        float hRange = area != null ? area.horizontalRange() : spell.range();
        float vRange = area != null ? area.verticalRange() : hRange * 0.5f;

        AABB box = new AABB(center.x - hRange, center.y - vRange, center.z - hRange,
                            center.x + hRange, center.y + vRange, center.z + hRange);

        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, box,
            e -> e.isAlive() && (area == null || area.includeCaster() || e != player));

        // Angle check
        float angle = area != null ? area.angleDegrees() : 360;
        Vec3 forward = player.getLookAngle().normalize();
        Vec3 finalCenter = center;

        for (LivingEntity entity : entities) {
            if (angle < 360) {
                Vec3 toTarget = entity.position().subtract(finalCenter).normalize();
                double dot = forward.dot(toTarget);
                if (Math.toDegrees(Math.acos(dot)) > angle / 2.0) continue;
            }
            applyImpact(player, entity, spell, level);
        }

        // AOE particles
        level.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y + 0.5, center.z, 5, hRange * 0.5, 0.5, hRange * 0.5, 0);
    }

    private static void executeCloud(ServerPlayer player, SpellDefinition spell, ServerLevel level) {
        Vec3 pos = player.position();
        if (spell.target() == TargetType.AIM) {
            LivingEntity target = getAimedTarget(player, spell.range());
            if (target != null) pos = target.position();
        }

        SpellCloudEntity cloud = new SpellCloudEntity(level, player, spell, pos);
        level.addFreshEntity(cloud);
    }

    private static void executeTeleport(ServerPlayer player, SpellDefinition spell, ServerLevel level) {
        if (spell.target() == TargetType.AIM) {
            // Teleport behind aimed target
            LivingEntity target = getAimedTarget(player, spell.range());
            if (target != null) {
                Vec3 behindTarget = target.position().add(target.getLookAngle().scale(-1.5));
                player.teleportTo(behindTarget.x, behindTarget.y, behindTarget.z);
            } else {
                // Teleport forward
                Vec3 forward = player.position().add(player.getLookAngle().scale(spell.range()));
                player.teleportTo(forward.x, player.getY(), forward.z);
            }
        } else {
            // Teleport forward
            Vec3 forward = player.position().add(player.getLookAngle().scale(spell.range()));
            player.teleportTo(forward.x, player.getY(), forward.z);
        }

        // Apply self-effects
        applyEffects(player, player, spell);
        level.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1, player.getZ(), 20, 0.5, 1, 0.5, 0);
    }

    private static void executeMelee(ServerPlayer player, SpellDefinition spell, ServerLevel level) {
        // Enhanced melee strike — damages nearby entity in front
        LivingEntity target = getAimedTarget(player, 5);
        if (target != null) {
            applyImpact(player, target, spell, level);
        }
    }

    private static void executeArrow(ServerPlayer player, SpellDefinition spell, ServerLevel level) {
        // Spawn enhanced arrows with attribute-scaled damage
        int arrowCount = 3; // barrage-style
        Vec3 look = player.getLookAngle();
        float baseVelocity = spell.projectile() != null ? spell.projectile().velocity() : 3.0f;

        // Scale damage with RANGED_DAMAGE attribute instead of hardcoded value
        double rangedPower = AttributeHelper.getValue(player, com.ultra.megamod.feature.attributes.MegaModAttributes.RANGED_DAMAGE);
        double baseDmg = Math.max(2.0, 2.0 + rangedPower * 0.1) * spell.damageCoefficient();

        // Scale velocity with ranged power (minor boost for high-stat players)
        float velocityMult = 1.0f + (float)(rangedPower * 0.003);

        for (int i = 0; i < arrowCount; i++) {
            Arrow arrow = new Arrow(level, player, player.getMainHandItem().copy(), null);
            float spread = (i - 1) * 5.0f;
            arrow.shoot(look.x, look.y, look.z, baseVelocity * velocityMult, spread);
            arrow.setBaseDamage(baseDmg);
            arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            level.addFreshEntity(arrow);
        }
    }

    private static void executeSpawn(ServerPlayer player, SpellDefinition spell, ServerLevel level) {
        // Spawn a temporary barrier entity using an invisible ArmorStand as marker
        Vec3 spawnPos = player.position();
        if (spell.target() == TargetType.AIM) {
            // Place barrier where the player is looking (within range)
            Vec3 start = player.getEyePosition();
            Vec3 look = player.getLookAngle();
            float range = spell.range() > 0 ? spell.range() : 4;

            // Raycast to find ground or use max range
            Vec3 end = start.add(look.scale(range));
            net.minecraft.world.phys.BlockHitResult blockHit = level.clip(
                new net.minecraft.world.level.ClipContext(start, end,
                    net.minecraft.world.level.ClipContext.Block.COLLIDER,
                    net.minecraft.world.level.ClipContext.Fluid.NONE, player));
            spawnPos = blockHit.getLocation();
        }

        // Create invisible armor stand as barrier marker
        net.minecraft.world.entity.decoration.ArmorStand barrier =
            new net.minecraft.world.entity.decoration.ArmorStand(level, spawnPos.x, spawnPos.y, spawnPos.z);
        barrier.setInvisible(true);
        barrier.setNoGravity(true);
        barrier.setInvulnerable(true);
        barrier.setCustomName(net.minecraft.network.chat.Component.literal(
            "\u00a7b\u00a7l" + spell.name() + " [" + player.getGameProfile().name() + "]"));
        barrier.setCustomNameVisible(true);
        barrier.setSilent(true);
        // Mark as spell barrier via tags
        barrier.addTag("megamod_spell_barrier");
        barrier.addTag("caster_" + player.getUUID());

        level.addFreshEntity(barrier);

        // Duration: use cloud TTL if defined, otherwise 10 seconds
        int durationTicks = spell.cloud() != null ? (int)(spell.cloud().timeToLiveSeconds() * 20) : 200;
        float knockbackRadius = spell.range() > 0 ? spell.range() : 3;

        // Schedule barrier tick logic — uses a repeating task via the level's tick
        UUID barrierId = barrier.getUUID();
        UUID casterId = player.getUUID();

        // Register a ticker that runs every tick for the barrier's lifetime
        SpellBarrierManager.registerBarrier(level, barrierId, casterId, spawnPos, knockbackRadius, durationTicks);

        // Particles at spawn
        level.sendParticles(ParticleTypes.END_ROD, spawnPos.x, spawnPos.y + 1, spawnPos.z,
            15, 0.5, 1.0, 0.5, 0.05);

        // Apply self-effects if any
        applyEffects(player, player, spell);
    }

    // ─── Impact application ───

    public static void applyImpact(ServerPlayer caster, LivingEntity target, SpellDefinition spell, ServerLevel level) {
        // Play impact sound from visuals
        String impactSoundId = SpellCastManager.getImpactSound(spell);
        if (impactSoundId != null) {
            try {
                var soundId = net.minecraft.resources.Identifier.fromNamespaceAndPath("megamod", impactSoundId.replace("combat.", "combat/"));
                var soundEvent = net.minecraft.sounds.SoundEvent.createVariableRangeEvent(soundId);
                level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    soundEvent, SoundSource.PLAYERS, 0.6f, 1.0f + level.random.nextFloat() * 0.2f);
            } catch (Exception ignored) {}
        }

        // Damage
        if (spell.damageCoefficient() > 0 && target != caster) {
            double[] result = AttributeHelper.calculateSpellDamage(caster, spell.school().name(), spell.damageCoefficient());
            float damage = (float) result[0];
            if (damage < 1) damage = 1;

            // Apply vulnerability (effect-based + mob-type weakness) before resistance
            float vulnerability = SpellVulnerability.getTotalVulnerability(target, spell.school());
            damage *= vulnerability;

            // Apply target's elemental resistance as percentage reduction (capped at 75%)
            double resistance = AttributeHelper.getTargetResistance(target, spell.school());
            if (resistance > 0) {
                float resistMult = 1.0f - (float) Math.min(resistance / 100.0, 0.75);
                damage *= resistMult;
                if (damage < 1) damage = 1;
            }

            DamageSource source = getSpellDamageSource(caster, spell.school());
            target.hurt(source, damage);
            if (spell.knockback() > 0) {
                Vec3 knockDir = target.position().subtract(caster.position()).normalize();
                target.push(knockDir.x * spell.knockback() * 0.5, 0.1 * spell.knockback(), knockDir.z * spell.knockback() * 0.5);
            }

            // Show spell damage combat text (crit or elemental)
            boolean isCrit = result[1] > 0.5;
            if (isCrit) {
                CombatTextSender.sendCrit(target, damage);
            } else {
                CombatTextSender.sendElemental(target, damage, schoolToElement(spell.school()));
            }
        }

        // Healing
        if (spell.healCoefficient() > 0) {
            double healPower = AttributeHelper.getSpellPower(caster, "HEALING");
            float healAmount = (float)(healPower * spell.healCoefficient());
            if (healAmount < 1) healAmount = spell.healCoefficient() * 2; // minimum heal
            target.heal(healAmount);

            // Show healing combat text
            CombatTextSender.sendLifesteal(target, healAmount);
        }

        // Status effects
        applyEffects(caster, target, spell);
    }

    private static void applyEffects(ServerPlayer caster, LivingEntity target, SpellDefinition spell) {
        for (StatusEffectDef effectDef : spell.effects()) {
            try {
                var effectHolder = net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT
                    .get(net.minecraft.resources.Identifier.parse(effectDef.effectId()));
                if (effectHolder.isPresent()) {
                    target.addEffect(new MobEffectInstance(
                        effectHolder.get(), effectDef.durationTicks(), effectDef.amplifier(),
                        false, !effectDef.harmful(), true));
                }
            } catch (Exception e) {
                // Effect not registered yet — skip silently
            }
        }
    }

    // ─── School-to-element mapping for combat text ───

    private static String schoolToElement(SpellSchool school) {
        return switch (school) {
            case FIRE -> "fire";
            case FROST -> "ice";
            case LIGHTNING -> "lightning";
            case SOUL -> "shadow";
            case HEALING -> "holy";
            case ARCANE -> "arcane";
            default -> "magic";
        };
    }

    /**
     * Creates a school-specific damage source for spell damage.
     * Uses custom damage types registered in data/megamod/damage_type/spell_{school}.json
     * Falls back to indirectMagic if custom type not found.
     */
    private static DamageSource getSpellDamageSource(ServerPlayer caster, SpellSchool school) {
        if (school == null) return caster.damageSources().indirectMagic(caster, caster);
        try {
            String typeKey = "spell_" + school.name().toLowerCase();
            var registryAccess = caster.level().registryAccess();
            var damageTypes = registryAccess.lookupOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE);
            var key = net.minecraft.resources.ResourceKey.create(
                    net.minecraft.core.registries.Registries.DAMAGE_TYPE,
                    net.minecraft.resources.Identifier.fromNamespaceAndPath("megamod", typeKey));
            var holder = damageTypes.get(key);
            if (holder.isPresent()) {
                return new DamageSource(holder.get(), caster, caster);
            }
        } catch (Exception ignored) {}
        return caster.damageSources().indirectMagic(caster, caster);
    }

    /**
     * Checks if the player has a specific enchantment on their mainhand or offhand item.
     */
    private static boolean hasEnchantment(ServerPlayer player, String enchantmentId) {
        try {
            var id = net.minecraft.resources.Identifier.parse(enchantmentId);
            var registry = player.level().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
            var enchRef = registry.get(net.minecraft.resources.ResourceKey.create(
                    net.minecraft.core.registries.Registries.ENCHANTMENT, id));
            if (enchRef.isEmpty()) return false;

            var mainEnch = player.getMainHandItem().getEnchantments();
            var offEnch = player.getOffhandItem().getEnchantments();
            return mainEnch.getLevel(enchRef.get()) > 0 || offEnch.getLevel(enchRef.get()) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    // ─── Target resolution ───

    private static LivingEntity getAimedTarget(ServerPlayer player, float range) {
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getLookAngle().scale(range));
        AABB searchBox = player.getBoundingBox().expandTowards(player.getLookAngle().scale(range)).inflate(2);

        LivingEntity closest = null;
        double closestDist = range * range;

        for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, searchBox,
                e -> e != player && e.isAlive() && e.isPickable())) {
            AABB entityBox = entity.getBoundingBox().inflate(0.3);
            var hit = entityBox.clip(start, end);
            if (hit.isPresent()) {
                double dist = start.distanceToSqr(hit.get());
                if (dist < closestDist) {
                    closestDist = dist;
                    closest = entity;
                }
            }
        }
        return closest;
    }

    // ─── Cooldown management ───

    public static boolean isOnCooldown(UUID playerId, String spellId) {
        Map<String, Long> playerCooldowns = cooldowns.get(playerId);
        if (playerCooldowns == null) return false;
        Long expiry = playerCooldowns.get(spellId);
        return expiry != null && System.currentTimeMillis() < expiry;
    }

    public static void setCooldown(UUID playerId, String spellId, int currentTick, int durationTicks) {
        cooldowns.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>())
            .put(spellId, System.currentTimeMillis() + (durationTicks * 50L));
    }

    public static float getCooldownRemaining(UUID playerId, String spellId) {
        Map<String, Long> playerCooldowns = cooldowns.get(playerId);
        if (playerCooldowns == null) return 0;
        Long expiry = playerCooldowns.get(spellId);
        if (expiry == null) return 0;
        long remaining = expiry - System.currentTimeMillis();
        return remaining > 0 ? remaining / 1000.0f : 0;
    }

    public static void clearCooldowns(UUID playerId) {
        cooldowns.remove(playerId);
    }

    /**
     * Clear all cooldown data for all players. Called on server shutdown.
     */
    public static void clearAllCooldowns() {
        cooldowns.clear();
    }
}
