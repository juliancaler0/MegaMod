package com.ultra.megamod.lib.etf.features.texture_handlers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ultra.megamod.lib.etf.ETF;
import com.ultra.megamod.lib.etf.config.ETFConfig;
import com.ultra.megamod.lib.etf.features.ETFManager;
import com.ultra.megamod.lib.etf.features.ETFRenderContext;
import com.ultra.megamod.lib.etf.utils.ETFUtils2;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/**
 * Armor + trim emissive/variant helper, ported from upstream {@code ETFArmorHandler}.
 * <p>
 * Instances are held per-entity so the {@link #renderTrimEmissive(PoseStack, MultiBufferSource, Model)}
 * can walk the resolved trim texture at the end of the armor rendering pass and render
 * an emissive overlay for it.
 */
@SuppressWarnings("unused")
public class ETFArmorHandler {

    private ETFTexture trimTexture = null;

    public void start() {
        ETFRenderContext.allowTexturePatching();
    }

    public void end() {
        ETFRenderContext.preventTexturePatching();
    }

    public void renderTrimEmissive(final PoseStack matrices, final MultiBufferSource buffers, final Model model) {
        ETFConfig cfg = ETF.config().getConfig();
        if (trimTexture != null && cfg.canDoEmissiveTextures()) {
            Identifier emissive = trimTexture.getEmissiveIdentifierOfCurrentState();
            if (emissive != null) {
                ETFRenderContext.startSpecialRenderOverlayPhase();
                RenderType type = RenderTypes.armorCutoutNoCull(emissive);
                VertexConsumer vc = buffers.getBuffer(type);
                matrices.scale(1.001f, 1.001f, 1.001f); // inflate to avoid z-fighting
                model.renderToBuffer(matrices, vc, ETF.EMISSIVE_FEATURE_LIGHT_VALUE, OverlayTexture.NO_OVERLAY);
                ETFRenderContext.endSpecialRenderOverlayPhase();
            }
        }
        trimTexture = null;
    }

    public void setTrim(Identifier trimBaseId) {
        if (ETF.config().getConfig().enableArmorAndTrims) {
            Identifier trimMaterialIdentifier = ETFUtils2.res(trimBaseId.getNamespace(),
                    "textures/" + trimBaseId.getPath() + ".png");
            ETFTexture trim = ETFManager.getInstance().getETFTextureNoVariation(trimMaterialIdentifier);
            if (trim.isEmissive()) trimTexture = trim;
        }
    }
}
