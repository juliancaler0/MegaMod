package com.ultra.megamod.lib.playeranim.minecraft;

import com.ultra.megamod.lib.playeranim.minecraft.animation.PlayerAnimationController;
import com.ultra.megamod.lib.playeranim.minecraft.animation.keyframe.event.builtin.AutoPlayingSoundKeyframeHandler;
import com.ultra.megamod.lib.playeranim.minecraft.api.PlayerAnimationFactory;
import com.ultra.megamod.lib.playeranim.core.PlayerAnimLib;
import com.ultra.megamod.lib.playeranim.core.animation.keyframe.event.CustomKeyFrameEvents;
import com.ultra.megamod.lib.playeranim.core.enums.PlayState;
import net.minecraft.resources.Identifier;

public class PlayerAnimLibMod extends PlayerAnimLib {
    public static final Identifier ANIMATION_LAYER_ID = PlayerAnimLibMod.id("factory");
    private static boolean initialized = false;

    public static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(MOD_ID, name);
    }

    public static void init() {
        if (initialized) return;
        initialized = true;

        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(ANIMATION_LAYER_ID, 1000,
                player -> new PlayerAnimationController(player, (c, s, a) -> PlayState.STOP)
        );
        CustomKeyFrameEvents.SOUND_KEYFRAME_EVENT.register(new AutoPlayingSoundKeyframeHandler());
        LOGGER.info("PlayerAnimationLib initialized");
    }
}
