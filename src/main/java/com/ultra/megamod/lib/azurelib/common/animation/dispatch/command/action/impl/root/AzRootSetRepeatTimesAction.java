package com.ultra.megamod.lib.azurelib.common.animation.dispatch.command.action.impl.root;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import com.ultra.megamod.lib.azurelib.AzureLib;
import com.ultra.megamod.lib.azurelib.common.animation.AzAnimator;
import com.ultra.megamod.lib.azurelib.common.animation.dispatch.AzDispatchSide;
import com.ultra.megamod.lib.azurelib.common.animation.dispatch.command.action.AzAction;

public record AzRootSetRepeatTimesAction(
    double repeatXTimes
) implements AzAction {

    public static final StreamCodec<FriendlyByteBuf, AzRootSetRepeatTimesAction> CODEC = StreamCodec.composite(
        ByteBufCodecs.DOUBLE,
        AzRootSetRepeatTimesAction::repeatXTimes,
        AzRootSetRepeatTimesAction::new
    );

    public static final Identifier RESOURCE_LOCATION = AzureLib.modResource("root/set_repeat_times_tick_offset");

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?, ?> animator) {
        animator.getAnimationControllerContainer()
            .getAll()
            .forEach(
                controller -> controller.setAnimationProperties(
                    controller.animationProperties().withRepeatXTimes(repeatXTimes)
                )
            );
    }

    @Override
    public Identifier getIdentifier() {
        return RESOURCE_LOCATION;
    }
}
