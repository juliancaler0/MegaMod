package com.ultra.megamod.feature.combat.animation.client;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.animation.WeaponAttributes.SwingDirection;
import com.ultra.megamod.feature.combat.spell.client.particle.SlashParticle;
import com.ultra.megamod.feature.combat.spell.client.particle.SpellParticleRegistry;
import com.ultra.megamod.feature.combat.spell.client.util.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Spawns custom 3D slash trail particles during active weapon swings.
 * Ported 1:1 from BetterCombat's SlashParticleUtil approach.
 *
 * Uses dual-sided SlashParticle with 3D rotation instead of vanilla SWEEP_ATTACK.
 * Each swing direction maps to specific slash particle types with matching
 * pitch/yaw/roll offsets from BetterCombat's TrailParticles configuration.
 */
@EventBusSubscriber(modid = MegaMod.MODID, value = Dist.CLIENT)
public class SwingParticleRenderer {

    private static final float PARTICLE_START = SwingAnimationState.WINDUP_END;
    private static final float PARTICLE_END = SwingAnimationState.ATTACK_END;

    // Default trail colors (from BetterCombat's TrailParticles.defaults())
    private static final long PRIMARY_COLOR = Color.WHITE.alpha(0.6f).toRGBA();
    private static final long SECONDARY_COLOR = Color.from(0x999999).alpha(0.4f).toRGBA();

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        // PAL handles its own animation ticking via its mixins.
        // Update BetterCombat pose animations per game tick.
        for (net.minecraft.world.entity.Entity e : mc.level.entitiesForRendering()) {
            if (e instanceof PlayerAttackAnimatable animatable) {
                animatable.updateAnimationsOnTick();
            }
        }

        // Smooth movement speed reduction during attacks (ported from BC's ClientPlayerEntityMixin)
        var bcClient = (com.ultra.megamod.feature.combat.animation.api.MinecraftClient_BetterCombat) mc;
        float speedMult = (float) Math.min(Math.max(
                com.ultra.megamod.feature.combat.animation.config.ScopedCombatConfig.movementSpeedWhileAttacking(mc.player), 0), 1);
        // Note: Our Attack record doesn't have movementSpeedMultiplier yet
        // so we skip per-attack multiplier for now
        if (speedMult < 1.0f && !mc.player.isPassenger()) {
            float swingProgress = bcClient.getSwingProgress();
            if (swingProgress < 0.98f) {
                if (com.ultra.megamod.feature.combat.animation.config.BetterCombatConfig.movement_speed_applied_smoothly) {
                    double p2;
                    if (swingProgress <= 0.5) {
                        p2 = com.ultra.megamod.feature.combat.animation.utils.MathHelper.easeOutCubic(swingProgress * 2);
                    } else {
                        p2 = com.ultra.megamod.feature.combat.animation.utils.MathHelper.easeOutCubic(1 - ((swingProgress - 0.5) * 2));
                    }
                    speedMult = (float) (1.0 - (1.0 - speedMult) * p2);
                }
                // Apply movement reduction via velocity scaling
                var vel = mc.player.getDeltaMovement();
                mc.player.setDeltaMovement(vel.x * speedMult, vel.y, vel.z * speedMult);
            }
        }

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof Player player)) continue;

            SwingAnimationState.ActiveSwing swing = SwingAnimationState.getActiveSwing(player.getId());
            if (swing == null || swing.isExpired()) continue;

            float progress = swing.progress();
            if (progress < PARTICLE_START || progress > PARTICLE_END) continue;

            // Only spawn once per swing (at the midpoint of the attack phase)
            float midPoint = (PARTICLE_START + PARTICLE_END) / 2f;
            if (Math.abs(progress - midPoint) > 0.05f) continue;

            spawnSlashParticles(player, swing.direction(), swing.isOffHand());
        }
    }

    private static void spawnSlashParticles(Player player, SwingDirection direction, boolean offHand) {
        float yaw = player.getYRot();
        float pitch = player.getXRot();
        boolean mirror = offHand;
        if (player.getMainArm() == net.minecraft.world.entity.HumanoidArm.LEFT) {
            mirror = !mirror;
        }
        float offhandFlip = mirror ? -1f : 1f;
        float offhandRoll = mirror ? 180f : 0f;

        // Compute spawn position (in front of player at eye height)
        Vec3 look = player.getLookAngle().normalize();
        Vec3 right = Vec3.directionFromRotation(0, yaw + 90).normalize();
        double x = player.getX();
        double y = player.getEyeY() - 0.25;
        double z = player.getZ();

        // Map swing direction to BetterCombat particle placement
        // From TrailParticles.defaults() configuration
        switch (direction) {
            case SLASH_RIGHT -> spawnLayered(player, x, y, z,
                    SpellParticleRegistry.TOPSLASH90.get(), SpellParticleRegistry.BOTSLASH90.get(),
                    1.0f, pitch, yaw, 0, 0 * offhandFlip + offhandRoll * offhandFlip,
                    0, -0.1f, 0);
            case SLASH_LEFT -> spawnLayered(player, x, y, z,
                    SpellParticleRegistry.TOPSLASH90.get(), SpellParticleRegistry.BOTSLASH90.get(),
                    1.0f, pitch, yaw, 0, (180f + offhandRoll) * offhandFlip,
                    0, -0.1f, 0);
            case SLASH_DOWN -> spawnLayered(player, x, y, z,
                    SpellParticleRegistry.TOPSLASH90.get(), SpellParticleRegistry.BOTSLASH90.get(),
                    1.0f, pitch + 45f, yaw, 0, (-85f + offhandRoll) * offhandFlip,
                    0.05f, -0.1f, 0);
            case STAB -> spawnLayered(player,
                    x + look.x * 0.7, y, z + look.z * 0.7,
                    SpellParticleRegistry.TOPSTAB.get(), SpellParticleRegistry.BOTSTAB.get(),
                    1.0f, pitch, yaw, 0, (-45f + offhandRoll) * offhandFlip,
                    0, -0.13f, 0.2f);
            case UPPERCUT -> spawnLayered(player, x, y, z,
                    SpellParticleRegistry.TOPSLASH90.get(), SpellParticleRegistry.BOTSLASH90.get(),
                    1.0f, pitch, yaw, 0, (75f + offhandRoll) * offhandFlip,
                    0, -0.1f, 0);
            case SPIN -> spawnLayered(player, x, y, z,
                    SpellParticleRegistry.TOPSLASH360.get(), SpellParticleRegistry.BOTSLASH360.get(),
                    1.0f, pitch, yaw, 0, (180f + offhandRoll) * offhandFlip,
                    0, -0.1f, 0);
        }
    }

    private static void spawnLayered(Player player, double x, double y, double z,
                                      SimpleParticleType topType, SimpleParticleType bottomType,
                                      float scale, float pitch, float yaw, float localYaw, float roll,
                                      float offsetX, float offsetY, float offsetZ) {
        var level = player.level();

        // Apply offsets relative to player facing
        Vec3 forward = Vec3.directionFromRotation(pitch, yaw).normalize();
        Vec3 right = Vec3.directionFromRotation(0, yaw + 90).normalize();
        double px = x + forward.x * offsetZ + right.x * offsetX;
        double py = y + offsetY;
        double pz = z + forward.z * offsetZ + right.z * offsetX;

        // Bottom layer (primary color — white/gray)
        SlashParticle.setParams(new SlashParticle.SlashParams(
                scale, pitch, yaw, localYaw, roll, false, PRIMARY_COLOR));
        level.addParticle(bottomType, px, py, pz, 0, 0, 0);

        // Top layer (secondary color — darker/translucent)
        SlashParticle.setParams(new SlashParticle.SlashParams(
                scale, pitch, yaw, localYaw, roll, false, SECONDARY_COLOR));
        level.addParticle(topType, px, py, pz, 0, 0, 0);
    }
}
