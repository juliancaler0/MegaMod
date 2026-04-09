package com.ultra.megamod.lib.playeranim.minecraft.accessors;

import com.ultra.megamod.lib.playeranim.minecraft.animation.AvatarAnimManager;
import com.ultra.megamod.lib.playeranim.core.animation.layered.IAnimation;
import net.minecraft.resources.Identifier;

public interface IAnimatedAvatar {
    AvatarAnimManager playerAnimLib$getAnimManager();
    IAnimation playerAnimLib$getAnimation(Identifier id);
}
