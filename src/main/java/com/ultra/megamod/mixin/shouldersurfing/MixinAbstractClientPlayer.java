package com.ultra.megamod.mixin.shouldersurfing;

import com.ultra.megamod.feature.shouldersurfing.api.model.Perspective;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(AbstractClientPlayer.class)
public class MixinAbstractClientPlayer
{
	@ModifyVariable
	(
		method = "getFieldOfViewModifier",
		at = @At("HEAD"),
		index = 1,
		argsOnly = true,
		require = 0
	)
	private boolean shouldersurfing$isFirstPerson(boolean isFirstPerson)
	{
		return isFirstPerson || Perspective.SHOULDER_SURFING == Perspective.current();
	}
}
