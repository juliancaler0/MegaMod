package com.ultra.megamod.mixin;

import com.ultra.megamod.feature.combat.animation.client.*;
import com.ultra.megamod.feature.combat.animation.config.BetterCombatConfig;
import com.ultra.megamod.feature.combat.animation.logic.AnimatedHand;
import com.ultra.megamod.feature.combat.animation.logic.PlayerAttackHelper;
import com.ultra.megamod.feature.combat.animation.logic.Pose;
import com.ultra.megamod.lib.playeranim.core.animation.Animation;
import com.ultra.megamod.lib.playeranim.core.api.firstPerson.FirstPersonConfiguration;
import com.ultra.megamod.lib.playeranim.core.enums.PlayState;
import com.ultra.megamod.lib.playeranim.minecraft.api.PlayerAnimationAccess;
import com.ultra.megamod.lib.playeranim.minecraft.animation.AvatarAnimManager;
import com.ultra.megamod.lib.playeranim.minecraft.animation.PlayerAnimResources;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Core client-side mixin implementing BetterCombat's player animation system.
 * Ported from BetterCombat's AbstractClientPlayerEntityMixin.
 *
 * Holds 5 animation controllers per player:
 * - 1 AttackAnimationStack at priority 2000 (overrides everything during attacks)
 * - 4 PoseAnimationStack at priorities 1-4 (idle weapon hold poses)
 *
 * All controllers extend PlayerAnimationController and are registered as layers
 * on the player's AvatarAnimManager via REGISTER_ANIMATION_EVENT.
 *
 * Implements PlayerAttackAnimatable to receive attack/pose animation commands.
 */
@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerMixin implements PlayerAttackAnimatable,
        com.ultra.megamod.feature.combat.animation.api.PlayerAttackProperties {

    @Unique
    private int megamod$comboCount = 0;

    @Override
    public int getComboCount() { return megamod$comboCount; }
    @Override
    public void setComboCount(int comboCount) { megamod$comboCount = comboCount; }

    @Unique
    private AttackAnimationStack megamod$attackAnimation;

    @Unique
    private PoseAnimationStack megamod$mainHandBodyPose;
    @Unique
    private PoseAnimationStack megamod$mainHandItemPose;
    @Unique
    private PoseAnimationStack megamod$offHandBodyPose;
    @Unique
    private PoseAnimationStack megamod$offHandItemPose;

    @Unique
    private Pose megamod$lastPose = new Pose("", "");

    @Unique
    private boolean megamod$animInitialized = false;

    /**
     * Initialize animation stacks when the player entity is created.
     * Registers all layers on the player's animation stack via AvatarAnimManager.
     */
    @Inject(method = "<init>*", at = @At("TAIL"))
    private void megamod$initAnimations(CallbackInfo ci) {
        var player = (AbstractClientPlayer) (Object) this;

        // AnimationStateHandler: just continue, we drive animations via triggerAnimation/replaceAnimationWithFade
        var handler = (com.ultra.megamod.lib.playeranim.core.animation.AnimationController.AnimationStateHandler)
                (controller, data, setter) -> PlayState.CONTINUE;

        megamod$attackAnimation = new AttackAnimationStack(player, handler);
        // 4-stack pose system matches source BetterCombat: (isMainHand, isBodyChannel).
        // Body channel can be suppressed during walking/sneaking while item channel keeps
        // showing the weapon-arm pose — fixes body-shake-after-swing on two-handed weapons.
        megamod$mainHandBodyPose = new PoseAnimationStack(player, true,  true);
        megamod$mainHandItemPose = new PoseAnimationStack(player, true,  false);
        megamod$offHandBodyPose  = new PoseAnimationStack(player, false, true);
        megamod$offHandItemPose  = new PoseAnimationStack(player, false, false);

        // Register all layers on the animation stack
        // Controllers ARE IAnimation instances — register them directly (no .base needed)
        try {
            AvatarAnimManager manager = PlayerAnimationAccess.getPlayerAnimManager(player);
            manager.addAnimLayer(PoseAnimationStack.MAIN_HAND_ITEM_PRIORITY, megamod$mainHandItemPose);
            manager.addAnimLayer(PoseAnimationStack.MAIN_HAND_BODY_PRIORITY, megamod$mainHandBodyPose);
            manager.addAnimLayer(PoseAnimationStack.OFF_HAND_ITEM_PRIORITY, megamod$offHandItemPose);
            manager.addAnimLayer(PoseAnimationStack.OFF_HAND_BODY_PRIORITY, megamod$offHandBodyPose);
            manager.addAnimLayer(AttackAnimationStack.PRIORITY, megamod$attackAnimation);
        } catch (IllegalArgumentException e) {
            // Animation manager not yet ready — will be registered via event
            com.ultra.megamod.MegaMod.LOGGER.debug("[AnimInit] Deferred layer registration for player {}", player.getName().getString());
        }

        megamod$animInitialized = true;
        com.ultra.megamod.MegaMod.LOGGER.info("[AnimInit] Player {} init complete", player.getName().getString());
    }

    // =============================================
    // PlayerAttackAnimatable implementation
    // =============================================

    @Override
    public void playAttackAnimation(String name, AnimatedHand hand, float length, float upswing) {
        if (!megamod$animInitialized || megamod$attackAnimation == null) {
            com.ultra.megamod.MegaMod.LOGGER.warn("[BetterCombat] playAttackAnimation called but not initialized! init={} stack={}", megamod$animInitialized, megamod$attackAnimation);
            return;
        }

        var player = (AbstractClientPlayer) (Object) this;
        // Animation name may already include namespace (e.g., "megamod:one_handed_slash")
        // or be plain (e.g., "one_handed_slash"). Identifier.parse handles both.
        Identifier animId = name.contains(":") ? Identifier.parse(name) : Identifier.fromNamespaceAndPath("megamod", name);
        Animation animation = PlayerAnimResources.getAnimation(animId);
        if (animation == null) {
            com.ultra.megamod.MegaMod.LOGGER.warn("[BetterCombat] Animation NOT FOUND: {}", animId);
            return;
        }

        // Calculate speed using endTick (actual hit point), NOT animation.length() (includes recovery)
        // This matches BetterCombat's original: speed = endTick / weaponCooldown.
        // Clamp length to >= 1 tick to avoid NaN/Infinity when called with length=0
        // (e.g. weapon-swap frame or broken attack data). (Task #46)
        float safeLength = Math.max(length, 1.0f);
        var endTick = animation.data().<Float>get(
                com.ultra.megamod.lib.playeranim.core.animation.ExtraAnimationData.END_TICK_KEY
        ).orElse(animation.length());
        float speed = endTick / safeLength;

        // Upswing speed: original uses upswingMultiplier config (default 0.5)
        float upswingMultiplier = Math.clamp(
                com.ultra.megamod.feature.combat.animation.config.ScopedCombatConfig.upswingMultiplier(player),
                0.2f, 1.0f);
        float trueUpswingRatio = upswing / upswingMultiplier;
        float upswingSpeed = speed / trueUpswingRatio;

        // Downswing speed: original BetterCombat lerp formula
        float downswingSpeed = (float) (speed *
                net.minecraft.util.Mth.lerp(
                        Math.max(upswingMultiplier - 0.5, 0) / 0.5,
                        (1F - upswing),
                        upswing / (1F - upswing)));

        // Gear shifts: upswingSpeed initially, then downswingSpeed, then baseSpeed
        List<TransmissionSpeedModifier.Gear> gears = List.of(
                new TransmissionSpeedModifier.Gear(safeLength * upswing, downswingSpeed),
                new TransmissionSpeedModifier.Gear(safeLength, speed)
        );

        // Mirror for off-hand / left-handed
        boolean mirror = hand.isOffHand();
        if (player.getMainArm() == HumanoidArm.LEFT) {
            mirror = !mirror;
        }

        // First-person configuration
        boolean showRightItem = true;
        boolean showLeftItem = BetterCombatConfig.isShowingOtherHandFirstPerson || hand == AnimatedHand.TWO_HANDED;
        boolean showRightArm = showRightItem && BetterCombatConfig.isShowingArmsInFirstPerson;
        boolean showLeftArm = showLeftItem && BetterCombatConfig.isShowingArmsInFirstPerson;
        var fpConfig = new FirstPersonConfiguration(showRightArm, showLeftArm, showRightItem, showLeftItem);
        if (hand.isOffHand()) {
            fpConfig = FirstPersonHelper.mirrored(fpConfig);
        }

        megamod$attackAnimation.playAnimation(name, mirror, upswingSpeed, gears, fpConfig);
    }

    @Override
    public void stopAttackAnimation(float length) {
        if (megamod$attackAnimation != null) {
            megamod$attackAnimation.stopAnimation(length);
        }
    }

    @Override
    public void updateAnimationsOnTick() {
        if (!megamod$animInitialized) return;
        var player = (AbstractClientPlayer) (Object) this;

        // Fire scheduled slash-trail particles at their target tick (matches source BetterCombat).
        if (megamod$scheduledParticles != null && megamod$scheduledParticles.time() == player.tickCount) {
            com.ultra.megamod.feature.combat.animation.client.particle.SlashParticleUtil.spawnParticles(
                    megamod$scheduledParticles.player(),
                    megamod$scheduledParticles.isOffHand(),
                    megamod$scheduledParticles.weaponRange(),
                    megamod$scheduledParticles.particles(),
                    megamod$scheduledParticles.appearance());
            megamod$scheduledParticles = null;
        }

        // Update idle weapon poses
        boolean isLeftHanded = player.getMainArm() == HumanoidArm.LEFT;

        // Determine pose from equipped weapons
        Pose pose = PlayerAttackHelper.poseForPlayer(player);

        // Match source BetterCombat AbstractClientPlayerEntityMixin.updateAnimationsOnTick exactly.
        // Important: do NOT clear poses during attack or spell cast — attack runs on a higher
        // priority layer (the triggered slot) and naturally overrides the base pose layer.
        // Clearing pose during attack caused a 1-frame gap at swing end where body snapped to
        // default before pose re-triggered — visible as the "body shake after hammer swing".
        boolean crossbowCharged = net.minecraft.world.item.CrossbowItem.isCharged(player.getMainHandItem());
        boolean isCastingSpell = player instanceof com.ultra.megamod.lib.spellengine.internals.casting.SpellCasterEntity caster
                && caster.isCastingSpell();
        boolean clearPoses = player.swinging           // vanilla hand-swing mid-flight
                || player.isSwimming()
                || player.onClimbable()
                || player.isUsingItem()                // bows, food, shields
                || player.isFallFlying()               // elytra
                || isCastingSpell                      // spell cast drives its own animation
                || crossbowCharged                     // loaded crossbow
                || !player.isAlive();

        if (clearPoses) {
            megamod$mainHandBodyPose.clearPose();
            megamod$mainHandItemPose.clearPose();
            megamod$offHandBodyPose.clearPose();
            megamod$offHandItemPose.clearPose();
            megamod$lastPose = new Pose("", "");
            return;
        }

        // Source BetterCombat semantics:
        //   - ITEM channel always shows the pose when set (weapon-arm must hold the pose)
        //   - BODY channel is suppressed during walking/sneaking for ONE-HANDED weapons
        //     (two-handed weapons keep full body pose since both arms are on the weapon)
        String mainPoseId = (pose.base() != null && !pose.base().isEmpty()) ? pose.base() : null;
        String offPoseId  = (pose.offHand() != null && !pose.offHand().isEmpty()) ? pose.offHand() : null;

        // Item poses: always apply
        megamod$mainHandItemPose.setPose(mainPoseId, isLeftHanded);
        megamod$offHandItemPose.setPose(offPoseId, isLeftHanded);

        // Body poses: suppress during walking/sneaking for one-handed wielding
        boolean twoHanded = PlayerAttackHelper.isTwoHandedWielding(player);
        boolean bodyClear = !twoHanded && (megamod$isWalking(player) || player.isCrouching());
        megamod$mainHandBodyPose.setPose(bodyClear ? null : mainPoseId, isLeftHanded);
        megamod$offHandBodyPose.setPose(bodyClear ? null : offPoseId, isLeftHanded);

        megamod$lastPose = pose;
    }

    @Unique
    private static boolean megamod$isWalking(AbstractClientPlayer player) {
        return !player.isDeadOrDying()
                && (player.isSwimming() || player.getDeltaMovement().horizontalDistance() > 0.03);
    }

    /**
     * Implementation of {@link PlayerAttackAnimatable#playAttackParticles}. Matches source
     * BetterCombat: stores a {@link com.ultra.megamod.feature.combat.animation.client.ScheduledSlashParticles}
     * record and defers spawning to {@link #updateAnimationsOnTick}, which fires the particles
     * via {@link com.ultra.megamod.feature.combat.animation.client.particle.SlashParticleUtil#spawnParticles}
     * at the target tick (upswing end). {@code TrailParticles.ENTRIES} and {@code SlashParticleEffect}
     * are both fully ported — this is the live spawn path, not a stub.
     */
    @Unique
    private com.ultra.megamod.feature.combat.animation.client.ScheduledSlashParticles megamod$scheduledParticles;

    @Override
    public void playAttackParticles(boolean isOffHand, float weaponRange, int delay,
                                    java.util.List<com.ultra.megamod.feature.combat.animation.api.fx.ParticlePlacement> particles,
                                    com.ultra.megamod.feature.combat.animation.api.fx.TrailAppearance appearance) {
        var player = (AbstractClientPlayer) (Object) this;
        megamod$scheduledParticles =
                new com.ultra.megamod.feature.combat.animation.client.ScheduledSlashParticles(
                        player, isOffHand, weaponRange, particles, appearance, player.tickCount + delay);
    }
}
