package com.ultra.megamod.mixin.pufferfish_skills;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBuffers;
import com.ultra.megamod.lib.pufferfish_skills.access.MinecraftClientAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Minecraft.class)
public class MinecraftClientMixin implements MinecraftClientAccess {
	@Shadow
	@Final
	private RenderBuffers renderBuffers;

	@Override
	public RenderBuffers getBufferBuilders() {
		return renderBuffers;
	}
}
