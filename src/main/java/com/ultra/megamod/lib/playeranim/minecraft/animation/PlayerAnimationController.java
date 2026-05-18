package com.ultra.megamod.lib.playeranim.minecraft.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.ultra.megamod.lib.playeranim.minecraft.PlayerAnimLibMod;
import com.ultra.megamod.lib.playeranim.minecraft.util.RenderUtil;
import com.ultra.megamod.lib.playeranim.core.animation.AnimationController;
import com.ultra.megamod.lib.playeranim.core.animation.HumanoidAnimationController;
import com.ultra.megamod.lib.playeranim.core.animation.layered.AnimationSnapshot;
import com.ultra.megamod.lib.playeranim.core.animation.layered.modifier.AbstractFadeModifier;
import com.ultra.megamod.lib.playeranim.core.bones.PlayerAnimBone;
import com.ultra.megamod.lib.playeranim.core.bones.ToggleablePlayerAnimBone;
import com.ultra.megamod.lib.playeranim.core.easing.EasingType;
import com.ultra.megamod.lib.playeranim.core.enums.State;
import com.ultra.megamod.lib.playeranim.core.math.Vec3f;
import com.ultra.megamod.lib.playeranim.core.molang.MochaEngine;
import com.ultra.megamod.lib.playeranim.core.molang.MolangLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class PlayerAnimationController extends HumanoidAnimationController {
    protected final Avatar avatar;

    /**
     * Instantiates a new {@code AnimationController}
     *
     * @param avatar           The object that will be animated by this controller
     * @param animationHandler The {@link AnimationStateHandler} animation state handler responsible for deciding which animations to play
     */
    public PlayerAnimationController(Avatar avatar, AnimationStateHandler animationHandler) {
        this(avatar, animationHandler, MolangLoader::createNewEngine);
    }

    /**
     * Instantiates a new {@code AnimationController}
     *
     * @param avatar           The object that will be animated by this controller
     * @param animationHandler The {@link AnimationStateHandler} animation state handler responsible for deciding which animations to play
     * @param molangRuntime    A function that provides the MoLang runtime engine for this animation controller when applied
     */
    public PlayerAnimationController(Avatar avatar, AnimationStateHandler animationHandler, Function<AnimationController, MochaEngine<AnimationController>> molangRuntime) {
        super(animationHandler, molangRuntime);
        this.avatar = avatar;
    }

    public Avatar getAvatar() {
        return this.avatar;
    }

    public boolean triggerAnimation(Identifier newAnimation, float startAnimFrom) {
        if (PlayerAnimResources.hasAnimation(newAnimation)) {
            triggerAnimation(PlayerAnimResources.getAnimation(newAnimation), startAnimFrom);
            return true;
        }
        PlayerAnimLibMod.LOGGER.error("Could not find animation with the name:" + newAnimation);
        return false;
    }

    public boolean triggerAnimation(Identifier newAnimation) {
        return triggerAnimation(newAnimation, 0);
    }

    public boolean replaceAnimationWithFade(@NotNull AbstractFadeModifier fadeModifier, @Nullable Identifier newAnimation, boolean fadeFromNothing) {
        if (PlayerAnimResources.hasAnimation(newAnimation)) {
            replaceAnimationWithFade(fadeModifier, PlayerAnimResources.getAnimation(newAnimation), fadeFromNothing);
            return true;
        }
        return false;
    }

    public boolean replaceAnimationWithFade(@NotNull AbstractFadeModifier fadeModifier, @Nullable Identifier newAnimation) {
        return replaceAnimationWithFade(fadeModifier, newAnimation, true);
    }

    /**
     * Fade out the currently playing animation to the default pose over {@code length} ticks.
     * <p>
     * The base {@link AnimationController#replaceAnimationWithFade} is unsuitable here because it
     * delegates to {@link AnimationController#triggerAnimation(com.ultra.megamod.lib.playeranim.core.animation.RawAnimation)}
     * which returns early on {@code null} — the fade modifier ends up overlaying a still-active
     * animation, the underlying pose never changes, and the player freezes mid-frame. (Source
     * SpellEngine works because it operates on a {@code ModifierLayer} whose {@code setAnimation(null)}
     * actually clears the underlying animation.)
     * <p>
     * This method snapshots the current activeBones into the fade modifier as transitionAnimation,
     * adds the modifier, then resets the controller's pose state so {@code get3DTransformRaw}
     * returns the identity pose. The modifier interpolates {@code snapshot * (1-a) + identity * a};
     * once the fade completes the modifier auto-removes (FADE_IN canRemove) and only the empty
     * controller remains.
     */
    public void fadeOut(int length) {
        if (length <= 0 || !this.isActive()) {
            this.stop();
            this.activeBones.clear();
            this.currentAnimation = null;
            this.currentRawAnimation = null;
            this.triggeredAnimation = null;
            this.animationState = State.STOPPED;
            for (PlayerAnimBone bone : this.bones.values()) bone.setToInitialPose();
            for (PlayerAnimBone bone : this.pivotBones.values()) bone.setToInitialPose();
            return;
        }

        Map<String, ToggleablePlayerAnimBone> snapshot = new HashMap<>();
        for (PlayerAnimBone bone : this.activeBones.values()) {
            snapshot.put(bone.getName(), new ToggleablePlayerAnimBone(bone));
        }

        AbstractFadeModifier fadeModifier = AbstractFadeModifier.standardFadeIn(length, EasingType.EASE_IN_OUT_SINE);
        fadeModifier.setTransitionAnimation(new AnimationSnapshot(snapshot));
        addModifierLast(fadeModifier);

        this.activeBones.clear();
        this.currentAnimation = null;
        this.currentRawAnimation = null;
        this.triggeredAnimation = null;
        this.animationState = State.STOPPED;
        for (PlayerAnimBone bone : this.bones.values()) bone.setToInitialPose();
        for (PlayerAnimBone bone : this.pivotBones.values()) bone.setToInitialPose();
    }

    /**
     * Get the position of a bone in the world in the form of a PoseStack.
     */
    public @Nullable PoseStack getBoneWorldPositionPoseStack(String name, float tickDelta, Vec3 cameraPos) {
        if (!this.activeBones.containsKey(name)) return null;
        PoseStack poseStack = new PoseStack();
        Vec3f pivot = getBonePosition(name);
        Vec3 position = avatar.getPosition(tickDelta).subtract(cameraPos).add(pivot.x(), pivot.y(), pivot.z());
        poseStack.translate(position.x(), position.y(), position.z());
        poseStack.mulPose(Axis.YP.rotationDegrees(180 - Mth.lerp(tickDelta, avatar.yBodyRotO, avatar.yBodyRot)));
        RenderUtil.translateMatrixToBone(poseStack, this.activeBones.get(name));
        return poseStack;
    }
}
