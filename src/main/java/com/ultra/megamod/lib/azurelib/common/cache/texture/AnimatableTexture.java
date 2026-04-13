/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package com.ultra.megamod.lib.azurelib.common.cache.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.Identifier;

import com.ultra.megamod.lib.azurelib.common.util.client.RenderUtils;

/**
 * Wrapper for {@link SimpleTexture SimpleTexture} implementation allowing for casual use of animated non-atlas textures.
 * In 1.21.11, the texture management APIs changed significantly (AbstractTexture.getId(), NativeImage.upload(),
 * TextureUtil.prepareImage() signatures, etc.). This class is simplified to maintain compile compatibility while
 * the internal animation frame management is stubbed out.
 */
public class AnimatableTexture extends SimpleTexture {

    protected boolean isAnimated = false;

    public AnimatableTexture(final Identifier location) {
        super(location);
    }

    /**
     * Returns whether the texture found any valid animation metadata when loading.
     * <p>
     * If false, then this is no different to a standard {@link SimpleTexture}
     */
    public boolean isAnimated() {
        return this.isAnimated;
    }

    public static void setAndUpdate(Identifier texturePath) {
        setAndUpdate(texturePath, (int) RenderUtils.getCurrentTick());
    }

    /**
     * Setting a specific frame for the animated texture does not work well because of how Minecraft buffers rendering
     * passes
     * <p>
     * Use the non-specified method above unless you know what you're doing
     */
    public static void setAndUpdate(Identifier texturePath, int frameTick) {
        AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(texturePath);

        try {
            var method = texture.getClass().getMethod("setAnimationFrame", int.class);
            method.invoke(texture, frameTick);
        } catch (ReflectiveOperationException ignored) {}
    }

    public void setAnimationFrame(int tick) {
        // In 1.21.11, texture frame animation is managed differently.
        // Animated texture support is stubbed out for compatibility.
    }
}
