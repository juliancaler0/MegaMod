package com.ultra.megamod.lib.azurelib.common.animation.dispatch.command.action.impl.controller;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import com.ultra.megamod.lib.azurelib.AzureLib;
import com.ultra.megamod.lib.azurelib.common.animation.AzAnimator;
import com.ultra.megamod.lib.azurelib.common.animation.dispatch.AzDispatchSide;
import com.ultra.megamod.lib.azurelib.common.animation.dispatch.command.action.AzAction;

public record AzControllerSetReverseAction(
    String controllerName,
    boolean hasReverse
) implements AzAction {

    public static final StreamCodec<FriendlyByteBuf, AzControllerSetReverseAction> CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8,
        AzControllerSetReverseAction::controllerName,
        ByteBufCodecs.BOOL,
        AzControllerSetReverseAction::hasReverse,
        AzControllerSetReverseAction::new
    );

    public static final Identifier RESOURCE_LOCATION = AzureLib.modResource(
        "controller/set_reverse_tick_offset"
    );

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?, ?> animator) {
        var controller = animator.getAnimationControllerContainer().getOrNull(controllerName);

        if (controller != null) {
            controller.setAnimationProperties(
                controller.animationProperties().withShouldReverse(hasReverse)
            );
        }
    }

    @Override
    public Identifier getIdentifier() {
        return RESOURCE_LOCATION;
    }
}
