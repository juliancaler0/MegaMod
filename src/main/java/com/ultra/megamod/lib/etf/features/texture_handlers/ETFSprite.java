package com.ultra.megamod.lib.etf.features.texture_handlers;

import com.mojang.blaze3d.platform.NativeImage;
import com.ultra.megamod.lib.etf.utils.ETFUtils2;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Atlas-sprite wrapper used by the painting mixin (and anything else that renders from
 * a sprite atlas rather than a raw texture identifier).
 * <p>
 * Ported 1:1 from upstream ETF's {@code ETFSprite}. Given the original vanilla sprite +
 * our resolved {@link ETFTexture}, builds a variant sprite from the ETF texture's
 * identifier and (if the ETF texture is emissive) a companion emissive sprite.
 */
public class ETFSprite {

    public final boolean isETFAltered;
    private final TextureAtlasSprite sprite;
    private final @Nullable TextureAtlasSprite emissiveSprite;

    public ETFSprite(@NotNull TextureAtlasSprite originalSprite, @NotNull ETFTexture etfTexture, boolean isNotVariant) {
        if (isNotVariant) {
            this.sprite = originalSprite;
            this.isETFAltered = false;
        } else {
            Identifier variantId = etfTexture.getTextureIdentifier(null);
            Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(variantId);
            TextureAtlasSprite possibleVariant = null;
            if (resource.isPresent()) {
                try (SpriteContents contents = load(ETFUtils2.res(variantId + "-etf_sprite"), resource.get())) {
                    if (contents != null) {
                        possibleVariant = buildSprite(variantId, contents);
                    }
                }
            }
            this.sprite = Objects.requireNonNullElse(possibleVariant, originalSprite);
            this.isETFAltered = possibleVariant != null;
        }

        TextureAtlasSprite possibleEmissive = null;
        if (etfTexture.eSuffix != null) {
            Identifier emissiveId = etfTexture.getEmissiveIdentifierOfCurrentState();
            if (emissiveId != null) {
                Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(emissiveId);
                if (resource.isPresent()) {
                    try (SpriteContents contents = load(ETFUtils2.res(emissiveId + "-etf_sprite"), resource.get())) {
                        if (contents != null) {
                            possibleEmissive = buildSprite(emissiveId, contents);
                        }
                    }
                }
            }
        }
        this.emissiveSprite = possibleEmissive;
    }

    /**
     * Instantiate a {@link TextureAtlasSprite} via reflection — the constructor is
     * protected in 1.21.11 and we want to stay independent of access-widener quirks.
     */
    @Nullable
    private static TextureAtlasSprite buildSprite(Identifier id, SpriteContents contents) {
        try {
            for (Constructor<?> c : TextureAtlasSprite.class.getDeclaredConstructors()) {
                if (c.getParameterCount() == 7) {
                    c.setAccessible(true);
                    return (TextureAtlasSprite) c.newInstance(id, contents, contents.width(), contents.height(), 0, 0, 0);
                }
            }
            for (Constructor<?> c : TextureAtlasSprite.class.getDeclaredConstructors()) {
                if (c.getParameterCount() == 6) {
                    c.setAccessible(true);
                    return (TextureAtlasSprite) c.newInstance(id, contents, contents.width(), contents.height(), 0, 0);
                }
            }
        } catch (ReflectiveOperationException e) {
            // fall through
        }
        return null;
    }

    @Nullable
    public static SpriteContents load(Identifier id, Resource resource) {
        ResourceMetadata metadata;
        try {
            metadata = resource.metadata();
        } catch (Exception e) {
            return null;
        }

        NativeImage image;
        try (InputStream in = resource.open()) {
            image = NativeImage.read(in);
        } catch (IOException e) {
            return null;
        }

        FrameSize frameSize = new FrameSize(image.getWidth(), image.getHeight());
        if (!Mth.isMultipleOf(image.getWidth(), frameSize.width())
                || !Mth.isMultipleOf(image.getHeight(), frameSize.height())) {
            image.close();
            return null;
        }

        try {
            return new SpriteContents(id, frameSize, image,
                    metadata.getSection(AnimationMetadataSection.TYPE),
                    metadata.getTypedSections(List.of(AnimationMetadataSection.TYPE)),
                    Optional.empty());
        } catch (NoSuchMethodError | Exception e) {
            image.close();
            return null;
        }
    }

    @NotNull
    public TextureAtlasSprite getSpriteVariant() {
        return sprite;
    }

    @Nullable
    public TextureAtlasSprite getEmissive() {
        return emissiveSprite;
    }

    public boolean isEmissive() {
        return emissiveSprite != null;
    }
}
