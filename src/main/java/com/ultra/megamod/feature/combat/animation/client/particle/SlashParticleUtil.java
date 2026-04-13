package com.ultra.megamod.feature.combat.animation.client.particle;

import com.ultra.megamod.feature.combat.animation.AttackHand;
import com.ultra.megamod.feature.combat.animation.WeaponAttributeRegistry;
import com.ultra.megamod.feature.combat.animation.api.fx.ParticlePlacement;
import com.ultra.megamod.feature.combat.animation.api.fx.TrailAppearance;
import com.ultra.megamod.feature.combat.animation.config.BetterCombatConfig;
import com.ultra.megamod.feature.combat.animation.config.TrailConfig;
import com.ultra.megamod.feature.combat.animation.particle.SlashParticleEffect;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Utility for spawning slash trail particles during weapon swings.
 * Ported 1:1 from BetterCombat (net.bettercombat.client.particle.SlashParticleUtil).
 */
public class SlashParticleUtil {

    public record SpawnArgs(
            AbstractClientPlayer player,
            boolean isOffhand,
            float weaponRange,
            List<ParticlePlacement> settingsList,
            TrailAppearance appearance
    ) {}

    public record ScheduledSpawnArgs(
            SpawnArgs args,
            int time
    ) {}

    public static void spawnParticles(SpawnArgs args) {
        spawnParticles(args.player, args.isOffhand, args.weaponRange, args.settingsList, args.appearance);
    }

    public static void spawnParticles(AbstractClientPlayer player, boolean isOffhand, float weaponRange,
                                      List<ParticlePlacement> settingsList, TrailAppearance appearance) {
        if (!BetterCombatConfig.isShowingWeaponTrails) {
            return;
        }
        if (settingsList.isEmpty()) {
            return;
        }
        var isLeftHanded = player.getMainArm() == HumanoidArm.LEFT;
        var mirror = isOffhand;
        if (isLeftHanded) {
            mirror = !mirror;
        }
        weaponRange -= -0.25F;
        for (var settings : settingsList) {
            var id = settings.particle_type();
            var trails = TrailParticles.ENTRIES.get(id);
            if (trails == null) {
                continue;
            }

            var offsetX = settings.x_addition();
            var offsetY = settings.y_addition();
            var offsetZ = settings.z_addition();

            float offhandRoll = mirror ? 180.0F : 0.0F;
            float offhandFlip = mirror ? -1.0F : 1.0F;
            float yaw = player.getYRot();
            float pitch = player.getXRot();
            Vec3 right = Vec3.directionFromRotation(0.0F, yaw + 90.0F).normalize();
            Vec3 forward = Vec3.directionFromRotation(pitch, yaw).normalize();
            double baseX = player.getX();
            double baseY = player.getEyeY() - 0.25 + (double) offsetY;
            double baseZ = player.getZ();
            Vec3 finalPosition = (new Vec3(baseX, baseY, baseZ)).add(forward.scale(offsetZ)).add(right.scale(offsetX * offhandFlip));
            Vec3 stabFinalPosition = (new Vec3(finalPosition.x, finalPosition.y, finalPosition.z)).add(forward.scale((double) weaponRange - 1.5));
            double x = finalPosition.x;
            double y = finalPosition.y;
            double z = finalPosition.z;
            double xStab = stabFinalPosition.x;
            double yStab = stabFinalPosition.y;
            double zStab = stabFinalPosition.z;

            for (var trail : trails) {
                var posX = trail.stabPosition() ? xStab : x;
                var posY = trail.stabPosition() ? yStab : y;
                var posZ = trail.stabPosition() ? zStab : z;
                for (var layeredParticle : trail.particles()) {
                    player.level().addParticle(new SlashParticleEffect(
                                    layeredParticle.bottom(), weaponRange,
                                    player.getXRot() + settings.pitch_addition(), player.getYRot(),
                                    settings.local_yaw() * offhandFlip,
                                    (settings.roll_set() + trail.rollOffset() + offhandRoll) * offhandFlip,
                                    appearance.primary.glows(), appearance.primary.color_rgba()),
                            posX, posY, posZ, 0.0, 0.0, 0.0);

                    player.level().addParticle(new SlashParticleEffect(
                                    layeredParticle.top(), weaponRange,
                                    player.getXRot() + settings.pitch_addition(), player.getYRot(),
                                    settings.local_yaw() * offhandFlip,
                                    (settings.roll_set() + trail.rollOffset() + offhandRoll) * offhandFlip,
                                    appearance.secondary.glows(), appearance.secondary.color_rgba()),
                            posX, posY, posZ, 0.0, 0.0, 0.0);
                }
            }
        }
    }

    public static List<ParticlePlacement> trailParticlesFromAttack(AttackHand attackHand) {
        if (attackHand.attack() != null && attackHand.attack().animation() != null) {
            // Check if the attack has explicit trail particles defined
            // For MegaMod we rely on animation-based config
            var config = TrailParticles.getTrailConfig();
            if (config != null) {
                var animations = config.animation_based;
                if (animations != null) {
                    var animationSpecific = animations.get("megamod:" + attackHand.attack().animation());
                    if (animationSpecific != null) {
                        return animationSpecific;
                    }
                }
            }
        }
        return List.of();
    }

    public static TrailAppearance appearanceFromItemStack(ItemStack stack) {
        var config = TrailParticles.getTrailConfig();
        var defaults = config != null ? config.trail_appearance : null;
        if (defaults == null) {
            return TrailAppearance.DEFAULT;
        }
        var weaponAttributes = WeaponAttributeRegistry.getAttributes(stack);
        // Weapon-specific trail appearance could be stored on WeaponAttributes in the future
        return defaults.resolve(stack);
    }
}
