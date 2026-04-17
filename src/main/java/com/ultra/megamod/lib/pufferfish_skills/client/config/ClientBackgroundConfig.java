package com.ultra.megamod.lib.pufferfish_skills.client.config;

import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.pufferfish_skills.common.BackgroundPosition;

public record ClientBackgroundConfig(
		Identifier texture,
		int width,
		int height,
		BackgroundPosition position
) {
	/**
	 * In 1.21.11, {@link net.minecraft.client.renderer.texture.TextureManager#getTexture(Identifier)}
	 * will create AND synchronously load a {@link net.minecraft.client.renderer.texture.SimpleTexture}
	 * on first blit if the identifier is not already registered. That synchronous
	 * {@code registerAndLoad} path is exactly what we want for datapack-provided textures — it reads
	 * the PNG from {@code assets/<ns>/<path>} and uploads it to the GPU before blit completes.
	 *
	 * <p>The previous implementation pre-registered a fresh {@code SimpleTexture} under a random id
	 * using {@code register(id, tex)} — which adds the texture to the internal map <em>without</em>
	 * triggering a load. Then blit's {@code getTexture(id)} would find the unloaded texture in the
	 * map, return it, and throw "Texture view does not exist" when {@code getTextureView()} was
	 * called on it, because {@code doLoad()} was never invoked. Skipping the pre-register lets
	 * {@code getTexture} take the happy path.
	 */
	public static ClientBackgroundConfig create(
			Identifier textureId,
			int width,
			int height,
			BackgroundPosition position
	) {
		return new ClientBackgroundConfig(
				textureId,
				width,
				height,
				position
		);
	}
}
