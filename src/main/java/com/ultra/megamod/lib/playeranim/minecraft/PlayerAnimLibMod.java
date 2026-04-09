package com.ultra.megamod.lib.playeranim.minecraft;

import com.ultra.megamod.lib.playeranim.minecraft.animation.PlayerAnimationController;
import com.ultra.megamod.lib.playeranim.minecraft.animation.keyframe.event.builtin.AutoPlayingSoundKeyframeHandler;
import com.ultra.megamod.lib.playeranim.minecraft.api.PlayerAnimationFactory;
import com.ultra.megamod.lib.playeranim.core.PlayerAnimLib;
import com.ultra.megamod.lib.playeranim.core.animation.keyframe.event.CustomKeyFrameEvents;
import com.ultra.megamod.lib.playeranim.core.enums.PlayState;
import net.minecraft.resources.Identifier;

public abstract class PlayerAnimLibMod extends PlayerAnimLib {
    public static final Identifier ANIMATION_LAYER_ID = PlayerAnimLibMod.id("factory");

    public static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(MOD_ID, name);
    }

    protected void init() {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(ANIMATION_LAYER_ID, 1000,
                player -> new PlayerAnimationController(player, (c, s, a) -> PlayState.STOP)
        );
        CustomKeyFrameEvents.SOUND_KEYFRAME_EVENT.register(new AutoPlayingSoundKeyframeHandler());
    }
}
