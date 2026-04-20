package com.ultra.megamod.feature.combat.animation.client;

import com.ultra.megamod.lib.playeranim.core.animation.layered.modifier.AdjustmentModifier;
import com.ultra.megamod.lib.playeranim.core.animation.layered.modifier.MirrorModifier;
import com.ultra.megamod.lib.playeranim.core.api.firstPerson.FirstPersonMode;
import com.ultra.megamod.lib.playeranim.core.enums.PlayState;
import com.ultra.megamod.lib.playeranim.core.math.Vec3f;
import com.ultra.megamod.lib.playeranim.minecraft.animation.PlayerAnimResources;
import com.ultra.megamod.lib.playeranim.minecraft.animation.PlayerAnimationController;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Idle weapon pose animation controller — 1:1 port of BetterCombat's PoseAnimationStack.
 *
 * <p>Source runs 4 instances per player: mainHandBody, mainHandItem, offHandBody, offHandItem.
 * The body/item channel split lets body poses be suppressed (e.g. during walking) while the
 * weapon-arm item pose keeps showing. Only the mainHand+body instance installs the sneaking
 * offset adjustment.</p>
 *
 * <p>Uses {@code triggerAnimation} / {@code stopTriggeredAnimation} (matches source) so pose
 * changes don't stack fade modifiers — previously used {@code replaceAnimationWithFade} which
 * caused the visible pose-transition jitter.</p>
 */
public class PoseAnimationStack extends PlayerAnimationController {

    public static final Identifier MAIN_HAND_BODY_ID = Identifier.fromNamespaceAndPath("megamod", "pose_main_hand_body");
    public static final Identifier MAIN_HAND_ITEM_ID = Identifier.fromNamespaceAndPath("megamod", "pose_main_hand_item");
    public static final Identifier OFF_HAND_BODY_ID  = Identifier.fromNamespaceAndPath("megamod", "pose_off_hand_body");
    public static final Identifier OFF_HAND_ITEM_ID  = Identifier.fromNamespaceAndPath("megamod", "pose_off_hand_item");

    // Priority ordering matches source BetterCombat (applied in AvatarAnimManager.addAnimLayer).
    public static final int MAIN_HAND_ITEM_PRIORITY = 1;
    public static final int MAIN_HAND_BODY_PRIORITY = 2;
    public static final int OFF_HAND_ITEM_PRIORITY  = 3;
    public static final int OFF_HAND_BODY_PRIORITY  = 4;

    public final MirrorModifier mirror = new MirrorModifier();

    private final boolean isMainHand;
    private final boolean isBodyChannel;
    private PoseData lastPose;

    public PoseAnimationStack(Player player, boolean isMainHand, boolean isBodyChannel) {
        super(player, (controller, data, setter) -> PlayState.CONTINUE);
        this.isMainHand = isMainHand;
        this.isBodyChannel = isBodyChannel;
        this.addModifier(mirror, 0);

        // Source only installs the sneaking offset adjustment on the mainHand+body instance.
        if (isMainHand && isBodyChannel) {
            this.addModifierLast(createPoseAdjustment());
        }

        // Poses never show in first-person (source sets DISABLED).
        this.firstPersonMode = (controller) -> FirstPersonMode.DISABLED;
    }

    /**
     * Set the idle pose animation. Matches source: uses {@code triggerAnimation}
     * (one-shot), dedups on lastPose equality, and clears via {@code stopTriggeredAnimation}.
     */
    public void setPose(@Nullable String animationId, boolean isLeftHanded) {
        boolean shouldMirror = isLeftHanded;
        if (!isMainHand) shouldMirror = !shouldMirror;

        var newPoseData = PoseData.from(animationId, shouldMirror);
        if (lastPose != null && newPoseData.equals(lastPose)) return;

        if (animationId == null || animationId.isEmpty()) {
            stopTriggeredAnimation();
        } else {
            Identifier animId = animationId.contains(":") ? Identifier.parse(animationId)
                    : Identifier.fromNamespaceAndPath("megamod", animationId);
            if (PlayerAnimResources.hasAnimation(animId)) {
                mirror.enabled = shouldMirror;
                triggerAnimation(animId);
            }
        }

        lastPose = newPoseData;
    }

    public void clearPose() {
        stopTriggeredAnimation();
        lastPose = null;
    }

    /**
     * Matches source's {@code createPoseAdjustment}: arms slide down by 3 units when sneaking
     * so a two-handed idle pose doesn't clip into the camera/body. Only installed on the
     * main-hand+body channel instance.
     */
    private AdjustmentModifier createPoseAdjustment() {
        return new AdjustmentModifier((partName, data) -> {
            float offsetY = 0;
            var player = this.getAvatar();
            if (!data.isFirstPersonPass()) {
                if (isArm(partName)) {
                    if (player.isCrouching()) {
                        offsetY -= 3;
                    }
                } else {
                    return Optional.empty();
                }
            }
            return Optional.of(new AdjustmentModifier.PartModifier(
                    new Vec3f(0, 0, 0),
                    new Vec3f(0, offsetY, 0)));
        });
    }

    private static boolean isArm(String partName) {
        return "right_arm".equals(partName) || "left_arm".equals(partName);
    }
}
