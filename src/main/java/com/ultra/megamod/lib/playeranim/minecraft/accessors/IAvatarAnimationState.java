package com.ultra.megamod.lib.playeranim.minecraft.accessors;

import com.ultra.megamod.lib.playeranim.minecraft.animation.AvatarAnimManager;

/**
 * Extension of PlayerRenderState
 */
public interface IAvatarAnimationState {
    boolean playerAnimLib$isFirstPersonPass();
    void playerAnimLib$setFirstPersonPass(boolean value);

    // AnimationApplier animationApplier
    void playerAnimLib$setAnimManager(AvatarAnimManager manager);
    AvatarAnimManager playerAnimLib$getAnimManager();
}
