package com.ultra.megamod.lib.azurelib.common.animation.dispatch.command.action.impl.root;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import com.ultra.megamod.lib.azurelib.AzureLib;
import com.ultra.megamod.lib.azurelib.common.animation.AzAnimator;
import com.ultra.megamod.lib.azurelib.common.animation.dispatch.AzDispatchSide;
import com.ultra.megamod.lib.azurelib.common.animation.dispatch.command.action.AzAction;

/**
 * Represents an action that sets the transition speed for all animation controllers within the associated
 * {@link AzAnimator}. This class is a record type, encapsulating a {@code float} value representing the transition
 * speed.
 */
public record AzRootSetTransitionSpeedAction(
    float transitionSpeed
) implements AzAction {

    public static final StreamCodec<FriendlyByteBuf, AzRootSetTransitionSpeedAction> CODEC = StreamCodec.composite(
        ByteBufCodecs.FLOAT,
        AzRootSetTransitionSpeedAction::transitionSpeed,
        AzRootSetTransitionSpeedAction::new
    );

    public static final Identifier RESOURCE_LOCATION = AzureLib.modResource("root/set_transition_speed");

    @Override
    public void handle(AzDispatchSide originSide, AzAnimator<?, ?> animator) {
        animator.getAnimationControllerContainer()
            .getAll()
            .forEach(
                controller -> controller.setAnimationProperties(
                    controller.animationProperties().withTransitionLength(transitionSpeed)
                )
            );
    }

    @Override
    public Identifier getIdentifier() {
        return RESOURCE_LOCATION;
    }
}
