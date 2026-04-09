package com.ultra.megamod.feature.citizen.blockui.controls;

import com.ultra.megamod.feature.citizen.blockui.BOGuiGraphics;
import com.ultra.megamod.feature.citizen.blockui.Pane;
import com.ultra.megamod.feature.citizen.blockui.PaneParams;
import com.ultra.megamod.feature.citizen.blockui.Parsers;
import com.ultra.megamod.feature.citizen.blockui.UiRenderMacros;
import com.ultra.megamod.feature.citizen.blockui.UiRenderMacros.ResolvedBlit;
import com.ultra.megamod.feature.citizen.blockui.util.records.SizeI;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Simple image element.
 */
public class Image extends Pane
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Image.class);

    protected Identifier resourceLocation = null;
    protected int u = 0;
    protected int v = 0;
    protected int uWidth = 0;
    protected int vHeight = 0;
    protected ResolvedBlit resolvedBlit = null;

    /**
     * Default Constructor.
     */
    public Image()
    {
        super();
    }

    /**
     * Constructor used by the xml loader.
     *
     * @param params PaneParams loaded from the xml.
     */
    public Image(final PaneParams params)
    {
        super(params);

        params.applyShorthand("imageoffset", Parsers.INT, 2, a -> {
            u = a.get(0);
            v = a.get(1);
        });

        params.applyShorthand("imagesize", Parsers.INT, 2, a -> {
            uWidth = a.get(0);
            vHeight = a.get(1);
        });

        resourceLocation = params.getResource("source");
    }

    /**
     * Set the image.
     *
     * @param rl      Identifier for the image.
     * @param u       image x offset.
     * @param v       image y offset.
     * @param uWidth  image width.
     * @param vHeight image height.
     */
    public void setImage(final Identifier rl, final int u, final int v, final int uWidth, final int vHeight)
    {
        if (Objects.equals(rl, resourceLocation) && this.u == u && this.v == v && this.uWidth == uWidth && this.vHeight == vHeight)
        {
            return;
        }

        this.resourceLocation = rl;
        this.u = u;
        this.v = v;
        this.uWidth = uWidth;
        this.vHeight = vHeight;
        this.resolvedBlit = null;
    }

    /**
     * Set the image.
     *
     * @param rl     Identifier for the image.
     * @param keepUv whether to keep previous u and v values or use full size
     */
    public void setImage(final Identifier rl, final boolean keepUv)
    {
        if (keepUv)
        {
            setImage(rl, u, v, uWidth, vHeight);
        }
        else
        {
            setImage(rl, 0, 0, 0, 0);
        }
    }

    /**
     * Draw this image on the GUI.
     *
     * @param mx Mouse x (relative to parent)
     * @param my Mouse y (relative to parent)
     */
    @Override
    public void drawSelf(final BOGuiGraphics target, final double mx, final double my)
    {
        // Dev-only null check (always runs)
        Objects.requireNonNull(resourceLocation, () -> "Missing image source: " + id + " | " + window.getXmlResourceLocation());

        if (resolvedBlit == null)
        {
            resolvedBlit = resolveBlit(resourceLocation, u, v, uWidth, vHeight);
        }

        // Blend state management removed in 1.21.11 - handled by RenderPipeline
        resolvedBlit.blit(target.pose(), x, y, width, height);
    }

    /**
     * @param resLoc texture Identifier
     * @return resolved blit - with precomputed values and detached from all possible instances
     */
    public static ResolvedBlit resolveBlit(final Identifier resLoc)
    {
        return resolveBlit(resLoc, 0, 0, 0, 0);
    }

    /**
     * @param resLoc texture Identifier
     * @param u in texels
     * @param v in texels
     * @param uWidth in texels
     * @param vHeight in texels
     * @return resolved blit - with precomputed values and detached from all possible instances
     */
    public static ResolvedBlit resolveBlit(final Identifier resLoc, final int u, final int v, final int uWidth, final int vHeight)
    {
        // if bad input skip resolving
        if (resLoc == null || resLoc == MissingTextureAtlasSprite.getLocation())
        {
            return (ps, x, y, w, h) -> blit(ps, MissingTextureAtlasSprite.getLocation(), x, y, w, h);
        }

        // For vanilla-style sprite references (like widget/button, widget/button_highlighted),
        // convert to a texture path. In 1.21.11, GUI sprites are accessed differently.
        // For most BlockUI usage, textures are direct paths (megamod:textures/gui/...).
        // For vanilla button sprites, try to resolve via the texture path convention.
        Identifier texturePath = resLoc;
        if (!resLoc.getPath().startsWith("textures/"))
        {
            // Sprite name like "widget/button" — convert to sprite texture path
            texturePath = Identifier.fromNamespaceAndPath(resLoc.getNamespace(),
                "textures/gui/sprites/" + resLoc.getPath() + ".png");
        }

        // Direct texture path (e.g. megamod:textures/gui/townhall_book.png)
        if (u == 0 && v == 0 && uWidth == 0 && vHeight == 0)
        {
            final Identifier finalPath = texturePath;
            return (ps, x, y, w, h) -> blit(ps, finalPath, x, y, w, h);
        }

        // Texture with UV offset/size - compute UV from texture dimensions
        final Identifier finalPath2 = texturePath;
        return (ps, x, y, w, h) -> {
            // Use a reasonable default for texture dimensions: most GUI textures are 256x256
            final int mapW = 256;
            final int mapH = 256;
            final float uMin = u / (float) mapW;
            final float uMax = uWidth == 0 ? 1.0f : (u + uWidth) / (float) mapW;
            final float vMin = v / (float) mapH;
            final float vMax = vHeight == 0 ? 1.0f : (v + vHeight) / (float) mapH;
            blit(ps, finalPath2, x, y, w, h, uMin, vMin, uMax, vMax);
        };
    }
}
