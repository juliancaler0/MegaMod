package com.ultra.megamod.feature.relics.weapons;

import com.ultra.megamod.feature.combat.spell.SpellAbilityBridge;
import com.ultra.megamod.feature.combat.spell.SpellDefinition;
import com.ultra.megamod.feature.combat.spell.SpellExecutor;
import com.ultra.megamod.feature.combat.spell.SpellRegistry;
import com.ultra.megamod.feature.relics.weapons.RpgWeaponItem;
import com.ultra.megamod.feature.relics.weapons.RpgWeaponRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.PowerParticleOption;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid="megamod")
public class RpgWeaponEvents {
    private static final Map<String, Long> COOLDOWNS = new HashMap<String, Long>();
    private static final Map<String, Long> ACTIVE_BUFFS = new HashMap<String, Long>();

    // Right-click skill casting REMOVED -- all RPG weapon skills now go through R keybind
    // via RpgWeaponSkillHandler -> executeSkillPublic()

    /**
     * Public entry point for the unified ability system. Handles cooldown checking,
     * skill execution, and cooldown application for any weapon skill.
     */
    public static void executeSkillPublic(ServerPlayer player, String weaponName, RpgWeaponItem.WeaponSkill skill) {
        executeSkillPublic(player, weaponName, skill, player.getMainHandItem());
    }

    public static void executeSkillPublic(ServerPlayer player, String weaponName, RpgWeaponItem.WeaponSkill skill, ItemStack weaponStack) {
        // Spell bridge: if this weapon is mapped to a spell, delegate to SpellExecutor
        String spellId = SpellAbilityBridge.getSpellForWeapon(weaponName);
        if (spellId != null) {
            SpellDefinition spell = SpellRegistry.get(spellId);
            if (spell != null) {
                boolean cast = SpellExecutor.cast(player, spell);
                if (cast) {
                    player.displayClientMessage(Component.literal((skill.name() + "!")).withStyle(ChatFormatting.GOLD), true);
                }
                return;
            }
        }

        String cooldownKey = String.valueOf(player.getUUID()) + ":" + weaponName + ":" + skill.name();
        ServerLevel level = (ServerLevel) player.level();
        long now = level.getGameTime();
        Long expiry = COOLDOWNS.get(cooldownKey);
        if (expiry != null && now < expiry) {
            int remaining = (int)((expiry - now) / 20L);
            player.displayClientMessage(Component.literal((skill.name() + " on cooldown (" + remaining + "s)")).withStyle(ChatFormatting.RED), true);
            return;
        }
        boolean success = RpgWeaponEvents.executeSkill(player, weaponName, skill, level);
        if (success) {
            int effectiveCooldown = getEffectiveWeaponCooldown(weaponStack, skill);
            COOLDOWNS.put(cooldownKey, now + (long)effectiveCooldown);
            player.displayClientMessage(Component.literal((skill.name() + "!")).withStyle(ChatFormatting.GOLD), true);
            // HUD popup so the player sees what ability just fired
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(
                    player,
                    new com.ultra.megamod.feature.hud.network.AbilityTriggerPayload(skill.name(), /*MANUAL=*/0));
        }
    }

    public static int getEffectiveWeaponCooldown(ItemStack stack, RpgWeaponItem.WeaponSkill skill) {
        CompoundTag tag = ((CustomData) stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        CompoundTag overrides = tag.getCompoundOrEmpty("weapon_skill_cooldowns");
        int override = overrides.getIntOr(skill.name(), -1);
        return override >= 0 ? override : skill.cooldownTicks();
    }

    /**
     * Get effective skills for an item. Checks CustomData for overrides first,
     * falls back to the weapon's default skills.
     */
    public static List<RpgWeaponItem.WeaponSkill> getEffectiveSkills(ItemStack stack) {
        // Check for custom skill overrides in CustomData
        CompoundTag tag = ((CustomData) stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        CompoundTag overrides = tag.getCompoundOrEmpty("weapon_skill_overrides");
        if (!overrides.keySet().isEmpty()) {
            java.util.ArrayList<RpgWeaponItem.WeaponSkill> result = new java.util.ArrayList<>();
            // Read skill_0, skill_1, etc.
            for (int i = 0; i < 10; i++) {
                String key = "skill_" + i;
                String skillName = overrides.getStringOr(key, "");
                if (skillName.isEmpty()) break;
                RpgWeaponItem.WeaponSkill skill = RpgWeaponRegistry.getSkillByName(skillName);
                if (skill != null) {
                    result.add(skill);
                }
            }
            if (!result.isEmpty()) return result;
        }
        // Fall back to default
        if (stack.getItem() instanceof RpgWeaponItem rpg) {
            return rpg.getSkills();
        }
        return List.of();
    }

    /**
     * Set custom skill overrides on an item. Pass null/empty list to clear overrides.
     */
    public static void setSkillOverrides(ItemStack stack, List<String> skillNames) {
        CompoundTag tag = ((CustomData) stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        if (skillNames == null || skillNames.isEmpty()) {
            tag.remove("weapon_skill_overrides");
        } else {
            CompoundTag overrides = new CompoundTag();
            for (int i = 0; i < skillNames.size(); i++) {
                overrides.putString("skill_" + i, skillNames.get(i));
            }
            tag.put("weapon_skill_overrides", (net.minecraft.nbt.Tag) overrides);
        }
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static void setWeaponCooldownOverride(ItemStack stack, String skillName, int ticks) {
        CompoundTag tag = ((CustomData) stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
        CompoundTag overrides = tag.getCompoundOrEmpty("weapon_skill_cooldowns");
        if (ticks < 0) {
            overrides.remove(skillName);
        } else {
            overrides.putInt(skillName, ticks);
        }
        tag.put("weapon_skill_cooldowns", overrides);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    /**
     * Returns ticks remaining on a weapon skill cooldown, or 0 if not on cooldown.
     */
    public static int getCooldownRemaining(ServerPlayer player, String weaponName, String skillName) {
        String cooldownKey = player.getUUID() + ":" + weaponName + ":" + skillName;
        ServerLevel level = (ServerLevel) player.level();
        long now = level.getGameTime();
        Long expiry = COOLDOWNS.get(cooldownKey);
        if (expiry != null && now < expiry) {
            return (int)(expiry - now);
        }
        return 0;
    }

    private static boolean executeSkill(ServerPlayer player, String weaponName, RpgWeaponItem.WeaponSkill skill, ServerLevel level) {
        return switch (weaponName) {
            case "megamod:vampiric_tome" -> RpgWeaponEvents.executeVampiricTome(player, skill, level);
            case "megamod:static_seeker" -> RpgWeaponEvents.executeStaticSeeker(player, skill, level);
            case "megamod:battledancer" -> RpgWeaponEvents.executeBattledancer(player, skill, level);
            case "megamod:ebonchill" -> RpgWeaponEvents.executeEbonchill(player, skill, level);
            case "megamod:lightbinder" -> RpgWeaponEvents.executeLightbinder(player, skill, level);
            case "megamod:crescent_blade" -> RpgWeaponEvents.executeCrescentBlade(player, skill, level);
            case "megamod:ghost_fang" -> RpgWeaponEvents.executeGhostFang(player, skill, level);
            case "megamod:terra_warhammer" -> RpgWeaponEvents.executeTerraWarhammer(player, skill, level);
            case "megamod:voidreaver" -> RpgWeaponEvents.executeVoidreaver(player, skill, level);
            case "megamod:solaris" -> RpgWeaponEvents.executeSolaris(player, skill, level);
            case "megamod:stormfury" -> RpgWeaponEvents.executeStormfury(player, skill, level);
            case "megamod:briarthorn" -> RpgWeaponEvents.executeBriarthorn(player, skill, level);
            case "megamod:abyssal_trident" -> RpgWeaponEvents.executeAbyssalTrident(player, skill, level);
            case "megamod:pyroclast" -> RpgWeaponEvents.executePyroclast(player, skill, level);
            case "megamod:whisperwind" -> RpgWeaponEvents.executeWhisperwind(player, skill, level);
            case "megamod:soulchain" -> RpgWeaponEvents.executeSoulchain(player, skill, level);
            case "megamod:soka_singing_blade" -> RpgWeaponEvents.executeSokaSingingBlade(player, skill, level);
            default -> false;
        };
    }

    public static void clearCooldownsForPlayer(ServerPlayer player) {
        String prefix = player.getUUID().toString() + ":";
        COOLDOWNS.entrySet().removeIf(entry -> entry.getKey().startsWith(prefix));
    }

    public static void setActiveBuff(ServerPlayer player, String buffType, long expiryTick) {
        ACTIVE_BUFFS.put(String.valueOf(player.getUUID()) + ":" + buffType, expiryTick);
    }

    private static boolean executeVampiricTome(ServerPlayer player, RpgWeaponItem.WeaponSkill skill, ServerLevel level) {
        if ("Drain Life".equals(skill.name())) {
            LivingEntity target = RpgWeaponEvents.findNearestMob(player, level, 8.0);
            if (target == null) {
                return false;
            }
            float damage = 6.0f + getWeaponDamage(player);
            target.hurt(level.damageSources().magic(), damage);
            player.heal(damage * 0.5f);
            // Blood drain beam: dark particles streaming from target to player
            Vec3 targetPos = new Vec3(target.getX(), target.getY() + 1.0, target.getZ());
            Vec3 playerPos = new Vec3(player.getX(), player.getY() + 1.0, player.getZ());
            WeaponEffects.line(level, ParticleTypes.DAMAGE_INDICATOR, targetPos, playerPos, 12, 1, 0.05);
            WeaponEffects.line(level, ParticleTypes.SOUL_FIRE_FLAME, targetPos, playerPos, 8, 1, 0.08);
            // Dark burst on target
            level.sendParticles((ParticleOptions)ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getY() + 1.0, target.getZ(), 8, 0.3, 0.5, 0.3, 0.02);
            level.sendParticles((ParticleOptions)ParticleTypes.CRIMSON_SPORE, target.getX(), target.getY() + 0.5, target.getZ(), 15, 0.4, 0.8, 0.4, 0.02);
            // Heal particles on player
            level.sendParticles((ParticleOptions)ParticleTypes.HEART, player.getX(), player.getY() + 1.5, player.getZ(), 3, 0.3, 0.3, 0.3, 0.0);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_HURT, SoundSource.PLAYERS, 0.5f, 1.5f);
            return true;
        }
        if ("Blood Pact".equals(skill.name())) {
            if (player.getHealth() <= 4.0f) {
                player.displayClientMessage(Component.literal("Not enough HP for Blood Pact!").withStyle(ChatFormatting.RED), true);
                return false;
            }
            player.hurt(level.damageSources().magic(), 4.0f);
            RpgWeaponEvents.setActiveBuff(player, "blood_pact_damage", level.getGameTime() + 100L);
            player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 100, 1, false, true, true));
            // Dark blood spiral rising around player
            WeaponEffects.spiral(level, ParticleTypes.CRIMSON_SPORE, player.getX(), player.getY(), player.getZ(), 1.2, 2.5, 24, 2);
            WeaponEffects.spiral(level, ParticleTypes.DAMAGE_INDICATOR, player.getX(), player.getY(), player.getZ(), 0.8, 2.0, 16, 1);
            // Dark aura burst
            level.sendParticles((ParticleOptions)ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 1.0, player.getZ(), 20, 0.6, 0.8, 0.6, 0.03);
            level.sendParticles((ParticleOptions)ParticleTypes.SMOKE, player.getX(), player.getY() + 0.5, player.getZ(), 15, 0.5, 0.3, 0.5, 0.02);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.3f, 2.0f);
            return true;
        }
        return false;
    }

    private static boolean executeStaticSeeker(ServerPlayer player, RpgWeaponItem.WeaponSkill skill, ServerLevel level) {
        if ("Chain Lightning".equals(skill.name())) {
            float damage = 6.0f + getWeaponDamage(player);
            AABB area = new AABB(player.blockPosition()).inflate(12.0);
            List<LivingEntity> all = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive());
            if (all.isEmpty()) {
                return false;
            }
            all.sort(Comparator.comparingDouble(e -> e.distanceToSqr((Entity)player)));
            HashSet<Integer> hitIds = new HashSet<Integer>();
            LivingEntity current = all.getFirst();
            Vec3 previousPos = new Vec3(player.getX(), player.getY() + 1.0, player.getZ());
            // Initial bolt from player hand
            level.sendParticles((ParticleOptions)ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.2, player.getZ(), 10, 0.15, 0.15, 0.15, 0.05);
            for (int i = 0; i < 3 && current != null; ++i) {
                current.hurt(level.damageSources().lightningBolt(), damage);
                hitIds.add(current.getId());
                Vec3 targetPos = new Vec3(current.getX(), current.getY() + 1.0, current.getZ());
                // Lightning bolt line between chain points
                WeaponEffects.line(level, ParticleTypes.ELECTRIC_SPARK, previousPos, targetPos, 16, 2, 0.1);
                // Impact burst at target
                level.sendParticles((ParticleOptions)ParticleTypes.ELECTRIC_SPARK, current.getX(), current.getY() + 1.0, current.getZ(), 20, 0.4, 0.6, 0.4, 0.15);
                // Flash at impact
                level.sendParticles((ParticleOptions)ParticleTypes.FLASH, current.getX(), current.getY() + 1.0, current.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
                // Per-target zap sound
                level.playSound(null, current.getX(), current.getY(), current.getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.4f, 1.8f + i * 0.2f);
                previousPos = targetPos;
                // Chain to next target — search wider (12 blocks from last hit, not 6)
                LivingEntity chainFrom = current;
                AABB chainArea = new AABB(chainFrom.blockPosition()).inflate(12.0);
                List<LivingEntity> next = level.getEntitiesOfClass(LivingEntity.class, chainArea, e -> e != player && e.isAlive() && !hitIds.contains(e.getId()));
                if (next.isEmpty()) break;
                next.sort(Comparator.comparingDouble(e -> e.distanceToSqr((Entity)chainFrom)));
                current = next.getFirst();
                damage *= 0.85f;
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.5f, 2.0f);
            return true;
        }
        if ("Overcharge".equals(skill.name())) {
            RpgWeaponEvents.setActiveBuff(player, "overcharge_next_hit", level.getGameTime() + 200L);
            // Electric spiral charging around player
            WeaponEffects.spiral(level, ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY(), player.getZ(), 1.0, 2.5, 28, 2);
            // Electric ring at feet
            WeaponEffects.ring(level, ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 0.1, player.getZ(), 1.5, 16, 2, 0.03);
            // Bright flash
            level.sendParticles((ParticleOptions)ParticleTypes.FLASH, player.getX(), player.getY() + 1.0, player.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            level.sendParticles((ParticleOptions)ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0, player.getZ(), 30, 0.5, 1.0, 0.5, 0.15);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.3f, 1.5f);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 0.5f, 2.0f);
            return true;
        }
        return false;
    }

    private static boolean executeBattledancer(ServerPlayer player, RpgWeaponItem.WeaponSkill skill, ServerLevel level) {
        if ("Whirlwind".equals(skill.name())) {
            float damage = 5.0f + getWeaponDamage(player);
            AABB area = new AABB(player.blockPosition()).inflate(4.0);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive() && (double)e.distanceTo((Entity)player) <= 4.0);
            for (LivingEntity target : targets) {
                target.hurt(level.damageSources().playerAttack((Player)player), damage);
            }
            // Spinning blade sweep rings at multiple heights
            WeaponEffects.ring(level, ParticleTypes.SWEEP_ATTACK, player.getX(), player.getY() + 0.5, player.getZ(), 2.5, 12, 1, 0.05);
            WeaponEffects.ring(level, ParticleTypes.SWEEP_ATTACK, player.getX(), player.getY() + 1.0, player.getZ(), 3.0, 16, 1, 0.05);
            WeaponEffects.ring(level, ParticleTypes.SWEEP_ATTACK, player.getX(), player.getY() + 1.5, player.getZ(), 2.5, 12, 1, 0.05);
            // Wind gust particles
            WeaponEffects.ring(level, ParticleTypes.CLOUD, player.getX(), player.getY() + 0.3, player.getZ(), 3.5, 12, 1, 0.1);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 0.8f);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.7f, 1.2f);
            return true;
        }
        if ("Riposte".equals(skill.name())) {
            RpgWeaponEvents.setActiveBuff(player, "riposte_counter", level.getGameTime() + 60L);
            // Golden parry shield effect
            WeaponEffects.ring(level, ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0, player.getZ(), 0.8, 12, 1, 0.02);
            // Shield flash in front of player
            Vec3 look = player.getLookAngle().normalize();
            double fx = player.getX() + look.x * 0.8;
            double fz = player.getZ() + look.z * 0.8;
            level.sendParticles((ParticleOptions)ParticleTypes.END_ROD, fx, player.getY() + 1.0, fz, 12, 0.3, 0.5, 0.3, 0.01);
            level.sendParticles((ParticleOptions)ParticleTypes.ENCHANT, fx, player.getY() + 0.5, fz, 10, 0.2, 0.8, 0.2, 0.5);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0f, 1.2f);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.4f, 1.8f);
            return true;
        }
        return false;
    }

    private static boolean executeEbonchill(ServerPlayer player, RpgWeaponItem.WeaponSkill skill, ServerLevel level) {
        if ("Frost Nova".equals(skill.name())) {
            float damage = 5.0f + getWeaponDamage(player);
            AABB area = new AABB(player.blockPosition()).inflate(5.0);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive() && (double)e.distanceTo((Entity)player) <= 5.0);
            for (LivingEntity target : targets) {
                target.hurt(level.damageSources().freeze(), damage);
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 3, false, true, true));
                target.setTicksFrozen(target.getTicksFrozen() + 140);
                // Ice shatter on each target
                level.sendParticles((ParticleOptions)ParticleTypes.SNOWFLAKE, target.getX(), target.getY() + 1.0, target.getZ(), 10, 0.3, 0.5, 0.3, 0.05);
                level.sendParticles((ParticleOptions)ParticleTypes.ITEM_SNOWBALL, target.getX(), target.getY() + 0.5, target.getZ(), 8, 0.3, 0.3, 0.3, 0.1);
            }
            // Expanding frost shockwave rings
            WeaponEffects.shockwave(level, ParticleTypes.SNOWFLAKE, player.getX(), player.getY() + 0.3, player.getZ(), 5.0, 3, 20, 2);
            // Ice crystal burst at center
            level.sendParticles((ParticleOptions)ParticleTypes.SNOWFLAKE, player.getX(), player.getY() + 1.5, player.getZ(), 25, 0.3, 1.0, 0.3, 0.1);
            level.sendParticles((ParticleOptions)ParticleTypes.ITEM_SNOWBALL, player.getX(), player.getY() + 0.5, player.getZ(), 20, 2.0, 0.3, 2.0, 0.08);
            // White cloud ring
            WeaponEffects.ring(level, ParticleTypes.CLOUD, player.getX(), player.getY() + 0.2, player.getZ(), 4.0, 16, 1, 0.1);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0f, 1.5f);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.POWDER_SNOW_STEP, SoundSource.PLAYERS, 1.0f, 0.5f);
            return true;
        }
        if ("Icicle Lance".equals(skill.name())) {
            float damage = 8.0f + getWeaponDamage(player);
            Vec3 look = player.getLookAngle().normalize();
            Vec3 start = player.getEyePosition();
            // Muzzle frost burst
            Vec3 muzzle = start.add(look.scale(1.0));
            level.sendParticles((ParticleOptions)ParticleTypes.SNOWFLAKE, muzzle.x, muzzle.y, muzzle.z, 12, 0.2, 0.2, 0.2, 0.08);
            level.sendParticles((ParticleOptions)ParticleTypes.ITEM_SNOWBALL, muzzle.x, muzzle.y, muzzle.z, 8, 0.15, 0.15, 0.15, 0.05);
            for (int i = 1; i <= 12; ++i) {
                Vec3 point = start.add(look.scale((double)i));
                AABB check = new AABB(point.x - 0.5, point.y - 0.5, point.z - 0.5, point.x + 0.5, point.y + 0.5, point.z + 0.5);
                List<LivingEntity> hit = level.getEntitiesOfClass(LivingEntity.class, check, e -> e != player && e.isAlive());
                for (LivingEntity target : hit) {
                    target.hurt(level.damageSources().freeze(), damage);
                    target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 2, false, true, true));
                    // Impact shatter
                    level.sendParticles((ParticleOptions)ParticleTypes.ITEM_SNOWBALL, target.getX(), target.getY() + 1.0, target.getZ(), 12, 0.3, 0.5, 0.3, 0.15);
                }
                // Dense ice shard trail - core line
                level.sendParticles((ParticleOptions)ParticleTypes.SNOWFLAKE, point.x, point.y, point.z, 5, 0.08, 0.08, 0.08, 0.01);
                // Trailing frost mist
                level.sendParticles((ParticleOptions)ParticleTypes.ITEM_SNOWBALL, point.x, point.y, point.z, 2, 0.15, 0.15, 0.15, 0.02);
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0f, 1.5f);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.4f, 2.0f);
            return true;
        }
        return false;
    }

    private static boolean executeLightbinder(ServerPlayer player, RpgWeaponItem.WeaponSkill skill, ServerLevel level) {
        if ("Holy Smite".equals(skill.name())) {
            LivingEntity target = RpgWeaponEvents.findNearestMob(player, level, 8.0);
            if (target == null) {
                return false;
            }
            float damage = 8.0f + getWeaponDamage(player);
            target.hurt(level.damageSources().magic(), damage);
            player.heal(4.0f);
            // Holy beam from sky: column of light descending onto target
            WeaponEffects.column(level, ParticleTypes.END_ROD, target.getX(), target.getY(), target.getZ(), 8.0, 16, 2, 0.1);
            // Bright impact ring at target
            WeaponEffects.ring(level, ParticleTypes.END_ROD, target.getX(), target.getY() + 0.3, target.getZ(), 1.2, 12, 2, 0.03);
            // Holy burst at impact point
            level.sendParticles((ParticleOptions)ParticleTypes.END_ROD, target.getX(), target.getY() + 1.0, target.getZ(), 20, 0.5, 0.8, 0.5, 0.08);
            level.sendParticles((ParticleOptions)ParticleTypes.FLASH, target.getX(), target.getY() + 1.0, target.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            // Heal shimmer on player
            level.sendParticles((ParticleOptions)ParticleTypes.HEART, player.getX(), player.getY() + 1.5, player.getZ(), 3, 0.3, 0.3, 0.3, 0.0);
            level.sendParticles((ParticleOptions)ParticleTypes.ENCHANT, player.getX(), player.getY() + 0.5, player.getZ(), 8, 0.3, 0.5, 0.3, 0.5);
            level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.7f, 2.0f);
            level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.4f, 1.5f);
            return true;
        }
        if ("Sacred Shield".equals(skill.name())) {
            RpgWeaponEvents.setActiveBuff(player, "sacred_shield", level.getGameTime() + 160L);
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 160, 2, false, true, true));
            // Golden shield dome effect
            WeaponEffects.ring(level, ParticleTypes.END_ROD, player.getX(), player.getY() + 0.2, player.getZ(), 1.5, 16, 2, 0.02);
            WeaponEffects.ring(level, ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0, player.getZ(), 1.2, 12, 2, 0.02);
            WeaponEffects.ring(level, ParticleTypes.END_ROD, player.getX(), player.getY() + 1.8, player.getZ(), 0.6, 8, 2, 0.02);
            // Enchant shimmer
            level.sendParticles((ParticleOptions)ParticleTypes.ENCHANT, player.getX(), player.getY() + 0.5, player.getZ(), 20, 0.8, 1.0, 0.8, 0.5);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0f, 1.5f);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.5f, 1.8f);
            return true;
        }
        return false;
    }

    private static boolean executeCrescentBlade(ServerPlayer player, RpgWeaponItem.WeaponSkill skill, ServerLevel level) {
        if ("Crescent Slash".equals(skill.name())) {
            float damage = 7.0f + getWeaponDamage(player);
            Vec3 look = player.getLookAngle().normalize();
            AABB area = new AABB(player.blockPosition()).inflate(4.0);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive() && (double)e.distanceTo((Entity)player) <= 4.0);
            for (LivingEntity target : targets) {
                Vec3 toTarget = target.position().subtract(player.position()).normalize();
                double dot = look.x * toTarget.x + look.z * toTarget.z;
                if (!(dot > 0.0)) continue;
                target.hurt(level.damageSources().playerAttack((Player)player), damage);
            }
            // Crescent arc sweep in front of player
            WeaponEffects.arc(level, ParticleTypes.SWEEP_ATTACK, player.getX(), player.getY() + 1.0, player.getZ(),
                look.x, look.z, 3.0, Math.PI * 0.75, 16, 1);
            WeaponEffects.arc(level, ParticleTypes.ENCHANTED_HIT, player.getX(), player.getY() + 1.0, player.getZ(),
                look.x, look.z, 2.5, Math.PI * 0.6, 12, 2);
            // Inner crescent glow
            WeaponEffects.arc(level, ParticleTypes.CRIT, player.getX(), player.getY() + 1.0, player.getZ(),
                look.x, look.z, 2.0, Math.PI * 0.5, 8, 1);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.2f, 1.0f);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 0.5f, 0.7f);
            return true;
        }
        if ("Shadow Dash".equals(skill.name())) {
            Vec3 look = player.getLookAngle().normalize();
            Vec3 start = player.position();
            float damage = 6.0f + getWeaponDamage(player);
            HashSet<Integer> hitIds = new HashSet<Integer>();
            // Dark burst at origin
            level.sendParticles((ParticleOptions)ParticleTypes.LARGE_SMOKE, start.x, start.y + 1.0, start.z, 15, 0.4, 0.8, 0.4, 0.03);
            level.sendParticles((ParticleOptions)ParticleTypes.SMOKE, start.x, start.y + 0.5, start.z, 10, 0.5, 0.5, 0.5, 0.02);
            for (int step = 1; step <= 8; ++step) {
                Vec3 point = start.add(look.scale((double)step));
                AABB check = new AABB(point.x - 0.8, point.y, point.z - 0.8, point.x + 0.8, point.y + 2.0, point.z + 0.8);
                List<LivingEntity> hit = level.getEntitiesOfClass(LivingEntity.class, check, e -> e != player && e.isAlive() && !hitIds.contains(e.getId()));
                for (LivingEntity target : hit) {
                    target.hurt(level.damageSources().playerAttack((Player)player), damage);
                    hitIds.add(target.getId());
                    // Slash hit effect on each enemy
                    level.sendParticles((ParticleOptions)ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1.0, target.getZ(), 3, 0.2, 0.2, 0.2, 0.0);
                }
                // Dense shadow trail
                level.sendParticles((ParticleOptions)ParticleTypes.SMOKE, point.x, point.y + 1.0, point.z, 5, 0.15, 0.4, 0.15, 0.01);
                level.sendParticles((ParticleOptions)ParticleTypes.LARGE_SMOKE, point.x, point.y + 0.5, point.z, 2, 0.2, 0.3, 0.2, 0.01);
                level.sendParticles((ParticleOptions)ParticleTypes.SOUL, point.x, point.y + 1.0, point.z, 2, 0.1, 0.3, 0.1, 0.02);
            }
            Vec3 destination = start.add(look.scale(8.0));
            player.teleportTo(destination.x, destination.y, destination.z);
            player.fallDistance = 0.0f;
            // Arrival burst
            level.sendParticles((ParticleOptions)ParticleTypes.LARGE_SMOKE, destination.x, destination.y + 1.0, destination.z, 10, 0.3, 0.6, 0.3, 0.02);
            level.sendParticles((ParticleOptions)ParticleTypes.REVERSE_PORTAL, destination.x, destination.y + 1.0, destination.z, 15, 0.3, 0.5, 0.3, 0.08);
            level.playSound(null, start.x, start.y, start.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5f, 1.8f);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.7f, 1.5f);
            return true;
        }
        return false;
    }

    private static boolean executeGhostFang(ServerPlayer player, RpgWeaponItem.WeaponSkill skill, ServerLevel level) {
        if ("Spectral Bite".equals(skill.name())) {
            LivingEntity target = RpgWeaponEvents.findNearestMob(player, level, 6.0);
            if (target == null) {
                return false;
            }
            float damage = 10.0f + getWeaponDamage(player);
            target.hurt(level.damageSources().magic(), damage);
            // Spectral fangs converging on target from all sides
            WeaponEffects.converge(level, ParticleTypes.SOUL, target.getX(), target.getY() + 1.0, target.getZ(), 2.0, 10, 2);
            // Ghost jaw snap particles
            level.sendParticles((ParticleOptions)ParticleTypes.SOUL, target.getX(), target.getY() + 1.0, target.getZ(), 15, 0.2, 0.4, 0.2, 0.08);
            level.sendParticles((ParticleOptions)ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + 0.8, target.getZ(), 8, 0.3, 0.3, 0.3, 0.05);
            // Spectral line from player to target
            Vec3 from = new Vec3(player.getX(), player.getY() + 1.0, player.getZ());
            Vec3 to = new Vec3(target.getX(), target.getY() + 1.0, target.getZ());
            WeaponEffects.line(level, ParticleTypes.SOUL, from, to, 10, 1, 0.05);
            // Damage flash
            level.sendParticles((ParticleOptions)ParticleTypes.ENCHANTED_HIT, target.getX(), target.getY() + 1.0, target.getZ(), 6, 0.3, 0.3, 0.3, 0.3);
            level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.PHANTOM_BITE, SoundSource.PLAYERS, 1.0f, 0.8f);
            level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 0.6f, 1.5f);
            return true;
        }
        if ("Phase".equals(skill.name())) {
            player.setInvulnerable(true);
            RpgWeaponEvents.setActiveBuff(player, "phase_intangible", level.getGameTime() + 40L);
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 0, false, false, true));
            // Ghostly dissolution — player fading out
            WeaponEffects.spiral(level, ParticleTypes.SOUL, player.getX(), player.getY(), player.getZ(), 1.0, 2.5, 20, 2);
            level.sendParticles((ParticleOptions)ParticleTypes.SOUL, player.getX(), player.getY() + 1.0, player.getZ(), 25, 0.5, 1.0, 0.5, 0.08);
            // Smoke dissipation
            level.sendParticles((ParticleOptions)ParticleTypes.LARGE_SMOKE, player.getX(), player.getY() + 0.5, player.getZ(), 12, 0.6, 0.8, 0.6, 0.02);
            // Reverse portal shimmer (ethereal look)
            level.sendParticles((ParticleOptions)ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 15, 0.4, 0.6, 0.4, 0.05);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PHANTOM_AMBIENT, SoundSource.PLAYERS, 0.7f, 1.2f);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.3f, 2.0f);
            return true;
        }
        return false;
    }

    private static boolean executeTerraWarhammer(ServerPlayer player, RpgWeaponItem.WeaponSkill skill, ServerLevel level) {
        if ("Earthquake".equals(skill.name())) {
            float damage = 6.0f + getWeaponDamage(player);
            AABB area = new AABB(player.blockPosition()).inflate(5.0);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive() && (double)e.distanceTo((Entity)player) <= 5.0);
            for (LivingEntity target : targets) {
                double dz;
                target.hurt(level.damageSources().playerAttack((Player)player), damage);
                double dx = target.getX() - player.getX();
                double dist = Math.sqrt(dx * dx + (dz = target.getZ() - player.getZ()) * dz);
                if (!(dist > 0.0)) continue;
                target.knockback(1.5, -dx / dist, -dz / dist);
            }
            // Ground slam shockwave — expanding rings
            WeaponEffects.shockwave(level, ParticleTypes.CAMPFIRE_COSY_SMOKE, player.getX(), player.getY() + 0.1, player.getZ(), 5.0, 4, 20, 1);
            // Dirt/rock debris flying up
            level.sendParticles((ParticleOptions)ParticleTypes.COMPOSTER, player.getX(), player.getY() + 0.2, player.getZ(), 30, 3.0, 0.1, 3.0, 0.15);
            // Central slam explosion
            level.sendParticles((ParticleOptions)ParticleTypes.EXPLOSION, player.getX(), player.getY() + 0.2, player.getZ(), 3, 0.5, 0.0, 0.5, 0.0);
            level.sendParticles((ParticleOptions)ParticleTypes.EXPLOSION_EMITTER, player.getX(), player.getY(), player.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            // Dust cloud
            WeaponEffects.ring(level, ParticleTypes.CAMPFIRE_COSY_SMOKE, player.getX(), player.getY() + 0.3, player.getZ(), 3.0, 16, 2, 0.15);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.7f, 0.6f);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.6f, 0.4f);
            return true;
        }
        if ("Fortify".equals(skill.name())) {
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 200, 1, false, true, true));
            RpgWeaponEvents.setActiveBuff(player, "fortify_armor", level.getGameTime() + 200L);
            // Earth armor rising around player
            WeaponEffects.spiral(level, ParticleTypes.COMPOSTER, player.getX(), player.getY(), player.getZ(), 1.2, 2.5, 24, 2);
            // Rock shield layers
            WeaponEffects.ring(level, ParticleTypes.COMPOSTER, player.getX(), player.getY() + 0.3, player.getZ(), 1.0, 12, 3, 0.05);
            WeaponEffects.ring(level, ParticleTypes.COMPOSTER, player.getX(), player.getY() + 1.0, player.getZ(), 0.8, 10, 3, 0.05);
            WeaponEffects.ring(level, ParticleTypes.COMPOSTER, player.getX(), player.getY() + 1.6, player.getZ(), 0.6, 8, 2, 0.05);
            // Dust cloud
            level.sendParticles((ParticleOptions)ParticleTypes.CAMPFIRE_COSY_SMOKE, player.getX(), player.getY() + 0.2, player.getZ(), 10, 0.6, 0.3, 0.6, 0.01);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 0.5f, 0.8f);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.STONE_BREAK, SoundSource.PLAYERS, 0.7f, 0.6f);
            return true;
        }
        return false;
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        Entity entity = event.getSource().getEntity();
        if (entity instanceof ServerPlayer) {
            ServerPlayer attacker = (ServerPlayer)entity;
            ServerLevel attackerLevel = (ServerLevel) attacker.level();
            long now = attackerLevel.getGameTime();
            String overchargeKey = String.valueOf(attacker.getUUID()) + ":overcharge_next_hit";
            Long overchargeExpiry = ACTIVE_BUFFS.get(overchargeKey);
            if (overchargeExpiry != null && now < overchargeExpiry) {
                event.setNewDamage(event.getNewDamage() * 3.0f);
                ACTIVE_BUFFS.remove(overchargeKey);
                attacker.displayClientMessage(Component.literal("OVERCHARGED!").withStyle(ChatFormatting.AQUA), true);
            }
        }
        // Archon's Judgment: amplify damage to marked targets by 50%
        LivingEntity target = event.getEntity();
        String archonMarkKey = target.getUUID() + ":archon_mark";
        if (ACTIVE_BUFFS.containsKey(archonMarkKey)) {
            event.setNewDamage(event.getNewDamage() * 1.5f);
        }
        if (target instanceof ServerPlayer) {
            ServerPlayer victim = (ServerPlayer)target;
            ServerLevel victimLevel = (ServerLevel) victim.level();
            long now = victimLevel.getGameTime();
            String riposteKey = String.valueOf(victim.getUUID()) + ":riposte_counter";
            Long riposteExpiry = ACTIVE_BUFFS.get(riposteKey);
            if (riposteExpiry != null && now < riposteExpiry) {
                Entity entity2 = event.getSource().getEntity();
                if (entity2 instanceof LivingEntity) {
                    LivingEntity attacker = (LivingEntity)entity2;
                    attacker.hurt(victimLevel.damageSources().thorns((Entity)victim), event.getNewDamage());
                    victim.displayClientMessage(Component.literal("Riposte!").withStyle(ChatFormatting.GOLD), true);
                }
                event.setNewDamage(0.0f);
                ACTIVE_BUFFS.remove(riposteKey);
            }
            // Arsenal riposte (Rapier parry counter)
            String arsenalRiposteKey = String.valueOf(victim.getUUID()) + ":riposte";
            Long arsenalRiposteExpiry = ACTIVE_BUFFS.get(arsenalRiposteKey);
            if (arsenalRiposteExpiry != null && now < arsenalRiposteExpiry) {
                Entity entity2 = event.getSource().getEntity();
                if (entity2 instanceof LivingEntity) {
                    LivingEntity attacker = (LivingEntity)entity2;
                    attacker.hurt(victimLevel.damageSources().thorns((Entity)victim), event.getNewDamage());
                    victim.displayClientMessage(Component.literal("Parry!").withStyle(ChatFormatting.GOLD), true);
                }
                event.setNewDamage(0.0f);
                ACTIVE_BUFFS.remove(arsenalRiposteKey);
            }
            // Spiked Block reflect (Shield1)
            String spikedKey = String.valueOf(victim.getUUID()) + ":spiked_reflect";
            Long spikedExpiry = ACTIVE_BUFFS.get(spikedKey);
            if (spikedExpiry != null && now < spikedExpiry) {
                Entity entity2 = event.getSource().getEntity();
                if (entity2 instanceof LivingEntity) {
                    LivingEntity attacker = (LivingEntity)entity2;
                    attacker.hurt(victimLevel.damageSources().thorns((Entity)victim), event.getNewDamage() * 0.5f);
                }
            }
            // Flame reflect (Shield3 Fear Aura)
            String flameKey = String.valueOf(victim.getUUID()) + ":flame_reflect";
            Long flameExpiry = ACTIVE_BUFFS.get(flameKey);
            if (flameExpiry != null && now < flameExpiry) {
                Entity entity2 = event.getSource().getEntity();
                if (entity2 instanceof LivingEntity) {
                    LivingEntity attacker = (LivingEntity)entity2;
                    attacker.setRemainingFireTicks(80);
                    attacker.hurt(victimLevel.damageSources().magic(), event.getNewDamage() * 0.3f);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        long now = event.getServer().overworld().getGameTime();
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            UUID playerId = player.getUUID();
            RpgWeaponEvents.checkAndExpireInvuln(player, String.valueOf(playerId) + ":phase_intangible", now);
        }
        COOLDOWNS.entrySet().removeIf(entry -> entry.getValue() <= now);
        // Archon's Judgment detonation: check for expiring marks before cleanup
        ServerLevel overworld = event.getServer().overworld();
        List<String> expiredArchons = new ArrayList<>();
        for (Map.Entry<String, Long> entry : ACTIVE_BUFFS.entrySet()) {
            if (entry.getKey().endsWith(":archon_mark") && entry.getValue() <= now) {
                String targetUuidStr = entry.getKey().replace(":archon_mark", "");
                try {
                    UUID targetUuid = UUID.fromString(targetUuidStr);
                    String dmgKey = targetUuid + ":archon_dmg";
                    float detonationDmg = ACTIVE_BUFFS.containsKey(dmgKey) ? ACTIVE_BUFFS.get(dmgKey).floatValue() : 120.0f;
                    // Find the entity across all levels
                    for (ServerLevel lvl : event.getServer().getAllLevels()) {
                        Entity targetEntity = lvl.getEntity(targetUuid);
                        if (targetEntity instanceof LivingEntity living && living.isAlive()) {
                            // Detonation: AOE damage in 8-block radius
                            AABB area = new AABB(living.blockPosition()).inflate(8.0);
                            List<LivingEntity> nearby = lvl.getEntitiesOfClass(LivingEntity.class, area,
                                e -> e.isAlive() && !(e instanceof ServerPlayer) && (double)e.distanceTo(living) <= 8.0);
                            for (LivingEntity hit : nearby) {
                                hit.hurt(lvl.damageSources().magic(), detonationDmg / Math.max(1, nearby.size()));
                                lvl.sendParticles(PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, 1.0f), hit.getX(), hit.getY() + 1.0, hit.getZ(), 8, 0.4, 0.4, 0.4, 0.05);
                            }
                            // Detonation VFX
                            WeaponEffects.shockwave(lvl, ParticleTypes.END_ROD, living.getX(), living.getY() + 1.0, living.getZ(), 8.0, 4, 20, 2);
                            lvl.sendParticles((ParticleOptions)ParticleTypes.EXPLOSION_EMITTER, living.getX(), living.getY() + 1.0, living.getZ(), 2, 0.0, 0.0, 0.0, 0.0);
                            lvl.playSound(null, living.getX(), living.getY(), living.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.2f, 0.6f);
                            break;
                        }
                    }
                    expiredArchons.add(dmgKey);
                    expiredArchons.add(targetUuid + ":archon_owner");
                } catch (Exception ignored) {}
                expiredArchons.add(entry.getKey());
            }
        }
        for (String key : expiredArchons) { ACTIVE_BUFFS.remove(key); }
        ACTIVE_BUFFS.entrySet().removeIf(entry -> entry.getValue() <= now);
    }

    private static void checkAndExpireInvuln(ServerPlayer player, String key, long now) {
        Long expiry = ACTIVE_BUFFS.get(key);
        if (expiry != null && now >= expiry) {
            player.setInvulnerable(false);
            ACTIVE_BUFFS.remove(key);
        }
    }

    /**
     * Get the effective attack damage of the player's held weapon.
     * This reads the actual attribute-modified damage so abilities scale with weapon quality.
     */
    private static float getWeaponDamage(ServerPlayer player) {
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) return 1.0f;
        // Get attack damage from item attributes (includes base + modifiers)
        double dmg = player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        return Math.max(1.0f, (float) dmg);
    }

    private static LivingEntity findNearestMob(ServerPlayer player, ServerLevel level, double range) {
        AABB area = new AABB(player.blockPosition()).inflate(range);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive() && (double)e.distanceTo((Entity)player) <= range);
        if (targets.isEmpty()) {
            return null;
        }
        targets.sort(Comparator.comparingDouble(e -> e.distanceToSqr((Entity)player)));
        return targets.getFirst();
    }

    private static boolean executeVoidreaver(ServerPlayer player, RpgWeaponItem.WeaponSkill skill, ServerLevel level) {
        if ("Nether Rend".equals(skill.name())) {
            AABB area = new AABB(player.blockPosition()).inflate(4.0);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive());
            if (targets.isEmpty()) return false;
            Vec3 look = player.getLookAngle();
            WeaponEffects.arc(level, ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 1.0, player.getZ(), look.x, look.z, 4.0, Math.PI * 2 / 3, 16, 2);
            for (LivingEntity target : targets) {
                Vec3 toTarget = target.position().subtract(player.position()).normalize();
                double dot = look.normalize().dot(toTarget);
                if (dot > 0.3) {
                    target.hurt(level.damageSources().magic(), 8.0f + getWeaponDamage(player));
                    level.sendParticles((ParticleOptions)ParticleTypes.SOUL, target.getX(), target.getY() + 1.0, target.getZ(), 5, 0.3, 0.3, 0.3, 0.02);
                }
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.7f, 0.5f);
            return true;
        }
        if ("Dimensional Collapse".equals(skill.name())) {
            Vec3 target = player.position().add(player.getLookAngle().scale(6.0));
            AABB area = new AABB(target.x - 5, target.y - 2, target.z - 5, target.x + 5, target.y + 3, target.z + 5);
            List<LivingEntity> mobs = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive());
            WeaponEffects.converge(level, ParticleTypes.REVERSE_PORTAL, target.x, target.y + 1.0, target.z, 5.0, 20, 3);
            WeaponEffects.ring(level, PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, 1.0f), target.x, target.y + 0.5, target.z, 3.0, 24, 2, 0.05);
            level.sendParticles((ParticleOptions)ParticleTypes.FLASH, target.x, target.y + 1.0, target.z, 1, 0.0, 0.0, 0.0, 0.0);
            for (LivingEntity mob : mobs) {
                mob.hurt(level.damageSources().magic(), 12.0f + getWeaponDamage(player));
                Vec3 pull = target.subtract(mob.position()).normalize().scale(0.5);
                mob.setDeltaMovement(mob.getDeltaMovement().add(pull));
            }
            level.playSound(null, target.x, target.y, target.z, SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.6f, 0.3f);
            return true;
        }
        return false;
    }

    private static boolean executeSolaris(ServerPlayer player, RpgWeaponItem.WeaponSkill skill, ServerLevel level) {
        if ("Consecrate".equals(skill.name())) {
            Vec3 pos = player.position();
            WeaponEffects.shockwave(level, ParticleTypes.END_ROD, pos.x, pos.y + 0.1, pos.z, 5.0, 3, 20, 2);
            WeaponEffects.column(level, ParticleTypes.FLAME, pos.x, pos.y, pos.z, 3.0, 8, 2, 0.1);
            AABB area = new AABB(player.blockPosition()).inflate(5.0);
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive())) {
                e.hurt(level.damageSources().magic(), 6.0f + getWeaponDamage(player));
                e.setRemainingFireTicks(60);
            }
            for (ServerPlayer ally : level.getEntitiesOfClass(ServerPlayer.class, area, e -> e != player)) {
                ally.heal(4.0f);
                level.sendParticles((ParticleOptions)ParticleTypes.HEART, ally.getX(), ally.getY() + 1.5, ally.getZ(), 3, 0.3, 0.3, 0.3, 0.0);
            }
            level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.8f, 1.2f);
            return true;
        }
        if ("Judgment".equals(skill.name())) {
            LivingEntity target = findNearestMob(player, level, 10.0);
            if (target == null) return false;
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, false, true, true));
            target.hurt(level.damageSources().magic(), 15.0f + getWeaponDamage(player));
            WeaponEffects.column(level, ParticleTypes.END_ROD, target.getX(), target.getY(), target.getZ(), 10.0, 20, 3, 0.1);
            level.sendParticles((ParticleOptions)ParticleTypes.FLASH, target.getX(), target.getY() + 1.0, target.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            level.sendParticles((ParticleOptions)ParticleTypes.EXPLOSION_EMITTER, target.getX(), target.getY() + 1.0, target.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.7f, 1.5f);
            return true;
        }
        return false;
    }

    private static boolean executeStormfury(ServerPlayer player, RpgWeaponItem.WeaponSkill skill, ServerLevel level) {
        if ("Lightning Dash".equals(skill.name())) {
            Vec3 look = player.getLookAngle().normalize();
            Vec3 start = player.position();
            Vec3 end = start.add(look.scale(10.0));
            WeaponEffects.line(level, ParticleTypes.ELECTRIC_SPARK, start.add(0, 1, 0), end.add(0, 1, 0), 16, 2, 0.1);
            AABB area = new AABB(Math.min(start.x, end.x) - 1, start.y - 1, Math.min(start.z, end.z) - 1, Math.max(start.x, end.x) + 1, start.y + 3, Math.max(start.z, end.z) + 1);
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive())) {
                e.hurt(level.damageSources().lightningBolt(), 7.0f + getWeaponDamage(player));
                level.sendParticles((ParticleOptions)ParticleTypes.SWEEP_ATTACK, e.getX(), e.getY() + 1.0, e.getZ(), 1, 0, 0, 0, 0);
            }
            player.teleportTo(end.x, end.y, end.z);
            player.fallDistance = 0.0f;
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.6f, 1.5f);
            return true;
        }
        if ("Thunder God's Descent".equals(skill.name())) {
            player.setDeltaMovement(player.getDeltaMovement().add(0, 1.5, 0));
            player.hurtMarked = true;
            AABB area = new AABB(player.blockPosition()).inflate(6.0);
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive())) {
                e.hurt(level.damageSources().lightningBolt(), 12.0f + getWeaponDamage(player));
                Vec3 kb = e.position().subtract(player.position()).normalize();
                e.knockback(1.5, -kb.x, -kb.z);
            }
            WeaponEffects.shockwave(level, ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY(), player.getZ(), 6.0, 4, 24, 3);
            level.sendParticles((ParticleOptions)ParticleTypes.FLASH, player.getX(), player.getY() + 1.0, player.getZ(), 1, 0, 0, 0, 0);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.8f, 0.5f);
            return true;
        }
        return false;
    }

    private static boolean executeBriarthorn(ServerPlayer player, RpgWeaponItem.WeaponSkill skill, ServerLevel level) {
        if ("Entangling Roots".equals(skill.name())) {
            Vec3 pos = player.position().add(player.getLookAngle().scale(4.0));
            AABB area = new AABB(pos.x - 4, pos.y - 1, pos.z - 4, pos.x + 4, pos.y + 3, pos.z + 4);
            List<LivingEntity> mobs = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive());
            if (mobs.isEmpty()) return false;
            WeaponEffects.ring(level, ParticleTypes.COMPOSTER, pos.x, pos.y + 0.2, pos.z, 3.5, 20, 2, 0.05);
            for (LivingEntity mob : mobs) {
                mob.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 4, false, true, true));
                mob.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1, false, true, true));
                WeaponEffects.spiral(level, ParticleTypes.COMPOSTER, mob.getX(), mob.getY(), mob.getZ(), 0.5, 1.5, 8, 1);
            }
            level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.GRASS_BREAK, SoundSource.PLAYERS, 0.8f, 0.6f);
            return true;
        }
        if ("Nature's Wrath".equals(skill.name())) {
            AABB area = new AABB(player.blockPosition()).inflate(6.0);
            List<LivingEntity> mobs = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive());
            if (mobs.isEmpty()) return false;
            for (LivingEntity mob : mobs) {
                mob.hurt(level.damageSources().magic(), 8.0f + getWeaponDamage(player));
                mob.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1, false, true, true));
                level.sendParticles((ParticleOptions)ParticleTypes.COMPOSTER, mob.getX(), mob.getY() + 3.0, mob.getZ(), 10, 0.5, 2.0, 0.5, 0.05);
            }
            WeaponEffects.shockwave(level, ParticleTypes.ITEM_SLIME, player.getX(), player.getY() + 0.1, player.getZ(), 5.0, 2, 16, 1);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GRASS_BREAK, SoundSource.PLAYERS, 1.0f, 0.4f);
            return true;
        }
        return false;
    }

    private static boolean executeAbyssalTrident(ServerPlayer player, RpgWeaponItem.WeaponSkill skill, ServerLevel level) {
        if ("Tidal Surge".equals(skill.name())) {
            Vec3 look = player.getLookAngle().normalize();
            Vec3 start = player.position().add(0, 1, 0);
            Vec3 end = start.add(look.scale(8.0));
            for (int i = -1; i <= 1; i++) {
                Vec3 offset = new Vec3(-look.z, 0, look.x).normalize().scale(i * 0.5);
                WeaponEffects.line(level, ParticleTypes.SPLASH, start.add(offset), end.add(offset), 16, 2, 0.1);
            }
            AABB area = new AABB(Math.min(start.x, end.x) - 2, start.y - 1, Math.min(start.z, end.z) - 2, Math.max(start.x, end.x) + 2, start.y + 2, Math.max(start.z, end.z) + 2);
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive())) {
                e.hurt(level.damageSources().magic(), 7.0f + getWeaponDamage(player));
                Vec3 kb = look.scale(1.5);
                e.setDeltaMovement(e.getDeltaMovement().add(kb.x, 0.3, kb.z));
                e.hurtMarked = true;
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 0.8f, 0.8f);
            return true;
        }
        if ("Maelstrom".equals(skill.name())) {
            Vec3 pos = player.position().add(player.getLookAngle().scale(5.0));
            WeaponEffects.ring(level, ParticleTypes.SPLASH, pos.x, pos.y + 0.5, pos.z, 4.0, 24, 2, 0.05);
            WeaponEffects.ring(level, ParticleTypes.SPLASH, pos.x, pos.y + 1.5, pos.z, 3.0, 20, 2, 0.05);
            WeaponEffects.converge(level, ParticleTypes.BUBBLE, pos.x, pos.y + 1.0, pos.z, 5.0, 20, 2);
            WeaponEffects.spiral(level, ParticleTypes.SNOWFLAKE, pos.x, pos.y, pos.z, 3.0, 3.0, 20, 1);
            AABB area = new AABB(pos.x - 5, pos.y - 1, pos.z - 5, pos.x + 5, pos.y + 3, pos.z + 5);
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive())) {
                e.hurt(level.damageSources().magic(), 10.0f + getWeaponDamage(player));
                Vec3 pull = pos.subtract(e.position()).normalize().scale(0.4);
                e.setDeltaMovement(e.getDeltaMovement().add(pull));
                e.hurtMarked = true;
            }
            level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 1.0f, 0.5f);
            return true;
        }
        return false;
    }

    private static boolean executePyroclast(ServerPlayer player, RpgWeaponItem.WeaponSkill skill, ServerLevel level) {
        if ("Molten Strike".equals(skill.name())) {
            Vec3 look = player.getLookAngle().normalize();
            Vec3 start = player.position().add(0, 0.5, 0);
            Vec3 end = start.add(look.scale(6.0));
            WeaponEffects.line(level, ParticleTypes.FLAME, start, end, 20, 2, 0.1);
            WeaponEffects.line(level, ParticleTypes.LAVA, start, end, 10, 1, 0.15);
            AABB area = new AABB(Math.min(start.x, end.x) - 1.5, start.y - 1, Math.min(start.z, end.z) - 1.5, Math.max(start.x, end.x) + 1.5, start.y + 2, Math.max(start.z, end.z) + 1.5);
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive())) {
                e.hurt(level.damageSources().magic(), 9.0f + getWeaponDamage(player));
                e.setRemainingFireTicks(80);
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.5f, 1.2f);
            return true;
        }
        if ("Eruption".equals(skill.name())) {
            Vec3 pos = player.position();
            WeaponEffects.column(level, ParticleTypes.FLAME, pos.x, pos.y, pos.z, 10.0, 20, 3, 0.2);
            WeaponEffects.shockwave(level, ParticleTypes.LAVA, pos.x, pos.y + 0.2, pos.z, 6.0, 3, 24, 2);
            level.sendParticles((ParticleOptions)ParticleTypes.EXPLOSION_EMITTER, pos.x, pos.y + 2.0, pos.z, 1, 0, 0, 0, 0);
            AABB area = new AABB(player.blockPosition()).inflate(6.0);
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive())) {
                e.hurt(level.damageSources().magic(), 14.0f + getWeaponDamage(player));
                e.setRemainingFireTicks(100);
                e.setDeltaMovement(e.getDeltaMovement().add(0, 0.8, 0));
                e.hurtMarked = true;
            }
            level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0f, 0.3f);
            return true;
        }
        return false;
    }

    private static boolean executeWhisperwind(ServerPlayer player, RpgWeaponItem.WeaponSkill skill, ServerLevel level) {
        if ("Piercing Gale".equals(skill.name())) {
            Vec3 look = player.getLookAngle().normalize();
            Vec3 start = player.position().add(0, 1.2, 0);
            Vec3 end = start.add(look.scale(12.0));
            WeaponEffects.line(level, ParticleTypes.CLOUD, start, end, 20, 2, 0.05);
            WeaponEffects.line(level, ParticleTypes.ENCHANTED_HIT, start, end, 12, 1, 0.03);
            AABB area = new AABB(Math.min(start.x, end.x) - 1, start.y - 1, Math.min(start.z, end.z) - 1, Math.max(start.x, end.x) + 1, start.y + 1, Math.max(start.z, end.z) + 1);
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive())) {
                e.hurt(level.damageSources().magic(), 6.0f + getWeaponDamage(player));
                level.sendParticles((ParticleOptions)ParticleTypes.SWEEP_ATTACK, e.getX(), e.getY() + 1.0, e.getZ(), 1, 0, 0, 0, 0);
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 0.6f, 1.5f);
            return true;
        }
        if ("Cyclone Volley".equals(skill.name())) {
            AABB area = new AABB(player.blockPosition()).inflate(12.0);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive());
            if (targets.isEmpty()) return false;
            targets.sort(java.util.Comparator.comparingDouble(e -> e.distanceToSqr((Entity)player)));
            WeaponEffects.ring(level, ParticleTypes.CLOUD, player.getX(), player.getY() + 1.0, player.getZ(), 1.5, 12, 2, 0.05);
            int count = Math.min(8, targets.size());
            for (int i = 0; i < count; i++) {
                LivingEntity t = targets.get(i);
                Vec3 from = player.position().add(0, 1.2, 0);
                Vec3 to = t.position().add(0, 1.0, 0);
                WeaponEffects.line(level, ParticleTypes.CRIT, from, to, 10, 1, 0.03);
                t.hurt(level.damageSources().magic(), 5.0f + getWeaponDamage(player));
                level.sendParticles((ParticleOptions)ParticleTypes.ENCHANTED_HIT, t.getX(), t.getY() + 1.0, t.getZ(), 5, 0.3, 0.3, 0.3, 0.02);
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 0.5f, 2.0f);
            return true;
        }
        return false;
    }

    private static boolean executeSoulchain(ServerPlayer player, RpgWeaponItem.WeaponSkill skill, ServerLevel level) {
        if ("Soul Lash".equals(skill.name())) {
            LivingEntity target = findNearestMob(player, level, 10.0);
            if (target == null) return false;
            Vec3 from = player.position().add(0, 1, 0);
            Vec3 to = target.position().add(0, 1, 0);
            WeaponEffects.line(level, ParticleTypes.SOUL, from, to, 12, 1, 0.05);
            WeaponEffects.line(level, ParticleTypes.SOUL_FIRE_FLAME, from, to, 8, 1, 0.08);
            target.hurt(level.damageSources().magic(), 6.0f + getWeaponDamage(player));
            Vec3 pull = player.position().subtract(target.position()).normalize().scale(1.2);
            target.setDeltaMovement(target.getDeltaMovement().add(pull.x, 0.3, pull.z));
            target.hurtMarked = true;
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PHANTOM_BITE, SoundSource.PLAYERS, 0.6f, 0.8f);
            return true;
        }
        if ("Reaping Harvest".equals(skill.name())) {
            AABB area = new AABB(player.blockPosition()).inflate(4.0);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive());
            if (targets.isEmpty()) return false;
            WeaponEffects.ring(level, ParticleTypes.SWEEP_ATTACK, player.getX(), player.getY() + 0.5, player.getZ(), 3.5, 12, 1, 0.05);
            WeaponEffects.ring(level, ParticleTypes.SWEEP_ATTACK, player.getX(), player.getY() + 1.0, player.getZ(), 3.0, 12, 1, 0.05);
            WeaponEffects.ring(level, ParticleTypes.SOUL, player.getX(), player.getY() + 0.3, player.getZ(), 4.0, 16, 1, 0.05);
            float totalHeal = 0;
            for (LivingEntity t : targets) {
                t.hurt(level.damageSources().magic(), 8.0f + getWeaponDamage(player));
                level.sendParticles((ParticleOptions)ParticleTypes.SOUL_FIRE_FLAME, t.getX(), t.getY() + 1.0, t.getZ(), 5, 0.3, 0.3, 0.3, 0.02);
                totalHeal += 1.5f;
            }
            player.heal(totalHeal);
            level.sendParticles((ParticleOptions)ParticleTypes.HEART, player.getX(), player.getY() + 1.5, player.getZ(), (int)totalHeal, 0.3, 0.3, 0.3, 0.0);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.8f, 0.7f);
            return true;
        }
        return false;
    }

    private static boolean executeSokaSingingBlade(ServerPlayer player, RpgWeaponItem.WeaponSkill skill, ServerLevel level) {
        float weaponDamage = 12.0f; // Soka base damage
        if ("Annihilating Slash".equals(skill.name())) {
            // 5x weapon damage cone (6-block range) ignoring armor
            Vec3 look = player.getLookAngle();
            AABB area = new AABB(player.blockPosition()).inflate(6.0);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive() && (double)e.distanceTo((Entity)player) <= 6.0);
            boolean hit = false;
            for (LivingEntity target : targets) {
                Vec3 toTarget = target.position().subtract(player.position()).normalize();
                double dot = look.normalize().dot(toTarget);
                if (dot > 0.4) { // ~70 degree cone
                    target.hurt(level.damageSources().magic(), weaponDamage * 5.0f);
                    level.sendParticles((ParticleOptions)ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1.0, target.getZ(), 3, 0.3, 0.3, 0.3, 0.0);
                    level.sendParticles((ParticleOptions)ParticleTypes.END_ROD, target.getX(), target.getY() + 1.0, target.getZ(), 8, 0.4, 0.5, 0.4, 0.05);
                    hit = true;
                }
            }
            if (!hit) return false;
            // Dimensional rift visual: arc of end rod particles
            WeaponEffects.arc(level, ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0, player.getZ(), look.x, look.z, 6.0, Math.PI / 2.5, 20, 2);
            WeaponEffects.arc(level, ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), look.x, look.z, 5.0, Math.PI / 3, 12, 1);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 0.5f);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.6f, 1.5f);
            return true;
        }
        if ("Grand Arcanum".equals(skill.name())) {
            // Massive energy nova: 8x damage in 10-block radius + self buffs
            float damage = weaponDamage * 8.0f;
            AABB area = new AABB(player.blockPosition()).inflate(10.0);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive() && (double)e.distanceTo((Entity)player) <= 10.0);
            for (LivingEntity target : targets) {
                target.hurt(level.damageSources().magic(), damage);
                level.sendParticles(PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, 1.0f), target.getX(), target.getY() + 1.0, target.getZ(), 10, 0.5, 0.5, 0.5, 0.05);
            }
            // Self buffs: Resistance III, Regen II, Speed III for 8 seconds (160 ticks)
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 160, 2, false, true, true));
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 1, false, true, true));
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 160, 2, false, true, true));
            // Nova explosion visual
            WeaponEffects.shockwave(level, ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0, player.getZ(), 10.0, 6, 30, 2);
            WeaponEffects.spiral(level, PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, 1.0f), player.getX(), player.getY(), player.getZ(), 2.0, 3.0, 30, 3);
            WeaponEffects.ring(level, ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 0.5, player.getZ(), 8.0, 24, 2, 0.1);
            level.sendParticles((ParticleOptions)ParticleTypes.EXPLOSION_EMITTER, player.getX(), player.getY() + 1.0, player.getZ(), 2, 0.0, 0.0, 0.0, 0.0);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0f, 0.8f);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f, 1.2f);
            return true;
        }
        if ("Archon's Judgment".equals(skill.name())) {
            // Mark target: amplifies damage by 50% for 5 seconds, then detonates for 10x weapon damage in 8-block radius
            LivingEntity target = findNearestMob(player, level, 8.0);
            if (target == null) return false;
            // Apply Glowing so the mark is visible
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0, false, true, true));
            // Apply Weakness (as damage amplification marker — the detonation handles the real damage)
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0, false, true, true));
            // Mark visual on target
            level.sendParticles((ParticleOptions)ParticleTypes.END_ROD, target.getX(), target.getY() + 2.0, target.getZ(), 20, 0.3, 0.3, 0.3, 0.02);
            level.sendParticles((ParticleOptions)ParticleTypes.ENCHANT, target.getX(), target.getY() + 1.0, target.getZ(), 15, 0.5, 1.0, 0.5, 0.1);
            level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.8f, 1.5f);
            // Schedule detonation after 5 seconds (100 ticks) using the buff system
            String markKey = target.getUUID() + ":archon_mark";
            ACTIVE_BUFFS.put(markKey, level.getGameTime() + 100L);
            // Store the detonation damage for the tick handler
            String dmgKey = target.getUUID() + ":archon_dmg";
            ACTIVE_BUFFS.put(dmgKey, (long)(weaponDamage * 10.0f));
            String ownerKey = target.getUUID() + ":archon_owner";
            ACTIVE_BUFFS.put(ownerKey, player.getUUID().getMostSignificantBits());
            return true;
        }
        return false;
    }
}
