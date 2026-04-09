package com.ultra.megamod.feature.relics.weapons;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.PowerParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ArsenalWeaponEvents {

    private static float getWeaponDamage(ServerPlayer player) {
        double dmg = player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        return Math.max(1.0f, (float) dmg);
    }

    public static boolean executeSkill(ServerPlayer player, String weaponName, RpgWeaponItem.WeaponSkill skill, ServerLevel level) {
        return switch (weaponName) {
            // Claymores
            case "megamod:unique_claymore_1" -> executeClaymore1(player, skill, level);
            case "megamod:unique_claymore_2" -> executeClaymore2(player, skill, level);
            case "megamod:unique_claymore_sw" -> executeClaymoreElven(player, skill, level);
            // Daggers
            case "megamod:unique_dagger_1" -> executeDagger1(player, skill, level);
            case "megamod:unique_dagger_2" -> executeDagger2(player, skill, level);
            case "megamod:unique_dagger_sw" -> executeDaggerElven(player, skill, level);
            // Double Axes
            case "megamod:unique_double_axe_1" -> executeDoubleAxe1(player, skill, level);
            case "megamod:unique_double_axe_2" -> executeDoubleAxe2(player, skill, level);
            case "megamod:unique_double_axe_sw" -> executeDoubleAxeElven(player, skill, level);
            // Glaives
            case "megamod:unique_glaive_1" -> executeGlaive1(player, skill, level);
            case "megamod:unique_glaive_2" -> executeGlaive2(player, skill, level);
            case "megamod:unique_glaive_sw" -> executeGlaiveElven(player, skill, level);
            // Hammers
            case "megamod:unique_hammer_1" -> executeHammer1(player, skill, level);
            case "megamod:unique_hammer_2" -> executeHammer2(player, skill, level);
            case "megamod:unique_hammer_sw" -> executeHammerElven(player, skill, level);
            // Maces
            case "megamod:unique_mace_1" -> executeMace1(player, skill, level);
            case "megamod:unique_mace_2" -> executeMace2(player, skill, level);
            case "megamod:unique_mace_sw" -> executeMaceElven(player, skill, level);
            // Sickles
            case "megamod:unique_sickle_1" -> executeSickle1(player, skill, level);
            case "megamod:unique_sickle_2" -> executeSickle2(player, skill, level);
            case "megamod:unique_sickle_sw" -> executeSickleElven(player, skill, level);
            // Spears
            case "megamod:unique_spear_1" -> executeSpear1(player, skill, level);
            case "megamod:unique_spear_2" -> executeSpear2(player, skill, level);
            case "megamod:unique_spear_sw" -> executeSpearElven(player, skill, level);
            // Longsword
            case "megamod:unique_longsword_sw" -> executeLongsword(player, skill, level);
            // Longbows
            case "megamod:unique_longbow_1" -> executeLongbow1(player, skill, level);
            case "megamod:unique_longbow_2" -> executeLongbow2(player, skill, level);
            case "megamod:unique_longbow_sw" -> executeLongbowElven(player, skill, level);
            // Heavy Crossbows
            case "megamod:unique_heavy_crossbow_1" -> executeHeavyCrossbow1(player, skill, level);
            case "megamod:unique_heavy_crossbow_2" -> executeHeavyCrossbow2(player, skill, level);
            case "megamod:unique_heavy_crossbow_sw" -> executeHeavyCrossbowElven(player, skill, level);
            // Damage Staves
            case "megamod:unique_staff_damage_1" -> executeStaffDamage1(player, skill, level);
            case "megamod:unique_staff_damage_2" -> executeStaffDamage2(player, skill, level);
            case "megamod:unique_staff_damage_3" -> executeStaffDamage3(player, skill, level);
            case "megamod:unique_staff_damage_4" -> executeStaffDamage4(player, skill, level);
            case "megamod:unique_staff_damage_5" -> executeStaffDamage5(player, skill, level);
            case "megamod:unique_staff_damage_6" -> executeStaffDamage6(player, skill, level);
            case "megamod:unique_staff_damage_sw" -> executeStaffDamageElven(player, skill, level);
            // Healing Staves
            case "megamod:unique_staff_heal_1" -> executeStaffHeal1(player, skill, level);
            case "megamod:unique_staff_heal_2" -> executeStaffHeal2(player, skill, level);
            case "megamod:unique_staff_heal_sw" -> executeStaffHealElven(player, skill, level);
            // Shields
            case "megamod:unique_shield_1" -> executeShield1(player, skill, level);
            case "megamod:unique_shield_2" -> executeShield2(player, skill, level);
            case "megamod:unique_shield_sw" -> executeShieldElven(player, skill, level);
            // Whips
            case "megamod:unique_whip_1" -> executeWhip1(player, skill, level);
            case "megamod:unique_whip_2" -> executeWhip2(player, skill, level);
            case "megamod:unique_whip_sw" -> executeWhipElven(player, skill, level);
            // Wands
            case "megamod:unique_wand_1" -> executeWand1(player, skill, level);
            case "megamod:unique_wand_2" -> executeWand2(player, skill, level);
            case "megamod:unique_wand_sw" -> executeWandElven(player, skill, level);
            // Katanas
            case "megamod:unique_katana_1" -> executeKatana1(player, skill, level);
            case "megamod:unique_katana_2" -> executeKatana2(player, skill, level);
            case "megamod:unique_katana_sw" -> executeKatanaElven(player, skill, level);
            // Greatshields
            case "megamod:unique_greatshield_1" -> executeGreatshield1(player, skill, level);
            case "megamod:unique_greatshield_2" -> executeGreatshield2(player, skill, level);
            case "megamod:unique_greatshield_sw" -> executeGreatshieldElven(player, skill, level);
            // Throwing Axes
            case "megamod:unique_throwing_axe_1" -> executeThrowingAxe1(player, skill, level);
            case "megamod:unique_throwing_axe_2" -> executeThrowingAxe2(player, skill, level);
            case "megamod:unique_throwing_axe_sw" -> executeThrowingAxeElven(player, skill, level);
            // Rapiers
            case "megamod:unique_rapier_1" -> executeRapier1(player, skill, level);
            case "megamod:unique_rapier_2" -> executeRapier2(player, skill, level);
            case "megamod:unique_rapier_sw" -> executeRapierElven(player, skill, level);
            // Fill-in variants + new longswords
            case "megamod:unique_longsword_1" -> executeLongsword1(player, skill, level);
            case "megamod:unique_longsword_2" -> executeLongsword2(player, skill, level);
            case "megamod:unique_claymore_3" -> executeClaymore3(player, skill, level);
            case "megamod:unique_dagger_3" -> executeDagger3(player, skill, level);
            case "megamod:unique_double_axe_3" -> executeDoubleAxe3(player, skill, level);
            case "megamod:unique_glaive_3" -> executeGlaive3(player, skill, level);
            case "megamod:unique_hammer_3" -> executeHammer3(player, skill, level);
            case "megamod:unique_mace_3" -> executeMace3(player, skill, level);
            case "megamod:unique_sickle_3" -> executeSickle3(player, skill, level);
            case "megamod:unique_spear_3" -> executeSpear3(player, skill, level);
            case "megamod:unique_longbow_3" -> executeLongbow3(player, skill, level);
            case "megamod:unique_heavy_crossbow_3" -> executeHeavyCrossbow3(player, skill, level);
            case "megamod:unique_staff_damage_8" -> executeStaffDamage8(player, skill, level);
            case "megamod:unique_staff_heal_3" -> executeStaffHeal3(player, skill, level);
            case "megamod:unique_shield_3" -> executeShield3(player, skill, level);
            case "megamod:unique_longsword_3" -> executeLongsword3(player, skill, level);
            case "megamod:unique_whip_3" -> executeWhip3(player, skill, level);
            case "megamod:unique_wand_3" -> executeWand3(player, skill, level);
            case "megamod:unique_katana_3" -> executeKatana3(player, skill, level);
            case "megamod:unique_greatshield_3" -> executeGreatshield3(player, skill, level);
            case "megamod:unique_throwing_axe_3" -> executeThrowingAxe3(player, skill, level);
            case "megamod:unique_rapier_3" -> executeRapier3(player, skill, level);
            default -> false;
        };
    }

    // ========================
    // CLAYMORES
    // ========================

    private static boolean executeClaymore1(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Explosive Strike".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 6.0);
            if (target == null) return false;
            doExplosion(p, l, target.position(), 8.0f + getWeaponDamage(p), 3.0);
            return true;
        }
        if ("Flame Cleave".equals(s.name())) {
            doConeAttack(p, l, 7.0f + getWeaponDamage(p), 4.0);
            doFlameParticles(l, p.position().add(p.getLookAngle().scale(2.0)), 15);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 0.7f);
            return true;
        }
        return false;
    }

    private static boolean executeClaymore2(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Radiant Strike".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 6.0);
            if (target == null) return false;
            target.hurt(l.damageSources().magic(), 8.0f + getWeaponDamage(p));
            doRadiance(p, l, 4.0f, 3.0);
            return true;
        }
        if ("Holy Cleave".equals(s.name())) {
            doConeAttack(p, l, 9.0f + getWeaponDamage(p), 5.0);
            doRadiance(p, l, 3.0f, 4.0);
            l.sendParticles((ParticleOptions) ParticleTypes.END_ROD, p.getX(), p.getY() + 1.0, p.getZ(), 20, 3.0, 1.0, 3.0, 0.05);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.6f, 1.8f);
            return true;
        }
        return false;
    }

    private static boolean executeClaymoreElven(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Soul Rampage".equals(s.name())) {
            doRampage(p, l, 200, 1);
            return true;
        }
        if ("Annihilating Slash".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 6.0);
            if (target == null) return false;
            target.hurt(l.damageSources().playerAttack(p), 20.0f + getWeaponDamage(p));
            // Massive slash arc + soul fire explosion
            Vec3 look = p.getLookAngle().normalize();
            WeaponEffects.arc(l, ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1.0, target.getZ(),
                look.x, look.z, 2.0, Math.PI * 0.8, 12, 2);
            l.sendParticles((ParticleOptions) ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + 1.0, target.getZ(), 20, 0.6, 0.8, 0.6, 0.08);
            l.sendParticles((ParticleOptions) ParticleTypes.ENCHANTED_HIT, target.getX(), target.getY() + 1.0, target.getZ(), 12, 0.4, 0.4, 0.4, 0.15);
            l.sendParticles((ParticleOptions) ParticleTypes.FLASH, target.getX(), target.getY() + 1.0, target.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            l.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.2f, 0.6f);
            return true;
        }
        return false;
    }

    // ========================
    // DAGGERS
    // ========================

    private static boolean executeDagger1(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Frostbite".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 5.0);
            if (target == null) return false;
            doFrostbite(p, l, target, 80, 1);
            return true;
        }
        if ("Frozen Barrage".equals(s.name())) {
            List<LivingEntity> targets = getTargetsInRange(p, l, 4.0);
            for (LivingEntity t : targets) {
                t.hurt(l.damageSources().freeze(), 4.0f + getWeaponDamage(p));
                t.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 2, false, true, true));
                t.setTicksFrozen(t.getTicksFrozen() + 100);
                l.sendParticles((ParticleOptions) ParticleTypes.ITEM_SNOWBALL, t.getX(), t.getY() + 0.5, t.getZ(), 6, 0.2, 0.4, 0.2, 0.08);
            }
            // Frost nova ring
            WeaponEffects.shockwave(l, ParticleTypes.SNOWFLAKE, p.getX(), p.getY() + 0.3, p.getZ(), 4.0, 2, 14, 2);
            l.sendParticles((ParticleOptions) ParticleTypes.SNOWFLAKE, p.getX(), p.getY() + 1.0, p.getZ(), 25, 2.0, 1.0, 2.0, 0.1);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.8f, 1.5f);
            return true;
        }
        return false;
    }

    private static boolean executeDagger2(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Life Leech".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 5.0);
            if (target == null) return false;
            doLifeLeech(p, l, target, 6.0f + getWeaponDamage(p), 0.5f);
            return true;
        }
        if ("Shadow Strike".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 12.0);
            if (target == null) return false;
            Vec3 origin = p.position();
            Vec3 behind = target.position().add(target.getLookAngle().scale(-1.5));
            // Vanish burst at origin
            l.sendParticles((ParticleOptions) ParticleTypes.LARGE_SMOKE, origin.x, origin.y + 1.0, origin.z, 12, 0.4, 0.6, 0.4, 0.02);
            l.sendParticles((ParticleOptions) ParticleTypes.REVERSE_PORTAL, origin.x, origin.y + 1.0, origin.z, 10, 0.3, 0.5, 0.3, 0.05);
            p.teleportTo(behind.x, behind.y, behind.z);
            p.fallDistance = 0.0f;
            target.hurt(l.damageSources().playerAttack(p), 12.0f + getWeaponDamage(p));
            // Arrival + backstab burst
            l.sendParticles((ParticleOptions) ParticleTypes.SMOKE, behind.x, behind.y + 1.0, behind.z, 15, 0.3, 0.5, 0.3, 0.05);
            l.sendParticles((ParticleOptions) ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1.0, target.getZ(), 4, 0.3, 0.3, 0.3, 0.0);
            l.sendParticles((ParticleOptions) ParticleTypes.CRIT, target.getX(), target.getY() + 1.0, target.getZ(), 8, 0.3, 0.4, 0.3, 0.1);
            l.playSound(null, origin.x, origin.y, origin.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.4f, 1.8f);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.6f, 1.5f);
            l.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 0.8f, 1.2f);
            return true;
        }
        return false;
    }

    private static boolean executeDaggerElven(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Armor Sunder".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 5.0);
            if (target == null) return false;
            doArmorSunder(p, l, target, 100);
            return true;
        }
        if ("Apocalypse Mark".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 8.0);
            if (target == null) return false;
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0, false, true, true));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 1, false, true, true));
            l.sendParticles((ParticleOptions) ParticleTypes.ANGRY_VILLAGER, target.getX(), target.getY() + 2.0, target.getZ(), 10, 0.5, 0.5, 0.5, 0.0);
            l.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.3f, 2.0f);
            return true;
        }
        return false;
    }

    // ========================
    // DOUBLE AXES
    // ========================

    private static boolean executeDoubleAxe1(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Butcher's Leech".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 5.0);
            if (target == null) return false;
            doLifeLeech(p, l, target, 8.0f + getWeaponDamage(p), 0.4f);
            return true;
        }
        if ("Cleaving Frenzy".equals(s.name())) {
            doWhirlwind(p, l, 7.0f + getWeaponDamage(p), 4.0);
            doWhirlwind(p, l, 5.0f + getWeaponDamage(p), 4.0);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.2f, 0.6f);
            return true;
        }
        return false;
    }

    private static boolean executeDoubleAxe2(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Wither Strike".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 5.0);
            if (target == null) return false;
            doWitherStrike(p, l, target, 100, 2);
            return true;
        }
        if ("Reaping Swing".equals(s.name())) {
            List<LivingEntity> targets = getTargetsInRange(p, l, 5.0);
            for (LivingEntity t : targets) {
                t.hurt(l.damageSources().playerAttack(p), 8.0f + getWeaponDamage(p));
                t.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 1, false, true, true));
            }
            l.sendParticles((ParticleOptions) ParticleTypes.SWEEP_ATTACK, p.getX(), p.getY() + 1.0, p.getZ(), 8, 3.0, 0.5, 3.0, 0.0);
            l.sendParticles((ParticleOptions) ParticleTypes.SMOKE, p.getX(), p.getY() + 1.0, p.getZ(), 20, 3.0, 1.0, 3.0, 0.05);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.WITHER_HURT, SoundSource.PLAYERS, 0.6f, 0.8f);
            return true;
        }
        return false;
    }

    private static boolean executeDoubleAxeElven(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("War Rampage".equals(s.name())) {
            doRampage(p, l, 240, 1);
            return true;
        }
        if ("Battle Cry".equals(s.name())) {
            p.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 200, 1, false, true, true));
            p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 200, 0, false, true, true));
            AABB area = new AABB(p.blockPosition()).inflate(8.0);
            for (Player ally : p.level().getEntitiesOfClass(Player.class, area, e -> e != p && e.isAlive())) {
                ally.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 200, 0, false, true, true));
                ally.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 200, 0, false, true, true));
            }
            l.sendParticles((ParticleOptions) ParticleTypes.TOTEM_OF_UNDYING, p.getX(), p.getY() + 1.0, p.getZ(), 30, 2.0, 1.0, 2.0, 0.3);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.EVOKER_PREPARE_ATTACK, SoundSource.PLAYERS, 1.0f, 0.8f);
            return true;
        }
        return false;
    }

    // ========================
    // GLAIVES
    // ========================

    private static boolean executeGlaive1(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Flame Strike".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 7.0);
            if (target == null) return false;
            doFlameCloud(p, l, target.position(), 6.0f + getWeaponDamage(p));
            return true;
        }
        if ("Infernal Thrust".equals(s.name())) {
            doLinePierce(p, l, 10.0f + getWeaponDamage(p), 10, true);
            return true;
        }
        return false;
    }

    private static boolean executeGlaive2(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Shockwave".equals(s.name())) {
            doShockwave(p, l, 7.0f + getWeaponDamage(p), 10);
            return true;
        }
        if ("Crystal Sweep".equals(s.name())) {
            List<LivingEntity> targets = getTargetsInRange(p, l, 5.0);
            for (LivingEntity t : targets) {
                t.hurt(l.damageSources().playerAttack(p), 7.0f + getWeaponDamage(p));
                double dx = t.getX() - p.getX();
                double dz = t.getZ() - p.getZ();
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > 0) t.knockback(1.5, -dx / dist, -dz / dist);
            }
            l.sendParticles((ParticleOptions) ParticleTypes.END_ROD, p.getX(), p.getY() + 1.0, p.getZ(), 25, 3.0, 0.5, 3.0, 0.05);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 1.2f);
            return true;
        }
        return false;
    }

    private static boolean executeGlaiveElven(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Fel Swirl".equals(s.name())) {
            doWhirlwind(p, l, 6.0f + getWeaponDamage(p), 4.0);
            l.sendParticles((ParticleOptions) ParticleTypes.SOUL_FIRE_FLAME, p.getX(), p.getY() + 1.0, p.getZ(), 20, 2.5, 0.5, 2.5, 0.05);
            return true;
        }
        if ("Spine Thrust".equals(s.name())) {
            doLinePierce(p, l, 9.0f + getWeaponDamage(p), 12, false);
            return true;
        }
        return false;
    }

    // ========================
    // HAMMERS
    // ========================

    private static boolean executeHammer1(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Shockwave Slam".equals(s.name())) {
            doGroundSlam(p, l, 8.0f + getWeaponDamage(p), 5.0);
            doShockwave(p, l, 6.0f + getWeaponDamage(p), 8);
            return true;
        }
        if ("Destined Impact".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 5.0);
            if (target == null) return false;
            target.hurt(l.damageSources().playerAttack(p), 18.0f + getWeaponDamage(p));
            doStun(l, target, 60);
            l.sendParticles((ParticleOptions) ParticleTypes.EXPLOSION, target.getX(), target.getY() + 1.0, target.getZ(), 3, 0.5, 0.5, 0.5, 0.0);
            l.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.0f, 0.5f);
            return true;
        }
        return false;
    }

    private static boolean executeHammer2(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Explosive Impact".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 5.0);
            if (target == null) return false;
            doExplosion(p, l, target.position(), 10.0f + getWeaponDamage(p), 3.5);
            return true;
        }
        if ("Scorched Earth".equals(s.name())) {
            doExplosion(p, l, p.position(), 6.0f + getWeaponDamage(p), 6.0);
            doFlameParticles(l, p.position(), 40);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0f, 0.5f);
            return true;
        }
        return false;
    }

    private static boolean executeHammerElven(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Holy Radiance".equals(s.name())) {
            doRadiance(p, l, 6.0f, 5.0);
            List<LivingEntity> targets = getTargetsInRange(p, l, 5.0);
            for (LivingEntity t : targets) {
                if (t.isInvertedHealAndHarm()) {
                    t.hurt(l.damageSources().magic(), 10.0f + getWeaponDamage(p));
                }
            }
            return true;
        }
        if ("Sanctify".equals(s.name())) {
            doRadiance(p, l, 8.0f, 6.0);
            List<LivingEntity> targets = getTargetsInRange(p, l, 6.0);
            for (LivingEntity t : targets) {
                if (t.isInvertedHealAndHarm()) {
                    t.hurt(l.damageSources().magic(), 15.0f + getWeaponDamage(p));
                }
            }
            l.sendParticles((ParticleOptions) ParticleTypes.END_ROD, p.getX(), p.getY() + 0.5, p.getZ(), 40, 4.0, 1.5, 4.0, 0.05);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f, 1.5f);
            return true;
        }
        return false;
    }

    // ========================
    // MACES
    // ========================

    private static boolean executeMace1(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Armor Sunder".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 5.0);
            if (target == null) return false;
            doArmorSunder(p, l, target, 100);
            return true;
        }
        if ("Bone Crush".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 5.0);
            if (target == null) return false;
            target.hurt(l.damageSources().playerAttack(p), 12.0f + getWeaponDamage(p));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1, false, true, true));
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 1, false, true, true));
            l.sendParticles((ParticleOptions) ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getY() + 1.0, target.getZ(), 8, 0.3, 0.3, 0.3, 0.0);
            l.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.PLAYERS, 0.6f, 1.5f);
            return true;
        }
        return false;
    }

    private static boolean executeMace2(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Stun Strike".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 5.0);
            if (target == null) return false;
            target.hurt(l.damageSources().playerAttack(p), 6.0f + getWeaponDamage(p));
            doStun(l, target, 40);
            l.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0f, 0.8f);
            return true;
        }
        if ("Storm Smash".equals(s.name())) {
            List<LivingEntity> targets = getTargetsInRange(p, l, 5.0);
            for (LivingEntity t : targets) {
                t.hurt(l.damageSources().lightningBolt(), 8.0f + getWeaponDamage(p));
                // Lightning impact per target
                l.sendParticles((ParticleOptions) ParticleTypes.ELECTRIC_SPARK, t.getX(), t.getY() + 1.0, t.getZ(), 12, 0.3, 0.5, 0.3, 0.12);
            }
            // Electric shockwave rings
            WeaponEffects.shockwave(l, ParticleTypes.ELECTRIC_SPARK, p.getX(), p.getY() + 0.3, p.getZ(), 5.0, 3, 16, 2);
            l.sendParticles((ParticleOptions) ParticleTypes.FLASH, p.getX(), p.getY() + 1.0, p.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.5f, 1.5f);
            return true;
        }
        return false;
    }

    private static boolean executeMaceElven(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Guarding Strike".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 5.0);
            if (target == null) return false;
            target.hurt(l.damageSources().playerAttack(p), 7.0f + getWeaponDamage(p));
            p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 100, 1, false, true, true));
            // Shield aura on player + hit effect on target
            WeaponEffects.ring(l, ParticleTypes.HAPPY_VILLAGER, p.getX(), p.getY() + 0.5, p.getZ(), 1.0, 8, 2, 0.03);
            l.sendParticles((ParticleOptions) ParticleTypes.ENCHANTED_HIT, target.getX(), target.getY() + 1.0, target.getZ(), 8, 0.3, 0.4, 0.3, 0.1);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.8f, 1.2f);
            return true;
        }
        if ("Archon's Judgment".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 8.0);
            if (target == null) return false;
            target.hurt(l.damageSources().magic(), 14.0f + getWeaponDamage(p));
            p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 200, 1, false, true, true));
            p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 1, false, true, true));
            // Holy beam from sky onto target
            WeaponEffects.column(l, ParticleTypes.END_ROD, target.getX(), target.getY(), target.getZ(), 8.0, 14, 2, 0.1);
            WeaponEffects.ring(l, ParticleTypes.END_ROD, target.getX(), target.getY() + 0.3, target.getZ(), 1.2, 10, 2, 0.03);
            l.sendParticles((ParticleOptions) ParticleTypes.FLASH, target.getX(), target.getY() + 1.0, target.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            // Shield on player
            WeaponEffects.ring(l, ParticleTypes.END_ROD, p.getX(), p.getY() + 1.0, p.getZ(), 1.0, 8, 2, 0.02);
            l.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.7f, 2.0f);
            return true;
        }
        return false;
    }

    // ========================
    // SICKLES
    // ========================

    private static boolean executeSickle1(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Poison Cloud".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 6.0);
            if (target == null) return false;
            doPoisonCloud(p, l, target.position(), 5.0f + getWeaponDamage(p));
            return true;
        }
        if ("Toxic Slash".equals(s.name())) {
            doConeAttack(p, l, 5.0f + getWeaponDamage(p), 4.0);
            List<LivingEntity> targets = getConeTargets(p, l, 4.0);
            for (LivingEntity t : targets) {
                t.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1, false, true, true));
            }
            l.sendParticles((ParticleOptions) ParticleTypes.ITEM_SLIME, p.getX(), p.getY() + 1.0, p.getZ(), 15, 2.0, 0.5, 2.0, 0.05);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.8f, 1.3f);
            return true;
        }
        return false;
    }

    private static boolean executeSickle2(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Infernal Explosion".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 5.0);
            if (target == null) return false;
            doExplosion(p, l, target.position(), 7.0f + getWeaponDamage(p), 2.5);
            return true;
        }
        if ("Soul Harvest".equals(s.name())) {
            List<LivingEntity> targets = getTargetsInRange(p, l, 5.0);
            float totalHeal = 0;
            for (LivingEntity t : targets) {
                t.hurt(l.damageSources().playerAttack(p), 5.0f + getWeaponDamage(p));
                totalHeal += 1.5f;
            }
            p.heal(totalHeal);
            l.sendParticles((ParticleOptions) ParticleTypes.SOUL, p.getX(), p.getY() + 1.0, p.getZ(), 20, 3.0, 1.0, 3.0, 0.05);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.WITHER_HURT, SoundSource.PLAYERS, 0.5f, 1.5f);
            return true;
        }
        return false;
    }

    private static boolean executeSickleElven(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Swirling Reap".equals(s.name())) {
            doWhirlwind(p, l, 5.0f + getWeaponDamage(p), 3.5);
            l.sendParticles((ParticleOptions) ParticleTypes.ENCHANT, p.getX(), p.getY() + 1.0, p.getZ(), 20, 2.0, 0.5, 2.0, 0.1);
            return true;
        }
        if ("Elven Harvest".equals(s.name())) {
            List<LivingEntity> targets = getTargetsInRange(p, l, 4.0);
            float totalHeal = 0;
            for (LivingEntity t : targets) {
                t.hurt(l.damageSources().playerAttack(p), 4.0f + getWeaponDamage(p));
                t.hurt(l.damageSources().playerAttack(p), 3.0f + getWeaponDamage(p));
                totalHeal += 2.0f;
            }
            p.heal(totalHeal);
            l.sendParticles((ParticleOptions) ParticleTypes.HEART, p.getX(), p.getY() + 2.0, p.getZ(), 5, 0.5, 0.5, 0.5, 0.0);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 1.0f);
            return true;
        }
        return false;
    }

    // ========================
    // SPEARS
    // ========================

    private static boolean executeSpear1(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Frostbite Thrust".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 7.0);
            if (target == null) return false;
            target.hurt(l.damageSources().playerAttack(p), 6.0f + getWeaponDamage(p));
            doFrostbite(p, l, target, 80, 1);
            return true;
        }
        if ("Sonic Impale".equals(s.name())) {
            doLinePierce(p, l, 9.0f + getWeaponDamage(p), 10, false);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0f, 1.2f);
            return true;
        }
        return false;
    }

    private static boolean executeSpear2(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Stun Thrust".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 7.0);
            if (target == null) return false;
            target.hurt(l.damageSources().playerAttack(p), 7.0f + getWeaponDamage(p));
            doStun(l, target, 40);
            l.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.8f, 1.0f);
            return true;
        }
        if ("Divine Impale".equals(s.name())) {
            doLinePierce(p, l, 10.0f + getWeaponDamage(p), 10, false);
            l.sendParticles((ParticleOptions) ParticleTypes.END_ROD, p.getX(), p.getY() + 1.0, p.getZ(), 15, 0.5, 0.5, 0.5, 0.1);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0f, 1.5f);
            return true;
        }
        return false;
    }

    private static boolean executeSpearElven(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Vengeance Leech".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 7.0);
            if (target == null) return false;
            doLifeLeech(p, l, target, 7.0f + getWeaponDamage(p), 0.5f);
            return true;
        }
        if ("Mounting Strike".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 7.0);
            if (target == null) return false;
            target.hurt(l.damageSources().playerAttack(p), 6.0f + getWeaponDamage(p));
            target.hurt(l.damageSources().playerAttack(p), 8.0f + getWeaponDamage(p));
            target.hurt(l.damageSources().playerAttack(p), 10.0f + getWeaponDamage(p));
            l.sendParticles((ParticleOptions) ParticleTypes.CRIT, target.getX(), target.getY() + 1.0, target.getZ(), 15, 0.3, 0.5, 0.3, 0.1);
            l.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0f, 1.0f);
            return true;
        }
        return false;
    }

    // ========================
    // LONGSWORD
    // ========================

    private static boolean executeLongsword(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Armor Sunder".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 5.0);
            if (target == null) return false;
            doArmorSunder(p, l, target, 100);
            return true;
        }
        if ("Dragon Strike".equals(s.name())) {
            doConeAttack(p, l, 10.0f + getWeaponDamage(p), 5.0);
            Vec3 look = p.getLookAngle().normalize();
            // Dragon fire breath cone
            for (int i = 1; i <= 5; i++) {
                Vec3 pt = p.position().add(look.scale(i));
                float spread = 0.2f + i * 0.15f;
                l.sendParticles((ParticleOptions) ParticleTypes.FLAME, pt.x, pt.y + 1.0, pt.z, 6 + i, spread, spread, spread, 0.03);
                l.sendParticles((ParticleOptions) ParticleTypes.SMOKE, pt.x, pt.y + 1.2, pt.z, 2, spread * 0.5, spread * 0.5, spread * 0.5, 0.01);
                if (i % 2 == 0)
                    l.sendParticles((ParticleOptions) ParticleTypes.LAVA, pt.x, pt.y + 0.8, pt.z, 1, spread * 0.3, 0.1, spread * 0.3, 0.0);
            }
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 0.8f);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.5f, 1.2f);
            return true;
        }
        return false;
    }

    // ========================
    // LONGBOWS
    // ========================

    private static boolean executeLongbow1(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Radiant Arrow".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 16.0);
            if (target == null) return false;
            target.hurt(l.damageSources().magic(), 8.0f + getWeaponDamage(p));
            doRadiance(p, l, 4.0f, 3.0);
            // Radiant arrow trail
            Vec3 from = new Vec3(p.getX(), p.getY() + 1.5, p.getZ());
            Vec3 to = new Vec3(target.getX(), target.getY() + 1.0, target.getZ());
            WeaponEffects.line(l, ParticleTypes.END_ROD, from, to, 12, 2, 0.06);
            // Impact burst
            l.sendParticles((ParticleOptions) ParticleTypes.END_ROD, target.getX(), target.getY() + 1.0, target.getZ(), 15, 0.4, 0.5, 0.4, 0.08);
            l.sendParticles((ParticleOptions) ParticleTypes.ENCHANTED_HIT, target.getX(), target.getY() + 1.0, target.getZ(), 6, 0.3, 0.3, 0.3, 0.1);
            return true;
        }
        if ("Solar Volley".equals(s.name())) {
            doArrowBurst(p, l, 5, 10.0f + getWeaponDamage(p), 16.0);
            l.sendParticles((ParticleOptions) ParticleTypes.END_ROD, p.getX(), p.getY() + 2.0, p.getZ(), 20, 1.0, 0.5, 1.0, 0.1);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0f, 1.5f);
            return true;
        }
        return false;
    }

    private static boolean executeLongbow2(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Withering Shot".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 16.0);
            if (target == null) return false;
            target.hurt(l.damageSources().magic(), 6.0f + getWeaponDamage(p));
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 2, false, true, true));
            // Dark arrow trail
            Vec3 from = new Vec3(p.getX(), p.getY() + 1.5, p.getZ());
            Vec3 to = new Vec3(target.getX(), target.getY() + 1.0, target.getZ());
            WeaponEffects.line(l, ParticleTypes.SMOKE, from, to, 12, 2, 0.06);
            WeaponEffects.line(l, ParticleTypes.SOUL_FIRE_FLAME, from, to, 6, 1, 0.04);
            // Wither impact
            WeaponEffects.spiral(l, ParticleTypes.SMOKE, target.getX(), target.getY(), target.getZ(), 0.7, 1.5, 10, 2);
            l.sendParticles((ParticleOptions) ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + 1.0, target.getZ(), 8, 0.3, 0.4, 0.3, 0.05);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0f, 0.8f);
            return true;
        }
        if ("Dark Barrage".equals(s.name())) {
            doArrowBurst(p, l, 4, 7.0f + getWeaponDamage(p), 16.0);
            List<LivingEntity> targets = getTargetsInRange(p, l, 16.0);
            for (LivingEntity t : targets.subList(0, Math.min(4, targets.size()))) {
                t.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 1, false, true, true));
            }
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0f, 0.6f);
            return true;
        }
        return false;
    }

    private static boolean executeLongbowElven(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Focusing Shot".equals(s.name())) {
            p.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 200, 1, false, true, true));
            l.sendParticles((ParticleOptions) ParticleTypes.ENCHANT, p.getX(), p.getY() + 1.0, p.getZ(), 15, 0.5, 1.0, 0.5, 0.1);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5f, 2.0f);
            return true;
        }
        if ("Arrow Rain".equals(s.name())) {
            doArrowBurst(p, l, 8, 8.0f + getWeaponDamage(p), 20.0);
            l.sendParticles((ParticleOptions) ParticleTypes.CRIT, p.getX(), p.getY() + 3.0, p.getZ(), 30, 3.0, 1.0, 3.0, 0.1);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.2f, 1.2f);
            return true;
        }
        return false;
    }

    // ========================
    // HEAVY CROSSBOWS
    // ========================

    private static boolean executeHeavyCrossbow1(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Flame Bolt".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 16.0);
            if (target == null) return false;
            target.hurt(l.damageSources().magic(), 8.0f + getWeaponDamage(p));
            // Fire bolt trail
            Vec3 from = new Vec3(p.getX(), p.getY() + 1.5, p.getZ());
            Vec3 to = new Vec3(target.getX(), target.getY() + 1.0, target.getZ());
            WeaponEffects.line(l, ParticleTypes.FLAME, from, to, 14, 2, 0.08);
            WeaponEffects.line(l, ParticleTypes.SMOKE, from, to, 7, 1, 0.06);
            doFlameCloud(p, l, target.position(), 5.0f + getWeaponDamage(p));
            return true;
        }
        if ("Phoenix Shot".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 20.0);
            if (target == null) return false;
            target.hurt(l.damageSources().magic(), 15.0f + getWeaponDamage(p));
            // Phoenix trail
            Vec3 from = new Vec3(p.getX(), p.getY() + 1.5, p.getZ());
            Vec3 to = new Vec3(target.getX(), target.getY() + 1.0, target.getZ());
            WeaponEffects.line(l, ParticleTypes.FLAME, from, to, 16, 3, 0.12);
            WeaponEffects.line(l, ParticleTypes.LAVA, from, to, 6, 1, 0.08);
            doExplosion(p, l, target.position(), 8.0f + getWeaponDamage(p), 4.0);
            l.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.8f, 1.2f);
            return true;
        }
        return false;
    }

    private static boolean executeHeavyCrossbow2(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Toxic Bolt".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 16.0);
            if (target == null) return false;
            target.hurt(l.damageSources().magic(), 7.0f + getWeaponDamage(p));
            // Poison bolt trail
            Vec3 from = new Vec3(p.getX(), p.getY() + 1.5, p.getZ());
            Vec3 to = new Vec3(target.getX(), target.getY() + 1.0, target.getZ());
            WeaponEffects.line(l, ParticleTypes.ITEM_SLIME, from, to, 12, 2, 0.06);
            doPoisonCloud(p, l, target.position(), 5.0f + getWeaponDamage(p));
            return true;
        }
        if ("Death Bolt".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 20.0);
            if (target == null) return false;
            target.hurt(l.damageSources().magic(), 14.0f + getWeaponDamage(p));
            // Death bolt trail
            Vec3 from = new Vec3(p.getX(), p.getY() + 1.5, p.getZ());
            Vec3 to = new Vec3(target.getX(), target.getY() + 1.0, target.getZ());
            WeaponEffects.line(l, ParticleTypes.SMOKE, from, to, 14, 2, 0.08);
            WeaponEffects.line(l, ParticleTypes.SOUL_FIRE_FLAME, from, to, 8, 1, 0.05);
            List<LivingEntity> nearby = l.getEntitiesOfClass(LivingEntity.class, new AABB(target.blockPosition()).inflate(4.0), e -> e != p && e.isAlive());
            for (LivingEntity t : nearby) {
                t.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 2, false, true, true));
            }
            // Dark death cloud on impact
            WeaponEffects.ring(l, ParticleTypes.SMOKE, target.getX(), target.getY() + 0.5, target.getZ(), 2.5, 12, 2, 0.15);
            l.sendParticles((ParticleOptions) ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + 1.0, target.getZ(), 15, 1.5, 0.8, 1.5, 0.05);
            l.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 0.8f, 0.8f);
            return true;
        }
        return false;
    }

    private static boolean executeHeavyCrossbowElven(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Multi-Shot".equals(s.name())) {
            doArrowBurst(p, l, 3, 9.0f + getWeaponDamage(p), 16.0);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0f, 1.2f);
            return true;
        }
        if ("Relentless Barrage".equals(s.name())) {
            doArrowBurst(p, l, 6, 8.0f + getWeaponDamage(p), 16.0);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.2f, 0.8f);
            return true;
        }
        return false;
    }

    // ========================
    // DAMAGE STAVES
    // ========================

    private static boolean executeStaffDamage1(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Frost Bolt".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 12.0);
            if (target == null) return false;
            target.hurt(l.damageSources().freeze(), 8.0f + getWeaponDamage(p));
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 2, false, true, true));
            // Frost bolt line from staff to target
            Vec3 from = new Vec3(p.getX(), p.getY() + 1.5, p.getZ());
            Vec3 to = new Vec3(target.getX(), target.getY() + 1.0, target.getZ());
            WeaponEffects.line(l, ParticleTypes.SNOWFLAKE, from, to, 12, 2, 0.08);
            WeaponEffects.line(l, ParticleTypes.ITEM_SNOWBALL, from, to, 8, 1, 0.05);
            // Impact shatter
            WeaponEffects.converge(l, ParticleTypes.SNOWFLAKE, target.getX(), target.getY() + 1.0, target.getZ(), 1.5, 8, 2);
            l.sendParticles((ParticleOptions) ParticleTypes.ITEM_SNOWBALL, target.getX(), target.getY() + 0.5, target.getZ(), 10, 0.3, 0.5, 0.3, 0.1);
            l.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.5f, 2.0f);
            return true;
        }
        if ("Cooldown Reset".equals(s.name())) {
            RpgWeaponEvents.clearCooldownsForPlayer(p);
            // Arcane spiral reset effect
            WeaponEffects.spiral(l, ParticleTypes.ENCHANT, p.getX(), p.getY(), p.getZ(), 1.0, 2.5, 20, 2);
            l.sendParticles((ParticleOptions) ParticleTypes.END_ROD, p.getX(), p.getY() + 1.0, p.getZ(), 15, 0.5, 0.8, 0.5, 0.08);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8f, 2.0f);
            return true;
        }
        return false;
    }

    private static boolean executeStaffDamage2(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Chain Reaction".equals(s.name())) {
            float damage = 7.0f + getWeaponDamage(p);
            AABB area = new AABB(p.blockPosition()).inflate(12.0);
            List<LivingEntity> all = l.getEntitiesOfClass(LivingEntity.class, area, e -> e != p && e.isAlive());
            if (all.isEmpty()) return false;
            all.sort(Comparator.comparingDouble(e -> e.distanceToSqr((Entity) p)));
            Set<Integer> hitIds = new HashSet<>();
            LivingEntity current = all.getFirst();
            Vec3 previousPos = new Vec3(p.getX(), p.getY() + 1.5, p.getZ());
            // Muzzle flash from staff
            l.sendParticles((ParticleOptions) ParticleTypes.ELECTRIC_SPARK, p.getX(), p.getY() + 1.5, p.getZ(), 10, 0.15, 0.15, 0.15, 0.05);
            for (int i = 0; i < 3 && current != null; i++) {
                current.hurt(l.damageSources().magic(), damage);
                hitIds.add(current.getId());
                Vec3 targetPos = new Vec3(current.getX(), current.getY() + 1.0, current.getZ());
                // Lightning bolt line between chain points
                WeaponEffects.line(l, ParticleTypes.ELECTRIC_SPARK, previousPos, targetPos, 16, 2, 0.1);
                // Impact burst
                l.sendParticles((ParticleOptions) ParticleTypes.ELECTRIC_SPARK, current.getX(), current.getY() + 1.0, current.getZ(), 20, 0.4, 0.6, 0.4, 0.15);
                l.sendParticles((ParticleOptions) ParticleTypes.FLASH, current.getX(), current.getY() + 1.0, current.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
                // Per-target zap sound
                l.playSound(null, current.getX(), current.getY(), current.getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.4f, 1.8f + i * 0.2f);
                previousPos = targetPos;
                LivingEntity chainFrom = current;
                List<LivingEntity> next = l.getEntitiesOfClass(LivingEntity.class, new AABB(chainFrom.blockPosition()).inflate(6.0), e -> e != p && e.isAlive() && !hitIds.contains(e.getId()));
                if (next.isEmpty()) break;
                next.sort(Comparator.comparingDouble(e -> e.distanceToSqr((Entity) chainFrom)));
                current = next.getFirst();
                damage *= 0.8f;
            }
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.5f, 2.0f);
            return true;
        }
        if ("Arcane Blast".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 12.0);
            if (target == null) return false;
            target.hurt(l.damageSources().magic(), 10.0f + getWeaponDamage(p));
            // Arcane bolt line from staff to target
            Vec3 from = new Vec3(p.getX(), p.getY() + 1.5, p.getZ());
            Vec3 to = new Vec3(target.getX(), target.getY() + 1.0, target.getZ());
            WeaponEffects.line(l, ParticleTypes.ENCHANT, from, to, 14, 2, 0.08);
            WeaponEffects.line(l, ParticleTypes.END_ROD, from, to, 8, 1, 0.05);
            // Impact burst
            l.sendParticles((ParticleOptions) ParticleTypes.ENCHANT, target.getX(), target.getY() + 1.0, target.getZ(), 20, 0.4, 0.6, 0.4, 0.3);
            l.sendParticles((ParticleOptions) ParticleTypes.ENCHANTED_HIT, target.getX(), target.getY() + 1.0, target.getZ(), 8, 0.3, 0.3, 0.3, 0.1);
            l.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.SHULKER_SHOOT, SoundSource.PLAYERS, 0.8f, 1.5f);
            return true;
        }
        return false;
    }

    private static boolean executeStaffDamage3(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Flame Cloud".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 12.0);
            if (target == null) return false;
            // Fireball line from staff to target
            Vec3 from = new Vec3(p.getX(), p.getY() + 1.5, p.getZ());
            Vec3 to = new Vec3(target.getX(), target.getY() + 1.0, target.getZ());
            WeaponEffects.line(l, ParticleTypes.FLAME, from, to, 12, 2, 0.1);
            WeaponEffects.line(l, ParticleTypes.SMOKE, from, to, 6, 1, 0.08);
            doFlameCloud(p, l, target.position(), 7.0f + getWeaponDamage(p));
            return true;
        }
        if ("Dragon Breath".equals(s.name())) {
            Vec3 look = p.getLookAngle().normalize();
            Vec3 start = p.getEyePosition();
            // Muzzle burst
            Vec3 muzzle = start.add(look.scale(1.0));
            l.sendParticles((ParticleOptions) ParticleTypes.FLAME, muzzle.x, muzzle.y, muzzle.z, 10, 0.2, 0.2, 0.2, 0.05);
            for (int i = 1; i <= 8; i++) {
                Vec3 pt = start.add(look.scale(i));
                float radius = 0.5f + i * 0.3f;
                AABB check = new AABB(pt.x - radius, pt.y - radius, pt.z - radius, pt.x + radius, pt.y + radius, pt.z + radius);
                List<LivingEntity> hit = l.getEntitiesOfClass(LivingEntity.class, check, e -> e != p && e.isAlive());
                for (LivingEntity t : hit) {
                    t.hurt(l.damageSources().magic(), 6.0f + getWeaponDamage(p));
                    t.setRemainingFireTicks(60);
                }
                // Expanding fire cone
                l.sendParticles((ParticleOptions) ParticleTypes.FLAME, pt.x, pt.y, pt.z, 6 + i, radius * 0.4, radius * 0.4, radius * 0.4, 0.03);
                l.sendParticles((ParticleOptions) ParticleTypes.SMOKE, pt.x, pt.y, pt.z, 2, radius * 0.3, radius * 0.3, radius * 0.3, 0.01);
                if (i % 2 == 0)
                    l.sendParticles((ParticleOptions) ParticleTypes.LAVA, pt.x, pt.y, pt.z, 1, radius * 0.2, 0.1, radius * 0.2, 0.0);
            }
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0f, 0.8f);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.5f, 0.8f);
            return true;
        }
        return false;
    }

    private static boolean executeStaffDamage4(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Soul Leech".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 12.0);
            if (target == null) return false;
            // Soul beam from staff to target
            Vec3 from = new Vec3(p.getX(), p.getY() + 1.5, p.getZ());
            Vec3 to = new Vec3(target.getX(), target.getY() + 1.0, target.getZ());
            WeaponEffects.line(l, ParticleTypes.SOUL, from, to, 10, 1, 0.06);
            doLifeLeech(p, l, target, 7.0f + getWeaponDamage(p), 0.5f);
            return true;
        }
        if ("Gargoyle's Curse".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 12.0);
            if (target == null) return false;
            target.hurt(l.damageSources().magic(), 5.0f + getWeaponDamage(p));
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1, false, true, true));
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 1, false, true, true));
            // Dark curse bolt from staff to target
            Vec3 from = new Vec3(p.getX(), p.getY() + 1.5, p.getZ());
            Vec3 to = new Vec3(target.getX(), target.getY() + 1.0, target.getZ());
            WeaponEffects.line(l, ParticleTypes.SMOKE, from, to, 12, 2, 0.08);
            WeaponEffects.line(l, ParticleTypes.SOUL_FIRE_FLAME, from, to, 8, 1, 0.05);
            // Curse spiral on target
            WeaponEffects.spiral(l, ParticleTypes.SMOKE, target.getX(), target.getY(), target.getZ(), 0.8, 2.0, 12, 2);
            l.sendParticles((ParticleOptions) ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + 1.0, target.getZ(), 10, 0.3, 0.5, 0.3, 0.05);
            l.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.WITHER_HURT, SoundSource.PLAYERS, 0.5f, 1.2f);
            return true;
        }
        return false;
    }

    private static boolean executeStaffDamage5(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Shockwave Burst".equals(s.name())) {
            Vec3[] dirs = {new Vec3(1, 0, 0), new Vec3(-1, 0, 0), new Vec3(0, 0, 1), new Vec3(0, 0, -1)};
            // Central burst
            WeaponEffects.ring(l, ParticleTypes.CLOUD, p.getX(), p.getY() + 0.5, p.getZ(), 1.5, 12, 2, 0.08);
            l.sendParticles((ParticleOptions) ParticleTypes.EXPLOSION, p.getX(), p.getY() + 0.5, p.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            for (Vec3 dir : dirs) {
                for (int i = 1; i <= 8; i++) {
                    Vec3 pt = p.position().add(dir.scale(i));
                    AABB check = new AABB(pt.x - 1.0, pt.y - 0.5, pt.z - 1.0, pt.x + 1.0, pt.y + 1.5, pt.z + 1.0);
                    List<LivingEntity> hit = l.getEntitiesOfClass(LivingEntity.class, check, e -> e != p && e.isAlive());
                    for (LivingEntity t : hit) {
                        t.hurt(l.damageSources().magic(), 6.0f + getWeaponDamage(p));
                        t.knockback(0.5, -dir.x, -dir.z);
                    }
                    // Cross-shaped shockwave trail
                    l.sendParticles((ParticleOptions) ParticleTypes.CLOUD, pt.x, pt.y + 0.5, pt.z, 3, 0.3, 0.1, 0.3, 0.01);
                    l.sendParticles((ParticleOptions) ParticleTypes.ENCHANTED_HIT, pt.x, pt.y + 0.5, pt.z, 2, 0.2, 0.1, 0.2, 0.02);
                }
            }
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.6f, 1.5f);
            return true;
        }
        if ("Arcane Barrage".equals(s.name())) {
            List<LivingEntity> targets = l.getEntitiesOfClass(LivingEntity.class, new AABB(p.blockPosition()).inflate(12.0), e -> e != p && e.isAlive());
            targets.sort(Comparator.comparingDouble(e -> e.distanceToSqr((Entity) p)));
            int count = Math.min(3, targets.size());
            Vec3 from = new Vec3(p.getX(), p.getY() + 1.5, p.getZ());
            for (int i = 0; i < count; i++) {
                LivingEntity t = targets.get(i);
                t.hurt(l.damageSources().magic(), 5.0f + getWeaponDamage(p));
                // Arcane bolt to each target
                Vec3 to = new Vec3(t.getX(), t.getY() + 1.0, t.getZ());
                WeaponEffects.line(l, ParticleTypes.ENCHANT, from, to, 10, 2, 0.06);
                l.sendParticles((ParticleOptions) ParticleTypes.ENCHANTED_HIT, t.getX(), t.getY() + 1.0, t.getZ(), 10, 0.3, 0.4, 0.3, 0.1);
            }
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.SHULKER_SHOOT, SoundSource.PLAYERS, 0.7f, 1.8f);
            return count > 0;
        }
        return false;
    }

    private static boolean executeStaffDamage6(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Frosty Puddle".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 12.0);
            if (target == null) return false;
            // Ice bolt from staff to target
            Vec3 from = new Vec3(p.getX(), p.getY() + 1.5, p.getZ());
            Vec3 to = new Vec3(target.getX(), target.getY() + 1.0, target.getZ());
            WeaponEffects.line(l, ParticleTypes.SNOWFLAKE, from, to, 10, 2, 0.08);
            doFrostCloud(p, l, target.position(), 5.0f + getWeaponDamage(p));
            return true;
        }
        if ("Blizzard".equals(s.name())) {
            List<LivingEntity> targets = getTargetsInRange(p, l, 8.0);
            for (LivingEntity t : targets) {
                t.hurt(l.damageSources().freeze(), 8.0f + getWeaponDamage(p));
                t.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 3, false, true, true));
                t.setTicksFrozen(t.getTicksFrozen() + 200);
                // Ice shatter on each target
                l.sendParticles((ParticleOptions) ParticleTypes.ITEM_SNOWBALL, t.getX(), t.getY() + 0.5, t.getZ(), 8, 0.3, 0.3, 0.3, 0.1);
            }
            // Blizzard: falling snowflakes + frost rings
            l.sendParticles((ParticleOptions) ParticleTypes.SNOWFLAKE, p.getX(), p.getY() + 4.0, p.getZ(), 60, 5.0, 2.0, 5.0, 0.05);
            WeaponEffects.shockwave(l, ParticleTypes.SNOWFLAKE, p.getX(), p.getY() + 0.2, p.getZ(), 8.0, 3, 20, 2);
            l.sendParticles((ParticleOptions) ParticleTypes.CLOUD, p.getX(), p.getY() + 3.0, p.getZ(), 20, 4.0, 0.5, 4.0, 0.01);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0f, 0.5f);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.POWDER_SNOW_STEP, SoundSource.PLAYERS, 1.0f, 0.5f);
            return true;
        }
        return false;
    }

    private static boolean executeStaffDamageElven(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Surging Power".equals(s.name())) {
            p.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 240, 1, false, true, true));
            p.addEffect(new MobEffectInstance(MobEffects.HASTE, 240, 0, false, true, true));
            l.sendParticles((ParticleOptions) ParticleTypes.ENCHANT, p.getX(), p.getY() + 1.0, p.getZ(), 20, 0.5, 1.0, 0.5, 0.2);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.7f, 2.0f);
            return true;
        }
        if ("Grand Arcanum".equals(s.name())) {
            List<LivingEntity> targets = getTargetsInRange(p, l, 10.0);
            for (LivingEntity t : targets) {
                t.hurt(l.damageSources().magic(), 12.0f + getWeaponDamage(p));
            }
            l.sendParticles((ParticleOptions) ParticleTypes.DRAGON_BREATH, p.getX(), p.getY() + 1.0, p.getZ(), 50, 6.0, 1.0, 6.0, 0.05);
            l.sendParticles((ParticleOptions) ParticleTypes.END_ROD, p.getX(), p.getY() + 2.0, p.getZ(), 30, 4.0, 2.0, 4.0, 0.1);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.8f, 1.8f);
            return true;
        }
        return false;
    }

    // ========================
    // HEALING STAVES
    // ========================

    private static boolean executeStaffHeal1(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Radiance".equals(s.name())) {
            doRadiance(p, l, 6.0f, 5.0);
            return true;
        }
        if ("Crystal Shield".equals(s.name())) {
            p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 2, false, true, true));
            AABB area = new AABB(p.blockPosition()).inflate(5.0);
            for (Player ally : p.level().getEntitiesOfClass(Player.class, area, e -> e != p && e.isAlive())) {
                ally.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 1, false, true, true));
                // Shield on each ally
                WeaponEffects.ring(l, ParticleTypes.END_ROD, ally.getX(), ally.getY() + 1.0, ally.getZ(), 0.8, 8, 2, 0.02);
            }
            // Crystal shield dome on caster
            WeaponEffects.ring(l, ParticleTypes.END_ROD, p.getX(), p.getY() + 0.2, p.getZ(), 1.5, 14, 2, 0.02);
            WeaponEffects.ring(l, ParticleTypes.END_ROD, p.getX(), p.getY() + 1.0, p.getZ(), 1.2, 10, 2, 0.02);
            WeaponEffects.ring(l, ParticleTypes.END_ROD, p.getX(), p.getY() + 1.8, p.getZ(), 0.6, 8, 2, 0.02);
            l.sendParticles((ParticleOptions) ParticleTypes.ENCHANT, p.getX(), p.getY() + 0.5, p.getZ(), 15, 1.0, 0.8, 1.0, 0.5);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.8f, 1.5f);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.5f, 1.8f);
            return true;
        }
        return false;
    }

    private static boolean executeStaffHeal2(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Guardian Remedy".equals(s.name())) {
            p.heal(8.0f);
            p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 120, 1, false, true, true));
            // Healing column + hearts
            WeaponEffects.column(l, ParticleTypes.END_ROD, p.getX(), p.getY(), p.getZ(), 3.0, 8, 2, 0.1);
            WeaponEffects.spiral(l, ParticleTypes.HEART, p.getX(), p.getY(), p.getZ(), 0.8, 2.0, 8, 1);
            l.sendParticles((ParticleOptions) ParticleTypes.ENCHANT, p.getX(), p.getY() + 0.5, p.getZ(), 12, 0.5, 0.8, 0.5, 0.5);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.6f, 2.0f);
            return true;
        }
        if ("Purify".equals(s.name())) {
            // Only remove harmful effects
            p.getActiveEffects().stream()
                .filter(e -> !e.getEffect().value().isBeneficial())
                .map(e -> e.getEffect())
                .toList()
                .forEach(p::removeEffect);
            p.heal(6.0f);
            AABB area = new AABB(p.blockPosition()).inflate(5.0);
            for (Player ally : p.level().getEntitiesOfClass(Player.class, area, e -> e != p && e.isAlive())) {
                // Only remove harmful effects from allies
                ally.getActiveEffects().stream()
                    .filter(e -> !e.getEffect().value().isBeneficial())
                    .map(e -> e.getEffect())
                    .toList()
                    .forEach(ally::removeEffect);
                ally.heal(4.0f);
            }
            l.sendParticles((ParticleOptions) ParticleTypes.HAPPY_VILLAGER, p.getX(), p.getY() + 1.0, p.getZ(), 25, 3.0, 1.0, 3.0, 0.0);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f, 1.8f);
            return true;
        }
        return false;
    }

    private static boolean executeStaffHealElven(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Cooldown Touch".equals(s.name())) {
            if (p.getHealth() > p.getMaxHealth() * 0.5f) return false;
            RpgWeaponEvents.clearCooldownsForPlayer(p);
            l.sendParticles((ParticleOptions) ParticleTypes.ENCHANT, p.getX(), p.getY() + 1.0, p.getZ(), 25, 0.5, 1.0, 0.5, 0.2);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8f, 2.0f);
            return true;
        }
        if ("Sin'dorei Blessing".equals(s.name())) {
            p.heal(p.getMaxHealth() * 0.5f);
            p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1, false, true, true));
            p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 2, false, true, true));
            p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 200, 0, false, true, true));
            AABB area = new AABB(p.blockPosition()).inflate(6.0);
            for (Player ally : p.level().getEntitiesOfClass(Player.class, area, e -> e != p && e.isAlive())) {
                ally.heal(ally.getMaxHealth() * 0.3f);
                ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0, false, true, true));
            }
            l.sendParticles((ParticleOptions) ParticleTypes.TOTEM_OF_UNDYING, p.getX(), p.getY() + 1.0, p.getZ(), 40, 3.0, 2.0, 3.0, 0.3);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.8f, 1.2f);
            return true;
        }
        return false;
    }

    // ========================
    // SHIELDS
    // ========================

    private static boolean executeShield1(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Spiked Block".equals(s.name())) {
            p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 60, 2, false, true, true));
            RpgWeaponEvents.setActiveBuff(p, "spiked_reflect", l.getGameTime() + 60L);
            // Spiked shield glow
            WeaponEffects.ring(l, ParticleTypes.CRIT, p.getX(), p.getY() + 1.0, p.getZ(), 0.8, 10, 2, 0.03);
            Vec3 look = p.getLookAngle().normalize();
            double fx = p.getX() + look.x * 0.6;
            double fz = p.getZ() + look.z * 0.6;
            l.sendParticles((ParticleOptions) ParticleTypes.CRIT, fx, p.getY() + 1.0, fz, 12, 0.3, 0.5, 0.3, 0.1);
            l.sendParticles((ParticleOptions) ParticleTypes.ENCHANTED_HIT, fx, p.getY() + 1.0, fz, 6, 0.2, 0.4, 0.2, 0.05);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0f, 0.8f);
            return true;
        }
        if ("Shield Bash".equals(s.name())) {
            List<LivingEntity> targets = getTargetsInRange(p, l, 3.0);
            Vec3 look = p.getLookAngle().normalize();
            for (LivingEntity t : targets) {
                t.hurt(l.damageSources().playerAttack(p), 5.0f + getWeaponDamage(p));
                doStun(l, t, 30);
                double dx = t.getX() - p.getX();
                double dz = t.getZ() - p.getZ();
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > 0) t.knockback(1.0, -dx / dist, -dz / dist);
            }
            // Shield bash shockwave in front
            WeaponEffects.arc(l, ParticleTypes.CLOUD, p.getX(), p.getY() + 1.0, p.getZ(),
                look.x, look.z, 2.5, Math.PI * 0.5, 10, 2);
            l.sendParticles((ParticleOptions) ParticleTypes.EXPLOSION, p.getX() + look.x * 1.5, p.getY() + 1.0, p.getZ() + look.z * 1.5, 1, 0.0, 0.0, 0.0, 0.0);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.2f, 0.6f);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.4f, 1.5f);
            return true;
        }
        return false;
    }

    private static boolean executeShield2(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Guarding Aura".equals(s.name())) {
            p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 100, 1, false, true, true));
            AABB area = new AABB(p.blockPosition()).inflate(5.0);
            for (Player ally : p.level().getEntitiesOfClass(Player.class, area, e -> e != p && e.isAlive())) {
                ally.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 100, 0, false, true, true));
            }
            l.sendParticles((ParticleOptions) ParticleTypes.HAPPY_VILLAGER, p.getX(), p.getY() + 1.0, p.getZ(), 15, 3.0, 1.0, 3.0, 0.0);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.8f, 1.2f);
            return true;
        }
        if ("Light Barrier".equals(s.name())) {
            p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 3, false, true, true));
            p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 200, 1, false, true, true));
            l.sendParticles((ParticleOptions) ParticleTypes.END_ROD, p.getX(), p.getY() + 1.0, p.getZ(), 20, 1.0, 1.0, 1.0, 0.05);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.7f, 1.5f);
            return true;
        }
        return false;
    }

    private static boolean executeShieldElven(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Unyielding".equals(s.name())) {
            p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 100, 2, false, true, true));
            l.sendParticles((ParticleOptions) ParticleTypes.COMPOSTER, p.getX(), p.getY() + 1.0, p.getZ(), 10, 0.5, 1.0, 0.5, 0.05);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 0.5f, 1.0f);
            return true;
        }
        if ("Bulwark Slam".equals(s.name())) {
            List<LivingEntity> targets = getTargetsInRange(p, l, 5.0);
            for (LivingEntity t : targets) {
                t.hurt(l.damageSources().playerAttack(p), 6.0f + getWeaponDamage(p));
                double dx = t.getX() - p.getX();
                double dz = t.getZ() - p.getZ();
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > 0) t.knockback(2.5, -dx / dist, -dz / dist);
            }
            l.sendParticles((ParticleOptions) ParticleTypes.EXPLOSION, p.getX(), p.getY() + 0.5, p.getZ(), 3, 2.0, 0.5, 2.0, 0.0);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.6f, 1.0f);
            return true;
        }
        return false;
    }

    // ========================
    // WHIPS
    // ========================

    private static boolean executeWhip1(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Venom Crack".equals(s.name())) {
            Vec3 look = p.getLookAngle().normalize();
            Vec3 start = p.position().add(0, 1.0, 0);
            Vec3 end = start.add(look.scale(8.0));
            WeaponEffects.line(l, ParticleTypes.ITEM_SLIME, start, end, 16, 2, 0.1);
            AABB area = new AABB(Math.min(start.x, end.x) - 1, start.y - 1, Math.min(start.z, end.z) - 1, Math.max(start.x, end.x) + 1, start.y + 2, Math.max(start.z, end.z) + 1);
            for (LivingEntity t : l.getEntitiesOfClass(LivingEntity.class, area, e -> e != p && e.isAlive())) {
                t.hurt(l.damageSources().magic(), 7.0f + getWeaponDamage(p));
                t.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1, false, true, true));
            }
            l.sendParticles((ParticleOptions) ParticleTypes.ITEM_SLIME, p.getX(), p.getY() + 1.0, p.getZ(), 10, 1.0, 0.3, 1.0, 0.05);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.8f, 1.3f);
            return true;
        }
        if ("Constrict".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 10.0);
            if (target == null) return false;
            Vec3 from = p.position().add(0, 1, 0);
            Vec3 to = target.position().add(0, 1, 0);
            WeaponEffects.line(l, ParticleTypes.ITEM_SLIME, from, to, 12, 2, 0.08);
            target.hurt(l.damageSources().magic(), 10.0f + getWeaponDamage(p));
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1, false, true, true));
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 2, false, true, true));
            Vec3 pull = p.position().subtract(target.position()).normalize().scale(1.0);
            target.setDeltaMovement(target.getDeltaMovement().add(pull.x, 0.2, pull.z));
            target.hurtMarked = true;
            l.sendParticles((ParticleOptions) ParticleTypes.ITEM_SLIME, target.getX(), target.getY() + 0.5, target.getZ(), 12, 0.4, 0.6, 0.4, 0.05);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 0.8f, 1.0f);
            return true;
        }
        return false;
    }

    private static boolean executeWhip2(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Fire Crack".equals(s.name())) {
            Vec3 look = p.getLookAngle().normalize();
            Vec3 start = p.position().add(0, 1.0, 0);
            Vec3 end = start.add(look.scale(8.0));
            WeaponEffects.line(l, ParticleTypes.FLAME, start, end, 16, 2, 0.1);
            WeaponEffects.line(l, ParticleTypes.SMOKE, start, end, 8, 1, 0.08);
            AABB area = new AABB(Math.min(start.x, end.x) - 1, start.y - 1, Math.min(start.z, end.z) - 1, Math.max(start.x, end.x) + 1, start.y + 2, Math.max(start.z, end.z) + 1);
            for (LivingEntity t : l.getEntitiesOfClass(LivingEntity.class, area, e -> e != p && e.isAlive())) {
                t.hurt(l.damageSources().magic(), 6.0f + getWeaponDamage(p));
                t.setRemainingFireTicks(80);
            }
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.6f, 1.2f);
            return true;
        }
        if ("Infernal Coil".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 10.0);
            if (target == null) return false;
            Vec3 from = p.position().add(0, 1, 0);
            Vec3 to = target.position().add(0, 1, 0);
            WeaponEffects.line(l, ParticleTypes.FLAME, from, to, 14, 2, 0.08);
            target.hurt(l.damageSources().magic(), 8.0f + getWeaponDamage(p));
            target.setRemainingFireTicks(100);
            Vec3 pull = p.position().subtract(target.position()).normalize().scale(1.2);
            target.setDeltaMovement(target.getDeltaMovement().add(pull.x, 0.2, pull.z));
            target.hurtMarked = true;
            doFlameParticles(l, target.position(), 12);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.5f, 0.8f);
            return true;
        }
        return false;
    }

    private static boolean executeWhipElven(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Thorn Snap".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 12.0);
            if (target == null) return false;
            Vec3 from = p.position().add(0, 1, 0);
            Vec3 to = target.position().add(0, 1, 0);
            WeaponEffects.line(l, ParticleTypes.SOUL, from, to, 14, 2, 0.06);
            WeaponEffects.line(l, ParticleTypes.SOUL_FIRE_FLAME, from, to, 8, 1, 0.04);
            target.hurt(l.damageSources().magic(), 8.0f + getWeaponDamage(p));
            doStun(l, target, 40);
            Vec3 pull = p.position().subtract(target.position()).normalize().scale(1.5);
            target.setDeltaMovement(target.getDeltaMovement().add(pull.x, 0.3, pull.z));
            target.hurtMarked = true;
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.PHANTOM_BITE, SoundSource.PLAYERS, 0.7f, 0.8f);
            return true;
        }
        if ("Binding Vines".equals(s.name())) {
            List<LivingEntity> targets = getTargetsInRange(p, l, 8.0);
            if (targets.isEmpty()) return false;
            Vec3 from = p.position().add(0, 1.2, 0);
            int count = Math.min(6, targets.size());
            for (int i = 0; i < count; i++) {
                LivingEntity t = targets.get(i);
                Vec3 to = t.position().add(0, 1, 0);
                WeaponEffects.line(l, ParticleTypes.ENCHANTED_HIT, from, to, 10, 1, 0.05);
                t.hurt(l.damageSources().magic(), 6.0f + getWeaponDamage(p));
                l.sendParticles((ParticleOptions) ParticleTypes.ENCHANTED_HIT, t.getX(), t.getY() + 1.0, t.getZ(), 8, 0.3, 0.3, 0.3, 0.1);
            }
            WeaponEffects.ring(l, ParticleTypes.ENCHANT, p.getX(), p.getY() + 1.0, p.getZ(), 1.5, 12, 2, 0.05);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 1.2f);
            return true;
        }
        return false;
    }

    // ========================
    // WANDS
    // ========================

    private static boolean executeWand1(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Arcane Missile".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 12.0);
            if (target == null) return false;
            Vec3 from = new Vec3(p.getX(), p.getY() + 1.5, p.getZ());
            Vec3 to = new Vec3(target.getX(), target.getY() + 1.0, target.getZ());
            WeaponEffects.line(l, ParticleTypes.ENCHANT, from, to, 14, 2, 0.06);
            WeaponEffects.line(l, ParticleTypes.END_ROD, from, to, 6, 1, 0.04);
            target.hurt(l.damageSources().magic(), 8.0f + getWeaponDamage(p));
            l.sendParticles((ParticleOptions) ParticleTypes.ENCHANTED_HIT, target.getX(), target.getY() + 1.0, target.getZ(), 12, 0.3, 0.4, 0.3, 0.1);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.SHULKER_SHOOT, SoundSource.PLAYERS, 0.7f, 1.5f);
            return true;
        }
        if ("Mana Burst".equals(s.name())) {
            List<LivingEntity> targets = getTargetsInRange(p, l, 8.0);
            if (targets.isEmpty()) return false;
            Vec3 from = new Vec3(p.getX(), p.getY() + 1.5, p.getZ());
            for (LivingEntity t : targets) {
                Vec3 to = new Vec3(t.getX(), t.getY() + 1.0, t.getZ());
                WeaponEffects.line(l, ParticleTypes.ENCHANT, from, to, 10, 1, 0.05);
                t.hurt(l.damageSources().magic(), 5.0f + getWeaponDamage(p));
                l.sendParticles((ParticleOptions) ParticleTypes.ENCHANTED_HIT, t.getX(), t.getY() + 1.0, t.getZ(), 8, 0.3, 0.3, 0.3, 0.08);
            }
            WeaponEffects.ring(l, ParticleTypes.END_ROD, p.getX(), p.getY() + 1.5, p.getZ(), 1.0, 10, 2, 0.03);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.SHULKER_SHOOT, SoundSource.PLAYERS, 0.6f, 1.8f);
            return true;
        }
        return false;
    }

    private static boolean executeWand2(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Ice Shard".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 12.0);
            if (target == null) return false;
            Vec3 from = new Vec3(p.getX(), p.getY() + 1.5, p.getZ());
            Vec3 to = new Vec3(target.getX(), target.getY() + 1.0, target.getZ());
            WeaponEffects.line(l, ParticleTypes.SNOWFLAKE, from, to, 12, 2, 0.06);
            WeaponEffects.line(l, ParticleTypes.ITEM_SNOWBALL, from, to, 6, 1, 0.04);
            target.hurt(l.damageSources().freeze(), 7.0f + getWeaponDamage(p));
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2, false, true, true));
            target.setTicksFrozen(target.getTicksFrozen() + 80);
            WeaponEffects.converge(l, ParticleTypes.SNOWFLAKE, target.getX(), target.getY() + 1.0, target.getZ(), 1.0, 8, 2);
            l.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.5f, 2.0f);
            return true;
        }
        if ("Flash Freeze".equals(s.name())) {
            List<LivingEntity> targets = getTargetsInRange(p, l, 6.0);
            if (targets.isEmpty()) return false;
            for (LivingEntity t : targets) {
                t.hurt(l.damageSources().freeze(), 6.0f + getWeaponDamage(p));
                t.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 3, false, true, true));
                t.setTicksFrozen(t.getTicksFrozen() + 120);
                l.sendParticles((ParticleOptions) ParticleTypes.SNOWFLAKE, t.getX(), t.getY() + 1.0, t.getZ(), 8, 0.3, 0.3, 0.3, 0.05);
            }
            WeaponEffects.shockwave(l, ParticleTypes.SNOWFLAKE, p.getX(), p.getY() + 0.3, p.getZ(), 6.0, 2, 16, 2);
            l.sendParticles((ParticleOptions) ParticleTypes.ITEM_SNOWBALL, p.getX(), p.getY() + 1.0, p.getZ(), 20, 3.0, 1.0, 3.0, 0.1);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.8f, 1.5f);
            return true;
        }
        return false;
    }

    private static boolean executeWandElven(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Starfall".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 14.0);
            if (target == null) return false;
            Vec3 from = new Vec3(p.getX(), p.getY() + 1.5, p.getZ());
            Vec3 to = new Vec3(target.getX(), target.getY() + 1.0, target.getZ());
            WeaponEffects.line(l, ParticleTypes.ELECTRIC_SPARK, from, to, 16, 2, 0.08);
            target.hurt(l.damageSources().lightningBolt(), 10.0f + getWeaponDamage(p));
            l.sendParticles((ParticleOptions) ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1.0, target.getZ(), 15, 0.4, 0.5, 0.4, 0.12);
            l.sendParticles((ParticleOptions) ParticleTypes.FLASH, target.getX(), target.getY() + 1.0, target.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            l.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.6f, 1.8f);
            return true;
        }
        if ("Celestial Beam".equals(s.name())) {
            List<LivingEntity> targets = getTargetsInRange(p, l, 10.0);
            if (targets.isEmpty()) return false;
            targets.sort(Comparator.comparingDouble(e -> e.distanceToSqr((Entity) p)));
            Vec3 from = new Vec3(p.getX(), p.getY() + 1.5, p.getZ());
            int count = Math.min(5, targets.size());
            for (int i = 0; i < count; i++) {
                LivingEntity t = targets.get(i);
                Vec3 to = new Vec3(t.getX(), t.getY() + 1.0, t.getZ());
                WeaponEffects.line(l, ParticleTypes.END_ROD, from, to, 10, 1, 0.05);
                t.hurt(l.damageSources().magic(), 6.0f + getWeaponDamage(p));
                l.sendParticles((ParticleOptions) ParticleTypes.TOTEM_OF_UNDYING, t.getX(), t.getY() + 1.0, t.getZ(), 8, 0.3, 0.4, 0.3, 0.1);
            }
            WeaponEffects.spiral(l, ParticleTypes.END_ROD, p.getX(), p.getY(), p.getZ(), 1.0, 2.5, 16, 2);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.7f, 2.0f);
            return true;
        }
        return false;
    }

    // ========================
    // KATANAS
    // ========================

    private static boolean executeKatana1(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Quick Draw".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 6.0);
            if (target == null) return false;
            target.hurt(l.damageSources().playerAttack(p), 10.0f + getWeaponDamage(p));
            Vec3 look = p.getLookAngle().normalize();
            WeaponEffects.arc(l, ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1.0, target.getZ(),
                look.x, look.z, 1.5, Math.PI * 0.6, 8, 1);
            l.sendParticles((ParticleOptions) ParticleTypes.CRIT, target.getX(), target.getY() + 1.0, target.getZ(), 10, 0.3, 0.4, 0.3, 0.1);
            l.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0f, 1.5f);
            return true;
        }
        if ("Blade Flurry".equals(s.name())) {
            doConeAttack(p, l, 8.0f + getWeaponDamage(p), 5.0);
            Vec3 look = p.getLookAngle().normalize();
            for (int i = 1; i <= 4; i++) {
                Vec3 pt = p.position().add(look.scale(i));
                l.sendParticles((ParticleOptions) ParticleTypes.CLOUD, pt.x, pt.y + 1.0, pt.z, 4, 0.3 + i * 0.1, 0.2, 0.3 + i * 0.1, 0.02);
                l.sendParticles((ParticleOptions) ParticleTypes.SWEEP_ATTACK, pt.x, pt.y + 1.0, pt.z, 2, 0.2, 0.1, 0.2, 0.0);
            }
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 1.3f);
            return true;
        }
        return false;
    }

    private static boolean executeKatana2(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Shadow Step".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 10.0);
            if (target == null) return false;
            Vec3 origin = p.position();
            Vec3 behind = target.position().add(target.getLookAngle().scale(-1.5));
            l.sendParticles((ParticleOptions) ParticleTypes.LARGE_SMOKE, origin.x, origin.y + 1.0, origin.z, 10, 0.4, 0.6, 0.4, 0.02);
            p.teleportTo(behind.x, behind.y, behind.z);
            p.fallDistance = 0.0f;
            target.hurt(l.damageSources().playerAttack(p), 9.0f + getWeaponDamage(p));
            l.sendParticles((ParticleOptions) ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1.0, target.getZ(), 3, 0.3, 0.3, 0.3, 0.0);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5f, 1.5f);
            return true;
        }
        if ("Moonlit Slash".equals(s.name())) {
            List<LivingEntity> targets = getTargetsInRange(p, l, 5.0);
            if (targets.isEmpty()) return false;
            for (LivingEntity t : targets) {
                t.hurt(l.damageSources().playerAttack(p), 5.0f + getWeaponDamage(p));
                t.hurt(l.damageSources().playerAttack(p), 4.0f + getWeaponDamage(p));
                l.sendParticles((ParticleOptions) ParticleTypes.SWEEP_ATTACK, t.getX(), t.getY() + 1.0, t.getZ(), 3, 0.3, 0.3, 0.3, 0.0);
            }
            WeaponEffects.ring(l, ParticleTypes.SWEEP_ATTACK, p.getX(), p.getY() + 0.8, p.getZ(), 4.0, 12, 1, 0.05);
            WeaponEffects.ring(l, ParticleTypes.CLOUD, p.getX(), p.getY() + 1.2, p.getZ(), 3.5, 10, 1, 0.03);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 1.5f);
            return true;
        }
        return false;
    }

    private static boolean executeKatanaElven(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Twilight Slash".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 8.0);
            if (target == null) return false;
            Vec3 origin = p.position();
            Vec3 toTarget = target.position().subtract(origin).normalize();
            Vec3 dest = target.position().add(toTarget.scale(1.5));
            l.sendParticles((ParticleOptions) ParticleTypes.REVERSE_PORTAL, origin.x, origin.y + 1.0, origin.z, 8, 0.3, 0.5, 0.3, 0.05);
            p.teleportTo(dest.x, dest.y, dest.z);
            p.fallDistance = 0.0f;
            target.hurt(l.damageSources().playerAttack(p), 18.0f + getWeaponDamage(p));
            WeaponEffects.line(l, ParticleTypes.ENCHANTED_HIT, origin.add(0, 1, 0), dest.add(0, 1, 0), 14, 2, 0.05);
            l.sendParticles((ParticleOptions) ParticleTypes.FLASH, target.getX(), target.getY() + 1.0, target.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            l.sendParticles((ParticleOptions) ParticleTypes.CRIT, target.getX(), target.getY() + 1.0, target.getZ(), 15, 0.3, 0.4, 0.3, 0.1);
            l.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.2f, 0.8f);
            return true;
        }
        if ("Dusk Storm".equals(s.name())) {
            List<LivingEntity> targets = getTargetsInRange(p, l, 4.0);
            if (targets.isEmpty()) return false;
            for (LivingEntity t : targets) {
                for (int i = 0; i < 4; i++) {
                    t.hurt(l.damageSources().playerAttack(p), 3.0f + getWeaponDamage(p));
                }
                l.sendParticles((ParticleOptions) ParticleTypes.SWEEP_ATTACK, t.getX(), t.getY() + 1.0, t.getZ(), 6, 0.4, 0.4, 0.4, 0.0);
                l.sendParticles((ParticleOptions) ParticleTypes.CRIT, t.getX(), t.getY() + 1.0, t.getZ(), 10, 0.3, 0.5, 0.3, 0.1);
            }
            WeaponEffects.ring(l, ParticleTypes.SWEEP_ATTACK, p.getX(), p.getY() + 0.5, p.getZ(), 3.5, 14, 1, 0.05);
            WeaponEffects.ring(l, ParticleTypes.SWEEP_ATTACK, p.getX(), p.getY() + 1.5, p.getZ(), 3.0, 12, 1, 0.05);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.2f, 1.8f);
            return true;
        }
        return false;
    }

    // ========================
    // GREATSHIELDS
    // ========================

    private static boolean executeGreatshield1(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Fortress".equals(s.name())) {
            p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 120, 2, false, true, true));
            p.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 120, 1, false, true, true));
            WeaponEffects.ring(l, ParticleTypes.CRIT, p.getX(), p.getY() + 0.3, p.getZ(), 1.2, 12, 2, 0.03);
            WeaponEffects.ring(l, ParticleTypes.CRIT, p.getX(), p.getY() + 1.0, p.getZ(), 1.0, 10, 2, 0.03);
            WeaponEffects.ring(l, ParticleTypes.CRIT, p.getX(), p.getY() + 1.7, p.getZ(), 0.8, 8, 2, 0.03);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.6f, 0.8f);
            return true;
        }
        if ("Battering Ram".equals(s.name())) {
            Vec3 look = p.getLookAngle().normalize();
            p.setDeltaMovement(look.x * 1.5, 0.2, look.z * 1.5);
            p.hurtMarked = true;
            List<LivingEntity> targets = getTargetsInRange(p, l, 4.0);
            for (LivingEntity t : targets) {
                t.hurt(l.damageSources().playerAttack(p), 8.0f + getWeaponDamage(p));
                doStun(l, t, 30);
                double dx = t.getX() - p.getX();
                double dz = t.getZ() - p.getZ();
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > 0) t.knockback(1.5, -dx / dist, -dz / dist);
            }
            WeaponEffects.arc(l, ParticleTypes.CLOUD, p.getX(), p.getY() + 1.0, p.getZ(),
                look.x, look.z, 3.0, Math.PI * 0.4, 10, 2);
            l.sendParticles((ParticleOptions) ParticleTypes.EXPLOSION, p.getX() + look.x * 2, p.getY() + 1.0, p.getZ() + look.z * 2, 1, 0.0, 0.0, 0.0, 0.0);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.2f, 0.5f);
            return true;
        }
        return false;
    }

    private static boolean executeGreatshield2(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Holy Barrier".equals(s.name())) {
            p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 100, 1, false, true, true));
            p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1, false, true, true));
            AABB area = new AABB(p.blockPosition()).inflate(5.0);
            for (Player ally : p.level().getEntitiesOfClass(Player.class, area, e -> e != p && e.isAlive())) {
                ally.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 100, 0, false, true, true));
                WeaponEffects.ring(l, ParticleTypes.HAPPY_VILLAGER, ally.getX(), ally.getY() + 0.5, ally.getZ(), 0.8, 8, 1, 0.02);
            }
            WeaponEffects.ring(l, ParticleTypes.END_ROD, p.getX(), p.getY() + 0.2, p.getZ(), 5.0, 20, 2, 0.03);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.8f, 1.2f);
            return true;
        }
        if ("Radiant Repulse".equals(s.name())) {
            doGroundSlam(p, l, 10.0f + getWeaponDamage(p), 5.0);
            return true;
        }
        return false;
    }

    private static boolean executeGreatshieldElven(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Unbreakable".equals(s.name())) {
            p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 160, 2, false, true, true));
            p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 160, 2, false, true, true));
            p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 0, false, true, true));
            WeaponEffects.ring(l, ParticleTypes.END_ROD, p.getX(), p.getY() + 0.2, p.getZ(), 1.5, 14, 2, 0.02);
            WeaponEffects.ring(l, ParticleTypes.END_ROD, p.getX(), p.getY() + 1.0, p.getZ(), 1.2, 10, 2, 0.02);
            WeaponEffects.ring(l, ParticleTypes.END_ROD, p.getX(), p.getY() + 1.8, p.getZ(), 0.8, 8, 2, 0.02);
            l.sendParticles((ParticleOptions) ParticleTypes.ENCHANT, p.getX(), p.getY() + 0.5, p.getZ(), 15, 1.0, 1.0, 1.0, 0.3);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.8f, 1.5f);
            return true;
        }
        if ("Titan Slam".equals(s.name())) {
            List<LivingEntity> targets = getTargetsInRange(p, l, 6.0);
            for (LivingEntity t : targets) {
                t.hurt(l.damageSources().magic(), 12.0f + getWeaponDamage(p));
                WeaponEffects.column(l, ParticleTypes.END_ROD, t.getX(), t.getY(), t.getZ(), 5.0, 8, 2, 0.1);
            }
            WeaponEffects.shockwave(l, ParticleTypes.END_ROD, p.getX(), p.getY() + 0.2, p.getZ(), 6.0, 3, 20, 2);
            l.sendParticles((ParticleOptions) ParticleTypes.FLASH, p.getX(), p.getY() + 1.0, p.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.6f, 1.5f);
            return true;
        }
        return false;
    }

    // ========================
    // THROWING AXES
    // ========================

    private static boolean executeThrowingAxe1(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Thunder Throw".equals(s.name())) {
            List<LivingEntity> targets = getTargetsInRange(p, l, 10.0);
            if (targets.isEmpty()) return false;
            targets.sort(Comparator.comparingDouble(e -> e.distanceToSqr((Entity) p)));
            Vec3 prev = p.position().add(0, 1.2, 0);
            int count = Math.min(4, targets.size());
            for (int i = 0; i < count; i++) {
                LivingEntity t = targets.get(i);
                Vec3 to = t.position().add(0, 1, 0);
                WeaponEffects.line(l, ParticleTypes.CRIT, prev, to, 10, 1, 0.05);
                t.hurt(l.damageSources().playerAttack(p), 6.0f + getWeaponDamage(p));
                l.sendParticles((ParticleOptions) ParticleTypes.CRIT, t.getX(), t.getY() + 1.0, t.getZ(), 8, 0.2, 0.3, 0.2, 0.1);
                prev = to;
            }
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 0.8f, 1.2f);
            return true;
        }
        if ("Boomerang Arc".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 12.0);
            if (target == null) return false;
            Vec3 from = p.position().add(0, 1.2, 0);
            Vec3 to = target.position().add(0, 1, 0);
            WeaponEffects.line(l, ParticleTypes.FLAME, from, to, 14, 2, 0.08);
            target.hurt(l.damageSources().playerAttack(p), 14.0f + getWeaponDamage(p));
            target.setRemainingFireTicks(60);
            doExplosion(p, l, target.position(), 5.0f + getWeaponDamage(p), 2.5);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0f, 0.8f);
            return true;
        }
        return false;
    }

    private static boolean executeThrowingAxe2(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Frozen Hurl".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 10.0);
            if (target == null) return false;
            Vec3 from = p.position().add(0, 1.2, 0);
            Vec3 to = target.position().add(0, 1, 0);
            WeaponEffects.line(l, ParticleTypes.SNOWFLAKE, from, to, 12, 2, 0.06);
            target.hurt(l.damageSources().freeze(), 8.0f + getWeaponDamage(p));
            doFrostbite(p, l, target, 80, 2);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 0.8f, 1.3f);
            return true;
        }
        if ("Shatter Throw".equals(s.name())) {
            doWhirlwind(p, l, 7.0f + getWeaponDamage(p), 5.0);
            List<LivingEntity> targets = getTargetsInRange(p, l, 5.0);
            for (LivingEntity t : targets) {
                t.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1, false, true, true));
            }
            doFrostParticles(l, p.position(), 15);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 1.0f);
            return true;
        }
        return false;
    }

    private static boolean executeThrowingAxeElven(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Windborne Hatchet".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 12.0);
            if (target == null) return false;
            Vec3 from = p.position().add(0, 1.2, 0);
            Vec3 to = target.position().add(0, 1, 0);
            WeaponEffects.line(l, ParticleTypes.SOUL, from, to, 14, 2, 0.06);
            WeaponEffects.line(l, ParticleTypes.ENCHANTED_HIT, from, to, 8, 1, 0.04);
            target.hurt(l.damageSources().magic(), 12.0f + getWeaponDamage(p));
            doLifeLeech(p, l, target, 0.0f + getWeaponDamage(p), 0.3f);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 0.8f, 1.0f);
            return true;
        }
        if ("Whirlwind Toss".equals(s.name())) {
            List<LivingEntity> targets = getTargetsInRange(p, l, 10.0);
            if (targets.isEmpty()) return false;
            targets.sort(Comparator.comparingDouble(e -> e.distanceToSqr((Entity) p)));
            Vec3 from = p.position().add(0, 1.5, 0);
            int count = Math.min(6, targets.size());
            for (int i = 0; i < count; i++) {
                LivingEntity t = targets.get(i);
                Vec3 to = t.position().add(0, 1, 0);
                WeaponEffects.line(l, ParticleTypes.ENCHANTED_HIT, from, to, 10, 1, 0.05);
                t.hurt(l.damageSources().magic(), 5.0f + getWeaponDamage(p));
                l.sendParticles((ParticleOptions) ParticleTypes.ENCHANTED_HIT, t.getX(), t.getY() + 1.0, t.getZ(), 6, 0.3, 0.3, 0.3, 0.08);
            }
            WeaponEffects.ring(l, ParticleTypes.CLOUD, p.getX(), p.getY() + 1.5, p.getZ(), 1.5, 10, 2, 0.05);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0f, 1.5f);
            return true;
        }
        return false;
    }

    // ========================
    // RAPIERS
    // ========================

    private static boolean executeRapier1(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Riposte".equals(s.name())) {
            // Parry stance: gain resistance and counter the next attack
            p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 40, 3, false, true, true));
            RpgWeaponEvents.setActiveBuff(p, "riposte", l.getGameTime() + 40L);
            WeaponEffects.ring(l, ParticleTypes.ENCHANTED_HIT, p.getX(), p.getY() + 1.0, p.getZ(), 0.8, 8, 2, 0.03);
            l.sendParticles((ParticleOptions) ParticleTypes.ENCHANTED_HIT, p.getX() + p.getLookAngle().x * 0.6, p.getY() + 1.0, p.getZ() + p.getLookAngle().z * 0.6, 10, 0.2, 0.4, 0.2, 0.05);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.8f, 1.5f);
            return true;
        }
        if ("Flurry of Blows".equals(s.name())) {
            // 4 rapid precise strikes on nearest target
            LivingEntity target = findNearestMob(p, l, 5.0);
            if (target == null) return false;
            for (int i = 0; i < 4; i++) {
                target.hurt(l.damageSources().playerAttack(p), 3.0f + getWeaponDamage(p));
            }
            l.sendParticles((ParticleOptions) ParticleTypes.CRIT, target.getX(), target.getY() + 1.0, target.getZ(), 12, 0.3, 0.4, 0.3, 0.1);
            l.sendParticles((ParticleOptions) ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1.0, target.getZ(), 4, 0.3, 0.3, 0.3, 0.0);
            l.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 0.8f, 1.5f);
            return true;
        }
        return false;
    }

    private static boolean executeRapier2(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Envenom".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 6.0);
            if (target == null) return false;
            target.hurt(l.damageSources().playerAttack(p), 7.0f + getWeaponDamage(p));
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 2, false, true, true));
            l.sendParticles((ParticleOptions) ParticleTypes.ITEM_SLIME, target.getX(), target.getY() + 1.0, target.getZ(), 8, 0.3, 0.3, 0.3, 0.05);
            l.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 0.8f, 1.3f);
            return true;
        }
        if ("Lethal Lunge".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 5.0);
            if (target == null) return false;
            for (int i = 0; i < 5; i++) {
                target.hurt(l.damageSources().playerAttack(p), 3.0f + getWeaponDamage(p));
            }
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1, false, true, true));
            l.sendParticles((ParticleOptions) ParticleTypes.CRIT, target.getX(), target.getY() + 1.0, target.getZ(), 15, 0.3, 0.5, 0.3, 0.1);
            l.sendParticles((ParticleOptions) ParticleTypes.ITEM_SLIME, target.getX(), target.getY() + 0.5, target.getZ(), 10, 0.3, 0.5, 0.3, 0.05);
            l.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 1.8f);
            return true;
        }
        return false;
    }

    private static boolean executeRapierElven(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Perfect Parry".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 10.0);
            if (target == null) return false;
            Vec3 origin = p.position();
            Vec3 dest = target.position().add(target.getLookAngle().scale(-1.0));
            l.sendParticles((ParticleOptions) ParticleTypes.REVERSE_PORTAL, origin.x, origin.y + 1.0, origin.z, 10, 0.3, 0.5, 0.3, 0.05);
            WeaponEffects.line(l, ParticleTypes.ENCHANTED_HIT, origin.add(0, 1, 0), dest.add(0, 1, 0), 12, 2, 0.05);
            p.teleportTo(dest.x, dest.y, dest.z);
            p.fallDistance = 0.0f;
            target.hurt(l.damageSources().playerAttack(p), 14.0f + getWeaponDamage(p));
            l.sendParticles((ParticleOptions) ParticleTypes.FLASH, target.getX(), target.getY() + 1.0, target.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            l.sendParticles((ParticleOptions) ParticleTypes.CRIT, target.getX(), target.getY() + 1.0, target.getZ(), 12, 0.3, 0.4, 0.3, 0.1);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.6f, 1.5f);
            l.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0f, 1.0f);
            return true;
        }
        if ("En Passant".equals(s.name())) {
            List<LivingEntity> targets = getTargetsInRange(p, l, 6.0);
            if (targets.isEmpty()) return false;
            p.addEffect(new MobEffectInstance(MobEffects.SPEED, 60, 2, false, true, true));
            for (LivingEntity t : targets) {
                t.hurt(l.damageSources().playerAttack(p), 4.0f + getWeaponDamage(p));
                t.hurt(l.damageSources().playerAttack(p), 4.0f + getWeaponDamage(p));
                t.hurt(l.damageSources().playerAttack(p), 4.0f + getWeaponDamage(p));
                l.sendParticles((ParticleOptions) ParticleTypes.SWEEP_ATTACK, t.getX(), t.getY() + 1.0, t.getZ(), 4, 0.3, 0.3, 0.3, 0.0);
            }
            WeaponEffects.ring(l, ParticleTypes.ENCHANTED_HIT, p.getX(), p.getY() + 0.5, p.getZ(), 5.0, 16, 1, 0.05);
            WeaponEffects.ring(l, ParticleTypes.SWEEP_ATTACK, p.getX(), p.getY() + 1.0, p.getZ(), 4.5, 14, 1, 0.05);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.2f, 1.5f);
            return true;
        }
        return false;
    }

    // ========================
    // FILL-IN LONGSWORDS
    // ========================

    private static boolean executeLongsword1(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Sweeping Cut".equals(s.name())) {
            doConeAttack(p, l, 8.0f + getWeaponDamage(p), 4.0);
            Vec3 look = p.getLookAngle().normalize();
            for (int i = 1; i <= 4; i++) {
                Vec3 pt = p.position().add(look.scale(i));
                l.sendParticles((ParticleOptions) ParticleTypes.FLAME, pt.x, pt.y + 1.0, pt.z, 5 + i, 0.2 + i * 0.1, 0.2, 0.2 + i * 0.1, 0.03);
            }
            List<LivingEntity> targets = getConeTargets(p, l, 4.0);
            for (LivingEntity t : targets) {
                t.setRemainingFireTicks(60);
            }
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 0.9f);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.4f, 1.2f);
            return true;
        }
        if ("Valiant Charge".equals(s.name())) {
            doWhirlwind(p, l, 9.0f + getWeaponDamage(p), 4.5);
            List<LivingEntity> targets = getTargetsInRange(p, l, 4.5);
            for (LivingEntity t : targets) {
                t.setRemainingFireTicks(80);
            }
            doFlameParticles(l, p.position(), 20);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 0.7f);
            return true;
        }
        return false;
    }

    private static boolean executeLongsword2(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Shadow Slash".equals(s.name())) {
            // Shadow-infused slash that weakens the target
            doConeAttack(p, l, 7.0f + getWeaponDamage(p), 4.0);
            List<LivingEntity> targets = getConeTargets(p, l, 4.0);
            for (LivingEntity t : targets) {
                t.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 1, false, true, true));
            }
            Vec3 look = p.getLookAngle().normalize();
            for (int i = 1; i <= 4; i++) {
                Vec3 pt = p.position().add(look.scale(i));
                l.sendParticles((ParticleOptions) ParticleTypes.SMOKE, pt.x, pt.y + 1.0, pt.z, 5 + i, 0.2 + i * 0.1, 0.2, 0.2 + i * 0.1, 0.03);
                l.sendParticles((ParticleOptions) ParticleTypes.SOUL_FIRE_FLAME, pt.x, pt.y + 1.0, pt.z, 2, 0.15, 0.15, 0.15, 0.02);
            }
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 0.8f);
            return true;
        }
        if ("Dark Rend".equals(s.name())) {
            // Devastating shadow strike that bypasses armor (magic damage)
            List<LivingEntity> targets = getTargetsInRange(p, l, 4.5);
            for (LivingEntity t : targets) {
                t.hurt(l.damageSources().magic(), 8.0f + getWeaponDamage(p));
                t.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1, false, true, true));
            }
            l.sendParticles((ParticleOptions) ParticleTypes.SMOKE, p.getX(), p.getY() + 1.0, p.getZ(), 25, 3.0, 0.5, 3.0, 0.03);
            l.sendParticles((ParticleOptions) ParticleTypes.SOUL_FIRE_FLAME, p.getX(), p.getY() + 1.0, p.getZ(), 15, 2.5, 0.5, 2.5, 0.05);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.WITHER_HURT, SoundSource.PLAYERS, 0.5f, 1.5f);
            return true;
        }
        return false;
    }

    // ========================
    // FILL-IN VARIANTS (_3)
    // ========================

    private static boolean executeClaymore3(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Frostwind Slash".equals(s.name())) {
            doConeAttack(p, l, 8.0f + getWeaponDamage(p), 5.0);
            List<LivingEntity> targets = getConeTargets(p, l, 5.0);
            for (LivingEntity t : targets) {
                t.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2, false, true, true));
                t.setTicksFrozen(t.getTicksFrozen() + 80);
            }
            l.sendParticles((ParticleOptions) ParticleTypes.SNOWFLAKE, p.getX(), p.getY() + 1.0, p.getZ(), 25, 3.0, 0.5, 3.0, 0.05);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 0.6f);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.5f, 1.5f);
            return true;
        }
        if ("Avalanche".equals(s.name())) {
            doGroundSlam(p, l, 12.0f + getWeaponDamage(p), 5.0);
            List<LivingEntity> targets = getTargetsInRange(p, l, 5.0);
            for (LivingEntity t : targets) {
                t.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 3, false, true, true));
                t.setTicksFrozen(t.getTicksFrozen() + 140);
            }
            WeaponEffects.shockwave(l, ParticleTypes.SNOWFLAKE, p.getX(), p.getY() + 0.3, p.getZ(), 5.0, 3, 18, 2);
            l.sendParticles((ParticleOptions) ParticleTypes.ITEM_SNOWBALL, p.getX(), p.getY() + 0.5, p.getZ(), 25, 3.0, 0.5, 3.0, 0.1);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0f, 0.5f);
            return true;
        }
        return false;
    }

    private static boolean executeDagger3(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Nerve Strike".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 5.0);
            if (target == null) return false;
            target.hurt(l.damageSources().playerAttack(p), 7.0f + getWeaponDamage(p));
            doStun(l, target, 40);
            l.sendParticles((ParticleOptions) ParticleTypes.CRIT, target.getX(), target.getY() + 1.0, target.getZ(), 10, 0.3, 0.4, 0.3, 0.1);
            l.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 0.8f, 1.3f);
            return true;
        }
        if ("Fan of Knives".equals(s.name())) {
            doConeAttack(p, l, 6.0f + getWeaponDamage(p), 4.0);
            List<LivingEntity> targets = getConeTargets(p, l, 4.0);
            for (LivingEntity t : targets) {
                t.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1, false, true, true));
            }
            doPoisonCloud(p, l, p.position().add(p.getLookAngle().scale(2.0)), 3.0f + getWeaponDamage(p));
            return true;
        }
        return false;
    }

    private static boolean executeDoubleAxe3(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Flame Rend".equals(s.name())) {
            List<LivingEntity> targets = getTargetsInRange(p, l, 4.0);
            for (LivingEntity t : targets) {
                t.hurt(l.damageSources().playerAttack(p), 10.0f + getWeaponDamage(p));
                t.setRemainingFireTicks(80);
            }
            doFlameParticles(l, p.position().add(p.getLookAngle().scale(2.0)), 15);
            l.sendParticles((ParticleOptions) ParticleTypes.SWEEP_ATTACK, p.getX(), p.getY() + 1.0, p.getZ(), 8, 2.0, 0.5, 2.0, 0.0);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 0.7f);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.4f, 1.0f);
            return true;
        }
        if ("Pyroclastic Frenzy".equals(s.name())) {
            doWhirlwind(p, l, 8.0f + getWeaponDamage(p), 5.0);
            List<LivingEntity> targets = getTargetsInRange(p, l, 5.0);
            for (LivingEntity t : targets) {
                t.setRemainingFireTicks(80);
            }
            doExplosion(p, l, p.position(), 5.0f + getWeaponDamage(p), 4.0);
            return true;
        }
        return false;
    }

    private static boolean executeGlaive3(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Crescent Arc".equals(s.name())) {
            // Wide moonlit arc hitting all enemies in front
            doConeAttack(p, l, 8.0f + getWeaponDamage(p), 5.0);
            Vec3 look = p.getLookAngle().normalize();
            WeaponEffects.arc(l, ParticleTypes.END_ROD, p.getX(), p.getY() + 1.0, p.getZ(),
                look.x, look.z, 4.0, Math.PI * 0.6, 12, 2);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 1.2f);
            return true;
        }
        if ("Lunar Pierce".equals(s.name())) {
            // Piercing thrust infused with arcane moonlight
            doLinePierce(p, l, 9.0f + getWeaponDamage(p), 10, false);
            Vec3 look = p.getLookAngle().normalize();
            for (int i = 1; i <= 10; i++) {
                Vec3 pt = p.position().add(look.scale(i));
                l.sendParticles((ParticleOptions) ParticleTypes.END_ROD, pt.x, pt.y + 1.0, pt.z, 3, 0.15, 0.15, 0.15, 0.03);
            }
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 0.8f, 1.5f);
            return true;
        }
        return false;
    }

    private static boolean executeHammer3(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Permafrost Slam".equals(s.name())) {
            doGroundSlam(p, l, 10.0f + getWeaponDamage(p), 5.0);
            List<LivingEntity> targets = getTargetsInRange(p, l, 5.0);
            for (LivingEntity t : targets) {
                t.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2, false, true, true));
                t.setTicksFrozen(t.getTicksFrozen() + 100);
                l.sendParticles((ParticleOptions) ParticleTypes.SNOWFLAKE, t.getX(), t.getY() + 1.0, t.getZ(), 10, 0.3, 0.5, 0.3, 0.05);
            }
            WeaponEffects.shockwave(l, ParticleTypes.SNOWFLAKE, p.getX(), p.getY() + 0.3, p.getZ(), 5.0, 2, 16, 2);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.8f, 0.8f);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.5f, 1.0f);
            return true;
        }
        if ("Glacier Crush".equals(s.name())) {
            List<LivingEntity> targets = getTargetsInRange(p, l, 6.0);
            for (LivingEntity t : targets) {
                t.hurt(l.damageSources().freeze(), 12.0f + getWeaponDamage(p));
                t.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 3, false, true, true));
                t.setTicksFrozen(t.getTicksFrozen() + 160);
            }
            WeaponEffects.shockwave(l, ParticleTypes.SNOWFLAKE, p.getX(), p.getY() + 0.3, p.getZ(), 6.0, 4, 20, 2);
            l.sendParticles((ParticleOptions) ParticleTypes.ITEM_SNOWBALL, p.getX(), p.getY() + 0.5, p.getZ(), 25, 3.0, 0.5, 3.0, 0.1);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0f, 0.5f);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.6f, 0.8f);
            return true;
        }
        return false;
    }

    private static boolean executeMace3(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Holy Impact".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 5.0);
            if (target == null) return false;
            target.hurt(l.damageSources().magic(), 10.0f + getWeaponDamage(p));
            doRadiance(p, l, 3.0f, 3.0);
            WeaponEffects.column(l, ParticleTypes.END_ROD, target.getX(), target.getY(), target.getZ(), 4.0, 8, 2, 0.1);
            l.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.6f, 1.5f);
            return true;
        }
        if ("Cleansing Smite".equals(s.name())) {
            doGroundSlam(p, l, 8.0f + getWeaponDamage(p), 4.0);
            doRadiance(p, l, 4.0f, 4.0);
            l.sendParticles((ParticleOptions) ParticleTypes.END_ROD, p.getX(), p.getY() + 0.5, p.getZ(), 20, 2.5, 0.5, 2.5, 0.05);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.8f, 1.2f);
            return true;
        }
        return false;
    }

    private static boolean executeSickle3(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Frozen Reap".equals(s.name())) {
            doConeAttack(p, l, 6.0f + getWeaponDamage(p), 4.0);
            List<LivingEntity> targets = getConeTargets(p, l, 4.0);
            for (LivingEntity t : targets) {
                t.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 2, false, true, true));
                t.setTicksFrozen(t.getTicksFrozen() + 80);
            }
            doFrostParticles(l, p.position().add(p.getLookAngle().scale(2.0)), 12);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.8f, 1.2f);
            return true;
        }
        if ("Winter's Harvest".equals(s.name())) {
            List<LivingEntity> targets = getTargetsInRange(p, l, 5.0);
            float totalHeal = 0;
            for (LivingEntity t : targets) {
                t.hurt(l.damageSources().freeze(), 6.0f + getWeaponDamage(p));
                t.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2, false, true, true));
                totalHeal += 1.5f;
            }
            p.heal(totalHeal);
            WeaponEffects.shockwave(l, ParticleTypes.SNOWFLAKE, p.getX(), p.getY() + 0.2, p.getZ(), 5.0, 2, 16, 2);
            l.sendParticles((ParticleOptions) ParticleTypes.HEART, p.getX(), p.getY() + 1.5, p.getZ(), (int) totalHeal, 0.3, 0.3, 0.3, 0.0);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.6f, 1.0f);
            return true;
        }
        return false;
    }

    private static boolean executeSpear3(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Charged Thrust".equals(s.name())) {
            doLinePierce(p, l, 9.0f + getWeaponDamage(p), 10, false);
            // Lightning trail along the pierce line
            Vec3 look = p.getLookAngle().normalize();
            for (int i = 1; i <= 10; i++) {
                Vec3 pt = p.getEyePosition().add(look.scale(i));
                l.sendParticles((ParticleOptions) ParticleTypes.ELECTRIC_SPARK, pt.x, pt.y, pt.z, 4, 0.2, 0.2, 0.2, 0.08);
            }
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0f, 1.0f);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.4f, 1.5f);
            return true;
        }
        if ("Chain Impale".equals(s.name())) {
            // Chain lightning pierce through up to 3 targets
            float damage = 10.0f + getWeaponDamage(p);
            AABB area = new AABB(p.blockPosition()).inflate(10.0);
            List<LivingEntity> all = l.getEntitiesOfClass(LivingEntity.class, area, e -> e != p && e.isAlive());
            if (all.isEmpty()) return false;
            all.sort(Comparator.comparingDouble(e -> e.distanceToSqr((Entity) p)));
            Set<Integer> hitIds = new HashSet<>();
            Vec3 prev = p.position().add(0, 1.2, 0);
            LivingEntity current = all.getFirst();
            for (int i = 0; i < 3 && current != null; i++) {
                current.hurt(l.damageSources().lightningBolt(), damage);
                hitIds.add(current.getId());
                Vec3 to = current.position().add(0, 1, 0);
                WeaponEffects.line(l, ParticleTypes.ELECTRIC_SPARK, prev, to, 14, 2, 0.1);
                l.sendParticles((ParticleOptions) ParticleTypes.ELECTRIC_SPARK, current.getX(), current.getY() + 1.0, current.getZ(), 12, 0.3, 0.5, 0.3, 0.12);
                l.playSound(null, current.getX(), current.getY(), current.getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.5f, 1.5f + i * 0.2f);
                prev = to;
                LivingEntity chainFrom = current;
                List<LivingEntity> next = l.getEntitiesOfClass(LivingEntity.class, new AABB(chainFrom.blockPosition()).inflate(6.0), e -> e != p && e.isAlive() && !hitIds.contains(e.getId()));
                if (next.isEmpty()) break;
                next.sort(Comparator.comparingDouble(e -> e.distanceToSqr((Entity) chainFrom)));
                current = next.getFirst();
                damage *= 0.85f;
            }
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.5f, 1.8f);
            return true;
        }
        return false;
    }

    private static boolean executeLongbow3(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Wind Arrow".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 16.0);
            if (target == null) return false;
            Vec3 from = new Vec3(p.getX(), p.getY() + 1.5, p.getZ());
            Vec3 to = new Vec3(target.getX(), target.getY() + 1.0, target.getZ());
            WeaponEffects.line(l, ParticleTypes.CLOUD, from, to, 14, 2, 0.06);
            WeaponEffects.line(l, ParticleTypes.ENCHANTED_HIT, from, to, 8, 1, 0.04);
            target.hurt(l.damageSources().magic(), 8.0f + getWeaponDamage(p));
            // Wind knockback
            Vec3 push = to.subtract(from).normalize().scale(1.2);
            target.setDeltaMovement(target.getDeltaMovement().add(push.x, 0.3, push.z));
            target.hurtMarked = true;
            l.sendParticles((ParticleOptions) ParticleTypes.CLOUD, target.getX(), target.getY() + 1.0, target.getZ(), 12, 0.4, 0.4, 0.4, 0.08);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0f, 1.3f);
            return true;
        }
        if ("Tornado Shot".equals(s.name())) {
            doArrowBurst(p, l, 6, 6.0f + getWeaponDamage(p), 16.0);
            List<LivingEntity> targets = getTargetsInRange(p, l, 16.0);
            for (LivingEntity t : targets.subList(0, Math.min(6, targets.size()))) {
                // Wind pushback
                Vec3 push = t.position().subtract(p.position()).normalize().scale(0.8);
                t.setDeltaMovement(t.getDeltaMovement().add(push.x, 0.4, push.z));
                t.hurtMarked = true;
            }
            l.sendParticles((ParticleOptions) ParticleTypes.CLOUD, p.getX(), p.getY() + 3.0, p.getZ(), 40, 4.0, 1.5, 4.0, 0.08);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0f, 1.0f);
            return true;
        }
        return false;
    }

    private static boolean executeHeavyCrossbow3(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Mana Bolt".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 16.0);
            if (target == null) return false;
            Vec3 from = new Vec3(p.getX(), p.getY() + 1.5, p.getZ());
            Vec3 to = new Vec3(target.getX(), target.getY() + 1.0, target.getZ());
            WeaponEffects.line(l, ParticleTypes.ENCHANT, from, to, 14, 2, 0.06);
            WeaponEffects.line(l, ParticleTypes.END_ROD, from, to, 8, 1, 0.04);
            target.hurt(l.damageSources().magic(), 9.0f + getWeaponDamage(p));
            l.sendParticles((ParticleOptions) ParticleTypes.ENCHANTED_HIT, target.getX(), target.getY() + 1.0, target.getZ(), 15, 0.4, 0.5, 0.4, 0.1);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 0.8f, 1.2f);
            l.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.SHULKER_SHOOT, SoundSource.PLAYERS, 0.4f, 1.5f);
            return true;
        }
        if ("Arcane Barrage".equals(s.name())) {
            doArrowBurst(p, l, 5, 7.0f + getWeaponDamage(p), 16.0);
            List<LivingEntity> targets = getTargetsInRange(p, l, 16.0);
            Vec3 from = new Vec3(p.getX(), p.getY() + 1.5, p.getZ());
            for (LivingEntity t : targets.subList(0, Math.min(5, targets.size()))) {
                Vec3 to = new Vec3(t.getX(), t.getY() + 1.0, t.getZ());
                WeaponEffects.line(l, ParticleTypes.ENCHANT, from, to, 10, 1, 0.05);
                l.sendParticles((ParticleOptions) ParticleTypes.ENCHANTED_HIT, t.getX(), t.getY() + 1.0, t.getZ(), 8, 0.3, 0.3, 0.3, 0.1);
            }
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0f, 0.8f);
            return true;
        }
        return false;
    }

    private static boolean executeStaffDamage8(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Ball Lightning".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 14.0);
            if (target == null) return false;
            Vec3 from = new Vec3(p.getX(), p.getY() + 1.5, p.getZ());
            Vec3 to = new Vec3(target.getX(), target.getY() + 1.0, target.getZ());
            WeaponEffects.line(l, ParticleTypes.ELECTRIC_SPARK, from, to, 16, 2, 0.1);
            target.hurt(l.damageSources().lightningBolt(), 10.0f + getWeaponDamage(p));
            // Ball lightning impact burst
            l.sendParticles((ParticleOptions) ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1.0, target.getZ(), 20, 0.5, 0.6, 0.5, 0.15);
            l.sendParticles((ParticleOptions) ParticleTypes.FLASH, target.getX(), target.getY() + 1.0, target.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            l.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.6f, 1.5f);
            return true;
        }
        if ("Tempest".equals(s.name())) {
            Vec3 pos = p.position().add(p.getLookAngle().scale(5.0));
            AABB area = new AABB(pos.x - 4, pos.y - 1, pos.z - 4, pos.x + 4, pos.y + 3, pos.z + 4);
            List<LivingEntity> targets = l.getEntitiesOfClass(LivingEntity.class, area, e -> e != p && e.isAlive());
            for (LivingEntity t : targets) {
                t.hurt(l.damageSources().lightningBolt(), 10.0f + getWeaponDamage(p));
                l.sendParticles((ParticleOptions) ParticleTypes.ELECTRIC_SPARK, t.getX(), t.getY() + 1.0, t.getZ(), 12, 0.3, 0.5, 0.3, 0.12);
            }
            WeaponEffects.shockwave(l, ParticleTypes.ELECTRIC_SPARK, pos.x, pos.y + 0.3, pos.z, 4.0, 3, 20, 2);
            l.sendParticles((ParticleOptions) ParticleTypes.FLASH, pos.x, pos.y + 1.0, pos.z, 2, 0.0, 0.0, 0.0, 0.0);
            l.playSound(null, pos.x, pos.y, pos.z, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.6f, 1.2f);
            return true;
        }
        return false;
    }

    private static boolean executeStaffHeal3(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Nature's Embrace".equals(s.name())) {
            p.heal(8.0f);
            p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1, false, true, true));
            AABB area = new AABB(p.blockPosition()).inflate(5.0);
            for (Player ally : p.level().getEntitiesOfClass(Player.class, area, e -> e != p && e.isAlive())) {
                ally.heal(4.0f);
                ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0, false, true, true));
                WeaponEffects.spiral(l, ParticleTypes.COMPOSTER, ally.getX(), ally.getY(), ally.getZ(), 0.6, 2.0, 8, 1);
            }
            WeaponEffects.spiral(l, ParticleTypes.COMPOSTER, p.getX(), p.getY(), p.getZ(), 1.0, 2.5, 12, 2);
            WeaponEffects.ring(l, ParticleTypes.HAPPY_VILLAGER, p.getX(), p.getY() + 0.3, p.getZ(), 5.0, 16, 2, 0.03);
            l.sendParticles((ParticleOptions) ParticleTypes.HEART, p.getX(), p.getY() + 2.0, p.getZ(), 5, 0.5, 0.5, 0.5, 0.0);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.7f, 1.8f);
            return true;
        }
        if ("Verdant Bloom".equals(s.name())) {
            p.heal(p.getMaxHealth() * 0.4f);
            p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1, false, true, true));
            p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 1, false, true, true));
            AABB area = new AABB(p.blockPosition()).inflate(6.0);
            for (Player ally : p.level().getEntitiesOfClass(Player.class, area, e -> e != p && e.isAlive())) {
                ally.heal(ally.getMaxHealth() * 0.25f);
                ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0, false, true, true));
            }
            l.sendParticles((ParticleOptions) ParticleTypes.COMPOSTER, p.getX(), p.getY() + 1.0, p.getZ(), 40, 3.0, 2.0, 3.0, 0.05);
            l.sendParticles((ParticleOptions) ParticleTypes.HAPPY_VILLAGER, p.getX(), p.getY() + 1.0, p.getZ(), 20, 3.0, 1.0, 3.0, 0.0);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f, 1.5f);
            return true;
        }
        return false;
    }

    private static boolean executeShield3(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Fear Aura".equals(s.name())) {
            p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 80, 1, false, true, true));
            p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 80, 0, false, true, true));
            RpgWeaponEvents.setActiveBuff(p, "flame_reflect", l.getGameTime() + 80L);
            WeaponEffects.ring(l, ParticleTypes.FLAME, p.getX(), p.getY() + 0.3, p.getZ(), 1.2, 10, 2, 0.03);
            WeaponEffects.ring(l, ParticleTypes.FLAME, p.getX(), p.getY() + 1.0, p.getZ(), 1.0, 8, 2, 0.03);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0f, 0.8f);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.4f, 1.0f);
            return true;
        }
        if ("Dread Slam".equals(s.name())) {
            List<LivingEntity> targets = getTargetsInRange(p, l, 4.0);
            for (LivingEntity t : targets) {
                t.hurt(l.damageSources().playerAttack(p), 7.0f + getWeaponDamage(p));
                t.setRemainingFireTicks(80);
                double dx = t.getX() - p.getX();
                double dz = t.getZ() - p.getZ();
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > 0) t.knockback(1.5, -dx / dist, -dz / dist);
            }
            Vec3 look = p.getLookAngle().normalize();
            WeaponEffects.arc(l, ParticleTypes.FLAME, p.getX(), p.getY() + 1.0, p.getZ(),
                look.x, look.z, 3.0, Math.PI * 0.5, 12, 2);
            doFlameParticles(l, p.position().add(look.scale(2.0)), 12);
            l.sendParticles((ParticleOptions) ParticleTypes.EXPLOSION, p.getX() + look.x * 2, p.getY() + 1.0, p.getZ() + look.z * 2, 1, 0.0, 0.0, 0.0, 0.0);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0f, 0.6f);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.4f, 1.2f);
            return true;
        }
        return false;
    }

    // ========================
    // FILL-IN _3 VARIANTS (NEW TYPES)
    // ========================

    private static boolean executeLongsword3(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Crescent Strike".equals(s.name())) {
            doConeAttack(p, l, 8.0f + getWeaponDamage(p), 4.0);
            Vec3 look = p.getLookAngle().normalize();
            for (int i = 1; i <= 4; i++) {
                Vec3 pt = p.position().add(look.scale(i));
                l.sendParticles((ParticleOptions) ParticleTypes.SWEEP_ATTACK, pt.x, pt.y + 1.0, pt.z, 3 + i, 0.2 + i * 0.1, 0.2, 0.2 + i * 0.1, 0.03);
            }
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 1.0f);
            return true;
        }
        if ("Blade Storm".equals(s.name())) {
            doWhirlwind(p, l, 9.0f + getWeaponDamage(p), 4.5);
            l.sendParticles((ParticleOptions) ParticleTypes.SWEEP_ATTACK, p.getX(), p.getY() + 1.0, p.getZ(), 20, 3.0, 0.5, 3.0, 0.05);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 0.8f);
            return true;
        }
        return false;
    }

    private static boolean executeWhip3(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Lightning Crack".equals(s.name())) {
            Vec3 look = p.getLookAngle().normalize();
            Vec3 start = p.position().add(0, 1.0, 0);
            Vec3 end = start.add(look.scale(8.0));
            WeaponEffects.line(l, ParticleTypes.ELECTRIC_SPARK, start, end, 16, 2, 0.1);
            AABB area = new AABB(Math.min(start.x, end.x) - 1, start.y - 1, Math.min(start.z, end.z) - 1, Math.max(start.x, end.x) + 1, start.y + 2, Math.max(start.z, end.z) + 1);
            for (LivingEntity t : l.getEntitiesOfClass(LivingEntity.class, area, e -> e != p && e.isAlive())) {
                t.hurt(l.damageSources().lightningBolt(), 7.0f + getWeaponDamage(p));
            }
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.6f, 1.5f);
            return true;
        }
        if ("Tempest Coil".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 10.0);
            if (target == null) return false;
            Vec3 from = p.position().add(0, 1, 0);
            Vec3 to = target.position().add(0, 1, 0);
            WeaponEffects.line(l, ParticleTypes.ELECTRIC_SPARK, from, to, 12, 2, 0.08);
            target.hurt(l.damageSources().lightningBolt(), 10.0f + getWeaponDamage(p));
            Vec3 pull = p.position().subtract(target.position()).normalize().scale(1.0);
            target.setDeltaMovement(target.getDeltaMovement().add(pull.x, 0.2, pull.z));
            target.hurtMarked = true;
            l.sendParticles((ParticleOptions) ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1.0, target.getZ(), 12, 0.4, 0.5, 0.4, 0.1);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.4f, 1.8f);
            return true;
        }
        return false;
    }

    private static boolean executeWand3(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Shadow Bolt".equals(s.name())) {
            LivingEntity target = findNearestMob(p, l, 12.0);
            if (target == null) return false;
            Vec3 from = new Vec3(p.getX(), p.getY() + 1.5, p.getZ());
            Vec3 to = new Vec3(target.getX(), target.getY() + 1.0, target.getZ());
            WeaponEffects.line(l, ParticleTypes.SMOKE, from, to, 14, 2, 0.06);
            WeaponEffects.line(l, ParticleTypes.SOUL_FIRE_FLAME, from, to, 6, 1, 0.04);
            target.hurt(l.damageSources().magic(), 8.0f + getWeaponDamage(p));
            l.sendParticles((ParticleOptions) ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + 1.0, target.getZ(), 12, 0.3, 0.4, 0.3, 0.1);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.SHULKER_SHOOT, SoundSource.PLAYERS, 0.7f, 0.8f);
            return true;
        }
        if ("Void Rift".equals(s.name())) {
            List<LivingEntity> targets = getTargetsInRange(p, l, 8.0);
            if (targets.isEmpty()) return false;
            for (LivingEntity t : targets) {
                t.hurt(l.damageSources().magic(), 5.0f + getWeaponDamage(p));
                t.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 1, false, true, true));
                l.sendParticles((ParticleOptions) ParticleTypes.REVERSE_PORTAL, t.getX(), t.getY() + 1.0, t.getZ(), 8, 0.3, 0.3, 0.3, 0.05);
            }
            WeaponEffects.shockwave(l, ParticleTypes.REVERSE_PORTAL, p.getX(), p.getY() + 0.3, p.getZ(), 8.0, 2, 16, 2);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5f, 0.8f);
            return true;
        }
        return false;
    }

    private static boolean executeKatana3(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Crescent Cut".equals(s.name())) {
            // Swift crescent slash with critical damage (higher than Quick Draw)
            LivingEntity target = findNearestMob(p, l, 6.0);
            if (target == null) return false;
            target.hurt(l.damageSources().playerAttack(p), 14.0f + getWeaponDamage(p));
            Vec3 look = p.getLookAngle().normalize();
            WeaponEffects.arc(l, ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1.0, target.getZ(),
                look.x, look.z, 2.0, Math.PI * 0.8, 12, 2);
            l.sendParticles((ParticleOptions) ParticleTypes.ENCHANTED_HIT, target.getX(), target.getY() + 1.0, target.getZ(), 12, 0.3, 0.4, 0.3, 0.1);
            l.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0f, 1.8f);
            return true;
        }
        if ("Phantom Dance".equals(s.name())) {
            // Dash through enemies 3 times dealing shadow damage
            LivingEntity target = findNearestMob(p, l, 8.0);
            if (target == null) return false;
            Vec3 origin = p.position();
            float dmg = 5.0f + getWeaponDamage(p);
            for (int i = 0; i < 3; i++) {
                Vec3 toTarget = target.position().subtract(p.position()).normalize();
                Vec3 dest = target.position().add(toTarget.scale(1.5));
                l.sendParticles((ParticleOptions) ParticleTypes.SMOKE, p.getX(), p.getY() + 1.0, p.getZ(), 8, 0.3, 0.5, 0.3, 0.03);
                p.teleportTo(dest.x, dest.y, dest.z);
                target.hurt(l.damageSources().playerAttack(p), dmg);
                l.sendParticles((ParticleOptions) ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1.0, target.getZ(), 2, 0.2, 0.2, 0.2, 0.0);
                dmg += 2.0f;
            }
            p.fallDistance = 0.0f;
            l.sendParticles((ParticleOptions) ParticleTypes.LARGE_SMOKE, p.getX(), p.getY() + 1.0, p.getZ(), 10, 0.4, 0.5, 0.4, 0.02);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5f, 1.8f);
            l.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 1.5f);
            return true;
        }
        return false;
    }

    private static boolean executeGreatshield3(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Earthen Bulwark".equals(s.name())) {
            // Plant shield creating a stone barrier with absorption
            p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 140, 2, false, true, true));
            p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 140, 1, false, true, true));
            // Earth barrier particles (composter = earth/stone)
            WeaponEffects.ring(l, ParticleTypes.COMPOSTER, p.getX(), p.getY() + 0.3, p.getZ(), 1.2, 14, 2, 0.03);
            WeaponEffects.ring(l, ParticleTypes.COMPOSTER, p.getX(), p.getY() + 1.0, p.getZ(), 1.0, 10, 2, 0.03);
            WeaponEffects.ring(l, ParticleTypes.COMPOSTER, p.getX(), p.getY() + 1.7, p.getZ(), 0.8, 8, 2, 0.03);
            l.sendParticles((ParticleOptions) ParticleTypes.CAMPFIRE_COSY_SMOKE, p.getX(), p.getY() + 0.5, p.getZ(), 8, 0.5, 0.3, 0.5, 0.01);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.STONE_BREAK, SoundSource.PLAYERS, 0.8f, 0.6f);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.4f, 0.8f);
            return true;
        }
        if ("Tremor Bash".equals(s.name())) {
            // Earthquake slam - ground slam with wider stagger
            doGroundSlam(p, l, 10.0f + getWeaponDamage(p), 5.0);
            List<LivingEntity> targets = getTargetsInRange(p, l, 5.0);
            for (LivingEntity t : targets) {
                doStun(l, t, 40);
            }
            l.sendParticles((ParticleOptions) ParticleTypes.COMPOSTER, p.getX(), p.getY() + 0.5, p.getZ(), 25, 3.0, 0.3, 3.0, 0.05);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.6f, 0.8f);
            return true;
        }
        return false;
    }

    private static boolean executeThrowingAxe3(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Ricochet Throw".equals(s.name())) {
            // Ricochets between 3 enemies with INCREASING damage per bounce
            List<LivingEntity> targets = getTargetsInRange(p, l, 10.0);
            if (targets.isEmpty()) return false;
            targets.sort(Comparator.comparingDouble(e -> e.distanceToSqr((Entity) p)));
            Vec3 prev = p.position().add(0, 1.2, 0);
            int count = Math.min(3, targets.size());
            float damage = 5.0f + getWeaponDamage(p);
            for (int i = 0; i < count; i++) {
                LivingEntity t = targets.get(i);
                Vec3 to = t.position().add(0, 1, 0);
                WeaponEffects.line(l, ParticleTypes.ENCHANTED_HIT, prev, to, 10, 1, 0.05);
                t.hurt(l.damageSources().playerAttack(p), damage);
                l.sendParticles((ParticleOptions) ParticleTypes.ENCHANTED_HIT, t.getX(), t.getY() + 1.0, t.getZ(), 8, 0.2, 0.3, 0.2, 0.1);
                prev = to;
                damage *= 1.3f; // Each ricochet deals 30% more damage
            }
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 0.8f, 1.4f);
            return true;
        }
        if ("Axe Storm".equals(s.name())) {
            // Throw multiple axes in a spiral hitting all nearby
            List<LivingEntity> targets = getTargetsInRange(p, l, 8.0);
            if (targets.isEmpty()) return false;
            Vec3 from = p.position().add(0, 1.2, 0);
            for (LivingEntity t : targets) {
                Vec3 to = t.position().add(0, 1, 0);
                WeaponEffects.line(l, ParticleTypes.CRIT, from, to, 10, 1, 0.05);
                t.hurt(l.damageSources().playerAttack(p), 7.0f + getWeaponDamage(p));
                l.sendParticles((ParticleOptions) ParticleTypes.SWEEP_ATTACK, t.getX(), t.getY() + 1.0, t.getZ(), 3, 0.2, 0.3, 0.2, 0.0);
            }
            WeaponEffects.ring(l, ParticleTypes.CRIT, p.getX(), p.getY() + 1.0, p.getZ(), 6.0, 16, 1, 0.08);
            l.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0f, 0.8f);
            return true;
        }
        return false;
    }

    private static boolean executeRapier3(ServerPlayer p, RpgWeaponItem.WeaponSkill s, ServerLevel l) {
        if ("Feint Strike".equals(s.name())) {
            // Deceptive strike that bypasses armor (magic damage)
            LivingEntity target = findNearestMob(p, l, 5.0);
            if (target == null) return false;
            target.hurt(l.damageSources().magic(), 12.0f + getWeaponDamage(p));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 1, false, true, true));
            l.sendParticles((ParticleOptions) ParticleTypes.SMOKE, target.getX(), target.getY() + 1.0, target.getZ(), 10, 0.3, 0.4, 0.3, 0.05);
            l.sendParticles((ParticleOptions) ParticleTypes.CRIT, target.getX(), target.getY() + 1.0, target.getZ(), 8, 0.2, 0.3, 0.2, 0.1);
            l.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 0.8f, 1.8f);
            return true;
        }
        if ("Blade Dance".equals(s.name())) {
            // 5-hit escalating damage combo
            LivingEntity target = findNearestMob(p, l, 5.0);
            if (target == null) return false;
            float baseDmg = 2.0f + getWeaponDamage(p);
            for (int i = 0; i < 5; i++) {
                target.hurt(l.damageSources().playerAttack(p), baseDmg + i * 1.5f);
            }
            l.sendParticles((ParticleOptions) ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1.0, target.getZ(), 8, 0.3, 0.4, 0.3, 0.0);
            l.sendParticles((ParticleOptions) ParticleTypes.CRIT, target.getX(), target.getY() + 1.0, target.getZ(), 15, 0.3, 0.5, 0.3, 0.1);
            p.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 1, false, true, true));
            l.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 1.8f);
            return true;
        }
        return false;
    }

    // ========================
    // SHARED UTILITY METHODS
    // ========================

    private static LivingEntity findNearestMob(ServerPlayer player, ServerLevel level, double range) {
        AABB area = new AABB(player.blockPosition()).inflate(range);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive() && (double) e.distanceTo(player) <= range);
        if (targets.isEmpty()) return null;
        targets.sort(Comparator.comparingDouble(e -> e.distanceToSqr((Entity) player)));
        return targets.getFirst();
    }

    private static List<LivingEntity> getTargetsInRange(ServerPlayer player, ServerLevel level, double range) {
        AABB area = new AABB(player.blockPosition()).inflate(range);
        return level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive() && (double) e.distanceTo(player) <= range);
    }

    private static List<LivingEntity> getConeTargets(ServerPlayer player, ServerLevel level, double range) {
        Vec3 look = player.getLookAngle().normalize();
        AABB area = new AABB(player.blockPosition()).inflate(range);
        return level.getEntitiesOfClass(LivingEntity.class, area, e -> {
            if (e == player || !e.isAlive() || e.distanceTo(player) > range) return false;
            Vec3 toTarget = e.position().subtract(player.position()).normalize();
            return look.x * toTarget.x + look.z * toTarget.z > 0;
        });
    }

    private static void doExplosion(ServerPlayer player, ServerLevel level, Vec3 center, float damage, double radius) {
        AABB area = new AABB(center.x - radius, center.y - radius, center.z - radius, center.x + radius, center.y + radius, center.z + radius);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive());
        for (LivingEntity t : targets) {
            double dist = t.position().distanceTo(center);
            if (dist <= radius) {
                float scaled = damage * (float) (1.0 - dist / radius);
                t.hurt(level.damageSources().magic(), scaled);
                t.setRemainingFireTicks(60);
            }
        }
        // Core explosion
        level.sendParticles((ParticleOptions) ParticleTypes.EXPLOSION_EMITTER, center.x, center.y + 0.5, center.z, 1, 0.0, 0.0, 0.0, 0.0);
        level.sendParticles((ParticleOptions) ParticleTypes.EXPLOSION, center.x, center.y + 1.0, center.z, 3, radius * 0.3, 0.5, radius * 0.3, 0.0);
        // Fire shockwave ring
        WeaponEffects.shockwave(level, ParticleTypes.FLAME, center.x, center.y + 0.3, center.z, radius, 2, 16, 2);
        // Ember debris
        level.sendParticles((ParticleOptions) ParticleTypes.LAVA, center.x, center.y + 0.5, center.z, 10, radius * 0.4, 0.3, radius * 0.4, 0.0);
        level.sendParticles((ParticleOptions) ParticleTypes.FLAME, center.x, center.y + 0.5, center.z, 20, radius * 0.5, 0.5, radius * 0.5, 0.05);
        // Smoke plume
        level.sendParticles((ParticleOptions) ParticleTypes.LARGE_SMOKE, center.x, center.y + 1.0, center.z, 10, radius * 0.3, 1.0, radius * 0.3, 0.02);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.7f, 1.0f);
    }

    private static void doRadiance(ServerPlayer player, ServerLevel level, float healAmount, double radius) {
        player.heal(healAmount);
        AABB area = new AABB(player.blockPosition()).inflate(radius);
        for (Player ally : player.level().getEntitiesOfClass(Player.class, area, e -> e != player && e.isAlive())) {
            ally.heal(healAmount * 0.5f);
            // Heal particles on each ally
            level.sendParticles((ParticleOptions) ParticleTypes.HEART, ally.getX(), ally.getY() + 1.5, ally.getZ(), 3, 0.3, 0.3, 0.3, 0.0);
        }
        // Expanding healing wave ring
        WeaponEffects.shockwave(level, ParticleTypes.END_ROD, player.getX(), player.getY() + 0.5, player.getZ(), radius, 2, 14, 2);
        // Central light column
        WeaponEffects.column(level, ParticleTypes.END_ROD, player.getX(), player.getY(), player.getZ(), 3.0, 6, 2, 0.1);
        // Hearts
        level.sendParticles((ParticleOptions) ParticleTypes.HEART, player.getX(), player.getY() + 2.0, player.getZ(), 5, 0.5, 0.3, 0.5, 0.0);
        // Enchant shimmer
        level.sendParticles((ParticleOptions) ParticleTypes.ENCHANT, player.getX(), player.getY() + 0.5, player.getZ(), 12, 1.0, 0.8, 1.0, 0.5);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.5f, 2.0f);
    }

    private static void doLifeLeech(ServerPlayer player, ServerLevel level, LivingEntity target, float damage, float healPercent) {
        target.hurt(level.damageSources().magic(), damage);
        player.heal(damage * healPercent);
        // Drain beam from target to player
        Vec3 tPos = new Vec3(target.getX(), target.getY() + 1.0, target.getZ());
        Vec3 pPos = new Vec3(player.getX(), player.getY() + 1.0, player.getZ());
        WeaponEffects.line(level, ParticleTypes.DAMAGE_INDICATOR, tPos, pPos, 10, 1, 0.05);
        WeaponEffects.line(level, ParticleTypes.SOUL_FIRE_FLAME, tPos, pPos, 6, 1, 0.08);
        // Target damage burst
        level.sendParticles((ParticleOptions) ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getY() + 1.0, target.getZ(), 8, 0.3, 0.5, 0.3, 0.02);
        level.sendParticles((ParticleOptions) ParticleTypes.CRIMSON_SPORE, target.getX(), target.getY() + 0.5, target.getZ(), 10, 0.4, 0.6, 0.4, 0.02);
        // Heal on player
        level.sendParticles((ParticleOptions) ParticleTypes.HEART, player.getX(), player.getY() + 1.5, player.getZ(), 2, 0.2, 0.2, 0.2, 0.0);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_HURT, SoundSource.PLAYERS, 0.4f, 1.5f);
    }

    private static void doFrostbite(ServerPlayer player, ServerLevel level, LivingEntity target, int duration, int amplifier) {
        target.hurt(level.damageSources().freeze(), 3.0f);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, duration, amplifier, false, true, true));
        target.setTicksFrozen(target.getTicksFrozen() + duration);
        // Frost convergence on target
        WeaponEffects.converge(level, ParticleTypes.SNOWFLAKE, target.getX(), target.getY() + 1.0, target.getZ(), 1.5, 8, 2);
        // Ice shatter
        level.sendParticles((ParticleOptions) ParticleTypes.ITEM_SNOWBALL, target.getX(), target.getY() + 0.5, target.getZ(), 8, 0.3, 0.5, 0.3, 0.1);
        level.sendParticles((ParticleOptions) ParticleTypes.SNOWFLAKE, target.getX(), target.getY() + 1.0, target.getZ(), 12, 0.3, 0.5, 0.3, 0.05);
        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.5f, 2.0f);
    }

    private static void doWitherStrike(ServerPlayer player, ServerLevel level, LivingEntity target, int duration, int amplifier) {
        target.hurt(level.damageSources().magic(), 5.0f);
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, duration, amplifier, false, true, true));
        // Dark wither spiral on target
        WeaponEffects.spiral(level, ParticleTypes.SMOKE, target.getX(), target.getY(), target.getZ(), 0.8, 2.0, 12, 2);
        level.sendParticles((ParticleOptions) ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + 1.0, target.getZ(), 10, 0.3, 0.5, 0.3, 0.05);
        level.sendParticles((ParticleOptions) ParticleTypes.SMOKE, target.getX(), target.getY() + 0.5, target.getZ(), 12, 0.4, 0.6, 0.4, 0.03);
        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.WITHER_HURT, SoundSource.PLAYERS, 0.5f, 1.0f);
    }

    private static void doArmorSunder(ServerPlayer player, ServerLevel level, LivingEntity target, int duration) {
        target.hurt(level.damageSources().playerAttack(player), 4.0f);
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 1, false, true, true));
        // Armor crack particles
        level.sendParticles((ParticleOptions) ParticleTypes.CRIT, target.getX(), target.getY() + 1.0, target.getZ(), 12, 0.3, 0.5, 0.3, 0.1);
        level.sendParticles((ParticleOptions) ParticleTypes.SMOKE, target.getX(), target.getY() + 0.5, target.getZ(), 8, 0.3, 0.5, 0.3, 0.03);
        // Sunder ring at target
        WeaponEffects.ring(level, ParticleTypes.ENCHANTED_HIT, target.getX(), target.getY() + 1.0, target.getZ(), 0.8, 8, 2, 0.03);
        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.3f, 2.0f);
    }

    private static void doStun(ServerLevel level, LivingEntity target, int ticks) {
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, ticks, 127, false, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, ticks, 127, false, true, true));
        // Stun stars circling above head
        WeaponEffects.ring(level, ParticleTypes.ENCHANTED_HIT, target.getX(), target.getY() + 2.0, target.getZ(), 0.5, 8, 2, 0.02);
        // Impact flash
        level.sendParticles((ParticleOptions) ParticleTypes.FLASH, target.getX(), target.getY() + 1.5, target.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
        level.sendParticles((ParticleOptions) ParticleTypes.ENCHANTED_HIT, target.getX(), target.getY() + 1.5, target.getZ(), 10, 0.3, 0.3, 0.3, 0.05);
    }

    private static void doWhirlwind(ServerPlayer player, ServerLevel level, float damage, double radius) {
        AABB area = new AABB(player.blockPosition()).inflate(radius);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive() && e.distanceTo(player) <= radius);
        for (LivingEntity t : targets) {
            t.hurt(level.damageSources().playerAttack(player), damage);
        }
        // Spinning sweep rings
        WeaponEffects.ring(level, ParticleTypes.SWEEP_ATTACK, player.getX(), player.getY() + 0.5, player.getZ(), radius * 0.7, 10, 1, 0.05);
        WeaponEffects.ring(level, ParticleTypes.SWEEP_ATTACK, player.getX(), player.getY() + 1.0, player.getZ(), radius * 0.8, 12, 1, 0.05);
        // Wind gust
        WeaponEffects.ring(level, ParticleTypes.CLOUD, player.getX(), player.getY() + 0.3, player.getZ(), radius, 10, 1, 0.1);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 0.8f);
    }

    private static void doConeAttack(ServerPlayer player, ServerLevel level, float damage, double range) {
        Vec3 look = player.getLookAngle().normalize();
        AABB area = new AABB(player.blockPosition()).inflate(range);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area, e -> {
            if (e == player || !e.isAlive() || e.distanceTo(player) > range) return false;
            Vec3 toTarget = e.position().subtract(player.position()).normalize();
            return look.x * toTarget.x + look.z * toTarget.z > 0;
        });
        for (LivingEntity t : targets) {
            t.hurt(level.damageSources().playerAttack(player), damage);
        }
        // Arc sweep in front
        WeaponEffects.arc(level, ParticleTypes.SWEEP_ATTACK, player.getX(), player.getY() + 1.0, player.getZ(),
            look.x, look.z, range * 0.7, Math.PI * 0.6, 12, 1);
        WeaponEffects.arc(level, ParticleTypes.CRIT, player.getX(), player.getY() + 1.0, player.getZ(),
            look.x, look.z, range * 0.5, Math.PI * 0.4, 8, 1);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    private static void doLinePierce(ServerPlayer player, ServerLevel level, float damage, int range, boolean fire) {
        Vec3 look = player.getLookAngle().normalize();
        Vec3 start = player.getEyePosition();
        Set<Integer> hitIds = new HashSet<>();
        // Muzzle burst
        Vec3 muzzle = start.add(look.scale(1.0));
        ParticleOptions mainParticle = fire ? (ParticleOptions) ParticleTypes.FLAME : (ParticleOptions) ParticleTypes.CRIT;
        level.sendParticles(mainParticle, muzzle.x, muzzle.y, muzzle.z, 8, 0.15, 0.15, 0.15, 0.05);
        for (int i = 1; i <= range; i++) {
            Vec3 pt = start.add(look.scale(i));
            AABB check = new AABB(pt.x - 0.6, pt.y - 0.6, pt.z - 0.6, pt.x + 0.6, pt.y + 0.6, pt.z + 0.6);
            List<LivingEntity> hit = level.getEntitiesOfClass(LivingEntity.class, check, e -> e != player && e.isAlive() && !hitIds.contains(e.getId()));
            for (LivingEntity t : hit) {
                t.hurt(level.damageSources().playerAttack(player), damage);
                if (fire) t.setRemainingFireTicks(60);
                hitIds.add(t.getId());
                // Impact burst on each hit target
                level.sendParticles(mainParticle, t.getX(), t.getY() + 1.0, t.getZ(), 8, 0.3, 0.3, 0.3, 0.08);
            }
            // Dense projectile trail
            level.sendParticles(mainParticle, pt.x, pt.y, pt.z, 4, 0.06, 0.06, 0.06, 0.01);
            // Secondary trail for depth
            if (fire) {
                level.sendParticles((ParticleOptions) ParticleTypes.SMOKE, pt.x, pt.y, pt.z, 1, 0.1, 0.1, 0.1, 0.01);
            } else {
                level.sendParticles((ParticleOptions) ParticleTypes.ENCHANTED_HIT, pt.x, pt.y, pt.z, 2, 0.08, 0.08, 0.08, 0.01);
            }
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    private static void doShockwave(ServerPlayer player, ServerLevel level, float damage, int range) {
        Vec3 look = player.getLookAngle().normalize();
        Vec3 start = player.position();
        Set<Integer> hitIds = new HashSet<>();
        for (int i = 1; i <= range; i++) {
            Vec3 pt = start.add(look.scale(i));
            AABB check = new AABB(pt.x - 1.5, pt.y - 0.5, pt.z - 1.5, pt.x + 1.5, pt.y + 1.5, pt.z + 1.5);
            List<LivingEntity> hit = level.getEntitiesOfClass(LivingEntity.class, check, e -> e != player && e.isAlive() && !hitIds.contains(e.getId()));
            for (LivingEntity t : hit) {
                t.hurt(level.damageSources().playerAttack(player), damage);
                t.knockback(0.5, -look.x, -look.z);
                hitIds.add(t.getId());
            }
            // Dense shockwave trail with ground debris
            level.sendParticles((ParticleOptions) ParticleTypes.CLOUD, pt.x, pt.y + 0.5, pt.z, 4, 0.5, 0.1, 0.5, 0.01);
            level.sendParticles((ParticleOptions) ParticleTypes.CAMPFIRE_COSY_SMOKE, pt.x, pt.y + 0.2, pt.z, 2, 0.4, 0.05, 0.4, 0.01);
            level.sendParticles((ParticleOptions) ParticleTypes.COMPOSTER, pt.x, pt.y + 0.1, pt.z, 3, 0.5, 0.1, 0.5, 0.08);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.5f, 1.5f);
    }

    private static void doGroundSlam(ServerPlayer player, ServerLevel level, float damage, double radius) {
        AABB area = new AABB(player.blockPosition()).inflate(radius);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive() && e.distanceTo(player) <= radius);
        for (LivingEntity t : targets) {
            t.hurt(level.damageSources().playerAttack(player), damage);
            double dx = t.getX() - player.getX();
            double dz = t.getZ() - player.getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > 0) t.knockback(1.5, -dx / dist, -dz / dist);
        }
        // Ground slam shockwave
        WeaponEffects.shockwave(level, ParticleTypes.CAMPFIRE_COSY_SMOKE, player.getX(), player.getY() + 0.1, player.getZ(), radius, 3, 16, 1);
        // Debris flying up
        level.sendParticles((ParticleOptions) ParticleTypes.COMPOSTER, player.getX(), player.getY() + 0.2, player.getZ(), 20, radius * 0.5, 0.1, radius * 0.5, 0.12);
        // Central slam
        level.sendParticles((ParticleOptions) ParticleTypes.EXPLOSION, player.getX(), player.getY() + 0.2, player.getZ(), 3, 0.5, 0.0, 0.5, 0.0);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.7f, 0.6f);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.5f, 0.5f);
    }

    private static void doRampage(ServerPlayer player, ServerLevel level, int duration, int amplifier) {
        player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, duration, amplifier, false, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.SPEED, duration, 0, false, true, true));
        // Rage power-up spiral
        WeaponEffects.spiral(level, ParticleTypes.FLAME, player.getX(), player.getY(), player.getZ(), 1.0, 2.5, 20, 2);
        // Fiery aura burst
        level.sendParticles((ParticleOptions) ParticleTypes.FLAME, player.getX(), player.getY() + 1.0, player.getZ(), 20, 0.5, 0.8, 0.5, 0.08);
        level.sendParticles((ParticleOptions) ParticleTypes.LAVA, player.getX(), player.getY() + 0.3, player.getZ(), 6, 0.4, 0.2, 0.4, 0.0);
        // Power ring at feet
        WeaponEffects.ring(level, ParticleTypes.FLAME, player.getX(), player.getY() + 0.1, player.getZ(), 1.2, 10, 2, 0.03);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EVOKER_PREPARE_ATTACK, SoundSource.PLAYERS, 0.6f, 1.0f);
    }

    private static void doFlameCloud(ServerPlayer player, ServerLevel level, Vec3 center, float damage) {
        AABB area = new AABB(center.x - 2.5, center.y - 0.5, center.z - 2.5, center.x + 2.5, center.y + 1.5, center.z + 2.5);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive());
        for (LivingEntity t : targets) {
            t.hurt(level.damageSources().magic(), damage);
            t.setRemainingFireTicks(80);
            // Fire on each target
            level.sendParticles((ParticleOptions) ParticleTypes.FLAME, t.getX(), t.getY() + 0.5, t.getZ(), 6, 0.2, 0.4, 0.2, 0.03);
        }
        // Fire cloud ring
        WeaponEffects.ring(level, ParticleTypes.FLAME, center.x, center.y + 0.3, center.z, 2.0, 12, 2, 0.1);
        doFlameParticles(level, center, 25);
        // Embers
        level.sendParticles((ParticleOptions) ParticleTypes.LAVA, center.x, center.y + 0.3, center.z, 6, 1.5, 0.2, 1.5, 0.0);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.6f, 1.0f);
    }

    private static void doFrostCloud(ServerPlayer player, ServerLevel level, Vec3 center, float damage) {
        AABB area = new AABB(center.x - 2.5, center.y - 0.5, center.z - 2.5, center.x + 2.5, center.y + 1.5, center.z + 2.5);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive());
        for (LivingEntity t : targets) {
            t.hurt(level.damageSources().freeze(), damage);
            t.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 2, false, true, true));
            t.setTicksFrozen(t.getTicksFrozen() + 100);
            // Frost on each target
            level.sendParticles((ParticleOptions) ParticleTypes.ITEM_SNOWBALL, t.getX(), t.getY() + 0.5, t.getZ(), 6, 0.2, 0.4, 0.2, 0.08);
        }
        // Frost cloud ring
        WeaponEffects.ring(level, ParticleTypes.SNOWFLAKE, center.x, center.y + 0.3, center.z, 2.0, 12, 2, 0.08);
        doFrostParticles(level, center, 25);
        // Ice mist
        level.sendParticles((ParticleOptions) ParticleTypes.CLOUD, center.x, center.y + 0.2, center.z, 8, 1.5, 0.2, 1.5, 0.01);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.5f, 1.5f);
    }

    private static void doPoisonCloud(ServerPlayer player, ServerLevel level, Vec3 center, float damage) {
        AABB area = new AABB(center.x - 2.5, center.y - 0.5, center.z - 2.5, center.x + 2.5, center.y + 1.5, center.z + 2.5);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive());
        for (LivingEntity t : targets) {
            t.hurt(level.damageSources().magic(), damage);
            t.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1, false, true, true));
        }
        // Poison cloud ring
        WeaponEffects.ring(level, ParticleTypes.ITEM_SLIME, center.x, center.y + 0.3, center.z, 2.0, 10, 2, 0.1);
        // Toxic fumes
        level.sendParticles((ParticleOptions) ParticleTypes.ITEM_SLIME, center.x, center.y + 0.5, center.z, 25, 1.5, 0.5, 1.5, 0.05);
        level.sendParticles((ParticleOptions) ParticleTypes.SMOKE, center.x, center.y + 1.0, center.z, 15, 1.5, 0.5, 1.5, 0.02);
        // Rising toxic mist
        WeaponEffects.column(level, ParticleTypes.ITEM_SLIME, center.x, center.y, center.z, 2.0, 5, 2, 0.4);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS, 0.6f, 0.8f);
    }

    private static void doArrowBurst(ServerPlayer player, ServerLevel level, int count, float damage, double range) {
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, new AABB(player.blockPosition()).inflate(range), e -> e != player && e.isAlive());
        targets.sort(Comparator.comparingDouble(e -> e.distanceToSqr((Entity) player)));
        Vec3 from = new Vec3(player.getX(), player.getY() + 1.5, player.getZ());
        for (int i = 0; i < Math.min(count, targets.size()); i++) {
            LivingEntity t = targets.get(i);
            t.hurt(level.damageSources().magic(), damage);
            // Arrow trail from player to target
            Vec3 to = new Vec3(t.getX(), t.getY() + 1.0, t.getZ());
            WeaponEffects.line(level, ParticleTypes.CRIT, from, to, 8, 1, 0.05);
            // Impact burst
            level.sendParticles((ParticleOptions) ParticleTypes.CRIT, t.getX(), t.getY() + 1.0, t.getZ(), 8, 0.2, 0.3, 0.2, 0.1);
            level.sendParticles((ParticleOptions) ParticleTypes.ENCHANTED_HIT, t.getX(), t.getY() + 1.0, t.getZ(), 4, 0.2, 0.3, 0.2, 0.05);
        }
    }

    private static void doFlameParticles(ServerLevel level, Vec3 pos, int count) {
        level.sendParticles((ParticleOptions) ParticleTypes.FLAME, pos.x, pos.y + 0.5, pos.z, count, 1.5, 0.5, 1.5, 0.03);
        level.sendParticles((ParticleOptions) ParticleTypes.LARGE_SMOKE, pos.x, pos.y + 1.0, pos.z, count / 3, 1.0, 0.5, 1.0, 0.02);
        level.sendParticles((ParticleOptions) ParticleTypes.LAVA, pos.x, pos.y + 0.3, pos.z, count / 5, 1.0, 0.2, 1.0, 0.0);
    }

    private static void doFrostParticles(ServerLevel level, Vec3 pos, int count) {
        level.sendParticles((ParticleOptions) ParticleTypes.SNOWFLAKE, pos.x, pos.y + 0.5, pos.z, count, 1.5, 0.5, 1.5, 0.05);
        level.sendParticles((ParticleOptions) ParticleTypes.ITEM_SNOWBALL, pos.x, pos.y + 0.3, pos.z, count / 3, 1.2, 0.3, 1.2, 0.05);
    }
}
