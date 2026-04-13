package com.ultra.megamod.lib.azurelib.common.cache.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import com.ultra.megamod.lib.azurelib.common.platform.Services;

public abstract class AzAbstractTexture extends SimpleTexture {

    private static final ConcurrentHashMap<String, RenderType> GLOWING_RENDER_TYPE_CACHE = new ConcurrentHashMap<>();

    protected static final String APPENDIX = "_glowmask";

    public AzAbstractTexture(Identifier location) {
        super(location);
    }

    public static void onRenderThread(Runnable renderCall) {
        if (!RenderSystem.isOnRenderThread()) {
            Minecraft.getInstance().execute(renderCall);
        } else {
            renderCall.run();
        }
    }

    /**
     * Generates the texture instance for the given path with the given appendix if it hasn't already been generated
     */
    protected static void generateTexture(
        Identifier texturePath,
        Consumer<TextureManager> textureManagerConsumer
    ) {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();

        if (!(textureManager.getTexture(texturePath) instanceof AzAbstractTexture))
            textureManagerConsumer.accept(textureManager);
    }

    /**
     * No-frills helper method for uploading {@link NativeImage images} into memory for use.
     * In 1.21.11, NativeImage.upload() and TextureUtil.prepareImage() signatures changed.
     * This is stubbed out for compatibility.
     */
    public static void uploadSimple(int texture, NativeImage image, boolean blur, boolean clamp) {
        // In 1.21.11, the texture upload API changed significantly.
        // This method is a no-op stub for compatibility.
    }

    public static Identifier appendToPath(Identifier location, String suffix) {
        String path = location.getPath();
        int i = path.lastIndexOf('.');

        return Identifier.fromNamespaceAndPath(
            location.getNamespace(),
            path.substring(0, i) + suffix + path.substring(i)
        );
    }

    /**
     * Debugging function to write out the generated glowmap image to disk
     */
    protected void printDebugImageToDisk(Identifier id, NativeImage newImage) {
        try {
            File file = new File(Services.PLATFORM.getGameDir().toFile(), "GeoTexture Debug Printouts");

            if (!file.exists()) {
                file.mkdirs();
            } else if (!file.isDirectory()) {
                file.delete();
                file.mkdirs();
            }

            file = new File(file, id.getPath().replace('/', '.'));

            if (!file.exists())
                file.createNewFile();

            newImage.writeToFile(file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Get the emissive resource equivalent of the input resource path.<br>
     * Additionally prepares the texture manager for the missing texture if the resource is not present
     *
     * @return The glowlayer resourcepath for the provided input path
     */
    public static Identifier getEmissiveResource(Identifier baseResource) {
        Identifier path = appendToPath(baseResource, APPENDIX);

        generateTexture(
            path,
            textureManager -> textureManager.register(path, new AutoGlowingTexture(baseResource, path))
        );

        return path;
    }

    /**
     * Return a cached instance of the RenderType for the given texture for GeoGlowingLayer rendering.
     */
    public static RenderType getRenderType(Identifier texture) {
        return getGlowingRenderType(getEmissiveResource(texture), false);
    }

    /**
     * Return a cached instance of the RenderType for the given texture for AutoGlowingGeoLayer rendering, while the
     * entity has an outline
     */
    public static RenderType getOutlineRenderType(Identifier texture) {
        return getGlowingRenderType(getEmissiveResource(texture), true);
    }

    private static RenderType getGlowingRenderType(Identifier texture, boolean isOutline) {
        String key = texture.toString() + "_" + isOutline;
        return GLOWING_RENDER_TYPE_CACHE.computeIfAbsent(key, k ->
            RenderTypes.entityTranslucentEmissive(texture)
        );
    }
}
