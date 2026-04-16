package com.ultra.megamod.lib.pufferfish_skills.client.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.pufferfish_skills.SkillsMod;
import com.ultra.megamod.lib.pufferfish_skills.common.BackgroundPosition;
import org.apache.commons.lang3.RandomStringUtils;

public record ClientBackgroundConfig(
		Identifier texture,
		int width,
		int height,
		BackgroundPosition position
) {
	public static ClientBackgroundConfig create(
			Identifier textureId,
			int width,
			int height,
			BackgroundPosition position
	) {
		var id = SkillsMod.createIdentifier(RandomStringUtils.random(16, "abcdefghijklmnopqrstuvwxyz0123456789"));
		Minecraft.getInstance().execute(() -> {
			var texture = new SimpleTexture(textureId);
			Minecraft.getInstance()
					.getTextureManager()
					.register(id, texture);
		});

		return new ClientBackgroundConfig(
				id,
				width,
				height,
				position
		);
	}
}
