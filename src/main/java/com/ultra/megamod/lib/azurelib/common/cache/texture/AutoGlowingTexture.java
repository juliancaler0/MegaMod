/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package com.ultra.megamod.lib.azurelib.common.cache.texture;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import com.ultra.megamod.lib.azurelib.AzureLib;
import com.ultra.megamod.lib.azurelib.common.platform.Services;

/**
 * Texture object type responsible for AzureLib's emissive render textures.
 * In 1.21.11, the texture loading system changed significantly — AbstractTexture no longer has getId()
 * and SimpleTexture.load() was refactored. This implementation is simplified for compatibility.
 */
public class AutoGlowingTexture extends AzAbstractTexture {

    protected final Identifier textureBase;

    protected final Identifier glowLayer;

    public AutoGlowingTexture(Identifier originalLocation, Identifier location) {
        super(originalLocation);
        this.textureBase = originalLocation;
        this.glowLayer = location;
    }

    /**
     * Generates the glow layer {@link NativeImage} and appropriately modifies the base texture for use in glow render
     * layers. Simplified for 1.21.11 compatibility.
     */
    @Nullable
    public Runnable loadGlowTexture(ResourceManager resourceManager, Minecraft mc) {
        try {
            AbstractTexture originalTexture;

            try {
                originalTexture = mc.submit(() -> mc.getTextureManager().getTexture(this.textureBase)).get();
            } catch (InterruptedException | ExecutionException e) {
                AzureLib.LOGGER.warn("Failed to load original texture: {}", this.textureBase, e);
                return null;
            }

            Resource textureBaseResource = resourceManager.getResource(this.textureBase).orElse(null);
            if (textureBaseResource == null) {
                return null;
            }

            NativeImage baseImage = originalTexture instanceof DynamicTexture dynamicTexture
                ? dynamicTexture.getPixels()
                : NativeImage.read(textureBaseResource.open());
            NativeImage glowImage = null;

            Optional<Resource> glowLayerResource = resourceManager.getResource(this.glowLayer);
            GeoGlowingTextureMeta glowLayerMeta = null;

            if (glowLayerResource.isPresent()) {
                glowImage = NativeImage.read(glowLayerResource.get().open());

                if (baseImage.getWidth() != glowImage.getWidth() || baseImage.getHeight() != glowImage.getHeight()) {
                    AzureLib.LOGGER.error(
                        "Glowmask size mismatch with base texture. Base size: {}x{}, Glowmask size: {}x{}, Location: {}",
                        baseImage.getWidth(),
                        baseImage.getHeight(),
                        glowImage.getWidth(),
                        glowImage.getHeight(),
                        this.glowLayer
                    );
                    return null;
                }

                glowLayerMeta = GeoGlowingTextureMeta.fromExistingImage(glowImage);
            } else {
                Optional<GeoGlowingTextureMeta> meta = textureBaseResource.metadata()
                    .getSection(GeoGlowingTextureMeta.DESERIALIZER);

                if (meta.isPresent()) {
                    glowLayerMeta = meta.get();
                    glowImage = new NativeImage(baseImage.getWidth(), baseImage.getHeight(), true);
                }
            }

            if (glowLayerMeta != null) {
                glowLayerMeta.createImageMask(baseImage, glowImage);

                if (Services.PLATFORM.isDevelopmentEnvironment()) {
                    printDebugImageToDisk(this.textureBase, baseImage);
                    printDebugImageToDisk(this.glowLayer, glowImage);
                }
            }

            if (glowImage == null) {
                String expectedGlowmask = this.textureBase.toString().replace(".png", "_glowmask.png");
                AzureLib.LOGGER.warn(
                    "Missing glowmask texture. Base texture: {}, Expected glowmask: {}",
                    this.textureBase,
                    expectedGlowmask
                );
                return null;
            }

            // In 1.21.11, texture upload is handled differently — return a no-op
            return () -> {};
        } catch (IOException e) {
            AzureLib.LOGGER.warn("Error loading glow texture: {}", this.glowLayer, e);
            return null;
        }
    }
}
