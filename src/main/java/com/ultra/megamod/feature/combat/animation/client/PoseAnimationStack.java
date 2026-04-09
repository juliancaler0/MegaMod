package com.ultra.megamod.feature.combat.animation.client;

import com.ultra.megamod.lib.playeranim.core.animation.layered.modifier.AbstractFadeModifier;
import com.ultra.megamod.lib.playeranim.core.animation.layered.modifier.MirrorModifier;
import com.ultra.megamod.lib.playeranim.core.easing.EasingType;
import com.ultra.megamod.lib.playeranim.core.enums.PlayState;
import com.ultra.megamod.lib.playeranim.minecraft.animation.PlayerAnimResources;
import com.ultra.megamod.lib.playeranim.minecraft.animation.PlayerAnimationController;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

/**
 * Idle weapon pose animation controller.
 * Ported from BetterCombat's PoseAnimationStack.
 * Extends PlayerAnimationController like AttackAnimationStack does.
 *
 * Each PoseAnimationStack manages one layer (body or item channel, main or off hand).
 * 4 instances per player: mainHandBody, mainHandItem, offHandBody, offHandItem.
 */
public class PoseAnimationStack extends PlayerAnimationController {

    public static final int MAIN_HAND_ITEM_PRIORITY = 1;
    public static final int MAIN_HAND_BODY_PRIORITY = 2;
    public static final int OFF_HAND_ITEM_PRIORITY = 3;
    public static final int OFF_HAND_BODY_PRIORITY = 4;

    public final MirrorModifier mirror = new MirrorModifier();

    private final boolean isMainHand;
    private PoseData lastPose;

    public PoseAnimationStack(Player player, boolean isMainHand) {
        super(player, (controller, data, setter) -> PlayState.CONTINUE);
        this.isMainHand = isMainHand;
        this.addModifier(mirror, 0);
    }

    /**
     * Set the idle pose animation. Skips if pose hasn't changed.
     */
    public void setPose(@Nullable String animationId, boolean isLeftHanded) {
        boolean shouldMirror = isLeftHanded;
        if (!isMainHand) shouldMirror = !shouldMirror;

        var newPoseData = PoseData.from(animationId, shouldMirror);
        if (lastPose != null && newPoseData.equals(lastPose)) return;

        if (animationId == null || animationId.isEmpty()) {
            stop();
        } else {
            Identifier animId = Identifier.fromNamespaceAndPath("megamod", animationId);
            if (PlayerAnimResources.hasAnimation(animId)) {
                mirror.enabled = shouldMirror;
                AbstractFadeModifier fade = AbstractFadeModifier.standardFadeIn(3, EasingType.EASE_IN_OUT_SINE);
                replaceAnimationWithFade(fade, animId);
            }
        }

        lastPose = newPoseData;
    }

    /**
     * Clear the pose animation.
     */
    public void clearPose() {
        stop();
        lastPose = null;
    }
}
