package com.ultra.megamod.lib.playeranim.minecraft.util;

import com.ultra.megamod.lib.playeranim.minecraft.accessors.IAnimatedAvatar;
import com.ultra.megamod.lib.playeranim.core.api.firstPerson.FirstPersonMode;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;

/**
 * Helper class for segregating client-side code
 */
public final class ClientUtil {
	/**
	 * Get the player on the client
	 */
	public static LocalPlayer getClientPlayer() {
		return Minecraft.getInstance().player;
	}

	/**
	 * Gets the current level on the client
	 */
	public static Level getLevel() {
		return Minecraft.getInstance().level;
	}

	@ApiStatus.Internal
	public static boolean shouldBeFirstPersonPass() {
		return shouldBeFirstPersonPass(Minecraft.getInstance().gameRenderer.getMainCamera());
	}

	@ApiStatus.Internal
	public static boolean shouldBeFirstPersonPass(Camera camera) {
		return !camera.isDetached() && camera.entity() instanceof IAnimatedAvatar player && player.playerAnimLib$getAnimManager().isActive()
				&& player.playerAnimLib$getAnimManager().getFirstPersonMode() == FirstPersonMode.THIRD_PERSON_MODEL
				&& (!(camera.entity() instanceof LivingEntity) || !((LivingEntity)camera.entity()).isSleeping());
	}
}
